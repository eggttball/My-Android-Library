package com.eggttball.library.samsung;

import android.os.Build;
import java.lang.reflect.Field;

/**
 * Samsung 手機專用的工具類，例如判斷 Spen 機型
 * Created by eggttball on 2015/1/11.
 */
public class SamsungUtility {

    /**
     * 判斷是否為擁有 Spen 的機型？
     */
    public boolean isSpenAvailable()	{
        // 以下分別為 Note1, Note2(雙型號), Note 10.1, GALAXY Note 3 4G LTE(2-serial), Note 3(2-serial), Note 10.1 wi-fi, Note 8", Note 8" wi-fi, Note2 cdma亞太雙卡版, Note2 wcdma 3g雙卡版
        String[] spenDevices = { "GT-N7000", "GT-7105", "GT-N7100","SM-N9005","GT-N9005","SM-N900" ,"GT-N9000", "GT-N8000", "GT-N8010", "GT-N5100", "GT-N5110", "GT-N719", "GT-N7102" };
        Field[] fields = Build.class.getDeclaredFields();
        String device = "";

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (field.getName().equals("MODEL"))    {
                    device = field.get(null).toString();
                    break;
                }

            } catch (Exception e) {	}
        }

        for (String d : spenDevices) {
            if (d.equalsIgnoreCase(device))	return true;
        }

        return false;
    }

}
