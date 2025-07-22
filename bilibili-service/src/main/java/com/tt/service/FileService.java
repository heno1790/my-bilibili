package com.tt.service;

import com.tt.dao.FileDao;
import com.tt.domain.File;
import com.tt.service.util.FastDFSUtil;
import com.tt.service.util.MD5Util;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * ClassName: FileService
 * Package: com.tt.service
 * Description:
 *
 * @Create 2025/6/9 21:13
 */
@Service
public class FileService {
    @Autowired
    private FastDFSUtil fastDFSUtil;
    @Autowired
    private FileDao fileDao;

    public String uploadFileBySlices(MultipartFile slice, String fileMd5, Integer sliceNo, Integer totalSliceNo) throws Exception {
        File dbFileMD5 = fileDao.getFileByMD5(fileMd5);
        //若数据库查得到这个文件对应的md5记录
        if (dbFileMD5 != null) {
            return dbFileMD5.getUrl(); //秒传已有的url，秒传功能的实现
        }
        String url = fastDFSUtil.uploadFileBySlices(slice, fileMd5, sliceNo, totalSliceNo);
        //断点续传完成后,return非空的url之前，先把这个新文件的md5字符串写进数据库
        if (!StringUtil.isNullOrEmpty(url)) {
            //主键id不用设置,插入数据库时会自动生成
            File file = new File();
            file.setCreateTime(new Date());
            file.setUrl(url);
            file.setType(fastDFSUtil.getFileType(slice));
            file.setMd5(fileMd5);
            //正式将数据插入数据库
            fileDao.addFile(file);
        }
        return url;
    }

    public String getFileMD5(MultipartFile file) throws Exception {
        return MD5Util.getFileMD5(file);
    }

    public File getFileByMd5(String fileMd5) {
        return fileDao.getFileByMD5(fileMd5);
    }
}


