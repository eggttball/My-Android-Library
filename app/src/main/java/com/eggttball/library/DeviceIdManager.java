package com.eggttball.library;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * 這個類別是為了取得設備的唯一 ID，意即不管重複安裝幾次，安裝了多少個 app，都會得到同樣的 ID。<br />
 * 需要 user-permission : READ_PHONE_STATE 和 ACCESS_WIFI_STATE <br />
 * 取得 ID 的程序如下：
 * <ol>
 * 	<li>先從 SharedPreferences 判別是否曾把 ID 儲存起來，若有，直接讀取。 </li>
 * 	<li>讀取 Device ID：必須是有通話功能的設備 (例如 wi-fi 平板將得到 null)，且少數手機也有 BUG</li>
 * 	<li>讀取 android.os.Build.SERIAL，但這是 API Level9 才有，且也不是所有設備都支援</li>
 * 	<li>讀取 WI-FI 的 MAC Address</li>
 * 	<li>讀取 ANDROID_ID：存在一些 BUG，且 Android 2.2 版本不可靠。若手機 Reset 此值也會改變</li>
 * 	<li>安裝 app 時亂數生成的 UUID</li>
 * </ol>
 * 因為無法保證前面幾個步驟絕對無誤，所以此值最後也無法保證能唯一識別設備，但應足以涵蓋絕大部分情況！
 * <pre>
 * 參考：http://luhuajcdd.iteye.com/blog/1608746
 * 參考：http://developer.samsung.com/android/technical-docs/How-to-retrieve-the-Device-Unique-ID-from-android-device
 * </pre>
 * Created by eggttball on 2015/1/2.
 */
public class DeviceIdManager {

    protected static final String SETTING_DEVICE_ID = "device_id";

    protected static UUID _deviceId;
    // 這是一個 Android_ID 的 BUG，很多手機會得到這樣一模一樣的 ID
    private static String ANDROID_ID_BUG = "9774d56d682e549c";


    public DeviceIdManager(Context context) {

        if (_deviceId == null) synchronized (getClass()) {
            if (_deviceId == null) {
                // 步驟 1. --- 先試著從 SharedPreferences 讀取之前算出來的值
                final SharedPreferences prefs = context.getSharedPreferences(Settings.FILE_NAME, 0);
                final String id = prefs.getString(SETTING_DEVICE_ID, null);

                if (id != null) {
                    _deviceId = UUID.fromString(id);
                    return;
                }


                // 步驟 2. --- 取得 Device ID
                TelephonyManager tm = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
                String deviceId = tm.getDeviceId();
                if (deviceId == null)
                    deviceId = tm.getSubscriberId();
                if (deviceId != null) {
                    try {
                        _deviceId = UUID.nameUUIDFromBytes(deviceId.getBytes("utf8"));
                        saveUUID(context);
                        return;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }


                // 步驟 3. --- 取得 android.os.Build.SERIAL
                String serialno = getSerialNo();
                if (serialno != null) {
                    try {
                        _deviceId = UUID.nameUUIDFromBytes(serialno.getBytes("utf8"));
                        saveUUID(context);
                        return;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                // 步驟 4. --- 取得 WI-FI 的 MAC Address
                String macAddr = getMacAddress(context);
                if (macAddr != null && !macAddr.equals("")) {
                    try {
                        _deviceId = UUID.nameUUIDFromBytes(macAddr.getBytes("utf8"));
                        saveUUID(context);
                        return;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }


                // 步驟 5. --- 取得 Android ID
                final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
                if (!ANDROID_ID_BUG.equals(androidId)) {
                    try {
                        _deviceId = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                        saveUUID(context);
                        return;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }


                // 步驟 6. --- 隨機產生 UUID
                _deviceId = UUID.randomUUID();
                saveUUID(context);
            }
        }
    }


    public UUID getDeviceId() {
        return _deviceId;
    }


    private void saveUUID(Context context)	{
        final SharedPreferences prefs = context.getSharedPreferences(Settings.FILE_NAME, 0);
        prefs.edit().putString(SETTING_DEVICE_ID, _deviceId.toString()).commit();
    }

    /**
     * 取得設備的 Serial Number，如果是 API Level9，直接讀取 android.os.Build.SERIAL 即可
     */
    private String getSerialNo()	{
//		try {
//			Class<?> c = Class.forName("android.os.SystemProperties");
//			Method get = c.getMethod("get", String.class, String.class );
//			serialnum = (String)(   get.invoke(c, "ro.serialno")  );
//		}
//		catch (Exception ignored)	{	}

        //return serialnum;
        return android.os.Build.SERIAL;
    }

    /**
     * 取得 wi-fi 的 mac address
     */
    private String getMacAddress(Context context)	{
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getMacAddress();
    }

}
