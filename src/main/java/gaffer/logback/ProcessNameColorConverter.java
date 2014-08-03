package gaffer.logback;

import static ch.qos.logback.core.pattern.color.ANSIConstants.BLUE_FG;
import static ch.qos.logback.core.pattern.color.ANSIConstants.CYAN_FG;
import static ch.qos.logback.core.pattern.color.ANSIConstants.GREEN_FG;
import static ch.qos.logback.core.pattern.color.ANSIConstants.MAGENTA_FG;
import static ch.qos.logback.core.pattern.color.ANSIConstants.RED_FG;
import static ch.qos.logback.core.pattern.color.ANSIConstants.WHITE_FG;
import static ch.qos.logback.core.pattern.color.ANSIConstants.YELLOW_FG;
import gaffer.process.ProcessManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

public class ProcessNameColorConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {
  private static final String[] COLORS = new String[] {CYAN_FG, YELLOW_FG, GREEN_FG, MAGENTA_FG,
      RED_FG, BLUE_FG};
  private final AtomicInteger index = new AtomicInteger(0);
  private final ConcurrentHashMap<String, String> cachedColors =
      new ConcurrentHashMap<String, String>();

  public ProcessNameColorConverter() {
    cachedColors.put(ProcessManager.GAFFER_LOGGER, WHITE_FG);
  }

  @Override
  protected String getForegroundColorCode(final ILoggingEvent event) {
    final String name = event.getLoggerName();

    if (cachedColors.containsKey(name)) {
      return cachedColors.get(name);
    }

    final int i = index.getAndIncrement() % COLORS.length;
    final String color = COLORS[i];
    cachedColors.putIfAbsent(name, color);

    return color;
  }
}
