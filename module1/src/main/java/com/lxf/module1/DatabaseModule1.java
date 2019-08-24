package com.lxf.module1;

import android.database.sqlite.SQLiteDatabase;

import com.lxf.storage.IModuleDatabaseHelper;
import com.lxf.storage.ModuleDatabase;

@ModuleDatabase(name = "module1", version = 1)
public class DatabaseModule1 implements IModuleDatabaseHelper {
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
