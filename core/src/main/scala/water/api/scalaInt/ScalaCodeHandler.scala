package water.api.scalaInt

import java.util.UUID

import org.apache.spark.SparkContext
import org.apache.spark.h2o.H2OContext
import org.apache.spark.repl.H2OILoop
import water.Iced
import water.api.Handler

import scala.collection.concurrent.TrieMap
import scala.compat.Platform

/**
 * ScalaCode Handler
 */
class ScalaCodeHandler(val sc: SparkContext, val h2oContext: H2OContext) extends Handler {

  val intrPoolSize = 1 // 1 only for development purposes
  val freeInterpreters = new java.util.concurrent.ConcurrentLinkedQueue[H2OILoop]
  val timeout = 300000 // 5 minutes in milliseconds
  var mapIntr = new TrieMap[String, (H2OILoop, Long)]
  initializeHandler()

  def interpret(version: Int, s: ScalaCodeV3): ScalaCodeV3 = {
    if (s.session_id == null || !mapIntr.isDefinedAt(s.session_id)) {
      // session ID not set
      s.response = "Create session ID using the address /3/scalaint"
    } else {
      mapIntr += s.session_id ->(mapIntr(s.session_id)._1, Platform.currentTime) // update the time
      val intp = mapIntr(s.session_id)._1
      s.status = intp.runCode(s.code).toString
      s.response = intp.interpreterResponse
      s.output = intp.printedOutput
    }
    s
  }

  def initSession(version: Int, s: ScalaSessionIdV3): ScalaSessionIdV3 = {
    val intp = getInterpreter()
    s.session_id = intp.sessionID
    s
  }

  def getInterpreter(): H2OILoop = {
    this.synchronized {
      if (!freeInterpreters.isEmpty) {
        val intp = freeInterpreters.poll()
        new Thread(new Runnable {
          def run(): Unit = {
            createInterpreter()
          }
        }).start()
        intp
      } else {
        // pool is empty at the moment and is being filled, return new interpreter without using the pool
        createInterpreter()
      }
    }
  }

  def destroySession(version: Int, s: ScalaMsgV3): ScalaMsgV3 = {
    mapIntr(s.session_id)._1.closeInterpreter()
    mapIntr -= s.session_id
    s.msg = "Session closed"
    s
  }

  def getSessions(version: Int, s: ScalaSessionsV3): ScalaSessionsV3 = {
    s.sessions = mapIntr.keys.toArray
    s
  }

  def initializeHandler(): Unit = {
    initializePool()
    val checkThread = new Thread(new Runnable {
      def run() {
        while (true) {
          mapIntr.foreach { case (id: String, (intr: H2OILoop, lastChecked: Long)) =>
            if (Platform.currentTime - lastChecked >= timeout) {
              mapIntr(id)._1.closeInterpreter()
              mapIntr -= id
            }
          }
          Thread.sleep(timeout)
        }
      }
    })
    checkThread.start()
  }

  def initializePool(): Unit = {
    for (i <- 0 until intrPoolSize) {
      createInterpreter()
    }
  }

  def createInterpreter(): H2OILoop = {
    val id = createUUID()
    val intp = ScalaCodeHandler.initializeInterpreter(sc, h2oContext,id)
    freeInterpreters.add(intp)
    mapIntr.put(id, (intp, Platform.currentTime))
    intp
  }

  def createUUID() : String = {
    var done = false
    var id = UUID.randomUUID().toString // simple solution for now ..
    do {
      if (!mapIntr.contains(id)) {
        done = true
      } else {
        id = UUID.randomUUID().toString
      }
    }
    while (!done)
    // id is also used as package for generated class names, dashes are not allowed and uuid is generated with dashes
    id = id.replaceAll("-","_")
    id
  }
}

object ScalaCodeHandler {
  def initializeInterpreter(sparkContext: SparkContext, h2oContext: H2OContext, sessionID: String): H2OILoop = {
    new H2OILoop(sparkContext, h2oContext,sessionID)
  }
}


private[api] class IcedCode(val session_id: String, val code: String) extends Iced[IcedCode] {

  def this() = this("", "") // initialize with empty values, this is used by the createImpl method in the
  //RequestServer, as it calls constructor without any arguments
}

private[api] class IcedSessions extends Iced[IcedSessions] {}

private[api] class IcedMsg(val session_id: String) extends Iced[IcedMsg] {
  def this() = this("")
}

private[api] class IcedSessionId extends Iced[IcedSessionId] {}


