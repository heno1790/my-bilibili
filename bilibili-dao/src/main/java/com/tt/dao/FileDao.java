package com.tt.dao;

import com.tt.domain.File;
import org.apache.ibatis.annotations.Mapper;

;

/**
 * ClassName: FileDao
 * Package: com.tt.dao
 * Description:
 *
 * @Create 2025/6/9 21:26
 */
@Mapper
public interface FileDao {
    //将第一次上传的文件的md5字符流信息等上传到数据库
    Integer addFile(File file);
    //根据md5字符流去数据库查询有无此份文件的记录
    File getFileByMD5(String md5);
}
