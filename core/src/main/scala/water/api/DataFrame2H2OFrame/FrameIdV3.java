package water.api.DataFrame2H2OFrame;

import water.api.API;
import water.api.Schema;

/**
 * Schema used for representing frame name
 */
public class FrameIdV3 extends Schema<IcedFrameName, FrameIdV3> {
    @API(help = "Id of frame to be transformed", direction = API.Direction.INPUT)
    public String frame_id;

    @API(help = "Id of frame after transformation", direction = API.Direction.OUTPUT)
    public String transformedId;
}
