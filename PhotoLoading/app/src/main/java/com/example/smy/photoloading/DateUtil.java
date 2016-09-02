package com.example.smy.photoloading;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by SMY on 2016/9/2.
 */
public class DateUtil {
    public static String formatToNormalStyle(long time) {
        Date date = new Date(time);
        String pattern = "yyyy-MM-dd HH:mm:ss";
        java.text.DateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }

    public static String formatToNormalStyleV2(long time) {
        Date date = new Date(time);
        String pattern = "yyyyMMddHHmmss";
        java.text.DateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }

    public static Date parseNormalDateV2(String strDate) {
        String pattern = "yyyyMMddHHmmss";
        java.text.DateFormat df = new SimpleDateFormat(pattern);
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatHttpParamStyle(long time) {
        Date date = new Date(time);
        String pattern = "yyyyMMdd'T'HHmmss";
        java.text.DateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }

    public static String formatNormalTimeStyle(long time) {
        Date date = new Date(time);
        String pattern = "HH:mm:ss";
        java.text.DateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }

    public static String formatYYYYMMDD(long time) {
        Date date = new Date(time);
        String pattern = "yyyyMMdd";
        java.text.DateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }

    public static String formatToEventTimeStyle(long time) {
        Date date = new Date(time);
        String pattern = "HH:mm";
        java.text.DateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }

    // 转换字符串到UTC时间
    public static long parseToUTCTime(String dateStr) {
        long time = 0;
        String pattern = "yyyyMMdd'T'HHmmss";
        java.text.DateFormat df = new SimpleDateFormat(pattern);
        df.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        try {
            time = df.parse(dateStr).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    // 转换K3文件日期
    public static Date getFileDate(String path) {
        String[] pathArray = path.split("/");
        if (pathArray.length >= 5) {
            String date = pathArray[3];
            String time = pathArray[4].split("\\.")[0];
            Date result = parseFileDate(date + " " + time);
            return result;
        }

        return null;
    }

    private static Date parseFileDate(String strDate) {
        String pattern = "yyyy_MM_dd HH_mm_ss";
        java.text.DateFormat df = new SimpleDateFormat(pattern);
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date getDateByYYYYHHMM(String strDate) {
        String pattern = "yyyyMMdd";
        java.text.DateFormat df = new SimpleDateFormat(pattern);
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 以yyyy_MM_dd格式化
     * @param time
     * @return
     */
    public static String formatyyyy_MM_dd(long time) {
        Date date = new Date(time);
        String pattern = "yyyy-MM-dd";
        java.text.DateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }

    public static String formatRecoredTime(int recordTime) {
        int hour = recordTime / 60 / 60;
        int minute = recordTime / 60;
        int second = recordTime % 60;
        String result = "REC ";
        if (hour > 0) {
            if (hour < 10) {
                result += "0" + hour;
            } else {
                result += "" + hour;
            }
        }

        if (minute < 10) {
            result += "0" + minute;
        } else {
            result += "" + minute;
        }
        result += ":";
        if (second < 10) {
            result += "0" + second;
        } else {
            result += "" + second;
        }
        return result;
    }

    private static StringBuilder mFormatBuilder;
    private static Formatter mFormatter;

    public static String stringForTime(int timeMs) {
        int totalSeconds = timeMs;
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public static String timelapsTimeFormat(int totalSeconds, Context mContext) {
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int minutes = totalSeconds / 60;
        int hours = totalSeconds / 3600;
        int day = totalSeconds / (3600 * 24);

        mFormatBuilder.setLength(0);

        if (totalSeconds % (3600 * 24) == 0) {
            return day + mContext.getString(R.string.day);
        } else if (totalSeconds % 3600 == 0) {
            return hours + mContext.getString(R.string.hour);
        } else if (totalSeconds % 60 == 0) {
            return minutes + mContext.getString(R.string.minute);
        } else {
            return totalSeconds + mContext.getString(R.string.second);
        }
    }

    /**
     * 秒转换为分秒
     *
     * @param duration
     * @return
     */
    public static String getTime(int duration) {
        StringBuilder time = new StringBuilder();
        int minute = duration / 60;
        int second = duration - minute * 60;
        time.append(minute < 10 ? "0" + minute : String.valueOf(minute)).append(":");
        time.append(second < 10 ? "0" + second : String.valueOf(second));
        return time.toString();
    }

    /**
     * 社区评论显示的时间
     *
     * @param time
     * @return
     */
    public static String getCommunityTime(Context context, long time) {
        long n = System.currentTimeMillis() - time;

        if (n < (long) 1000l * 60) {
            return context.getString(R.string.a_moment_ago);
        } else if (n < (long) 1000l * 60 * 60) {
            long min = n / (long) (1000l * 60);
            if (min > 1) {
                return min + context.getString(R.string.minutes_ago);
            } else {
                return min + context.getString(R.string.minutes_ago).replace("s", "");
            }
        } else if (n < (long) 1000l * 60 * 60 * 24) {
            long hour = n / (long) (1000l * 60 * 60);
            if (hour > 1) {
                return hour + context.getString(R.string.hours_ago);
            } else {
                return hour + context.getString(R.string.hours_ago).replace("s", "");
            }
        } else if (n < (long) 1000l * 60 * 60 * 24 * 30) {
            long day = n / (long) (1000l * 60 * 60 * 24);
            if (day > 1) {
                return day + context.getString(R.string.days_ago);
            } else {
                return day + context.getString(R.string.days_ago).replace("s", "");
            }
        } else if (n < (long) 1000l * 60 * 60 * 24 * 30 * 12) {
            long month = n / (long) (1000l * 60 * 60 * 24 * 30);
            if (month > 1) {
                return month + context.getString(R.string.months_ago);
            } else {
                return month + context.getString(R.string.months_ago).replace("s", "");
            }
        } else {
            return "";
        }
    }

    /**
     * 相册的显示时间
     *
     * @param mContext
     * @param mTime
     * @return
     */
    public static String getAlbumDateString(Context mContext, long mTime) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(new Date(mTime));
        c2.setTime(new Date());
        int mCurrnetDay = c1.get(Calendar.DAY_OF_YEAR);
        int mSystemDay = c2.get(Calendar.DAY_OF_YEAR);
        int compareNum = mSystemDay - mCurrnetDay;
        if (compareNum == 0) {
            return mContext.getString(R.string.album_today) + " ";
        } else if (compareNum == 1) {
            return mContext.getString(R.string.album_yesterday) + " ";
        }
        return formatyyyy_MM_dd(mTime);
    }
}
