package com.synaric.app.rxmodel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 装饰类，提供数据库操作的总控制。
 * Created by Synaric on 2016/8/23 0023.
 */
@SuppressWarnings("ALL")
public final class RxModel {

    //RxModel的数据库没有版本概念，恒为1
    public static final int VERSION = 1;

    private Context context;
    private String dbName;
    private SQLiteOpenHelper devHelper;
    private SQLiteDatabase database;
    private ReadWriteLock lock;

    private RxModel(Builder builder) {
        context = builder.context;
        dbName = builder.dbName;
        devHelper = new DevHelper(context, dbName, null, VERSION);
        database = devHelper.getWritableDatabase();
        lock = new ReentrantReadWriteLock();
    }

    /**
     * 获取未被锁定的数据库。
     */
    public SQLiteDatabase getDataBase() {
        try {
            SQLiteDatabase db = devHelper.getWritableDatabase();
            db.enableWriteAheadLogging();
            return db;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取全局读写锁。
     */
    public ReadWriteLock getGlobalLock() {
        return lock;
    }

    private static class DevHelper extends SQLiteOpenHelper {

        public DevHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d("RxModel", "DataBase onCreate.");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d("RxModel", "DataBase onUpgrade.");
        }
    }

    public static class Builder {

        private Context context;
        private String dbName;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * 设置数据库名称。
         */
        public Builder dbName(String dbName) {
            this.dbName = dbName;
            return this;
        }

        /**
         * 构建RxModel控制总线。
         */
        public RxModel build() {
            return new RxModel(this);
        }
    }

    public interface IdBinder<T> {

        String onBind(T t);
    }
}
