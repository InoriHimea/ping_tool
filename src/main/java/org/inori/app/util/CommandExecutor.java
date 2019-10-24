package org.inori.app.util;

import org.inori.app.model.PingModel;
import org.inori.app.thread.ExecutorServiceManager;
import org.inori.app.thread.NamedCallable;
import org.inori.app.thread.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class CommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    private static CommandExecutor executor;
    private static ExecutorService service;

    public static CommandExecutor getInstance() {
        if (executor == null) {
            executor = new CommandExecutor();
        }
        if (service == null) {
            service = ExecutorServiceManager.getCachedExecutorService("I/O读写组");
        }

        return executor;
    }

    public void exec(String command) throws IOException {
        Process exec = Runtime.getRuntime().exec(command);
        startProcess(exec, null);
    }

    public void exec(List<String> commandList) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(commandList);
        Process exec = builder.start();
        startProcess(exec, null);
    }

    public <V> V exec(List<String> commandList, Class<V> clazz) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(commandList);
        Process exec = builder.start();
        return startProcess(exec, clazz);
    }

    private <V> V startProcess(Process process, Class<V> clazz) {
        logger.debug("开始读取执行内容");

        Future<?> result = null;
        Future<?> error;
        if (clazz == null) {
            result = service.submit(defaultSuccessOutput(process));
        } else {
            result = service.submit(new NamedCallable<V>() {

                @Override
                public V runAfter() throws Exception {
                    List<String> lineList = new LinkedList<>();

                    try (InputStream is = process.getInputStream();
                            InputStreamReader isr = new InputStreamReader(is, "GBK");
                            BufferedReader br = new BufferedReader(isr)) {

                        String line;
                        while ((line = br.readLine()) != null) {
                            logger.debug("{}", line);
                            lineList.add(line);
                        }
                    }

                    return (V) clazz.getMethod("castResult2Model", List.class).invoke(clazz.newInstance(), lineList);
                }
            }.setName("正确流"));
        }

        error = service.submit(new NamedRunnable() {

            @Override
            public void runAfter() throws Exception {
                try (InputStream es = process.getErrorStream();
                     InputStreamReader isr = new InputStreamReader(es, "GBK");
                     BufferedReader br = new BufferedReader(isr)) {

                    String line;
                    while ((line = br.readLine()) != null) {
                        logger.error("{}", line);
                    }
                }
            }
        }.setName("异常流"));

        try {
            int code = process.waitFor();
            if (code == 0) {
                return (V) result.get();
            } else {
                logger.warn("Code为：{}", code);
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("命令执行中断, {}", e.getMessage(), e);
        }

        return null;
    }

    private NamedRunnable defaultSuccessOutput(Process process) {
        return new NamedRunnable() {

            @Override
            public void runAfter() throws Exception {
                try (InputStream is = process.getInputStream();
                     InputStreamReader isr = new InputStreamReader(is, "GBK");
                     BufferedReader br = new BufferedReader(isr)) {

                    String line;
                    while ((line = br.readLine()) != null) {
                        logger.debug("{}", line);
                    }
                }
            }
        }.setName("正确流");
    }
}
