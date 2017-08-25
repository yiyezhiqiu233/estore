package demo.test

import breeze.linalg.{DenseVector, max, min}

import scala.math.{abs, sqrt}


@SerialVersionUID(39793743L)
class SvdFeatureStandalone(baseScore: Double,
                           numUser: Int,
                           numItem: Int,
                           dimFeatures: Int,
                           dimGlobal: Int,
                           dimUser: Int,
                           dimItem: Int,
                           //learning rate & lambdas 1-5
                           _learningRate: Double,
                           lambdaP: Double,
                           lambdaQ: Double,
                           lambdaBiasUser: Double,
                           lambdaBiasItem: Double,
                           lambdaBiasGlobal: Double,
                           learningRateDecay: Double = 0.0,
                           learningRateMin: Double = 0.0001,
                           learningRateMax: Double = 0.1,
                           biasInitF: () => Double = SvdFeatureUtils.initialization.defaultBiasInitializationF,
                           vecInitF: () => Double = SvdFeatureUtils.initialization.biasGaussian(0.0, 0.01f),
                           regularizationF: (Double, Double) => Double = SvdFeatureUtils.single.regularization.defaultRegularizationF
                          ) extends Serializable {
  assert(numUser > 0, "Number of users:" + numUser.toString + "<=0")
  assert(numItem > 0, "Number of items:" + numItem.toString + "<=0")
  assert(dimFeatures > 0, "Number of features:" + dimFeatures.toString + "<=0")
  assert(dimGlobal >= 0, "Number of global dims:" + dimGlobal.toString + "<0")
  assert(learningRateDecay >= 0.0, s"Learning Rate Decay should >=0):$learningRateDecay")
  assert(learningRateMin > 0.0 && learningRateMin <= learningRateMax, s"Learning Rate Min should be (0.0,Max]:$learningRateMin")
  assert(learningRateMax < 1.0, s"Learning Rate Decay should be [Min,1.0):$learningRateMax")

  private var learningRate = min(learningRateMax, _learningRate)
  learningRate = max(learningRateMin, learningRate)

  var rounds = 0L
  var times = 0L
  var reportGap = 1L
  var avgErrorOnTrain: Double = 0.0
  var avgErrorOnTest: Double = 0.0
  var rmseOnTrain: Double = 0.0
  var rmseOnTest: Double = 0.0

  //a copy of parameters
  def copy(): SvdFeatureStandalone = {
    val newSvdFeatureStandalone = new SvdFeatureStandalone(
      baseScore = baseScore,
      numUser = numUser,
      numItem = numItem,
      dimFeatures = dimFeatures,
      dimGlobal = dimGlobal,
      dimUser = dimUser,
      dimItem = dimItem,
      _learningRate = learningRate,
      lambdaP = lambdaP,
      lambdaQ = lambdaQ,
      lambdaBiasUser = lambdaBiasUser,
      lambdaBiasItem = lambdaBiasItem,
      lambdaBiasGlobal = lambdaBiasGlobal,
      learningRateDecay = learningRateDecay,
      learningRateMin = learningRateMin,
      learningRateMax = learningRateMax,
      biasInitF = this.biasInitF,
      vecInitF = this.vecInitF,
      regularizationF = this.regularizationF
    )

    newSvdFeatureStandalone.learningRate = this.learningRate
    newSvdFeatureStandalone.rounds = this.rounds
    newSvdFeatureStandalone.times = this.times
    newSvdFeatureStandalone.reportGap = this.reportGap
    newSvdFeatureStandalone.avgErrorOnTrain = this.avgErrorOnTrain
    newSvdFeatureStandalone.avgErrorOnTest = this.avgErrorOnTest
    newSvdFeatureStandalone.rmseOnTrain = this.rmseOnTrain
    newSvdFeatureStandalone.rmseOnTest = this.rmseOnTest
    newSvdFeatureStandalone.vAlpha = this.vAlpha
    newSvdFeatureStandalone.vBeta = vBeta
    newSvdFeatureStandalone.vGamma = vGamma
    newSvdFeatureStandalone.mP = mP.clone()
    newSvdFeatureStandalone.mQ = mQ.clone()
    newSvdFeatureStandalone.vBiasUser = this.vBiasUser
    newSvdFeatureStandalone.vBiasItem = this.vBiasItem
    newSvdFeatureStandalone.vBiasGlobal = this.vBiasGlobal

    newSvdFeatureStandalone
  }

  def setGap(_gap: Long): Unit = {
    if (_gap > 0) reportGap = _gap
  }

  def setRounds(_rounds: Long): Unit = {
    if (rounds >= 0) {
      rounds = _rounds
      times = 0
      avgErrorOnTrain = 0.0f
      rmseOnTrain = 0.0

      //update lr
      this.learningRate *= (1.0 - learningRateDecay)
      learningRate = min(learningRateMax, learningRate)
      learningRate = max(learningRateMin, learningRate)
      //show current learning rate
      println("LEARNING rate:", learningRate)
    }
  }

  //user
  private var vAlpha: Array[(Long, Double)] = Array.fill[(Long, Double)](numUser)(0L, 0.0f)
  //randomize P matrix
  private var mP: Array[DenseVector[Double]] = new Array[DenseVector[Double]](numUser)
  for (i <- 0 until numUser) {
    mP(i) = new DenseVector[Double](Array.fill[Double](dimFeatures)(vecInitF()))
  }
  //biasUser- init as 0
  private var vBiasUser: DenseVector[Double] = new DenseVector(Array.fill[Double](numUser)(biasInitF()))


  //item
  private var vBeta: Array[(Long, Double)] = Array.fill[(Long, Double)](numItem)(0L, 0.0f)
  private var mQ: Array[DenseVector[Double]] = new Array[DenseVector[Double]](numItem)
  for (i <- 0 until numItem) {
    mQ(i) = new DenseVector[Double](Array.fill(dimFeatures)(vecInitF()))
  }
  //biasItem init as 0
  private var vBiasItem: DenseVector[Double] = new DenseVector(Array.fill[Double](numItem)(biasInitF()))

  //global
  private var vGamma: Array[(Long, Double)] = Array.fill[(Long, Double)](dimGlobal)(0L, 0.0f)
  //biasGlobal
  private var vBiasGlobal: DenseVector[Double] = new DenseVector(Array.fill[Double](dimGlobal)(biasInitF()))


  private def setVectorAlpha(array: Array[(Long, Double)]): Unit = {
    vAlpha = array
  }

  private def setVectorBeta(array: Array[(Long, Double)]): Unit = {
    vBeta = array
  }

  private def setVectorGamma(array: Array[(Long, Double)]): Unit = {
    vGamma = array
  }

  //predict
  def predict(alpha: Array[(Long, Double)] = null,
              beta: Array[(Long, Double)] = null,
              gamma: Array[(Long, Double)] = null): Double = {
    if (alpha != null) setVectorAlpha(alpha)
    if (beta != null) setVectorBeta(beta)
    if (gamma != null) setVectorGamma(gamma)

    var pred1: Double = 0.0
    //\Simga vAlpha*biasUser
    for ((uid, value) <- vAlpha)
      pred1 += value * vBiasUser(uid.toInt)

    //\Simga vBeta*biasItem
    //vTemp = vBeta
    for ((iid, value) <- vBeta)
      pred1 += value * vBiasItem(iid.toInt)

    //\Simga vGamma*biasGlobal
    for ((gid, value) <- vGamma)
      pred1 += value * vBiasGlobal(gid.toInt)

    //\Sigma \alpha_j*p_j
    var pred2 = new DenseVector[Double](new Array[Double](dimFeatures))
    for ((uid, value) <- vAlpha) {
      pred2 += value * mP(uid.toInt)
    }

    //\Sigma \beta_j*q_j
    var pred3 = new DenseVector[Double](new Array[Double](dimFeatures))
    for ((iid, value) <- vBeta) {
      pred3 += value * mQ(iid.toInt)
    }

    pred1 + pred2.dot(pred3) + baseScore
  }

  def gradientDescent(target: Double,
                      alpha: Array[(Long, Double)],
                      beta: Array[(Long, Double)],
                      gamma: Array[(Long, Double)]): Unit = {
    setVectorAlpha(alpha)
    setVectorBeta(beta)
    setVectorGamma(gamma)

    val prediction: Double = predict()
    val error = target - prediction

    avgErrorOnTrain += abs(error)
    rmseOnTrain += error * error

    if (times % reportGap == 0) {
      println("Rounds:" + rounds + " Times:" + times)
      println("prediction:", prediction, "target:", target, "error:", error)
      val t_accError = avgErrorOnTrain / (1 + times)
      var t_rmse = rmseOnTrain / (1.0f + times)
      t_rmse = sqrt(t_rmse)
      println("avgErr:", t_accError.toString, "RMSE-Train:", t_rmse.toString)
    }

    times += 1
    val delta_p: Array[DenseVector[Double]] = Array.fill(alpha.length)(new DenseVector[Double](dimFeatures))
    val delta_q: Array[DenseVector[Double]] = Array.fill(beta.length)(new DenseVector[Double](dimFeatures))
    val delta_bu: DenseVector[Double] = new DenseVector[Double](alpha.length)
    val delta_bi: DenseVector[Double] = new DenseVector[Double](beta.length)
    val delta_bg: DenseVector[Double] = new DenseVector[Double](gamma.length)


    /** get delta P
      * get delta biasU
      */
    //get \Sigma q_j \beta_j first
    var vTemp = new DenseVector[Double](dimFeatures)
    for ((iid, value) <- vBeta) {
      vTemp += value * mQ(iid.toInt)
    }
    for (i <- vAlpha.indices) {
      delta_p(i) = learningRate * error * vAlpha(i)._2 * vTemp
      delta_bu(i) = learningRate * error * vAlpha(i)._2
    }
    /** get delta Q
      * get delta biasI
      */
    //get \Sigma p_j \alpha_j first
    //clear vTemp
    vTemp = vTemp.map(_ => 0.0)
    for ((uid, value) <- vAlpha) {
      vTemp += value * mP(uid.toInt)
    }
    for (i <- vBeta.indices) {
      delta_q(i) = learningRate * error * vBeta(i)._2 * vTemp
      delta_bi(i) = learningRate * error * vBeta(i)._2
    }

    /** get delta BiasG */
    for (i <- vGamma.indices) {
      delta_bg(i) = learningRate * error * vGamma(i)._2
    }


    /** update delta P
      * update delta biasU
      */
    for (i <- vAlpha.indices) {
      for (j <- 0 until dimFeatures) {
        mP(vAlpha(i)._1.toInt).update(j, updateParamPQ(mP(vAlpha(i)._1.toInt)(j), delta_p(i)(j)))
      }
      vBiasUser(vAlpha(i)._1.toInt) = updateParam(vBiasUser(vAlpha(i)._1.toInt), delta_bu(i))
    }
    /** update delta Q
      * update delta biasI
      */
    for (i <- vBeta.indices) {
      for (j <- 0 until dimFeatures) {
        mQ(vBeta(i)._1.toInt).update(j, updateParamPQ(mQ(vBeta(i)._1.toInt)(j), delta_q(i)(j)))
      }
      vBiasItem(vBeta(i)._1.toInt) = updateParam(vBiasItem(vBeta(i)._1.toInt), delta_bi(i))
    }

    /** update delta globalBias */
    for (i <- 0 until dimGlobal) {
      vBiasGlobal(i) = updateParam(vBiasGlobal(i), delta_bg(i))
    }

    //Regularization
    for (i <- 0 until vBiasGlobal.length) {
      vBiasGlobal.update(i, regularizationF(vBiasGlobal(i), learningRate * lambdaBiasGlobal))
    }
    for ((uid, _) <- vAlpha) {
      vBiasUser.update(uid.toInt, regularizationF(vBiasUser(uid.toInt), learningRate * lambdaBiasUser))
      for (i <- 0 until mP(uid.toInt).length) {
        mP(uid.toInt).update(i, regularizationF(mP(uid.toInt)(i), learningRate * lambdaP))
      }
    }
    for ((iid, _) <- vBeta) {
      vBiasItem.update(iid.toInt, regularizationF(vBiasItem(iid.toInt), learningRate * lambdaBiasItem))
      for (i <- 0 until mQ(iid.toInt).length) {
        mQ(iid.toInt).update(i, regularizationF(mQ(iid.toInt)(i), learningRate * lambdaQ))
      }
    }
  }

  def updateParam(w: Double, wd: Double): Double = {
    w + wd
  }

  def updateParamPQ(w: Double, wd: Double): Double = {
    w + wd
  }
}