package gaffer.cli;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommands;

public class Commands {
  private static Map<String, Command> ALL;

  @SuppressWarnings("unchecked")
  public static Map<String, Command> all() {
    if (ALL == null) {
      try {
        ALL = new HashMap<String, Command>();
        Field cmdField = Gaffer.class.getDeclaredField("cmd");
        SubCommands subCommands = cmdField.getAnnotation(SubCommands.class);
        for (SubCommand sub : subCommands.value()) {
          if (Command.class.isAssignableFrom(sub.impl())) {
            Class<Command> clazz = (Class<Command>) sub.impl();
            ALL.put(sub.name(), clazz.newInstance());
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

    }

    return ALL;
  }

  public static Command get(String name) {
    return all().get(name);
  }

  public static void printUsage(PrintStream out) {
    out.println("Usage: " + Gaffer.COMMAND_NAME + " <command> [<args>]");
    out.println();
    out.println("Available commands:");
    out.println("   start     Start the application");
    out.println("   run       Run a one-off command");
    out.println("   version   Display current version");
    out.println("   help      Show this help");
    out.println();
    out.println("Run '" + Gaffer.COMMAND_NAME + " help [command]' for details.");
  }
}
