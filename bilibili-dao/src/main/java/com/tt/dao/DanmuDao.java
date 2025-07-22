package com.tt.dao;

import com.tt.domain.Danmu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * ClassName: DanmuDao
 * Package: com.tt.dao
 * Description:
 *
 * @Create 2025/6/17 21:57
 */
@Mapper
public interface DanmuDao {
    void addDanmu(Danmu danmu);

    List<Danmu> getDanmus(Map<String, Object> params);
}
