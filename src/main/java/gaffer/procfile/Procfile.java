package gaffer.procfile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Procfile {
  private static final Pattern PROCFILE_REGEXP_PATTERN = Pattern
      .compile("^([A-Za-z0-9_]+):\\s*(.+)$");
  private final List<ProcfileEntry> entries = new LinkedList<ProcfileEntry>();
  private final Path path;

  private Procfile(final Path path) {
    this.path = path;
  }

  public Path getPath() {
    return path;
  }

  public ProcfileEntry[] getEntries() {
    return entries.toArray(new ProcfileEntry[entries.size()]);
  }

  private void addEntry(final ProcfileEntry entry) {
    entries.add(entry);
  }

  public static Procfile read(final Path path) throws IOException {
    final List<String> lines = Files.readAllLines(path);

    final Procfile procfile = new Procfile(path);
    for (final String line : lines) {
      final Matcher matcher = PROCFILE_REGEXP_PATTERN.matcher(line);
      if (matcher.matches()) {
        final ProcfileEntry entry = new ProcfileEntry(matcher.group(1), matcher.group(2));
        procfile.addEntry(entry);
      }
    }

    return procfile;
  }
}
