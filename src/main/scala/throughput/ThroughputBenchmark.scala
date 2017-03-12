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
  val system = ActorSystem("benchmark", config.getConfig("benchmark").withFallback(config))

  def main(args: Array[String]): Unit = {
    val messageCount = 10000000L
    val oneAtTimeDispatcher = "one-at-time-dispatcher"
    val throughputDispatcher = "benchmark.throughput-dispatcher"

    println("Warm up...")
    runScenario(100, messageCount, throughputDispatcher, warmup = true, batch = true) // warm up
    runScenario(100, messageCount, throughputDispatcher, warmup = true, batch = false) // warm up
    runScenario(100, messageCount, oneAtTimeDispatcher, warmup = true, batch = true) // warm up
    runScenario(100, messageCount, oneAtTimeDispatcher, warmup = true, batch = false) // warm up
    println("Warm up ends")

    println("================")
    println("oneAtTime")
    println("----------------")
    println("Ping-pong")
    runSequenceScenario(messageCount, oneAtTimeDispatcher, warmup = false, batch = false)
    println("----------------")
    println("Batch")
    runSequenceScenario(messageCount, oneAtTimeDispatcher, warmup = false, batch = true)

    println("================")
    println("Throughput")
    println("----------------")
    println("Ping-pong")
    runSequenceScenario(messageCount, throughputDispatcher, warmup = false, batch = false)
    println("----------------")
    println("Batch")
    runSequenceScenario(messageCount, throughputDispatcher, warmup = false, batch = true)

    Await.ready(system.terminate(), Duration(1, TimeUnit.MINUTES))
  }

  def runSequenceScenario(messageCount: Long, dispatcher: String, warmup: Boolean, batch: Boolean): Unit = {
    runScenario(1, messageCount, dispatcher, warmup, batch)
    runScenario(2, messageCount, dispatcher, warmup, batch)
    runScenario(3, messageCount, dispatcher, warmup, batch)
    runScenario(4, messageCount, dispatcher, warmup, batch)
    runScenario(5, messageCount, dispatcher, warmup, batch)
    runScenario(6, messageCount, dispatcher, warmup, batch)
    runScenario(7, messageCount, dispatcher, warmup, batch)
    runScenario(8, messageCount, dispatcher, warmup, batch)
    runScenario(9, messageCount, dispatcher, warmup, batch)
    runScenario(10, messageCount, dispatcher, warmup, batch)
    runScenario(12, messageCount, dispatcher, warmup, batch)
    runScenario(14, messageCount, dispatcher, warmup, batch)
    runScenario(16, messageCount, dispatcher, warmup, batch)
    runScenario(18, messageCount, dispatcher, warmup, batch)
    runScenario(20, messageCount, dispatcher, warmup, batch)
    runScenario(30, messageCount, dispatcher, warmup, batch)
    runScenario(40, messageCount, dispatcher, warmup, batch)
    runScenario(50, messageCount, dispatcher, warmup, batch)
    runScenario(60, messageCount, dispatcher, warmup, batch)
    runScenario(70, messageCount, dispatcher, warmup, batch)
    runScenario(80, messageCount, dispatcher, warmup, batch)
    runScenario(90, messageCount, dispatcher, warmup, batch)
    runScenario(100, messageCount, dispatcher, warmup, batch)
    runScenario(150, messageCount, dispatcher, warmup, batch)
    runScenario(200, messageCount, dispatcher, warmup, batch)
    runScenario(250, messageCount, dispatcher, warmup, batch)
  }

  /**
    * Runs scenario.
    * @param pairs - number of ping-pong pairs
    * @param messageCount - number of messages
    */
  def runScenario(pairs: Int, messageCount: Long, dispatcher: String, warmup: Boolean, batch: Boolean): Unit = {
    val latch = new CountDownLatch(pairs)
    val messagesPerClient = messageCount / pairs

    val pongs = for (i <- 0 until pairs)
      yield system.actorOf(Props[Pong].withDispatcher(dispatcher))
    val pings = for (pong <- pongs)
      yield system.actorOf(Props(new Ping(pong, latch, messagesPerClient)).withDispatcher(dispatcher))

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
      println("actors " + pairs + ", messages: " + messageCount + ", mes/s: " +
        (messageCount / durationS).toInt + ", time: " + durationS)
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
