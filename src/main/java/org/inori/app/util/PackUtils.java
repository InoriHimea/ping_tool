package org.inori.app.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 压缩工具类
 * @author InoriHimea
 * @version 1.0
 * @date 2018/8/25 16:42
 * @since jdk1.8
 */
public class PackUtils {

    /**
     * 将文件打包成<span style="color: red; font-size: 24px;">7z</span>格式
     * @param path
     */
    public static void packFile2SevenZ(String path) {
        System.out.println("当前打包路径为：" + path);
        File fileDir = new File(path);
        writePackInfo(path);
        Map<String, File> dirFileMap = new LinkedHashMap<>();

        putFiles2DirFileMap(fileDir, path.substring(path.lastIndexOf("/") + 1), dirFileMap);

        if (dirFileMap.isEmpty()) {
            new FileNotFoundException("所指定的路径不包含文件");
        }
    }

    private static void putFiles2DirFileMap(File fileDir, String substring, Map<String, File> dirFileMap) {

    }

    private static void writePackInfo(String path) {

    }
}
