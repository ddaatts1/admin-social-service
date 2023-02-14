package com.epay.ewallet.service.admin.payloads.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OnoffAutoRemoveRequest {

    private String groupId;
    private String flag;
    private String limitReportPost;
    private String limitReportComment;


    public String getLimitReportPost() {
        return limitReportPost;
    }

    public void setLimitReportPost(String limitReportPost) {
        this.limitReportPost = limitReportPost;
    }

    public String getLimitReportComment() {
        return limitReportComment;
    }

    public void setLimitReportComment(String limitReportComment) {
        this.limitReportComment = limitReportComment;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return "OnoffAutoRemoveRequest [groupId=" + groupId + ", flag=" + flag + ", limitReportPost=" + limitReportPost
                + ", limitReportComment=" + limitReportComment + "]";
    }

}
