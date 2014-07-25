package gaffer.logback;

import gaffer.core.ProcessManager;

import java.util.concurrent.atomic.AtomicInteger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import static ch.qos.logback.core.pattern.color.ANSIConstants.*;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

public class ProcessNameColorConverter extends
		ForegroundCompositeConverterBase<ILoggingEvent> {
	private static final String[] COLORS = new String[] { CYAN_FG, BLUE_FG,
			MAGENTA_FG, GREEN_FG, MAGENTA_FG, YELLOW_FG };
	private final AtomicInteger index = new AtomicInteger(0);

	@Override
	protected String getForegroundColorCode(ILoggingEvent event) {
		if (ProcessManager.GAFFER_LOGGER.equals(event.getLoggerName())) {
			return WHITE_FG;
		}

		int i = index.getAndIncrement() % COLORS.length;
		return COLORS[i];
	}
}
