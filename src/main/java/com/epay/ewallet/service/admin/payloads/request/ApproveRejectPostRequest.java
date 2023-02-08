package com.epay.ewallet.service.admin.payloads.request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ApproveRejectPostRequest {

    private  String postId;
    private String flag;
    private String reportType;
    private String reason = null;

    @JsonProperty("referenceId")
    public void setReferenceId(String referenceId) {
        this.postId = referenceId;
    }

    @JsonProperty("appealId")
    public void setAppealId(String appealId) {
        this.postId = appealId;
    }

    @Override
    public String toString() {
        return "ApproveRejectPostRequest{" +
                "postId='" + postId + '\'' +
                ", flag='" + flag + '\'' +
                ", reportType='" + reportType + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
