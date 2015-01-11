package com.eggttball.library.io;

import java.io.*;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

/**
 * 檔案操作的常用工具類
 * Created by eggttball on 2015/1/11.
 */
public class FileUtility {

    private Context _context;


    public FileUtility(Context context)	{
        _context = context;
    }


    /**
     * 判斷裝置上是否有 SDCard
     */
    public boolean hasSDCard(boolean requireWriteAccess)	{
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state))
            return true;
        else if (!requireWriteAccess && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
            return true;
        else
            return false;
    }


    /**
     * 儲存文字檔到 Internal Storage
     */
    public boolean writeInternal(String fileName, String content)	{
        FileOutputStream fos = null;
        boolean result = false;

        try {
            fos = _context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    /**
     * 儲存圖檔到 Internal Storage
     * @param fileName	可以含有子資料夾
     */
    public boolean writeInternal(String fileName, Bitmap bmp)	{
        FileOutputStream fos = null;
        boolean result = false;

        if (fileName.contains("/")) {
            // 含有子資料夾，先取得資料夾的絕對路徑
            String path = fileName.substring(0, fileName.lastIndexOf("/") + 1);
            if (!path.startsWith("/"))	path = "/" + path;
            File file = new File(_context.getApplicationContext().getFilesDir() + path);

            // 資料夾沒建立成功，不用作了
            if (!file.exists() && !file.mkdirs())	return false;

            try {
                fos = new FileOutputStream(_context.getApplicationContext().getFilesDir() + "/" + fileName, true);
                result = bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
        {
            try {
                fos = _context.openFileOutput(fileName, Context.MODE_PRIVATE);
                result = bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        fos = null;

        Log.i(this.toString(), "writeInternal : " + fileName + " ..... " + result);

        return result;
    }


    /**
     * 寫入二進位資料到 Internal Storage
     * @param fileName	可以含有子資料夾
     */
    public boolean writeInternal(String fileName, byte[] buffer)	{
        FileOutputStream fos = null;
        boolean result = false;

        Log.i(this.toString(), "writeInternal : " + fileName);

        if (fileName.contains("/")) {
            // 含有子資料夾，先取得資料夾的絕對路徑
            String path = fileName.substring(0, fileName.lastIndexOf("/") + 1);
            if (!path.startsWith("/"))	path = "/" + path;
            File file = new File(_context.getApplicationContext().getFilesDir() + path);

            // 資料夾沒建立成功，不用作了
            if (!file.exists() && !file.mkdirs())	return false;

            try {
                fos = new FileOutputStream(_context.getApplicationContext().getFilesDir() + "/" + fileName, true);
                fos.write(buffer);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
        {
            try {
                fos = _context.openFileOutput(fileName, Context.MODE_PRIVATE);
                fos.write(buffer);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        fos = null;

        result = true;

        Log.i(this.toString(), "writeInternal : " + fileName + "....." + true);

        return result;
    }


    /**
     * 從 Internal Storage 讀取文字檔內容
     */
    public String readInternal(String fileName)	{

        StringBuilder sb = new StringBuilder();

        try {
            FileInputStream fis = _context.openFileInput(fileName);
            byte[] buffer = new byte[256];

            while((fis.read(buffer)) != -1)	{
                sb.append(new String(buffer).trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }


    /**
     * 讀取一個資料夾內所有檔案。如果給的不是資料夾名稱，將回傳 null
     */
    public File[] readInternalFiles(String pathName)	{

        File file = new File(_context.getApplicationContext().getFilesDir() + "/" + pathName);
        Log.i(this.toString(), "readInternalFiles : file.isDirectory() " + file.isDirectory());
        if (!file.isDirectory())	return null;

        return file.listFiles();
    }


    /**
     * 讀取一個資料夾內所有檔案。如果給的不是資料夾名稱，將回傳 null
     */
    public File[] readInternalFiles(String pathName, FilenameFilter filter)	{

        File file = new File(_context.getApplicationContext().getFilesDir() + "/" + pathName);
        Log.i(this.toString(), "readInternalFiles : file.isDirectory() " + file.isDirectory());
        if (!file.isDirectory())	return null;

        return file.listFiles(filter);
    }


    /**
     * 從 Internal Storage 讀取圖檔
     */
    public Bitmap readInternalBitmap(String fileName)	{

        Bitmap bmp = null;
        FileInputStream fis = null;

        if (fileName.contains("/")) {
            // 含有子資料夾
            File file = new File(_context.getApplicationContext().getFilesDir() + "/" + fileName);
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                fis = _context.openFileInput(fileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }


        if (fis == null)	return null;

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_4444;
        opt.inPurgeable = true;
        opt.inInputShareable = true;

        bmp = BitmapFactory.decodeStream(fis, null, opt);

        Log.i(this.toString(), "readInternalBitmap : " + (bmp == null));

        return bmp;
    }


    public Bitmap readFile(File file, int requiredSize){

        Bitmap bmp = null;
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }

        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(fis, null, option);

        // Find the correct scale value. It should be the power of 2.
        int scale = 1;
        while (option.outWidth/scale/2 >= requiredSize && option.outHeight/scale/2 >= requiredSize)
            scale*= 2;

        // Decode with inSampleSize
        BitmapFactory.Options option2 = new BitmapFactory.Options();
        option2.inSampleSize = scale;
        bmp = BitmapFactory.decodeStream(fis, null, option2);

        return bmp;
    }


    /**
     * 複製檔案
     * @return	複製成功則回傳 true, 否則回傳 false
     */
    public boolean copyFile(File src, File dst)	{
        try {

            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            out.close();

        } catch (IOException e) {
            return false;
        }

        return true;
    }


    /**
     * 從 Stream 讀取字串
     */
    public String readStream(InputStream stream)	{
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;

        try {
            while((line = br.readLine()) != null){
                sb.append(line);
            }
            stream.close();
            br.close();
        } catch (Exception e) {	}

        return sb.toString();
    }


    /**
     * 從 Asset 讀取某一檔案的內容
     */
    public String readAsset(String fileName)	{
        AssetManager assetManager = _context.getAssets();
        String value = null;
        try {
            InputStream stream = assetManager.open(fileName);
            value = readStream(stream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return value;
    }

}
