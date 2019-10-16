package org.inori.app;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class HostsTest {

    private static final Logger logger = LoggerFactory.getLogger(HostsTest.class);

    @Test
    public void testReadHosts() {
        File file = new File("C:\\Windows\\System32\\drivers\\etc\\hosts");

        try (FileInputStream is = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(is, "UTF-8");
             BufferedReader br = new BufferedReader(isr)) {

            logger.info("开始读取hosts文件");
            String line;
            while ((line = br.readLine()) != null) {
                logger.info(line);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWriteHosts() {
        String host = "www.qq.com";
        String ip = "157.255.192.44";
        File file = new File("C:\\Windows\\System32\\drivers\\etc\\hosts");

        try (FileOutputStream fos = new FileOutputStream(file, true);
               OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
               BufferedWriter bw = new BufferedWriter(osw)) {

            bw.newLine();
            bw.newLine();
            bw.write(ip + "\t" + host);
            bw.newLine();

            bw.flush();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
