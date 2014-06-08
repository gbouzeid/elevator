package com.elevis.platform

import com.elevis.platform.common.{Command, Passenger}
import scala.io.ReadStdin
import akka.actor.{Props, ActorSystem}

/**
 * the Main class or interface with the Unit of control / elevators
 * User: bouzeig
 *
 * TODO using the logging instead of println!!
 *
 */
object Main {
  lazy val system = ActorSystem("application")
  lazy val unit = system.actorOf(Props(new controls.Unit), name = "unit")

  def main(args: Array[String]) {

    var command: Command.Value = null
    do {
      command = Command.withName(ReadStdin.readLine().toLowerCase().trim())
      try {
        command match {
          case Command.Add => {
            println("enter: weight fromFloor toFloor")
            val passengerInfo : Array[String] = ReadStdin.readLine().toLowerCase().trim().split(" ")
            unit ! (new Passenger(passengerInfo.apply(0).toDouble, passengerInfo.apply(1).toInt, passengerInfo.apply(2).toInt))
            println("passenger being added")
          }
          case Command.Status => {
            unit ! Command.Status
          }
          case Command.Exit => {
            unit ! Command.Exit
          }
          case _ => {
            println("type: 'exit' to exit")
            println("type: 'status' for elevators status")
            println("type: 'add' to add a passenger in the available elevalor")
          }
        }
      } catch {
        case e : Exception =>
          e.printStackTrace()
          println("wrong command - type help for more information or exit to exit.")
      }
    } while (!Command.Exit.equals(command))
    println("Please wait: exiting...")
  }
}
