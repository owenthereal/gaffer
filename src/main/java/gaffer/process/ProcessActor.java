package gaffer.process;

import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;

public class ProcessActor extends UntypedActor {
  private final Process process;

  private Cancellable healthCheck;

  public ProcessActor(final Process process) {
    this.process = process;
  }

  @Override
  public void preStart() throws Exception {
    scheduleHealthCheck();
  }

  @Override
  public void postStop() throws Exception {
    healthCheck.cancel();
  }

  @Override
  public void onReceive(final Object signal) {
    if (signal == Signal.FORK) {
      try {
        process.start();
      } catch (final ProcessException e) {
        stopSelf();
      }
    } else if (signal == Signal.TERM) {
      process.kill();
    } else if (signal == Signal.CHECK) {
      if (process.exitWithError()) {
        stopSelf();
      }
    } else {
      unhandled(signal);
    }
  }

  private void scheduleHealthCheck() {
    final Runnable healthCheckRunnable = new Runnable() {
      @Override
      public void run() {
        getSelf().tell(Signal.CHECK, getSelf());
      }
    };
    healthCheck =
        getContext()
            .system()
            .scheduler()
            .schedule(Duration.create(1000, TimeUnit.MILLISECONDS),
                Duration.create(1000, TimeUnit.MILLISECONDS), healthCheckRunnable,
                getContext().dispatcher());
  }


  private void stopSelf() {
    getContext().stop(getSelf());
  }
}
