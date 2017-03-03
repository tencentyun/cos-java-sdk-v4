package com.qcloud.cos.request;

import com.qcloud.cos.common_utils.CommonParamCheckUtils;
import com.qcloud.cos.exception.ParamException;

/**
 * @author chengwu 获取目录成员请求
 */
public class ListFolderRequest extends AbstractBaseRequest {

    // 默认获取的最大目录成员数量
    private final int DEFAULT_LIST_NUM = 199;
    private final int DEFAULT_LIST_FLAG = 1;

    private int num = DEFAULT_LIST_NUM;
    private int listFlag = DEFAULT_LIST_FLAG;
    private String context = "";
    private String prefix = "";
    private String delimiter = "/";

    public ListFolderRequest(String bucketName, String cosPath) {
        super(bucketName, cosPath);
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getListFlag() {
        return listFlag;
    }

    public void setListFlag(int listFlag) {
        this.listFlag = listFlag;
    }
    
    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public void check_param() throws ParamException {
        super.check_param();
        CommonParamCheckUtils.AssertLegalCosFolderPath(getCosPath());
        CommonParamCheckUtils.AssertNotNull("context", this.context);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", num:").append(this.num);
        sb.append(", context:").append(getMemberStringValue(this.context));
        sb.append(", listFlag:").append(this.listFlag);
        sb.append(", prefix:").append(getMemberStringValue(this.prefix));
        return sb.toString();
    }

}
