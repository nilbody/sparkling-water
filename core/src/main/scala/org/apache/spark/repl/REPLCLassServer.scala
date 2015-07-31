/**
 * This code is based on code org.apache.spark.repl.SparkIMain released under Apache 2.0"
 * Link to Github: https://github.com/apache/spark/blob/master/repl/scala-2.10/src/main/scala/org/apache/spark/repl/SparkIMain.scala
 * Author:  Paul Phillips
 */

package org.apache.spark.repl

import org.apache.spark.util.Utils
import org.apache.spark.{HttpServer, Logging, SecurityManager, SparkConf}


/**
 * Various interpreter utils
 */
object REPLCLassServer extends Logging {

  private var started = false
  private val conf = new SparkConf()

  private val SPARK_DEBUG_REPL: Boolean = (System.getenv("SPARK_DEBUG_REPL") == "1")
  /** Local directory to save .class files too */
  private lazy val outputDir = {
    val tmp = System.getProperty("java.io.tmpdir")
    val rootDir = conf.get("spark.repl.classdir",  tmp)
    Utils.createTempDir(rootDir)
  }
  logInfo("Directory to save .class files to = " + outputDir)
  /**
   * Returns the path to the output directory containing all generated
   * class files that will be served by the REPL class server.
   */
  lazy val getClassOutputDirectory = outputDir

  //private val virtualDirectory                              = new PlainFile(outputDir) // "directory" for classfiles
  /** Jetty server that will serve our classes to worker nodes */
  private val classServerPort                               = conf.getInt("spark.replClassServer.port", 0)
  private val classServer                                   = new HttpServer(conf, outputDir, new SecurityManager(conf), classServerPort, "HTTP class server")

  def classServerUri = classServer.uri
  def start() ={
    classServer.start()
    started = true
    logInfo("Class server started, URI = " + classServerUri)
  }
  def close() {
    classServer.stop()
  }

  def isRunning() = {
    started
  }
}
