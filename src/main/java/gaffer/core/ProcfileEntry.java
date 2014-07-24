package gaffer.core;

public class ProcfileEntry {
	private String name;
	private String command;

	public ProcfileEntry(String name, String command) {
		this.name = name;
		this.command = command;
	}

	public String getName() {
		return name;
	}

	public String getCommand() {
		return command;
	}
}
