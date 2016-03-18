package gaffer.cli;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import gaffer.environment.Environment;
import gaffer.process.ProcessException;
import gaffer.process.ProcessManager;

public class RunCommand extends Command {
  private static final String NAME = "run";

  private static final String DESC = "Run a one-off command";

  private static final String EXAMPLES = Gaffer.COMMAND_NAME + " run bin/migrate";

  @Argument(usage = "command", metaVar = "command", required = true)
  private String[] cmd;

  @Option(name = "-p", usage = "Default: 5000", metaVar = "port")
  private int flagPort = 5000;

  @Option(name = "-e", usage = "Default: .env", metaVar = "environment")
  private String flagEnvironment = ".env";

  public RunCommand() {
    super(NAME, DESC, EXAMPLES);
  }

  @Override
  public void execute() throws CommandException {
    final Path envPath = Paths.get(flagEnvironment).toAbsolutePath();
    final Environment env = Environment.read(envPath);
    try {
      new ProcessManager(env).run(cmd, flagPort);
    } catch (final ProcessException e) {
      throw new CommandException(e.getMessage());
    }
  }
}
