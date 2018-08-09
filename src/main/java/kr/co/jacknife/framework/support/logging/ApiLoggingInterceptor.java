package kr.co.jacknife.framework.support.logging;

import kr.co.jacknife.framework.document.annotation.OptionalYN;
import kr.co.jacknife.framework.document.annotation.ParamType;
import kr.co.jacknife.framework.document.annotation.RestApi;
import kr.co.jacknife.framework.document.annotation.RestApiParam;
import kr.co.jacknife.framework.support.filter.ReReadableHttpRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class ApiLoggingInterceptor implements HandlerInterceptor {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private boolean isBinaryContent(final HttpServletRequest request) {
        if (request.getContentType() == null) {
            return false;
        }
        return request.getContentType().startsWith("image") || request.getContentType().startsWith("video") || request.getContentType().startsWith("audio");
    }

    private boolean isMultipart(final HttpServletRequest request) {
        return request.getContentType() != null && request.getContentType().startsWith("multipart/form-data");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (handler instanceof HandlerMethod)
        {
            HandlerMethod hm = (HandlerMethod) handler;


            if (hm.hasMethodAnnotation(RestApi.class))
            {
                RestApi pai = hm.getMethodAnnotation(RestApi.class);
                RequestMapping rm = hm.getMethodAnnotation(RequestMapping.class);
                String[] paths = rm.path();
                RequestMethod[] requestMethods = rm.method();

                logger.info("#=====================================================================================================");
                logger.info("# API Name      : {}", pai.apiName());
                logger.info("# API Code      : {}", pai.apiCode());
                logger.info("# API Path      : {}", String.join(",", paths ));
                logger.info("# RequestMethod : {}", request.getMethod());
                logger.info("# Request URI   : {}", request.getRequestURI() );

                logger.info("## API PathVariables ");
                Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                for (MethodParameter mp : hm.getMethodParameters())
                {
                    PathVariable pv =  mp.getParameterAnnotation(PathVariable.class);
                    RestApiParam pap = mp.getParameterAnnotation(RestApiParam.class);
                    if (pap != null && pap.type() == ParamType.PATH)
                    {
                        logger.info("=== {}[optional={}] -> {} ", pap.name(),pap.optional(), pathVariables.get(pap.name()));
                    }
                    else if (pv != null)
                    {
                        logger.info("=== {}[optional={}] -> {} ", pv.name(), pv.required() ? OptionalYN.N : OptionalYN.Y, pathVariables.get(pv.name()));
                    }
                }

                logger.info("## API Request Parameters ");
                for (MethodParameter mp : hm.getMethodParameters())
                {
                    RequestParam rp =  mp.getParameterAnnotation(RequestParam.class);
                    RestApiParam pap = mp.getParameterAnnotation(RestApiParam.class);
                    if (pap != null && pap.type() == ParamType.QUERY)
                    {
                        logger.info("=== {}[optional={}] -> {} ", pap.name(),pap.optional(), request.getParameter(pap.name()) );
                    }
                    else if (rp != null)
                    {
                        logger.info("=== {}[optional={}] -> {} ", rp.name(),
                                                                  rp.required() ? OptionalYN.N : OptionalYN.Y,
                                                                  String.join(",", request.getParameterValues(rp.name())));
                    }
                }

                if (request instanceof ReReadableHttpRequestFilter.RequestWrapper  && !isMultipart(request) && !isBinaryContent(request))
                {
                    ReReadableHttpRequestFilter.RequestWrapper rw = (ReReadableHttpRequestFilter.RequestWrapper)request;

                    String bodyString = "";
                    if (rw.toByteArray().length == 0) {
                        byte[] buf = new byte[rw.getContentLength()];
                        rw.getInputStream().read(buf, 0, rw.getContentLength());
                        bodyString = new String(buf, "UTF-8");
                    } else {
                        bodyString = new String(rw.toByteArray(), "UTF-8");
                    }

                    logger.info("## REQUEST BODY -\n{}", bodyString);
                }
            }
        }


        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;

            if (hm.hasMethodAnnotation(RestApi.class))
            {
                logger.info("handlerClass -=> {} ", handler == null ? " NULL " :  handler.getClass());
                logger.info("view class   -=> {} ", modelAndView == null ? " ModelAndView is NULL " :
                        modelAndView.getView() == null ? " View is NULL " :
                                modelAndView.getView().getClass());
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            if (hm.hasMethodAnnotation(RestApi.class)) {
                logger.info("hanlderClass -=> {} ", handler == null ? " Handler is NULL " : handler.getClass());
                logger.info("ex Class     -=> {} ", ex == null ? "ex is NULL " : ex.getClass());
            }
        }
    }
}



