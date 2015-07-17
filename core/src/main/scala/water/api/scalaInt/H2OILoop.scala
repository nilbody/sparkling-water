package water.api.scalaInt

import java.io._

import org.apache.spark.SparkContext
import org.apache.spark.h2o.H2OContext
import org.apache.spark.sql.SQLContext

import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter._

/**
 * Class which creates the correct environment for interpreting scala code in the H2O environment and is used
 * to interpret bunch of scala code
 */
class H2OILoop(val sc: SparkContext, val h2oContext: H2OContext, outWriter: StringWriter = new StringWriter()) extends ILoop(None, new JPrintWriter(outWriter)) {

  createSettings()
  createInterpreter()
  private val baos = new ByteArrayOutputStream()
  private val printStream = new PrintStream(baos)

  def runCode(code: String): String = {
    import java.io.{BufferedReader, StringReader}
    // set the input stream
    val input = new BufferedReader(new StringReader(code))
    in = SimpleReader(input, out, false)
    // redirect output from console to our own stream

    scala.Console.withOut(printStream) {
      try loop()
      catch AbstractOrMissingHandler()
    }
    if (intp.reporter.hasErrors) {
      "Error"
    } else {
      "Success"
    }
  }

  override def createInterpreter(): Unit = {
    super.createInterpreter()
    addThunk({
      intp.quietImport("org.apache.spark.h2o._", "org.apache.spark.rdd._", "org.apache.spark._")
      intp.quietBind("sc", sc)
      intp.quietBind("h2oContext", h2oContext)
      intp.quietBind("sqlContext", new SQLContext(sc))
    })
    intp.initializeSynchronous()
    postInitialization()
    loadFiles(settings)
  }

  def interpreterResponse: String = {
    val res = outWriter.toString
    outWriter.getBuffer.setLength(0) // reset the writer
    res
  }

  def printedOutput: String = {
    val result = baos.toString()
    baos.reset()
    result
  }

  private def createSettings(): Unit = {
    settings = new Settings()
    settings.usejavacp.value = true

    // set the classloader of some H2O class
    settings.embeddedDefaults[H2OContext]
    // synchronous calls
    settings.Yreplsync.value = true
  }
}
