package com.eggttball.library.io;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

import com.eggttball.library.Settings;

/**
 * 存取 Shared Preference 的常用方法
 * Created by eggttball on 2015/1/11.
 */
public class SharedPrefs {

    private Context _context;
    private SharedPreferences _sharedPreferences;

    public SharedPrefs(Context context)	{
        this._context = context;
    }

    SharedPreferences getSharedPrefs()	{
        if (_sharedPreferences == null)
            _sharedPreferences = _context.getSharedPreferences(Settings.FILE_NAME, Context.MODE_PRIVATE);

        return _sharedPreferences;
    }

    public boolean writeString(String key, String value)	{
        return this.getSharedPrefs()
                .edit()
                .putString(key, value)
                .commit();
    }

    public String readString(String key)	{
        return this.getSharedPrefs().getString(key, null);
    }

    public boolean writeInteger(String key, int value)	{
        return this.getSharedPrefs()
                .edit()
                .putInt(key, value)
                .commit();
    }

    public Integer readInteger(String key)	{
        return this.getSharedPrefs().getInt(key, 0);
    }

    public boolean writeLong(String key, long value)	{
        return this.getSharedPrefs()
                .edit()
                .putLong(key, value)
                .commit();
    }

    public Long readLong(String key)	{
        return this.getSharedPrefs().getLong(key, 0);
    }

    public boolean writeBoolean(String key, boolean value)	{
        return this.getSharedPrefs()
                .edit()
                .putBoolean(key, value)
                .commit();
    }

    public boolean readBoolean(String key)	{
        return this.getSharedPrefs().getBoolean(key, false);
    }

    public void deleteKey(String key)	{
        this.getSharedPrefs().edit().remove(key).commit();
    }

    public void deleteKeys(String keyPrefix)	{
        Map<String, ?> values = this.getSharedPrefs().getAll();
        for (String key : values.keySet()) {
            if (key.startsWith(keyPrefix))
                deleteKey(key);
        }
    }

}
