package com.qcloud.cos.sign;

import java.util.concurrent.ThreadLocalRandom;

import com.qcloud.cos.common_utils.CommonCodecUtils;
import com.qcloud.cos.common_utils.CommonPathUtils;
import com.qcloud.cos.exception.AbstractCosException;
import com.qcloud.cos.exception.UnknownException;

/**
 * @author chengwu 封装签名类，包括单次，多次以及下载签名
 */
public class Sign {

	/**
	 * 返回用户访问资源的签名
	 * 
	 * @param cred
	 *            包含用户秘钥信息
	 * @param bucketName
	 *            bucket名
	 * @param cosPath
	 *            要签名的cos路径
	 * @param expired
	 *            超时时间
	 * @param uploadFlag
	 *            除了生成下载签名，其他情况updateFlag皆为true
	 * @return 返回base64编码的字符串
	 * @throws AbstractCosException
	 */
	private static String appSignatureBase(Credentials cred, String bucketName, String cosPath, long expired,
			boolean uploadFlag) throws AbstractCosException {
		long appId = cred.getAppId();
		String secretId = cred.getSecretId();
		String secretKey = cred.getSecretKey();
		long now = System.currentTimeMillis() / 1000;
		int rdm = Math.abs(ThreadLocalRandom.current().nextInt());
		String fileId = null;
		if (uploadFlag) {
			fileId = String.format("/%d/%s%s", appId, bucketName, cosPath);
		} else {
			fileId = cosPath;
		}
		fileId = CommonPathUtils.encodeRemotePath(fileId);
		String plainText = String.format("a=%s&k=%s&e=%d&t=%d&r=%d&f=%s&b=%s", appId, secretId, expired, now, rdm,
				fileId, bucketName);

		byte[] hmacDigest;
		try {
			hmacDigest = CommonCodecUtils.HmacSha1(plainText, secretKey);
		} catch (Exception e) {
			throw new UnknownException(e.getMessage());
		}
		byte[] signContent = new byte[hmacDigest.length + plainText.getBytes().length];
		System.arraycopy(hmacDigest, 0, signContent, 0, hmacDigest.length);
		System.arraycopy(plainText.getBytes(), 0, signContent, hmacDigest.length, plainText.getBytes().length);

		return CommonCodecUtils.Base64Encode(signContent);
	}

	/**
	 * 获取多次签名, 一段时间内有效, 针对上传文件，重命名文件, 创建目录, 获取文件目录属性, 拉取目录列表
	 * 
	 * @param bucketName
	 *            bucket名称
	 * @param cosPath
	 *            要签名的cos路径
	 * @param cred
	 *            用户的身份信息, 包括appid, secret_id和secret_key
	 * @param expired
	 *            签名过期时间, UNIX时间戳。如想让签名在30秒后过期, 即可将expired设成当前时间加上30秒
	 * @return base64编码的字符串
	 * @throws AbstractCosException
	 */
	public static String getPeriodEffectiveSign(String bucketName, String cosPath, Credentials cred, long expired)
			throws AbstractCosException {
		return appSignatureBase(cred, bucketName, cosPath, expired, true);
	}

	/**
	 * 获取单次签名, 一次有效，针对删除和更新文件目录
	 * 
	 * @param bucketName
	 *            bucket名称
	 * @param cosPath
	 *            要签名的cos路径
	 * @param cred
	 *            用户的身份信息, 包括appid, secret_id和secret_key
	 * @return base64编码的字符串
	 * @throws AbstractCosException
	 */
	public static String getOneEffectiveSign(String bucketName, String cosPath, Credentials cred)
			throws AbstractCosException {
		return appSignatureBase(cred, bucketName, cosPath, 0, true);
	}

	/**
	 * 下载签名, 用于获取后拼接成下载链接，下载私有bucket的文件
	 * 
	 * @param bucketName
	 *            bucket名称
	 * @param cosPath
	 *            要签名的cos路径
	 * @param cred
	 *            用户的身份信息, 包括appid, secret_id和secret_key
	 * @param expired
	 *            签名过期时间, UNIX时间戳。如想让签名在30秒后过期, 即可将expired设成当前时间加上30秒
	 * @return base64编码的字符串
	 * @throws AbstractCosException
	 */
	public static String getDownLoadSign(String bucketName, String cosPath, Credentials cred, long expired)
			throws AbstractCosException {
		return appSignatureBase(cred, bucketName, cosPath, expired, false);
	}

}
