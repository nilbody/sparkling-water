package water.api.DataFrame2H2OFrame

import org.apache.spark.SparkContext
import org.apache.spark.h2o.{H2OFrame, H2OContext}
import org.apache.spark.sql.SQLContext
import water.{DKV, Iced}
import water.api.Handler

/**
 * Handles transformation between DataFrame and H2OFrame based on the request
 */
class DataFrame2H2OFrameHandler(val sc: SparkContext, val h2oContext: H2OContext) extends Handler {
  implicit val sqlContext = new SQLContext(sc)

  def fromH2OFrameToDataFrame(version: Int, s: FrameIdV3): FrameIdV3 = {
    val value = DKV.get(s.frame_id)
    val h2oFrame: H2OFrame = value.get()
    val dataFrame = h2oContext.asDataFrame(h2oFrame)
    s.transformedId = "dataFrame" + s.frame_id
    dataFrame.registerTempTable(s.transformedId)
    s
  }
}

private[api] class IcedFrameName(val frame_id: String) extends Iced[IcedFrameName] {

  def this() = this("") // initialize with empty values, this is used by the createImpl method in the
  //RequestServer, as it calls constructor without any arguments
}
