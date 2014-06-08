package com.elevis.platform.common

import scala.collection.mutable
import akka.actor.Actor
import java.util.Calendar

/**
 * User: bouzeig
 *
 * timeBetweenFloors: in ms per floor
 * TODO should include floor range
 * TODO replace the println by the logging
 * TODO use conf/property file
 */
class Elevator(timeBetweenFloors : Long, timeToStart: Long, timeToStop: Long, maxWeight: Double) extends StateMachine {
  private var direction : Direction.Value = Direction.Ready
  private var currentFloor : Int = 0
  private var idle = true

  private val onboardedPassengers : mutable.ListBuffer[Passenger] = mutable.ListBuffer()
  private val queuedPassenger : mutable.ListBuffer[Passenger] = mutable.ListBuffer()

  /**
   * main method for receiving messages
   * @return
   */
  def receive: Actor.Receive = basic orElse {
    case (passenger : Passenger) => {
      if (direction==Direction.Ready || passenger.direction==direction) {
        queuedPassenger+=passenger
        direction = Direction.getDirection(currentFloor, passenger.fromFloor) match {
          case Direction.Ready => passenger.direction
          case other => other
        }
        if (idle) {
          idle = false    // if it was idle, prepare to exit this mode since passenger was received
          goto(Idle())
        }
        sender ! true       // passenger was added
      } else {
        sender ! false      // passenger could not be added
      }
    }
    case (direction : Direction.Value, fromFloor: Int) => sender ! getOptimum(direction, fromFloor)
    case Command.Status => println(status())  // TODO logging needed
  }

  def getOptimum(direction : Direction.Value, fromFloor : Int) : Double = {
    // TODO implement algo - get algo class based on config
    maxWeight
  }

  /**
   * compute how many floors remaining until next stop
   * @return
   */
  def computeNextFloor : Int = {
    if (!onboardedPassengers.isEmpty || !queuedPassenger.isEmpty) {
      // compute the next floor to stop on to - beaware, onboardedPassengers or queuedPassenger might be empty
      direction match {
        case Direction.Up=>List(if (onboardedPassengers.isEmpty) Int.MaxValue else onboardedPassengers.minBy(_.toFloor).toFloor, if (queuedPassenger.isEmpty) Int.MaxValue else queuedPassenger.minBy(_.fromFloor).fromFloor).min
        case Direction.Down=>List(if (onboardedPassengers.isEmpty) Int.MinValue else onboardedPassengers.maxBy(_.toFloor).toFloor, if (queuedPassenger.isEmpty) Int.MinValue else queuedPassenger.maxBy(_.fromFloor).fromFloor).max
        case _=>currentFloor
      }
    } else {
      currentFloor
    }
  }

  /**
   * Starting state - Moving state is next
   */
  final case class Starting() extends State("Starting") {
    def run = {
      println(status(this.getClass()))
      waitgo(Moving(computeNextFloor), timeToStart)
    }
  }

  /**
   * Moving - up or down
   * Stopping or Moving states are next
   * floors contains the value "in how many floors the elevator should be stopping"
   */
  final case class Moving(floors: Int) extends State("Moving") {
    def run = {
      println(status(this.getClass()))
      direction match {
        case Direction.Up =>currentFloor+=1
        case Direction.Down =>currentFloor-=1
      }

      floors match {
        case 1=> waitgo(Stopping(), timeBetweenFloors)
        case _=> waitgo(Moving(scala.math.abs(computeNextFloor-currentFloor)), timeBetweenFloors)
      }
    }
  }

  /**
   * Stopping state - OffBoarding state is next
   */
  final case class Stopping() extends State("Stopping") {
    def run = {
      println(status(this.getClass()))
      waitgo(OffBoarding(), timeToStop)
    }
  }

  /**
   * OffBoarding state = OnBoarding state is next
   */
  final case class OffBoarding() extends State("Off Boarding") {
    def run = {
      println(status(this.getClass()))
      val offBoardingPassengers = onboardedPassengers.filter(passenger=>passenger.toFloor==currentFloor)
      if (!offBoardingPassengers.isEmpty) {
        offBoardingPassengers.foreach(passenger => onboardedPassengers -= passenger)
      }
      waitgo(OnBoarding(), 500L + offBoardingPassengers.length*500L)   // TODO this should be from a property -
                                                                       // for now assuming minimum offboarding time is 0.5s plus 0.5 per passenger
    }
  }

  /**
   * OnBoarding state - Idle state is next
   */
  final case class OnBoarding() extends State("On Boarding") {
    def run = {
      println(status(this.getClass()))
      // check if the direction should be switched
      if (!queuedPassenger.isEmpty) {
        if (queuedPassenger.filter(passenger=>passenger.direction==direction).isEmpty) {
          // switch direction
          direction = queuedPassenger(0).direction    // since all passengers have the same direction
        }
      }
      // on boarding passengers
      val onBoardingPassengers = queuedPassenger.filter(passenger=>passenger.fromFloor==currentFloor)     // TODO we may want to double check the direction
      if (!onBoardingPassengers.isEmpty) {
        onBoardingPassengers.foreach(passenger => {
          onboardedPassengers += passenger
          queuedPassenger -= passenger
        })
      }
      waitgo(Idle(), 500L + onBoardingPassengers.length*500L)   // TODO this should be from a property
    }
  }

  /**
   * Idle state -
   * Starting or OnBoarding are next if passengers are available to either off board or on board
   */
  final case class Idle() extends State("Going into idle mode") {
    def run = {
      println(status(this.getClass()))
      if (!queuedPassenger.isEmpty || !onboardedPassengers.isEmpty) {
        computeNextFloor match {
          case floor if (floor==currentFloor)=>goto(OnBoarding())
          case _=>goto(Starting())
        }
      } else {
        idle = true
        direction = Direction.Ready
      }
    }
  }

  def status(currentClass : Object = this.getClass(), delimiter : String = ", ") = {
    "Elevator: '" + this.hashCode() + "'" + delimiter +
    "Class: '" + currentClass + "'" + delimiter +
    "Time: '" + Calendar.getInstance().getTime() + "'" + delimiter +
    "Travel direction: '" + direction + "'" + delimiter +
    "currentFloor: '" + currentFloor + "'" + delimiter  +
    "waiting passengers: '" + queuedPassenger.toString() + "'" + delimiter  +
    "onboarded passengers: '" + onboardedPassengers.toString() + "'."
  }


}