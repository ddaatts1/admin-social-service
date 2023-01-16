package com.epay.ewallet.service.admin.service;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.epay.ewallet.service.admin.mapperDataOne.IUser;

@Service
public class JwtUserDetailsService implements UserDetailsService {

	@Autowired
	private IUser userDao;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Map user = userDao.loadUserByPhone(username);
		if (user == null) {
			// throw new UsernameNotFoundException("User not found with username: " +
			// username);
			return null;
		}
		String userName = (String) user.get("PHONE");
		String pass = (String) user.get("PASSWORD");

		return new org.springframework.security.core.userdetails.User(userName, pass, new ArrayList<>());
	}

}
