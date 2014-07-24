package gaffer.cli;

import gaffer.core.ProcessManager;
import gaffer.core.Procfile;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main {
	public static void main(String args[]) throws IOException,
			InterruptedException, ExecutionException {
		String dir = System.getProperty("user.dir");
		Procfile procfile = Procfile.read(dir);
		ProcessManager manager = new ProcessManager(procfile);
		manager.start(dir);
	}
}