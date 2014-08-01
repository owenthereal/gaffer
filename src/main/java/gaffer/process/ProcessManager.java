package gaffer.process;

import gaffer.procfile.Procfile;
import gaffer.procfile.ProcfileEntry;

import java.util.ArrayList;
import java.util.List;
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

    private ShutdownHook(ActorSystem system, ActorRef processManager, CountDownLatch latch) {
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
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static final String GAFFER_LOGGER = "gaffer";
  private static final Logger LOGGER = LoggerFactory.getLogger(GAFFER_LOGGER);

  private Procfile procfile;

  public ProcessManager(Procfile procfile) {
    this.procfile = procfile;
  }

  public void start(String dir) {
    ProcfileEntry[] entries = procfile.getEntries();
    List<Process> processes = new ArrayList<Process>(entries.length);
    for (int i = 0; i < entries.length; i++) {
      ProcfileEntry entry = entries[i];
      processes.add(new Process(dir, entry.getName(), entry.getCommand()));
    }

    CountDownLatch latch = new CountDownLatch(1);
    ActorSystem system = ActorSystem.create("start");
    ActorRef processManager =
        system.actorOf(Props.create(ProcessManagerActor.class, processes, LOGGER),
            ProcessManagerActor.class.getName());
    system.actorOf(Props.create(ProcessTerminatorActor.class, processManager, latch),
        ProcessTerminatorActor.class.getName());
    Runtime.getRuntime().addShutdownHook(new ShutdownHook(system, processManager, latch));
  }
}
