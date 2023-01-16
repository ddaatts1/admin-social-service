package com.epay.ewallet.service.admin.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.epay.ewallet.service.admin.model.Groupsetting;


@Repository
public interface GroupsettingRepository extends MongoRepository<Groupsetting, String> {
    
    @Query("{'groupId' : ?0 }")
    Groupsetting findBygroupIdAndReferenceId(String groupId);

}