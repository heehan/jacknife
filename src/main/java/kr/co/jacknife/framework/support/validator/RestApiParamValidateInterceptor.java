package kr.co.jacknife.framework.support.validator;

import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestApiParamValidateInterceptor implements HandlerInterceptor {

    private MethodParameter findBindingResult(MethodParameter[] parameters )
    {

        MethodParameter _val = null;
        for (MethodParameter mp : parameters) {
            if (mp.getParameterType().isAssignableFrom(Errors.class)) {
                _val = mp;
                break;
            }
        }
        return _val;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (handler instanceof HandlerMethod)
        {
            HandlerMethod hm = (HandlerMethod) handler;
            MethodParameter bindingResults = findBindingResult(hm.getMethodParameters());

            if (bindingResults == null)
                return true;



        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
