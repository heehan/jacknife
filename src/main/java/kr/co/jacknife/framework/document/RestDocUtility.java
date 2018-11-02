package kr.co.jacknife.framework.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import kr.co.jacknife.framework.document.annotation.*;
import kr.co.jacknife.utils.ExcelUtil;
import kr.co.jacknife.utils.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

public class RestDocUtility {

    private static final Set<String> javaPrimitiveTypes = new HashSet<String>(){{
        add("byte"      );
        add("short"     );
        add("int"       );
        add("long"      );
        add("float"     );
        add("double"    );
        add("char"      );
        add("boolean"   );
    }};

    private static final Set<String> javaPrimitiveWrapperTypes = new HashSet<String>(){{
        add("class java.lang.Byte"      );
        add("class java.lang.Short"     );
        add("class java.lang.Integer"   );
        add("class java.lang.Long"      );
        add("class java.lang.Float"     );
        add("class java.lang.Double"    );
        add("class java.lang.Character" );
        add("class java.lang.Boolean"   );
        add("class java.lang.String"    );
        add("class java.math.BigInteger");
    }};

    private static final Map<String, String> javaPrimitiveWrapperMap = new HashMap<String, String>(){{
        put("byte"      , "class java.lang.Byte"      );
        put("short"     , "class java.lang.Short"     );
        put("int"       , "class java.lang.Integer"   );
        put("long"      , "class java.lang.Long"      );
        put("float"     , "class java.lang.Float"     );
        put("double"    , "class java.lang.Double"    );
        put("char"      , "class java.lang.Character" );
        put("boolean"   , "class java.lang.Boolean"   );
    }};

    public static boolean isIgnoreField  (Field field)
    {
        return  ReflectionUtils.isPublicStaticFinal(field)
                || field.getAnnotation(JsonIgnore.class) != null
                || field.getAnnotation(JsonIgnoreProperties.class) != null;
    }

    public static boolean isArrayType(String type) {
        return type.indexOf("[]") >= 0;
    }
    public static boolean isCollectionType(Class field)
    {
        return     field.getName().endsWith("[]")
                || field.getName().endsWith("java.util.List")
                || field.getName().endsWith("java.util.Set")
                || field.getName().endsWith("java.util.Collection");

    }

    public static boolean isWrappedOptionalType (Class field)
    {
        return field.getName().endsWith("java.util.Optional");
    }

    public static boolean isPrimitiveType(Class field)
    {
        return javaPrimitiveTypes.contains(field.toString());
    }

    public static boolean isWrapperPrimitiveType(Class field)
    {
        return javaPrimitiveWrapperTypes.contains(field.toString());
    }

    public static String findMatchType(Class clazz)
    {
        if (isPrimitiveType(clazz))
        {
            String wrapperTypeString = javaPrimitiveWrapperMap.get(clazz.toString());
            int lastDotIdx = wrapperTypeString.lastIndexOf(".");
            return wrapperTypeString.substring(lastDotIdx + 1);
        }
        else if (isWrapperPrimitiveType(clazz))
        {
            String wrapperTypeString = clazz.toString();//javaPrimitiveWrapperMap.get(field.getType().toString());
            int lastDotIdx = wrapperTypeString.lastIndexOf(".");
            return wrapperTypeString.substring(lastDotIdx + 1);
        }
        else
        {
            return "Object";
        }
    }

    public static String findMatchType(Field field) {
        if (field.isEnumConstant()) {
            return "String";
        } else if (isWrappedOptionalType(field.getType())) {
            Class clazz = null;
            try {
                clazz = Class.forName(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0].getTypeName());
            } catch(Exception e){}
            return findMatchType(clazz);
        } else
            return findMatchType(field.getType());
    }

    public static Class stripeXXXEntityType(Class clazz)
    {
        Class result = clazz;
        if (result.getName().endsWith("org.springframework.http.ResponseEntity"))
        {
            try {
                Method method = clazz.getDeclaredMethod("getBody");
                result = method.getReturnType();
            } catch (Exception e) { }
        }
        return result;
    }

    public static List<Field> findAllField(Class clazz) {
        System.out.println("#### -->>> " + clazz.getName());
        List<Field> fields = new ArrayList<>();
        while(true) {
            for (Field field : clazz.getDeclaredFields()) {
                if ( !java.lang.reflect.Modifier.isStatic(field.getModifiers())
                  && !java.lang.reflect.Modifier.isNative(field.getModifiers())
                 ) {
                    fields.add(field);
                }
            }
            clazz = clazz.getSuperclass();
            if (clazz == null) {
                break;
            }
        }
        return fields;
    }

    public static void readClassPropertyInfo(Integer step , Class clazz, List<ApiFieldInfo> apiFieldInfoList)
    {
        List<Field> fields = findAllField(clazz);//clazz.getDeclaredFields();//clazz.getFields();
        for (Field field : fields) {
            if (isIgnoreField(field)) continue;

            ApiComment pd = field.getAnnotation(ApiComment.class);

            boolean isArray   = isCollectionType(field.getType());

            String fieldName  = field.getName();
            String matchType  = pd != null ? pd.type() : isArray ? "[]" : findMatchType(field);
            String fieldComment =  (pd != null ? pd.value() + ("".equals(pd.pattern()) ? "" : " [Pattern : "+pd.pattern()+"]") : "No Comment") ;

            boolean isOptional = isWrappedOptionalType(field.getType());
            apiFieldInfoList.add(
                    new ApiFieldInfo().setDepth(step).setName(fieldName)
                                                     .setFieldType(matchType)
                                                     .setOptional(isOptional)
                                                     .setComment(fieldComment)
            );
            try {
                if (matchType.equals("[]")) {
                    ParameterizedType pt = (ParameterizedType)field.getGenericType();
                    String className = pt.getActualTypeArguments()[0].getTypeName();
                    readClassPropertyInfo(step + 1 , Class.forName(className), apiFieldInfoList);
                } else if (matchType.equals("Object")) {
                    readClassPropertyInfo(step + 1, field.getType(), apiFieldInfoList);
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public static List<ApiFieldInfo> readClassPropertyInfo(Integer step, String className)
    {
        Class clazz =  null;
        try
        {
            clazz = Class.forName(className);
            return readClassPropertyInfo(step, clazz);
        } catch (Exception e) {
            throw new RuntimeException("class not found");
        }
    }
    public static List<ApiFieldInfo> readClassPropertyInfo(Integer step , Class clazz)
    {
        List<ApiFieldInfo> apiFieldInfos = new ArrayList<>();
        readClassPropertyInfo(step, clazz, apiFieldInfos);
        return apiFieldInfos;
    }

    public static void createXlsxDocument(ConfigurableApplicationContext ctx, String destPath, String fileName, Map<String, String> envDomainKeyValueMap) throws Exception {
        RestDocUtility.createXlsxDocument(RestDocUtility.findApiInfosBySpringContext(ctx), destPath, fileName,envDomainKeyValueMap);
    }

    public static void createXlsxDocument(List<ApiInfo> apiInfoList , String destPath, String fileName, Map<String, String> envDomainKeyValueMap) throws IOException {

        ExcelUtil.DataHandler dataHandler = ExcelUtil.newWorksheetConfigurer()
                                                     .addWorksheet("Index")
                                                     .setActiveWorksheet("Index")
                                                     .setCursor("B", 2);
        dataHandler.addRowData(new ExcelUtil.ColumnData("환경별 도메인").setSize(10).setEmphasized(true));
        for (Map.Entry<String,String> entry : envDomainKeyValueMap.entrySet()) {
            dataHandler.addRowData(new ExcelUtil.ColumnData(entry.getKey()).setSize(10).setEmphasized(true)
                                ,  new ExcelUtil.ColumnData(entry.getValue()) );
        }

        dataHandler.addRowData(
                new ExcelUtil.ColumnData("API 코드").setSize(10).setEmphasized(true) ,
                new ExcelUtil.ColumnData("이름").setSize(30).setEmphasized(true));
        for (ApiInfo apiInfo : apiInfoList) {
            dataHandler.addRowData(new ExcelUtil.ColumnData(apiInfo.getCode(), HorizontalAlignment.LEFT)
                                 , new ExcelUtil.ColumnData(apiInfo.getComment(), HorizontalAlignment.LEFT));
        }

        for (ApiInfo apiInfo : apiInfoList) {
            dataHandler.changeWorksheet(apiInfo.getCode()).setCursor("B",2);
            dataHandler.addRowData(new ExcelUtil.ColumnData("API Code", HorizontalAlignment.LEFT).setSize(10).setEmphasized(true)
                                 , new ExcelUtil.ColumnData(apiInfo.getCode(), HorizontalAlignment.LEFT).setSize(10));
            dataHandler.addRowData(new ExcelUtil.ColumnData("API Name", HorizontalAlignment.LEFT).setSize(10).setEmphasized(true)
                                 , new ExcelUtil.ColumnData(apiInfo.getName(), HorizontalAlignment.LEFT).setSize(30));
            dataHandler.addRowData(new ExcelUtil.ColumnData("URL", HorizontalAlignment.LEFT).setSize(10).setEmphasized(true)
                                 , new ExcelUtil.ColumnData(apiInfo.getUrl(), HorizontalAlignment.LEFT).setSize(30));
            dataHandler.addRowData(new ExcelUtil.ColumnData("Method", HorizontalAlignment.LEFT).setSize(10).setEmphasized(true)
                                 , new ExcelUtil.ColumnData(apiInfo.getMethod(), HorizontalAlignment.LEFT).setSize(20));
            dataHandler.addRowData(new ExcelUtil.ColumnData("Comment", HorizontalAlignment.LEFT).setSize(10).setEmphasized(true)
                                 , new ExcelUtil.ColumnData(apiInfo.getComment(), HorizontalAlignment.LEFT).setSize(40));

            if (!apiInfo.getHeaderParamList().isEmpty()) {
                dataHandler.addRowData(new ExcelUtil.ColumnData(""));
                dataHandler.addRowData(new ExcelUtil.ColumnData("Request Header Structure", HorizontalAlignment.LEFT).setSize(20).setEmphasized(true));
                dataHandler.addRowData(new ExcelUtil.ColumnData("Header Variable", HorizontalAlignment.LEFT).setSize(20).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("Name", HorizontalAlignment.LEFT).setSize(20).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("Type", HorizontalAlignment.LEFT).setSize(10).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("OptionalYN", HorizontalAlignment.LEFT).setSize(10).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("Comment", HorizontalAlignment.LEFT).setSize(40).setEmphasized(true));

                for (RestApiParam param : apiInfo.getHeaderParamList())
                {
                    dataHandler.addRowData(new ExcelUtil.ColumnData("")
                            , new ExcelUtil.ColumnData(param.name(), HorizontalAlignment.LEFT)
                            , new ExcelUtil.ColumnData(param.valueType(), HorizontalAlignment.LEFT)
                            , new ExcelUtil.ColumnData(param.optional() == OptionalYN.Y ? "Y" : "", HorizontalAlignment.LEFT)
                            , new ExcelUtil.ColumnData(param.desc(), HorizontalAlignment.LEFT));
                }
            }
            if (!apiInfo.getPathParamList().isEmpty()) {
                dataHandler.addRowData(new ExcelUtil.ColumnData(""));
                dataHandler.addRowData(new ExcelUtil.ColumnData("Request Structure", HorizontalAlignment.LEFT).setSize(20).setEmphasized(true));
                dataHandler.addRowData(new ExcelUtil.ColumnData("Path Variable", HorizontalAlignment.LEFT).setSize(20).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("Name", HorizontalAlignment.LEFT).setSize(20).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("Type", HorizontalAlignment.LEFT).setSize(10).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("OptionalYN", HorizontalAlignment.LEFT).setSize(10).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("Comment", HorizontalAlignment.LEFT).setSize(40).setEmphasized(true));

                for (RestApiParam param : apiInfo.getPathParamList())
                {
                    dataHandler.addRowData(new ExcelUtil.ColumnData("")
                            , new ExcelUtil.ColumnData(param.name(), HorizontalAlignment.LEFT)
                            , new ExcelUtil.ColumnData(param.valueType(), HorizontalAlignment.LEFT)
                            , new ExcelUtil.ColumnData(param.optional() == OptionalYN.Y ? "Y" : "", HorizontalAlignment.LEFT)
                            , new ExcelUtil.ColumnData(param.desc(), HorizontalAlignment.LEFT));
                }
            }

            if (!apiInfo.getQueryParamList().isEmpty()) {
                dataHandler.addRowData(new ExcelUtil.ColumnData(""));
                dataHandler.addRowData(new ExcelUtil.ColumnData("Query Parameter", HorizontalAlignment.LEFT).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("Name", HorizontalAlignment.LEFT).setSize(20).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("OptionalYN", HorizontalAlignment.LEFT).setSize(10).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("Type", HorizontalAlignment.LEFT).setSize(10).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("Comment", HorizontalAlignment.LEFT).setSize(40).setEmphasized(true));

                for (RestApiParam param : apiInfo.getQueryParamList()) {
                    dataHandler.addRowData(
                            new ExcelUtil.ColumnData("")
                            , new ExcelUtil.ColumnData(param.name(), HorizontalAlignment.LEFT)
                            , new ExcelUtil.ColumnData(param.optional() == OptionalYN.Y ? "Y" : "", HorizontalAlignment.LEFT)
                            , new ExcelUtil.ColumnData(param.valueType(), HorizontalAlignment.LEFT)
                            , new ExcelUtil.ColumnData(param.desc(), HorizontalAlignment.LEFT)
                    );
                }
            }

            if (!apiInfo.getRequestBodyPropList().isEmpty()) {
                List<ApiFieldInfo> apiFieldInfos =  apiInfo.getRequestBodyPropList();//RestDocUtility.readClassPropertyInfo(1, apiInfo.getReqBodyType());

                dataHandler.addRowData(new ExcelUtil.ColumnData(""));
                dataHandler.addRowData(new ExcelUtil.ColumnData("RequestBody", HorizontalAlignment.LEFT).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("Name", HorizontalAlignment.LEFT).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("Type", HorizontalAlignment.LEFT).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("OptionalYN", HorizontalAlignment.LEFT).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("Comment", HorizontalAlignment.LEFT).setEmphasized(true));

                for (ApiFieldInfo apiFieldInfo: apiFieldInfos) {
                    dataHandler.addRowData(
                            new ExcelUtil.ColumnData("")
                            , new ExcelUtil.ColumnData(apiFieldInfo.getName(), HorizontalAlignment.LEFT)
                            , new ExcelUtil.ColumnData(apiFieldInfo.getFieldType(), HorizontalAlignment.LEFT)
                            , new ExcelUtil.ColumnData(apiFieldInfo.isOptional() ? "Y" : "", HorizontalAlignment.LEFT)
                            , new ExcelUtil.ColumnData(apiFieldInfo.getComment(), HorizontalAlignment.LEFT)
                    );
                }
            }

            if (!apiInfo.getResponseBodyPropList().isEmpty()) {
                List<ApiFieldInfo> apiFieldInfos = apiInfo.getResponseBodyPropList();//RestDocUtility.readClassPropertyInfo(1, apiInfo.getReturnType());

                dataHandler.addRowData(new ExcelUtil.ColumnData(""));
                dataHandler.addRowData(new ExcelUtil.ColumnData("Response", HorizontalAlignment.LEFT).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("Name", HorizontalAlignment.LEFT).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("Type", HorizontalAlignment.LEFT).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("OptionalYN", HorizontalAlignment.LEFT).setEmphasized(true)
                        ,new ExcelUtil.ColumnData("Comment", HorizontalAlignment.LEFT).setEmphasized(true));

                for (ApiFieldInfo apiFieldInfo: apiFieldInfos) {
                    dataHandler.addRowData(
                            new ExcelUtil.ColumnData("")
                            , new ExcelUtil.ColumnData(apiFieldInfo.getName(), HorizontalAlignment.LEFT)
                            , new ExcelUtil.ColumnData(apiFieldInfo.getFieldType(), HorizontalAlignment.LEFT)
                            , new ExcelUtil.ColumnData(apiFieldInfo.isOptional() ? "Y" : "", HorizontalAlignment.LEFT)
                            , new ExcelUtil.ColumnData(apiFieldInfo.getComment(), HorizontalAlignment.LEFT)
                    );
                }
            }

            if (apiInfo.getSuccessCodes() == null) continue;

            dataHandler.addRowData(new ExcelUtil.ColumnData(""));
            dataHandler.addRowData(new ExcelUtil.ColumnData(apiInfo.getSuccessCodes().type() + " 정의", HorizontalAlignment.LEFT).setEmphasized(true));
            dataHandler.addRowData(
                      new ExcelUtil.ColumnData("HttpStatus", HorizontalAlignment.LEFT).setEmphasized(true)
                    , new ExcelUtil.ColumnData("status", HorizontalAlignment.LEFT).setEmphasized(true)
                    , new ExcelUtil.ColumnData("Comment", HorizontalAlignment.LEFT).setEmphasized(true));
            for (ResponseCode resCode : apiInfo.getSuccessCodes().rescodes()) {
                dataHandler.addRowData(
                          new ExcelUtil.ColumnData(resCode.httpStatus().value() + "", HorizontalAlignment.LEFT)
                        , new ExcelUtil.ColumnData(resCode.status() + "", HorizontalAlignment.LEFT)
                        , new ExcelUtil.ColumnData(resCode.comment(), HorizontalAlignment.LEFT));
            }

            dataHandler.addRowData(new ExcelUtil.ColumnData(""));
            dataHandler.addRowData(new ExcelUtil.ColumnData(apiInfo.getErrorCodes().type() + " 정의", HorizontalAlignment.LEFT).setEmphasized(true));
            dataHandler.addRowData(new ExcelUtil.ColumnData("HttpStatus", HorizontalAlignment.LEFT).setEmphasized(true)
                                ,  new ExcelUtil.ColumnData("status", HorizontalAlignment.LEFT).setEmphasized(true)
                                ,  new ExcelUtil.ColumnData("Comment", HorizontalAlignment.LEFT).setEmphasized(true));

            for (ResponseCode resCode : apiInfo.getErrorCodes().rescodes()) {
                dataHandler.addRowData( new ExcelUtil.ColumnData(resCode.httpStatus().value() + "", HorizontalAlignment.LEFT)
                                      , new ExcelUtil.ColumnData(resCode.status() + "", HorizontalAlignment.LEFT)
                                      , new ExcelUtil.ColumnData(resCode.comment(), HorizontalAlignment.LEFT));
            }

            dataHandler.addRowData(new ExcelUtil.ColumnData(""));
            dataHandler.addRowData(new ExcelUtil.ColumnData(""));
            for (ResponseCode resCode : apiInfo.getSuccessCodes().rescodes())
            {
                dataHandler.addRowData(new ExcelUtil.ColumnData(resCode.status() + "샘플").setEmphasized(true));
                if (!"".equals(resCode.curlSamplePath())) {
                    try {
                        String _tmp = new String(IOUtils.toByteArray(RestDocUtility.class.getClassLoader().getResourceAsStream(resCode.curlSamplePath())));
                        dataHandler.addRowData( new ExcelUtil.ColumnData("curl" , HorizontalAlignment.LEFT).setEmphasized(true)
                                , new ExcelUtil.ColumnData(_tmp , HorizontalAlignment.LEFT)
                        );
                    } catch (Exception e) {}
                }
                //if (reqSampleFile.exists()) {
                if (!"".equals(resCode.reqSamplePath())) {
                    try {
                        String _tmp = new String(IOUtils.toByteArray(RestDocUtility.class.getClassLoader().getResourceAsStream(resCode.reqSamplePath())));
                        dataHandler.addRowData(new ExcelUtil.ColumnData("req", HorizontalAlignment.LEFT).setEmphasized(true)
                                , new ExcelUtil.ColumnData(_tmp, HorizontalAlignment.LEFT)
                        );
                    } catch (Exception e) {}
                }
                if (!"".equals(resCode.resSamplePath())) {
                    try {
                        String _tmp = new String(IOUtils.toByteArray(RestDocUtility.class.getClassLoader().getResourceAsStream(resCode.resSamplePath())));
                        dataHandler.addRowData( new ExcelUtil.ColumnData("res" , HorizontalAlignment.LEFT).setEmphasized(true)
                                , new ExcelUtil.ColumnData(_tmp , HorizontalAlignment.LEFT)
                        );
                    } catch (Exception e) {}
                }
            }

            dataHandler.addRowData(new ExcelUtil.ColumnData(""));
            dataHandler.addRowData(new ExcelUtil.ColumnData(""));
            for (ResponseCode resCode : apiInfo.getErrorCodes().rescodes())  //.getSuccessCodes().rescodes())
            {
                dataHandler.addRowData(new ExcelUtil.ColumnData(resCode.status() + "샘플").setEmphasized(true));
                if (!"".equals(resCode.curlSamplePath())) {
                    try {
                        String _tmp = new String(IOUtils.toByteArray(RestDocUtility.class.getClassLoader().getResourceAsStream(resCode.curlSamplePath())));
                        dataHandler.addRowData( new ExcelUtil.ColumnData("curl" , HorizontalAlignment.LEFT).setEmphasized(true)
                                , new ExcelUtil.ColumnData(_tmp , HorizontalAlignment.LEFT)
                        );
                    } catch (Exception e) {}
                }
                //if (reqSampleFile.exists()) {
                if (!"".equals(resCode.reqSamplePath())) {
                    try {
                        String _tmp = new String(IOUtils.toByteArray(RestDocUtility.class.getClassLoader().getResourceAsStream(resCode.reqSamplePath())));
                        dataHandler.addRowData(new ExcelUtil.ColumnData("req", HorizontalAlignment.LEFT).setEmphasized(true)
                                , new ExcelUtil.ColumnData(_tmp, HorizontalAlignment.LEFT)
                        );
                    } catch (Exception e) {}
                }
                if (!"".equals(resCode.resSamplePath())) {
                    try {
                        String _tmp = new String(IOUtils.toByteArray(RestDocUtility.class.getClassLoader().getResourceAsStream(resCode.resSamplePath())));
                        dataHandler.addRowData( new ExcelUtil.ColumnData("res" , HorizontalAlignment.LEFT).setEmphasized(true)
                                , new ExcelUtil.ColumnData(_tmp , HorizontalAlignment.LEFT)
                        );
                    } catch (Exception e) {}
                }
            }

        }

        File file = new File(destPath);
        file = dataHandler.done().write(file, fileName);
        System.out.println(file.getPath());
    }

    public static List<ApiInfo> findApiInfosBySpringContext(ConfigurableApplicationContext ctx) throws Exception
    {
        List<ApiInfo> apiInfoList = new ArrayList<ApiInfo>();

        Map<String, Object> beansMap = ctx.getBeansWithAnnotation(Controller.class);

        for (Map.Entry<String, Object> entry : beansMap.entrySet())
        {
            Class clazz = entry.getValue().getClass();

            Method[] methods = clazz.getDeclaredMethods();
            for (Method m : methods)
            {
                RestApi pal = m.getDeclaredAnnotation(RestApi.class) ;
                if (pal == null) continue;

                ApiComment desc = m.getDeclaredAnnotation(ApiComment.class);

                ApiInfo apiInfo = new ApiInfo().setCode(pal.apiCode())
                        .setName(pal.apiName())
                        .setUrl( pal.path().length > 0 ? String.join(",", pal.path())
                                : String.join(",", pal.value())  )
                        .setMethod(StringUtils.join(pal.method()));
                if (desc != null) apiInfo.setComment(desc.value());

                Parameter[] parameters = m.getParameters();

                for (Parameter p : parameters) {
                    RestApiParam pap = p.getAnnotation(RestApiParam.class);
                    if (pap != null && pap.type() == ParamType.HEADER)
                        apiInfo.getHeaderParamList().add(pap);
                }

                for (Parameter p : parameters) {
                    RestApiParam pap = p.getAnnotation(RestApiParam.class);
                    if (pap != null && pap.type() == ParamType.PATH)
                        apiInfo.getPathParamList().add(pap);
                }

                for (Parameter p : parameters)
                {
                    RestApiParam pap = p.getAnnotation(RestApiParam.class);
                    if (pap != null && pap.type() == ParamType.QUERY)
                        apiInfo.getQueryParamList().add(pap);
                }
                for (Parameter p : parameters)
                {
                    RestApiParam pap = p.getAnnotation(RestApiParam.class);
                    if (pap != null && pap.type() == ParamType.BODY)
                    {
                        List<ApiFieldInfo> apiFieldInfos = RestDocUtility.readClassPropertyInfo(1, p.getType());
                        apiInfo.getRequestBodyPropList().addAll(apiFieldInfos);
                    }
                }

                if (m.getReturnType().equals(ResponseEntity.class)) {
                    Type[] returnType = ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments();
                    List<ApiFieldInfo> apiFieldInfos = new ArrayList<>();

                    if (returnType.length > 0) {
                        String typeName = ((ParameterizedTypeImpl)returnType[0]).getTypeName();
                        typeName = typeName.replaceAll(">", "");
                        String[] types = typeName.split("<");

                        int stepCount = 0;
                        for (String type : types) {
                            if (RestDocUtility.isArrayType(type)) {
                                apiFieldInfos.get(apiFieldInfos.size()-1).setFieldType("[]");
                                continue;
                            }
                            Class _clazz = Class.forName(type);
                            if (RestDocUtility.isCollectionType(_clazz))
                            {
                                apiFieldInfos.get(apiFieldInfos.size()-1).setFieldType("[]");
                                continue;
                            }
                            RestDocUtility.readClassPropertyInfo(++stepCount, _clazz, apiFieldInfos);
                        }
                    }
                    apiInfo.getResponseBodyPropList().addAll(apiFieldInfos);
                    //apiInfo.setReturnType(((ParameterizedTypeImpl) returnType[0]).getRawType());
                } else {
                    List<ApiFieldInfo> apiFieldInfos = RestDocUtility.readClassPropertyInfo(1, m.getReturnType());
                    apiInfo.getResponseBodyPropList().addAll(apiFieldInfos);
                }

                ResponseSuccessCodes rsc = m.getDeclaredAnnotation(ResponseSuccessCodes.class);
                apiInfo.setSuccessCodes(rsc);
                ResponseErrorCodes rec = m.getDeclaredAnnotation(ResponseErrorCodes.class);
                apiInfo.setErrorCodes(rec);
                apiInfoList.add(apiInfo);
            }
        }
        Collections.sort(apiInfoList);

        return apiInfoList;
    }
//    public static void main(String args[]) throws Exception
//    {
//        //ResponseEntity<ApiResultBean<PromotionMktProcessBean >> x = new ResponseEntity<ApiResultBean<PromotionMktProcessBean>>(HttpStatus.OK);
//        //String x = "ResponseEntity<ApiResultBean<PageResultBean<CarWashSchedulePolicyTblVO>>>";
//        //ResponseEntity<ApiResultBean<PageResultBean<? extends CarWashSchedulePolicyTblVO>>> xxx = null;
//        //String x = "ResponseEntity<ApiResultBean<PageResultBean<? extends CarWashSchedulePolicyTblVO>>>";//l xxx = null;
//
//        //List<String> queue = new ArrayList<>();
////        System.out.println(x.replaceAll(">", ""));
////        String[] queue = x.replaceAll(">","").split("<");
////
////        for (String q : queue)
////        {
////            Class.forName()
////        }
////        Method  _m = null;
////        for (Method m : CarWashReservationControl.class.getDeclaredMethods())
////        {
////            if(m.getName().equals("admEncarCarWashReservation"))
////            {
////                _m = m;
////                break;
////            }
////        }
////
//        //List<ApiFieldInfo> lists = readClassPropertyInfo(1, ApiResultBean.AdmEncarCarWashReservationResponse.class);
////        List<ApiFieldInfo> lists = readClassPropertyInfo(1,_m.getReturnType());
//        List<ApiFieldInfo> lists = readClassPropertyInfo(1,ResponseEntity.class );
//
//        System.out.println(String.format("%-50s\t%-10s\t%-20s\t%-20s", new Object[]{"FieldName", "FieldType", "Optional","Comment"}));
//        for (ApiFieldInfo apiFieldInfo : lists)
//        {
//            System.out.println(String.format("%-50s\t%-10s\t%-20s\t%-20s", new Object[]{apiFieldInfo.getName(), apiFieldInfo.getFieldType(), apiFieldInfo.isOptional(), apiFieldInfo.getComment()}));
//        }
//    }
}
