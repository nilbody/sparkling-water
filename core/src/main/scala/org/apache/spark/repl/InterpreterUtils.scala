package org.apache.spark.repl

import java.net.URL

import org.apache.spark.SparkEnv

import scala.tools.nsc.interpreter.AbstractFileClassLoader
import scala.tools.nsc.util.ScalaClassLoader.URLClassLoader

/**
 * Various utils needed to work for the interpreters. Mainly this object is used to register repl classloader, which is
 * shared amongs all the interpreters.
 * For the first time the H2OIMain is initialized, id creates the repl classloader and stores it here. Other instances
 * of H2OIMain then obtain the classloader from here.
 */
 object InterpreterUtils{
  private var _replClassLoader: AbstractFileClassLoader = null
  private var _runtimeClassLoader: URLClassLoader with ExposeAddUrl = null // wrapper exposing addURL

  def getClassOutputDir = {
    ReplCLassServer.getClassOutputDirectory
  }

  def classServerUri = {
    if (!ReplCLassServer.isRunning) {
      ReplCLassServer.start()
    }
    ReplCLassServer.classServerUri
  }


  def REPLCLassLoader = this.synchronized{
    _replClassLoader
  }
  def ensureREPLClassLoader(classLoader: AbstractFileClassLoader) = this.synchronized{
    if(_replClassLoader == null) {
      _replClassLoader = classLoader
      SparkEnv.get.serializer.setDefaultClassLoader(_replClassLoader)
    }
  }
  def resetREPLCLassLoader() : Unit = this.synchronized{
    _replClassLoader = null
  }

  def runtimeClassLoader = this.synchronized{
    _runtimeClassLoader
  }
  def ensureRuntimeCLassLoader(classLoader: URLClassLoader with ExposeAddUrl) = this.synchronized{
    if(_runtimeClassLoader == null){
      _runtimeClassLoader = classLoader
    }
  }

}

private[repl] trait ExposeAddUrl extends URLClassLoader {
  def addNewUrl(url: URL) = this.addURL(url)
}