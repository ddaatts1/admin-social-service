package com.epay.ewallet.service.admin.payloads.request;

import lombok.Data;

@Data
public class GetListReportsRequest {
    private String objectId;


    @Override
    public String toString() {
        return "GetListReportsRequest{" +
                "postId='" + objectId + '\'' +
                '}';
    }
}
