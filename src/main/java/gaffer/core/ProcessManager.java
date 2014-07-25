package gaffer.core;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessManager {
	public static final String GAFFER_LOGGER = "gaffer";
	private static final Logger LOGGER = LoggerFactory.getLogger(GAFFER_LOGGER);

	private Procfile procfile;

	public ProcessManager(Procfile procfile) {
		this.procfile = procfile;
	}

	public void start(String dir) {
		ProcfileEntry[] entries = procfile.getEntries();
		Process[] processes = new Process[entries.length];
		for (int i = 0; i < entries.length; i++) {
			ProcfileEntry entry = entries[i];
			processes[i] = new Process(dir, entry.getName(), entry.getCommand());
		}

		ExecutorService pool = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors());
		try {
			CompletionService<Process> ecs = new ExecutorCompletionService<Process>(
					pool);
			runAll(ecs, processes);
		} finally {
			pool.shutdown();
		}
	}

	private void runAll(CompletionService<Process> ecs, Process[] processes) {
		for (Process process : processes) {
			ecs.submit(process);
		}

		for (int i = 0; i < processes.length; i++) {
			try {
				ecs.take().get();
			} catch (Exception e) {
				killAll(processes);
			}
		}
	}

	private void killAll(Process[] processes) {
		for (Process process : processes) {
			if (process.isAlive()) {
				LOGGER.debug("Sending SIGTERM to {}", process.getName());
				process.kill();
			}
		}
	}
}