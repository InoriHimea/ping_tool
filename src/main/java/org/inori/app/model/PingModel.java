package org.inori.app.model;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

@Data
public class PingModel {

    private String hostName;
    private String ip;
    private int bytes;
    private int sendNum;
    private int receiveNum;
    private int loseNum;
    private double lostPercent;
    private int minTime;
    private int maxTime;
    private int avgTime;
    private List<Pings> pings;

    public PingModel castResult2Model(List<String> lineList) {
        PingModel pingModel = new PingModel();
        List<Pings> pingsList = new LinkedList<>();

        for (String line : lineList) {
            if (line.equals("请求超时。")) {
                pingsList.add(new Pings());
            }

            if (line.startsWith("正在")) {
                String[] pick = line.split(" ");

                if (line.contains("[") && line.contains("]")) {
                    pingModel.setHostName(pick[2]);
                    pingModel.setIp(pick[3].replace("[", "").replace("]", ""));
                    pingModel.setBytes(Integer.parseInt(pick[5]));
                } else {
                    pingModel.setIp(pick[2]);
                    pingModel.setBytes(Integer.parseInt(pick[4]));
                }
            } else if (line.startsWith("来自")) {
                String[] pick2 = line.split(": ");
                String[] pick3 = pick2[0].split(" ");
                String[] pick4 = pick2[1].split(" ");

                Pings pings = new Pings();
                pings.setIp(pick3[1]);
                pings.setBytes(Integer.parseInt(pick4[0].split("=")[1]));
                pings.setTime(Integer.parseInt(pick4[1].split("=")[1].replace("ms", "")));
                pings.setTtl(Integer.parseInt(pick4[2].split("=")[1]));
                pingsList.add(pings);
            } else if (line.trim().startsWith("数据包")) {
                String[] pick5 = line.split(": ")[1].split("，");

                pingModel.setSendNum(Integer.parseInt(pick5[0].split("=")[1].trim()));
                pingModel.setReceiveNum(Integer.parseInt(pick5[1].split("=")[1].trim()));
                pingModel.setLoseNum(Integer.parseInt(pick5[2].split("=")[1].split(" \\(")[0].trim()));
                pingModel.setLostPercent(Double.parseDouble(pick5[2].split(" \\(")[1].split("%")[0].trim()));
            } else if (line.trim().startsWith("最短")) {
                String[] pick6 = line.split("，");

                pingModel.setMinTime(Integer.parseInt(pick6[0].split("=")[1].trim().replace("ms", "")));
                pingModel.setMaxTime(Integer.parseInt(pick6[1].split("=")[1].trim().replace("ms", "")));
                pingModel.setAvgTime(Integer.parseInt(pick6[2].split("=")[1].trim().replace("ms", "")));
            } else {
                System.out.println("不做处理的行：" + line);
            }
        }

        pingModel.setPings(pingsList);

        return pingModel;
    }
}
