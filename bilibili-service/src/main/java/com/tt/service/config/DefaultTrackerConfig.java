package com.tt.service.config;

import com.github.tobato.fastdfs.domain.fdfs.StorageNode;
import com.github.tobato.fastdfs.domain.fdfs.StorageNodeInfo;
import com.github.tobato.fastdfs.service.DefaultTrackerClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * ClassName: DefaultTrackerConfig
 * Package: com.tt.service.config
 * Description:老版本fastDFS存在bug,storage的默认端口为0而不是linux中的23000,所以写个配置类修改一下
 *
 * @Create 2025/6/11 21:06
 */
@Primary
@Component
public class DefaultTrackerConfig extends DefaultTrackerClient {
    @Override
    public StorageNode getStoreStorage(String groupName) {
        StorageNode result = super.getStoreStorage(groupName);
        result.setPort(23000);
        return result;
    }

    @Override
    public StorageNodeInfo getUpdateStorage(String groupName, String filename) {
        StorageNodeInfo result = super.getUpdateStorage(groupName, filename);
        result.setPort(23000);
        return result;
    }

    // 覆盖用于下载文件的方法
    @Override
    public StorageNodeInfo getFetchStorage(String groupName, String filename) {
        StorageNodeInfo result = super.getFetchStorage(groupName, filename);
        result.setPort(23000);
        return result;
    }
}
