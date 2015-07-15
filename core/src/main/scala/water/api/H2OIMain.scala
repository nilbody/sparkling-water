package water.api

import java.io.{ByteArrayOutputStream, PrintStream, PrintWriter, StringWriter}

import scala.tools.nsc.{ConsoleWriter, NewLinePrintWriter, interpreter, Settings}
import scala.tools.nsc.interpreter.{JPrintWriter, IMain}

class H2OIMain(settings: Settings, out : StringWriter) extends IMain(settings,new PrintWriter(out)){

  private val baos = new ByteArrayOutputStream()

  val printStream = new PrintStream(baos)

  def printed : String = {
    baos.toString()
  }

  def getOutputStringWriter(): StringWriter  = {
  return out
}

}
