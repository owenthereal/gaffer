package gaffer.core;

import java.io.File;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Process implements Callable<Process> {
	private String dir;
	private String name;
	private String cmd;

	private AtomicInteger exitCode = new AtomicInteger(0);
	private java.lang.Process p;
	private final ReentrantLock processLock = new ReentrantLock();
	private final Logger logger;

	public Process(String dir, String name, String cmd) {
		this.dir = dir;
		this.name = name;
		this.cmd = cmd;
		logger = LoggerFactory.getLogger(name);
	}

	public void start() throws ProcessException {
		ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
		pb.directory(new File(dir));
		pb.inheritIO();
		try {
			try {
				processLock.lock();
				p = pb.start();
			} finally {
				processLock.unlock();
			}

			Scanner scanner = new Scanner(p.getInputStream());
			try {
				while (scanner.hasNext()) {
					System.out.println(scanner.nextLine());
				}
			} finally {
				scanner.close();
			}

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