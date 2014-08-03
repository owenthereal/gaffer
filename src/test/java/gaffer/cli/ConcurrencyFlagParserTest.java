package gaffer.cli;

import static org.junit.Assert.assertEquals;
import gaffer.cli.ConcurrencyFlagParser.ConcurrencyFlagParseException;

import java.util.Map;

import org.junit.Test;

public class ConcurrencyFlagParserTest {
  @Test
  public void parseValidConcurrencyFlag() throws ConcurrencyFlagParseException {
    final Map<String, Integer> result = ConcurrencyFlagParser.parse("foo=1,bar=2");
    assertEquals(2, result.size());
    assertEquals((Integer) 1, result.get("foo"));
    assertEquals((Integer) 2, result.get("bar"));
  }

  @Test(expected = ConcurrencyFlagParseException.class)
  public void parseInValidConcurrencyFlag() throws ConcurrencyFlagParseException {
    ConcurrencyFlagParser.parse("foo, 1");
  }
}
