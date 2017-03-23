/**
  * @author Yex
  */

package benchmarking

import java.util.concurrent.CountDownLatch

import akka.actor.{Actor, ActorRef}
import benchmarking.messsage.{PingMessage, PongMessage, StartBatchMessage, StartMessage}

/**
  * Sends messages and waits them back.
  * @param pong - receiver
  * @param latch - latch to see if work is done
  * @param maxMessageCount - number of messages to be sent
  */
class PingActor(
                     pong: ActorRef,
                     latch: CountDownLatch,
                     maxMessageCount: Long
                   ) extends Actor {

  var sent = 0L
  var received = 0L

  override def receive: Receive = {
    case StartMessage =>
      sent += 1
      pong ! PingMessage
    case StartBatchMessage =>
      for (i <- 0L until maxMessageCount) {
        sent += 1
        pong ! PingMessage
      }
    case PongMessage =>
      received += 1
      if (sent < maxMessageCount) {
        sent += 1
        pong ! PingMessage
      } else if (received >= maxMessageCount) {
        latch.countDown()
      }
  }

}