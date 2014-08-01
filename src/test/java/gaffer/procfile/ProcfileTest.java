package gaffer.procfile;

import static org.junit.Assert.*;
import gaffer.procfile.Procfile;
import gaffer.procfile.ProcfileEntry;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class ProcfileTest {
  @Test
  public void readProcfile() throws IOException, URISyntaxException {
    Path path = Paths.get(File.separator + "fixtures", "Procfile");
    URL url = getClass().getResource(path.toString());
    Procfile procfile = Procfile.read(Paths.get(url.toURI()));
    ProcfileEntry[] entries = procfile.getEntries();

    assertEquals(2, entries.length);
    assertEquals("web", entries[0].getName());
    assertEquals("./script/server", entries[0].getCommand());
    assertEquals("worker", entries[1].getName());
    assertEquals("rake jobs:work", entries[1].getCommand());
  }
}
