package com.epay.ewallet.service.admin.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.epay.ewallet.service.admin.model.User;

@Repository
public interface UserRepository extends MongoRepository<User, Integer> {
}
