/**
  * @author Yex
  */

package throughput

import java.util.concurrent.{CountDownLatch, TimeUnit}

import akka.actor.{ActorSystem, Props}
import benchmarking.messsage.{StartMessage, StartBatchMessage}
import benchmarking.{PingActor, PongActor}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * The goal is to benchmark the throughput of Akka.
  * 1st scenario is to get maximum throughput on a single machine.
  */
object SingleNodeThroughputBenchmark {

  val config = ConfigFactory.load()
  val system = ActorSystem("benchmark", config.getConfig("benchmark").withFallback(config))

  def main(args: Array[String]): Unit = {
    val messageCount = 1000L//10000000L
    val oneAtTimeDispatcher = "one-at-time-dispatcher"
    val throughputDispatcher = "benchmark.throughput-dispatcher"

    println("Warm up...")
    runScenario(100, messageCount, throughputDispatcher, batch = true) // warm up
    runScenario(100, messageCount, throughputDispatcher, batch = false) // warm up
    runScenario(100, messageCount, oneAtTimeDispatcher, batch = true) // warm up
    runScenario(100, messageCount, oneAtTimeDispatcher, batch = false) // warm up
    println("Warm up ends")

    println("================")
    println("oneAtTime")
    println("----------------")
    println("Ping-pong")
    runTest(messageCount, oneAtTimeDispatcher, batch = false)

    println("----------------")
    println("Batch")
    runTest(messageCount, oneAtTimeDispatcher, batch = true)

    println("================")
    println("Throughput")
    println("----------------")
    println("Ping-pong")
    runTest(messageCount, throughputDispatcher, batch = false)
    println("----------------")
    println("Batch")
    runTest(messageCount, throughputDispatcher, batch = true)

    Thread.sleep(1000)
    Await.ready(system.terminate(), Duration(1, TimeUnit.MINUTES))
  }

  /**
    * Runs scenarios, get average and prints it.
    * @param messageCount
    * @param dispatcher
    * @param batch
    */
  def runTest(messageCount: Long, dispatcher: String, batch: Boolean): Unit = {
    getAverageScenario(1, messageCount, dispatcher, batch)
    getAverageScenario(2, messageCount, dispatcher, batch)
    getAverageScenario(3, messageCount, dispatcher, batch)
    getAverageScenario(4, messageCount, dispatcher, batch)
    getAverageScenario(5, messageCount, dispatcher, batch)
    getAverageScenario(6, messageCount, dispatcher, batch)
    getAverageScenario(7, messageCount, dispatcher, batch)
    getAverageScenario(8, messageCount, dispatcher, batch)
    getAverageScenario(9, messageCount, dispatcher, batch)
    getAverageScenario(10, messageCount, dispatcher, batch)
    getAverageScenario(12, messageCount, dispatcher, batch)
    getAverageScenario(14, messageCount, dispatcher, batch)
    getAverageScenario(16, messageCount, dispatcher, batch)
    getAverageScenario(18, messageCount, dispatcher, batch)
    getAverageScenario(20, messageCount, dispatcher, batch)
    getAverageScenario(30, messageCount, dispatcher, batch)
    getAverageScenario(40, messageCount, dispatcher, batch)
    getAverageScenario(50, messageCount, dispatcher, batch)
    getAverageScenario(60, messageCount, dispatcher, batch)
    getAverageScenario(70, messageCount, dispatcher, batch)
    getAverageScenario(80, messageCount, dispatcher, batch)
    getAverageScenario(90, messageCount, dispatcher, batch)
    getAverageScenario(100, messageCount, dispatcher, batch)
    getAverageScenario(150, messageCount, dispatcher, batch)
    getAverageScenario(200, messageCount, dispatcher, batch)
    getAverageScenario(250, messageCount, dispatcher, batch)
  }

  def getAverageScenario(pairs: Int, messageCount: Long, dispatcher: String, batch: Boolean): Unit = {
    def results = for (i <- 0 until 10)
      yield runScenario(pairs, messageCount, dispatcher, batch)
    println(results.sum / results.length)
  }

  /**
    * Runs scenario.
    * @param pairs - number of ping-pong pairs
    * @param messageCount - number of messages
    * @param dispatcher - dispatcher (throughput)
    * @param batch - is it batching job
    */
  def runScenario(pairs: Int, messageCount: Long, dispatcher: String, batch: Boolean): Long = {
    val latch = new CountDownLatch(pairs)
    val messagesPerClient = messageCount / pairs

    val pongs = for (i <- 0 until pairs)
      yield system.actorOf(Props[PongActor].withDispatcher(dispatcher))
    val pings = for (pong <- pongs)
      yield system.actorOf(Props(new PingActor(pong, latch, messagesPerClient)).withDispatcher(dispatcher))

    val maxRunDuration = 1000000
    val start = System.nanoTime
    if (batch)
      pings.foreach(_ ! StartBatchMessage)
    else
      pings.foreach(_ ! StartMessage)
    val ok = latch.await(maxRunDuration, TimeUnit.MILLISECONDS)
    val time = System.nanoTime - start
    val durationS = time.toDouble / 1000000000.0

    pongs.foreach(system.stop(_))
    pings.foreach(system.stop(_))

    return (messageCount / durationS).toLong
  }

}
