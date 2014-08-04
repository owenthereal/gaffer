package gaffer.process;

import akka.actor.UntypedActor;

public class ProcessActor extends UntypedActor {
  public static enum State {
    CREATED(false), TERMINATED(true), ERROR(true);

    private final boolean isDead;

    private State(final boolean isDead) {
      this.isDead = isDead;
    }

    public boolean isDead() {
      return isDead;
    }
  }

  public static class Message {
    private final String process;
    private final State state;

    public static Message created(final String process) {
      return new Message(process, State.CREATED);
    }

    public static Message terminated(final String process) {
      return new Message(process, State.TERMINATED);
    }

    public static Message error(final String process) {
      return new Message(process, State.ERROR);
    }

    public Message(final String process, final State state) {
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

  public ProcessActor(final Process process) {
    this.process = process;
  }

  @Override
  public void onReceive(final Object signal) {
    if (signal == Signal.FORK) {
      try {
        process.start();
        getSender().tell(Message.created(process.getName()), getSelf());
      } catch (final ProcessException e) {
        getSender().tell(Message.error(process.getName()), getSelf());
      }
    } else if (signal == Signal.TERM) {
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
