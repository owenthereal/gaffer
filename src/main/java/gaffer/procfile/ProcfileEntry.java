package gaffer.procfile;

public class ProcfileEntry {
  private final String name;
  private final String command;

  public ProcfileEntry(final String name, final String command) {
    this.name = name;
    this.command = command;
  }

  public String getName() {
    return name;
  }

  public String getCommand() {
    return command;
  }

  public String[] getCommandArray() {
    return command.split(" ");
  }
}
