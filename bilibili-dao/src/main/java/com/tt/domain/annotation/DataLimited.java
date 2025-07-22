package com.tt.domain.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * ClassName: DataLimited
 * Package: com.tt.domain.annotation
 * Description:
 *
 * @Create 2025/4/10 16:43
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Component
public @interface DataLimited {

}
