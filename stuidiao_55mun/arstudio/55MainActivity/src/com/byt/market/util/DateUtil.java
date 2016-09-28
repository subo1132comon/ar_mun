package com.byt.market.util;

import java.io.Serializable;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public final class DateUtil implements Serializable {

	private static final long serialVersionUID = -3098985139095632110L;

	private DateUtil() {
	}

	public static String dateFormat(String sdate, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		java.sql.Date date = java.sql.Date.valueOf(sdate);
		String dateString = formatter.format(date);

		return dateString;
	}

	public static long getIntervalDays(String sd, String ed) {
		return ((java.sql.Date.valueOf(ed)).getTime() - (java.sql.Date
				.valueOf(sd)).getTime()) / (3600 * 24 * 1000);
	}

	public static int getInterval(String beginMonth, String endMonth) {
		int intBeginYear = Integer.parseInt(beginMonth.substring(0, 4));
		int intBeginMonth = Integer.parseInt(beginMonth.substring(beginMonth
				.indexOf("-") + 1));
		int intEndYear = Integer.parseInt(endMonth.substring(0, 4));
		int intEndMonth = Integer.parseInt(endMonth.substring(endMonth
				.indexOf("-") + 1));

		return ((intEndYear - intBeginYear) * 12)
				+ (intEndMonth - intBeginMonth) + 1;
	}

	public static Date getDate(String sDate, String dateFormat) {
		SimpleDateFormat fmt = new SimpleDateFormat(dateFormat);
		ParsePosition pos = new ParsePosition(0);

		return fmt.parse(sDate, pos);
	}

	public static String getCurrentYear() {
		return getFormatCurrentTime("yyyy");
	}

	public static String getBeforeYear() {
		String currentYear = getFormatCurrentTime("yyyy");
		int beforeYear = Integer.parseInt(currentYear) - 1;
		return "" + beforeYear;
	}

	public static String getCurrentMonth() {
		return getFormatCurrentTime("MM");
	}

	public static String getCurrentDay() {
		return getFormatCurrentTime("dd");
	}

	public static String getCurrentDate() {
		return getFormatDateTime(new Date(), "yyyy-MM-dd");
	}

	public static String getCurrentDateTime() {
		return getFormatDateTime(new Date(), "yyyy-MM-dd HH:mm:ss");
	}

	public static String getFormatDate(Date date) {
		return getFormatDateTime(date, "yyyy-MM-dd");
	}

	public static String getFormatDate(String format) {
		return getFormatDateTime(new Date(), format);
	}

	public static String getCurrentTime() {
		return getFormatDateTime(new Date(), "yyyy-MM-dd HH:mm:ss");
	}

	public static String getFormatTime(Date date) {
		return getFormatDateTime(date, "yyyy-MM-dd HH:mm:ss");
	}

	public static String getFormatShortTime(long timemillis) {
		Date date = new Date(timemillis);
		return getFormatShortTime2(date);
	}
	public static String getFormatShortTime3(long timemillis) {
		Date date = new Date(timemillis);
		return getFormatShortTime3(date);
	}
	public static String getFormatShortTime(Date date) {
		return getFormatDateTime(date, "yyyy-MM-dd");
	}
	public static String getFormatShortTime2(Date date) {
		return getFormatDateTime(date, "yyyy-MM-dd HH:mm");
	}
	public static String getFormatShortTime3(Date date) {
		return getFormatDateTime(date, "dd/MM/yyyy ");
	}
	public static String getFormatCurrentTime(String format) {
		return getFormatDateTime(new Date(), format);
	}

	public static String getFormatDateTime(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	public static Date getDateObj(int year, int month, int day) {
		Calendar c = new GregorianCalendar();
		c.set(year, month - 1, day);
		return c.getTime();
	}

	public static String getDateTomorrow(String date) {

		Date tempDate = null;
		if (date.indexOf("/") > 0)
			tempDate = getDateObj(date, "[/]");
		if (date.indexOf("-") > 0)
			tempDate = getDateObj(date, "[-]");
		tempDate = getDateAdd(tempDate, 1);
		return getFormatDateTime(tempDate, "yyyy/MM/dd");
	}

	public static String getDateOffset(String date, int offset) {

		// Date tempDate = getDateObj(date, "[/]");
		Date tempDate = null;
		if (date.indexOf("/") > 0)
			tempDate = getDateObj(date, "[/]");
		if (date.indexOf("-") > 0)
			tempDate = getDateObj(date, "[-]");
		tempDate = getDateAdd(tempDate, offset);
		return getFormatDateTime(tempDate, "yyyy/MM/dd");
	}

	public static Date getDateObj(String argsDate, String split) {
		String[] temp = argsDate.split(split);
		int year = new Integer(temp[0]).intValue();
		int month = new Integer(temp[1]).intValue();
		int day = new Integer(temp[2]).intValue();
		return getDateObj(year, month, day);
	}

	public static Date getDateFromString(String dateStr, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		Date resDate = null;
		try {
			resDate = sdf.parse(dateStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resDate;
	}

	public static Date getDateObj() {
		Calendar c = new GregorianCalendar();
		return c.getTime();
	}

	public static int getDaysOfCurMonth() {
		int curyear = new Integer(getCurrentYear()).intValue(); // ��ǰ���
		int curMonth = new Integer(getCurrentMonth()).intValue();// ��ǰ�·�
		int mArray[] = new int[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30,
				31 };
		if ((curyear % 400 == 0)
				|| ((curyear % 100 != 0) && (curyear % 4 == 0))) {
			mArray[1] = 29;
		}
		return mArray[curMonth - 1];
	}

	public static int getDaysOfCurMonth(final String time) {
		if (time.length() != 7) {
		}
		String[] timeArray = time.split("-");
		int curyear = new Integer(timeArray[0]).intValue(); // ��ǰ���
		int curMonth = new Integer(timeArray[1]).intValue();// ��ǰ�·�
		if (curMonth > 12) {
			throw new NullPointerException(
					"����ĸ�ʽ������yyyy-MM�������·ݱ���С�ڵ���12��");
		}
		int mArray[] = new int[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30,
				31 };
		// �ж��������� ��2�·���29�죻
		if ((curyear % 400 == 0)
				|| ((curyear % 100 != 0) && (curyear % 4 == 0))) {
			mArray[1] = 29;
		}
		if (curMonth == 12) {
			return mArray[0];
		}
		return mArray[curMonth - 1];
		// ���Ҫ�����¸��µ�����ע�⴦���·�12���������ֹ����Խ�磻
		// ���Ҫ�����ϸ��µ�����ע�⴦���·�1���������ֹ����Խ�磻
	}

	public static int getDayofWeekInMonth(String year, String month,
			String weekOfMonth, String dayOfWeek) {
		Calendar cal = new GregorianCalendar();
		// �ھ���Ĭ�����Ի�����Ĭ��ʱ����ʹ�õ�ǰʱ�乹��һ��Ĭ�ϵ� GregorianCalendar��
		int y = new Integer(year).intValue();
		int m = new Integer(month).intValue();
		cal.clear();// ��������ǰ������
		cal.set(y, m - 1, 1);// ����������Ϊ���µĵ�һ�졣
		cal.set(Calendar.DAY_OF_WEEK_IN_MONTH,
				new Integer(weekOfMonth).intValue());
		cal.set(Calendar.DAY_OF_WEEK, new Integer(dayOfWeek).intValue());
		// System.out.print(cal.get(Calendar.MONTH)+" ");
		// System.out.print("��"+cal.get(Calendar.WEEK_OF_MONTH)+"\t");
		// WEEK_OF_MONTH��ʾ�����ڱ��µĵڼ����ܡ�����1�������ڼ�������ʾ�ڱ��µĵ�һ����
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * ���ָ����������Сʱ���룬����һ��java.Util.Date����
	 * 
	 * @param year
	 *            ��
	 * @param month
	 *            �� 0-11
	 * @param date
	 *            ��
	 * @param hourOfDay
	 *            Сʱ 0-23
	 * @param minute
	 *            �� 0-59
	 * @param second
	 *            �� 0-59
	 * @return һ��Date����
	 */
	public static Date getDate(int year, int month, int date, int hourOfDay,
			int minute, int second) {
		Calendar cal = new GregorianCalendar();
		cal.set(year, month, date, hourOfDay, minute, second);
		return cal.getTime();
	}

	/**
	 * ���ָ�����ꡢ�¡��շ��ص�ǰ�����ڼ���1��ʾ�����졢2��ʾ����һ��7��ʾ������
	 * 
	 * @param year
	 * @param month
	 *            month�Ǵ�1��ʼ��12����
	 * @param day
	 * @return ����һ����?�����������ڼ������֡�1��ʾ�����졢2��ʾ����һ��7��ʾ������
	 */
	public static int getDayOfWeek(String year, String month, String day) {
		Calendar cal = new GregorianCalendar(new Integer(year).intValue(),
				new Integer(month).intValue() - 1, new Integer(day).intValue());
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * ���ָ�����ꡢ�¡��շ��ص�ǰ�����ڼ���1��ʾ�����졢2��ʾ����һ��7��ʾ������
	 * 
	 * @param date
	 *            "yyyy/MM/dd",����"yyyy-MM-dd"
	 * @return ����һ����?�����������ڼ������֡�1��ʾ�����졢2��ʾ����һ��7��ʾ������
	 */
	public static int getDayOfWeek(String date) {
		String[] temp = null;
		if (date.indexOf("/") > 0) {
			temp = date.split("/");
		}
		if (date.indexOf("-") > 0) {
			temp = date.split("-");
		}
		return getDayOfWeek(temp[0], temp[1], temp[2]);
	}

	/**
	 * ���ص�ǰ���������ڼ������磺�����ա�����һ��������ȵȡ�
	 * 
	 * @param date
	 *            ��ʽΪ yyyy/MM/dd ���� yyyy-MM-dd
	 * @return ���ص�ǰ���������ڼ�
	 */
	public static String getChinaDayOfWeek(String date) {
		String[] weeks = new String[] { "������", "����һ", "���ڶ�", "������",
				"������", "������", "������" };
		int week = getDayOfWeek(date);
		return weeks[week - 1];
	}

	/**
	 * ���ָ�����ꡢ�¡��շ��ص�ǰ�����ڼ���1��ʾ�����졢2��ʾ����һ��7��ʾ������
	 * 
	 * @param date
	 * 
	 * @return ����һ����?�����������ڼ������֡�1��ʾ�����졢2��ʾ����һ��7��ʾ������
	 */
	public static int getDayOfWeek(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * �����ƶ��������ڵ�����һ���еĵڼ����ܡ�<br>
	 * created by wangmj at 20060324.<br>
	 * 
	 * @param year
	 * @param month
	 *            ��Χ1-12<br>
	 * @param day
	 * @return int
	 */
	public static int getWeekOfYear(String year, String month, String day) {
		Calendar cal = new GregorianCalendar();
		cal.clear();
		cal.set(new Integer(year).intValue(),
				new Integer(month).intValue() - 1, new Integer(day).intValue());
		return cal.get(Calendar.WEEK_OF_YEAR);
	}

	/**
	 * ȡ�ø����ڼ���һ�����������ڶ���.
	 * 
	 * @param date
	 *            ������ڶ���
	 * @param amount
	 *            ��Ҫ��ӵ������������ǰ������ʹ�ø���Ϳ���.
	 * @return Date ����һ�������Ժ��Date����.
	 */
	public static Date getDateAdd(Date date, int amount) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(GregorianCalendar.DATE, amount);
		return cal.getTime();
	}

	/**
	 * ȡ�ø����ڼ���һ�����������ڶ���.
	 * 
	 * @param date
	 *            ������ڶ���
	 * @param amount
	 *            ��Ҫ��ӵ������������ǰ������ʹ�ø���Ϳ���.
	 * @param format
	 *            �����ʽ.
	 * @return Date ����һ�������Ժ��Date����.
	 */
	public static String getFormatDateAdd(Date date, int amount, String format) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(GregorianCalendar.DATE, amount);
		return getFormatDateTime(cal.getTime(), format);
	}

	/**
	 * ��õ�ǰ���ڹ̶������������ڣ���ǰ60��dateAdd(-60)
	 * 
	 * @param amount
	 *            �����ļ�����ڳ��ȣ���ǰΪ�������Ϊ��
	 * @param format
	 *            ������ڵĸ�ʽ.
	 * @return java.lang.String ���ո�ʽ����ļ���������ַ�.
	 */
	public static String getFormatCurrentAdd(int amount, String format) {

		Date d = getDateAdd(new Date(), amount);

		return getFormatDateTime(d, format);
	}

	/**
	 * ȡ�ø��ʽ��������������
	 * 
	 * @param format
	 *            ��������ĸ�ʽ
	 * @return String ���ʽ�������ַ�.
	 */
	public static String getFormatYestoday(String format) {
		return getFormatCurrentAdd(-1, format);
	}

	/**
	 * ����ָ�����ڵ�ǰһ�졣<br>
	 * 
	 * @param sourceDate
	 * @param format
	 *            yyyy MM dd hh mm ss
	 * @return ���������ַ���ʽ��formcatһ�¡�
	 */
	public static String getYestoday(String sourceDate, String format) {
		return getFormatDateAdd(getDateFromString(sourceDate, format), -1,
				format);
	}

	/**
	 * ������������ڣ�<br>
	 * 
	 * @param format
	 * @return ���������ַ���ʽ��formcatһ�¡�
	 */
	public static String getFormatTomorrow(String format) {
		return getFormatCurrentAdd(1, format);
	}

	/**
	 * ����ָ�����ڵĺ�һ�졣<br>
	 * 
	 * @param sourceDate
	 * @param format
	 * @return ���������ַ���ʽ��formcatһ�¡�
	 */
	public static String getFormatDateTommorrow(String sourceDate, String format) {
		return getFormatDateAdd(getDateFromString(sourceDate, format), 1,
				format);
	}

	/**
	 * ��������Ĭ�� TimeZone�������ָ����ʽ��ʱ���ַ�
	 * 
	 * @param dateFormat
	 * @return ���������ַ���ʽ��formcatһ�¡�
	 */
	public static String getCurrentDateString(String dateFormat) {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		sdf.setTimeZone(TimeZone.getDefault());

		return sdf.format(cal.getTime());
	}

	// /**
	// * @deprecated ������ʹ�á� ���ص�ǰʱ�䴮 ��ʽ:yyMMddhhmmss,���ϴ�����ʱʹ��
	// *
	// * @return String
	// */
	// public static String getCurDate() {
	// GregorianCalendar gcDate = new GregorianCalendar();
	// int year = gcDate.get(GregorianCalendar.YEAR);
	// int month = gcDate.get(GregorianCalendar.MONTH) + 1;
	// int day = gcDate.get(GregorianCalendar.DAY_OF_MONTH);
	// int hour = gcDate.get(GregorianCalendar.HOUR_OF_DAY);
	// int minute = gcDate.get(GregorianCalendar.MINUTE);
	// int sen = gcDate.get(GregorianCalendar.SECOND);
	// String y;
	// String m;
	// String d;
	// String h;
	// String n;
	// String s;
	// y = new Integer(year).toString();
	//
	// if (month < 10) {
	// m = "0" + new Integer(month).toString();
	// } else {
	// m = new Integer(month).toString();
	// }
	//
	// if (day < 10) {
	// d = "0" + new Integer(day).toString();
	// } else {
	// d = new Integer(day).toString();
	// }
	//
	// if (hour < 10) {
	// h = "0" + new Integer(hour).toString();
	// } else {
	// h = new Integer(hour).toString();
	// }
	//
	// if (minute < 10) {
	// n = "0" + new Integer(minute).toString();
	// } else {
	// n = new Integer(minute).toString();
	// }
	//
	// if (sen < 10) {
	// s = "0" + new Integer(sen).toString();
	// } else {
	// s = new Integer(sen).toString();
	// }
	//
	// return "" + y + m + d + h + n + s;
	// }

	/**
	 * ��ݸ�ĸ�ʽ������ʱ���ַ� ��getFormatDate(String format)���ơ�
	 * 
	 * @param format
	 *            yyyy MM dd hh mm ss
	 * @return ����һ��ʱ���ַ�
	 */
	public static String getCurTimeByFormat(String format) {
		Date newdate = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(newdate);
	}

	/**
	 * ��ȡ����ʱ�䴮ʱ��Ĳ�ֵ����λΪ��
	 * 
	 * @param startTime
	 *            ��ʼʱ�� yyyy-MM-dd HH:mm:ss
	 * @param endTime
	 *            ����ʱ�� yyyy-MM-dd HH:mm:ss
	 * @return ����ʱ��Ĳ�ֵ(��)
	 */
	public static long getDiff(String startTime, String endTime) {
		long diff = 0;
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date startDate = ft.parse(startTime);
			Date endDate = ft.parse(endTime);
			diff = startDate.getTime() - endDate.getTime();
			diff = diff / 1000;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return diff;
	}

	/**
	 * ��ȡСʱ/����/��
	 * 
	 * @param second
	 *            ��
	 * @return ��Сʱ�����ӡ����ʱ���ַ�����3Сʱ23����13�롣
	 */
	public static String getHour(long second) {
		long hour = second / 60 / 60;
		long minute = (second - hour * 60 * 60) / 60;
		long sec = (second - hour * 60 * 60) - minute * 60;

		return hour + "Сʱ" + minute + "����" + sec + "��";

	}

	/**
	 * ����ָ��ʱ���ַ�
	 * <p>
	 * ��ʽ��yyyy-MM-dd HH:mm:ss
	 * 
	 * @return String ָ����ʽ�������ַ�.
	 */
	public static String getDateTime(long microsecond) {
		return getFormatDateTime(new Date(microsecond), "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * ���ص�ǰʱ���ʵ��Сʱ�������ʱ�䡣
	 * <p>
	 * ��ʽ��yyyy-MM-dd HH:mm:ss
	 * 
	 * @return Float �Ӽ�ʵ��Сʱ.
	 */
	public static String getDateByAddFltHour(float flt) {
		int addMinute = (int) (flt * 60);
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		cal.add(GregorianCalendar.MINUTE, addMinute);
		return getFormatDateTime(cal.getTime(), "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * ����ָ��ʱ���ָ��Сʱ��������ʱ�䡣
	 * <p>
	 * ��ʽ��yyyy-MM-dd HH:mm:ss
	 * 
	 * @return ʱ��.
	 */
	public static String getDateByAddHour(String datetime, int minute) {
		String returnTime = null;
		Calendar cal = new GregorianCalendar();
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date;
		try {
			date = ft.parse(datetime);
			cal.setTime(date);
			cal.add(GregorianCalendar.MINUTE, minute);
			returnTime = getFormatDateTime(cal.getTime(), "yyyy-MM-dd HH:mm:ss");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return returnTime;

	}

	/**
	 * ��ȡ����ʱ�䴮ʱ��Ĳ�ֵ����λΪСʱ
	 * 
	 * @param startTime
	 *            ��ʼʱ�� yyyy-MM-dd HH:mm:ss
	 * @param endTime
	 *            ����ʱ�� yyyy-MM-dd HH:mm:ss
	 * @return ����ʱ��Ĳ�ֵ(��)
	 */
	public static int getDiffHour(String startTime, String endTime) {
		long diff = 0;
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date startDate = ft.parse(startTime);
			Date endDate = ft.parse(endTime);
			diff = startDate.getTime() - endDate.getTime();
			diff = diff / (1000 * 60 * 60);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new Long(diff).intValue();
	}

	/**
	 * ������ݵ�������
	 * 
	 * @param selectName
	 *            ���������
	 * @param value
	 *            ��ǰ�������ֵ
	 * @param startYear
	 *            ��ʼ���
	 * @param endYear
	 *            �������
	 * @return ����������html
	 */
	public static String getYearSelect(String selectName, String value,
			int startYear, int endYear) {
		int start = startYear;
		int end = endYear;
		if (startYear > endYear) {
			start = endYear;
			end = startYear;
		}
		StringBuffer sb = new StringBuffer("");
		sb.append("<select name=\"" + selectName + "\">");
		for (int i = start; i <= end; i++) {
			if (!value.trim().equals("") && i == Integer.parseInt(value)) {
				sb.append("<option value=\"" + i + "\" selected>" + i
						+ "</option>");
			} else {
				sb.append("<option value=\"" + i + "\">" + i + "</option>");
			}
		}
		sb.append("</select>");
		return sb.toString();
	}

	/**
	 * ������ݵ�������
	 * 
	 * @param selectName
	 *            ���������
	 * @param value
	 *            ��ǰ�������ֵ
	 * @param startYear
	 *            ��ʼ���
	 * @param endYear
	 *            �������
	 *            ���翪ʼ���Ϊ2001�������Ϊ2005��ô������������ֵ����2001��2002
	 *            ��2003��2004��2005����
	 * @return ������ݵ��������html��
	 */
	public static String getYearSelect(String selectName, String value,
			int startYear, int endYear, boolean hasBlank) {
		int start = startYear;
		int end = endYear;
		if (startYear > endYear) {
			start = endYear;
			end = startYear;
		}
		StringBuffer sb = new StringBuffer("");
		sb.append("<select name=\"" + selectName + "\">");
		if (hasBlank) {
			sb.append("<option value=\"\"></option>");
		}
		for (int i = start; i <= end; i++) {
			if (!value.trim().equals("") && i == Integer.parseInt(value)) {
				sb.append("<option value=\"" + i + "\" selected>" + i
						+ "</option>");
			} else {
				sb.append("<option value=\"" + i + "\">" + i + "</option>");
			}
		}
		sb.append("</select>");
		return sb.toString();
	}

	/**
	 * ������ݵ�������
	 * 
	 * @param selectName
	 *            ���������
	 * @param value
	 *            ��ǰ�������ֵ
	 * @param startYear
	 *            ��ʼ���
	 * @param endYear
	 *            �������
	 * @param js
	 *            �����jsΪjs�ַ����� " onchange=\"changeYear()\" "
	 *            ,�����κ�js�ķ����Ϳ�����jspҳ���б�д���������롣
	 * @return ������ݵ�������
	 */
	public static String getYearSelect(String selectName, String value,
			int startYear, int endYear, boolean hasBlank, String js) {
		int start = startYear;
		int end = endYear;
		if (startYear > endYear) {
			start = endYear;
			end = startYear;
		}
		StringBuffer sb = new StringBuffer("");

		sb.append("<select name=\"" + selectName + "\" " + js + ">");
		if (hasBlank) {
			sb.append("<option value=\"\"></option>");
		}
		for (int i = start; i <= end; i++) {
			if (!value.trim().equals("") && i == Integer.parseInt(value)) {
				sb.append("<option value=\"" + i + "\" selected>" + i
						+ "</option>");
			} else {
				sb.append("<option value=\"" + i + "\">" + i + "</option>");
			}
		}
		sb.append("</select>");
		return sb.toString();
	}

	/**
	 * ������ݵ�������
	 * 
	 * @param selectName
	 *            ���������
	 * @param value
	 *            ��ǰ�������ֵ
	 * @param startYear
	 *            ��ʼ���
	 * @param endYear
	 *            �������
	 * @param js
	 *            �����jsΪjs�ַ����� " onchange=\"changeYear()\" "
	 *            ,�����κ�js�ķ����Ϳ�����jspҳ���б�д���������롣
	 * @return ������ݵ�������
	 */
	public static String getYearSelect(String selectName, String value,
			int startYear, int endYear, String js) {
		int start = startYear;
		int end = endYear;
		if (startYear > endYear) {
			start = endYear;
			end = startYear;
		}
		StringBuffer sb = new StringBuffer("");
		sb.append("<select name=\"" + selectName + "\" " + js + ">");
		for (int i = start; i <= end; i++) {
			if (!value.trim().equals("") && i == Integer.parseInt(value)) {
				sb.append("<option value=\"" + i + "\" selected>" + i
						+ "</option>");
			} else {
				sb.append("<option value=\"" + i + "\">" + i + "</option>");
			}
		}
		sb.append("</select>");
		return sb.toString();
	}

	/**
	 * ��ȡ�·ݵ�������
	 * 
	 * @param selectName
	 * @param value
	 * @param hasBlank
	 * @return �����·ݵ�������
	 */
	public static String getMonthSelect(String selectName, String value,
			boolean hasBlank) {
		StringBuffer sb = new StringBuffer("");
		sb.append("<select name=\"" + selectName + "\">");
		if (hasBlank) {
			sb.append("<option value=\"\"></option>");
		}
		for (int i = 1; i <= 12; i++) {
			if (!value.trim().equals("") && i == Integer.parseInt(value)) {
				sb.append("<option value=\"" + i + "\" selected>" + i
						+ "</option>");
			} else {
				sb.append("<option value=\"" + i + "\">" + i + "</option>");
			}
		}
		sb.append("</select>");
		return sb.toString();
	}

	/**
	 * ��ȡ�·ݵ�������
	 * 
	 * @param selectName
	 * @param value
	 * @param hasBlank
	 * @param js
	 * @return �����·ݵ�������
	 */
	public static String getMonthSelect(String selectName, String value,
			boolean hasBlank, String js) {
		StringBuffer sb = new StringBuffer("");
		sb.append("<select name=\"" + selectName + "\" " + js + ">");
		if (hasBlank) {
			sb.append("<option value=\"\"></option>");
		}
		for (int i = 1; i <= 12; i++) {
			if (!value.trim().equals("") && i == Integer.parseInt(value)) {
				sb.append("<option value=\"" + i + "\" selected>" + i
						+ "</option>");
			} else {
				sb.append("<option value=\"" + i + "\">" + i + "</option>");
			}
		}
		sb.append("</select>");
		return sb.toString();
	}

	/**
	 * ��ȡ���������Ĭ�ϵ�Ϊ1-31�� ע�⣺�˷������ܹ����·����������������
	 * 
	 * @param selectName
	 * @param value
	 * @param hasBlank
	 * @return ������������
	 */
	public static String getDaySelect(String selectName, String value,
			boolean hasBlank) {
		StringBuffer sb = new StringBuffer("");
		sb.append("<select name=\"" + selectName + "\">");
		if (hasBlank) {
			sb.append("<option value=\"\"></option>");
		}
		for (int i = 1; i <= 31; i++) {
			if (!value.trim().equals("") && i == Integer.parseInt(value)) {
				sb.append("<option value=\"" + i + "\" selected>" + i
						+ "</option>");
			} else {
				sb.append("<option value=\"" + i + "\">" + i + "</option>");
			}
		}
		sb.append("</select>");
		return sb.toString();
	}

	/**
	 * ��ȡ���������Ĭ�ϵ�Ϊ1-31
	 * 
	 * @param selectName
	 * @param value
	 * @param hasBlank
	 * @param js
	 * @return ��ȡ���������
	 */
	public static String getDaySelect(String selectName, String value,
			boolean hasBlank, String js) {
		StringBuffer sb = new StringBuffer("");
		sb.append("<select name=\"" + selectName + "\" " + js + ">");
		if (hasBlank) {
			sb.append("<option value=\"\"></option>");
		}
		for (int i = 1; i <= 31; i++) {
			if (!value.trim().equals("") && i == Integer.parseInt(value)) {
				sb.append("<option value=\"" + i + "\" selected>" + i
						+ "</option>");
			} else {
				sb.append("<option value=\"" + i + "\">" + i + "</option>");
			}
		}
		sb.append("</select>");
		return sb.toString();
	}

	/**
	 * ��������֮���ж��ٸ���ĩ�������ĩ��ָ������������죬һ����ĩ���ؽ��Ϊ2������Ϊ4���Դ���
	 * �ơ��� ���˷���Ŀǰ����ͳ��˾���ó���¼���� ע�⿪ʼ���ںͽ�������Ҫͳһ������
	 * 
	 * @param startDate
	 *            ��ʼ���� ����ʽ"yyyy/MM/dd" ����"yyyy-MM-dd"
	 * @param endDate
	 *            �������� ����ʽ"yyyy/MM/dd"����"yyyy-MM-dd"
	 * @return int
	 */
	public static int countWeekend(String startDate, String endDate) {
		int result = 0;
		Date sdate = null;
		Date edate = null;
		if (startDate.indexOf("/") > 0 && endDate.indexOf("/") > 0) {
			sdate = getDateObj(startDate, "/"); // ��ʼ����
			edate = getDateObj(endDate, "/");// ��������
		}
		if (startDate.indexOf("-") > 0 && endDate.indexOf("-") > 0) {
			sdate = getDateObj(startDate, "-"); // ��ʼ����
			edate = getDateObj(endDate, "-");// ��������
		}

		// ���ȼ����������Щ���ڣ�Ȼ���ҳ������������������
		int sumDays = Math.abs(getDiffDays(startDate, endDate));
		int dayOfWeek = 0;
		for (int i = 0; i <= sumDays; i++) {
			dayOfWeek = getDayOfWeek(getDateAdd(sdate, i)); // ����ÿ��һ�������
			if (dayOfWeek == 1 || dayOfWeek == 7) { // 1 ������ 7������
				result++;
			}
		}
		return result;
	}

	/**
	 * ������������֮���������졣 ע�⿪ʼ���ںͽ�������Ҫͳһ������
	 * 
	 * @param startDate
	 *            ��ʽ"yyyy/MM/dd" ����"yyyy-MM-dd"
	 * @param endDate
	 *            ��ʽ"yyyy/MM/dd" ����"yyyy-MM-dd"
	 * @return ����
	 */
	public static int getDiffDays(String startDate, String endDate) {
		long diff = 0;
		SimpleDateFormat ft = null;
		if (startDate.indexOf("/") > 0 && endDate.indexOf("/") > 0) {
			ft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		}
		if (startDate.indexOf("-") > 0 && endDate.indexOf("-") > 0) {
			ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		try {
			Date sDate = ft.parse(startDate + " 00:00:00");
			Date eDate = ft.parse(endDate + " 00:00:00");
			diff = eDate.getTime() - sDate.getTime();
			diff = diff / 86400000;// 1000*60*60*24;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return (int) diff;

	}

	/**
	 * ������������֮�����ϸ��������(������ʼ���ںͽ�������)�� ���磺2007/07/01
	 * ��2007/07/03 ,��ô�������� {"2007/07/01","2007/07/02","2007/07/03"}
	 * ע�⿪ʼ���ںͽ�������Ҫͳһ������
	 * 
	 * @param startDate
	 *            ��ʽ"yyyy/MM/dd"���� yyyy-MM-dd
	 * @param endDate
	 *            ��ʽ"yyyy/MM/dd"���� yyyy-MM-dd
	 * @return ����һ���ַ��������
	 */
	public static String[] getArrayDiffDays(String startDate, String endDate) {
		int LEN = 0; // ������������֮���ܹ��ж�����
		// ���������ںͿ�ʼ������ͬ
		if (startDate.equals(endDate)) {
			return new String[] { startDate };
		}
		Date sdate = null;
		if (startDate.indexOf("/") > 0 && endDate.indexOf("/") > 0) {
			sdate = getDateObj(startDate, "/"); // ��ʼ����
		}
		if (startDate.indexOf("-") > 0 && endDate.indexOf("-") > 0) {
			sdate = getDateObj(startDate, "-"); // ��ʼ����
		}

		LEN = getDiffDays(startDate, endDate);
		String[] dateResult = new String[LEN + 1];
		dateResult[0] = startDate;
		for (int i = 1; i < LEN + 1; i++) {
			if (startDate.indexOf("/") > 0 && endDate.indexOf("/") > 0) {
				dateResult[i] = getFormatDateTime(getDateAdd(sdate, i),
						"yyyy/MM/dd");
			}
			if (startDate.indexOf("-") > 0 && endDate.indexOf("-") > 0) {
				dateResult[i] = getFormatDateTime(getDateAdd(sdate, i),
						"yyyy-MM-dd");
			}
		}

		return dateResult;
	}

	/**
	 * �ж�һ�������Ƿ��ڿ�ʼ���ںͽ�������֮�䡣
	 * 
	 * @param srcDate
	 *            Ŀ������ yyyy/MM/dd ���� yyyy-MM-dd
	 * @param startDate
	 *            ��ʼ���� yyyy/MM/dd ���� yyyy-MM-dd
	 * @param endDate
	 *            �������� yyyy/MM/dd ���� yyyy-MM-dd
	 * @return ���ڵ��ڿ�ʼ����С�ڵ��ڽ������ڣ���ô����true�����򷵻�false
	 */
	public static boolean isInStartEnd(String srcDate, String startDate,
			String endDate) {
		if (startDate.compareTo(srcDate) <= 0
				&& endDate.compareTo(srcDate) >= 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * ��ȡ���������Ĭ�ϵ�Ϊ1-4�� ע�⣺�˷������ܹ����·����������������
	 * 
	 * @param selectName
	 * @param value
	 * @param hasBlank
	 * @return ��ü��ȵ�������
	 */
	public static String getQuarterSelect(String selectName, String value,
			boolean hasBlank) {
		StringBuffer sb = new StringBuffer("");
		sb.append("<select name=\"" + selectName + "\">");
		if (hasBlank) {
			sb.append("<option value=\"\"></option>");
		}
		for (int i = 1; i <= 4; i++) {
			if (!value.trim().equals("") && i == Integer.parseInt(value)) {
				sb.append("<option value=\"" + i + "\" selected>" + i
						+ "</option>");
			} else {
				sb.append("<option value=\"" + i + "\">" + i + "</option>");
			}
		}
		sb.append("</select>");
		return sb.toString();
	}

	/**
	 * ��ȡ���ȵ�������Ĭ�ϵ�Ϊ1-4
	 * 
	 * @param selectName
	 * @param value
	 * @param hasBlank
	 * @param js
	 * @return ��ȡ���ȵ�������
	 */
	public static String getQuarterSelect(String selectName, String value,
			boolean hasBlank, String js) {
		StringBuffer sb = new StringBuffer("");
		sb.append("<select name=\"" + selectName + "\" " + js + ">");
		if (hasBlank) {
			sb.append("<option value=\"\"></option>");
		}
		for (int i = 1; i <= 4; i++) {
			if (!value.trim().equals("") && i == Integer.parseInt(value)) {
				sb.append("<option value=\"" + i + "\" selected>" + i
						+ "</option>");
			} else {
				sb.append("<option value=\"" + i + "\">" + i + "</option>");
			}
		}
		sb.append("</select>");
		return sb.toString();
	}

	/**
	 * ����ʽΪyyyy����yyyy.MM����yyyy.MM.dd������ת��Ϊyyyy/MM/dd�ĸ�ʽ��λ����ģ�����
	 * 01��<br>
	 * ����.1999 = 1999/01/01 ���磺1989.02=1989/02/01
	 * 
	 * @param argDate
	 *            ��Ҫ����ת�������ڡ���ʽ����Ϊyyyy����yyyy.MM����yyyy.MM.dd
	 * @return ���ظ�ʽΪyyyy/MM/dd���ַ�
	 */
	public static String changeDate(String argDate) {
		if (argDate == null || argDate.trim().equals("")) {
			return "";
		}
		String result = "";
		// ����Ǹ�ʽΪyyyy/MM/dd�ľ�ֱ�ӷ���
		if (argDate.length() == 10 && argDate.indexOf("/") > 0) {
			return argDate;
		}
		String[] str = argDate.split("[.]"); // .�Ƚ�����
		int LEN = str.length;
		for (int i = 0; i < LEN; i++) {
			if (str[i].length() == 1) {
				if (str[i].equals("0")) {
					str[i] = "01";
				} else {
					str[i] = "0" + str[i];
				}
			}
		}
		if (LEN == 1) {
			result = argDate + "/01/01";
		}
		if (LEN == 2) {
			result = str[0] + "/" + str[1] + "/01";
		}
		if (LEN == 3) {
			result = str[0] + "/" + str[1] + "/" + str[2];
		}
		return result;
	}

	/**
	 * ����ʽΪyyyy����yyyy.MM����yyyy.MM.dd������ת��Ϊyyyy/MM/dd�ĸ�ʽ��λ����ģ�����
	 * 01��<br>
	 * ����.1999 = 1999/01/01 ���磺1989.02=1989/02/01
	 * 
	 * @param argDate
	 *            ��Ҫ����ת�������ڡ���ʽ����Ϊyyyy����yyyy.MM����yyyy.MM.dd
	 * @return ���ظ�ʽΪyyyy/MM/dd���ַ�
	 */
	public static String changeDateWithSplit(String argDate, String split) {
		if (argDate == null || argDate.trim().equals("")) {
			return "";
		}
		if (split == null || split.trim().equals("")) {
			split = "-";
		}
		String result = "";
		// ����Ǹ�ʽΪyyyy/MM/dd�ľ�ֱ�ӷ���
		if (argDate.length() == 10 && argDate.indexOf("/") > 0) {
			return argDate;
		}
		// ����Ǹ�ʽΪyyyy-MM-dd�ľ�ֱ�ӷ���
		if (argDate.length() == 10 && argDate.indexOf("-") > 0) {
			return argDate;
		}
		String[] str = argDate.split("[.]"); // .�Ƚ�����
		int LEN = str.length;
		for (int i = 0; i < LEN; i++) {
			if (str[i].length() == 1) {
				if (str[i].equals("0")) {
					str[i] = "01";
				} else {
					str[i] = "0" + str[i];
				}
			}
		}
		if (LEN == 1) {
			result = argDate + split + "01" + split + "01";
		}
		if (LEN == 2) {
			result = str[0] + split + str[1] + split + "01";
		}
		if (LEN == 3) {
			result = str[0] + split + str[1] + split + str[2];
		}
		return result;
	}

	/**
	 * ����ָ�����ڵĵ���һ���µ�����
	 * 
	 * @param argDate
	 *            ��ʽΪyyyy-MM-dd����yyyy/MM/dd
	 * @return ��һ���µ�����
	 */
	public static int getNextMonthDays(String argDate) {
		String[] temp = null;
		if (argDate.indexOf("/") > 0) {
			temp = argDate.split("/");
		}
		if (argDate.indexOf("-") > 0) {
			temp = argDate.split("-");
		}
		Calendar cal = new GregorianCalendar(new Integer(temp[0]).intValue(),
				new Integer(temp[1]).intValue() - 1,
				new Integer(temp[2]).intValue());
		int curMonth = cal.get(Calendar.MONTH);
		cal.set(Calendar.MONTH, curMonth + 1);

		int curyear = cal.get(Calendar.YEAR);// ��ǰ���
		curMonth = cal.get(Calendar.MONTH);// ��ǰ�·�,0-11

		int mArray[] = new int[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30,
				31 };
		// �ж��������� ��2�·���29�죻
		if ((curyear % 400 == 0)
				|| ((curyear % 100 != 0) && (curyear % 4 == 0))) {
			mArray[1] = 29;
		}
		return mArray[curMonth];
	}

	public static void main(String[] args) {
		System.out.println(DateUtil.getCurrentDateTime());
		System.out.println("first=" + changeDateWithSplit("2000.1", ""));
		System.out.println("second=" + changeDateWithSplit("2000.1", "/"));
		String[] t = getArrayDiffDays("2008/02/15", "2008/02/19");
		for (int i = 0; i < t.length; i++) {
			System.out.println(t[i]);
		}
		t = getArrayDiffDays("2008-02-15", "2008-02-19");
		for (int i = 0; i < t.length; i++) {
			System.out.println(t[i]);
		}
		System.out.println(getNextMonthDays("2008/02/15") + "||"
				+ getCurrentMonth() + "||" + DateUtil.changeDate("1999"));
		System.out.println(DateUtil.changeDate("1999.1"));
		System.out.println(DateUtil.changeDate("1999.11"));
		System.out.println(DateUtil.changeDate("1999.1.2"));
		System.out.println(DateUtil.changeDate("1999.11.12"));
	}

	/**
	 * 返回指定时间戳的 日期
	 * 
	 * @param milis
	 * @return
	 */
	public static String TimeMilisToDate(String milis) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return (sdf.format(Long.valueOf(milis)));
	}

	/**
	 * 比较两个日期的大小 "1999-12-11 09:59"在"1995-11-12 15:21"之前,返回1;否则返回-1
	 * 
	 * @param DATE1
	 * @param DATE2
	 * @return
	 */
	public static int compareDate(Date date1, Date date2) {

		try {
			if (date1.getTime() > date2.getTime()) {
				return 1;
			} else if (date1.getTime() < date2.getTime()) {
				return -1;
			} else {
				return 0;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0;
	}

}