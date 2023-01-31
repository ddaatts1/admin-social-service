package com.epay.ewallet.service.admin.mapperDataOne;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.epay.ewallet.service.admin.model.User;

@Mapper
public interface IUser {

	Map loadUserByPhone(String phone);

	User getUserByPhone(@Param("PHONE_NUMBER") String phone);

	User getUserById(String userId);

	String getPositionByPhone(@Param("PHONE_NUMBER") String phone);
	List<User> getAllUserByPosts(@Param("list") List<String> postId);

}
