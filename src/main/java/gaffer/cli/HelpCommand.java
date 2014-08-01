package gaffer.cli;

import java.io.PrintStream;

import org.kohsuke.args4j.Argument;

public class HelpCommand extends Command {
  private static final String NAME = "help";
  private static final String DESC = "Show help";

  @Argument(usage = "command", metaVar = "command")
  private String cmd;

  public HelpCommand() {
    super(NAME, DESC, null);
  }

  @Override
  public void execute() throws Exception {}

  @Override
  public boolean help() {
    return true;
  }

  @Override
  public void printHelp(PrintStream out) {
    if (cmd == null) {
      Commands.printUsage(out);
      return;
    }

    Command command = Commands.get(cmd);
    if (command == null) {
      out.printf("Unknown help topic: \"%s\". Run '%s help'.\n", cmd, Gaffer.COMMAND_NAME);
    } else {
      command.printHelp(out);
    }
  }
}
