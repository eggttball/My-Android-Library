package com.eggttball.library;

/**
 * 常用於讀取資料後 callback 的介面
 * Created by eggttball on 2015/1/3.
 */
public abstract class DataListener <T> {

    public abstract void onDataComplete(T data);

    public void onDataError(T data)    {}
}
