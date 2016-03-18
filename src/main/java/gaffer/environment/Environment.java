package gaffer.environment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Environment {
	private static final Pattern ENV_REGEXP_PATTERN = Pattern.compile("^([A-Za-z0-9_]+)=\\s*(.+)$");
	private final List<EnvironmentEntry> entries = new LinkedList<EnvironmentEntry>();
	private final Path path;

	private Environment(final Path path) {
		this.path = path;
	}

	public Path getPath() {
		return path;
	}

	public List<EnvironmentEntry> getEntries() {
		return entries;
	}

	private void addEntry(final EnvironmentEntry entry) {
		entries.add(entry);
	}

	public static Environment read(final Path path) throws IOException {
		final List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

		final Environment environment = new Environment(path);
		for (final String line : lines) {
			final Matcher matcher = ENV_REGEXP_PATTERN.matcher(line);
			if (matcher.matches()) {
				final EnvironmentEntry entry = new EnvironmentEntry(matcher.group(1), matcher.group(2));
				environment.addEntry(entry);
			}
		}

		return environment;
	}

	public Map<String, String> enhance(Map<String, String> parent) {
		for (EnvironmentEntry environmentEntry : getEntries()) {
			parent.put(environmentEntry.getName(), environmentEntry.getValue());
		}
		return parent;
	}

}
