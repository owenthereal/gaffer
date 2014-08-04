package gaffer.procfile;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

public class ProcfileTest {
  @Test
  public void readProcfile() throws IOException, URISyntaxException {
    final Path path = Paths.get(File.separator + "fixtures", "Procfile");
    final URL url = getClass().getResource(path.toString());
    final Procfile procfile = Procfile.read(Paths.get(url.toURI()));
    final List<ProcfileEntry> entries = procfile.getEntries();

    assertEquals(2, entries.size());
    assertEquals("web", entries.get(0).getName());
    assertEquals("./script/server", entries.get(0).getCommand());
    assertEquals("worker", entries.get(1).getName());
    assertEquals("rake jobs:work", entries.get(1).getCommand());
  }
}
