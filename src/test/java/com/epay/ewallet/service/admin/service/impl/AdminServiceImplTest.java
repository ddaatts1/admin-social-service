package com.epay.ewallet.service.admin.service.impl;

import com.epay.ewallet.service.admin.constant.EcodeConstant;
import com.epay.ewallet.service.admin.model.Posts;
import com.epay.ewallet.service.admin.model.User;
import com.epay.ewallet.service.admin.model.UserGroup;
import com.epay.ewallet.service.admin.payloads.request.GetListPostFilterRequest;
import com.epay.ewallet.service.admin.payloads.response.CommonResponse;
import com.epay.ewallet.service.admin.repository.AdminRepositoryNative;
import com.epay.ewallet.service.admin.repository.UserGroupRepository;
import com.epay.ewallet.service.admin.service.UserService;
import com.mongodb.Function;
import com.mongodb.client.MongoIterable;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
class AdminServiceImplTest {

    @Mock
    private AdminRepositoryNative adminRepositoryNative;

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminServiceImpl adminServiceImpl;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetListPostFilter_Success() {
        GetListPostFilterRequest request = new GetListPostFilterRequest();
        request.setFlag("SAVED");
        request.setScope("ALL");

        User user = new User();
        user.setId(1);

        //user request la super admin
        UserGroup userGroup = new UserGroup();
        userGroup.setRoleId(3);
        when(userGroupRepository.findByUserId(Integer.toString(user.getId()), "1")).thenReturn(userGroup);

        List<Posts> listPosts = new ArrayList<>();
        Posts post = new Posts();
        post.set_id("1");
        post.setUserId("1");
        listPosts.add(post);
        listPosts.add(new Posts("2", "2"));
        when(adminRepositoryNative.getListPostFilter(any(GetListPostFilterRequest.class),
                any(User.class))).thenReturn(listPosts);

        List<Document> listMedia = new ArrayList<>();
        Document media = new Document();
        media.put("referenceId", "1");
        media.put("mediaUrl", "url");
        listMedia.add(media);
        when(adminRepositoryNative.getListMediaByListPosts(any(List.class))).thenReturn(listMedia);

        User user1 = new User();
        user1.setId(2323);
        List<User> listUser = new ArrayList<>();
        listUser.add(user1);
        when(userService.getAllUserByPosts(any(List.class))).thenReturn(listUser);

        CommonResponse<Object> response = adminServiceImpl.get_list_post_filter(request, user, "requestId");

        assertEquals(EcodeConstant.SUCCESS, response.getEcode());
        assertEquals(2, ((List<Posts>) response.getData()).size());
    }

    @Test
    public void testGetListPostFilter_FlagNull() {
        GetListPostFilterRequest request = new GetListPostFilterRequest();
        request.setFlag("Wrong flag");
        request.setScope("ALL");

        User user = new User();
        user.setId(2);

        CommonResponse<Object> response = adminServiceImpl.get_list_post_filter(request, user, "requestid");

        assertEquals(EcodeConstant.FLAG_NULL_EMPTY, response.getEcode());

    }


    @Test
    public void testGetListPostFilter_UserNotAdmin() {
        GetListPostFilterRequest request = new GetListPostFilterRequest();
        request.setScope("ALL");
        request.setFlag("SAVED");

        User user1 = new User();
        user1.setId(1);

        User user2= new User();
        user2.setId(2);

        UserGroup userGroup1 = new UserGroup();
        userGroup1.setUserId("1");
        userGroup1.setRoleId(1);

        when(userGroupRepository.findByUserId(Integer.toString(user1.getId()), "1")).thenReturn(userGroup1);
        when(userGroupRepository.findByUserId(Integer.toString(user2.getId()), "1")).thenReturn(null);
        CommonResponse<Object> response = new CommonResponse<>();
        response = adminServiceImpl.get_list_post_filter(request, user1, "request");
        assertEquals(EcodeConstant.ERROR_NOT_ADMIN, response.getEcode());
        response = adminServiceImpl.get_list_post_filter(request,user2,"request");
        assertEquals(EcodeConstant.ERROR_NOT_ADMIN,response.getEcode());
    }



}


