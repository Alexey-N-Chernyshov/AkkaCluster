/**
  * @author Yex
  */

package throughput

import java.util.concurrent.{CountDownLatch, TimeUnit}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * The goal is to benchmark the throughput of Akka.
  * 1st scenario is to get maximum throughput on a single machine.
  */
object ThroughputBenchmark {

  val config = ConfigFactory.load()
  val throughputDispatcher = "benchmark.throughput-dispatcher"
  val system = ActorSystem("benchmark", config.getConfig("benchmark").withFallback(config))

  def main(args: Array[String]): Unit = {
    val messageCount = 10000000L
    runScenario(10, messageCount, warmup = true, batch = true) // warm up

    println("Batch")
    runScenario(1, messageCount, warmup = false, batch = true)
    runScenario(2, messageCount, warmup = false, batch = true)
    runScenario(3, messageCount, warmup = false, batch = true)
    runScenario(4, messageCount, warmup = false, batch = true)
    runScenario(5, messageCount, warmup = false, batch = true)
    runScenario(6, messageCount, warmup = false, batch = true)
    runScenario(7, messageCount, warmup = false, batch = true)
    runScenario(8, messageCount, warmup = false, batch = true)
    runScenario(9, messageCount, warmup = false, batch = true)
    runScenario(10, messageCount, warmup = false, batch = true)
    runScenario(12, messageCount, warmup = false, batch = true)
    runScenario(14, messageCount, warmup = false, batch = true)
    runScenario(16, messageCount, warmup = false, batch = true)
    runScenario(18, messageCount, warmup = false, batch = true)
    runScenario(20, messageCount, warmup = false, batch = true)
    runScenario(30, messageCount, warmup = false, batch = true)
    runScenario(40, messageCount, warmup = false, batch = true)
    runScenario(50, messageCount, warmup = false, batch = true)
    runScenario(60, messageCount, warmup = false, batch = true)
    runScenario(70, messageCount, warmup = false, batch = true)
    runScenario(80, messageCount, warmup = false, batch = true)
    runScenario(90, messageCount, warmup = false, batch = true)
    runScenario(100, messageCount, warmup = false, batch = true)
    runScenario(200, messageCount, warmup = false, batch = true)

    println("----------------")
    println("Ping-pong")
    runScenario(1, messageCount, warmup = false, batch = false)
    runScenario(2, messageCount, warmup = false, batch = false)
    runScenario(3, messageCount, warmup = false, batch = false)
    runScenario(4, messageCount, warmup = false, batch = false)
    runScenario(5, messageCount, warmup = false, batch = false)
    runScenario(6, messageCount, warmup = false, batch = false)
    runScenario(7, messageCount, warmup = false, batch = false)
    runScenario(8, messageCount, warmup = false, batch = false)
    runScenario(9, messageCount, warmup = false, batch = false)
    runScenario(10, messageCount, warmup = false, batch = false)
    runScenario(12, messageCount, warmup = false, batch = false)
    runScenario(14, messageCount, warmup = false, batch = false)
    runScenario(16, messageCount, warmup = false, batch = false)
    runScenario(18, messageCount, warmup = false, batch = false)
    runScenario(20, messageCount, warmup = false, batch = false)
    runScenario(30, messageCount, warmup = false, batch = false)
    runScenario(40, messageCount, warmup = false, batch = false)
    runScenario(50, messageCount, warmup = false, batch = false)
    runScenario(60, messageCount, warmup = false, batch = false)
    runScenario(70, messageCount, warmup = false, batch = false)
    runScenario(80, messageCount, warmup = false, batch = false)
    runScenario(90, messageCount, warmup = false, batch = false)
    runScenario(100, messageCount, warmup = false, batch = false)
    runScenario(200, messageCount, warmup = false, batch = false)

    Await.ready(system.terminate(), Duration(1, TimeUnit.MINUTES))
  }

  /**
    * Runs scenario.
    * @param pairs - number of ping-pong pairs
    * @param messageCount - number of messages
    */
  def runScenario(pairs: Int, messageCount: Long, warmup: Boolean, batch: Boolean): Unit = {
    val latch = new CountDownLatch(pairs)
    val messagesPerClient = messageCount / pairs

    val pongs = for (i <- 0 until pairs)
      yield system.actorOf(Props[Pong].withDispatcher(throughputDispatcher))
    val pings = for (pong <- pongs)
      yield system.actorOf(Props(new Ping(pong, latch, messagesPerClient)).withDispatcher(throughputDispatcher))

    val maxRunDuration = 1000000
    val start = System.nanoTime
    if (batch)
      pings.foreach(_ ! StartBatchMessage)
    else
      pings.foreach(_ ! StartMessage)
    val ok = latch.await(maxRunDuration, TimeUnit.MILLISECONDS)
    val time = System.nanoTime - start
    val durationS = time.toDouble / 1000000000.0

    if (!warmup) {
      println("actors " + pairs + ", messages: " + messageCount + ", mes/s: " + (messageCount / durationS).toInt)
    }

    pongs.foreach(system.stop(_))
    pings.foreach(system.stop(_))
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
    * Message for batch job.
    */
  case object StartBatchMessage

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
        sent += 1
        pong ! PingMessage
      case StartBatchMessage =>
        for (i <- 0L until messageCount) {
          sent += 1
          pong ! PingMessage
        }
    }

  }

}
