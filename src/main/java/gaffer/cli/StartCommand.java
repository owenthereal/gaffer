package gaffer.cli;

import gaffer.process.ProcessManager;
import gaffer.procfile.Procfile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class StartCommand extends Command {
  private static final String NAME = "start";

  private static final String DESC =
      "Start the application specified by a Procfile (defaults to ./Procfile)";

  private static final String EXAMPLES;

  static {
    StringBuilder examples = new StringBuilder();
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
  private String procfile = "Procfile";

  @Option(name = "-p", usage = "Default: 5000", metaVar = "port")
  private String port = "5000";


  public StartCommand() {
    super(NAME, DESC, EXAMPLES);
  }

  @Override
  public void execute() throws CommandException {
    Path path = Paths.get(procfile).toAbsolutePath();
    try {
      Procfile pf = Procfile.read(path);
      ProcessManager manager = new ProcessManager(pf);

      String dir = System.getProperty("user.dir");
      manager.start(dir, port);
    } catch (IOException e) {
      throw new CommandException("error reading " + path);
    }
  }
}
