package org.apache.spark.examples.h2o

import org.apache.log4j.{Level, Logger}
import org.apache.spark.h2o.{SparklingConf, H2OContext}
import org.apache.spark.repl.{InterpreterUtils, REPLCLassServer}
import org.apache.spark.{SparkConf, SparkContext}
import water.app.SparkContextSupport


object InterpretersDemo extends SparkContextSupport with org.apache.spark.Logging {

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.INFO)
    // Configure this application
    val conf: SparkConf = new SparklingConf().sparkConf
    
    // Create SparkContext to execute application on Spark cluster
    val sc = new SparkContext(conf)
    val h2oContext = new H2OContext(sc).start()
    // Infinite wait
    this.synchronized( while(true) wait )
  }
}