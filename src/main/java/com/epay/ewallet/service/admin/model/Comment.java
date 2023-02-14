package com.epay.ewallet.service.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.Date;

@Document(collection = "comments")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Comment {

    @Id
    private String _id;
    private String userId;
    private String referenceId;
    private String status;
    private String content;
    private Date createDate;
    private Date editDate;
    private String byRole;
    private int countReport;

}
