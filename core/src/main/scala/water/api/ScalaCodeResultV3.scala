package water.api

/**
 * Schema for /3/ScalaCode used as a result after passing the scala code from H2O Flow to backend
 */
class ScalaCodeResultV3 extends Schema[IcedResult,ScalaCodeResultV3] {
  @API(help = "Result of the code execution", direction = API.Direction.OUTPUT)
  var result: String = _
  @API(help = "Response of the REPL", direction = API.Direction.OUTPUT)
  var response: String = _
  @API(help = "Session Id used to identify subsequent calls", direction = API.Direction.INPUT)
  var sessionId: String = _
}
