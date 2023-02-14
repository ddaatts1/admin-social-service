package com.epay.ewallet.service.admin.service;

import com.epay.ewallet.service.admin.model.User;
import com.epay.ewallet.service.admin.payloads.request.*;
import com.epay.ewallet.service.admin.payloads.response.CommonResponse;
import org.bson.Document;


public interface AdminService {

	CommonResponse<Object> approve(ApprovePostRequest request, User user, String requestId);

	CommonResponse<Object> validate(ApprovePostRequest request);

	CommonResponse<Object> listAdmin(ApprovePostRequest request, User user, String requestId);
	
	CommonResponse<Object> on_off_auto_remove(OnoffAutoRemoveRequest request, User user, String requestId);

	CommonResponse<Object> assign_admin(AssignAdminRequest request, User user, String requestId, Document action);

    CommonResponse<Object> assign_superadmin(AssignAdminRequest request, User user, String requestId, Document action);

    CommonResponse<Object> get_list_post_filter(GetListPostFilterRequest request,User user,String requestId);

    CommonResponse<Object> get_list_reports(GetListReportsRequest getListReportsRequest, User user, String requestId);

	CommonResponse<Object> approve_reject_post(ApproveRejectPostRequest approveRejectPostRequest, User user, String requestId, Document action);

	CommonResponse<Object> remove_reported_obj(ReportObjectRequest approveRejectPostRequest, User user, String requestId, Document action);

    CommonResponse<Object> appeal_post(AppealPostRequest appealPostRequest, User user, String requestId, Document action);

	CommonResponse<Object> report_obj(ReportObjectRequest reportObjRequest, User user, String requestId, Document action);

	CommonResponse<Object> approve_appeal(ApproveRejectPostRequest approve_appeal, User user, String requestId, Document action);
}
