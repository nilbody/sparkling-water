/**
 * This code is based on code org.apache.spark.repl.SparkIMain released under Apache 2.0"
 * Link to Github: https://github.com/apache/spark/blob/master/repl/scala-2.10/src/main/scala/org/apache/spark/repl/Main.scala
 */
package org.apache.spark.repl

/**
 * Main starting point for sparkling-shell
 */
object H2OMain {
  private var _interp: H2OILoop = _

  def interp = _interp

  def interp_=(i: H2OILoop) { _interp = i }

  def main(args: Array[String]) {
    _interp = new H2OILoop
    _interp.process(args)
  }
}
