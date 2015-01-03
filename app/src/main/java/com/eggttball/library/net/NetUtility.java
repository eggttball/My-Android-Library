package com.eggttball.library.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 常用的網路工具類別，例如偵測網路狀態
 * Created by eggttball on 2015/1/3.
 */
public class NetUtility {
    private Context _context;
    private static NetworkInfo _networkInfo;

    public NetUtility(Context context)	{
        _context = context;
    }

    /**
     * 必要權限：ACCESS_NETWORK_STATE
     */
    private synchronized static NetworkInfo getNetworkInfo(Context context)    {
        if (_networkInfo == null)   {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            _networkInfo = connManager.getActiveNetworkInfo();
        }

        return _networkInfo;
    }


    /**
     * 判斷使用者手機目前是否能上網<br />
     * 必要權限：ACCESS_NETWORK_STATE
     */
    public boolean isNetworkAvailable()	{
        _networkInfo = getNetworkInfo(_context);

        // 使用者根本沒有網路的情況
        if (_networkInfo == null)
            return false;
        else
            return _networkInfo.isConnected();
    }

    /**
     * 判斷使用者手機目前使用哪一種網路？例如 3G, WIFI, WIMAX...<br />
     * 必要權限：ACCESS_NETWORK_STATE
     */
    public int getNetworkType()	{
        if (!isNetworkAvailable())	return -1;

        return _networkInfo.getType();
    }

}
