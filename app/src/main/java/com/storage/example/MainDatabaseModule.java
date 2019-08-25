package com.storage.example;

import android.database.sqlite.SQLiteDatabase;

import com.lxf.annotations.ModuleDatabase;
import com.lxf.storage.IModuleDatabaseHelper;

@ModuleDatabase(name = "mainModule", version = 1)
public class MainDatabaseModule implements IModuleDatabaseHelper {
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
