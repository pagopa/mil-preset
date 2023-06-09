/**
 * 
 */
package it.pagopa.swclient.mil.preset.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {

	private DateUtils() {}

	/**
	 * Generates the current timestamp (UTC time) in the uuuu-MM-dd'T'HH:mm:ss format
	 * @return the timestamp
	 */
	public static String getCurrentTimestamp() {
		return LocalDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC)
				.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}


}
