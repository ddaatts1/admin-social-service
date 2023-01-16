package com.epay.ewallet.service.admin.model;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;
import javax.persistence.Id;

@Document(collection = "user_group")
public class UserGroup {
	@Id
	private String Id;
	private String userId;
	private String groupId;
	private Integer roleId;
	private String type;
	private String status;
	private Date createDate;
	
	public UserGroup() {
		
	}
	
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
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
	public Integer getRoleId() {
		return roleId;
	}
	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public UserGroup(String id, String userId, String groupId, Integer roleId, String type, String status,
			Date createDate) {
		super();
		Id = id;
		this.userId = userId;
		this.groupId = groupId;
		this.roleId = roleId;
		this.type = type;
		this.status = status;
		this.createDate = createDate;
	}

}
