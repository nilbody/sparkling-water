package org.apache.spark.repl

import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by kuba on 22/07/15.
 */
import scala.collection.mutable.Set

object H2OMain {
  private var _interp: H2OILoop = _

  def interp = _interp

  def interp_=(i: H2OILoop) { _interp = i }

}
