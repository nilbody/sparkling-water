package org.apache.spark.repl

import java.net.URL

import org.apache.spark.SparkEnv

import scala.tools.nsc.interpreter.AbstractFileClassLoader
import scala.tools.nsc.util.ScalaClassLoader.URLClassLoader


 object InterpreterUtils{
  def getClassOutputDir = {
      REPLCLassServer.getClassOutputDirectory
  }

  def classServerUri = {
    if(!REPLCLassServer.isRunning){
      REPLCLassServer.start()
    }
    REPLCLassServer.classServerUri
  }

  private var _replClassLoader : AbstractFileClassLoader = null
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

  private var _runtimeClassLoader : URLClassLoader with ExposeAddUrl = null // wrapper exposing addURL
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