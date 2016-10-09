package com.qcloud.cos.request;

import java.util.HashMap;
import java.util.Map;

import com.qcloud.cos.common_utils.CommonParamCheckUtils;
import com.qcloud.cos.exception.ParamException;
import com.qcloud.cos.meta.FileAuthority;

/**
 * @author chengwu 更新文件请求
 *
 */
public class UpdateFileRequest extends AbstractBaseRequest {
    // 用户更新标识，更新bizAttr是0x01, 更新authority0x80, 更新custom_httpheader是0x40
    // 更新多个属性是这些这些值取或
    private int updatFlag = 0;

    // biz_attr属性
    private String bizAttr = "";
    // 权限
    private FileAuthority authority = FileAuthority.INVALID;
    // HTTP Cache-Control属性
    private String cacheControl = "";
    // HTTP Content-Type属性
    private String contentType = "";
    // HTTP Content-Disposition属性
    private String contentDisposition = "";
    // HTTP Content-Language属性
    private String contentLanguage = "";
    // HTTP Content-Encoding属性
    private String contentEncoding = "";
    // 自定义http头
    private Map<String, String> customHeaders = new HashMap<>();
    // 自定义http头, key为x-cos-meta-开头, value为字符串
    private Map<String, String> xCosMetaHeaders = new HashMap<>();

    public UpdateFileRequest(String bucketName, String cosPath) {
        super(bucketName, cosPath);
    }

    public int getUpdateFlag() {
        return updatFlag;
    }

    public String getBizAttr() {
        return bizAttr;
    }

    public void setBizAttr(String bizAttr) {
        this.bizAttr = bizAttr;
        this.updatFlag |= 0x01;
    }

    public FileAuthority getAuthority() {
        return authority;
    }

    public void setAuthority(FileAuthority authority) {
        this.authority = authority;
        this.updatFlag |= 0x80;
    }

    public Map<String, String> getCustomHeaders() {
        return customHeaders;
    }

    public void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
        this.updatFlag |= 0x40;
        this.customHeaders.put("Cache-Control", cacheControl);
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
        this.updatFlag |= 0x40;
        this.customHeaders.put("Content-Type", contentType);
    }

    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
        this.updatFlag |= 0x40;
        this.customHeaders.put("Content-Disposition", contentDisposition);
    }

    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
        this.updatFlag |= 0x40;
        this.customHeaders.put("Content-Language", contentLanguage);
    }


    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        this.updatFlag |= 0x40;
        this.customHeaders.put("Content-Encoding", contentEncoding);
    }

    public void setXCosMeta(String key, String value) {
        this.xCosMetaHeaders.put(key, value);
        this.customHeaders.put(key, value);
        this.updatFlag |= 0x40;
    }

    @Override
    public void check_param() throws ParamException {
        super.check_param();
        CommonParamCheckUtils.AssertLegalCosFilePath(this.getCosPath());
        CommonParamCheckUtils.AssertLegalUpdateFlag(this.updatFlag);
        CommonParamCheckUtils.AssertNotNull("biz_attr", this.bizAttr);
        CommonParamCheckUtils.AssertNotNull("authority", this.authority);
        CommonParamCheckUtils.AssertNotNull("cacheControl", this.cacheControl);
        CommonParamCheckUtils.AssertNotNull("contentType", this.contentType);
        CommonParamCheckUtils.AssertNotNull("contentDisposition", this.contentDisposition);
        CommonParamCheckUtils.AssertNotNull("contentLanguage", this.contentLanguage);
        CommonParamCheckUtils.AssertNotNull("contentEncoding", this.contentEncoding);
        CommonParamCheckUtils.AssertLegalXCosMeta(this.xCosMetaHeaders);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", biz_attr:").append(getMemberStringValue(bizAttr));
        sb.append(", authority:").append(this.authority);
        sb.append(", cacheControl:").append(getMemberStringValue(this.cacheControl));
        sb.append(", contentType:").append(getMemberStringValue(this.contentType));
        sb.append(", contentDisposition:").append(getMemberStringValue(this.contentDisposition));
        sb.append(", contentLanguage:").append(getMemberStringValue(this.contentLanguage));
        sb.append(", contentEncoding:").append(getMemberStringValue(this.contentEncoding));
        for (String key : this.xCosMetaHeaders.keySet()) {
            sb.append(", x_cos_meta_key:").append(getMemberStringValue(key));
            sb.append(", x_cos_meta_value:")
                    .append(getMemberStringValue(this.xCosMetaHeaders.get(key)));
        }
        sb.append(", authority:");
        if (this.authority == null) {
            sb.append("null");
        } else {
            sb.append(this.authority);
        }
        return sb.toString();
    }
}
