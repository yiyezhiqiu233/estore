package demo.test


import java.io.{File, PrintWriter}

import breeze.linalg.DenseVector

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.math._
import scala.util.Random


object MovieLensBig {
  def t(): Unit = {
    var l_pred: ArrayBuffer[Double] = ArrayBuffer()
    var l_target: ArrayBuffer[Double] = ArrayBuffer()
    for (line <- Source.fromFile("/Users/marme/Downloads/ml-1m/pred.txt").getLines) {
      l_pred.append(line.toDouble)
    }
    for (line <- Source.fromFile("/Users/marme/Downloads/ml-1m/ua_1m.test.shuffle").getLines) {

      l_target.append(line.split("\t")(2).toDouble)
    }
    println(l_pred.length, l_target.length)
    val len = l_pred.length

    var accError: Double = 0.0f
    var RMSE: Double = 0.0f
    for (i <- 0 to len - 1) {
      accError += abs(l_pred(i) - l_target(i))
      RMSE += (l_pred(i) - l_target(i)) * (l_pred(i) - l_target(i))
    }
    println("AvgErr", accError / len)
    RMSE /= len
    RMSE = sqrt(RMSE)
    println("RMSE", RMSE)
  }

  def main(args: Array[String]): Unit = {
    //val spark = SparkSession.builder().appName("testKMeans").getOrCreate()
    //t()
    //return
    val trainSetFile = "/Users/marme/Downloads/ml-1m/ub_1m.base.shuffle"
    val testSetFile = "/Users/marme/Downloads/ml-1m/ub_1m.test.shuffle"

    val MAX_ROUNDS: Int = 32

    val numUser = 6040
    val numItem = 3952


    var avgScore: Double = 0.0
    var cnt: Int = 0
    for (line <- Source.fromFile(testSetFile).getLines) {
      //println(line)
      var temp = line.split("\t")
      var score = temp(2).toDouble
      avgScore = avgScore * cnt / (cnt + 1.0f) + score / (cnt + 1.0f)
      cnt += 1
    }
    var total_test_ratings: Int = cnt
    var total_train_ratings: Int = 800200

    var svdFeature = new SvdFeature3(
      baseScore = avgScore,
      numUser = numUser,
      numItem = numItem,
      dimFeatures = 64,
      dimGlobal = 0,
      dimUser = 1,
      dimItem = 1,
      //matScore = matScore,
      _learningRate = 0.005,
      lambdaBiasUser = 0.001f,
      lambdaBiasItem = 0.001f,
      lambdaBiasGlobal = 0.001f,
      lambdaP = 0.004,
      lambdaQ = 0.004)
    var svdFeatureLoadedFromFile: SvdFeature3 = null
    //svdFeature.setVectorAlpha(Array[Double](1, 1, 0, 0, 1))
    //svdFeature.setVectorAlpha(Array.fill(numUser)(Random.nextDouble()))
    //var t: Array[Double] = new Array[Double](numUser)
    //for (i <- 0 to numUser - 1) t(i) = i * 1.0f / numUser
    //svdFeature.setVectorAlpha(t)
    //svdFeature.setVectorBeta(Array[Double](1, 0, 0, 1))
    svdFeature.setVectorAlpha(Array.fill(numItem)(0))
    svdFeature.setVectorBeta(Array.fill(numItem)(0))
    svdFeature.setVectorGamma(Array.fill(0)(0))
    ////var t2: Array[Double] = new Array[Double](numItem)
    //for (i <- 0 to numItem - 1) t(i) = i * 1.0f / numItem
    //svdFeature.setVectorBeta(t2)

    var bestSvdFeature: SvdFeature3 = null

    var forceGen: Boolean = true
    var removeOld: Boolean = true
    if (removeOld) {
      for (rounds <- 0 to MAX_ROUNDS) {
        var filename = "/Users/marme/Downloads/ml-1m/scala_" + rounds.toString
        var f = new java.io.File(filename)
        if (f.exists()) f.delete()
      }
    }

    svdFeature.setGap(min(total_train_ratings, total_train_ratings - 2))
    for (rounds <- 0 to MAX_ROUNDS) {
      //check force model/model exists
      var loaded: Boolean = false
      var filename = "/Users/marme/Downloads/ml-1m/scala_" + rounds.toString
      if (new File(filename).exists) {
        svdFeatureLoadedFromFile = Serialization.deserialize[SvdFeature3](filename)

        //compare
        if (svdFeatureLoadedFromFile.rounds == rounds) {
          //svdFeature = svdFeatureLoadedFromFile
          loaded = true
        }
      }

      if (!loaded || forceGen) {
        var cnt: Int = 0
        svdFeature.setRounds(rounds)
        for (line <- Source.fromFile(trainSetFile).getLines) {
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

          svdFeature.vAlpha(uid) = 1.0f
          svdFeature.vBeta(iid) = 1.0f

          //println("Round:" + rounds.toString + " ")
          svdFeature.gradientDescent(score, uid, iid)
        }


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

          var pred = svdFeature.predict(uid, iid)
          var err = score - pred
          accErr += abs(err)
          RMSE += err * err
          var errToInt: Int = round(err).toInt
        }
        RMSE /= cnt
        RMSE = sqrt(RMSE)
        accErr /= cnt
        svdFeature.rmseOnTest = RMSE
        svdFeature.accErrorOnTest = accErr
        println("Round:" + rounds.toString + "\tAccErr-Test:" + accErr.toString + "\tRMSE-Test:" + RMSE.toString)

        //var need_saving: Boolean = true
        if (svdFeatureLoadedFromFile != null &&
          svdFeatureLoadedFromFile.rounds == rounds &&
          svdFeatureLoadedFromFile.rmseOnTest <= svdFeature.rmseOnTest) {
          //need_saving = false
          println("Round:" + rounds.toString + " uses saved model")
          Serialization.serialize[SvdFeature3](svdFeatureLoadedFromFile, filename)
        }
        else {
          println("Round:" + rounds.toString + " saves new model")
          Serialization.serialize[SvdFeature3](svdFeature, filename)
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
    println("AccError-Test:" + (bestSvdFeature.accErrorOnTest).toString)
    println("RMSE-Train:" + (bestSvdFeature.rmse / bestSvdFeature.times).toString)
    println("AccError-Train:" + (bestSvdFeature.accError / bestSvdFeature.times).toString)


    val filename_out = "/Users/marme/Downloads/ml-1m/pred_scala.txt"
    val out_writer = new PrintWriter(new File(filename_out))
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

      var pred = bestSvdFeature.predict(uid, iid)
      var err = score - pred
      accErr += abs(err)
      RMSE += err * err
      var errToInt: Int = round(err).toInt

      def cat(str: String, maxLength: Int = 6): String = {
        if (str.length > 6) str.substring(0, 6)
        else str
      }

      out_writer.write(cat(score.toString) + "\t")
      out_writer.write(cat(pred.toString) + "\t")
      out_writer.write(cat(err.toString) + "\t")
      out_writer.write(cat((accErr / cnt).toString) + "\t")
      out_writer.write(cat((sqrt(RMSE / cnt)).toString) + "\n")
    }
    out_writer.close()

    //spark.stop()
  }
}


@SerialVersionUID(39793743L)
class SvdFeature3(baseScore: Double,
                  numUser: Int,
                  numItem: Int,
                  dimFeatures: Int,
                  dimGlobal: Int,
                  dimUser: Int,
                  dimItem: Int,
                  //matScore: Matrix[Double],
                  //learning rate & lambdas 1-5
                  _learningRate: Double,
                  lambdaP: Double,
                  lambdaQ: Double,
                  lambdaBiasUser: Double,
                  lambdaBiasItem: Double,
                  lambdaBiasGlobal: Double,
                  arrAlpha: Array[Double] = Array.empty[Double],
                  arrBeta: Array[Double] = Array.empty[Double],
                  arrGamma: Array[Double] = Array.empty[Double]
                 ) extends Serializable {
  assert(numUser > 0, "Number of users:" + numUser.toString() + "<=0")
  assert(numItem > 0, "Number of items:" + numItem.toString() + "<=0")
  assert(dimFeatures > 0, "Number of features:" + dimFeatures.toString() + "<=0")
  assert(dimGlobal >= 0, "Number of global dims:" + dimGlobal.toString() + "<0")
  //assert(matScore.rows == numUser, "Number of users:" + numUser.toString() + " != matScore.numRows:" + matScore.rows)
  //assert(matScore.cols == numItem, "Number of items:" + numItem.toString() + " != matScore.numCols:" + matScore.cols)

  def copy(): SvdFeature3 = {
    var t = new SvdFeature3(baseScore = baseScore,
      numUser = numUser,
      numItem = numItem,
      dimFeatures = dimFeatures,
      dimGlobal = dimGlobal,
      dimUser = dimUser,
      dimItem = dimItem,
      _learningRate = learningRate,
      lambdaP = lambdaP,
      lambdaQ = lambdaQ,
      lambdaBiasGlobal = lambdaBiasGlobal,
      lambdaBiasUser = lambdaBiasUser,
      lambdaBiasItem = lambdaBiasItem)

    t.learningRate = this.learningRate
    t.rounds = this.rounds
    t.times = this.times
    t.gap = this.gap
    t.accError = this.accError
    t.accErrorOnTest = this.accErrorOnTest
    t.rmse = this.rmse
    t.rmseOnTest = this.rmseOnTest
    t.vAlpha = this.vAlpha
    t.vBeta = vBeta
    t.vGamma = vGamma
    t.mP = this.mP.clone()
    t.mQ = mQ.clone()
    t.vBiasUser = this.vBiasUser
    t.vBiasItem = this.vBiasItem
    t.vBiasGlobal = this.vBiasGlobal
    t
  }

  var learningRate = _learningRate
  var rounds = 0
  var times = 0
  var gap = 1
  var accError: Double = 0.0
  var accErrorOnTest: Double = 0.0
  var rmse: Double = 0.0
  var rmseOnTest: Double = 0.0

  var randomGenerator: Random = new Random(3979374L)

  def setGap(_gap: Int): Unit = {
    if (_gap > 0) gap = _gap
  }

  def setRounds(_rounds: Int): Unit = {
    if (rounds >= 0) {
      rounds = _rounds
      times = 0
      accError = 0.0f
      rmse = 0.0
      //learningRate *= (1 - 0.01f)
      if (learningRate < 0.001) learningRate = 0.001f
      println("LEARNING rate:", learningRate)
    }
  }

  def constructDVbyArray(array: Array[Double], length: Int): DenseVector[Double] = {
    assert(length >= 0, "constructDVbyArray failed with DV length:" + length.toString())
    if (array.length != length) {
      println("constructDVbyArray with new DV, origin array length:" + array.length + " != " length)
      new DenseVector(new Array[Double](length))
    } else {
      new DenseVector(array)
    }
  }

  //user
  var vAlpha: DenseVector[Double] = constructDVbyArray(arrAlpha, numUser)
  //randomize P matrix
  var mP: Array[DenseVector[Double]] = new Array[DenseVector[Double]](numUser)
  for (i <- 0 to numUser - 1) {
    mP(i) = new DenseVector[Double](Array.fill(dimFeatures)(randomGenerator.nextGaussian()))
    var acc: Double = 0.0f
    mP(i).data foreach (acc += _)
    acc /= mP(i).data.length
    for (j <- 0 to mP(i).data.length - 1) {
      mP(i).update(j, mP(i)(j) * 0.01f)
    }
    //acc = 0.0f
    //mP(i).data foreach (acc += _)
    //mP(i).data.foreach(x => x * 0.01f)
    //mP(i) = new DenseVector[Double](new Array[Double](dimFeatures))
  }
  //randomize biasUser
  //var vBiasUser: DenseVector[Double] = new DenseVector(Array.fill(numUser)(randomGenerator.nextDouble()))
  var vBiasUser: DenseVector[Double] = new DenseVector(new Array[Double](numUser))


  //item
  var vBeta: DenseVector[Double] = constructDVbyArray(arrBeta, numItem)
  var mQ: Array[DenseVector[Double]] = new Array[DenseVector[Double]](numItem)
  for (i <- 0 to numItem - 1) {
    mQ(i) = new DenseVector[Double](Array.fill(dimFeatures)(randomGenerator.nextGaussian()))
    //var acc: Double = 0.0f
    //mQ(i).data foreach (acc += _)
    //acc /= mQ(i).data.length
    for (j <- 0 to mQ(i).data.length - 1) {
      mQ(i).update(j, mQ(i)(j) * 0.01f)
    }
    //mQ(i).data.foreach(x => x - acc)
    //mQ(i).data.foreach(x => x * 0.01f)
    //mQ(i) = new DenseVector[Double](new Array[Double](dimFeatures))
  }
  //randomize biasItem
  //var vBiasItem: DenseVector[Double] = new DenseVector(Array.fill(numItem)(randomGenerator.nextGaussian()))
  var vBiasItem: DenseVector[Double] = new DenseVector(new Array[Double](numItem))

  //global
  var vGamma: DenseVector[Double] = constructDVbyArray(arrGamma, dimGlobal)
  //randomize biasGlobal
  var vBiasGlobal: DenseVector[Double] = new DenseVector(Array.fill(dimGlobal)(randomGenerator.nextGaussian() * 0.1f))

  def setVectorAlpha(array: Array[Double]): Unit = {
    vAlpha = constructDVbyArray(array, numUser)
  }

  def setVectorBeta(array: Array[Double]): Unit = {
    vBeta = constructDVbyArray(array, numItem)
  }

  def setVectorGamma(array: Array[Double]): Unit = {
    vGamma = constructDVbyArray(array, dimGlobal)
  }

  //predict
  def predict(uid: Int, iid: Int): Double = {
    var pred1: Double = 0.0
    //\Simga vAlpha*biasUser
    //pred1 += vTemp.dot(vBiasUser)
    pred1 += vAlpha(uid) * vBiasUser(uid)

    //\Simga vBeta*biasItem
    //vTemp = vBeta
    //pred1 += vTemp.dot(vBiasItem)
    pred1 += vBeta(iid) * vBiasItem(iid)

    //\Simga vGamma*biasGlobal
    var vTemp = vGamma
    pred1 += vTemp.dot(vBiasGlobal)

    //TODO

    //\Sigma \alpha_j*p_j
    //var pred2 = new DenseVector[Double](new Array[Double](numFeatures))
    //for (j <- 0 to numUser - 1) {
    var pred2 = mP(uid) * vAlpha(uid)
    //}

    //\Sigma \beta_j*q_j
    //var pred3 = new DenseVector[Double](new Array[Double](numFeatures))
    //for (j <- 0 to numItem - 1) {
    var pred3 = mQ(iid) * vBeta(iid)
    //}

    pred1 + pred2.dot(pred3) + baseScore
  }

  def gradientDescent(target: Double, uid: Int, iid: Int): Unit = {
    var pred = predict(uid, iid)
    var error = target - pred

    accError += abs(error)
    rmse += error * error

    if (times % gap == 0) {
      println("Rounds:" + rounds + " Times:" + times)
      println("pred:", pred, "target:", target, "error:", error)
      var t_accError = accError / (1 + times)
      var t_rmse = rmse / (1.0f + times)
      t_rmse = sqrt(t_rmse)
      println("avgErr:", t_accError.toString, "RMSE-Train:", t_rmse.toString)
    }

    times += 1
    /*
        var delta_p: Array[DenseVector[Double]] = Array.fill(numUser)(new DenseVector[Double](numFeatures))
        var delta_q: Array[DenseVector[Double]] = Array.fill(numItem)(new DenseVector[Double](numFeatures))
        var delta_bu: DenseVector[Double] = new DenseVector[Double](numUser)
        var delta_bi: DenseVector[Double] = new DenseVector[Double](numItem)
        var delta_bg: DenseVector[Double] = new DenseVector[Double](numGlobalDim)
    */
    var delta_p: DenseVector[Double] = new DenseVector[Double](dimFeatures)
    var delta_q: DenseVector[Double] = new DenseVector[Double](dimFeatures)
    var delta_bu: Double = 0
    var delta_bi: Double = 0
    var delta_bg: DenseVector[Double] = new DenseVector[Double](dimGlobal)

    /** get delta P
      * get delta biasU
      */
    //for (i: Int <- 0 to numUser - 1) {
    /*update P_i*/
    //clear dp
    //for (j <- 0 to delta_p(i).length - 1) delta_p(i)(j) = 0.0
    //\Sigma q_j*\beta_j
    for (j <- 0 to dimItem - 1) {
      delta_p += mQ(iid) * vBeta(iid)
    }
    //error*\alpha_i
    delta_p *= vAlpha(uid) * error
    //delta_p -= lambdaP * mP(uid)
    delta_p *= learningRate

    /*update biasUser_i*/
    delta_bu = error * vAlpha(uid)
    delta_bu *= learningRate
    //}

    /** get delta Q
      * get delta biasI
      */
    //for (i: Int <- 0 to numItem - 1) {
    /*update Q*/
    //clear delta_q
    //for (j <- 0 to delta_q(i).length - 1) delta_q(i)(j) = 0.0

    //\Sigma p_j*\alpha_j
    for (j <- 0 to dimUser - 1) {
      delta_q += mP(uid) * vAlpha(uid)
    }
    //error*\beta_i
    delta_q *= vBeta(iid) * error
    //delta_q -= lambdaQ * mQ(iid)
    delta_q *= learningRate

    /*update biasUser_i*/
    delta_bi = error * vBeta(iid)
    delta_bi *= learningRate
    //}

    /** get delta globalBias
      *
      */
    for (i: Int <- 0 to dimGlobal - 1) {
      delta_bg(i) = error * vGamma(i)
      delta_bg(i) *= learningRate
    }

    /** update delta P
      * update delta biasU
      */
    //for (i: Int <- 0 to numUser - 1) {
    for (j <- 0 to dimFeatures - 1)
      mP(uid).update(j, updateParamPQ(mP(uid)(j), delta_p(j), dimFeatures))
    //mP(i) += delta_p


    //vBiasUser(i) += delta_bu
    vBiasUser(uid) = updateParam(vBiasUser(uid), delta_bu)
    //}

    /** update delta Q
      * update delta biasI
      */
    //for (i: Int <- 0 to numItem - 1) {
    /*update Q*/
    for (j <- 0 to dimFeatures - 1)
      mQ(iid).update(j, updateParamPQ(mQ(iid)(j), delta_q(j), dimFeatures))
    //mQ(i) += delta_q

    /*update biasUser_i*/
    //vBiasItem(i) += delta_bi
    vBiasItem(iid) = updateParam(vBiasItem(iid), delta_bi)
    //}

    /** update delta globalBias
      *
      */
    for (i: Int <- 0 to dimGlobal - 1) {
      //vBiasGlobal(i) += delta_bg
      vBiasGlobal(i) = updateParam(vBiasGlobal(i), delta_bg(i))
    }

    //Regularization
    vBiasGlobal *= (1.0f - learningRate * lambdaBiasGlobal)
    vBiasUser(uid) *= (1.0f - learningRate * lambdaBiasUser)
    vBiasItem(iid) *= (1.0f - learningRate * lambdaBiasItem)
    mP(uid) *= (1.0f - learningRate * lambdaP)
    mQ(iid) *= (1.0f - learningRate * lambdaQ)
  }

  def updateParam(w: Double, wd: Double): Double = {
    reg_L1(w, wd)
  }

  def updateParamPQ(w: Double, wd: Double, num: Int): Double = {
    w + (wd / 1)
  }

  def updateParamPQ2(w: Double, wd: Double, num: Int): Double = {
    var t = w
    if (t >= 1.0f) t = 1.0f - scala.Double.MinPositiveValue
    if (t <= 0.0f) t = scala.Double.MinPositiveValue

    t = 1.0f / t
    t -= 1.0f
    if (t <= 0.0f) t = scala.Double.MinPositiveValue
    t = -1.0f * log(t)
    t += wd

    t = exp(-1.0f * t) + 1
    1.0f / num / t
  }

  def reg_L1(w: Double, wd: Double): Double = {
    w + wd
  }

  def reg_L1_(w: Double, wd: Double): Double = {
    val sameSign: Boolean = (w * wd) > 0
    val _w = abs(w)
    val _wd = abs(wd)

    if (_w > _wd) w + wd
    w
  }

  def reg_L1_old(w: Double, wd: Double): Double = {
    if (w > wd) w - wd
    else {
      if (w < -wd) w + wd
      else 0.0f
    }
  }
}