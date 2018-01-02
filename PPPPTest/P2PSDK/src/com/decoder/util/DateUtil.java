package com.decoder.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

	public static String formatToNormalStyle(long time) {
		Date date = new Date(time);
		String pattern = "yyyy-MM-dd HH:mm:ss";
		java.text.DateFormat df = new SimpleDateFormat(pattern);
		return df.format(date);
	}
	
	
	public static String formatToCameraProgressStyle(long time) {
		Date date = new Date(time);
		String pattern = "MM/dd HH:mm:ss";
		java.text.DateFormat df = new SimpleDateFormat(pattern);
		return df.format(date);
	}
	
	public static String formatUTCTimeToNormalStyle(long time){
		Date date = new Date(time);
		String pattern = "yyyy-MM-dd HH:mm:ss";
		java.text.DateFormat df = new SimpleDateFormat(pattern);
		df.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
		return df.format(date);
	}

	public static String formatToNormalStyleV2(long time) {
		Date date = new Date(time);
		String pattern = "yyyyMMddHHmmss";
		java.text.DateFormat df = new SimpleDateFormat(pattern);
		return df.format(date);
	}
	
	public static String formatToNormalStyleV3(long time) {
	    Date date = new Date(time);
	    String pattern = "yyyyMMddHHmm";
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

	public static String formatToEventDateStyle(long time) {
		Date date = new Date(time);
		String pattern = "yyyy-MM-dd";
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



	public static String formatToK3Time(Date date) {
		String pattern = "yyyy_MM_dd";
		java.text.DateFormat df = new SimpleDateFormat(pattern);
		return df.format(date);
	}
}
