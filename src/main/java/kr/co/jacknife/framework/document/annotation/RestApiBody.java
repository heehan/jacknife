package kr.co.jacknife.framework.document.annotation;


import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestApiBody
{
    String     desc() default "";
    @AliasFor(annotation = RequestBody.class)
    boolean required() default true;
}
