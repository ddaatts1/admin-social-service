package com.epay.ewallet.service.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.Date;

@Document(value = "tags")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Tags {

    @Id
    private String _id;
    private String userId;
    private String postId;
    private Date createDate;
    private String status;


}
