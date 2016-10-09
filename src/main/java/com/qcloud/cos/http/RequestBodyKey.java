package com.qcloud.cos.http;

/**
 * @author chengwu 封装HTTP请求包体中的K-V对中key枚举值类
 */
public class RequestBodyKey {
	public static final String OP = "op";
	public static final String BIZ_ATTR = "biz_attr";
	public static final String UPDATE_FLAG = "flag";
	public static final String AUTHORITY = "authority";
	public static final String CUSTOM_HEADERS = "custom_headers";
	public static final String INSERT_ONLY = "insertOnly";
	public static final String DEST_FIELD = "dest_fileid";
	public static final String TO_OVER_WRITE = "to_over_write";
	public static final String PREFIX = "prefix";
	public static final String NUM = "num";
	public static final String CONTEXT = "context";
	public static final String LIST_FLAG = "list_flag";
	public static final String SHA = "sha";
	public static final String FILE_CONTENT = "fileContent";
	public static final String FILE_SIZE = "filesize";
	public static final String SLICE_SIZE = "slice_size";
	public static final String SESSION = "session";
	public static final String OFFSET = "offset";
	public static final String UPLOAD_PARTS = "uploadparts";

	public class UploadParts {
		public static final String OFFSET = "offset";
		public static final String DATA_LEN = "datalen";
		public static final String DATA_SHA = "datasha";
	}
}
