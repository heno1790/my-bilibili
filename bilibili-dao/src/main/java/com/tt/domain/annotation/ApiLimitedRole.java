package com.tt.domain.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * ClassName: ApiLimitedRole
 * Package: com.tt.domain.annotation
 * Description:
 *
 * @Create 2025/4/10 14:21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Component
public @interface ApiLimitedRole {
    String[] limitedRoleCodeList() default {};
}
