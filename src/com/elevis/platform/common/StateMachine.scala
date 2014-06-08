package com.elevis.platform.common


import akka.actor.{Deploy, Props, Actor}
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * User: bouzeig
 */
trait StateMachine extends Actor {
  def factory = ConfigFactory load
  lazy val config = factory

  /**
   * go to the next state immediately
   * @param state
   */
  def goto(state: State) = self ! SwitchState(state)

  /**
   * wait a delay below going into the next state
   * @param state
   * @param delay
   * @return
   */
  def waitgo(state: State, delay: Long) = context.system.scheduler.scheduleOnce(delay milliseconds, self, SwitchState(state))

  def basic: Receive = {
    case SwitchState(state) => state run
  }

  // TODO those should be in a superviser type of trait
  def spawn(who: => Actor) = context.actorOf(Props(who).withDeploy(Deploy(config = config)))
  def named(who: => Actor, as: String) = context.actorOf(Props(who).withDeploy(Deploy(config = config)), as)
}

/**
 * for state switching - handled by the StateMachine class
 * @param what
  */
final case class SwitchState(what: State)

/**
 * simple state abstract class
 * @param tag or name or description
 */
abstract class State(tag: String) {
  override def toString = tag
  def run: Unit
}
