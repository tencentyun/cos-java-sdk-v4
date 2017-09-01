package com.qcloud.cos.common_utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.qcloud.cos.exception.ParamException;

/**
 * @author chengwu 封装一些参数检查的类,如果检查未通过抛出参数异常
 */
public class CommonParamCheckUtils {

	/**
	 * 判断参数是否为NULL，如果为NULL，抛出参数异常
	 * 
	 * @param objName
	 *            参数名
	 * @param obj
	 *            参数对象
	 * @throws ParamException
	 */
	public static void AssertNotNull(String objName, Object obj) throws ParamException {
		if (obj == null) {
			throw new ParamException(objName + " is null, please check!");
		}
	}

	/**
	 * 判断文件是否适合使用整文件上传，使用范围0 ~ 10MB, 大文件应该用分片上传
	 * 
	 * @param localFilePath
	 *            本地文件路径
	 * @throws ParamException
	 */
	public static void AssertUploadEntireFileInRange(String localFilePath) throws ParamException {
		long fileSize = 0;
		try {
			fileSize = CommonFileUtils.getFileLength(localFilePath);
		} catch (Exception e) {
			throw new ParamException(localFilePath + " is not effective file!");
		}

		long maxFileSize = 10 * 1024 * 1024;
		if (fileSize > maxFileSize) {
			throw new ParamException(localFilePath + " is too large, please use uploadSliceFile interface!");
		}
	}

	/**
	 * 判断分片尺寸是否在规定的范围内，抛出参数异常，目前的有效值为64KB ~ 10MB
	 * 
	 * @param sliceSize
	 *            分片大小, 单位Byte
	 * @throws ParamException
	 */
	public static void AssertSliceInRange(int sliceSize) throws ParamException {
		int maxSliceSize = 10 * 1024 * 1024; // 10MB
		int minSliceSize = 64 * 1024; // 64KB
		if (sliceSize > maxSliceSize || sliceSize < minSliceSize) {
			throw new ParamException("sliceSize legal value is [64KB, 100MB]");
		}
	}

    private static void AssertNotContainIllegalLetter(String cosPath) throws ParamException {
        String[] illegalLetters = {"?", "*", ":", "|", "\\", "<", ">", "\""};
        for (String illegalLetter : illegalLetters) {
            if (cosPath.contains(illegalLetter)) {
                throw new ParamException("cosFilePath contail illeagl letter " + illegalLetter);
            }
        }
        String pattern = "/(\\s*)/";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(cosPath);
        if (m.find()) {
            throw new ParamException("cosFilePath contail illeagl letter / /");
        }
    }
    
	/**
	 * 判断用户指定的cos目录路径是否合法有效, 即必须以/结尾, 同时不包含非法字符
	 * 
	 * @param cosFolderPath
	 *            cos目录路径
	 * @throws ParamException
	 */
	public static void AssertLegalCosFolderPath(String cosFolderPath) throws ParamException {
		if (cosFolderPath == null || !cosFolderPath.startsWith("/") || !cosFolderPath.endsWith("/")) {
			throw new ParamException(cosFolderPath + " is not cos folder path! Tips: make sure ends with /");
		}
		AssertNotContainIllegalLetter(cosFolderPath);
	}

	/**
	 * 判断用户指定的cos文件路径是否合法有效, 即不以/结尾
	 * 
	 * @param cosFilePath
	 *            cos文件路径
	 * @throws ParamException
	 */
	public static void AssertLegalCosFilePath(String cosFilePath) throws ParamException {
		if (cosFilePath == null || !cosFilePath.startsWith("/") || cosFilePath.endsWith("/")) {
			throw new ParamException(cosFilePath + " is not cos file path! Tips: make sure not ends with /");
		}
	}

	/**
	 * 判断cos目录是否是根路径，即路径为/
	 * 
	 * @param cosCosFolderPath
	 *            cos目录路径
	 * @throws ParamException
	 */
	public static void AssertNotRootCosPath(String cosCosFolderPath) throws ParamException {
		if (cosCosFolderPath == null || cosCosFolderPath.equals("/")) {
			throw new ParamException(
					"bucket operation is only allowed by web console! please visit http://console.qcloud.com/cos!");
		}
	}

	/**
	 * 判断用户指定的本地文件路径是否合法有效，即文件存在且可读
	 * 
	 * @param localFilePath
	 *            本地文件路径
	 * @throws ParamException
	 */
	public static void AssertLegalLocalFilePath(String localFilePath) throws ParamException {
		if (localFilePath == null || !CommonFileUtils.isLegalFile(localFilePath)) {
			throw new ParamException(localFilePath + " is not file or not exist or can't be read!");
		}
	}

	public static void AssertLegalXCosMeta(Map<String, String> xCosMetaMap) throws ParamException {
		for (String x_cos_meta_key : xCosMetaMap.keySet()) {
			AssertNotNull("x_cos_meta_key", x_cos_meta_key);
			if (!x_cos_meta_key.startsWith("x-cos-meta-")) {
				throw new ParamException("x-cos-meta name must starts with x-cos-meta-");
			}
			String x_cos_meta_value = xCosMetaMap.get(x_cos_meta_key);
			AssertNotNull("x_cos_meta_value", x_cos_meta_value);
			if (x_cos_meta_value.isEmpty()) {
				throw new ParamException("x-cos-meta value can't be empty!");
			}
		}

	}

	public static void AssertLegalUpdateFlag(int updateFlag) throws ParamException {
		if (updateFlag == 0) {
			throw new ParamException("please update at least one attribute!");
		}
	}

	public static void AssertLegalSliceSize(int sliceSize) throws ParamException {
		if (sliceSize != 512 * 1024 && sliceSize != 1024 * 1024 && sliceSize != 2 * 1024 * 1024
				&& sliceSize != 3 * 1024 * 1024) {
			throw new ParamException("valid slice is 512KB, 1MB, 2MB, 3MB");
		} else {
			
		}
	}
	
}
