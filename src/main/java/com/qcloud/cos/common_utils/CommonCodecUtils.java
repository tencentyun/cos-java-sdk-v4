package com.qcloud.cos.common_utils;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcloud.cos.http.RequestBodyKey;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author chengwu 封装了常用的MD5、SHA1、HmacSha1函数
 */
public class CommonCodecUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CommonCodecUtils.class);

    private static final String HMAC_SHA1 = "HmacSHA1";

    /**
     * 对二进制数据进行BASE64编码
     * 
     * @param binaryData 二进制数据
     * @return 编码后的字符串
     */
    public static String Base64Encode(byte[] binaryData) {
        String encodedstr = new String(Base64.encodeBase64(binaryData, false), Charsets.UTF_8);
        return encodedstr;
    }

    /**
     * 获取buffer内容的sha1
     * 
     * @param contentBuffer 要计算sha1的buffer
     * @return 编码后的字符串
     * @throws Exception
     */
    public static String getBufferSha1(byte[] contentBuffer) throws Exception {
        return DigestUtils.sha1Hex(contentBuffer);
    }

    /**
     * 获取整个文件的SHA1
     * 
     * @param fileInputStream 文件的输入流
     * @return 文件对应的SHA1值
     * @throws Exception
     */
    public static String getEntireFileSha1(String filePath) throws Exception {
        InputStream fileInputStream = null;
        try {
            fileInputStream = CommonFileUtils.getFileInputStream(filePath);
            String sha1Digest = DigestUtils.sha1Hex(fileInputStream);
            return sha1Digest;
        } catch (Exception e) {
            String errMsg = "getFileSha1 occur a exception, file:" + filePath + ", exception:"
                    + e.toString();
            LOG.error(errMsg);
            throw new Exception(errMsg);
        } finally {
            try {
                CommonFileUtils.closeFileStream(fileInputStream, filePath);
            } catch (Exception e) {
                throw e;
            }
        }
    }

    /**
     * 获取分片的sha1, 并以JSON数组字符串的形式返回，每一个成员都是分片的sha信息
     * 除最后一片外，每一片的sha信息都是中间状态,即sha算法update后的五个常量值的十六进制字符串 因此最后一片的sha值和全文sha是一样的
     * 
     * @param localPath 本地文件路径
     * @param sliceSize 分片大小
     * @param entireSha1Builder 存储全文sha的对象
     * @return 返回分片sha的JSON格式的字符串
     * @throws Exception
     */
    public static String getSlicePartSha1(String localPath, int sliceSize,
            StringBuilder entireSha1Builder) throws Exception {
        // 超过1M的按照1M来计算sha
        if (sliceSize > 1024 * 1024) {
            sliceSize = 1024 * 1024;
        }

        JSONArray jsonArray = new JSONArray();
        InputStream fileInput = null;
        try {
            CommonSha1Utils sha1Utils = new CommonSha1Utils();
            sha1Utils.init();

            fileInput = CommonFileUtils.getFileInputStream(localPath);
            long fileLength = CommonFileUtils.getFileLength(localPath);
            int sliceCount = new Long((fileLength + (sliceSize - 1)) / sliceSize).intValue();

            final int BUFFER_LEN = 1024;
            byte[] contentBuf = null;

            // 先求第一片到倒数第二片的sha信息，这些片的大小都是sliceSize
            for (int sliceIndex = 0; sliceIndex < sliceCount - 1; ++sliceIndex) {
                int totalCount = 0;
                long sliceOffset = sliceIndex;
                sliceOffset *= sliceSize;

                while (totalCount < sliceSize) {
                    int maxRead = sliceSize - totalCount;
                    if (maxRead > BUFFER_LEN) {
                        maxRead = BUFFER_LEN;
                    }
                    contentBuf = new byte[maxRead];
                    fileInput.read(contentBuf, 0, maxRead);
                    sha1Utils.update(contentBuf);
                    totalCount += maxRead;
                }

                JSONObject sliceJson = new JSONObject();
                sliceJson.put(RequestBodyKey.UploadParts.OFFSET, sliceOffset);
                sliceJson.put(RequestBodyKey.UploadParts.DATA_LEN, totalCount);
                sliceJson.put(RequestBodyKey.UploadParts.DATA_SHA, sha1Utils.dumpTempState());
                jsonArray.put(sliceIndex, sliceJson);
            }

            // 求最后一片的sha信息
            long sliceOffset = (sliceCount - 1) * (long)sliceSize;
            int leftSlice = new Long(fileLength - sliceOffset).intValue();
            int totalCount = 0;
            while (totalCount < leftSlice) {
                int maxRead = leftSlice - totalCount;
                if (maxRead > BUFFER_LEN) {
                    maxRead = BUFFER_LEN;
                }
                contentBuf = new byte[maxRead];
                fileInput.read(contentBuf, 0, maxRead);
                sha1Utils.update(contentBuf);
                totalCount += maxRead;
            }
            sha1Utils.finish();

            entireSha1Builder.append(sha1Utils.digout());

            JSONObject sliceJson = new JSONObject();
            sliceJson.put(RequestBodyKey.UploadParts.OFFSET, sliceOffset);
            sliceJson.put(RequestBodyKey.UploadParts.DATA_LEN, totalCount);
            sliceJson.put(RequestBodyKey.UploadParts.DATA_SHA, sha1Utils.digout());
            jsonArray.put(sliceCount - 1, sliceJson);

        } catch (Exception e) {
            LOG.error("getSlicePartSha1 occur a error, filePath:{}, sliceSize:{}, exception:{}",
                    localPath, sliceSize, e.toString());
            throw e;
        } finally {
            CommonFileUtils.closeFileStream(fileInput, localPath);
        }
        return jsonArray.toString();
    }

    public static String getSlicePartSha1(byte[] contentBuffer, int sliceSize,
            StringBuilder entireSha1Builder) throws Exception {
        // 超过1M的按照1M来计算sha
        if (sliceSize > 1024 * 1024) {
            sliceSize = 1024 * 1024;
        }

        JSONArray jsonArray = new JSONArray();
        InputStream fileInput = null;
        try {
            CommonSha1Utils sha1Utils = new CommonSha1Utils();
            sha1Utils.init();

            fileInput = new ByteArrayInputStream(contentBuffer);
            long fileLength = contentBuffer.length;
            int sliceCount = new Long((fileLength + (sliceSize - 1)) / sliceSize).intValue();

            final int BUFFER_LEN = 1024;
            byte[] contentBuf = null;

            // 先求第一片到倒数第二片的sha信息，这些片的大小都是sliceSize
            for (int sliceIndex = 0; sliceIndex < sliceCount - 1; ++sliceIndex) {
                int totalCount = 0;
                long sliceOffset = sliceIndex * (long)sliceSize;

                while (totalCount < sliceSize) {
                    int maxRead = sliceSize - totalCount;
                    if (maxRead > BUFFER_LEN) {
                        maxRead = BUFFER_LEN;
                    }
                    contentBuf = new byte[maxRead];
                    fileInput.read(contentBuf, 0, maxRead);
                    sha1Utils.update(contentBuf);
                    totalCount += maxRead;
                }

                JSONObject sliceJson = new JSONObject();
                sliceJson.put(RequestBodyKey.UploadParts.OFFSET, sliceOffset);
                sliceJson.put(RequestBodyKey.UploadParts.DATA_LEN, totalCount);
                sliceJson.put(RequestBodyKey.UploadParts.DATA_SHA, sha1Utils.dumpTempState());
                jsonArray.put(sliceIndex, sliceJson);
            }

            // 求最后一片的sha信息
            long sliceOffset = (sliceCount - 1) * (long)sliceSize;
            int leftSlice = new Long(fileLength - sliceOffset).intValue();
            int totalCount = 0;
            while (totalCount < leftSlice) {
                int maxRead = leftSlice - totalCount;
                if (maxRead > BUFFER_LEN) {
                    maxRead = BUFFER_LEN;
                }
                contentBuf = new byte[maxRead];
                fileInput.read(contentBuf, 0, maxRead);
                sha1Utils.update(contentBuf);
                totalCount += maxRead;
            }
            sha1Utils.finish();

            entireSha1Builder.append(sha1Utils.digout());

            JSONObject sliceJson = new JSONObject();
            sliceJson.put(RequestBodyKey.UploadParts.OFFSET, sliceOffset);
            sliceJson.put(RequestBodyKey.UploadParts.DATA_LEN, totalCount);
            sliceJson.put(RequestBodyKey.UploadParts.DATA_SHA, sha1Utils.digout());
            jsonArray.put(sliceCount - 1, sliceJson);

        } catch (Exception e) {
            LOG.error("getSlicePartSha1 from buffer occur a error, sliceSize:{}, exception:{}",
                    sliceSize, e.toString());
            throw e;
        } finally {
            if (fileInput != null) {
                try {
                    fileInput.close();
                } catch (IOException e) {
                }
            }
        }

        return jsonArray.toString();
    }

    /**
     * 计算数据的Hmac值
     * 
     * @param binaryData 二进制数据
     * @param key 秘钥
     * @return 加密后的hmacsha1值
     */
    public static byte[] HmacSha1(byte[] binaryData, String key) throws Exception {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1);
            mac.init(secretKey);
            byte[] HmacSha1Digest = mac.doFinal(binaryData);
            return HmacSha1Digest;

        } catch (NoSuchAlgorithmException e) {
            LOG.error("mac not find algorithm {}", HMAC_SHA1);
            throw e;
        } catch (InvalidKeyException e) {
            LOG.error("mac init key {} occur a error {}", key, e.toString());
            throw e;
        } catch (IllegalStateException e) {
            LOG.error("mac.doFinal occur a error {}", e.toString());
            throw e;
        }
    }

    /**
     * 计算数据的Hmac值
     * 
     * @param plainText 文本数据
     * @param key 秘钥
     * @return 加密后的hmacsha1值
     */
    public static byte[] HmacSha1(String plainText, String key) throws Exception {
        return HmacSha1(plainText.getBytes(), key);
    }
}
