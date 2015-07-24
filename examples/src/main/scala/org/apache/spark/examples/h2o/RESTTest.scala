package org.apache.spark.examples.h2o

import org.apache.spark.h2o.H2OContext
import org.apache.spark.repl.REPLCLassServer
import org.apache.spark.{SparkConf, SparkContext}
import water.app.SparkContextSupport


object RESTTest extends SparkContextSupport with org.apache.spark.Logging {

  def main(args: Array[String]): Unit = {
    // Configure this application
    val conf: SparkConf = new SparkConf()
    conf.set("spark.repl.class.uri",REPLCLassServer.classServerUri)
    // Create SparkContext to execute application on Spark cluster
    val sc = new SparkContext(conf)
    val h2oContext = new H2OContext(sc).start()

    // Infinite wait
    this.synchronized( while(true) wait )
  }
}
