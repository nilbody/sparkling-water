package water.api.scalaInt

import java.io.{ByteArrayOutputStream, PrintStream, StringWriter}
import java.util.UUID

import org.apache.spark.SparkContext
import org.apache.spark.h2o.H2OContext
import org.apache.spark.sql.SQLContext
import water.Iced
import water.api.{H2OIMain, Handler}
import water.fvec.NFSFileVec

import scala.collection.concurrent.TrieMap
import scala.compat.Platform
import scala.tools.nsc.Settings

/**
 * ScalaCode Handler
 */
class ScalaCodeHandler(val sc: SparkContext, val h2oContext: H2OContext) extends Handler {

  val intrPoolSize = 3
  val freeInterpreters = new java.util.concurrent.ConcurrentLinkedQueue[H2OIMain]
  var mapIntr = new TrieMap[String, (H2OIMain, Long)]
  val timeout = 300000 // 5 minutes in milliseconds


  initializeHandler()
  def interpret(version: Int, s: ScalaCodeV3): ScalaCodeV3 = {
    if (s.session_id == null || !mapIntr.isDefinedAt(s.session_id)) {
      // session ID not set
      s.response = "Create session ID using the address /3/scalaint"
    } else {
      mapIntr += s.session_id ->(mapIntr(s.session_id)._1, Platform.currentTime) // update the time
      val interpreter = mapIntr(s.session_id)._1
      Console.withOut(interpreter.printStream){
        s.status = interpreter.interpret(s.code).toString
      }
      s.response = interpreter.getOutputStringWriter().toString
      interpreter.getOutputStringWriter().getBuffer.setLength(0) // reset the writer
      s.output = interpreter.printed
    }
    s
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
    s.session_id = id
    s
  }

  def destroySession(version: Int, s: ScalaMsgV3) : ScalaMsgV3 = {
    mapIntr(s.session_id)._1.close()
    mapIntr -= s.session_id
    s.msg = "Session closed"
    s
  }

  def getSessions(version: Int, s: ScalaSessionsV3) : ScalaSessionsV3 = {
    s.sessions = mapIntr.keys.toArray
    s
  }

  def getInterpreter(): H2OIMain = {
    this.synchronized {
      if (!freeInterpreters.isEmpty) {
        val intr = freeInterpreters.poll()
        new Thread(new Runnable {
          def run(): Unit = {
            freeInterpreters.add(ScalaCodeHandler.initializeInterpreter(sc,h2oContext))
          }
        }).start()
        intr
      } else {
        // pool is empty at the moment and is being filled, return new interpreter without using the pool
        ScalaCodeHandler.initializeInterpreter(sc,h2oContext)
      }
    }
  }

  def initializePool(): Unit = {
    for (i <- 0 until intrPoolSize) {
      freeInterpreters.add(ScalaCodeHandler.initializeInterpreter(sc,h2oContext))
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
  def initializeInterpreter(sparkContext: SparkContext, h2OContext: H2OContext): H2OIMain = {
    val settings = new Settings
    settings.usejavacp.value = true
    // setup the classloader of some H2O class
    settings.embeddedDefaults[NFSFileVec]
    val imain = new H2OIMain(settings, new StringWriter())
    imain.quietBind("sc", sparkContext)
    imain.quietBind("h2oContext",h2OContext)
    imain.quietBind("sqlContext",new SQLContext(sparkContext))
    imain
  }
}


private[api] class IcedCode(val session_id: String,val code: String) extends Iced[IcedCode] {

  def this() = this("", "") // initialize with empty values, this is used by the createImpl method in the
  //RequestServer, as it calls constructor without arguments
}
private[api] class IcedSessions extends Iced[IcedSessions] {}
private[api] class IcedMsg(val session_id: String, val msg: String) extends Iced[IcedMsg] {
  def this() = this("","")
}
private[api] class IcedSessionId extends Iced[IcedSessionId] {}


