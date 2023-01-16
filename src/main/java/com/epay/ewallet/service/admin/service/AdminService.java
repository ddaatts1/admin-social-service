package com.epay.ewallet.service.admin.service;

import com.epay.ewallet.service.admin.model.User;
import com.epay.ewallet.service.admin.payloads.request.ApprovePostRequest;
import com.epay.ewallet.service.admin.payloads.request.AssignAdminRequest;
import com.epay.ewallet.service.admin.payloads.request.OnoffAutoRemoveRequest;
import com.epay.ewallet.service.admin.payloads.response.CommonResponse;


public interface AdminService {

	CommonResponse<Object> approve(ApprovePostRequest request, User user, String requestId);

	CommonResponse<Object> validate(ApprovePostRequest request);

	CommonResponse<Object> listAdmin(ApprovePostRequest request, User user, String requestId);
	
	CommonResponse<Object> on_off_auto_remove(OnoffAutoRemoveRequest request, User user, String requestId);

	CommonResponse<Object> assign_admin(AssignAdminRequest request,User user, String requestId);

}
