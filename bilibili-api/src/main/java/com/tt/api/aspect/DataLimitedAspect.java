package com.tt.api.aspect;

import com.tt.api.support.UserSupport;
import com.tt.domain.UserMoment;
import com.tt.domain.auth.UserRole;
import com.tt.domain.constant.AuthRoleConstant;
import com.tt.domain.exception.ConditionException;
import com.tt.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ClassName: DataLimitedAspect
 * Package: com.tt.api.aspect
 * Description:
 *
 * @Create 2025/4/10 16:44
 */
@Order(1)
@Component
@Aspect
public class DataLimitedAspect {
    @Autowired
    private UserSupport userSupport;
    @Autowired
    private UserRoleService userRoleService;

    //将切点定义为被自定义的注解修饰的相关方法
    @Pointcut("@annotation(com.tt.domain.annotation.DataLimited)")
    public void check() {
    }

    //在切入方法check执行之前需要执行的函数逻辑
    @Before("check()")
    public void doBefore(JoinPoint joinPoint) {
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId); //实际上根据id只能查出一个UserRole
        //获取用户所属的角色
        Set<String> userRoleSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        //获取方法的所有参数
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) { //遍历所有参数，找到UserMoment的参数
            if (arg instanceof UserMoment) {
                UserMoment userMoment = (UserMoment) arg; //类型转换
                String type = userMoment.getType();
                //Lv1的用户只能发布type为0的动态,强行发布的话会抛异常
                if (userRoleSet.contains(AuthRoleConstant.ROLE_CODE_LV1) && !"0".equals(type)) {
                    throw new ConditionException("本用户无权发布此种类型的动态,请检查发布动态的type字段");
                }
            }
        }
    }
}
