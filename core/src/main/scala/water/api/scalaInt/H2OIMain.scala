package water.api.scalaInt

import scala.tools.nsc.interpreter.Results.Result
import scala.tools.nsc.interpreter.{IMain, JPrintWriter}
import scala.tools.nsc.{Settings, interpreter}

class H2OIMain(settings: Settings, out: JPrintWriter) extends IMain(settings,out){


  private var _lastResult: interpreter.IR.Result = interpreter.IR.Success
  override def interpret(line: String, synthetic: Boolean): Result = {
    val result = super.interpret(line, synthetic)
    // ensures that result incomplete or error remains to the end
    if(_lastResult == interpreter.IR.Success) {
      _lastResult = result
    }
    result
  }

  def lastResult(): String ={
    val result = _lastResult
    _lastResult = interpreter.IR.Success
    result.toString
  }
}
