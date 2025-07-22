package com.tt.service.util;

import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tt.domain.exception.ConditionException;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * ClassName: FastDFSUtil
 * Package: com.tt.service.util
 * Description:
 *
 * @Create 2025/6/9 11:24
 */
@Component
public class FastDFSUtil {
    //上传普通文件到fastDFS服务器的依赖bean
    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    //上传较大文件到fastDFS服务器的依赖bean(内含断点续传相关的api)
    @Autowired
    private AppendFileStorageClient appendFileStorageClient;

    //断点续传api功能需用到redis数据库,故引入相关依赖bean
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    //获取fdfs服务器资源的http请求前缀
    @Value("${fdfs.http.storage-addr}")
    private String fdfsStorageAddrPrefix;

    //上传文件到storage服务器时,默认上传到的服务器组别为group1
    private static final String DEFAULT_GROUP = "group1";

    //断点续传方法中涉及到的三个redis键名静态参数
    private static final String PATH_KEY = "path-key:";
    private static final String UPLOADED_SIZE_KEY = "uploaded-size-key:";
    private static final String UPLOADED_NO_KEY = "uploaded-no-key:";
    //文件分片的默认大小
    private static final int SLICE_SIZE = 1024 * 1024 * 5;  //20MB


    public String getFileType(MultipartFile file) {
        // 获取文件后缀名
        if (file == null) {
            throw new ConditionException("文件不存在!");
        }
        String fileName = file.getOriginalFilename();
        int index = fileName.lastIndexOf(".");
        return fileName.substring(index + 1);
    }

    //上传
    public String uploadCommonFile(MultipartFile file) throws Exception {
        //存储文件属性的set，比如所有者，创建时间，读写权限等
        Set<MetaData> metaDataSet = new HashSet<>();
        String fileType = this.getFileType(file);
        //StorePath:文件上传成功后在服务器中的路径信息，FastDFS封装好的类
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), fileType, metaDataSet);
        return storePath.getPath();  //上传成功后返回其在DFS服务器中的相对路径
    }

    //删除
    public void deleteFile(String filePath) {
        fastFileStorageClient.deleteFile(filePath);
    }

    //上传文件第一片，file是已经分片完成的文件
    public String uploadAppenderFile(MultipartFile file) throws Exception {
        String fileType = this.getFileType(file);
        //先上传断点续传文件第一片的内容，并返回上传成功的第一片文件在服务器中的位置信息
        StorePath storePath = appendFileStorageClient.uploadAppenderFile(DEFAULT_GROUP, file.getInputStream(), file.getSize(), fileType);
        return storePath.getPath();
    }

    //追加上传文件的第二片之后内容
    public void modifyAppenderFile(MultipartFile file, String filePath, long offset) throws Exception {
        //需要的传参有:上传到第几组,上传到的路径，上传的文件内容和大小，要上传的file在服务器中已有文件的追加位置(即偏移量)
        appendFileStorageClient.modifyFile(DEFAULT_GROUP, filePath, file.getInputStream(), file.getSize(), offset);
    }

    /**
     * @param file,当前文件分片对象
     * @param fileMd5,文件内容进行md5加密后形成的唯一标识符字符串,本api中被作为rediskey的一部分,后续还可被用于秒传功能的开发
     * @param sliceNo,当前上传的分片的序号
     * @param totalSliceNo,整个文件总共包含的分片数
     * @return String, 返回上传成功的文件在服务器中的路径, 如果所有分片上传完成, 则返回的是完整文件的路径, 否则返回空字符串
     */
    public String uploadFileBySlices(MultipartFile file, String fileMd5, Integer sliceNo, Integer totalSliceNo) throws Exception {
        if (file == null || sliceNo == null || totalSliceNo == null) {
            throw new ConditionException("参数异常!");
        }
        //当前文件路径值的完整键名,fileMd5是完整文件经md5加密后的字符串?后续再看看
        String pathKey = PATH_KEY + fileMd5;
        //当前存在于服务器上的(已上传完成的部分)的文件的大小值的键名
        String uploadedSizeKey = UPLOADED_SIZE_KEY + fileMd5;
        //已经上传成功的文件部分的最大片序号的值的键名
        String uploadedNoKey = UPLOADED_NO_KEY + fileMd5;
        //从redis中获取已上传完成的文件大小值，若还没上传，则为空
        String uploadedSizeStr = redisTemplate.opsForValue().get(uploadedSizeKey);
        Long uploadedSize = 0L;  //存储已上传的文件的大小值的变量
        if (!StringUtil.isNullOrEmpty(uploadedSizeStr)) {
            uploadedSize = Long.valueOf(uploadedSizeStr);
        }
        if (sliceNo == 1) { //若上传的是第一个分片
            String path = this.uploadAppenderFile(file); //上传分片1并返回其在服务器中存储的路径
            if (StringUtil.isNullOrEmpty(path)) {
                throw new ConditionException("上传失败");
            }
            //能走到这,说明至少第一片是上传成功的,文件在服务器中的路径还是能获得的
            redisTemplate.opsForValue().set(pathKey, path);
            redisTemplate.opsForValue().set(uploadedNoKey, "1");
        }
        //当前在上传的文件分片不是第一片的情况
        else {
            String filePath = redisTemplate.opsForValue().get(pathKey);
            //明明不是第一片,却找不到第一片在服务器上的路径,于是抛异常
            if (StringUtil.isNullOrEmpty(filePath)) {
                throw new ConditionException("上传失败，找不到文件路径");
            }
            //偏移量就是服务器中已有的文件的大小,也就是新分片的追加位置
            this.modifyAppenderFile(file, filePath, uploadedSize);
            redisTemplate.opsForValue().increment(uploadedNoKey); //已上传分片数量+1
        }
        uploadedSize += file.getSize(); //更新已加载的文件大小（偏移量）
        //更新redis中的对应键值
        redisTemplate.opsForValue().set(uploadedSizeKey, String.valueOf(uploadedSize));
        //当前分片上传完成了,判断一下是不是所有的分片都上传完成了
        String uploadedNoStr = redisTemplate.opsForValue().get(uploadedNoKey);
        Integer uploadedNo = Integer.valueOf(uploadedNoStr);
        String resultPath = "";
        if (uploadedNo.equals(totalSliceNo)) { //已上传分片数等于总分片数,说明所有分片都上传完成了
            // 目标路径，如果前端接收到不为空的目标路径,说明所有分片路径都上传完成了
            resultPath = redisTemplate.opsForValue().get(pathKey);
            //把redis中存储的三个键名组成一个列表
            List<String> keyList = Arrays.asList(uploadedNoKey, pathKey, uploadedSizeKey);
            //redis缓存一次性删除列表中所有的键名所对应的键值对
            redisTemplate.delete(keyList);
        }
        return resultPath;
    }

    // 文件分片，实际上是由前端完成这个工作
    public void convertFileToSlices(MultipartFile multipartFile) throws IOException {
        //获取文件信息并转为file类型
        String fileName = multipartFile.getOriginalFilename();
        String fileType = this.getFileType(multipartFile);
        File file = this.multipartFileToFile(multipartFile);

        long fileLength = file.length();
        int count = 1;//分片的计数标识
        for (int i = 0; i < fileLength; i += SLICE_SIZE) {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r"); //只有读权限
            randomAccessFile.seek(i); //设置随机读取器的读取位置
            byte[] bytes = new byte[SLICE_SIZE];
            //将随机读取器的内容读进byte数组
            int len = randomAccessFile.read(bytes);
            //设置临时分片文件在客户端的存放位置
            String path = "F:/DFS_temp/" + count + "." + fileType;
            //承载分片的文件对象
            File slice = new File(path);
            FileOutputStream fos = new FileOutputStream(slice);
            fos.write(bytes, 0, len);
            fos.close();
            randomAccessFile.close();
            count++;
        }
        //分片完成，删除临时文件
        file.delete();
    }

    //将MultipartFile类型的文件转为File类型
    public File multipartFileToFile(MultipartFile multipartFile) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();
        //将文件名按照"."进行分割,得到一个字符串数组,数组的第一个元素是文件名,第二个元素是文件后缀名
        String[] fileName = originalFileName.split("\\.");
        //创建一个临时文件用于承接multipartFile
        File file = File.createTempFile(fileName[0], "." + fileName[1]);
        multipartFile.transferTo(file);
        return file;
    }

    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String path) throws Exception {
        //从FastDFS存储服务器查询视频文件的元数据
        FileInfo fileInfo = fastFileStorageClient.queryFileInfo(DEFAULT_GROUP, path);
        long totalFileSize = fileInfo.getFileSize();
        //拼接出完整的http资源请求路径
        String url = fdfsStorageAddrPrefix + path;
        //将客户端原始请求的所有请求头（如 User-Agent、Range 等）复制到 headers Map中，以便转发给FastDFS服务器。
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, Object> headers = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            headers.put(header, request.getHeader(header));
        }
        //解析Range请求头，range字段中包含了分片的起始位置和结束位置
        String rangeStr = request.getHeader("Range");
        String[] range;
        if (StringUtil.isNullOrEmpty(rangeStr)) {
            rangeStr = "bytes=0-" + (totalFileSize - 1);
        }
        range = rangeStr.split("bytes=|-");
        long begin = 0;
        //长度至少为2,说明请求头中含分片的起始位置
        if (range.length >= 2) {
            begin = Long.parseLong(range[1]);
        }
        long end = totalFileSize - 1;
        //长度至少为3，说明请求头中也包含分片的结束位置
        if (range.length >= 3) {
            end = Long.parseLong(range[2]);
        }
        long len = end - begin + 1;

        // 设置HTTP响应头
        String contentRange = "bytes " + begin + "-" + end + "/" + totalFileSize;
        response.setHeader("Content-Range", contentRange);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Type", "video/mp4");
        response.setContentLength((int) len);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        //转发完整的请求给DFS服务器，并提前为DFS做好一些响应头的设置
        HttpUtil.get(url, headers, response);
    }
}
