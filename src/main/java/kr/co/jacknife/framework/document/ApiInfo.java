package kr.co.jacknife.framework.document;

import kr.co.jacknife.framework.document.annotation.ResponseErrorCodes;
import kr.co.jacknife.framework.document.annotation.ResponseSuccessCodes;
import kr.co.jacknife.framework.document.annotation.RestApiParam;

import java.util.ArrayList;
import java.util.List;

public class ApiInfo implements Comparable<ApiInfo>{

    private String code;
    private String name;
    private String url;
    private String method;
    private String comment;

    private List<RestApiParam> headerParamList;
    private List<RestApiParam> pathParamList;
    private List<RestApiParam> queryParamList;
    private List<ApiFieldInfo> requestBodyPropList;
    private List<ApiFieldInfo> responseBodyPropList;

    private ResponseSuccessCodes successCodes;
    private ResponseErrorCodes errorCodes;

    public ApiInfo()
    {
        this.headerParamList = new ArrayList<>();
        this.pathParamList = new ArrayList<>();
        this.queryParamList = new ArrayList<>();
        this.requestBodyPropList = new ArrayList<>();
        this.responseBodyPropList = new ArrayList<>();
    }

    public String getCode() {
        return code;
    }

    public ApiInfo setCode(String code) {
        this.code = code;
        return this;
    }

    public String getName() {
        return name;
    }

    public ApiInfo setName(String name) {
        this.name = name;
        this.comment = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public ApiInfo setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public ApiInfo setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public ApiInfo setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public List<RestApiParam> getPathParamList() {
        return pathParamList;
    }

    public ApiInfo setPathParamList(List<RestApiParam> pathParamList) {
        this.pathParamList = pathParamList;
        return this;
    }

    public List<RestApiParam> getQueryParamList() {
        return queryParamList;
    }

    public ApiInfo setQueryParamList(List<RestApiParam> queryParamList) {
        this.queryParamList = queryParamList;
        return this;
    }


    public ResponseSuccessCodes getSuccessCodes() {
        return successCodes;
    }

    public ApiInfo setSuccessCodes(ResponseSuccessCodes successCodes) {
        this.successCodes = successCodes;
        return this;
    }

    public ResponseErrorCodes getErrorCodes() {
        return errorCodes;
    }

    public ApiInfo setErrorCodes(ResponseErrorCodes errorCodes) {
        this.errorCodes = errorCodes;
        return this;
    }

    public List<ApiFieldInfo> getRequestBodyPropList() {
        return requestBodyPropList;
    }

    public void setRequestBodyPropList(List<ApiFieldInfo> requestBodyPropList) {
        this.requestBodyPropList = requestBodyPropList;
    }

    public List<ApiFieldInfo> getResponseBodyPropList() {
        return responseBodyPropList;
    }

    public void setResponseBodyPropList(List<ApiFieldInfo> responseBodyPropList) {
        this.responseBodyPropList = responseBodyPropList;
    }

    public List<RestApiParam> getHeaderParamList() {
        return headerParamList;
    }

    public void setHeaderParamList(List<RestApiParam> headerParamList) {
        this.headerParamList = headerParamList;
    }

    @Override
    public int compareTo(ApiInfo o) {
        String srcPrefix = this.getCode().split("-")[0];
        String othersPrefix = o.getCode().split("-")[0];
        int prefixCompareVal = srcPrefix.compareTo(othersPrefix);
        if (prefixCompareVal != 0)
            return prefixCompareVal;
        String srcIdx = this.getCode().split("-")[1];
        String othersIdx = o.getCode().split("-")[1];
        Long srcIdxL = Long.parseLong(srcIdx);
        Long othersL = Long.parseLong(othersIdx);
        return srcIdxL.compareTo(othersL);
    }

}
