package water.api

import java.io.{StringWriter, ByteArrayOutputStream, PrintWriter}
import java.util.UUID

import org.apache.spark.SparkContext
import water.Iced
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
  initializePool()
  val sparkContext: SparkContext = sc
  var mapIntr = new TrieMap[String, (H2OIMain,Long)]
  val timeout = 300000 // 5 minutes in milliseconds
  val checkThread = new Thread(new Runnable {
    def run() {
      while(true){
        mapIntr.foreach{case (id: String, (intr: H2OIMain, lastChecked: Long))=>{
          if(Platform.currentTime-lastChecked>=timeout){
            mapIntr(id)._1.close()
            mapIntr -= id
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
      reply.response = "Create session ID using the address /3/initinterpreter"
    } else {
      mapIntr += s.sessionId -> (mapIntr(s.sessionId)._1,Platform.currentTime) // update the time
      val interpreter = mapIntr(s.sessionId)._1
      reply.result =interpreter.interpret(s.code).toString
      reply.response = interpreter.getOutputStream().toString
      interpreter.getOutputStream().getBuffer.setLength(0)
      reply.sessionId = s.sessionId
    }
    reply
  }

  def createSession(version:Int, s: ScalaCodeV3) :  ScalaCodeResultV3 = {
    val intr = getInterpreter()
    var done = false
    var id = UUID.randomUUID().toString // simple solution for now ..
    do{
      val previous = mapIntr.putIfAbsent(id, (intr, Platform.currentTime))
      if(previous == None){
        done = true
      }else{
        id = UUID.randomUUID().toString
      }
    }
    while (!done)
    val reply = new ScalaCodeResultV3
    reply.sessionId = id
    reply
  }

  def initializePool(): Unit ={
    for(i <- 0 to intrPoolSize){
      freeInterpreters.add(ScalaCodeHandler.initializeInterpreter(sc))
    }
  }

  def getInterpreter(): H2OIMain ={
      this.synchronized{
        if(!freeInterpreters.isEmpty) {
          val intr = freeInterpreters.poll()
          new Thread(new Runnable {
            def run(): Unit = {
                freeInterpreters.add(ScalaCodeHandler.initializeInterpreter(sc))
            }
          }).start()
          intr
        }else{
          // pool is empty at the moment and is being filled, return new interpreter without using the pool
          ScalaCodeHandler.initializeInterpreter(sc)
        }
      }
  }
}

object ScalaCodeHandler{
  def initializeInterpreter(sparkContext: SparkContext): H2OIMain = {
    val settings = new Settings
    settings.usejavacp.value = true
    // setup the classloader of some H2O class
    settings.embeddedDefaults[NFSFileVec]
    val imain = new H2OIMain(settings,new StringWriter())
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


