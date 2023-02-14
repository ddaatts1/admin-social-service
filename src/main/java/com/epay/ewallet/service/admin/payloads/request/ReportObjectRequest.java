package com.epay.ewallet.service.admin.payloads.request;

import lombok.Data;

@Data
public class ReportObjectRequest {

    private String referenceId;
    private String flag;
    private String reportType;
    private String type;
    private String reason;

}
