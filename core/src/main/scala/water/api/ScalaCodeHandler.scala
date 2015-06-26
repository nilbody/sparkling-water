package water.api

import org.apache.spark.SparkContext
import org.apache.spark.repl.{SparkILoop, SparkIMain}
import water.Iced

import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.{ILoop, IMain}


/**
 * ScalaCode Handler
 */
class ScalaCodeHandler(val sc: SparkContext)  extends Handler {

  val sparkContext: SparkContext = sc
  val intr = initializeInterpreter()

  def interpret(version:Int, s: ScalaCodeV3): ScalaCodeResultV3 = {
    val reply = new ScalaCodeResultV3
    reply.result = intr.interpret(s.code).toString
    reply
  }

  def initializeInterpreter(): IMain = {
    val settings = new Settings
    settings.usejavacp.value = true

    val imain = new IMain(settings)
    imain.bind("sc",sc)
    imain
  }


}


private[api] class IcedCode(val code: String) extends Iced[IcedCode] {

def this() = this("") // initialize with the empty code, this is used by the createImpl method in the
  //RequestServer, as it calls constructor without arguments
}

private[api] class IcedResult extends Iced[IcedResult] {
}


