package com.eggttball.library;

import android.content.Context;
import android.content.pm.PackageInfo;
import java.util.List;

/**
 * App 常用的方法集合類
 * Created by eggttball on 2015/1/11.
 */
public class AppUtility {

    private Context _context;

    public AppUtility(Context context)  {
        _context = context;
    }


    /**
     * 手機上有沒有任何 app 是特定的 package ?
     */
    public boolean IsAppExist(String packageName)	{
        if (packageName == null || "".equals(packageName))
            return false;

        List<PackageInfo> packs = _context.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++)	{
            PackageInfo p = packs.get(i);
            if (packageName.equals(p.packageName))
                return true;
        }

        return false;
    }


    /**
     * 取得 App 的 Version Name，例如 1.3.5
     */
    public String getVersionName()	{
        String versionName = null;

        try {
            PackageInfo info = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0);
            versionName = info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return versionName;
    }

}
