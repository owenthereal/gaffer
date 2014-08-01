package gaffer.cli;

public class CommandException extends Exception {
  private static final long serialVersionUID = 974553763738904807L;
  
  public CommandException(String msg) {
    super(msg);
  }
}
