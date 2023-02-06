package com.epay.ewallet.service.admin.repository;

import com.epay.ewallet.service.admin.model.Posts;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostsRepository extends MongoRepository<Posts,String> {
    @Query("{'_id' : ?0 }")
    Optional<Posts> findById(String _id);
}
