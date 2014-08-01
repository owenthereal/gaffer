package gaffer.process;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Process {
	private final class StdoutRunnable implements Runnable {
		private final InputStream src;

		private StdoutRunnable(InputStream src) {
			this.src = src;
		}

		public void run() {
			Scanner sc = new Scanner(src);
			try {
				while (sc.hasNextLine()) {
					logger.debug(sc.nextLine());
				}
			} finally {
				sc.close();
			}
		}
	}

	private String dir;
	private String name;
	private String cmd;

	private java.lang.Process p;
	private final Logger logger;
	private final ExecutorService pool;

	public Process(String dir, String name, String cmd) {
		this.dir = dir;
		this.name = name;
		this.cmd = cmd;
		this.logger = LoggerFactory.getLogger(name);
		this.pool = Executors.newFixedThreadPool(2);
	}

	public synchronized void start() throws ProcessException {
		ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
		pb.directory(new File(dir));
		pb.redirectInput(Redirect.INHERIT);
		pb.redirectErrorStream();

		try {
			p = pb.start();
			pool.execute(new StdoutRunnable(new BufferedInputStream(p
					.getInputStream())));
			pool.execute(new StdoutRunnable(new BufferedInputStream(p
					.getErrorStream())));
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new ProcessException(e.getMessage());
		} finally {
			pool.shutdown();
		}
	}

	public synchronized boolean isAlive() {
		return p != null && p.isAlive();
	}

	public synchronized void kill() {
		if (isAlive()) {
			p.destroy();
		}
	}

	public String getName() {
		return name;
	}
}