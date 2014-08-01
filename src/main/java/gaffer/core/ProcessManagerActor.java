package gaffer.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class ProcessManagerActor extends UntypedActor {
	private final Map<ActorRef, ProcessActor.State> processState = new HashMap<ActorRef, ProcessActor.State>();
	private final List<Process> processes;
	private final Logger logger;

	private final Cancellable tick = getContext()
			.system()
			.scheduler()
			.schedule(Duration.create(1000, TimeUnit.MILLISECONDS),
					Duration.create(1000, TimeUnit.MILLISECONDS), getSelf(),
					Signal.CHECK, getContext().dispatcher(), null);

	public ProcessManagerActor(List<Process> processes, Logger logger) {
		this.processes = processes;
		this.logger = logger;
	}

	@Override
	public void preStart() {
		for (Process process : processes) {
			ActorRef processActor = getContext().actorOf(
					Props.create(ProcessActor.class, process, logger),
					process.getName());
			processActor.tell(Signal.FORK, getSelf());
			processState.put(processActor, ProcessActor.State.CREATED);
		}
	}

	@Override
	public void postStop() {
		tick.cancel();
	}

	@Override
	public void onReceive(Object msg) {
		if (msg instanceof ProcessActor.Message) {
			ProcessActor.Message message = (ProcessActor.Message) msg;
			processState.put(getSender(), message.getState());

			if (message.getState() == ProcessActor.State.ERROR) {
				getSelf().tell(Signal.TERM, getSelf());
			}

			if (shouldShutdown()) {
				getContext().stop(getSelf());
			}
		} else if (msg == Signal.TERM) {
			killAll();
		} else if (msg == Signal.CHECK) {
			checkAll();
		} else {
			unhandled(msg);
		}
	}

	private void killAll() {
		for (Entry<ActorRef, ProcessActor.State> entry : processState
				.entrySet()) {
			if (!entry.getValue().isDead()) {
				entry.getKey().tell(Signal.TERM, getSelf());
			}
		}
	}

	private void checkAll() {
		for (Entry<ActorRef, ProcessActor.State> entry : processState
				.entrySet()) {
			if (!entry.getValue().isDead()) {
				entry.getKey().tell(Signal.CHECK, getSelf());
			}
		}
	}

	private boolean shouldShutdown() {
		for (ProcessActor.State state : processState.values()) {
			if (!state.isDead()) {
				return false;
			}
		}

		return true;
	}
}