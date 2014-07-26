package gaffer.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Process implements Callable<Process> {
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

	private final AtomicInteger exitCode = new AtomicInteger(0);
	private java.lang.Process p;
	private final ReentrantLock processLock = new ReentrantLock();
	private final Logger logger;
	private final ExecutorService pool;

	public Process(String dir, String name, String cmd) {
		this.dir = dir;
		this.name = name;
		this.cmd = cmd;
		this.logger = LoggerFactory.getLogger(name);
		this.pool = Executors.newFixedThreadPool(2);
	}

	public void start() throws ProcessException {
		ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
		pb.directory(new File(dir));
		pb.redirectInput(Redirect.INHERIT);
		pb.redirectErrorStream();

		try {
			try {
				processLock.lock();
				p = pb.start();
			} finally {
				processLock.unlock();
			}

			pool.submit(new StdoutRunnable(new BufferedInputStream(p
					.getInputStream())));
			pool.submit(new StdoutRunnable(new BufferedInputStream(p
					.getErrorStream())));

			exitCode.set(p.waitFor());

			if (exitCode.get() != 0) {
				String errMsg = String.format("Process %s exits with %d", name,
						exitCode.get());
				throw new ProcessException(errMsg);
			}
		} catch (Exception e) {
			if (exitCode.get() == 0) {
				exitCode.set(1);
			}

			logger.error(e.getMessage());
			throw new ProcessException(e.getMessage());
		} finally {
			pool.shutdown();
		}
	}

	public boolean isAlive() {
		try {
			processLock.lock();
			return isAliveInteranl();
		} finally {
			processLock.unlock();
		}
	}

	private boolean isAliveInteranl() {
		return p != null && p.isAlive();

	}

	public void kill() {
		try {
			processLock.lock();
			if (isAliveInteranl()) {
				p.destroyForcibly();
			}
		} finally {
			processLock.unlock();
		}
	}

	public String getName() {
		return name;
	}

	@Override
	public Process call() throws Exception {
		start();
		return this;
	}
}