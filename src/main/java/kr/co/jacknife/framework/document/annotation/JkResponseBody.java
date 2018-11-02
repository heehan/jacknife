package kr.co.jacknife.framework.document.annotation;

import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**************************************************************************
 *  <br>捲土重來</br>
 *  author : 윤희한
 *  email  : ryys1993@nate.com , scyun2015@gmail.com
 *  Date   : 2018. 10. 2.
 *************************************************************************/

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ResponseBody
public @interface JkResponseBody
{
}
