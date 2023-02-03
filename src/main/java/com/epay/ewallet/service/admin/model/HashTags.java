package com.epay.ewallet.service.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.Date;

@Document(value = "hashtags")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HashTags {

    @Id
    String _id;
    String tagcontent;
    String postId;
    String status;
    String seq;
    Date createDate;

}
