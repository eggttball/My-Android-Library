package com.eggttball.library;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import java.util.List;

/**
 * 與 App 直接互動的常用方法，例如控制軟體鍵盤
 * Created by eggttball on 2015/1/11.
 */
public class InteractUtility {

    private Context _context;

    public InteractUtility(Context context) {
        _context = context;
    }


    /**
     * 強制彈出軟體鍵盤
     */
    public void ShowKeypad(Context context)	{
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        // showSoftInput 似乎有時候無效，所以改用 toggleSoftInput
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }


    /**
     * 強制隱藏軟體鍵盤
     */
    public void HideKeypad(Context context, EditText editText)	{
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }


    /**
     * 取得目前正在前景執行的 app Package Name
     */
    @Deprecated
    public String getFrontApp()	{
        ActivityManager am = (ActivityManager) _context.getSystemService(Context.ACTIVITY_SERVICE);

        List<RunningTaskInfo> Info = am.getRunningTasks(1);

        return Info.get(0).topActivity.getPackageName();
    }


    /**
     * 取得直接執行另一個 app 的 Intent
     * @param packageName	另一個 app 的 package name
     * @param mimeType		例如 image/png
     * @return
     */
    public Intent getIntentForOtherApp(String packageName, String mimeType)	{
        Intent intent = _context.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setType(mimeType);
        return intent;
    }


    /**
     * 取得特定名稱的 Intent (避免跳出 Intent 選單)
     * @param action	例如 Intent.ACTION_VIEW、Intent.ACTION_SEND
     * @param keyword	app 的關鍵字，例如，想直接跳 Gmail 讓使用者寄信，就寫 "Gmail"
     * @param uri
     * @return
     */
    public Intent getSpecifiedIntent(String action, String keyword, Uri uri) {
        boolean found = false;
        Intent intent = (uri == null) ? new Intent(action) : new Intent(action, uri);

        // 取得所有可以被載入的 intents
        List<ResolveInfo> resInfo = _context.getPackageManager().queryIntentActivities(intent, 0);

        if (!resInfo.isEmpty()){
            for (ResolveInfo info : resInfo) {
                if (info.activityInfo.packageName.toLowerCase().contains(keyword) ||
                        info.activityInfo.name.toLowerCase().contains(keyword) ) {

                    intent.setPackage(info.activityInfo.packageName);
                    found = true;
                    break;
                }
            }

            if (!found)	return null;

            return Intent.createChooser(intent, "Select");
        }

        return null;
    }

}
