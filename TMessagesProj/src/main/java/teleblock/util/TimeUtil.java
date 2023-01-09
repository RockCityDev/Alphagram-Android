package teleblock.util;

import android.text.TextUtils;

import com.blankj.utilcode.util.TimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeUtil {

    public static String setSecond2Minute(int i) {
        if (i < 10) {
            return "00:0" + i;
        } else if (i < 60) {
            return "00:" + i;
        } else if (i < 3600) {
            int i2 = i / 60;
            int i3 = i % 60;
            if (i2 < 10) {
                if (i3 < 10) {
                    return "0" + i2 + ":0" + i3;
                }
                return "0" + i2 + ":" + i3;
            } else if (i3 < 10) {
                return i2 + ":0" + i3;
            } else {
                return i2 + ":" + i3;
            }
        } else {
            int i4 = i / 3600;
            int i5 = (i % 3600) / 60;
            int i6 = (i - (i4 * 3600)) - (i5 * 60);
            if (i4 < 10) {
                if (i5 < 10) {
                    if (i6 < 10) {
                        return "0" + i4 + ":0" + i5 + ":0" + i6;
                    }
                    return "0" + i4 + ":0" + i5 + ":" + i6;
                } else if (i6 < 10) {
                    return "0" + i4 + i5 + ":0" + i6;
                } else {
                    return "0" + i4 + i5 + ":" + i6;
                }
            } else if (i5 < 10) {
                if (i6 < 10) {
                    return i4 + ":0" + i5 + ":0" + i6;
                }
                return i4 + ":0" + i5 + ":" + i6;
            } else if (i6 < 10) {
                return (i4 + i5) + ":0" + i6;
            } else {
                return (i4 + i5) + ":" + i6;
            }
        }
    }

    public static int getNowHour() {
        return Calendar.getInstance().get(11);
    }

    public static String getSFormatHM2(long j) {
        long j2 = j / 1000;
        int i = (int) ((j2 % 3600) / 60);
        int i2 = (int) (j2 / 3600);
        if (i2 != 0) {
            return String.format("%02d小时%02d分钟", new Object[]{Integer.valueOf(i2), Integer.valueOf(i)});
        } else if (i <= 0) {
            return "<1分钟";
        } else {
            return String.format("%d分钟", new Object[]{Integer.valueOf(i)});
        }
    }

    public static String getSFormatHM(long j) {
        long j2 = j / 1000;
        int i = (int) ((j2 % 3600) / 60);
        int i2 = (int) (j2 / 3600);
        if (i2 != 0) {
            return String.format("%02d时%02d分", new Object[]{Integer.valueOf(i2), Integer.valueOf(i)});
        } else if (i <= 0) {
            return "<1分钟";
        } else {
            return String.format("%d分钟", new Object[]{Integer.valueOf(i)});
        }
    }

    public static int getSFormatH(long j) {
        return (int) (j / 3600);
    }

    public static int getSFormatM(long j) {
        return (int) ((j % 3600) / 60);
    }

    public static String getFormatMS(long second) {
        return String.format("%02d:%02d", new Object[]{Long.valueOf(second / 60), Long.valueOf(second % 60)});
    }

    public static String getTimestamp2String(long j) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(j));
        } catch (NumberFormatException unused) {
            return "";
        }
    }

    public static String getDate2String(long j) {
        try {
            return new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date(j));
        } catch (NumberFormatException unused) {
            return "";
        }
    }

    public static String getNowTimestamp2ShortY() {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
        } catch (NumberFormatException unused) {
            return "";
        }
    }

    public static String getNowTimesHM() {
        try {
            return new SimpleDateFormat("MM-dd HH:mm").format(new Date(System.currentTimeMillis()));
        } catch (NumberFormatException unused) {
            return "";
        }
    }

    public static String getNowTimesHour() {
        try {
            return new SimpleDateFormat("HH:mm").format(new Date(System.currentTimeMillis()));
        } catch (NumberFormatException unused) {
            return "";
        }
    }

    public static String getTimestamp2ShortStr(Date date) {
        try {
            String[] split = new SimpleDateFormat("MM-dd").format(date).split("-");
            if (split.length != 2) {
                return "";
            }
            return Integer.parseInt(split[0]) + "月" + Integer.parseInt(split[1]) + "日";
        } catch (NumberFormatException unused) {
            return "";
        }
    }

    public static String getDate2Str(Date date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        } catch (NumberFormatException unused) {
            return "";
        }
    }

    public static String getDate2ShortStr(Date date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
        } catch (NumberFormatException unused) {
            return "";
        }
    }

    public static String getDate2ShortMStr(Date date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").format(date);
        } catch (NumberFormatException unused) {
            return "";
        }
    }

    public static String getDate2ShortHmStr(Date date) {
        try {
            return new SimpleDateFormat("HH:mm").format(date);
        } catch (NumberFormatException unused) {
            return "";
        }
    }

    public static String getWeek(long j) {
        Date date = new Date(j);
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        return new SimpleDateFormat("EEEE").format(instance.getTime());
    }

    public static int getTodayWeekInt() {
        Date date = new Date();
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        int i = instance.get(Calendar.DAY_OF_WEEK);
        if (i == Calendar.SUNDAY) {
            return 7;
        }
        return i - 1;
    }

    public static int getWeekInt(long j) {
        Date date = new Date(j);
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        return instance.get(Calendar.DAY_OF_WEEK);
    }


    public static String getWeekTime(String time) {
        if (TextUtils.isEmpty(time))
            return "";
        String[] strings = time.split(",");
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : strings) {
            if (s.equals("1")) stringBuilder.append("周一 ");
            if (s.equals("2")) stringBuilder.append("周二 ");
            if (s.equals("3")) stringBuilder.append("周三 ");
            if (s.equals("4")) stringBuilder.append("周四 ");
            if (s.equals("5")) stringBuilder.append("周五 ");
            if (s.equals("6")) stringBuilder.append("周六 ");
            if (s.equals("7")) stringBuilder.append("周日 ");
        }
        if (strings.length == 7) stringBuilder = new StringBuilder("每日 ");
        return stringBuilder.toString().substring(0, stringBuilder.length() - 1);
    }

    public static String getDayTime() {
        int i = Calendar.getInstance().get(11);
        if (i >= 5 && i <= 7) {
            return "早上好";
        }
        if (i >= 8 && i <= 10) {
            return "上午好";
        }
        if (i >= 11 && i <= 12) {
            return "中午好";
        }
        if (i < 13 || i > 16) {
            return (i < 17 || i > 18) ? "晚上好" : "傍晚好";
        }
        return "下午好";
    }

    public static long getTimerShort(String str) {
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            date = null;
        }
        return date.getTime();
    }

    public static long getTimer(String str) {
        if (str.isEmpty()) {
            return 0;
        }
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    public static Date getTimeDate(String pattern, String str) {
        if (str.isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat(pattern).parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Date getTimeDate(String str) {
        if (str.isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Date getTimeYDate(String str) {
        if (str.isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Date getTimeHDate(String str) {
        if (str.isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 当前月份
     *
     * @param date
     * @return
     */
    public static String getCurrentMonth(Date date) {
        try {
            return new SimpleDateFormat("yyyy-MM").format(date);
        } catch (NumberFormatException unused) {
            return "";
        }
    }

    /**
     * 获取当前年
     *
     * @param date
     * @return
     */
    public static String getCurrentYear(Date date) {
        try {
            return new SimpleDateFormat("yyyy").format(date);
        } catch (NumberFormatException unused) {
            return "";
        }
    }


    /**
     * 获取日期 月日 0908格式
     *
     * @param date
     * @return
     */
    public static String getCurrentDate(Date date) {
        try {
            return new SimpleDateFormat("MMdd").format(date);
        } catch (NumberFormatException unused) {
            return "";
        }
    }

    /**
     * 获取一周前的日期
     *
     * @param date
     * @return
     */
    public static Date getWeekData(Date date) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, -7);
        Date daysBeforeDate = cal.getTime();
        return daysBeforeDate;
    }

    /**
     * 当前月 +1 -1
     *
     * @param date
     * @return
     */
    public static Date beforeMonth(Date date, int number) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, number);
        Date time = calendar.getTime();
        return time;
    }

    /**
     * 当前年 +1 -1
     *
     * @param date
     * @return
     */
    public static Date beforeYear(Date date, int number) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, number);
        Date time = calendar.getTime();
        return time;
    }

    /**
     * 当前日期+1 -1
     *
     * @param date
     * @param number
     * @return
     */
    public static Date beforeDay(Date date, int number) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, number);
        Date time = calendar.getTime();
        return time;
    }

    /**
     * 得到本周周一
     *
     * @return
     */
    public static Date getMondayOfThisWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int day_of_week = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (day_of_week == 0)
            day_of_week = 7;
        c.add(Calendar.DATE, -day_of_week + 1);
        Date time = c.getTime();
        return time;

    }


    /**
     * 获得两个日期间距多少天
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long getTimeDistance(Date beginDate, Date endDate) {
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(beginDate);
        fromCalendar.set(Calendar.HOUR_OF_DAY, fromCalendar.getMinimum(Calendar.HOUR_OF_DAY));
        fromCalendar.set(Calendar.MINUTE, fromCalendar.getMinimum(Calendar.MINUTE));
        fromCalendar.set(Calendar.SECOND, fromCalendar.getMinimum(Calendar.SECOND));
        fromCalendar.set(Calendar.MILLISECOND, fromCalendar.getMinimum(Calendar.MILLISECOND));

        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(endDate);
        toCalendar.set(Calendar.HOUR_OF_DAY, fromCalendar.getMinimum(Calendar.HOUR_OF_DAY));
        toCalendar.set(Calendar.MINUTE, fromCalendar.getMinimum(Calendar.MINUTE));
        toCalendar.set(Calendar.SECOND, fromCalendar.getMinimum(Calendar.SECOND));
        toCalendar.set(Calendar.MILLISECOND, fromCalendar.getMinimum(Calendar.MILLISECOND));

        long dayDistance = (toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / 30;
        dayDistance = Math.abs(dayDistance);

        return dayDistance;
    }

    /**
     * 获取两个日期的周数差
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static long getDifferWeek(Date startTime, Date endTime) {
        Date startFirstDayOfWeek = getMondayOfThisWeek(startTime); // 获取这天所在的周的第一天
        Date endFirstDayOfWeek = getMondayOfThisWeek(endTime);

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startFirstDayOfWeek);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endFirstDayOfWeek);

        long dayOffset = getTimeDistance(startFirstDayOfWeek, endFirstDayOfWeek);

        long weekOffset = dayOffset / 7;
        return weekOffset;
    }

    /**
     * 获取两个日期的月数差
     *
     * @param fromDate
     * @param toDate
     * @return
     */
    public static int getDifferMonth(Date fromDate, Date toDate) {
        Calendar fromDateCal = Calendar.getInstance();
        Calendar toDateCal = Calendar.getInstance();
        fromDateCal.setTime(fromDate);
        toDateCal.setTime(toDate);

        int fromYear = fromDateCal.get(Calendar.YEAR);
        int toYear = toDateCal.get((Calendar.YEAR));
        if (fromYear == toYear) {
            return Math.abs(fromDateCal.get(Calendar.MONTH) - toDateCal.get(Calendar.MONTH));
        } else {
            int fromMonth = 12 - (fromDateCal.get(Calendar.MONTH) + 1);
            int toMonth = toDateCal.get(Calendar.MONTH) + 1;
            return Math.abs(toYear - fromYear - 1) * 12 + fromMonth + toMonth;
        }
    }

    public static String dateToString(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日");
        String df = sdf.format(date);
        return df;
    }

    /**
     * 获取当前日期是星期几<br>
     *
     * @param dt
     * @return 当前日期是星期几
     */
    public static String getWeekOfDate(Date dt) {
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

    /***
     * 获取12小时制时间
     * @param time
     * @return
     */
    public static String gethhDate(long time) {
        StringBuffer timeStr = new StringBuffer();
        if (TimeUtils.isAm(time)) {
            timeStr.append("AM");
        } else {
            timeStr.append("PM");
        }
        timeStr.append(TimeUtils.millis2String(time, "h:mm"));
        return timeStr.toString();
    }

}
