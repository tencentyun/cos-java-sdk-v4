package com.qcloud.cos;

import java.io.InputStream;

import com.qcloud.cos.request.CreateFolderRequest;
import com.qcloud.cos.request.DelFileRequest;
import com.qcloud.cos.request.DelFolderRequest;
import com.qcloud.cos.request.GetFileInputStreamRequest;
import com.qcloud.cos.request.GetFileLocalRequest;
import com.qcloud.cos.request.ListFolderRequest;
import com.qcloud.cos.request.MoveFileRequest;
import com.qcloud.cos.request.StatFileRequest;
import com.qcloud.cos.request.StatFolderRequest;
import com.qcloud.cos.request.UpdateFileRequest;
import com.qcloud.cos.request.UpdateFolderRequest;
import com.qcloud.cos.request.UploadFileRequest;
import com.qcloud.cos.request.UploadSliceFileRequest;

/**
 * @author chengwu
 * COS提供给用户使用的API接口
 */

public interface COS {

	/**
	 * 上传文件请求, 对小文件(8MB以下)使用单文件上传接口, 大文件使用分片上传接口, 推荐使用
	 * 
	 * @param request
	 *            上传文件请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 */
    String uploadFile(UploadFileRequest request);
    
	/**
	 * 上传单文件请求, 不分片,优先推荐使用uploadFile接口
	 * 
	 * @param request
	 *            上传文件请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 */ 
    String uploadSingleFile(UploadFileRequest request);

	/**
	 * 分片上传文件
	 * 
	 * @param request
	 *            分片上传请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":$mess}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 */         
    String uploadSliceFile(UploadSliceFileRequest request);
    
	/**
	 * 获取文件属性
	 * 
	 * @param request
	 *            获取文件属性请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
     */
    String statFile(StatFileRequest request);
    
	/**
	 * 更新文件属性
	 * 
	 * @param request
	 *            更新文件属性请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
     */
    String updateFile(UpdateFileRequest request);
    
    /**                                                                                             
     * 移动文件                                                                                     
     *                                                                                              
     * @param request                                                                               
     *            移动文件请求                                                                      
     * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,          
     *         其他为失败, message为success或者失败原因                                             
     */                                                                                             
    String moveFile(MoveFileRequest request);
    
	/**
	 * 删除文件
	 * 
	 * @param request
	 *            删除文件请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 */
	String delFile(DelFileRequest request);



	/**
	 * 下载文件到本地
	 * 
	 * @param request
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 */
	String getFileLocal(GetFileLocalRequest request);

	/**
	 * 下载文件并得到下载流
	 * 
	 * @param request
	 * @return 下载输入流
	 */
	InputStream getFileInputStream(GetFileInputStreamRequest request) throws Exception;

	/**
	 * 创建目录
	 * 
	 * @param request
	 *            创建目录请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
     */
    String createFolder(CreateFolderRequest request);	
    
	/**
	 * 更新目录属性
	 * 
	 * @param request
	 *            更新目录属性请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
     */
    String updateFolder(UpdateFolderRequest request);

	/**
	 * 获取目录属性请求
	 * 
	 * @param request
	 *            获取目录属性请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
     */
    String statFolder(StatFolderRequest request);

	/**
	 * 获取目录列表请求
	 * 
	 * @param request
	 *            获取目录列表请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
     */
    String listFolder(ListFolderRequest request);
    
	/**
	 * 删除目录请求
	 * 
	 * @param request
	 *            删除目录请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
     */
    String delFolder(DelFolderRequest request);   
    
    /**
     * 关闭COS客户端连接池，释放涉及的资源，释放后，不能再使用COS的接口，必须重新生成一个新对象
     */
    void shutdown();

}
