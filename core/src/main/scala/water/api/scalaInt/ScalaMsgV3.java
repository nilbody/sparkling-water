package water.api.scalaInt;

import water.api.API;
import water.api.Schema;

/**
 * Schema used for representing arbitrary text messages. ( So far used as message for DELETE request)
 */
public class ScalaMsgV3 extends Schema<IcedMsg, ScalaMsgV3> {
    @API(help = "Session id identifying the correct interpreter", direction = API.Direction.INPUT)
    public String session_id;

    @API(help = "Message from interpreter", direction = API.Direction.OUTPUT)
    public String msg;
}
