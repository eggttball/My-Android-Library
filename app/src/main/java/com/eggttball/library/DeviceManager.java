package com.eggttball.library;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * 集合與 Device 相關的常用方法，例如取得設備的唯一 ID。
 * Created by eggttball on 2015/1/2.
 */
public final class DeviceManager {

    private Context _context;

    protected static final String SETTING_DEVICE_ID = "device_id";

    protected static UUID _deviceId;
    // 這是一個 Android_ID 的 BUG，很多手機會得到這樣一模一樣的 ID
    private static String ANDROID_ID_BUG = "9774d56d682e549c";


    public DeviceManager(Context context) {
        _context = context;
    }


    /**
     * 取得設備的唯一 ID，意即不管重複安裝幾次，安裝了多少個 App，都會得到同樣的 ID。<br />
     * 但是使用 DeviceID 來協助識別使用者，在某些情況下會有問題。<br />
     * 例如某人安裝了 App，後來將手機 reset 並轉贈他人，之後又安裝了同一款 App，那麼應該要識別為不同的使用者。<br />
     * 解決方式可參考 {@link com.eggttball.library.Installation}
     * 取得 ID 的程序如下：
     * <ol>
     * 	<li>先從 SharedPreferences 判別是否曾把 ID 儲存起來，若有，直接讀取。 </li>
     * 	<li>讀取 ANDROID_ID：存在一些 BUG，且 Android 2.2 版本不可靠。若手機 Reset 此值也會改變</li>
     * 	<li>讀取 android.os.Build.SERIAL，但這是 API Level9 以後才有，且也不是所有設備都支援</li>
     * 	<li>亂數生成的 UUID (但這只能識別此安裝，不能識別此設備)</li>
     * </ol>
     * 因為無法保證前面幾個步驟絕對無誤，所以此值最後也無法保證能唯一識別設備，但應足以涵蓋絕大部分情況！
     * 另外有些方法是不建議的，例如讀取 Wi-Fi 的 MAC Address 當作 DeviceID，因為 Wi-Fi 關閉時可能無法取得。
     * 而使用 IMEI/IMSI 當作 DeviceID 雖然很準確，但需要額外權限 READ_PHONE_STATE。
     * <pre>
     * 參考：http://luhuajcdd.iteye.com/blog/1608746
     * 參考：http://developer.samsung.com/android/technical-docs/How-to-retrieve-the-Device-Unique-ID-from-android-device
     * 參考：http://android-developers.blogspot.tw/2011/03/identifying-app-installations.html
     * </pre>
     */
    public UUID getDeviceId() {

        if (_deviceId == null) synchronized (getClass()) {

            if (_deviceId == null) {
                // 步驟 1. --- 先試著從 SharedPreferences 讀取之前算出來的值
                final SharedPreferences prefs = _context.getSharedPreferences(Settings.FILE_NAME, 0);
                final String id = prefs.getString(SETTING_DEVICE_ID, null);

                if (id != null) {
                    _deviceId = UUID.fromString(id);
                    return _deviceId;
                }


                // 步驟 2. --- 依序讀取 ANDROID_ID、Serial Number、Random UUID
                _deviceId = formatUUID(getAndroid_Id(_context));
                if (_deviceId == null)  _deviceId = formatUUID(getSerialNo());
                if (_deviceId == null)  _deviceId = UUID.randomUUID();

                if (_deviceId != null)  saveDeviceId(_context);
            }
        }

        return _deviceId;
    }


    /**
     * 取得 Unique number (IMEI, MEID, ESN, IMSI)
     * 必要權限 READ_PHONE_STATE
     */
    private String getTelephonyDeviceId(Context context)    {
        TelephonyManager tm = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        String deviceId = tm.getDeviceId();     // IMEI
        if (deviceId == null)
            deviceId = tm.getSubscriberId();    // IMSI

        return deviceId;
    }


    /**
     * 取得 ANDROID_ID
     * @return  以 UUID 表示的 ANDROID_ID，若失敗則為 null
     */
    private String getAndroid_Id(Context context)   {
        final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        if (androidId == null || androidId.equals(ANDROID_ID_BUG))
            return null;
        else
            return androidId;
    }


    /**
     * 儲存 DeviceId，以便之後可直接取得。
     */
    private void saveDeviceId(Context context)	{
        final SharedPreferences prefs = context.getSharedPreferences(Settings.FILE_NAME, 0);
        prefs.edit().putString(SETTING_DEVICE_ID, _deviceId.toString()).commit();
    }


    /**
     * 取得設備的 Serial Number，如果是 API Level9 或以上，直接讀取 android.os.Build.SERIAL 即可
     */
    private String getSerialNo()	{
//		try {
//			Class<?> c = Class.forName("android.os.SystemProperties");
//			Method get = c.getMethod("get", String.class, String.class );
//			serialnum = (String)(   get.invoke(c, "ro.serialno")  );
//		}
//		catch (Exception ignored)	{	}
//
//        return serialnum;
        return android.os.Build.SERIAL;
    }


    /**
     * 將字串轉換為 UUID 表示
     */
    private UUID formatUUID(String value){
        if (value == null || value.equals(""))
            return null;

        try {
            return UUID.nameUUIDFromBytes(value.getBytes("utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
