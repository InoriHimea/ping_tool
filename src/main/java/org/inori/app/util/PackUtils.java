package org.inori.app.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;

/**
 * 压缩工具类
 * @author InoriHimea
 * @version 1.1
 * @date 2018/8/25 16:42
 * @since jdk1.8
 */
@Slf4j
public class PackUtils {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z E", Locale.CHINESE);

    public static void compressFiles(String basePath, String packageName, String type) throws FileNotFoundException {
        File dirPath = new File(basePath);
        writePackInfo(basePath);
        FilesUtils.getDirSize(dirPath);
        Map<String, File> dirFileMap = new LinkedHashMap<>();

        //递归来生成打包的目录结构Map
        putFiles2DirFileMap(dirPath, null, dirFileMap);

        //如果结构目录为空则抛出没找到文件终止打包
        if (dirFileMap.isEmpty()) {
            throw new FileNotFoundException("指定目录下没有文件或文件夹");
        }

        //根据给定的格式进行打包
    }

    /**
     * 打包文件为7zip格式，但是目前对于空文件夹会有一个0字节文件的问题
     * @param path
     * @param packageName
     */
    public static void packFile2SevenZ(String path, String packageName) throws IOException {
        File dirPath = new File(path);
        writePackInfo(path);
        Map<String, File> dirFileMap = new LinkedHashMap<>();

        //递归调用以生成目录结构
        putFiles2DirFileMap(dirPath, null, dirFileMap);

        if (dirFileMap.isEmpty()) {
            throw new FileNotFoundException("当前打包目录找不到");
        }

        try (SevenZOutputFile sevenZFile = new SevenZOutputFile(new File(packageName + ".7z"))) {
            log.debug("开始处理7zip压缩包实体");

            for (Map.Entry<String, File> entry : dirFileMap.entrySet()) {
                SevenZArchiveEntry archiveEntry = sevenZFile.createArchiveEntry(entry.getValue(), entry.getKey());
                sevenZFile.putArchiveEntry(archiveEntry);

                if (! archiveEntry.isDirectory()) {
                    try (FileInputStream fis = new FileInputStream(entry.getValue())) {
                        byte[] array = new byte[1024 * 8];
                        int len;
                        while ((len = fis.read(array)) != -1) {
                            sevenZFile.write(array, 0, len);
                        }
                    }
                }

                sevenZFile.closeArchiveEntry();
            }

            sevenZFile.setContentCompression(SevenZMethod.LZMA2);
            sevenZFile.finish();
        }
    }

    public static void packFile2Zip(String path, String packageName) throws IOException {
        File dirPath = new File(path);
        writePackInfo(path);
        Map<String, File> dirFileMap = new LinkedHashMap<>();

        //递归调用以生成目录结构
        putFiles2DirFileMap(dirPath, null, dirFileMap);

        if (dirFileMap.isEmpty()) {
            throw new FileNotFoundException("当前打包目录找不到");
        }

        try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(new File(packageName + ".zip"))) {
            log.debug("开始处理Zip实体");

            for (Map.Entry<String, File> entry : dirFileMap.entrySet()) {
                ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(entry.getValue(), entry.getKey());
                zaos.putArchiveEntry(zipArchiveEntry);

                if (! zipArchiveEntry.isDirectory()) {
                    try (FileInputStream fos = new FileInputStream(entry.getValue())) {
                        byte[] array = new byte[1024 * 8];

                        int len;
                        while ((len = fos.read(array)) != -1) {
                            zaos.write(array, 0, len);
                        }
                    }
                }

                zaos.closeArchiveEntry();
            }

            zaos.setUseZip64(Zip64Mode.Always);
            zaos.setLevel(Deflater.BEST_COMPRESSION);
            zaos.setComment("版权所有");
            zaos.setEncoding("GBK");
            zaos.finish();
        }
    }

    private synchronized static void putFiles2DirFileMap(File file, String parentName, Map<String, File> dirFileMap) {

        if (file.isDirectory()) {
            File[] files = file.listFiles();

            assert files != null;
            if (files.length == 0) {
                dirFileMap.put(parentName, file);
            } else {
                for (File f : files) {
                    String name = parentName == null ? f.getName() : parentName + File.separator + f.getName();
                    putFiles2DirFileMap(f, name, dirFileMap);
                }
            }
        } else {
            dirFileMap.put(parentName, file);
        }
    }

    private static void writePackInfo(String path) {
        try (FileOutputStream fos = new FileOutputStream(new File(path, "打包说明.txt"));
                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                BufferedWriter writer = new BufferedWriter(osw)) {

            writer.write("------------------------------------");
            writer.newLine();
            writer.write("本压缩包由基于JAVA编写的打包工具压缩而成");
            writer.newLine();
            writer.write("时间：" + sdf.format(Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai")).getTime()));
            writer.newLine();
            writer.write("------------------------------------");

            writer.flush();
        } catch (UnsupportedEncodingException e) {
            log.error("不支持的编码格式【UTF-8】", e);
        } catch (FileNotFoundException e) {
            log.error("文件未找到", e);
        } catch (IOException e) {
            log.error("I/O异常", e);
        }
    }

    public static void main(String[] args) {
        try {
            PackUtils.compressFiles("D:/wallpaper壁纸", "零食包", "7z");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
