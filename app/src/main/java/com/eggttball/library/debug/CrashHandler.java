package com.eggttball.library.debug;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.eggttball.library.net.NetUtility;
import com.eggttball.library.display.DisplayUtility;

/**
 * 可自動收集當機的錯誤報告，並存成檔案
 * @author eggttball
 */
public class CrashHandler implements UncaughtExceptionHandler {
    private static final String TAG = "MyCrashHandler";
    // Crash 錯誤報告檔案的附檔名
    private static final String CRASH_REPORTER_EXTENSION = ".cr";
    private static final String KEY_VERSION_NAME = "VersionName";
    private static final String KEY_VERSION_CODE = "VersionCode";
    private static final String KEY_STACK_TRACE = "STACK_TRACE";
    private static CrashHandler INSTANCE = new CrashHandler();
    // 處理異常訊息的同時，是否也一併輸出到 Log 視窗，開發階段可給予 true，更方面偵錯
    private boolean _debugInLog = false;
    // 當機時，是否直接 Kill Process？或是照樣彈出預設當機畫面？
    private boolean _exitInCrash = true;
    // 讓 User 自行決定在當機發生時，是否提交錯誤報告到遠端伺服器，暫不使用
    private boolean _reportByUserClick = false;
    // 保存設備資訊
    private Properties _deviceInfo = new Properties();
    // 要接收錯誤報告的遠端伺服器
    private static final String API_ROOT = "http://ure-dev.appspot.com";
    private Context _context;
    private Thread.UncaughtExceptionHandler _exceptionHandler;
    // 這些是只要設備資訊，將會存入遠端伺服器資料庫，以便一覽，才不用所有資訊都得打開 .cr 檔案才可以查
    private String api_device = "", api_versionName, api_versionCode, api_manufacturer = "", api_cause = "";

    // 搭配 INSTANCE，保證只有一個實例物件
    private CrashHandler() { }


    public static CrashHandler getInstance() {	return INSTANCE;	}


    /**
     * 初始化當機處理機制
     * @param ctx
     * @param debugInLog	處理異常訊息的同時，是否也一併輸出到 Log 視窗，開發階段應給予 true
     * @param exitInCrash	當機時，是否直接 Kill Process？或是照樣彈出預設當機畫面？建議給予 true，雖然讓使用者突然感到疑惑，但總比當機畫面讓人感到憤怒的好
     */
    public void init(Context ctx, boolean debugInLog, boolean exitInCrash) {
        _debugInLog = debugInLog;
        _exitInCrash = exitInCrash;
        _context = ctx;
        _exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!_exitInCrash || (!handleException(ex) && _exceptionHandler != null)) {
            // 如果用戶沒有處理，則讓系統預設的異常處理器接手
            _exceptionHandler.uncaughtException(thread, ex);
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }


    /**
     * 自定義錯誤處理：收集錯誤資訊，傳送錯誤報告
     * @return	正確無誤的處理完該異常，為 true；否則為 false
     */
    private boolean handleException(Throwable ex)	{
        if (ex == null)	return true;

        final String msg = ex.getLocalizedMessage();


        /* 如果有需要，可以借用 UI Thread 顯示任何提示、對話框訊息....
        new Handler(Looper.getMainLooper()).post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

			}
		});
        ***********************************************/



        /* 或是使用 Toast 提示使用者
        new Thread()	{
        	public void run() {
        		Looper.prepare();
        		Toast.makeText(_context, "The app run into some error : " + msg, Toast.LENGTH_LONG).show();
        		Looper.loop();
        	};
        }.start();
        ***********************************************/

        // ※以上兩種寫法都可以



        // 收集設備資訊
        collectDeviceInfo(_context);
        // 儲存錯誤報告到檔案中
        saveCrashInfoToFile(ex);
        // 將檔案上傳到遠端伺服器
        reportToServer();

        return true;
    }


    /**
     * 收集設備相關資訊
     */
    private void collectDeviceInfo(Context ctx)	{
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                api_versionName = pi.versionName == null ? "not set" : pi.versionName;
                api_versionCode = String.valueOf(pi.versionCode);
                _deviceInfo.put(KEY_VERSION_NAME, api_versionName);
                _deviceInfo.put(KEY_VERSION_CODE, api_versionCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while collect package info", e);
        }

        // 使用反射來收集設備資訊，例如系統版本號、設備生產商
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                String value = field.get(null).toString();
                _deviceInfo.put(field.getName(), value);

                if (field.getName().equals("DEVICE"))
                    api_device = value;
                if (field.getName().equals("MANUFACTURER"))
                    api_manufacturer = value;

                if (_debugInLog)
                    Log.d(TAG, field.getName() + " : " + value);

            } catch (Exception e) {
                Log.e(TAG, "Error while collect crash info", e);
            }
        }

    }


    /**
     * 將詳細的當機相關報告儲存到檔案中
     */
    private String saveCrashInfoToFile(Throwable ex)	{
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        ex.printStackTrace(printWriter);

        // 如果還有 cause，也逐一取得其資訊
        Throwable cause = ex.getCause();
        if (cause != null)
            api_cause = cause.toString();	// 當機的主要原因
        while (cause != null)	{
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }

        // 以上所有資訊，輸出到字串後，存入 Property
        String result = info.toString();
        printWriter.close();
        _deviceInfo.put(KEY_STACK_TRACE, result);

        try	{
            // 根據當機時間點決定檔案名稱
            long timestamp = System.currentTimeMillis();
            String fileName = "crash-" + timestamp + CRASH_REPORTER_EXTENSION;

            // 開始輸出到檔案中
            FileOutputStream fos = _context.openFileOutput(fileName, Context.MODE_PRIVATE);
            _deviceInfo.store(fos, "");

            fos.flush();
            fos.close();
            return fileName;

        } catch (Exception e)	{
            Log.e(TAG, "an error occured while writing report file...", e);
        }

        return null;
    }


    /**
     * 取得所有錯誤報告的檔案名稱
     */
    private String[] getCrashReportFiles(Context ctx) {
        File filesDir = ctx.getFilesDir();
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(CRASH_REPORTER_EXTENSION);
            }
        };
        return filesDir.list(filter);
    }


    /**
     * 將所有錯誤報告資訊，傳送到遠端伺服器以便偵錯
     * 此方法在 app 才剛啟動時，也應手動呼叫，所以設為 public 權限。因有可能上次網路問題，導致一部分檔案沒上傳。
     */
    public void reportToServer()	{
        NetUtility utility = new NetUtility(_context);
        // 有網路時才傳送錯誤報告
        if (!utility.isNetworkAvailable())	{
            Log.i(TAG, "Network is not available to report to server.");
            return;
        }


        String[] crFiles = getCrashReportFiles(_context);
        if (crFiles != null && crFiles.length > 0) {
            TreeSet<String> sortedFiles = new TreeSet<String>();
            sortedFiles.addAll(Arrays.asList(crFiles));

            try {
                for (String fileName : sortedFiles) {
                    File cr = new File(_context.getFilesDir(), fileName);
                    FileInputStream fis = _context.openFileInput(fileName);
                    byte[] buffer = new byte[(int) cr.length()];

                    fis.read(buffer);

                    callWebApi(buffer, fileName);
                    cr.delete();
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

        }
    }


    private static String getUploadUrl()	{

        String url = null;
        HttpClient hc = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(API_ROOT + "/crashes/getUploadUrl");

        try {
            HttpResponse resp = hc.execute(httpPost);
            url = EntityUtils.toString(resp.getEntity());
        } catch (Exception e) {
            Log.e(e.getClass().toString(), e.getMessage());
            e.printStackTrace();
        }

        return url;
    }


    private void callWebApi(byte[] buffer, String fileName)	{
        String uploadUrl = getUploadUrl();
        if (uploadUrl == null)	return;

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(uploadUrl);
        ByteArrayBody bab = new ByteArrayBody(buffer, "text/plain", fileName);

        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        DisplayUtility utility = new DisplayUtility(_context);
        try {
            reqEntity.addPart("packageName", new StringBody(_context.getPackageName()));
            reqEntity.addPart("device", new StringBody(api_device));
            reqEntity.addPart("versionName", new StringBody(api_versionName));
            reqEntity.addPart("versionCode", new StringBody(api_versionCode));
            reqEntity.addPart("width", new StringBody(String.valueOf(utility.getScreenWidth())));
            reqEntity.addPart("height", new StringBody(String.valueOf(utility.getScreenHeight())));
            reqEntity.addPart("density", new StringBody(String.valueOf(utility.getScreenDensity())));
            reqEntity.addPart("manufacturer", new StringBody(api_manufacturer));
            reqEntity.addPart("sdk", new StringBody(Build.VERSION.SDK));
            reqEntity.addPart("cause", new StringBody(api_cause));
            // 這些欄位名稱請參考實際 Web API 的命名
            reqEntity.addPart("reportFile", bab);

            postRequest.setEntity(reqEntity);
            httpClient.execute(postRequest);

        } catch (Exception e) {	}
    }

}

