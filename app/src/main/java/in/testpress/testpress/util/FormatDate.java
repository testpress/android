package in.testpress.testpress.util;

import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class FormatDate {

    private static final String ABBREV_DAY = "d ago";
    private static final String ABBREV_HOUR = "h ago";
    private static final String ABBREV_MINUTE = "m ago";

    public String formatDate(String inputString) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            if(inputString != null && !inputString.isEmpty()) {
                date = simpleDateFormat.parse(inputString);
                DateFormat dateformat = DateFormat.getDateInstance();
                return dateformat.format(date);
            }
        } catch (ParseException e) {
        }
        return null;
    }

    public String getDate(String startDate, String endDate) {
        if((startDate != null) && (endDate != null)) {
            return formatDate(startDate) + " to " + formatDate(endDate);
        }
        return null;
    }

    public static boolean compareDate(String dateString1, String dateString2, String inputFormat,
                                      String timezone) {

        Date date1 =  getDate(dateString1, inputFormat, timezone);
        Date date2 =  getDate(dateString2, inputFormat, timezone);
        return  date1 != null && date2 != null && date1.after(date2);
    }

    public String formatDateTime(String inputString) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            if(inputString != null && !inputString.isEmpty()) {
                date = simpleDateFormat.parse(inputString);
                DateFormat dateformat = DateFormat.getDateTimeInstance();
                return dateformat.format(date);
            }
        } catch (ParseException e) {
        }
        return null;
    }

    public boolean isDateValid(String inputString) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
                date = simpleDateFormat.parse(inputString); //check format
        } catch (ParseException e) {
            return false;
        }
        return simpleDateFormat.format(date).equals(inputString); //check valid
    }

    public static Date getDate(String inputString, String inputFormat, String timezone) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(inputFormat);
        if (timezone != null) {
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        try {
            if(inputString != null && !inputString.isEmpty()) {
                date = simpleDateFormat.parse(inputString);
                return date;
            }
        } catch (ParseException e) {
        }
        return null;
    }

    public static String getAbbreviatedTimeSpan(long timeMillis) {
        long span = Math.max(System.currentTimeMillis() - timeMillis, 0);
        if (span >= DateUtils.WEEK_IN_MILLIS) {
            return DateUtils.getRelativeTimeSpanString(timeMillis).toString();
        }
        if (span >= DateUtils.DAY_IN_MILLIS) {
            return (span / DateUtils.DAY_IN_MILLIS) + ABBREV_DAY;
        }
        if (span >= DateUtils.HOUR_IN_MILLIS) {
            long hour = span / DateUtils.HOUR_IN_MILLIS;
            return hour + ABBREV_HOUR;
        }
        long min = span / DateUtils.MINUTE_IN_MILLIS;
        if (min == 0) {
            return "Just now";
        }
        return min + ABBREV_MINUTE;
    }
}
