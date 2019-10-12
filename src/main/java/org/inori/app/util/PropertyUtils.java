package org.inori.app.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 加载配置文件
 * @author InoriHimea
 * @version 1.0
 * @date 2018/8/25 16:54
 * @since jdk1.8
 */
public class PropertyUtils {

    private static Properties prop;

    static {
        loadProp();
    }

    private static synchronized void loadProp() {
        prop = new Properties();

        //读取classPath下的properties配置文件
        InputStream propIs = ClassLoader.getSystemResourceAsStream("app.properties");
        try {
            prop.load(propIs);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //加载目录下的配置文件
        File appFile = new File("resources/app.properties");
        try (FileInputStream fis = new FileInputStream(appFile)) {
            prop.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查找需要找的key的值
     * @param key
     * @return
     */
    public static String getProperty(String key) {
        if (key == null || key.equals("") || key == "") {
            new IllegalArgumentException("未找到相关的参数");
        }

        if (prop == null) {
            loadProp();
        }

        return prop.getProperty(key);
    }

    /**
     * 传入一个默认值，如果能在配置文件中找到相关值，
     * 则使用找到的值，否则，使用传入的参数作为值
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getProperty(String key, String defaultValue) {
        return prop.getProperty(key, defaultValue);
    }
}
