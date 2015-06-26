package water.api

import java.io.{ByteArrayOutputStream, PrintWriter}
import java.util.UUID

import org.apache.spark.SparkContext
import org.apache.spark.repl.{SparkILoop, SparkIMain}
import water.Iced
import water.fvec.NFSFileVec

import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.{NamedParam, ILoop, IMain}


/**
 * ScalaCode Handler
 */
class ScalaCodeHandler(val sc: SparkContext)  extends Handler {

  val sparkContext: SparkContext = sc
  var mapIntr : Map[String, IMain] = Map()

  def interpret(version:Int, s: ScalaCodeV3): ScalaCodeResultV3 = {
    val reply = new ScalaCodeResultV3
    if (s.sessionId == null || !mapIntr.isDefinedAt(s.sessionId)) {
      // session ID not set so far
      reply.response = "Create session ID using the address /3/sessionId"
    } else {
      reply.result = mapIntr(s.sessionId).interpret(s.code).toString
      reply.sessionId = s.sessionId
    }
    reply
  }

  def createSession(version:Int, s: ScalaCodeV3) :  ScalaCodeResultV3 = {
    val intr = initializeInterpreter();
    val id = UUID.randomUUID().toString // simple solution for now ..
    mapIntr += id -> intr
    val reply = new ScalaCodeResultV3
    reply.sessionId = id
    reply
  }

  def initializeInterpreter(): IMain = {
    val settings = new Settings
    settings.usejavacp.value = true
    // setup the classloader of some H2O class
    settings.embeddedDefaults[NFSFileVec]
    val imain = new IMain(settings)
    imain.quietBind("sc",sc)
    imain
  }

}

private[api] class IcedCode(val code: String, val sessionId: String) extends Iced[IcedCode] {

def this() = this("","") // initialize with the empty code, this is used by the createImpl method in the
  //RequestServer, as it calls constructor without arguments
}

private[api] class IcedResult extends Iced[IcedResult] {
}


