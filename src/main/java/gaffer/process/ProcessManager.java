package gaffer.process;

import gaffer.procfile.Procfile;
import gaffer.procfile.ProcfileEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class ProcessManager {
  private static final class ShutdownHook extends Thread {
    private final ActorSystem system;
    private final ActorRef processManager;
    private final CountDownLatch latch;

    private ShutdownHook(final ActorSystem system, final ActorRef processManager,
        final CountDownLatch latch) {
      this.system = system;
      this.processManager = processManager;
      this.latch = latch;
    }

    @Override
    public void run() {
      if (system.isTerminated()) {
        return;
      }

      processManager.tell(Signal.TERM, processManager);
      try {
        latch.await(5, TimeUnit.SECONDS);
      } catch (final InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static final String GAFFER_LOGGER = "gaffer";
  private static final Logger LOGGER = LoggerFactory.getLogger(GAFFER_LOGGER);

  private final Procfile procfile;

  public ProcessManager(final Procfile procfile) {
    this.procfile = procfile;
  }

  public void start(final String dir, final Map<String, Integer> concurrency, final int flagPort) {
    final ProcfileEntry[] entries = procfile.getEntries();
    final List<Process> processes = new ArrayList<Process>(entries.length);
    for (int idx = 0; idx < entries.length; idx++) {
      final ProcfileEntry entry = entries[idx];
      final int numProcs = concurrency.getOrDefault(entry.getName(), 1);
      for (int procNum = 0; procNum < numProcs; procNum++) {
        final Process process =
            new Process(idx, procNum, dir, entry.getName(), entry.getCommand(), flagPort);
        processes.add(process);
      }
    }

    final CountDownLatch latch = new CountDownLatch(1);
    final ActorSystem system = ActorSystem.create("start");
    final ActorRef processManager =
        system.actorOf(Props.create(ProcessManagerActor.class, processes, LOGGER),
            ProcessManagerActor.class.getName());
    system.actorOf(Props.create(ProcessTerminatorActor.class, processManager, latch),
        ProcessTerminatorActor.class.getName());
    Runtime.getRuntime().addShutdownHook(new ShutdownHook(system, processManager, latch));
  }
}
