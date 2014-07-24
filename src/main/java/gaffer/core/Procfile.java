package gaffer.core;


public class Procfile {
	public ProcfileEntry[] getEntries() {
		return new ProcfileEntry[] {
				new ProcfileEntry("web", "./script/server"),
				new ProcfileEntry("worker", "bundle exec rake jobs:work") };
	}

	public static Procfile read(String dir2) {
		return new Procfile();
	}
}
