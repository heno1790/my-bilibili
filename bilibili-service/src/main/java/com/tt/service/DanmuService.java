package com.tt.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tt.dao.DanmuDao;
import com.tt.domain.Danmu;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ClassName: DanmuService
 * Package: com.tt.service
 * Description:
 *
 * @Create 2025/6/17 21:53
 */
@Service
public class DanmuService {
    @Autowired
    private DanmuDao danmuDao;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String DANMU_KEY = "danmu-video-";

    //添加弹幕
    public void addDanmu(Danmu danmu) {
        danmuDao.addDanmu(danmu);
    }

    //异步方式添加弹幕
    @Async
    public void asyncAddDanmu(Danmu danmu) {
        danmuDao.addDanmu(danmu);
    }

    //根据视频id和弹幕的创建时间信息(筛选条件来的)(若Map中有相关的合法传参)查询弹幕
    //service层负责编写具体的查弹幕逻辑，优先去redis查，没办法了才去查数据库。
    public List<Danmu> getDanmus(Map<String, Object> params) throws ParseException {
        //先从redis处获取所有的本视频的弹幕信息
        Long videoId = (Long) params.get("videoId");
        String startTime = (String) params.get("startTime");
        String endTime = (String) params.get("endTime");
        String key = DANMU_KEY + videoId;
        String value = redisTemplate.opsForValue().get(key);
        List<Danmu> list;
        //redis中有,就从redis里查,先不考虑redis里面未命中等问题，后期继续优化时再说
        if (!StringUtil.isNullOrEmpty(value)) {
            list = JSONArray.parseArray(value, Danmu.class);
            if (!StringUtil.isNullOrEmpty(startTime) && !StringUtil.isNullOrEmpty(endTime)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date startDate = sdf.parse(startTime);
                Date endDate = sdf.parse(endTime);
                List<Danmu> childList = new ArrayList<>();
                for (Danmu danmu : list) {
                    Date createTime = danmu.getCreateTime();
                    if (createTime.after(startDate) && createTime.before(endDate)) {
                        childList.add(danmu);
                    }
                }
                list=childList;
            }
        }
        else {
            list = danmuDao.getDanmus(params);
            //将弹幕信息保存到redis,这里有bug,那就是缓存中可能只存了部分时间段的数据,但用户可能要查本视频的全部弹幕,这时候就会查不全
            redisTemplate.opsForValue().set(key, JSONObject.toJSONString(list));
        }
        return list;
    }

    //将新弹幕添加到redis缓存中,方便客户端查询时快速获取弹幕
    public void addDanmusToRedis(Danmu danmu) {
        String key = DANMU_KEY + danmu.getVideoId();
        String value = redisTemplate.opsForValue().get(key);
        List<Danmu> list = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(value)) {
            list = JSONArray.parseArray(value, Danmu.class);
        }
        list.add(danmu);
        System.out.println("redis中弹幕时间信息为:" + danmu.getCreateTime());
        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(list));
    }
}
