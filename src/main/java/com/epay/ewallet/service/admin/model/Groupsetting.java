package com.epay.ewallet.service.admin.model;

import java.util.Date;

import javax.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.querydsl.core.annotations.QueryEntity;


//@Document(collection = "approvepost")
@Document(collection = "groupsetting")
@QueryEntity
public class Groupsetting {

    @Id
    private String id;
    private String userId;
    private String groupId;
    private Date createdDate;
    private Date updateDate;
    private String en_admin_approve_post;
    
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public String getEn_admin_approve_post() {
		return en_admin_approve_post;
	}
	public void setEn_admin_approve_post(String en_admin_approve_post) {
		this.en_admin_approve_post = en_admin_approve_post;
	}
	@Override
	public String toString() {
		return "ApprovePost [id=" + id + ", userId=" + userId + ", groupId=" + groupId + ", createdDate=" + createdDate
				+ ", updateDate=" + updateDate + ", en_admin_approve_post=" + en_admin_approve_post + "]";
	}

   

    
}