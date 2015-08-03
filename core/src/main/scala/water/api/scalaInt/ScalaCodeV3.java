package water.api.scalaInt;


import water.api.API;
import water.api.Schema;

/**
 * Schema used for representing scala code
 */
public class ScalaCodeV3 extends Schema<IcedCode, ScalaCodeV3> {
    @API(help = "Session id identifying the correct interpreter", direction = API.Direction.INPUT)
    public int session_id;

    @API(help = "Scala code to interpret", direction = API.Direction.INPUT)
    public String code;

    @API(help = "Status of the code execution", direction = API.Direction.OUTPUT)
    public String status;

    @API(help = "Response of the interpreter", direction = API.Direction.OUTPUT)
    public String response;

    @API(help = "Redirected console output, for example output of println is stored to this field",
            direction = API.Direction.OUTPUT)
    public String output;
}

