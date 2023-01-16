package com.epay.ewallet.service.admin.payloads.request;

public class DeviceRequest {
	private String deviceId;
	private String deviceName;
	private String os;
	private String versionId;
	private String deviceTOKEN;
	private String ipAddress;

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getVersionId() {
		return versionId;
	}

	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}

	public String getDeviceTOKEN() {
		return deviceTOKEN;
	}

	public void setDeviceTOKEN(String deviceTOKEN) {
		this.deviceTOKEN = deviceTOKEN;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

}
