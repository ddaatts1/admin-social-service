package com.epay.ewallet.service.admin.repository;

import com.epay.ewallet.service.admin.model.Posts;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostsRepository extends MongoRepository<Posts,String> {
}
