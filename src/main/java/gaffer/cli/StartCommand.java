package gaffer.cli;

import gaffer.cli.ConcurrencyFlagParser.ConcurrencyFlagParseException;
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
      "Start the application specified by a Procfile (defaults to ./Procfile)";

  private static final String EXAMPLES;

  static {
    final StringBuilder examples = new StringBuilder();
    examples.append(Gaffer.COMMAND_NAME + " start");
    examples.append("\n");
    examples.append(Gaffer.COMMAND_NAME + " start web");
    examples.append("\n");
    examples.append(Gaffer.COMMAND_NAME + " -f Procfile.test -e env.test");

    EXAMPLES = examples.toString();
  }

  @Argument(usage = "process name", metaVar = "process name")
  private String process;

  @Option(name = "-f", usage = "Default: Procfile", metaVar = "procfile")
  private final String flagProcfile = "Procfile";

  @Option(name = "-p", usage = "Default: 5000", metaVar = "port")
  private final int flagPort = 5000;

  @Option(name = "-c", usage = "Concurrency", metaVar = "concurrency")
  private String flagConcurrency;


  public StartCommand() {
    super(NAME, DESC, EXAMPLES);
  }

  @Override
  public void execute() throws CommandException {
    final Path path = Paths.get(flagProcfile).toAbsolutePath();
    try {
      final Map<String, Integer> concurrency = ConcurrencyFlagParser.parse(flagConcurrency);
      final Procfile pf = Procfile.read(path);
      final ProcessManager manager = new ProcessManager(pf);

      final String dir = path.getParent().toString();
      manager.start(dir, concurrency, flagPort);
    } catch (final IOException e) {
      throw new CommandException("error reading " + path);
    } catch (final ConcurrencyFlagParseException e) {
      throw new CommandException("error parsing concurrency flag: " + e.getMessage());
    }
  }
}
