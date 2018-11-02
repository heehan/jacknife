package kr.co.jacknife.framework.support.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.co.jacknife.framework.document.annotation.OptionalYN;
import kr.co.jacknife.framework.document.annotation.ParamType;
import kr.co.jacknife.framework.document.annotation.RestApi;
import kr.co.jacknife.framework.document.annotation.RestApiParam;
import kr.co.jacknife.framework.support.filter.ReReadableHttpRequestFilter;

public class ApiLoggingInterceptor implements HandlerInterceptor {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private boolean isBinaryContent(final HttpServletRequest request) {
        if (request.getContentType() == null) {
            return false;
        }
        return request.getContentType().startsWith("image")
            || request.getContentType().startsWith("video")
            || request.getContentType().startsWith("audio");
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

                if (logger.isDebugEnabled()) {
                    logger.debug("# RequestHeaders ...........");
                    Enumeration<String> headerNames = request.getHeaderNames();
                    while(headerNames.hasMoreElements()) {
                        String headerName = headerNames.nextElement();
                        Enumeration<String> values = request.getHeaders(headerName);
                        StringBuilder builder = new StringBuilder();
                        while(values.hasMoreElements()) {
                            builder.append(values.nextElement()).append(",");
                        }
                        logger.debug("# RequestHeader : Name -> {}, Values ->{}", headerName,
                                builder.length() <= 0 ? "" : builder.substring(0, builder.length() - 1));
                    }
                } else {
                    Enumeration<String> values = request.getHeaders("Accept");
                    StringBuilder builder = new StringBuilder();
                    while(values.hasMoreElements()) {
                        builder.append(values.nextElement()).append(",");
                    }
                    logger.debug("# Accept Header : Values ->{}",
                            builder.length() <= 0 ? "" : builder.substring(0, builder.length() - 1));
                }

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

                if (request instanceof ReReadableHttpRequestFilter.RequestWrapper  && !isMultipart(request) && !isBinaryContent(request)
                        && !request.getMethod().equalsIgnoreCase("get") )
                {
                    ReReadableHttpRequestFilter.RequestWrapper rw = (ReReadableHttpRequestFilter.RequestWrapper)request;
                    logger.info("## REQUEST BODY -\n{}", rw.getRequestBody());
                }
            }
        }


        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        if (handler instanceof HandlerMethod) {
//            HandlerMethod hm = (HandlerMethod) handler;
//
////            if (hm.hasMethodAnnotation(RestApi.class))
////            {
////                logger.info("handlerClass -=> {} ", handler == null ? " NULL " :  handler.getClass());
////                logger.info("view class   -=> {} ", modelAndView == null ? " ModelAndView is NULL " : modelAndView.getView() == null ? " View is NULL " : modelAndView.getView().getClass());
////            }
//        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        if (handler instanceof HandlerMethod) {
//            HandlerMethod hm = (HandlerMethod) handler;
//            if (hm.hasMethodAnnotation(RestApi.class)) {
//                logger.info("hanlderClass -=> {} ", handler == null ? " Handler is NULL " : handler.getClass());
//                logger.info("ex Class     -=> {} ", ex == null ? "ex is NULL " : ex.getClass());
//            }
//        }
    }
}



