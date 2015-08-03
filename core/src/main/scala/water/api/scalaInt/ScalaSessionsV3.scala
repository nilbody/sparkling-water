package water.api.scalaInt

import water.api.{API, Schema}

/**
 * Schema used for representing all valid scala interpreters sessions
 */
class ScalaSessionsV3 extends Schema[IcedSessions,ScalaSessionsV3]{
  @API(help = "List of session IDs", direction = API.Direction.OUTPUT)
  var sessions: Array[Int] = _
}
