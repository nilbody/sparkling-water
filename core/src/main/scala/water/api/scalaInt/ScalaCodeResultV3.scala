package water.api.scalaInt

import water.api.{API, Schema}

/**
 * Schema used for representing a result after passing the scala code from H2O Flow to scala interpreter
 * at the backend
 */
class ScalaCodeResultV3 extends Schema[IcedCodeResult,ScalaCodeResultV3] {
  @API(help = "Status of the code execution", direction = API.Direction.OUTPUT)
  var status: String = _
  @API(help = "Response of the interpreter", direction = API.Direction.OUTPUT)
  var response: String = _
}
