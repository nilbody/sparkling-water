package org.apache.spark.examples.h2o

import org.apache.spark.h2o.H2OContext
import org.apache.spark.{SparkConf, SparkContext}
import water.app.SparkContextSupport


object RESTTest extends SparkContextSupport {

  def main(args: Array[String]): Unit = {
    // Configure this application

    val conf: SparkConf = configure("Sparkling Water: REST Test")
    // Create SparkContext to execute application on Spark cluster
    val sc = new SparkContext("local","REST TEST",conf)
    val h2oContext = new H2OContext(sc).start()

    // Infinite wait
    this.synchronized( while(true) wait )
  }
}
