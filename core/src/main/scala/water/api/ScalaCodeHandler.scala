package water.api

import org.apache.spark.SparkContext
import water.Iced

import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain


/**
 * ScalaCode Handler
 */
class ScalaCodeHandler(val sc: SparkContext)  extends Handler {

  val settings = new Settings
  settings.usejavacp.value = true
  val n = new IMain(settings)

  def interpret(version:Int, s: ScalaCodeV3): ScalaCodeResultV3 = {
    val reply = new ScalaCodeResultV3
    reply.result = n.interpret(s.code).toString
    reply
  }
}

private[api] class IcedCode(val code: String) extends Iced[IcedCode] {

def this() = this("") // initialize with the empty code, this is used by the createImpl method in the
  //RequestServer, as it calls constructor without arguments
}

private[api] class IcedResult extends Iced[IcedResult] {
}


