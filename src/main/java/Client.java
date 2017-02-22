import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeoutException;

/**
 * @author Yex
 */
public class Client {

    public static void main(String[] args) {
        String[] ports = { "0" };
        startup(ports);
//        if (args.length == 0)
//            startup(new String[] { "2551", "2552", "0" });
//        else
//            startup(args);
    }

    static void startup(String[] ports) {
        for (String port : ports) {
            // Override the configuration of the port
            Config config = ConfigFactory.parseString(
                    "akka.remote.netty.tcp.port=" + port).withFallback(
                    ConfigFactory.load());

            // Create an Akka system
            ActorSystem system = ActorSystem.create("ClusterSystem", config);

            // Create "actor-in-a-box"
            final Inbox inbox = Inbox.create(system);

//            final ActorRef worker = system.actorOf(Props.create(WorkerActor.class), "worker");
//            getContext().actorSelection(member.address() + "/user/frontend").tell(
//                    BACKEND_REGISTRATION, getSelf());

            ActorRef ref = system.actorFor("akka.tcp://ClusterSystem@127.0.0.1:2551/user/worker");
            inbox.send(ref, new Task("dowork", new GreetingAlgorithm()));
            try {
                Result result = (Result) inbox.receive(Duration.create(5, "seconds"));
                System.out.println("Result: " + result.data);
            } catch (TimeoutException e) {
                System.out.println(e.toString());
            }
        }
    }
}
