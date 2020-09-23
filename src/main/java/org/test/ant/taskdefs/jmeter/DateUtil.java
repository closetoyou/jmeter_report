package org.test.ant.taskdefs.jmeter;



import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static String getCurrentTime()
    {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(new Date());
    }

    public static String getCurrentDate()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(new Date());
    }

    public static String getDate(String format)
    {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(new Date());
    }


}

