package gaffer.cli;

import java.io.PrintStream;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;

public class Gaffer {
	public static final String COMMAND_NAME = "gaffer";

	@Argument(usage = "Available commands", handler = SubCommandHandler.class)
	@SubCommands({ @SubCommand(name = "start", impl = StartCommand.class),
			@SubCommand(name = "version", impl = VersionCommand.class),
			@SubCommand(name = "help", impl = HelpCommand.class) })
	private Command cmd;

	@Option(name = "--help", aliases = "-h", usage = "Print this message", help = true, hidden = true)
	private boolean help = false;

	private CmdLineParser parser;

	private final PrintStream outputStream;

	private final String[] args;

	public Gaffer(PrintStream outputStream, String[] args) {
		this.outputStream = outputStream;
		this.args = args;
	}

	public void run() throws Exception {
		try {
			getParser().parseArgument(args);
			if (help) {
				printUsage();
				return;
			}
		} catch (CmdLineException e) {
			printUsage();
			return;
		}

		if (cmd == null) {
			printUsage();
		} else {
			if (cmd.help()) {
				cmd.printHelp(getOutputStream());
			} else {
				cmd.execute();
			}
		}
	}

	private CmdLineParser getParser() {
		if (parser == null) {
			ParserProperties properties = ParserProperties.defaults();
			properties.withUsageWidth(80);

			parser = new CmdLineParser(this, properties);
		}

		return parser;
	}

	private PrintStream getOutputStream() {
		return outputStream;
	}

	private void printUsage() {
		PrintStream out = getOutputStream();
		Commands.printUsage(out);
	}

	public static void main(String args[]) throws Exception {
		new Gaffer(System.out, args).run();
	}
}
