package com.epay.ewallet.service.admin.model;


import com.epay.ewallet.service.admin.payloads.response.UserDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import javax.validation.constraints.Max;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "posts")
public class Posts {
    @Id
    private String _id;
    private String userId;
    private String groupId;
    private String content;
    private String status;
    private Date createDate;
    private Date editDate;
    private String byAdmin;
    private String reason;
    private String marketType;
    private String category;
    private String price;
    private String postByRole;
    private int countReport;


    public Posts() {
    }

    public Posts(String post_id, String userId){
        this.set_id(post_id);
        this.setUserId(userId);
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public Date getEditDate() {
        return editDate;
    }

    public void setEditDate(Date editDate) {
        this.editDate = editDate;
    }

    public String getByAdmin() {
        return byAdmin;
    }

    public void setByAdmin(String byAdmin) {
        this.byAdmin = byAdmin;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMarketType() {
        return marketType;
    }

    public void setMarketType(String marketType) {
        this.marketType = marketType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPostByRole() {
        return postByRole;
    }

    public void setPostByRole(String postByRole) {
        this.postByRole = postByRole;
    }

    public int getCountReport() {
        return countReport;
    }

    public void setCountReport(int countReport) {
        this.countReport = countReport;
    }
}
