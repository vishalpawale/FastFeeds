package com.sudosaints.rssfeeds.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.PatternSyntaxException;

public class DateHelper {

	public static final String DATE_FORMAT = "dd-MM-yyyy";
	public static final String TIME_FORMAT = "HH:mm";
	public static final String DATE_TIME_FORMAT = DATE_FORMAT+" "+TIME_FORMAT;
	
	public static final String DATE_FORMAT_FOR_DEVICE = "yyyy-MM-dd";
	public static final String DAY_DATE_TIME_LOCALE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";

	public static String getFormattedTime(Date d) {
		return new SimpleDateFormat(TIME_FORMAT).format(d);
	}
	
	public static String getFormattedDate(Date d) {
		return new SimpleDateFormat(DATE_FORMAT).format(d);		
	}

	public static String getFormattedDateForDevice(Date d) {
		return new SimpleDateFormat(DATE_FORMAT_FOR_DEVICE).format(d);		
	}

	public static String getFormattedTimestamp(Date d) {
		return new SimpleDateFormat(DATE_TIME_FORMAT).format(d);		
	}
	
	public static Date parseDateTime(String dateTime) {
		try {
			return new SimpleDateFormat(DATE_TIME_FORMAT).parse(dateTime);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Date parseDate(String date) {
		try {
			return new SimpleDateFormat(DATE_FORMAT).parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Date getCurrDateWithTimeZone(String timeZone) {
		Date date = new Date();
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		cal.setTime(date);
		return cal.getTime();
	}
	
	public static void setToStartOfDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		date.setTime(cal.getTimeInMillis());
	}
	
	public static void setToEndOfDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		date.setTime(cal.getTimeInMillis());
	}
	
	public static void setToFirstDayOfPreviousWeek(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		date.setTime(cal.getTimeInMillis());
	}
	
	public static boolean validateTime(String str) throws NumberFormatException, PatternSyntaxException {
		try {
			String[] hrsAndMinsStrings = str.split(":");
			if(hrsAndMinsStrings.length != 2) {
				return false;
			}
			if(Integer.parseInt(hrsAndMinsStrings[0]) > 23 || Integer.parseInt(hrsAndMinsStrings[1]) > 59) {
				return false;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new NumberFormatException("Invalid hrs./mins. values");
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
			throw new PatternSyntaxException("Invalid time format", ":", -1);
		}
		return true;		
	}
	
	/**
	 * Compares fromDateString with toDateString
	 * @param fromDateString fromDate which is supposed to be initial/earliest date
	 * @param toDateString toDate which is supposed to be final/later date
	 * @return returns true if fromDate is less than toDate otherwise false
	 * @author Vishal Pawale
	 */
	public static boolean compareDateStrings(String fromDateString, String toDateString) {
		Date fromDate = parseDate(fromDateString);
		Date toDate = parseDate(toDateString);
		if(toDate.compareTo(fromDate) >= 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Compares fromDate with toDate
	 * @param fromDate fromDate which is supposed to be initial/earliest date
	 * @param toDate toDate which is supposed to be final/later date
	 * @return returns true if fromDate is less than toDate otherwise false
	 * @throws NullPointerException if any of the date is null
	 * @author Vishal Pawale
	 */
	public static boolean compareDates(Date fromDate, Date toDate) {
		if(toDate.compareTo(fromDate) >= 0) {
			return true;
		}
		return false;
	}
	
	public static int getMinsFromHHMM(String hhMMString) {
		
		String[] tokenArray = hhMMString.split(":");
		int hours = Integer.valueOf(tokenArray[0]);
		int mins = Integer.valueOf(tokenArray[1]);
		return (hours * 60) + mins;
	}
	
	public static String getHHMMFromMins(int mins) {
		
		int hours = mins / 60;
		int remainMinute = mins % 60;
		String result = String.format("%02d", hours) + ":" + String.format("%02d", remainMinute);
		return result;
	}
	
	public static Date parseDayDateTimeLocale(String dateTime) {
		try {
			//http://docs.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
			return new SimpleDateFormat(DAY_DATE_TIME_LOCALE_FORMAT).parse(dateTime);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}
