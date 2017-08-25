package demo.test

import breeze.numerics.{pow, sqrt}
import com.github.fommil.netlib.BLAS.{getInstance => blas}

import scala.util.Random

object SvdFeatureUtils {

  /** Functions for SGD */
  object single {

    /** Functions for regularization */
    object regularization {
      def l2(w: Double, ratio: Double): Double = {
        w * (1.0 - ratio)
      }

      def l1(w: Double, ratio: Double): Double = {
        if (w > ratio) w - ratio
        else {
          if (w < -ratio) w + ratio
          else 0.0
        }
      }

      def defaultRegularizationF(w: Double, delta: Double): Double = l2(w, delta)
    }

  }

  /** Functions for batch/mini-batch */
  object batch {

    /** Ratio functions */
    object ratio {
      def suggestRatioF(numGradients: Int): Double = {
        1.0 / pow(numGradients, 2.0 / 3.0)
      }

      def standardRatioF(numGradients: Int): Double = {
        1.0 / numGradients
      }

      def sqrtRatioF(numGradients: Int): Double = {
        1.0 / sqrt(numGradients)
      }
    }

    /** Batch Regularization */
    object regularization {
      def l2(arr: Array[Double], lambda: Double): Unit = {
        blas.dscal(arr.length, 1.0 - lambda, arr, 1)
      }

      def defaultRegularization: (Array[Double], Double) => Unit = l2
    }

  }


  /** Functions for initializing user/items */
  object initialization {
    def biasConst(const: Double)(): Double = {
      const
    }

    def biasGaussian(mean: Double = 0.0f, deviation: Double = 1.0f)(): Double = {
      mean + Random.nextGaussian() * deviation
    }

    def vecConst(const: Double)(dimFeatures: Int): Unit = {
      Array.fill[Double](dimFeatures)(const)
    }

    def vecGaussian(mean: Double = 0.0f, deviation: Double = 1.0f)(dimFeatures: Int): Array[Double] = {
      Array.fill[Double](dimFeatures)(Random.nextGaussian() * deviation + mean)
    }

    def defaultBiasInitializationF: () => Double = biasConst(0.0)

    def defaultVecInitializationF: (Int) => Array[Double] = vecGaussian(0.0, 0.01f)
  }

}
