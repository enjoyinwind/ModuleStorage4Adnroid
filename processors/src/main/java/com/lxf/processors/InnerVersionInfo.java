package com.lxf.processors;

import javax.lang.model.element.TypeElement;

public class InnerVersionInfo {
    int oldVersion;
    int newVersion;
    TypeElement listenerType;

    InnerVersionInfo(int oldVersion, int newVersion) {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }
}
