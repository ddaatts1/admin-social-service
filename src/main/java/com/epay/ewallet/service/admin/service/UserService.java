package com.epay.ewallet.service.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epay.ewallet.service.admin.mapperDataOne.IUser;
import com.epay.ewallet.service.admin.model.User;

import java.util.List;

@Service
public class UserService {

	@Autowired
	private IUser userDao;

	public User getUser(String phone) {
		return userDao.getUserByPhone(phone);
	}
	public User getUserById(String Id){
		return  userDao.getUserById(Id);
	}

	public List<User> getAllUserByPosts(List<String> postId){
		return  userDao.getAllUserByPosts(postId);
	}
}
