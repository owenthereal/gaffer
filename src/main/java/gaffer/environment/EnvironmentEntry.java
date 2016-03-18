package gaffer.environment;

public class EnvironmentEntry {
	private final String name;
	private final String value;

	public EnvironmentEntry(final String name, final String command) {
	    this.name = name;
	    this.value = command;
	  }

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
}
