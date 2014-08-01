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
  private List<ProcfileEntry> entries = new LinkedList<ProcfileEntry>();

  public ProcfileEntry[] getEntries() {
    return entries.toArray(new ProcfileEntry[entries.size()]);
  }

  private void addEntry(ProcfileEntry entry) {
    entries.add(entry);
  }

  public static Procfile read(Path path) throws IOException {
    List<String> lines = Files.readAllLines(path);

    Procfile procfile = new Procfile();
    for (String line : lines) {
      Matcher matcher = PROCFILE_REGEXP_PATTERN.matcher(line);
      if (matcher.matches()) {
        ProcfileEntry entry = new ProcfileEntry(matcher.group(1), matcher.group(2));
        procfile.addEntry(entry);
      }
    }

    return procfile;
  }
}
