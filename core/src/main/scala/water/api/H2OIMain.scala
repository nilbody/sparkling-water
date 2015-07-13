package water.api

import java.io.{PrintWriter, StringWriter}

import scala.tools.nsc.{ConsoleWriter, NewLinePrintWriter, interpreter, Settings}
import scala.tools.nsc.interpreter.{JPrintWriter, IMain}

class H2OIMain(settings: Settings, out : StringWriter) extends IMain(settings,new PrintWriter(out)){

  def getOutputStream(): StringWriter  ={
  return out
}

}
