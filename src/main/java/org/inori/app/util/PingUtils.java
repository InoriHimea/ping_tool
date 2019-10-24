package org.inori.app.util;

import org.inori.app.model.PingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PingUtils {

    private static final Logger logger = LoggerFactory.getLogger(PingUtils.class);

    /**
     * 给指定的IP进行ping测试
     * @param ipOrHost
     * @param count
     */
    public static PingModel ping(String ipOrHost, int count) throws IOException {
        List<String> commandList = new LinkedList<>();
        commandList.add("cmd");
        commandList.add("/c");
        commandList.add("ping");
        commandList.add(ipOrHost);
        commandList.add("/n");
        commandList.add(String.valueOf(count));
        logger.debug("当前的命令为，{}", JacksonUtils.toDefaultPrettyJson(commandList));

        return CommandExecutor.getInstance().exec(commandList, PingModel.class);
    }
}
