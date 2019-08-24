package com.lxf.storage;

import android.database.sqlite.SQLiteDatabase;

public interface IModuleDatabaseHelper {

    void onCreate(SQLiteDatabase db);

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

}
