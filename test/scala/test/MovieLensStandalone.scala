package demo.test


import java.io.{File, PrintWriter}

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession

import scala.io.Source
import scala.math._


object MovieLensStandalone {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .appName("MovieLens")
      .getOrCreate()
    val sc: SparkContext = spark.sparkContext

    val trainSetFile = "/Users/marme/Downloads/ml-1m/ub_1m.base.shuffle"
    val testSetFile = "/Users/marme/Downloads/ml-1m/ub_1m.test.shuffle"

    val MAX_ROUNDS: Long = 64

    val numUser = 6040
    // 6040//138493
    val numUserExtraInfo = 0
    val numItem = 3952
    //3952//131262
    val numItemExtraInfo = 0

    //Average score of train set
    var avgScore: Double = 0.0
    var cnt: Long = 0
    for (line <- Source.fromFile(trainSetFile).getLines) {
      val temp = line.split("\t")
      val score = temp(2).toDouble
      avgScore = avgScore * cnt / (cnt + 1.0f) + score / (cnt + 1.0f)
      cnt += 1
    }
    val total_train_ratings: Long = cnt
    println(s"AverageScore:${avgScore}")

    //单机版SGD
    val svdFeature = new SvdFeatureStandalone(
      baseScore = avgScore,
      numUser = numUser + numUserExtraInfo,
      numItem = numItem + numItemExtraInfo,
      dimFeatures = 64,
      dimGlobal = 0,
      dimUser = 1,
      dimItem = 1,
      _learningRate = 0.05,
      lambdaBiasUser = 0.1f,
      lambdaBiasItem = 0.1f,
      lambdaBiasGlobal = 0.1f,
      lambdaP = 0.1,
      lambdaQ = 0.1)
    var svdFeatureLoadedFromFile: SvdFeatureStandalone = null

    var bestSvdFeature: SvdFeatureStandalone = null

    val forceGen: Boolean = true //generate new models
    val removeOld: Boolean = false //remove old models
    if (removeOld) {
      for (rounds <- 0L to MAX_ROUNDS) {
        val filename = "/Users/marme/Downloads/ml-1m/scala_" + rounds.toString
        val f = new java.io.File(filename)
        if (f.exists()) f.delete()
      }
    }

    val totalTimeStart: Long = System.currentTimeMillis()
    var totalTrainTime: Long = 0
    var totalTestTime: Long = 0

    svdFeature.setGap(min(total_train_ratings, total_train_ratings - 2))
    for (rounds <- 0L to MAX_ROUNDS) {
      //check force model/model exists
      var loaded: Boolean = false
      val filename = "/Users/marme/Downloads/ml-1m/scala_" + rounds.toString
      if (new File(filename).exists) {
        svdFeatureLoadedFromFile = Serialization.deserialize[SvdFeatureStandalone](filename)

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
        for (line <- Source.fromFile(trainSetFile).getLines) {
          cnt += 1
          //println(line)
          val temp = line.split("\t")
          val score: Double = temp(2).toDouble
          var numG = 0
          var numU = 1
          var numI = 1
          val uid = temp(0).toLong - 1
          val iid = temp(1).toLong - 1

          //println(score, numG, numU, numI, uid, iid, uv, iv)

          val tempAlpha = Array.fill[(Long, Double)](1)((uid, 1.0f))
          val tempBeta = Array.fill[(Long, Double)](1)((iid, 1.0f))
          val tempGamma = Array.fill[(Long, Double)](0)((uid, 1.0f))

          //println("Round:" + rounds.toString + " ")
          svdFeature.gradientDescent(score, tempAlpha, tempBeta, tempGamma)
        }
        val trainTimeStop = System.currentTimeMillis()
        totalTrainTime += trainTimeStop - trainTimeStart
        println(s"Train time:${trainTimeStop - trainTimeStart}")

        val testTimeStart = System.currentTimeMillis()
        cnt = 0
        var accErr: Double = 0.0f
        var RMSE: Double = 0.0f
        var MAP: Double = 0.0f
        for (line <- Source.fromFile(testSetFile).getLines) {
          cnt += 1
          //println(line)
          var temp = line.split("\t")
          var score = temp(2).toDouble
          var numG = 0
          var numU = 1
          var numI = 1
          var uid = temp(0).toInt - 1
          var iid = temp(1).toInt - 1
          //println(score, numG, numU, numI, uid, iid, uv, iv)

          val tempAlpha = Array.fill[(Long, Double)](1)((uid, 1.0f))
          val tempBeta = Array.fill[(Long, Double)](1)((iid, 1.0f))
          val tempGamma = Array.fill[(Long, Double)](0)((uid, 1.0f))

          var pred = svdFeature.predict(tempAlpha, tempBeta, tempGamma)
          var err = score - pred
          accErr += abs(err)
          RMSE += err * err
          var errToInt: Int = round(err).toInt
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


        /** Choose minimum RMSE */
        if (svdFeatureLoadedFromFile != null &&
          svdFeatureLoadedFromFile.rounds == rounds &&
          svdFeatureLoadedFromFile.rmseOnTest <= svdFeature.rmseOnTest) {
          //need_saving = false
          println("Round:" + rounds.toString + " uses saved model")
          Serialization.serialize[SvdFeatureStandalone](svdFeatureLoadedFromFile, filename)
        }
        else {
          println("Round:" + rounds.toString + " saves new model")
          Serialization.serialize[SvdFeatureStandalone](svdFeature, filename)
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
    println(s"TotalTrainTime:${totalTrainTime}")
    println(s"TotalTestTime:${totalTestTime}")

    //output prediction
    val filename_out = "/Users/marme/Downloads/ml-1m/pred_scala.txt"
    val out_writer = new PrintWriter(new File(filename_out))
    cnt = 0
    var accErr: Double = 0.0f
    var RMSE: Double = 0.0f
    for (line <- Source.fromFile(testSetFile).getLines) {
      cnt += 1
      val temp = line.split("\t")
      val score = temp(2).toDouble
      var numG = 0
      var numU = 1
      var numI = 1
      val uid = temp(0).toInt - 1
      val iid = temp(1).toInt - 1

      val tempAlpha = Array.fill[(Long, Double)](1)((uid, 1.0f))
      val tempBeta = Array.fill[(Long, Double)](1)((iid, 1.0f))
      val tempGamma = Array.fill[(Long, Double)](0)((uid, 1.0f))

      val pred = bestSvdFeature.predict(tempAlpha, tempBeta, tempGamma)
      val err = score - pred
      accErr += abs(err)
      RMSE += err * err

      def cat(str: String, maxLength: Int = 6): String = {
        if (str.length > 6) str.substring(0, 6)
        else str
      }

      out_writer.write(cat(score.toString) + "\t")
      out_writer.write(cat(pred.toString) + "\t")
      out_writer.write(cat(err.toString) + "\t")
      out_writer.write(cat((accErr / cnt).toString) + "\t")
      out_writer.write(cat(sqrt(RMSE / cnt).toString) + "\n")
    }
    out_writer.close()

    spark.stop()
  }
}