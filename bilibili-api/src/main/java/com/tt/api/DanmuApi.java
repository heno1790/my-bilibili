package com.tt.api;

import com.tt.api.support.UserSupport;
import com.tt.domain.Danmu;
import com.tt.domain.JsonResponse;
import com.tt.service.DanmuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName: DanmuApi
 * Package: com.tt.api
 * Description:
 *
 * @Create 2025/6/18 16:25
 */
@RestController
public class DanmuApi {
    @Autowired
    private DanmuService danmuService;
    @Autowired
    private UserSupport userSupport;

    //弹幕查询的api,用户和游客有不同的查询权限
    @GetMapping("/danmus")
    public JsonResponse<List<Danmu>> getDanmus(@RequestParam Long videoId,
                                               String startTime,
                                               String endTime) throws Exception {
        List<Danmu> list;//初始化一个空的弹幕列表,后面再往里加
        Map<String, Object> params = new HashMap<>();
        params.put("videoId", videoId);
        //service层会编写具体的查弹幕逻辑，优先去redis查，没办法了才去查数据库。
        try {
            userSupport.getCurrentUserId();
            params.put("startTime", startTime);
            params.put("endTime", endTime);
            list = danmuService.getDanmus(params);
        } catch (Exception e) {
            //游客模式下，不允许按照时间来查弹幕
            params.put("startTime", null);
            params.put("endTime", null);
            list = danmuService.getDanmus(params);
        }
        return new JsonResponse<>(list);
    }
}
