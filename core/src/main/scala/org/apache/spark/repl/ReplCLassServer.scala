/**
 * This code is based on code org.apache.spark.repl.SparkIMain released under Apache 2.0"
 * Link to Github: https://github.com/apache/spark/blob/master/repl/scala-2.10/src/main/scala/org/apache/spark/repl/SparkIMain.scala
 * Author:  Paul Phillips
 */

package org.apache.spark.repl

import org.apache.spark.util.Utils
import org.apache.spark.{HttpServer, Logging, SecurityManager, SparkConf}


/**
 * HTTP Server containing classes defined in repl
 */
private[repl] object ReplCLassServer extends Logging {

  lazy val getClassOutputDirectory = outputDir
  /** Local directory to save .class files too */
  private lazy val outputDir = {
    val tmp = System.getProperty("java.io.tmpdir")
    val rootDir = conf.get("spark.repl.classdir",  tmp)
    Utils.createTempDir(rootDir)
  }
  private val conf = new SparkConf()
  private val SPARK_DEBUG_REPL: Boolean = System.getenv("SPARK_DEBUG_REPL") == "1"

  logInfo("Directory to save .class files to = " + outputDir)
  /** Jetty server that will serve our classes to worker nodes */
  private val classServerPort = conf.getInt("spark.replClassServer.port", 0)
  private val classServer = new HttpServer(conf, outputDir, new SecurityManager(conf), classServerPort, "HTTP class server")
  private var _isRunning = false

  def start() ={
    classServer.start()
    _isRunning = true
    logInfo("Class server started, URI = " + classServerUri)
  }

  def classServerUri = classServer.uri

  def close() {
    classServer.stop()
  }

  def isRunning: Boolean = {
    _isRunning
  }
}
