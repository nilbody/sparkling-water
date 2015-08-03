package org.apache.spark.h2o

import org.apache.spark.SparkConf
import org.apache.spark.repl.InterpreterUtils

/**
 * Wrapper around spark configuration
 * @param loadDefaults
 */
class SparklingConf(loadDefaults: Boolean)  {

  /** Create a SparkConf that loads defaults from system properties and the classpath */
  def this() = this(true)

  private val conf = new SparkConf(loadDefaults)
  conf.set("spark.repl.class.uri", InterpreterUtils.classServerUri)


  def sparkConf  = conf
}
