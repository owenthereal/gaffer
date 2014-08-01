package gaffer.process;

import org.slf4j.Logger;

import akka.actor.UntypedActor;

public class ProcessActor extends UntypedActor {
  public static enum State {
    CREATED(false), TERMINATED(true), ERROR(true);

    private final boolean isDead;

    private State(boolean isDead) {
      this.isDead = isDead;
    }

    public boolean isDead() {
      return isDead;
    }
  }

  public static class Message {
    private final String process;
    private final State state;

    public static Message created(String process) {
      return new Message(process, State.CREATED);
    }

    public static Message terminated(String process) {
      return new Message(process, State.TERMINATED);
    }

    public static Message error(String process) {
      return new Message(process, State.ERROR);
    }

    public Message(String process, State state) {
      this.process = process;
      this.state = state;
    }

    public String getProcess() {
      return process;
    }

    public State getState() {
      return state;
    }

    @Override
    public String toString() {
      return "process=" + getProcess() + " state=" + getState();
    }
  }

  private final Process process;
  private final Logger logger;

  public ProcessActor(Process process, Logger logger) {
    this.process = process;
    this.logger = logger;
  }

  @Override
  public void onReceive(Object signal) {
    if (signal == Signal.FORK) {
      try {
        process.start();
        getSender().tell(Message.created(process.getName()), getSelf());
      } catch (ProcessException e) {
        getSender().tell(Message.error(process.getName()), getSelf());
      }
    } else if (signal == Signal.TERM) {
      logger.debug("sending SIGTERM to " + process.getName());
      process.kill();
      getSender().tell(Message.terminated(process.getName()), getSelf());
    } else if (signal == Signal.CHECK) {
      if (!process.isAlive()) {
        getSender().tell(Message.error(process.getName()), getSelf());
      }
    } else {
      unhandled(signal);
    }
  }
}
