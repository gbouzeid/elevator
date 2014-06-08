package com.elevis.platform.common

/**
 * User: bouzeig
 *
 * As simple as it can get: a Passenger class!
 */
class Passenger(val weight : Double, val fromFloor: Int, val toFloor : Int) {
  val direction : Direction.Value = Direction.getDirection(fromFloor, toFloor)

  override def toString: String = {
    mkString()
  }

  def mkString(delimiter: String = ", "): String = {
    "Passenger: '" + this.hashCode() + "'" + delimiter +
    "Weight: '" + weight + "'" + delimiter +
    "fromFloor: '" + fromFloor + "'" + delimiter +
    "toFloor: '" + toFloor + "'" + delimiter +
    "direction: '" + direction + "'."
  }
}
