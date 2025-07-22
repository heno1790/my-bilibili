package com.tt.api.aspect;

import com.tt.api.support.UserSupport;
import com.tt.domain.annotation.ApiLimitedRole;
import com.tt.domain.auth.UserRole;
import com.tt.domain.exception.ConditionException;
import com.tt.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ClassName: ApiLimitedRoleAspect
 * Package: com.tt.api.aspect
 * Description:
 *
 * @Create 2025/4/10 14:26
 */
@Order(1)
@Component
@Aspect
public class ApiLimitedRoleAspect {
    @Autowired
    private UserSupport userSupport;
    @Autowired
    private UserRoleService userRoleService;

    //定义切点：所有被@ApiLimitedRole注解标记的方法都会被拦截。
    @Pointcut("@annotation(com.tt.domain.annotation.ApiLimitedRole)")
    public void check() {}

    //前置通知：在目标方法执行前触发，并通过@annotation(apiLimitedRole)获取方法上的注解实例。
    @Before("check() && @annotation(apiLimitedRole)")
    //JoinPoint是Spring AOP中封装目标方法上下文信息的对象，通过它可以获取方法签名、参数、目标对象等元数据
    public void doBefore(JoinPoint joinPoint, ApiLimitedRole apiLimitedRole) {
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId); //实际上根据id只能查出一个UserRole
        //注解中定义的,需要限制接口访问的角色代码列表
        String[] limitedRoleCodeList = apiLimitedRole.limitedRoleCodeList();
        //用set去重 两个set的元素（Lv0）对应数据库中auth_role表的code字段
        Set<String> limitedRoleCodeSet = Arrays.stream(limitedRoleCodeList).collect(Collectors.toSet());
        Set<String> userRoleSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        //取交集，如果用户的roleCode和限制表的roleCode有重合，则代表这个用户被限制了
        userRoleSet.retainAll(limitedRoleCodeSet);
        if (!userRoleSet.isEmpty()) {
            throw new ConditionException("用户所属角色无权访问该接口!");
        }
    }
}
