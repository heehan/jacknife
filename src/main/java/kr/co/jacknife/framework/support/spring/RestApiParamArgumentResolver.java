package kr.co.jacknife.framework.support.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.jacknife.framework.document.annotation.OptionalYN;
import kr.co.jacknife.framework.document.annotation.ParamType;
import kr.co.jacknife.framework.document.annotation.RestApiParam;
import kr.co.jacknife.framework.support.filter.ReReadableHttpRequestFilter;
import org.springframework.core.MethodParameter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

public class RestApiParamArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RestApiParam.class);
    }

    private boolean isEmpty(Object obj) {
        return obj == null || obj.toString().equals("") ;
    }

    private Object getRowValue(MethodParameter parameter, ReReadableHttpRequestFilter.RequestWrapper  request)
    {
        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        RestApiParam restApiParam = parameter.getParameterAnnotation(RestApiParam.class);
        ParamType paramType = restApiParam.type();
        String    paramName = restApiParam.name();
        if (paramType == ParamType.PATH)
        {
            return pathVariables.get(paramName);
        }
        else if (paramType == ParamType.QUERY)
        {
            String reqVal = request.getParameter(paramName);
            return isEmpty(reqVal) ? null : reqVal;
        }
        return null;
    }


    private Object toTargetType(MethodParameter parameter, ReReadableHttpRequestFilter.RequestWrapper  request)
            throws MethodArgumentNotValidException,
                    IllegalAccessException,
                    InvocationTargetException,
                    InstantiationException,
                    ClassNotFoundException {

        RestApiParam restApiParam = parameter.getParameterAnnotation(RestApiParam.class);
        DateTimeFormat dtf  =     parameter.getParameterAnnotation(DateTimeFormat.class);
        String    paramName = restApiParam.name();
        OptionalYN optionYN  = restApiParam.optional();
        Object result = getRowValue(parameter, request);
        Class clazz = null;
        if (parameter.isOptional()) {
            clazz = Class.forName(((ParameterizedType) parameter.getGenericParameterType()).getActualTypeArguments()[0].getTypeName());
        } else {
            clazz = parameter.getParameterType();
        }
        if (clazz.equals(LocalDateTime.class))
        {
            if (dtf == null) throw new MethodArgumentNotValidException(parameter, new BindException(result,paramName));
            if (!isEmpty(result))
            {
                try {
                    result = LocalDateTime.parse(result.toString(),DateTimeFormatter.ofPattern(dtf.pattern()));
                } catch (Exception e) {
                    LocalDate _tmpLocalDate = LocalDate.parse(result.toString(),DateTimeFormatter.ofPattern(dtf.pattern()));
                    LocalTime _tmpLocalTime = LocalTime.of(0,0,0,0);
                    result = LocalDateTime.of(_tmpLocalDate,_tmpLocalTime);
                    return result;
                }
            }
        }
        else if (clazz.equals(LocalDate.class))
        {
            if (dtf == null) throw new MethodArgumentNotValidException(parameter, new BindException(result,paramName));
            if (!isEmpty(result)) {
                result = LocalDate.parse(result.toString(),DateTimeFormatter.ofPattern(dtf.pattern()));
            }
        }
        else
        {
            if (!isEmpty(result))
            {
                if ( parameter.getParameterType().isEnum() )
                {
                    try {
                        Method m = parameter.getParameterType().getMethod("valueOf", String.class);
                        result = m.invoke(null,result  );
                    } catch (NoSuchMethodException e) {
                        throw new MethodArgumentNotValidException(parameter, new BindException(result,paramName));
                    }
                }
                else
                {
                    try {
                        Constructor c = clazz.getConstructor(String.class);
                        if (c != null)
                            result = c.newInstance(result);
                    } catch (NoSuchMethodException nsme) { }
                }
            }
        }

        if (optionYN == OptionalYN.N && result == null)
            throw new MethodArgumentNotValidException(parameter, new BindException(result,paramName));

        return result;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        RestApiParam restApiParam = parameter.getParameterAnnotation(RestApiParam.class);
        ParamType paramType = restApiParam.type();
        ReReadableHttpRequestFilter.RequestWrapper request = webRequest.getNativeRequest(ReReadableHttpRequestFilter.RequestWrapper.class);

        Object result = null;
        switch (paramType)
        {
            case PATH:
            case QUERY:
                    result = toTargetType(parameter, request);
                break;
            case BODY:
            {
//                String bodyString = null;
//                if (request.toByteArray().length == 0)
//                {
//                    byte[] buf = new byte[request.getContentLength()];
//                    request.getInputStream().read(buf, 0, request.getContentLength());
//                    bodyString = new String(buf, "UTF-8");
//                }
//                else
//                {
//                    bodyString = new String(request.toByteArray(), "UTF-8");
//                }
//                System.out.println(bodyString);
//                Class<?> clazz = parameter.getParameterType();
//                ObjectMapper objectMapper = new ObjectMapper();
//                result = objectMapper.readValue(bodyString, clazz);
            }
        }

        if (parameter.isOptional())
            return Optional.ofNullable(result);
        return result;
    }
}
