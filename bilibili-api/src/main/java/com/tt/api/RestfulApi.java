package com.tt.api;

import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: RESTfulApi
 * Package: com.tt.com.tt.api
 * Description:
 *
 * @Create 2025/3/15 17:40
 */
@RestController
public class RestfulApi {
    private final Map<Integer, Map<String, Object>> dataMap;

    public RestfulApi() {
        dataMap = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", i);
            data.put("name", "name" + i);
            dataMap.put(i, data);
        }
    }

    @GetMapping("objects/{id}")
    public Map<String, Object> getData(@PathVariable Integer id) {
        return dataMap.get(id);
    }

    @DeleteMapping("objects/{id}")
    public String deleteData(@PathVariable Integer id) {
        dataMap.remove(id);
        return "delete success";
    }

    @PostMapping("objects")
    public String postData(@RequestBody Map<String, Object> data) {
        Integer[] idArray = dataMap.keySet().toArray(new Integer[0]);
        Arrays.sort(idArray);
        int nextId = idArray[idArray.length - 1] + 1;
        dataMap.put(nextId, data);
        return "post success";
    }

    @PutMapping("objects")
    public String putData(@RequestBody Map<String, Object> data) {
        //data.get("id") 的返回值类型是 Object,转为字符串再转整数
        Integer id = Integer.valueOf(String.valueOf(data.get("id")));
        Map<String, Object> containedData = dataMap.get(id);
        if (!dataMap.containsKey(id)) {
            Integer[] idArray = dataMap.keySet().toArray(new Integer[0]);
            Arrays.sort(idArray);
            int nextId = idArray[idArray.length - 1] + 1;
            dataMap.put(nextId, data);
        } else { //如果已经包含id，则更新id对应的data
            dataMap.put(id, data);
        }
        return "put success";
    }

}
