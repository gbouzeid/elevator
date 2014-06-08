package com.elevis.platform.common

/**
 * handle the elevator or passenger travel direction
 * User: bouzeig
 */
class Direction extends Enumeration {
    type status = Value
    val Up = Value("up")
    val Down = Value("down")
    val Ready = Value("ready")
  }

/**
 * calculate the travel direction based on the fromFloor and the toFloor
 */
object Direction extends Direction {
  def getDirection(from : Int, to: Int) = {
    (to - from) match {
      case 0 => Ready
      case positive if (positive>0) => Up
      case _ => Down
    }
  }
}
