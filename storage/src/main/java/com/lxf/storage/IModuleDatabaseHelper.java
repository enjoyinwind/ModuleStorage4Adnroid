package com.lxf.storage;

import android.database.sqlite.SQLiteDatabase;

public interface IModuleDatabaseHelper {

    void onCreate(SQLiteDatabase db);

    void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

}
