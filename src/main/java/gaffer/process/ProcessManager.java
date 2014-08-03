package gaffer.process;

import gaffer.procfile.Procfile;
import gaffer.procfile.ProcfileEntry;

import java.util.ArrayList;
import java.util.Arrays;
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
    private static final int SHUTDOWN_TIMEOUT_SEC = 5;
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
        latch.await(SHUTDOWN_TIMEOUT_SEC, TimeUnit.SECONDS);
      } catch (final InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static final String GAFFER_LOGGER = "gaffer";
  private static final Logger LOGGER = LoggerFactory.getLogger(GAFFER_LOGGER);

  public void run(final String[] cmd, final int flagPort) throws ProcessException {
    final String dir = System.getProperty("user.dir");
    final Process process = new Process(dir, GAFFER_LOGGER, cmd, flagPort);
    LOGGER.debug("starting process on port " + process.getPort());
    process.start();
    process.waitFor();
  }

  public void start(final Procfile procfile, final String processName,
      final Map<String, Integer> concurrency, final int flagPort) throws ProcessException {
    final ProcfileEntry[] entries = procfile.getEntries();
    final String dir = procfile.getPath().getParent().toString();
    final List<Process> processes = new ArrayList<Process>(entries.length);
    final List<ProcfileEntry> filteredEntries = filterEntries(entries, processName);
    for (int idx = 0; idx < filteredEntries.size(); idx++) {
      final ProcfileEntry entry = filteredEntries.get(idx);
      final int numProcs = concurrency.getOrDefault(entry.getName(), 1);
      for (int procNum = 0; procNum < numProcs; procNum++) {
        final String name = entry.getName() + "." + (procNum + 1);
        final int port = flagPort + (idx * 100);
        final Process process = new Process(dir, name, entry.getCommandArray(), port);
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

  private List<ProcfileEntry> filterEntries(final ProcfileEntry[] entries, final String processName)
      throws ProcessException {
    if (processName == null) {
      return Arrays.asList(entries);
    }

    final ArrayList<ProcfileEntry> result = new ArrayList<ProcfileEntry>();
    for (final ProcfileEntry entry : entries) {
      if (processName.equals(entry.getName())) {
        result.add(entry);
      }
    }

    if (result.isEmpty()) {
      throw new ProcessException("no such process " + processName);
    }

    return result;
  }
}
