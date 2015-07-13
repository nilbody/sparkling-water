package water.api.scalaInt

import water.api.{Schema, API}

/**
 * Schema used for representing session id
 */
class ScalaSessionIdV3 extends Schema[IcedSessionId,ScalaSessionIdV3] {
  @API(help = "Session id identifying the correct interpreter", direction = API.Direction.OUTPUT)
  var session_id: String = _
}
