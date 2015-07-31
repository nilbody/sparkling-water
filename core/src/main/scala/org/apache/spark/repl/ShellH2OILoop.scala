package org.apache.spark.repl

import java.net.URI

import org.apache.spark.util.Utils

import scala.tools.nsc.SparkHelper
import scala.tools.nsc.interpreter.Formatting
import scala.tools.nsc.util.ClassPath


class ShellH2OILoop extends SparkILoop{

  class H2OILoopInterpreter extends H2OIMain(settings,out) {
    outer =>
    override private[repl] lazy val formatting = new Formatting {
      def prompt = ShellH2OILoop.this.prompt
    }
    override protected def parentClassLoader = SparkHelper.explicitParentLoader(settings).getOrElse(classOf[H2OILoop].getClassLoader)
  }
  /**
   * Constructs a new interpreter.
   */
  override protected def createInterpreter() {
    this.createInterpreter()
    intp = new H2OILoopInterpreter
  }

}
