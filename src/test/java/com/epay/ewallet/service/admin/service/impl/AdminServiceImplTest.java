package com.epay.ewallet.service.admin.service.impl;

import com.epay.ewallet.service.admin.constant.EcodeConstant;
import com.epay.ewallet.service.admin.model.Posts;
import com.epay.ewallet.service.admin.model.User;
import com.epay.ewallet.service.admin.model.UserGroup;
import com.epay.ewallet.service.admin.payloads.request.GetListPostFilterRequest;
import com.epay.ewallet.service.admin.payloads.response.CommonResponse;
import com.epay.ewallet.service.admin.payloads.response.UserDTO;
import com.epay.ewallet.service.admin.repository.AdminRepositoryNative;
import com.epay.ewallet.service.admin.repository.UserGroupRepository;
import com.epay.ewallet.service.admin.service.UserService;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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

        UserGroup userGroup = new UserGroup();
        userGroup.setRoleId(3);
        Mockito.when(userGroupRepository.findByUserId(Integer.toString(user.getId()), "1")).thenReturn(userGroup);

        List<Posts> listPosts = new ArrayList<>();
        Posts post = new Posts();
        post.set_id("1");
        post.setUserId("1");
        listPosts.add(post);
        Mockito.when(adminRepositoryNative.getListPostFilter(Mockito.any(GetListPostFilterRequest.class), Mockito.any(User.class))).thenReturn(listPosts);

        List<Document> listMedia = new ArrayList<>();
        Document media = new Document();
        media.put("referenceId", "1");
        media.put("mediaUrl", "url");
        listMedia.add(media);
        Mockito.when(adminRepositoryNative.getListMediaByListPosts(Mockito.any(List.class))).thenReturn(listMedia);

        User user1 = new User();
        user1.setId(2323);
        List<User> listUser = new ArrayList<>();
        listUser.add(user1);
        Mockito.when(userService.getAllUserByPosts(Mockito.any(List.class))).thenReturn(listUser);

        CommonResponse<Object> response = adminServiceImpl.get_list_post_filter(request, user, "requestId");

        assertEquals(EcodeConstant.SUCCESS, response.getEcode());
        assertEquals(1, ((List<Posts>) response.getData()).size());
    }


    }


