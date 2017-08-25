package demo.test


import breeze.numerics.{abs, sqrt}
import com.github.fommil.netlib.BLAS.{getInstance => blas}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx.{Edge, EdgeContext, Graph, VertexId}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.Random


object SvdPlusPlusTest {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)
    val spark = SparkSession.builder().appName("testKMeans").getOrCreate()
    val sc = spark.sparkContext

    val trainSetFile = "/Users/marme/Downloads/ml-1m/ub_1m.base.shuffle"
    val testSetFile = "/Users/marme/Downloads/ml-1m/ub_1m.test.shuffle"

    type InputRating = (Double, Array[(Long, Double)], Array[(Long, Double)], Array[(Long, Double)])

    val MAX_ROUNDS: Int = 128
    var BatchSize: Int = 10000

    val numUser = 6040
    // 6040//138493
    val numUserExtraInfo = 0
    val numItem = 3952
    //3952//131262
    val numItemExtraInfo = 0


    var cnt: Long = 0
    var testEdges: ArrayBuffer[Edge[Double]] = new ArrayBuffer[Edge[Double]]()
    var trainEdges: ArrayBuffer[Edge[Double]] = new ArrayBuffer[Edge[Double]]()
    for (line <- Source.fromFile(testSetFile).getLines) {
      //println(line)
      var temp = line.split("\t")
      var score = temp(2).toDouble
      val uid = temp(0).toLong - 1
      val iid = temp(1).toLong - 1
      if (uid >= numUser || uid < 0) println("UID out of range")
      if (iid >= numItem || iid < 0) println("IID out of range")
      cnt += 1
      val edge: Edge[Double] = new Edge[Double](
        uid,
        iid + numUser,
        score
      )
      testEdges.append(edge)
    }
    var total_test_ratings: Long = cnt
    cnt = 0
    for (line <- Source.fromFile(trainSetFile).getLines) {
      //println(line)
      var temp = line.split("\t")
      var score = temp(2).toDouble
      val uid = temp(0).toLong - 1
      val iid = temp(1).toLong - 1
      if (uid >= numUser || uid < 0) println("UID out of range")
      if (iid >= numItem || iid < 0) println("IID out of range")
      cnt += 1
      val edge: Edge[Double] = new Edge[Double](
        uid,
        iid + numUser,
        score
      )
      trainEdges.append(edge)
    }
    var total_train_ratings: Long = cnt

    val conf: SVDPlusPlus.Conf = new SVDPlusPlus.Conf(
      rank = 64,
      maxIters = 1,
      minVal = 0.0,
      maxVal = 5.0,
      gamma1 = 0.0005,
      gamma2 = 0.005,
      gamma6 = 0.2,
      gamma7 = 0.1
    )
    val trainEdgesRDD = sc.parallelize(trainEdges)
    //val testEdgesRDD = sc.parallelize(testEdges.toArray)

    val CONST_NUM = 1
    var vertices: RDD[(VertexId, (Array[Double], Array[Double], Double, Double))] = null
    for (i <- 0 until MAX_ROUNDS) {
      println(s"ROUND:$i")
      val (g, u) = SVDPlusPlus.run(trainEdgesRDD, conf, 3.58, vertices)
      vertices=g.vertices
      println(s"ROUND-${i} train completed with avgScore:${u}")
      //test
      if (i % CONST_NUM == 0) {
        val verticesCollected: Array[(VertexId, (Array[Double], Array[Double], Double, Double))] =
          g.vertices.collect()
        val map: scala.collection.mutable.Map[VertexId, (Double, Array[Double])] = scala.collection.mutable.Map[VertexId, (Double, Array[Double])]()
        for ((id, (vec, _, bias, _)) <- verticesCollected) {
          if (map.contains(id)) {
            println(s"Duplicated Key Found:${id}")
          }
          else {
            map.put(id, (bias, vec))
          }
        }
        var accErr: Double = 0
        var rmse: Double = 0
        var cnt: Int = 0
        for (t <- testEdges) {
          cnt += 1
          val (uid, iid, target) = (t.srcId, t.dstId, t.attr)
          if (!map.contains(uid)) {
            //  println(s"UID Not Found:\n${uid + 1}\\t${iid + 1 - numUser}")
          }
          if (!map.contains(iid)) {
            //  println(s"IID Not Found:\n${uid + 1}\\t${iid + 1 - numUser}")
          }
          if (map.contains(uid) && map.contains(iid)) {
            val pred = u + map(uid)._1 + map(iid)._1 +
              blas.ddot(conf.rank, map(uid)._2, 1, map(iid)._2, 1)
            val err = pred - target
            accErr += abs(err)
            rmse += err * err
          }
        }
        println(s"Test-AccErr:${accErr / cnt}")
        println(s"Test-RMSE:${sqrt(rmse / cnt)}")
      }
    }
  }
}

object SVDPlusPlus {

  /** Configuration parameters for SVDPlusPlus. */
  class Conf(
              var rank: Int,
              var maxIters: Int,
              var minVal: Double,
              var maxVal: Double,
              var gamma1: Double,
              var gamma2: Double,
              var gamma6: Double,
              var gamma7: Double)
    extends Serializable

  /**
    * Implement SVD++ based on "Factorization Meets the Neighborhood:
    * a Multifaceted Collaborative Filtering Model",
    * available at <a href="http://public.research.att.com/~volinsky/netflix/kdd08koren.pdf">
    * here</a>.
    *
    * The prediction rule is rui = u + bu + bi + qi*(pu + |N(u)|^^-0.5^^*sum(y)),
    * see the details on page 6.
    *
    * @param edges edges for constructing the graph
    * @param conf  SVDPlusPlus parameters
    * @return a graph with vertex attributes containing the trained model
    */
  def run(edges: RDD[Edge[Double]], conf: Conf, u: Double,
          vertices: RDD[(VertexId, (Array[Double], Array[Double], Double, Double))] = null)
  : (Graph[(Array[Double], Array[Double], Double, Double), Double], Double) = {
    require(conf.maxIters > 0, s"Maximum of iterations must be greater than 0," +
      s" but got ${conf.maxIters}")
    require(conf.maxVal > conf.minVal, s"MaxVal must be greater than MinVal," +
      s" but got {maxVal: ${conf.maxVal}, minVal: ${conf.minVal}}")

    // Generate default vertex attribute
    def defaultF(rank: Int): (Array[Double], Array[Double], Double, Double) = {
      // TODO: use a fixed random seed
      val v1 = Array.fill(rank)(Random.nextGaussian() * 0.01)
      val v2 = Array.fill(rank)(Random.nextGaussian() * 0.01)
      (v1, v2, 0.0, 0.0)
    }

    // calculate global rating mean
    edges.cache()
    // val (rs, rc) = edges.map(e => (e.attr, 1L)).reduce((a, b) => (a._1 + b._1, a._2 + b._2))
    //val u = rs / rc

    // construct graph
    var g: Graph[(Array[Double], Array[Double], Double, Double), (Double)] = null
    if (vertices == null)
      g = Graph.fromEdges(edges, defaultF(conf.rank)).cache()
    else
      g = Graph(vertices, edges, defaultF(conf.rank)).cache()
    materialize(g)
    edges.unpersist()

    // Calculate initial bias and norm
    val t0 = g.aggregateMessages[(Long, Double)](
      ctx => {
        ctx.sendToSrc((1L, ctx.attr));
        ctx.sendToDst((1L, ctx.attr))
      },
      (g1, g2) => (g1._1 + g2._1, g1._2 + g2._2))

    val gJoinT0 = g.outerJoinVertices(t0) {
      (vid: VertexId, vd: (Array[Double], Array[Double], Double, Double),
       msg: Option[(Long, Double)]) =>
        (vd._1, vd._2, msg.get._2 / msg.get._1 - u, 1.0 / scala.math.sqrt(msg.get._1))
    }.cache()
    materialize(gJoinT0)
    g.unpersist()
    g = gJoinT0

    def sendMsgTrainF(conf: Conf, u: Double)
                     (ctx: EdgeContext[
                       (Array[Double], Array[Double], Double, Double),
                       Double,
                       (Array[Double], Array[Double], Double)]) {
      val (usr, itm) = (ctx.srcAttr, ctx.dstAttr)
      val (p, q) = (usr._1, itm._1)
      val rank = p.length
      var pred = u + usr._3 + itm._3 + blas.ddot(rank, q, 1, usr._2, 1)
      pred = math.max(pred, conf.minVal)
      pred = math.min(pred, conf.maxVal)
      val err = ctx.attr - pred
      // updateP = (err * q - conf.gamma7 * p) * conf.gamma2
      val updateP = q.clone()
      blas.dscal(rank, err * conf.gamma2, updateP, 1)
      blas.daxpy(rank, -conf.gamma7 * conf.gamma2, p, 1, updateP, 1)
      // updateQ = (err * usr._2 - conf.gamma7 * q) * conf.gamma2
      val updateQ = usr._2.clone()
      blas.dscal(rank, err * conf.gamma2, updateQ, 1)
      blas.daxpy(rank, -conf.gamma7 * conf.gamma2, q, 1, updateQ, 1)
      // updateY = (err * usr._4 * q - conf.gamma7 * itm._2) * conf.gamma2
      val updateY = q.clone()
      blas.dscal(rank, err * usr._4 * conf.gamma2, updateY, 1)
      blas.daxpy(rank, -conf.gamma7 * conf.gamma2, itm._2, 1, updateY, 1)
      ctx.sendToSrc((updateP, updateY, (err - conf.gamma6 * usr._3) * conf.gamma1))
      ctx.sendToDst((updateQ, updateY, (err - conf.gamma6 * itm._3) * conf.gamma1))
    }

    for (i <- 0 until conf.maxIters) {
      // Phase 1, calculate pu + |N(u)|^(-0.5)*sum(y) for user nodes
      g.cache()
      val t1 = g.aggregateMessages[Array[Double]](
        ctx => ctx.sendToSrc(ctx.dstAttr._2),
        (g1, g2) => {
          val out = g1.clone()
          blas.daxpy(out.length, 1.0, g2, 1, out, 1)
          out
        })
      val gJoinT1 = g.outerJoinVertices(t1) {
        (vid: VertexId, vd: (Array[Double], Array[Double], Double, Double),
         msg: Option[Array[Double]]) =>
          if (msg.isDefined) {
            val out = vd._1.clone()
            blas.daxpy(out.length, vd._4, msg.get, 1, out, 1)
            (vd._1, out, vd._3, vd._4)
          } else {
            vd
          }
      }.cache()
      materialize(gJoinT1)
      g.unpersist()
      g = gJoinT1

      // Phase 2, update p for user nodes and q, y for item nodes
      g.cache()
      val t2 = g.aggregateMessages(
        sendMsgTrainF(conf, u),
        (g1: (Array[Double], Array[Double], Double), g2: (Array[Double], Array[Double], Double)) => {
          val out1 = g1._1.clone()
          blas.daxpy(out1.length, 1.0, g2._1, 1, out1, 1)
          val out2 = g1._2.clone()
          blas.daxpy(out2.length, 1.0, g2._2, 1, out2, 1)
          (out1, out2, g1._3 + g2._3)
        })
      val gJoinT2 = g.outerJoinVertices(t2) {
        (vid: VertexId,
         vd: (Array[Double], Array[Double], Double, Double),
         msg: Option[(Array[Double], Array[Double], Double)]) => {
          val out1 = vd._1.clone()
          blas.daxpy(out1.length, 1.0, msg.get._1, 1, out1, 1)
          val out2 = vd._2.clone()
          blas.daxpy(out2.length, 1.0, msg.get._2, 1, out2, 1)
          (out1, out2, vd._3 + msg.get._3, vd._4)
        }
      }.cache()
      materialize(gJoinT2)
      g.unpersist()
      g = gJoinT2
    }

    // calculate error on training set
    def sendMsgTestF(conf: Conf, u: Double)
                    (ctx: EdgeContext[(Array[Double], Array[Double], Double, Double), Double, Double]) {
      val (usr, itm) = (ctx.srcAttr, ctx.dstAttr)
      val (p, q) = (usr._1, itm._1)
      var pred = u + usr._3 + itm._3 + blas.ddot(q.length, q, 1, usr._2, 1)
      pred = math.max(pred, conf.minVal)
      pred = math.min(pred, conf.maxVal)
      val err = (ctx.attr - pred) * (ctx.attr - pred)
      ctx.sendToDst(err)
    }

    g.cache()
    val t3 = g.aggregateMessages[Double](sendMsgTestF(conf, u), _ + _)
    val gJoinT3 = g.outerJoinVertices(t3) {
      (vid: VertexId, vd: (Array[Double], Array[Double], Double, Double), msg: Option[Double]) =>
        if (msg.isDefined) (vd._1, vd._2, vd._3, msg.get) else vd
    }.cache()
    materialize(gJoinT3)
    g.unpersist()
    g = gJoinT3

    // Convert DoubleMatrix to Array[Double]:
    val newVertices = g.vertices.mapValues(v => (v._1.toArray, v._2.toArray, v._3, v._4))
    (Graph(newVertices, g.edges), u)
  }

  /**
    * Forces materialization of a Graph by count()ing its RDDs.
    */
  private def materialize(g: Graph[_, _]): Unit = {
    g.vertices.count()
    g.edges.count()
  }
}
