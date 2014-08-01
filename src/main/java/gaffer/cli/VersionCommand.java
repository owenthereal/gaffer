package gaffer.cli;

public class VersionCommand extends Command {
	private static final String NAME = "version";
	private static final String DESC = "Display current version";
	private static final String EXAMPLES = Gaffer.COMMAND_NAME + " version";
	private static final String VERSION = "dev";

	public VersionCommand() {
		super(NAME, DESC, EXAMPLES);
	}

	@Override
	public void execute() throws Exception {
		System.out.println(VERSION);
	}
}