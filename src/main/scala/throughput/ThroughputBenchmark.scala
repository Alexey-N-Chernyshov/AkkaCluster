/**
  * @author Yex
  */

package throughput

import java.util.concurrent.{CountDownLatch, TimeUnit}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * The goal is to benchmark the throughput of Akka.
  * 1st scenario is to get maximum throughput on a single machine.
  */
object ThroughputBenchmark {

  def main(args: Array[String]): Unit = {
    //runScenario(2, 1000000)
    runScenario(2, 1000000)
    runScenario(20, 1000000)
    runScenario(100, 1000000)
    runScenario(200, 1000000)
  }

  /**
    * Runs scenario.
    * @param pairs - number of ping-pong pairs
    * @param messageCount
    */
  def runScenario(pairs: Int, messageCount: Long): Unit = {
    val latch = new CountDownLatch(pairs)
    val config = ConfigFactory.load()
    val throughputDispatcher = "benchmark.throughput-dispatcher"
    val system = ActorSystem("benchmark", config.getConfig("benchmark").withFallback(config))
    val pongs = for (i <- 0 until pairs)
      yield system.actorOf(Props[Pong].withDispatcher(throughputDispatcher))
    val pings = for (pong <- pongs)
      yield system.actorOf(Props(new Ping(pong, latch, messageCount)).withDispatcher(throughputDispatcher))

    val maxRunDuration = 1000000
    val start = System.nanoTime
    pings.foreach(_ ! StartMessage)
    val ok = latch.await(maxRunDuration, TimeUnit.MILLISECONDS)
    val time = System.nanoTime - start
    val durationS = time.toDouble / 1000000000.0
    println(durationS)
    println(messageCount.toDouble / durationS)

    pings.foreach(system.stop(_))
    pongs.foreach(system.stop(_))

  }

  /**
    * Message for benchmarking.
    */
  case object PingMessage

  /**
    * Start benchmarking.
    */
  case object StartMessage

  /**
    * Replyes on the message, pong it back to the sender
    */
  class Pong extends Actor {

    override def receive: Receive = {
      case PingMessage =>
        sender ! PingMessage
    }

  }

  /**
    * Sends messages and waits them back.
    * @param pong - receiver
    * @param messageCount - number of messages to be sent
    */
  class Ping(
    pong: ActorRef,
    latch: CountDownLatch,
    messageCount: Long
    ) extends Actor {

    var sent = 0L
    var received = 0L

    override def receive: Receive = {
      case PingMessage =>
        received += 1
        if (sent < messageCount) {
          sent += 1
          pong ! PingMessage
        } else if (received >= messageCount) {
          latch.countDown()
        }
      case StartMessage =>
        for (i <- 0L until messageCount) {
          sent += 1
          pong ! PingMessage
        }
    }

  }

}
