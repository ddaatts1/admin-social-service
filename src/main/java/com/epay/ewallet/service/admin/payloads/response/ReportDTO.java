package com.epay.ewallet.service.admin.payloads.response;

import lombok.Data;

import java.util.List;

@Data
public class ReportDTO {

    private int spam;
    private int falseNew;
    private int breaksCompanyRule;
    private int memberConflict;
    private List<String> reportReason;
    private String rejectReason;
    private String appealReason;
    private String postStatus ;

}
