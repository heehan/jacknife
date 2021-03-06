package kr.co.jacknife.framework.document.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseErrorCodes {
     String         type()     default "";
     ResponseCode[] rescodes() default {};
}
