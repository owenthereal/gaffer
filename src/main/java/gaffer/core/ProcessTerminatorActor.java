package gaffer.core;

import java.util.concurrent.CountDownLatch;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;

public class ProcessTerminatorActor extends UntypedActor {
	private final CountDownLatch latch;

	public ProcessTerminatorActor(ActorRef ref, CountDownLatch latch) {
		this.latch = latch;
		getContext().watch(ref);
	}

	@Override
	public void onReceive(Object msg) {
		if (msg instanceof Terminated) {
			getContext().system().shutdown();
			latch.countDown();
		} else {
			unhandled(msg);
		}
	}
}