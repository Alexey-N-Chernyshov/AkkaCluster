/**
  * @author Yex
  */

package benchmarking

import akka.actor.Actor
import benchmarking.messsage.{PingMessage, PongMessage}

/**
  * Replyes on the message, ponging back to the sender
  */
class PongActor extends Actor {
  override def receive: Receive = {
    case PingMessage =>
      sender ! PongMessage
  }
}
