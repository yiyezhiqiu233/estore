package demo.test


import java.io.{File, PrintWriter}

import com.github.fommil.netlib.BLAS.{getInstance => blas}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.math._


object MovieLensStandaloneMiniBatch {
  def main(args: Array[String]): Unit = {
    val trainSetFile = "/Users/marme/Downloads/ml-1m/ub_1m.base.shuffle"
    val testSetFile = "/Users/marme/Downloads/ml-1m/ub_1m.test.shuffle"

    type InputRating = (Double, Array[(Long, Double)], Array[(Long, Double)], Array[(Long, Double)])

    val MAX_ROUNDS: Long = 128
    val BatchSize: Int = 10000

    val numUser = 6040
    // 6040//138493
    val numUserExtraInfo = 0
    val numItem = 3952
    //3952//131262
    val numItemExtraInfo = 0


    var avgScore: Double = 0.0
    var cnt: Long = 0
    for (line <- Source.fromFile(testSetFile).getLines) {
      //println(line)
      val temp = line.split("\t")
      val score = temp(2).toDouble
      avgScore = avgScore * cnt / (cnt + 1.0f) + score / (cnt + 1.0f)
      cnt += 1
    }
    val total_train_ratings: Long = 800000L
    println(s"AverageScore:$avgScore")


    val totalTimeStart: Long = System.currentTimeMillis()
    var totalTrainTime: Long = 0L
    var totalTestTime: Long = 0L

    val svdFeature = new SvdFeatureStandaloneMiniBatch(
      baseScore = avgScore,
      numUser = numUser + numUserExtraInfo,
      numItem = numItem + numItemExtraInfo,
      dimFeatures = 64,
      dimGlobal = 0,
      dimUser = 1,
      dimItem = 1,
      //matScore = matScore,
      _learningRate = 0.05,
      lambdaBiasUser = 0.1f,
      lambdaBiasItem = 0.1f,
      lambdaBiasGlobal = 0.1f,
      lambdaP = 0.1,
      lambdaQ = 0.1)
    var svdFeatureLoadedFromFile: SvdFeatureStandaloneMiniBatch = null

    var bestSvdFeature: SvdFeatureStandaloneMiniBatch = null

    val forceGen: Boolean = true
    val removeOld: Boolean = true
    if (removeOld) {
      for (rounds <- 0L to MAX_ROUNDS) {
        val filename = "/Users/marme/Downloads/ml-1m/scala_" + rounds.toString
        val f = new java.io.File(filename)
        if (f.exists()) f.delete()
      }
    }

    svdFeature.setGap(min(total_train_ratings, total_train_ratings - 2))
    for (rounds <- 0L to MAX_ROUNDS) {
      //check force model/model exists
      var loaded: Boolean = false
      val filename = "/Users/marme/Downloads/ml-1m/scala_" + rounds.toString
      if (new File(filename).exists) {
        svdFeatureLoadedFromFile = Serialization.deserialize[SvdFeatureStandaloneMiniBatch](filename)

        //compare
        if (svdFeatureLoadedFromFile.rounds == rounds) {
          //svdFeature = svdFeatureLoadedFromFile
          loaded = true
        }
      }

      if (!loaded || forceGen) {
        //train
        val trainTimeStart = System.currentTimeMillis()
        var cnt: Long = 0
        svdFeature.setRounds(rounds)
        val inputs: ArrayBuffer[InputRating] = new ArrayBuffer[InputRating]()
        for (line <- Source.fromFile(trainSetFile).getLines) {
          cnt += 1
          //println(line)
          val temp = line.split("\t")
          val score: Double = temp(2).toDouble
          val numGlobal = 0
          val numUser = 1
          val numItem = 1
          val uid = temp(0).toLong - 1
          val iid = temp(1).toLong - 1

          //println(score, numG, numU, numI, uid, iid, uv, iv)

          val tempAlpha = Array.fill[(Long, Double)](numUser)((uid, 1.0f))
          val tempBeta = Array.fill[(Long, Double)](numItem)((iid, 1.0f))
          val tempGamma = Array.fill[(Long, Double)](numGlobal)((uid, 1.0f))

          inputs.append((score, tempAlpha, tempBeta, tempGamma))

          if (inputs.length >= BatchSize) {
            svdFeature.gradientDescent(inputs.toArray)
            inputs.clear()
          }
        }
        if (inputs.nonEmpty) {
          svdFeature.gradientDescent(inputs.toArray)
          inputs.clear()
        }
        val trainTimeStop = System.currentTimeMillis()
        totalTrainTime += trainTimeStop - trainTimeStart
        println(s"Train time:${trainTimeStop - trainTimeStart}")

        val testTimeStart = System.currentTimeMillis()
        cnt = 0
        var accErr: Double = 0.0f
        var RMSE: Double = 0.0f
        for (line <- Source.fromFile(testSetFile).getLines) {
          cnt += 1
          //println(line)
          val temp = line.split("\t")
          val score = temp(2).toDouble
          val numGlobal = 0
          val numUser = 1
          val numItem = 1
          val uid = temp(0).toInt - 1
          val iid = temp(1).toInt - 1
          //println(score, numG, numU, numI, uid, iid, uv, iv)

          val tempAlpha = Array.fill[(Long, Double)](numUser)((uid, 1.0f))
          val tempBeta = Array.fill[(Long, Double)](numItem)((iid, 1.0f))
          val tempGamma = Array.fill[(Long, Double)](numGlobal)((uid, 1.0f))

          val prediction = svdFeature.predict(tempAlpha, tempBeta, tempGamma)
          val err: Double = score - prediction
          accErr += abs(err)
          RMSE += err * err
        }
        RMSE /= cnt
        RMSE = sqrt(RMSE)
        accErr /= cnt
        svdFeature.rmseOnTest = RMSE
        svdFeature.avgErrorOnTest = accErr
        println("Round:" + rounds.toString + "\tAccErr-Test:" + accErr.toString + "\tRMSE-Test:" + RMSE.toString)
        val testTimeStop = System.currentTimeMillis()
        totalTestTime += testTimeStop - testTimeStart
        println(s"Test time:${testTimeStop - testTimeStart}")

        //var need_saving: Boolean = true
        if (svdFeatureLoadedFromFile != null &&
          svdFeatureLoadedFromFile.rounds == rounds &&
          svdFeatureLoadedFromFile.rmseOnTest <= svdFeature.rmseOnTest) {
          //need_saving = false
          println("Round:" + rounds.toString + " uses saved model")
          Serialization.serialize[SvdFeatureStandaloneMiniBatch](svdFeatureLoadedFromFile, filename)
        }
        else {
          println("Round:" + rounds.toString + " saves new model")
          Serialization.serialize[SvdFeatureStandaloneMiniBatch](svdFeature, filename)
          //bestSvdFeature=svdFeature
        }

        if (bestSvdFeature == null || bestSvdFeature.rmseOnTest > svdFeature.rmseOnTest)
          bestSvdFeature = svdFeature.copy()
        if (svdFeatureLoadedFromFile != null &&
          bestSvdFeature.rmseOnTest > svdFeatureLoadedFromFile.rmseOnTest)
          bestSvdFeature = svdFeatureLoadedFromFile.copy()
      }
    }

    println("BestModel:")
    println("Round:" + bestSvdFeature.rounds.toString)
    println("RMSE-Test:" + bestSvdFeature.rmseOnTest.toString)
    println("AccError-Test:" + bestSvdFeature.avgErrorOnTest.toString)
    println("RMSE-Train:" + (bestSvdFeature.rmseOnTrain / bestSvdFeature.times).toString)
    println("AccError-Train:" + (bestSvdFeature.avgErrorOnTrain / bestSvdFeature.times).toString)

    val totalTimeStop = System.currentTimeMillis()
    println(s"TotalTime:${totalTimeStop - totalTimeStart}")
    println(s"TotalTrainTime:$totalTrainTime")
    println(s"TotalTestTime:$totalTestTime")

    val filename_out = "/Users/marme/Downloads/ml-1m/pred_scala.txt"
    val out_writer = new PrintWriter(new File(filename_out))
    cnt = 0
    var accErr: Double = 0.0f
    var RMSE: Double = 0.0f
    for (line <- Source.fromFile(testSetFile).getLines) {
      cnt += 1
      //println(line)
      val temp = line.split("\t")
      val score = temp(2).toDouble
      val numGlobal = 0
      val numUser = 1
      val numItem = 1
      val uid = temp(0).toInt - 1
      val iid = temp(1).toInt - 1

      val tempAlpha = Array.fill[(Long, Double)](numUser)((uid, 1.0f))
      val tempBeta = Array.fill[(Long, Double)](numItem)((iid, 1.0f))
      val tempGamma = Array.fill[(Long, Double)](numGlobal)((uid, 1.0f))

      val prediction = bestSvdFeature.predict(tempAlpha, tempBeta, tempGamma)
      val err = score - prediction
      accErr += abs(err)
      RMSE += err * err

      def cat(str: String, maxLength: Int = 6): String = {
        if (str.length > 6) str.substring(0, 6)
        else str
      }

      out_writer.write(cat(score.toString) + "\t")
      out_writer.write(cat(prediction.toString) + "\t")
      out_writer.write(cat(err.toString) + "\t")
      out_writer.write(cat((accErr / cnt).toString) + "\t")
      out_writer.write(cat(sqrt(RMSE / cnt).toString) + "\n")
    }
    out_writer.close()
  }
}