package com.lxf.module1;

import android.database.sqlite.SQLiteDatabase;

import com.lxf.annotations.ModuleDatabase;
import com.lxf.storage.IModuleDatabaseHelper;

@ModuleDatabase(name = "module1", version = 1)
public class DatabaseModule1 implements IModuleDatabaseHelper {
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table module1table1(id integer primary key autoincrement, name varchar(64), address varchar(64))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
