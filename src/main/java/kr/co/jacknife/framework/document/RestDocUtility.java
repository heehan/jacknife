package kr.co.jacknife.framework.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import kr.co.jacknife.framework.document.annotation.ApiComment;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class RestDocUtility {

    private static final Set<String> javaPrimitiveTypes = new HashSet<String>(){{
        add("byte"      ); add("short"     ); add("int"       ); add("long"      ); add("float"     ); add("double"    ); add("char"      ); add("boolean"   );
    }};

    private static final Set<String> javaPrimitiveWrapperTypes = new HashSet<String>(){{
        add("class java.lang.Byte"      ); add("class java.lang.Short"     ); add("class java.lang.Integer"   ); add("class java.lang.Long"      ); add("class java.lang.Float"     ); add("class java.lang.Double"    ); add("class java.lang.Character" ); add("class java.lang.Boolean"   );
        add("class java.lang.String");
    }};

    private static final Map<String, String> javaPrimitiveWrapperMap = new HashMap<String, String>(){{
        put("byte"      , "class java.lang.Byte"      ); put("short"     , "class java.lang.Short"     ); put("int"       , "class java.lang.Integer"   ); put("long"      , "class java.lang.Long"      ); put("float"     , "class java.lang.Float"     ); put("double"    , "class java.lang.Double"    ); put("char"      , "class java.lang.Character" );
        put("boolean"   , "class java.lang.Boolean"   );
    }};

    private static boolean isIgnoreField  (Field field)
    {
        return  ReflectionUtils.isPublicStaticFinal(field)
                || field.getAnnotation(JsonIgnore.class) != null
                || field.getAnnotation(JsonIgnoreProperties.class) != null;
    }

    private static boolean isCollectionType(Class field)
    {
        //System.out.println("\t\t\t\t isCollectionType ->" + field.getTypeName());
        return field.getName().endsWith("java.util.List");
    }

    private static boolean isWrappedOptionalType (Class field)
    {
        return field.getName().endsWith("java.util.Optional");
    }

    private static boolean isPrimitiveType(Class field)
    {
        return javaPrimitiveTypes.contains(field.toString());
    }

    private static boolean isWrapperPrimitiveType(Class field)
    {
        return javaPrimitiveWrapperTypes.contains(field.toString());
    }

    private static String findMatchType(Class clazz)
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

    private static String findMatchType(Field field)
    {
        if (field.isEnumConstant())
        {
            return "String";
        }
        else if (isWrappedOptionalType(field.getType()))
        {
            Class clazz = null;

            try {
                clazz = Class.forName(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0].getTypeName());
            }catch(Exception e){}
            return findMatchType(clazz);
        }
        else
            return findMatchType(field.getType());
    }

    private static Class stripeXXXEntityType(Class clazz)
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

    private static void readClassPropertyInfo(Integer step , Class clazz, List<ApiFieldInfo> apiFieldInfoList)
    {

        Field[] fields = clazz.getDeclaredFields();//clazz.getFields();

        for (Field field : fields)
        {
            if (isIgnoreField(field)) continue;
            //if (!clazz.getName().startsWith("net.genieworks")) continue;
            boolean isArray   = isCollectionType(field.getType());
            String fieldName  = field.getName();
            String matchType  = isArray ? "[]"  : findMatchType(field);
            boolean isOptional = isWrappedOptionalType(field.getType());
            ApiComment pd = field.getAnnotation(ApiComment.class);
            apiFieldInfoList.add(
                    new ApiFieldInfo().setDepth(step).setName(fieldName).setFieldType(matchType).setOptional(isOptional).setComment(pd == null ? "No Comment" : pd.value())
            );
            try {
                if (matchType.equals("[]"))
                {
                    ParameterizedType pt = (ParameterizedType)field.getGenericType();
                    String className = pt.getActualTypeArguments()[0].getTypeName();
                    readClassPropertyInfo(step + 1 , Class.forName(className), apiFieldInfoList);
                }
                else if (matchType.equals("Object"))
                {
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

    public static void main(String args[]) throws Exception
    {
        //ResponseEntity<ApiResultBean<PromotionMktProcessBean >> x = new ResponseEntity<ApiResultBean<PromotionMktProcessBean>>(HttpStatus.OK);
        //String x = "ResponseEntity<ApiResultBean<PageResultBean<CarWashSchedulePolicyTblVO>>>";
        //ResponseEntity<ApiResultBean<PageResultBean<? extends CarWashSchedulePolicyTblVO>>> xxx = null;
        //String x = "ResponseEntity<ApiResultBean<PageResultBean<? extends CarWashSchedulePolicyTblVO>>>";//l xxx = null;

        //List<String> queue = new ArrayList<>();
//        System.out.println(x.replaceAll(">", ""));
//        String[] queue = x.replaceAll(">","").split("<");
//
//        for (String q : queue)
//        {
//            Class.forName()
//        }
//        Method  _m = null;
//        for (Method m : CarWashReservationControl.class.getDeclaredMethods())
//        {
//            if(m.getName().equals("admEncarCarWashReservation"))
//            {
//                _m = m;
//                break;
//            }
//        }
//
        //List<ApiFieldInfo> lists = readClassPropertyInfo(1, ApiResultBean.AdmEncarCarWashReservationResponse.class);
//        List<ApiFieldInfo> lists = readClassPropertyInfo(1,_m.getReturnType());
        List<ApiFieldInfo> lists = readClassPropertyInfo(1,ResponseEntity.class );

        System.out.println(String.format("%-50s\t%-10s\t%-20s\t%-20s", new Object[]{"FieldName", "FieldType", "Optional","Comment"}));
        for (ApiFieldInfo apiFieldInfo : lists)
        {
            System.out.println(String.format("%-50s\t%-10s\t%-20s\t%-20s", new Object[]{apiFieldInfo.getName(), apiFieldInfo.getFieldType(), apiFieldInfo.isOptional(), apiFieldInfo.getComment()}));
        }
    }
}
