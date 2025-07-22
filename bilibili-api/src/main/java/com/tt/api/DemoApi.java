package com.tt.api;

import com.tt.service.DemoService;
import com.tt.service.util.FastDFSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * ClassName: DemoApi
 * Package: com.tt.com.tt.api
 * Description:
 *
 * @Create 2025/3/14 19:55
 */
@RestController
public class DemoApi {
    @Autowired
    private DemoService demoService;

    @Autowired
    private FastDFSUtil fastDFSUtil;

    @GetMapping("/qurey")
    public Map<String, Object> query(Long id) {
        return demoService.query(id);
    }

    @GetMapping("/slices")
    public void slices(MultipartFile file) throws IOException {
        fastDFSUtil.convertFileToSlices(file);
    }
}
