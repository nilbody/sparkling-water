package water.api.scalaInt

import java.io.StringWriter
import java.util.UUID

import org.apache.spark.SparkContext
import water.Iced
import water.api.{H2OIMain, Handler}
import water.fvec.NFSFileVec

import scala.collection.concurrent.TrieMap
import scala.compat.Platform
import scala.tools.nsc.Settings

/**
 * ScalaCode Handler
 */
class ScalaCodeHandler(val sc: SparkContext) extends Handler {

  val intrPoolSize = 3
  val freeInterpreters = new java.util.concurrent.ConcurrentLinkedQueue[H2OIMain]
  val sparkContext: SparkContext = sc
  var mapIntr = new TrieMap[String, (H2OIMain, Long)]
  val timeout = 300000 // 5 minutes in milliseconds
  initializeHandler()

  def interpret(version: Int, s: ScalaCodeV3): ScalaCodeResultV3 = {
    val reply = new ScalaCodeResultV3
    if (s.session_id == null || !mapIntr.isDefinedAt(s.session_id)) {
      // session ID not set
      reply.response = "Create session ID using the address /3/scalaint"
    } else {
      mapIntr += s.session_id ->(mapIntr(s.session_id)._1, Platform.currentTime) // update the time
      val interpreter = mapIntr(s.session_id)._1
      reply.status = interpreter.interpret(s.code).toString
      reply.response = interpreter.getOutputStream().toString
      interpreter.getOutputStream().getBuffer.setLength(0)
    }
    reply
  }

  def initSession(version: Int, s: ScalaSessionIdV3): ScalaSessionIdV3 = {
    val intr = getInterpreter()
    var done = false
    var id = UUID.randomUUID().toString // simple solution for now ..
    do {
      val previous = mapIntr.putIfAbsent(id, (intr, Platform.currentTime))
      if (previous == None) {
        done = true
      } else {
        id = UUID.randomUUID().toString
      }
    }
    while (!done)
    val reply = new ScalaSessionIdV3
    reply.session_id = id
    reply
  }

  def destroySession(version: Int, s: ScalaMsgV3) : ScalaMsgV3 = {
    mapIntr(s.session_id)._1.close()
    mapIntr -= s.session_id
    val scalaMsg = new ScalaMsgV3
    scalaMsg.msg = "Session closed"
    scalaMsg
  }

  def getSessions(version: Int, s: ScalaSessionsV3) : ScalaSessionsV3 = {
    val scalaCodeSessions = new ScalaSessionsV3
    scalaCodeSessions.sessions = mapIntr.keys.toArray
    scalaCodeSessions
  }

  def getInterpreter(): H2OIMain = {
    this.synchronized {
      if (!freeInterpreters.isEmpty) {
        val intr = freeInterpreters.poll()
        new Thread(new Runnable {
          def run(): Unit = {
            freeInterpreters.add(ScalaCodeHandler.initializeInterpreter(sc))
          }
        }).start()
        intr
      } else {
        // pool is empty at the moment and is being filled, return new interpreter without using the pool
        ScalaCodeHandler.initializeInterpreter(sc)
      }
    }
  }

  def initializePool(): Unit = {
    for (i <- 0 to intrPoolSize) {
      freeInterpreters.add(ScalaCodeHandler.initializeInterpreter(sc))
    }
  }

  def initializeHandler(): Unit ={
    initializePool()
    val checkThread = new Thread(new Runnable {
      def run() {
        while (true) {
          mapIntr.foreach { case (id: String, (intr: H2OIMain, lastChecked: Long)) => {
            if (Platform.currentTime - lastChecked >= timeout) {
              mapIntr(id)._1.close()
              mapIntr -= id
            }
          }
          }
          Thread.sleep(timeout)
        }
      }
    })
    checkThread.start()
  }
}

object ScalaCodeHandler {
  def initializeInterpreter(sparkContext: SparkContext): H2OIMain = {
    val settings = new Settings
    settings.usejavacp.value = true
    // setup the classloader of some H2O class
    settings.embeddedDefaults[NFSFileVec]
    val imain = new H2OIMain(settings, new StringWriter())
    imain.quietBind("sc", sparkContext)
    imain
  }
}


private[api] class IcedCode(val session_id: String,val code: String) extends Iced[IcedCode] {

  def this() = this("", "") // initialize with empty values, this is used by the createImpl method in the
  //RequestServer, as it calls constructor without arguments
}
private[api] class IcedCodeResult extends Iced[IcedCodeResult] {}
private[api] class IcedSessions extends Iced[IcedSessions] {}
private[api] class IcedMsg(val session_id: String, val msg: String) extends Iced[IcedMsg] {
  def this() = this("","")
}
private[api] class IcedSessionId extends Iced[IcedSessionId] {}


