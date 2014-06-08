package com.elevis.platform.controls

import com.elevis.platform.common.{Command, StateMachine, Elevator, Passenger}
import akka.actor.{Actor}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.{Await}
import scala.concurrent.duration._

/**
 * A simple unit of control class - maybe I should rename it UnitOfControl
 * User: bouzeig
 */
final class Unit extends StateMachine {
  implicit lazy val genericTimeout = Timeout(1 second)     // TODO timeout/genericTimeout should be from a property
  lazy val timeout = 1000 milliseconds                     // same as above.... property/config!!

  // initialized elevators actors
  private val elevators = List(spawn(new Elevator(3000, 1000, 2000, 2000.0)), spawn(new Elevator(3000, 1000, 2000, 2000.0))) // TODO load this from a property - also include BANKs

  def receive: Actor.Receive = basic orElse {
    case (passenger : Passenger) => {
      val future = elevators.maxBy(elevator => Await.result((elevator ? (passenger.direction, passenger.fromFloor)), timeout).asInstanceOf[Double]) ? passenger
      if (!(Await.result(future, timeout).asInstanceOf[Boolean])) {
        self ! passenger    // if adding was not successful, retry - currently infinite number of time - we should check into a weightened solution
                              // (higher priority would be given to passenger waiting longer
      }
    }
    case Command.Status => elevators.foreach( _ ! Command.Status)     // get the status from all elevators
    case Command.Exit => {
      // TODO check into using PoisonPill
      context.system.shutdown()
    }
  }
}
