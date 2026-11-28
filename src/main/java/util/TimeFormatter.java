package util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TimeFormatter {	    
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
	
	public static String formatTimeAgo(LocalDateTime utcTime, String viewerTimezone) {
		Instant past = utcTime.toInstant(ZoneOffset.UTC);
    	Duration diff = Duration.between(past, Instant.now());
    	
    	long minutes = diff.toMinutes();
        long hours = diff.toHours();
        long days = diff.toDays();

        if (minutes < 60) {
            if (minutes <= 0) return "Just now";
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } 
        else if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } 
        else if (days < 7) {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        }
        
        return FORMATTER.format(past.atZone(ZoneId.of(viewerTimezone)));
    }
}