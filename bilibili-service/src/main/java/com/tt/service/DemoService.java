package com.tt.service;

import com.tt.dao.DemoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * ClassName: DemoService
 * Package: com.tt.service
 * Description:
 *
 * @Create 2025/3/14 19:52
 */
@Service
public class DemoService {
    @Autowired
    private DemoDao demoDao;
    public Map<String, Object> query(Long id) {
        return demoDao.query(id);
    }
}
