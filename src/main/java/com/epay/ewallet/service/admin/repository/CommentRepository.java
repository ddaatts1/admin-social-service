package com.epay.ewallet.service.admin.repository;

import com.epay.ewallet.service.admin.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends MongoRepository<Comment,String> {
}

