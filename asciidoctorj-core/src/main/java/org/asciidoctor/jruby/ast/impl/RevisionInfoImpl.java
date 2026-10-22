package org.asciidoctor.jruby.ast.impl;

import java.util.Map;

import org.asciidoctor.ast.RevisionInfo;

public class RevisionInfoImpl implements RevisionInfo {

    private static String REV_DATE_ATTRIBUTE_NAME = "revdate";
    private static String REV_NUMBER_ATTRIBUTE_NAME = "revnumber";
    private static String REV_REMARK_ATTRIBUTE_NAME = "revremark";

    private String date;
    private String number;
    private String remark;

    public static RevisionInfo getInstance(Map<String, Object> attributes) {

        RevisionInfoImpl revisionInfo = new RevisionInfoImpl();

        if (attributes.containsKey(REV_DATE_ATTRIBUTE_NAME)) {
            revisionInfo.setDate((String) attributes.get(REV_DATE_ATTRIBUTE_NAME));
        }

        if (attributes.containsKey(REV_NUMBER_ATTRIBUTE_NAME)) {
            revisionInfo.setNumber((String) attributes.get(REV_NUMBER_ATTRIBUTE_NAME));
        }

        if (attributes.containsKey(REV_REMARK_ATTRIBUTE_NAME)) {
            revisionInfo.setRemark((String) attributes.get(REV_REMARK_ATTRIBUTE_NAME));
        }

        return revisionInfo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}
