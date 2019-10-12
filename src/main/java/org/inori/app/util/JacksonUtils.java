package org.inori.app.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JacksonUtils {

    private static final Logger logger = LoggerFactory.getLogger(JacksonUtils.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 对象转Json
     * @param object
     * @return
     */
    public static String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("对象{}转换异常", object, e);
        }
        return null;
    }

    /**
     * 默认格式化输出
     * @param object
     * @return
     */
    public static String toDefaultPrettyJson(Object object) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("对象{}转换异常", object, e);
        }
        return null;
    }

    /**
     * Json转成指定对象
     * @param json
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T fromJson(String json, T t) {
        return null;
    }

    /**
     * 转换成复杂对象
     */
    public static void fromJson() {

    }
}
