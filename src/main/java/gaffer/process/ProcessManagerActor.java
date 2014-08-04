package gaffer.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;

public class ProcessManagerActor extends UntypedActor {
  private static final int SHUTDOWN_GRACE_TIME = 3000;

  private static enum State {
    STARTED, TERMINATING, TERMINATED;
  }

  private final List<Process> processes;

  private final Logger logger;

  private Router router;

  private Map<ActorRef, String> routees;

  private State state;

  public ProcessManagerActor(final List<Process> processes, final Logger logger) {
    this.processes = processes;
    this.logger = logger;
  }

  @Override
  public void preStart() {
    router = new Router(new BroadcastRoutingLogic());
    routees = new HashMap<ActorRef, String>(processes.size());

    for (final Process process : processes) {
      logger.debug("starting " + process.getName() + " on port " + process.getPort());

      final ActorRef processActor =
          getContext().actorOf(Props.create(ProcessActor.class, process), process.getName());
      getContext().watch(processActor);
      router = router.addRoutee(processActor);
      routees.put(processActor, process.getName());
    }

    router.route(Signal.FORK, getSelf());
    state = State.STARTED;
  }

  @Override
  public void onReceive(final Object msg) {
    if (msg instanceof Terminated) {
      removeRoutee(((Terminated) msg).actor());
      terminateAll();
      if (shouldShutdown()) {
        getContext().stop(getSelf());
      }
    } else if (msg == Signal.TERM) {
      terminateAll();
    } else {
      unhandled(msg);
    }
  }

  private void removeRoutee(final ActorRef ref) {
    router = router.removeRoutee(ref);
    routees.remove(ref);
    if (routees.isEmpty()) {
      state = State.TERMINATED;
    }
  }

  private boolean shouldShutdown() {
    return state == State.TERMINATED;
  }

  private void terminateAll() {
    if (state == State.TERMINATING || state == State.TERMINATED) {
      return;
    }

    for (final Entry<ActorRef, String> routee : routees.entrySet()) {
      logger.debug("sending SIGTERM to " + routee.getValue());
    }

    router.route(Signal.TERM, getSelf());
    scheduleKillAll();
    state = State.TERMINATING;
  }

  private void scheduleKillAll() {
    final Runnable killAll = new Runnable() {
      @Override
      public void run() {
        router.route(PoisonPill.getInstance(), getSelf());

      }
    };
    getContext()
        .system()
        .scheduler()
        .scheduleOnce(Duration.create(SHUTDOWN_GRACE_TIME, TimeUnit.MILLISECONDS), killAll,
            getContext().dispatcher());
  }
}
