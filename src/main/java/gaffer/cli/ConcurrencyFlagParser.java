package gaffer.cli;

import java.util.HashMap;
import java.util.Map;

public class ConcurrencyFlagParser {
  public static class ConcurrencyFlagParseException extends Exception {
    private static final long serialVersionUID = 7411581830623067366L;

    public ConcurrencyFlagParseException(final String msg) {
      super(msg);
    }
  }

  public static Map<String, Integer> parse(final String flagConcurrency)
      throws ConcurrencyFlagParseException {
    final HashMap<String, Integer> result = new HashMap<String, Integer>();
    if (flagConcurrency == null) {
      return result;
    }

    for (final String part : flagConcurrency.trim().split(",")) {
      if (!part.contains("=")) {
        throw new ConcurrencyFlagParseException("Concurrency should be in the format: foo=1,bar=2");
      }

      final String[] nameValue = part.split("=");
      final String name = nameValue[0].trim();
      final String value = nameValue[1].trim();
      if (name.isEmpty() || value.isEmpty()) {
        throw new ConcurrencyFlagParseException("Concurrency should be in the format: foo=1,bar=2");
      }

      final int numProcs = Integer.parseInt(value);
      result.put(name, numProcs);
    }

    return result;
  }
}
