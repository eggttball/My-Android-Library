package com.eggttball.library;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 集合常用的日期時間相關方法，例如常用日期時間格式的解析與格式化
 * Created by eggttball on 2015/1/10.
 */
public class DateTimeUtility {

    private static final String _defaultFormatString = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String _slashFormatString = "yyyy/MM/dd HH:mm:ss";

    private static SimpleDateFormat _defaultFormat;	// 後台傳來的預設日期時間格式
    private static SimpleDateFormat _slashFormat;


    public TimeZone getUTCTimeZone()    {
        return TimeZone.getTimeZone("GMT+0");
    }


    /**
     * 範例：2013-09-16T14:10:23	(GMT+0)
     */
    public synchronized SimpleDateFormat getDefaultFormat()	{
        if (_defaultFormat == null) {
            _defaultFormat = new SimpleDateFormat(_defaultFormatString, Locale.getDefault());
            _defaultFormat.setTimeZone(getUTCTimeZone());
        }

        return _defaultFormat;
    }


    /**
     * 範例：2013-09-16T14:10:23	(自訂時區)
     */
    public SimpleDateFormat getDefaultFormat(TimeZone timeZone)	{
        if (timeZone.equals(getUTCTimeZone()))
            return getDefaultFormat();
        else {
            SimpleDateFormat sdf = new SimpleDateFormat(_defaultFormatString, Locale.getDefault());
            sdf.setTimeZone(timeZone);
            return sdf;
        }
    }


    /**
     * 範例：2013/09/16 14:10:23	(GMT+0)
     */
    public synchronized SimpleDateFormat getSlashFormat()	{
        if (_slashFormat == null)   {
            _slashFormat = new SimpleDateFormat(_slashFormatString, Locale.getDefault());
            _slashFormat.setTimeZone(getUTCTimeZone());
        }

        return _slashFormat;
    }


    /**
     * 範例：2013/09/16 14:10:23	(自訂時區)
     */
    public SimpleDateFormat getSlashFormat(TimeZone timeZone)	{
        if (timeZone.equals(getUTCTimeZone()))
            return getSlashFormat();
        else {
            SimpleDateFormat sdf = new SimpleDateFormat(_slashFormatString, Locale.getDefault());
            sdf.setTimeZone(timeZone);
            return sdf;
        }
    }

}
