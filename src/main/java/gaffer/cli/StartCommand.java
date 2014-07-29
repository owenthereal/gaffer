package gaffer.cli;

import gaffer.core.ProcessManager;
import gaffer.core.Procfile;

import java.io.IOException;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class StartCommand extends Command {
	private static final String NAME = "start";

	private static final String DESC;

	static {
		StringBuilder desc = new StringBuilder();
		desc.append("Start the application specified by a Procfile (defaults to ./Procfile)");
		desc.append("\n");
		desc.append("\n");
		desc.append("Examples:");
		desc.append("\n");
		desc.append("\n");
		desc.append(Gaffer.COMMAND_NAME + " start");
		desc.append("\n");
		desc.append(Gaffer.COMMAND_NAME + " start web");
		desc.append("\n");
		desc.append(Gaffer.COMMAND_NAME + " -f Procfile.test -e env.test");
		DESC = desc.toString();
	}

	@Argument(usage = "process name", metaVar = "process name")
	private String process;

	@Option(name = "-f", usage = "path to Procfile", metaVar = "procfile")
	private String procfile = "Procfile";

	public StartCommand() {
		super(NAME, DESC);
	}

	@Override
	public void execute() throws IOException {
		String dir = System.getProperty("user.dir");
		Procfile procfile = Procfile.read(dir);
		ProcessManager manager = new ProcessManager(procfile);
		manager.start(dir);
	}
}
