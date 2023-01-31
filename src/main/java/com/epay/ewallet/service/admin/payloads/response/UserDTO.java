package com.epay.ewallet.service.admin.payloads.response;


import com.epay.ewallet.service.admin.model.User;
import lombok.Data;

@Data
public class UserDTO {

    String userId;
    String avartar;
    String name;


    public UserDTO(User user){
        this.setUserId( Integer.toString(user.getId()));
        this.setAvartar(user.getAvatar());
        this.setName(user.getName());
    }

    public UserDTO() {

    }
}
