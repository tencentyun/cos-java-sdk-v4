package com.qcloud.cos.http;

/**
 * @author chengwu
 * 封装服务器端返回的应答包体的关键字枚举类
 */
public class ResponseBodyKey {
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String DATA = "data";

    public class Data {
        public static final String SESSION = "session";
        public static final String OFFSET = "offset";
        public static final String SLICE_SIZE = "slice_size";
        public static final String SERIAL_UPLOAD = "serial_upload";
        public static final String ACCESS_URL = "access_url";
        public static final String URL = "url";
        public static final String RESOURCE_PATH = "resource_path";
        public static final String NAME = "name";
        public static final String BIZ_ATTR = "biz_attr";
        public static final String FILESIZE = "filesize";
        public static final String SHA = "sha";
        public static final String CTIME = "ctime";
        public static final String MTIME = "mtime";
    }
}
