package water.api.scalaInt;


import water.api.API;
import water.api.Schema;


/**
 * Schema used for representing scala code
 */
public class ScalaCodeV3 extends Schema<IcedCode, ScalaCodeV3> {
    @API(help = "Session id identifying the correct interpreter", direction = API.Direction.INPUT)
    public String session_id;

    @API(help = "Scala code to interpret", direction = API.Direction.INPUT)
    public String code;
}

