package demo.test

import java.io._

object Serialization {
  def serialize[T](o: T, filename: String) {
    val bos = new FileOutputStream(filename)
    val oos = new ObjectOutputStream(bos)
    oos.writeObject(o)
    oos.close()
  }


  def deserialize[T](filename: String): T = {
    val bis = new FileInputStream(filename)
    val ois = new ObjectInputStream(bis)
    ois.readObject.asInstanceOf[T]
  }
}
