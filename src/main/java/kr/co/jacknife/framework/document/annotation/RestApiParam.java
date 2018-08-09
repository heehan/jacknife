package kr.co.jacknife.framework.document.annotation;


import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestApiParam
{
    ParamType  type() default ParamType.QUERY;
    String     name() default "";
    OptionalYN optional() default OptionalYN.N;
    String     valueType() default "Object";
    String     desc() default "";
}
