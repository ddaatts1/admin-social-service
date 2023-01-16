package com.epay.ewallet.service.admin.model;

public class User {

	private int id;
	private String phoneNumber;
	private String name;
	private String email;
	private Integer status;
	private String personalId;
	private String personalIdType;
	private String address;
	private String lang;
	private String password;
	private int companyId;
	private String avatar;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getPersonalId() {
		return personalId;
	}

	public void setPersonalId(String personalId) {
		this.personalId = personalId;
	}

	public String getPersonalIdType() {
		return personalIdType;
	}

	public void setPersonalIdType(String personalIdType) {
		this.personalIdType = personalIdType;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	/**
	 * @return the avatar
	 */
	public String getAvatar() {
		return avatar;
	}

	/**
	 * @param avatar the avatar to set
	 */
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", phoneNumber=" + phoneNumber + ", name=" + name + ", email=" + email + ", status="
				+ status + ", personalId=" + personalId + ", personalIdType=" + personalIdType + ", address=" + address
				+ ", lang=" + lang + ", password=" + password + ", companyId=" + companyId + ", avatar=" + avatar + "]";
	}

	
	
}
