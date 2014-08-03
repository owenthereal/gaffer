package gaffer.cli;

import gaffer.process.ProcessException;
import gaffer.process.ProcessManager;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class RunCommand extends Command {
  private static final String NAME = "run";

  private static final String DESC = "Run a one-off command";

  private static final String EXAMPLES = Gaffer.COMMAND_NAME + " run bin/migrate";

  @Argument(usage = "command", metaVar = "command", required = true)
  private String[] cmd;

  @Option(name = "-p", usage = "Default: 5000", metaVar = "port")
  private int flagPort = 5000;

  public RunCommand() {
    super(NAME, DESC, EXAMPLES);
  }

  @Override
  public void execute() throws CommandException {
    try {
      new ProcessManager().run(cmd, flagPort);
    } catch (final ProcessException e) {
      throw new CommandException(e.getMessage());
    }
  }
}
