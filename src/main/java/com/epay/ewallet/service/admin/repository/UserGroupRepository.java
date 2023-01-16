package com.epay.ewallet.service.admin.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.epay.ewallet.service.admin.model.UserGroup;

@Repository
public interface UserGroupRepository extends MongoRepository<UserGroup, String> {

	@Query("{'userId' : :#{#userId}, 'status' : :#{#status}}")
	UserGroup findByUserId(String userId, String status);
}
