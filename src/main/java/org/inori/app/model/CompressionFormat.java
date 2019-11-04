package org.inori.app.model;

/**
 * 压缩格式
 */
public enum CompressionFormat {

    _7z("7z", "7zip压缩格式"),
    _zip("zip", "zip压缩格式"),
    _tar("tar", "tar压缩格式"),
    _jar("jar", "jar压缩格式"),
    _gz("gz", "gz压缩格式"),
    _rar("rar", "rar压缩格式");

    private String suffix;
    private String desc;

    CompressionFormat(String suffix, String desc) {
        this.suffix = suffix;
        this.desc = desc;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return "CompressionFormat{" +
                "suffix='" + suffix + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
