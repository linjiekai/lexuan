package com.zhuanbo.core.util;

/**
 * Created by rome.chen on 2018/12/15.
 */

import org.apache.commons.lang3.StringUtils;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class DateUtil {

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String MINUTE_PATTERN = "yyyy-MM-dd HH:mm";
    public static final String HOUR_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "HH:mm:ss";
    public static final String MONTH_PATTERN = "yyyy-MM";
    public static final String YEAR_PATTERN = "yyyy";
    public static final String MINUTE_ONLY_PATTERN = "mm";
    public static final String HOUR_ONLY_PATTERN = "HH";

    public static String date8() {
        return date8(new Timestamp(System.currentTimeMillis()));
    }

    public static String date8(Object t) {
        return formatTimestamp2String(t, "yyyyMMdd");
    }

    public static String date10() {
        return date10(new Timestamp(System.currentTimeMillis()));
    }

    public static String date10(Object t) {
        return formatTimestamp2String(t, DATE_PATTERN);
    }

    public static String date14() {
        return date14(new Timestamp(System.currentTimeMillis()));
    }

    public static String date14(Object t) {
        return formatTimestamp2String(t, "yyyyMMddHHmmss");
    }
    public static String time8() {
        return time8(new Timestamp(System.currentTimeMillis()));
    }

    public static String time8(Object t) {
        return formatTimestamp2String(t, "HH:mm:ss");
    }
    /**
     * 格式化时间戳为字符串
     * @param date
     * @param format
     * @return
     */
    public static String formatTimestamp2String(Object date, String format) {
        if (null == date) {
            return null;
        }
        if (null == format || ("").equals(format)) {
            return null;
        }
        if ((date instanceof Date) || (date instanceof Timestamp)) {
            return new SimpleDateFormat(format).format(date);
        }
        return null;
    }
    
    /**
	 * 格式化字符串为时间戳
	 * @param date
	 * @param format
	 * @return
	 */
	public static Timestamp formatString2Timestamp(String date, String format) {
		if (null == date || ("").equals(date)) {
			return null;
		}
		if (null == format || ("").equals(format)) {
			return null;
		}
		return new Timestamp(formatStringToDate(date, format).getTime());
	}
	
	/**
	 * 将日期格式的字符串转换为日期
	 * @param date 源日期字符串
	 * @param format 源日期字符串格式
	 */
	public static Date formatStringToDate(String date, String format) {
		if (null == date || ("").equals(date)) {
			return null;
		}
		if (null == format || ("").equals(format)) {
			return null;
		}
		SimpleDateFormat format2 = new SimpleDateFormat(format);
		try {
			Date newDate = format2.parse(date);
			return newDate;
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}

	}
    /**
     * 日期相加减天数
     *
     * @param date        如果为Null，则为当前时间
     * @param days        加减天数
     * @param includeTime 是否包括时分秒,true表示包含
     * @return
     * @throws ParseException
     */
    public static Date dateAdd(Date date, int days, boolean includeTime) throws ParseException {
        if (date == null) {
            date = new Date();
        }
        if (!includeTime) {
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATE_PATTERN);
            date = sdf.parse(sdf.format(date));
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    /**
     * 时间格式化成字符串
     *
     * @param date    Date
     * @param pattern StrUtils.DATE_TIME_PATTERN || StrUtils.DATE_PATTERN， 如果为空，则为yyyy-MM-dd
     * @return
     * @throws ParseException
     */
    public static String dateFormat(Date date, String pattern) throws ParseException {
        if (StringUtils.isBlank(pattern)) {
            pattern = DateUtil.DATE_PATTERN;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * 字符串解析成时间对象
     *
     * @param dateTimeString String
     * @param pattern        StrUtils.DATE_TIME_PATTERN || StrUtils.DATE_PATTERN，如果为空，则为yyyy-MM-dd
     * @return
     * @throws ParseException
     */
    public static Date dateParse(String dateTimeString, String pattern) throws ParseException {
        if (StringUtils.isBlank(pattern)) {
            pattern = DateUtil.DATE_PATTERN;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.parse(dateTimeString);
    }

    /**
     * 将日期时间格式成只有日期的字符串（可以直接使用dateFormat，Pattern为Null进行格式化）
     *
     * @param dateTime Date
     * @return
     * @throws ParseException
     */
    public static String dateTimeToDateString(Date dateTime) throws ParseException {
        String dateTimeString = DateUtil.dateFormat(dateTime, DateUtil.DATE_TIME_PATTERN);
        return dateTimeString.substring(0, 10);
    }

    /**
     * 当时、分、秒为00:00:00时，将日期时间格式成只有日期的字符串，
     * 当时、分、秒不为00:00:00时，直接返回
     *
     * @param dateTime Date
     * @return
     * @throws ParseException
     */
    public static String dateTimeToDateStringIfTimeEndZero(Date dateTime) throws ParseException {
        String dateTimeString = DateUtil.dateFormat(dateTime, DateUtil.DATE_TIME_PATTERN);
        if (dateTimeString.endsWith("00:00:00")) {
            return dateTimeString.substring(0, 10);
        } else {
            return dateTimeString;
        }
    }

    /**
     * 将日期时间格式成日期对象，和dateParse互用
     *
     * @param dateTime Date
     * @return Date
     * @throws ParseException
     */
    public static Date dateTimeToDate(Date dateTime) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTime);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 时间加减小时
     *
     * @param startDate 要处理的时间，Null则为当前时间
     * @param hours     加减的小时
     * @return Date
     */
    public static Date dateAddHours(Date startDate, int hours) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.HOUR, c.get(Calendar.HOUR) + hours);
        return c.getTime();
    }

    /**
     * 时间加减分钟
     *
     * @param startDate 要处理的时间，Null则为当前时间
     * @param minutes   加减的分钟
     * @return
     */
    public static Date dateAddMinutes(Date startDate, int minutes) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + minutes);
        return c.getTime();
    }

    /**
     * 时间加减秒数
     *
     * @param startDate 要处理的时间，Null则为当前时间
     * @param seconds   加减的秒数
     * @return
     */
    public static Date dateAddSeconds(Date startDate, int seconds) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.SECOND, c.get(Calendar.SECOND) + seconds);
        return c.getTime();
    }

    /**
     * 时间加减天数
     *
     * @param startDate 要处理的时间，Null则为当前时间
     * @param days      加减的天数
     * @return Date
     */
    public static Date dateAddDays(Date startDate, int days) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.DATE, c.get(Calendar.DATE) + days);
        return c.getTime();
    }

    /**
     * 时间加减月数
     *
     * @param startDate 要处理的时间，Null则为当前时间
     * @param months    加减的月数
     * @return Date
     */
    public static Date dateAddMonths(Date startDate, int months) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.MONTH, c.get(Calendar.MONTH) + months);
        return c.getTime();
    }

    /**
     * 时间加减年数
     *
     * @param startDate 要处理的时间，Null则为当前时间
     * @param years     加减的年数
     * @return Date
     */
    public static Date dateAddYears(Date startDate, int years) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.YEAR, c.get(Calendar.YEAR) + years);
        return c.getTime();
    }

    /**
     * 时间比较（如果myDate>compareDate返回1，<返回-1，相等返回0）
     *
     * @param myDate      时间
     * @param compareDate 要比较的时间
     * @return int
     */
    public static int dateCompare(Date myDate, Date compareDate) {
        Calendar myCal = Calendar.getInstance();
        Calendar compareCal = Calendar.getInstance();
        myCal.setTime(myDate);
        compareCal.setTime(compareDate);
        return myCal.compareTo(compareCal);
    }

    /**
     * 获取两个时间中最小的一个时间
     *
     * @param date
     * @param compareDate
     * @return
     */
    public static Date dateMin(Date date, Date compareDate) {
        if (date == null) {
            return compareDate;
        }
        if (compareDate == null) {
            return date;
        }
        if (1 == dateCompare(date, compareDate)) {
            return compareDate;
        } else if (-1 == dateCompare(date, compareDate)) {
            return date;
        }
        return date;
    }

    /**
     * 获取两个时间中最大的一个时间
     *
     * @param date
     * @param compareDate
     * @return
     */
    public static Date dateMax(Date date, Date compareDate) {
        if (date == null) {
            return compareDate;
        }
        if (compareDate == null) {
            return date;
        }
        if (1 == dateCompare(date, compareDate)) {
            return date;
        } else if (-1 == dateCompare(date, compareDate)) {
            return compareDate;
        }
        return date;
    }

    /**
     * 获取两个日期（不含时分秒）相差的天数，不包含今天
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ParseException
     */
    public static int dateBetween(Date startDate, Date endDate) throws ParseException {
        Date dateStart = dateParse(dateFormat(startDate, DATE_PATTERN), DATE_PATTERN);
        Date dateEnd = dateParse(dateFormat(endDate, DATE_PATTERN), DATE_PATTERN);
        return (int) ((dateEnd.getTime() - dateStart.getTime()) / 1000 / 60 / 60 / 24);
    }

    /**
     * 获取两个日期（不含时分秒）相差的天数，包含今天
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ParseException
     */
    public static int dateBetweenIncludeToday(Date startDate, Date endDate) throws ParseException {
        return dateBetween(startDate, endDate) + 1;
    }

    /**
     * 获取日期时间的年份，如2017-02-13，返回2017
     *
     * @param date
     * @return
     */
    public static int getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    /**
     * 获取日期时间的月份，如2017年2月13日，返回2
     *
     * @param date
     * @return
     */
    public static int getMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }

    /**
     * 获取日期时间的第几天（即返回日期的dd），如2017-02-13，返回13
     *
     * @param date
     * @return
     */
    public static int getDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DATE);
    }

    /**
     * 获取日期时间当月的总天数，如2017-02-13，返回28
     *
     * @param date
     * @return
     */
    public static int getDaysOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.getActualMaximum(Calendar.DATE);
    }

    /**
     * 获取日期时间当年的总天数，如2017-02-13，返回2017年的总天数
     *
     * @param date
     * @return
     */
    public static int getDaysOfYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.getActualMaximum(Calendar.DAY_OF_YEAR);
    }

    /**
     * 根据时间获取当月最大的日期
     * <li>2017-02-13，返回2017-02-28</li>
     * <li>2016-02-13，返回2016-02-29</li>
     * <li>2016-01-11，返回2016-01-31</li>
     *
     * @param date Date
     * @return
     * @throws Exception
     */
    public static Date maxDateOfMonth(Date date) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int value = cal.getActualMaximum(Calendar.DATE);
        return dateParse(dateFormat(date, MONTH_PATTERN) + "-" + value, null);
    }

    /**
     * 根据时间获取当月最小的日期，也就是返回当月的1号日期对象
     *
     * @param date Date
     * @return
     * @throws Exception
     */
    public static Date minDateOfMonth(Date date) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int value = cal.getActualMinimum(Calendar.DATE);
        return dateParse(dateFormat(date, MONTH_PATTERN) + "-" + value, null);
    }

    public static XMLGregorianCalendar xmlToDate(Date date){
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        XMLGregorianCalendar gc = null;
        try {
            gc = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gc;
    }

    public static LocalDateTime stringToLocalDateTime(String dataTime) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return  LocalDateTime.parse(dataTime,df);
    }
    public static String LocalDateTimeToString(LocalDateTime dataTime,String formatter) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern(formatter);//yyyy-MM-dd HH:mm:ss
        return  df.format(dataTime);
    }
    public static Date UTCDateTimeToDate(String dataTime) {
        dataTime = dataTime.replace("Z", " UTC");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
        Date d = null;
        try {
            d = format.parse(dataTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  d;
    }

    public static Date getPeriodTime(int period, String periodUnit) {
        Calendar cal = Calendar.getInstance();
        switch (periodUnit) {
            case "00":
                cal.add(Calendar.MINUTE, period);
                break;
            case "01":
                cal.add(Calendar.HOUR_OF_DAY, period);
                break;
            case "02":
                cal.add(Calendar.DAY_OF_MONTH, period);
                break;
            case "03":
                cal.add(Calendar.MONTH, period);
                break;
            default:
                cal.add(Calendar.MINUTE, period);
        }
        return new Date(cal.getTimeInMillis());
    }

    /**
     * 获取精确到秒的时间戳
     * @param date
     * @return
     */
    public static int getSecondTimestamp(Long date){
        if (null == date) {
            return 0;
        }
        String timestamp = String.valueOf(date/1000);
        return Integer.valueOf(timestamp);
    }

    /**
     * 转格式：yyyy-MM-dd
     * @param now
     * @return
     */
    public static String toyyyy_MM_dd(LocalDateTime now){
        return now.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
    }

    /**
     * 转格式：HH:mm:ss
     * @param now
     * @return
     */
    public static String toHH_mm_ss(LocalDateTime now){
        return now.format(DateTimeFormatter.ofPattern(TIME_PATTERN));
    }

    /**
     * 转格式：yyyy-MM-dd HH:mm:ss
     */
    public static String toyyyy_MM_dd_HH_mm_ss(LocalDateTime now){
        return now.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
    }

    /**
     * 转格式：yyyy-MM
     */
    public static String toyyyy_MM(LocalDateTime now){
        return now.format(DateTimeFormatter.ofPattern(MONTH_PATTERN));
    }

    /**
	 * 获取当前时间前几天的时间,负数为后几天
	 * @param minute
	 * @return
	 */
	public static Date beforeDay(int day) {
		Calendar calendar = Calendar.getInstance();
		int i = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DAY_OF_MONTH, i - day);
		return new Date(calendar.getTimeInMillis());
	}

	/**
	 * 获取当前时间前几天的时间,负数为后几天
	 * @param minute
	 * @return
	 */
	public static Date beforeDay(Date date, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int i = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DAY_OF_MONTH, i - day);
		date = calendar.getTime();
		return date;
	}
	
	
	/**
	 * 获取当前时间后几天的时间,负数为前几天
	 * @param minute
	 * @return
	 */
	public static Date afterDay(int day) {
		Calendar calendar = Calendar.getInstance();
		int i = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DAY_OF_MONTH, i + day);
		return new Date(calendar.getTimeInMillis());
	}

	/**
	 * 获取当前时间后几天的时间,负数为前几天
	 * @return
	 */
	public static Date afterDay(Date date, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int i = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DAY_OF_MONTH, i + day);
		date = calendar.getTime();
		return date;
	}
	
    public static void main(String[] args) throws Exception {

        long time = DateUtil.getPeriodTime(1, "02").getTime();
        System.out.println(time);
        LocalDateTime date =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        System.out.println(date);
    }

    public static Date applyDate(int month, int day) {
        Calendar cal = Calendar.getInstance();

        int today = cal.get(Calendar.DAY_OF_MONTH);

        if (today >= 20) {
            cal.add(Calendar.MONTH, month + 1);
            cal.set(Calendar.DAY_OF_MONTH, day);
        } else {
            cal.add(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
        }

        return new Date(cal.getTimeInMillis());
    }
}
