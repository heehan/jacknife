package kr.co.jacknife.framework.document;

import java.util.ArrayList;
import java.util.List;

public class ApiFieldInfo {
    private String name;
    private String fieldType;
    private boolean optional;
    private String comment;
    private Integer depth;

    private List<ApiFieldInfo> apiFieldInfoList = new ArrayList<>();

    public List<ApiFieldInfo> getApiFieldInfoList() { return apiFieldInfoList; }
    public ApiFieldInfo setApiFieldInfoList(List<ApiFieldInfo> apiFieldInfoList) { this.apiFieldInfoList = apiFieldInfoList; return this; }

    public ApiFieldInfo() { this(1); }
    public ApiFieldInfo(Integer depth) {this.depth = depth;};

    public String getName() {
        StringBuilder builder = new StringBuilder();
        for (int i=0;i<this.depth-1;++i)
            builder.append("    ");
        return builder.append(name).toString();
    }
    public ApiFieldInfo setName(String name) { this.name = name; return this; }
    public String getFieldType() { return fieldType; }
    public ApiFieldInfo setFieldType(String fieldType) { this.fieldType = fieldType; return this; }
    public boolean isOptional() { return optional; }
    public ApiFieldInfo setOptional(boolean optional) { this.optional = optional; return this; }
    public String getComment() { return comment; }
    public ApiFieldInfo setComment(String comment) { this.comment = comment; return this; }
    public Integer getDepth() { return depth; }
    public ApiFieldInfo setDepth(Integer depth) { this.depth = depth; return this; }


}
