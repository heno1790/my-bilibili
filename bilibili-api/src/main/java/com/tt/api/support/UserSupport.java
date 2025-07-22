package com.tt.api.support;

import com.tt.domain.exception.ConditionException;
import com.tt.service.util.TokenUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * ClassName: UserSupport
 * Package: com.tt.com.tt.api.support
 * Description:
 *
 * @Create 2025/3/18 16:37
 */
@Component
public class UserSupport {
    public Long getCurrentUserId() {
        //获取前端请求并强制类型转换为ServletRequestAttributes
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String token = requestAttributes.getRequest().getHeader("token");
        //先校验,后查询,校验不通过的话,才查数据库,提升系统性能
        Long userId = TokenUtil.verifyToken(token);
        if (userId < 0) {
            throw new ConditionException("accessToken,前端收到此异常后，请代替用户向本服务端调用刷新token接口" +
                    "尝试更新accessToken,若更新成功，再用accessToken来登录!");
        }
        return userId;
    }
}
