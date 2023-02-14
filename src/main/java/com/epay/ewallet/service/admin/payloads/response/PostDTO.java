package com.epay.ewallet.service.admin.payloads.response;

import com.epay.ewallet.service.admin.model.HashTags;
import com.epay.ewallet.service.admin.model.Posts;
import com.epay.ewallet.service.admin.model.Tags;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.util.Date;
import java.util.List;

@Data
public class PostDTO {

    private String postId;
    private String userId;
    private String content;
    private String status;
    private Date postedDate;
    private int countReport;
    private String price;

    List<String> images;
    UserDTO user;
    List<HashTags> hashTags;
    List<Tags> tagList;

    public PostDTO(Posts posts){
        postId = posts.get_id();
        countReport = posts.getCountReport();
        status = posts.getStatus();
        postedDate = posts.getCreateDate();
        content = posts.getContent();
        userId = posts.getUserId();
        price = posts.getPrice();

    }


}
