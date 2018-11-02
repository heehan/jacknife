package kr.co.jacknife.framework.document.annotation;

import org.springframework.http.HttpStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseCode {
    HttpStatus httpStatus() default HttpStatus.OK;
    String     status()     default "";
    String     comment()    default "";
    String     curlSamplePath() default "";
    String     reqSamplePath() default "";
    String     resSamplePath() default "";
}
