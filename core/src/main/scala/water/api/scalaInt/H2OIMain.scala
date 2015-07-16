package water.api.scalaInt

import scala.tools.nsc.interpreter.Results.Result
import scala.tools.nsc.interpreter.{IMain, JPrintWriter}
import scala.tools.nsc.{Settings, interpreter}

class H2OIMain(settings: Settings, out: JPrintWriter) extends IMain(settings,out){
  var lastResult: String = interpreter.IR.Success.toString
  override def interpret(line: String, synthetic: Boolean): Result = {
    val result = super.interpret(line, synthetic)
    lastResult = result.toString
    result
  }

}
