package water.api

import java.io.{ByteArrayOutputStream, PrintWriter}
import java.util.UUID

import org.apache.spark.SparkContext
import org.apache.spark.repl.{SparkILoop, SparkIMain}
import water.Iced
import water.fvec.NFSFileVec

import scala.compat.Platform
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.{NamedParam, ILoop, IMain}


/**
 * ScalaCode Handler
 */
class ScalaCodeHandler(val sc: SparkContext)  extends Handler {

  val sparkContext: SparkContext = sc
  var mapIntr : scala.collection.mutable.Map[String, IMain] = scala.collection.mutable.Map()
  var mapTimes : Map[String,Long] = Map()
  val timeout = 30000 // 5 minutes in miliseconds
  val checkThread = new Thread(new Runnable {
    def run() {
      while(true){
        mapTimes.foreach{case (id: String, lastChecked: Long)=>{
          if(Platform.currentTime-lastChecked>=timeout){
            mapIntr(id).close()
            mapIntr -= id
            mapTimes -= id
          }
        }}
        Thread.sleep(timeout)
      }
    }
  })
  checkThread.start()

  def interpret(version:Int, s: ScalaCodeV3): ScalaCodeResultV3 = {
    val reply = new ScalaCodeResultV3
    if (s.sessionId == null || !mapIntr.isDefinedAt(s.sessionId)) {
      // session ID not set
      reply.response = "Create session ID using the address /3/sessionId"
    } else {
      mapTimes += s.sessionId -> Platform.currentTime // update the time
      reply.result = mapIntr(s.sessionId).interpret(s.code).toString
      reply.sessionId = s.sessionId
    }
    reply
  }

  def createSession(version:Int, s: ScalaCodeV3) :  ScalaCodeResultV3 = {
    val intr = ScalaCodeHandler.initializeInterpreter(sparkContext);
    val id = UUID.randomUUID().toString // simple solution for now ..
    mapIntr += id -> intr
    mapTimes += id -> Platform.currentTime
    val reply = new ScalaCodeResultV3
    reply.sessionId = id
    reply
  }


}

object ScalaCodeHandler{
  def initializeInterpreter(sparkContext: SparkContext): IMain = {
    val settings = new Settings
    settings.usejavacp.value = true
    // setup the classloader of some H2O class
    settings.embeddedDefaults[NFSFileVec]
    val imain = new IMain(settings)
    imain.quietBind("sc",sparkContext)
    imain
  }
}

private[api] class IcedCode(val code: String, val sessionId: String) extends Iced[IcedCode] {

def this() = this("","") // initialize with the empty code, this is used by the createImpl method in the
  //RequestServer, as it calls constructor without arguments
}

private[api] class IcedResult extends Iced[IcedResult] {
}


