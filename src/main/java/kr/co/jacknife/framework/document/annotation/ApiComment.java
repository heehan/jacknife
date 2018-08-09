package kr.co.jacknife.framework.document.annotation;


import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiComment {
    String value() default "No Comment";
}
