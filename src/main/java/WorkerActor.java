/**
 * @author Yex
 */

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

class WorkerActor extends AbstractActor {
    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.
                match(Task.class, message -> sender().tell(new Result(message.execute()), self())).
                build();
    }
}
