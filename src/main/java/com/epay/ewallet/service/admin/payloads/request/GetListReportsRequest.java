package com.epay.ewallet.service.admin.payloads.request;

import lombok.Data;

@Data
public class GetListReportsRequest {
    private String postId;


    @Override
    public String toString() {
        return "GetListReportsRequest{" +
                "postId='" + postId + '\'' +
                '}';
    }
}
