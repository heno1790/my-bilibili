package com.tt.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * ClassName: DemoDao
 * Package: com.tt
 * Description:
 *
 * @Create 2025/3/12 17:05
 */

@Mapper
public interface DemoDao {
    public Map<String, Object> query(Long id);
}
