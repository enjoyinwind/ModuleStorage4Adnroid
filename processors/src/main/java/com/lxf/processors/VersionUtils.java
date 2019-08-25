package com.lxf.processors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class VersionUtils {
    private static final String VersionFileName = "moduleDatabaseVersion.txt";

    public static void read(Map<String, InnerVersionInfo> map, String fileName) {
        System.out.println("VersionUtils read: " + System.getProperty("user.dir"));
        File file = new File(System.getProperty("user.dir") + File.separator + fileName);
        if (!file.exists()) {
            return;
        }

        try{
            BufferedReader in = new BufferedReader(new FileReader(file));

            try {
                String line = in.readLine();
                while (line != null) {
                    String[] data = line.split(" ");

                    InnerVersionInfo versionInfo = new InnerVersionInfo(Integer.parseInt(data[1]), 0);
                    map.put(data[0], versionInfo);

                    line = in.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(in);
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    public static void write(Map<String, InnerVersionInfo> map, String fileName) {
        try {
            File file = new File(System.getProperty("user.dir") + File.separator + fileName);
            BufferedWriter out = new BufferedWriter(new FileWriter(file));

            try {
                for (String key : map.keySet()) {
                    InnerVersionInfo versionInfo = map.get(key);
                    out.write(key + " " + versionInfo.newVersion);
                    out.newLine();
                }
            } finally {
                close(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
