package water.api;



/**
 * Schema for /3/ScalaCode used to passing the scala code from H2O Flow to backend
 */
public class ScalaCodeV3 extends Schema<IcedCode, ScalaCodeV3> {
    @API(help = "Scala code to interpret", direction = API.Direction.INPUT)
    public String code;

    @API(help = "Session Id used to identify subsequent calls", direction = API.Direction.INPUT)
    public String sessionId;
}