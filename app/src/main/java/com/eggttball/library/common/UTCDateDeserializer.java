package com.eggttball.library.common;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

import com.eggttball.library.DateTimeUtility;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * 將日期格式 2013-09-16T14:10:23 反序列化為 Date 型別，但時區為 UTC
 * 會有這個類別是因為 Gson 的日期反序列化無法指定時區
 * Created by eggttball on 2015/1/11.
 */
public class UTCDateDeserializer implements JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
        String date = element.getAsString();

        DateTimeUtility utility = new DateTimeUtility();
        SimpleDateFormat formatter = utility.getDefaultFormat();

        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            Log.e(UTCDateDeserializer.class.getCanonicalName(), "Failed to parse Date due to:" + e);
            return null;
        }
    }
}