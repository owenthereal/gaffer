package gaffer.cli;

import gaffer.cli.ConcurrencyFlagParser.ConcurrencyFlagParseException;
import gaffer.environment.Environment;
import gaffer.process.ProcessException;
import gaffer.process.ProcessManager;
import gaffer.procfile.Procfile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class StartCommand extends Command {
  private static final String NAME = "start";

  private static final String DESC =
      "Start the application specified by a Procfile (defaults to ./Procfile) and reading environment varibales from a .env file (defaults to ./.env)";

  private static final String EXAMPLES;

  static {
    final StringBuilder examples = new StringBuilder();
    examples.append(Gaffer.COMMAND_NAME + " start");
    examples.append("\n");
    examples.append(Gaffer.COMMAND_NAME + " start web");
    examples.append("\n");
    examples.append(Gaffer.COMMAND_NAME + " start -f Procfile.test -c worker=2");
    examples.append("\n");
    examples.append(Gaffer.COMMAND_NAME + " start -f Procfile.test -c worker=2 -e my_env_file");

    EXAMPLES = examples.toString();
  }

  @Argument(usage = "process name", metaVar = "process name")
  private String process;

  @Option(name = "-f", usage = "Default: Procfile", metaVar = "procfile")
  private String flagProcfile = "Procfile";

  @Option(name = "-p", usage = "Default: 5000", metaVar = "port")
  private int flagPort = 5000;

  @Option(name = "-c", usage = "Concurrency", metaVar = "concurrency")
  private String flagConcurrency;

  @Option(name = "-e", usage = "Default: .env", metaVar = "environment")
  private String flagEnvironment = ".env";

  public StartCommand() {
    super(NAME, DESC, EXAMPLES);
  }

  @Override
  public void execute() throws CommandException {
    final Path path = Paths.get(flagProcfile).toAbsolutePath();
    final Path envPath = Paths.get(flagEnvironment).toAbsolutePath();
    try {
      final Map<String, Integer> concurrency = ConcurrencyFlagParser.parse(flagConcurrency);
      final Procfile pf = Procfile.read(path);
      final Environment env = Environment.read(envPath);
      new ProcessManager(env).start(pf, process, concurrency, flagPort);
    } catch (final IOException e) {
      throw new CommandException("error reading " + path + " or " + envPath, e);
    } catch (final ConcurrencyFlagParseException e) {
      throw new CommandException("error parsing concurrency flag: " + e.getMessage());
    } catch (final ProcessException e) {
      throw new CommandException(e.getMessage());
    }
  }
}
