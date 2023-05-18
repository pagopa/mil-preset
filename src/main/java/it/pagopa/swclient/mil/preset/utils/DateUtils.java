/**
 * 
 */
package it.pagopa.swclient.mil.preset.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

	/**
	 * Utility method. Generate a formatted current date time
	 * @return formatted current date time. Format yyyy-MM-dd'T'HH:mm:ss.SS
	 */
	public static String getAndFormatCurrentDate() {
		final String pattern = "yyyy-MM-dd'T'HH:mm:ss.SS";

		DateFormat df 		= new SimpleDateFormat(pattern);
		Date currentDate 	= Calendar.getInstance().getTime();        
		return df.format(currentDate);
	}
}
