package demo.test

import breeze.linalg.{max, min}
import com.github.fommil.netlib.BLAS.{getInstance => blas}

import scala.math.{abs, sqrt}

@SerialVersionUID(39793743L)
class SvdFeatureStandaloneMiniBatch(baseScore: Double,
                                    numUser: Int,
                                    numItem: Int,
                                    dimFeatures: Int,
                                    dimGlobal: Int,
                                    dimUser: Int,
                                    dimItem: Int,
                                    //matScore: Matrix[Double],
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
                                    vecInitF: (Int) => Array[Double] = SvdFeatureUtils.initialization.defaultVecInitializationF,
                                    biasRegularizationF: (Double, Double) => Double = SvdFeatureUtils.single.regularization.defaultRegularizationF,
                                    vecRegularizationF: (Array[Double], Double) => Unit = SvdFeatureUtils.batch.regularization.defaultRegularization,
                                    miniBatchRatioF: (Int) => Double = SvdFeatureUtils.batch.ratio.suggestRatioF
                                   ) extends Serializable {
  assert(numUser > 0, "Number of users:" + numUser.toString + "<=0")
  assert(numItem > 0, "Number of items:" + numItem.toString + "<=0")
  assert(dimFeatures > 0, "Number of features:" + dimFeatures.toString + "<=0")
  assert(dimGlobal >= 0, "Number of global dims:" + dimGlobal.toString + "<0")
  assert(learningRateDecay >= 0.0, s"Learning Rate Decay should >=0):$learningRateDecay")
  assert(learningRateMin > 0.0 && learningRateMin <= learningRateMax, s"Learning Rate Min should be (0.0,Max]:$learningRateMin")
  assert(learningRateMax < 1.0, s"Learning Rate Decay should be [Min,1.0):$learningRateMax")


  def copy(): SvdFeatureStandaloneMiniBatch = {
    val t = new SvdFeatureStandaloneMiniBatch(baseScore = baseScore,
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
      biasInitF = biasInitF,
      vecInitF = vecInitF,
      biasRegularizationF = biasRegularizationF,
      vecRegularizationF = vecRegularizationF,
      miniBatchRatioF = miniBatchRatioF
    )

    t.learningRate = this.learningRate
    t.rounds = this.rounds
    t.times = this.times
    t.reportGap = this.reportGap
    t.avgErrorOnTrain = this.avgErrorOnTrain
    t.avgErrorOnTest = this.avgErrorOnTest
    t.rmseOnTrain = this.rmseOnTrain
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

  private var learningRate = _learningRate
  var rounds = 0L
  var times = 0L
  private var reportGap = 1L
  var avgErrorOnTrain: Double = 0.0
  var avgErrorOnTest: Double = 0.0
  var rmseOnTrain: Double = 0.0
  var rmseOnTest: Double = 0.0

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
  private var mP: Array[Array[Double]] = new Array[Array[Double]](numUser)
  for (i <- 0 until numUser) {
    mP(i) = vecInitF(dimFeatures)
  }
  //biasUser
  private var vBiasUser: Array[Double] = Array.fill[Double](numUser)(biasInitF())


  //item
  private var vBeta: Array[(Long, Double)] = Array.fill[(Long, Double)](numItem)(0L, 0.0f)
  private var mQ: Array[Array[Double]] = new Array[Array[Double]](numItem)
  for (i <- 0 until numItem) {
    mQ(i) = vecInitF(dimFeatures)
  }
  private var vBiasItem: Array[Double] = Array.fill[Double](numItem)(biasInitF())

  //global
  private var vGamma: Array[(Long, Double)] = Array.fill[(Long, Double)](dimGlobal)(0L, 0.0f)
  // biasGlobal
  private var vBiasGlobal: Array[Double] = Array.fill[Double](dimGlobal)(biasInitF())

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
    val pred2 = new Array[Double](dimFeatures)
    for ((uid, value) <- vAlpha) {
      blas.daxpy(dimFeatures, value, mP(uid.toInt), 1, pred2, 1)
    }

    //\Sigma \beta_j*q_j
    val pred3 = new Array[Double](dimFeatures)
    for ((iid, value) <- vBeta) {
      blas.daxpy(dimFeatures, value, mQ(iid.toInt), 1, pred3, 1)
    }

    //pred1 + pred2 dot pred3 + u
    pred1 + blas.ddot(dimFeatures, pred2, 1, pred3, 1) + baseScore
  }

  type InputRating = (Double, Array[(Long, Double)], Array[(Long, Double)], Array[(Long, Double)])
  type MMap[A, B] = scala.collection.mutable.Map[A, B]

  def gradientDescent(inputs: Array[InputRating]): Unit = {
    //1.gradients for each batch
    //ID->(count,deltas)
    val deltaP: MMap[Long, (Int, Array[Double])] = scala.collection.mutable.Map[Long, (Int, Array[Double])]()
    val deltaQ: MMap[Long, (Int, Array[Double])] = scala.collection.mutable.Map[Long, (Int, Array[Double])]()
    val deltaBu: MMap[Long, (Int, Double)] = scala.collection.mutable.Map[Long, (Int, Double)]()
    val deltaBi: MMap[Long, (Int, Double)] = scala.collection.mutable.Map[Long, (Int, Double)]()
    val deltaBg: MMap[Long, (Int, Double)] = scala.collection.mutable.Map[Long, (Int, Double)]()

    def aggregateDeltaVector(map: MMap[Long, (Int, Array[Double])],
                             id: Long,
                             msg: Array[Double]): Unit = {
      if (map.contains(id)) {
        var (oldCount, oldMsg) = map(id)
        oldCount += 1
        blas.daxpy(oldMsg.length, 1.0, msg, 1, oldMsg, 1)
        map.update(id, (oldCount, oldMsg))
      } else {
        map.put(id, (1, msg))
      }
    }

    def aggregateDeltaBias(map: MMap[Long, (Int, Double)],
                           id: Long,
                           msg: Double): Unit = {
      if (map.contains(id)) {
        val (oldCount, oldMsg) = map(id)
        map.update(id, (oldCount + 1, oldMsg + msg))
      } else {
        map.put(id, (1, msg))
      }
    }


    for ((target, alpha, beta, gamma) <- inputs) {
      val prediction: Double = predict(alpha, beta, gamma)
      val error = target - prediction

      avgErrorOnTrain += abs(error)
      rmseOnTrain += error * error

      if (times % reportGap == 0) {
        println("Rounds:" + rounds + " Times:" + times)
        println("pred:", prediction, "target:", target, "error:", error)
        val t_accError = avgErrorOnTrain / (1 + times)
        var t_rmse = rmseOnTrain / (1.0f + times)
        t_rmse = sqrt(t_rmse)
        println("avgErr:", t_accError.toString, "RMSE-Train:", t_rmse.toString)
      }

      times += 1

      /** get delta P
        * get delta biasU
        */
      //get \Sigma q_j \beta_j first
      val sigmaBetaQ = new Array[Double](dimFeatures)
      for ((iid, _) <- vBeta) {
        blas.daxpy(dimFeatures, 1.0, mQ(iid.toInt), 1, sigmaBetaQ, 1)
      }
      for ((uid, value) <- vAlpha) {
        val temp = sigmaBetaQ.clone()
        val deltaBiasU = learningRate * error * value
        blas.dscal(dimFeatures, deltaBiasU, temp, 1)
        aggregateDeltaVector(deltaP, uid, temp)
        aggregateDeltaBias(deltaBu, uid, deltaBiasU)
        //delta_p(i) = learningRate * error * vAlpha(i)._2 * sigmaBetaQ
        //delta_bu(i) = learningRate * error * vAlpha(i)._2
      }
      /** get delta Q
        * get delta biasI
        */
      //get \Sigma p_j \alpha_j first
      val sigmaAlphaP = new Array[Double](dimFeatures)
      for ((uid, _) <- vAlpha) {
        blas.daxpy(dimFeatures, 1.0, mP(uid.toInt), 1, sigmaAlphaP, 1)
      }
      for ((iid, value) <- vBeta) {
        val temp = sigmaAlphaP.clone()
        val deltaBiasI = learningRate * error * value
        blas.dscal(dimFeatures, deltaBiasI, temp, 1)
        aggregateDeltaVector(deltaQ, iid, temp)
        aggregateDeltaBias(deltaBi, iid, deltaBiasI)
      }

      /** get delta BiasG */
      for ((gid, value) <- vGamma) {
        val deltaBiasG = learningRate * error * value
        aggregateDeltaBias(deltaBg, gid, deltaBiasG)
      }
    }

    /** update delta P / Q
      */
    for ((uid, (num, delta)) <- deltaP) {
      val ratio = miniBatchRatioF(num)
      //blas.dscal(dimFeatures,ratio,delta,1)
      blas.daxpy(dimFeatures, ratio, delta, 1, mP(uid.toInt), 1)
      //regularization
      vecRegularizationF(mP(uid.toInt), learningRate * lambdaP)
    }
    for ((iid, (num, delta)) <- deltaQ) {
      val ratio = miniBatchRatioF(num)
      //blas.dscal(dimFeatures,ratio,delta,1)
      blas.daxpy(dimFeatures, ratio, delta, 1, mQ(iid.toInt), 1)
      //regularization
      vecRegularizationF(mQ(iid.toInt), learningRate * lambdaQ)
    }
    /** update delta biasU/I/G
      */
    for ((uid, (num, delta)) <- deltaBu) {
      val ratio = miniBatchRatioF(num)
      vBiasUser(uid.toInt) += ratio * delta
      //regularization
      vBiasUser(uid.toInt) = biasRegularizationF(vBiasUser(uid.toInt), learningRate * lambdaBiasUser)
    }
    for ((iid, (num, delta)) <- deltaBi) {
      val ratio = miniBatchRatioF(num)
      vBiasItem(iid.toInt) += ratio * delta
      //regularization
      vBiasItem(iid.toInt) = biasRegularizationF(vBiasItem(iid.toInt), learningRate * lambdaBiasItem)
    }
    for ((gid, (num, delta)) <- deltaBg) {
      val ratio = miniBatchRatioF(num)
      vBiasGlobal(gid.toInt) += ratio * delta
      //regularization
      vBiasGlobal(gid.toInt) = biasRegularizationF(vBiasGlobal(gid.toInt), learningRate * lambdaBiasGlobal)
    }
  }
}