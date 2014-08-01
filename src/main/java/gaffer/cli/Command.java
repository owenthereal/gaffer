package gaffer.cli;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.OptionHandler;

public abstract class Command {
  @Option(name = "--help", aliases = "-h", usage = "Print this message", help = true, hidden = true)
  private boolean help = false;

  private String name;
  private String desc;
  private String examples;

  public Command(String name, String desc, String examples) {
    this.name = name;
    this.desc = desc;
    this.examples = examples;
  }

  public abstract void execute() throws Exception;

  public void printHelp(PrintStream out) {
    CmdLineParser parser = new CmdLineParser(this);
    removeHiddenOption(parser);

    out.print("Usage: " + Gaffer.COMMAND_NAME + " " + name);
    parser.printSingleLineUsage(out);
    out.println();
    out.println();
    out.println(desc);
    if (examples != null) {
      out.println();
      out.println("Examples:");
      out.println();
      out.println(formatExamples(examples));
    }
  }

  private static final String formatExamples(String examples) {
    Pattern pattern = Pattern.compile("^", Pattern.MULTILINE);
    return pattern.matcher(examples).replaceAll("   ");
  }

  @SuppressWarnings("rawtypes")
  private void removeHiddenOption(CmdLineParser parser) {
    Iterator<OptionHandler> iterator = parser.getOptions().iterator();
    while (iterator.hasNext()) {
      OptionHandler next = iterator.next();
      if (next.option.hidden()) {
        iterator.remove();
      }
    }
  }

  public boolean help() {
    return help;
  }
}
