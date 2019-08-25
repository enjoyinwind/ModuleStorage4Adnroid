package com.lxf.processors;

public class InnerVersionInfo {
    int oldVersion;
    int newVersion;
    String listenerName;

    InnerVersionInfo(int oldVersion, int newVersion) {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }
}
