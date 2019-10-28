package org.inori.app;

import org.inori.app.thread.ExecutorServiceManager;
import org.inori.app.thread.NamedRunnable;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class PingTest {

    @Test
    void testPings() {

        try {
            List<String> command = new LinkedList<>();
            command.add("cmd");
            command.add("/c");
            command.add("ping");
            command.add("222.222.222.222");
            command.add("&");
            command.add("exit");

            ProcessBuilder builder = new ProcessBuilder(command);
            final Process start = builder.start();

            ExecutorService pingService = ExecutorServiceManager.getCachedExecutorService("ping服务");
            pingService.submit(new NamedRunnable() {
                @Override
                public void runAfter() {
                    try (InputStream is = start.getInputStream();
                            InputStreamReader isr = new InputStreamReader(is, "GBK");
                            BufferedReader br = new BufferedReader(isr)) {

                        String line;
                        while ((line = br.readLine()) != null) {
                            System.out.println(line);
                        }

                    } catch (Exception e) {

                    }
                }
            });

            int i = start.waitFor();
            System.out.println(start.exitValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
