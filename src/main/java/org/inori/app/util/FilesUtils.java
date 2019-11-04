package org.inori.app.util;

import org.apache.commons.io.FileUtils;
import org.inori.app.thread.ExecutorServiceManager;
import org.inori.app.thread.NamedCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @Author InoriHimea
 * @Date 2019/10/21
 */
public class FilesUtils {

    private static final Logger logger = LoggerFactory.getLogger(FilesUtils.class);

    /**
     * 把内容写入到指定文件，以覆盖的形式
     * @param lines
     * @param target
     */
    public static void writeContent2File(List<String> lines, File target) {
        writeContent2File(lines, target, false);
    }

    /**
     * 覆盖文件为当前字符串
     * @param line
     * @param target
     */
    public static void writeContent2File(String line, File target) {
        writeContent2File(line, target, false);
    }

    /**
     * 把内容写入到文件，可以为追加模式
     * @param lines
     * @param target
     * @param append
     */
    public static void writeContent2File(List<String> lines, File target, boolean append) {
        logger.info("开始写入文件，模式为{}", append ? "追加" : "覆盖");

        try {
            FileUtils.writeLines(target, "UTF-8", lines, "\r\n", append);
            logger.info("写入成功！");
        } catch (IOException e) {
            logger.error("写入失败，{}", e.getMessage(), e);
        }
    }

    /**
     * 写入接入，可以为追加模式
     * @param line
     * @param target
     * @param append
     */
    public static void writeContent2File(String line, File target, boolean append) {
        logger.info("开始写入文件，模式为{}", append ? "追加" : "覆盖");

        try {
            FileUtils.writeStringToFile(target, line, "UTF-8", append);
            logger.info("写入成功！");
        } catch (IOException e) {
            logger.error("写入失败，{}", e.getMessage(), e);
        }
    }

    public static void getDirSize(File dirPath) {
        ExecutorService jisuan = ExecutorServiceManager.getForkWorkerJoinServer(4, "jisuan");
        jisuan.submit(new NamedCallable<Integer>() {

            @Override
            public Integer runAfter() throws Exception {
                return 3;
            }
        }.setName("haxi"));
    }
}
