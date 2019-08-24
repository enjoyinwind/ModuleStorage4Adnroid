package com.lxf.storage;

import android.database.sqlite.SQLiteOpenHelper;

public class ModuleDatabseManager {
    private static SQLiteOpenHelper instance;

    public static SQLiteOpenHelper getInstance(){
        return instance;
    }
}
