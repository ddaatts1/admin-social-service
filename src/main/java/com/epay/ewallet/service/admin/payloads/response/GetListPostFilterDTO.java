package com.epay.ewallet.service.admin.payloads.response;

import com.epay.ewallet.service.admin.model.Comment;
import com.epay.ewallet.service.admin.model.Posts;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GetListPostFilterDTO {
    List<PostDTO> listPost;
    List<Comment> listComment;

    public GetListPostFilterDTO(List<PostDTO> postDTOList) {
        listPost = postDTOList;
    }
}
