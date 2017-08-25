package demo.test


import breeze.linalg.DenseVector

import scala.math._
import scala.util.Random


object SvdFeature {
  def main(args: Array[String]): Unit = {
    //val spark = SparkSession.builder().appName("testKMeans").getOrCreate()

    val numUser = 5000
    val numItem = 5

    var svdFeature = new SvdFeature(
      target = 5.0,
      numUser = numUser,
      numItem = numItem,
      numFeatures = 1,
      numGlobalDim = 0,
      //matScore = matScore,
      learningRate = 0.0001,
      lambdaBiasUser = 0.0003,
      lambdaBiasItem = 0.0003,
      lambdaBiasGlobal = 0.0003,
      lambdaP = 0.0001,
      lambdaQ = 0.0001)
    //svdFeature.setVectorAlpha(Array[Double](1, 1, 0, 0, 1))
    svdFeature.setVectorAlpha(Array.fill(numUser)(Random.nextDouble()))
    var t: Array[Double] = new Array[Double](numUser)
    for (i <- 0 to numUser - 1) t(i) = i * 1.0f / numUser
    //svdFeature.setVectorAlpha(t)
    //svdFeature.setVectorBeta(Array[Double](1, 0, 0, 1))
    svdFeature.setVectorBeta(Array.fill(numItem)(Random.nextDouble()))
    var t2: Array[Double] = new Array[Double](numItem)
    for (i <- 0 to numItem - 1) t(i) = i * 1.0f / numItem
    //svdFeature.setVectorBeta(t2)
    for (i <- 0 to 83)
      svdFeature.gradientDescent()
    svdFeature.gradientDescent()
    svdFeature.gradientDescent()
    svdFeature.gradientDescent()
    svdFeature.gradientDescent()
    svdFeature.gradientDescent()
    svdFeature.gradientDescent()
    svdFeature.gradientDescent()
    svdFeature.gradientDescent()
    svdFeature.gradientDescent()
    for (i <- 0 to 100)
      svdFeature.gradientDescent()

    //spark.stop()
  }
}

class SvdFeature(target: Double,
                 numUser: Int,
                 numItem: Int,
                 numFeatures: Int,
                 numGlobalDim: Int,
                 //matScore: Matrix[Double],
                 //learning rate & lambdas 1-5
                 learningRate: Double,
                 lambdaP: Double,
                 lambdaQ: Double,
                 lambdaBiasUser: Double,
                 lambdaBiasItem: Double,
                 lambdaBiasGlobal: Double,
                 arrAlpha: Array[Double] = Array.empty[Double],
                 arrBeta: Array[Double] = Array.empty[Double],
                 arrGamma: Array[Double] = Array.empty[Double]
                ) {
  assert(numUser > 0, "Number of users:" + numUser.toString() + "<=0")
  assert(numItem > 0, "Number of items:" + numItem.toString() + "<=0")
  assert(numFeatures > 0, "Number of features:" + numFeatures.toString() + "<=0")
  assert(numGlobalDim >= 0, "Number of global dims:" + numGlobalDim.toString() + "<0")
  //assert(matScore.rows == numUser, "Number of users:" + numUser.toString() + " != matScore.numRows:" + matScore.rows)
  //assert(matScore.cols == numItem, "Number of items:" + numItem.toString() + " != matScore.numCols:" + matScore.cols)

  var times = 0

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
    mP(i) = new DenseVector[Double](Array.fill(numFeatures)(Random.nextDouble()))
  }
  //randomize biasUser
  var vBiasUser: DenseVector[Double] = new DenseVector(Array.fill(numUser)(Random.nextDouble()))


  //item
  var vBeta: DenseVector[Double] = constructDVbyArray(arrBeta, numItem)
  var mQ: Array[DenseVector[Double]] = new Array[DenseVector[Double]](numItem)
  for (i <- 0 to numItem - 1) {
    mQ(i) = new DenseVector[Double](Array.fill(numFeatures)(Random.nextDouble()))
  }
  //randomize biasItem
  var vBiasItem: DenseVector[Double] = new DenseVector(Array.fill(numItem)(Random.nextDouble()))

  //global
  var vGamma: DenseVector[Double] = constructDVbyArray(arrGamma, numGlobalDim)
  //randomize biasGlobal
  var vBiasGlobal: DenseVector[Double] = new DenseVector(Array.fill(numGlobalDim)(Random.nextDouble()))

  def setVectorAlpha(array: Array[Double]): Unit = {
    vAlpha = constructDVbyArray(array, numUser)
  }

  def setVectorBeta(array: Array[Double]): Unit = {
    vBeta = constructDVbyArray(array, numItem)
  }

  def setVectorGamma(array: Array[Double]): Unit = {
    vGamma = constructDVbyArray(array, numGlobalDim)
  }

  //predict
  def predict(): Double = {
    var pred1: Double = 0.0
    //\Simga vAlpha*biasUser
    var vTemp = vAlpha
    pred1 += vTemp.dot(vBiasUser)
    //\Simga vBeta*biasItem
    vTemp = vBeta
    pred1 += vTemp.dot(vBiasItem)
    //\Simga vGamma*biasGlobal
    vTemp = vGamma
    pred1 += vTemp.dot(vBiasGlobal)

    //\Sigma \alpha_j*p_j
    var pred2 = new DenseVector[Double](new Array[Double](numFeatures))
    for (j <- 0 to numUser - 1) {
      pred2 += mP(j) * vAlpha(j)
    }

    //\Sigma \beta_j*q_j
    var pred3 = new DenseVector[Double](new Array[Double](numFeatures))
    for (j <- 0 to numItem - 1) {
      pred3 += mQ(j) * vBeta(j)
    }

    pred1 + pred2.dot(pred3)
  }

  def gradientDescent(): Unit = {
    var pred = predict()
    var error = target - pred
    println("No:" + times)
    times += 1
    println("pred:", pred, "target:", target, "error:", error)

    var delta_p: Array[DenseVector[Double]] = Array.fill(numUser)(new DenseVector[Double](numFeatures))
    var delta_q: Array[DenseVector[Double]] = Array.fill(numItem)(new DenseVector[Double](numFeatures))
    var delta_bu: DenseVector[Double] = new DenseVector[Double](numUser)
    var delta_bi: DenseVector[Double] = new DenseVector[Double](numItem)
    var delta_bg: DenseVector[Double] = new DenseVector[Double](numGlobalDim)
    /** get delta P
      * get delta biasU
      */
    for (i: Int <- 0 to numUser - 1) {
      if (i == 4994) {
        var t: Int = 0
        t += 2
      }
      /*update P_i*/
      //clear dp
      for (j <- 0 to delta_p(i).length - 1) delta_p(i)(j) = 0.0
      //\Sigma q_j*\beta_j
      for (j <- 0 to numItem - 1) {
        delta_p(i) += mQ(j) * vBeta(j)
      }
      //error*\alpha_i
      delta_p(i) *= vAlpha(i) * error
      delta_p(i) -= lambdaP * mP(i)
      delta_p(i) *= learningRate

      /*update biasUser_i*/
      delta_bu(i) = error * vAlpha(i) - lambdaBiasUser * vBiasUser(i)
      delta_bu(i) *= learningRate
    }

    /** get delta Q
      * get delta biasI
      */
    for (i: Int <- 0 to numItem - 1) {
      /*update Q*/
      //clear delta_q
      for (j <- 0 to delta_q(i).length - 1) delta_q(i)(j) = 0.0

      //\Sigma p_j*\alpha_j
      for (j <- 0 to numUser - 1) {
        delta_q(i) += mP(j) * vAlpha(j)
      }
      //error*\beta_i
      delta_q(i) *= vBeta(i) * error
      delta_q(i) -= lambdaQ * mQ(i)
      delta_q(i) *= learningRate

      /*update biasUser_i*/
      delta_bi(i) = error * vBeta(i) - lambdaBiasItem * vBiasItem(i)
      delta_bi(i) *= learningRate
    }

    /** get delta globalBias
      *
      */
    for (i: Int <- 0 to numGlobalDim - 1) {
      delta_bg(i) = error * vGamma(i) - lambdaBiasGlobal * vBiasGlobal(i)
      delta_bg(i) *= learningRate
    }

    /** update delta P
      * update delta biasU
      */
    for (i: Int <- 0 to numUser - 1) {
      if (i == 4994) {
        var a: Int = 0
        a += 3
      }
      for (j <- 0 to numFeatures - 1)
        mP(i).update(j, updateParamPQ(mP(i)(j), delta_p(i)(j), numUser))
      //mP(i) += delta_p


      //vBiasUser(i) += delta_bu
      vBiasUser(i) = updateParam(vBiasUser(i), delta_bu(i))
    }

    /** update delta Q
      * update delta biasI
      */
    for (i: Int <- 0 to numItem - 1) {
      /*update Q*/
      for (j <- 0 to numFeatures - 1)
        mQ(i).update(j, updateParamPQ(mQ(i)(j), delta_q(i)(j), numItem))
      //mQ(i) += delta_q

      /*update biasUser_i*/
      //vBiasItem(i) += delta_bi
      vBiasItem(i) = updateParam(vBiasItem(i), delta_bi(i))
    }

    /** update delta globalBias
      *
      */
    for (i: Int <- 0 to numGlobalDim - 1) {
      //vBiasGlobal(i) += delta_bg
      vBiasGlobal(i) = updateParam(vBiasGlobal(i), delta_bg(i))
    }
  }

  def updateParam(w: Double, wd: Double): Double = {
    reg_L1(w, wd)
  }

  def updateParamPQ2(w: Double, wd: Double, num: Int): Double = {
    val sign = signum(w)
    var _w = w + (wd / num)
    if (_w == scala.Double.NaN) {
      if (sign > 0.0) scala.Double.MaxValue
      else scala.Double.MinValue
    }
    else if (_w > scala.Double.MaxValue) scala.Double.MaxValue
    else if (_w < scala.Double.MinPositiveValue) scala.Double.MinPositiveValue
    _w
  }

  def updateParamPQ(w: Double, wd: Double, num: Int): Double = {
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