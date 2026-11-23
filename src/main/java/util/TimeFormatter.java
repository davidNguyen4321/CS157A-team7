package util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import servlet.security.ParamSecurity;

public class TimeFormatter{	    
    public static String formatTimeAgo(String pastDate, String fromTimezone, String toTimezone) {
        if (pastDate == null || pastDate.trim().isEmpty()) {
            return "";
        }
        
        ZoneId fromTz;
        
        try {
            if (fromTimezone == null || fromTimezone.trim().isEmpty()) {
                throw new IllegalArgumentException("No timezone provided");
            }
            fromTz = ZoneId.of(fromTimezone);
        } catch (Exception e) {
        	fromTz = ZoneId.of("America/Los_Angeles");
        }
        
        try {
        	LocalDateTime localDateTime = LocalDateTime.parse(pastDate);
        	Date parsedDate = Date.from(localDateTime.atZone(fromTz).toInstant());
            return formatTimeAgo(parsedDate, toTimezone);
        } catch (Exception e) {
        	e.printStackTrace();
            return ""; 
        }
    }
    
    private static String formatTimeAgo(Date pastDate, String toTimezone) {
        if (pastDate == null) return "";

        long now = System.currentTimeMillis();
        long past = pastDate.getTime();
        long diff = now - past;

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (minutes < 60) {
            if (minutes <= 0) return "Just now";
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else if (days < 7) {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
            TimeZone tz;
            
            try {
                if (toTimezone == null || toTimezone.trim().isEmpty()) {
                    throw new IllegalArgumentException("No timezone provided");
                }
                tz = TimeZone.getTimeZone(ZoneId.of(toTimezone));
            } catch (Exception e) {
                tz = TimeZone.getTimeZone("America/Los_Angeles");
            }
            
            sdf.setTimeZone(tz);
            return sdf.format(pastDate);
        }
    }
}