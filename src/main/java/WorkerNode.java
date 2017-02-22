/**
 * @author Yex
 */

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import java.io.Serializable;
import java.util.concurrent.TimeoutException;

public class WorkerNode {

    public static void main(String[] args) {
        String[] ports = { "2551" };
        startup(ports);
//        if (args.length == 0)
//            startup(new String[] { "2551", "2552", "0" });
//        else
//            startup(args);


//        // Create the 'helloakka' actor system
//        final ActorSystem system = ActorSystem.create("helloakka");
//
//        // Create "actor-in-a-box"
//        final Inbox inbox = Inbox.create(system);
//
//        // worker
//        final ActorRef worker = system.actorOf(Props.create(WorkerActor.class), "worker");
//        inbox.send(worker, new Task("dowork", new GreetingAlgorithm()));
//        try {
//            Result result = (Result) inbox.receive(Duration.create(5, "seconds"));
//            System.out.println("Result: " + result.data);
//        } catch (TimeoutException e) {
//            System.out.println(e.toString());
//        }
    }

    private static void startup(String[] ports) {
        for (String port : ports) {
            // Override the configuration of the port
            Config config = ConfigFactory.parseString(
                    "akka.remote.netty.tcp.port=" + port).withFallback(
                    ConfigFactory.load());

            // Create an Akka system
            ActorSystem system = ActorSystem.create("ClusterSystem", config);

            // Create an actor that handles cluster domain events
            system.actorOf(Props.create(WorkerActor.class), "worker");
        }
    }
}
