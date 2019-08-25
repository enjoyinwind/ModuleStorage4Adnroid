package com.lxf.storage;

public class VersionInfo {
    int oldVersion;
    int newVersion;
    IModuleDatabaseHelper listener;

    VersionInfo(int oldVersion, int newVersion, IModuleDatabaseHelper listener) {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
        this.listener = listener;
    }
}
