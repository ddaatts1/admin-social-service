package com.epay.ewallet.service.admin.payloads.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignAdminRequest {
    private String userIdDest;
    private String flag;


    public AssignAdminRequest() {
    }

    public AssignAdminRequest(String userIdDest, String flag) {
        this.userIdDest = userIdDest;
        this.flag = flag;
    }

    public String getUserIdDest() {
        return userIdDest;
    }

    public void setUserIdDest(String userIdDest) {
        this.userIdDest = userIdDest;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return "AssignAdminRequest{" +
                "userIdDest='" + userIdDest + '\'' +
                ", flag='" + flag + '\'' +
                '}';
    }
}
