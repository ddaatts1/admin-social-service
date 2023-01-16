package com.epay.ewallet.service.admin.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.epay.ewallet.service.admin.model.Groupsetting;
import com.mongodb.client.MongoClient;


@Repository
public class GroupsettingRepositoryNative  {
    
	@Autowired
	private MongoClient mongoClient;
	
	@Value("${spring.data.mongodb.database_}")
	private String db;
	
	
	

}