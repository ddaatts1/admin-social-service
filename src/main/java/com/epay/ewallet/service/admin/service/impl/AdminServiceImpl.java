package com.epay.ewallet.service.admin.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.epay.ewallet.service.admin.payloads.request.AssignAdminRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epay.ewallet.service.admin.constant.EcodeConstant;
import com.epay.ewallet.service.admin.model.Ecode;
import com.epay.ewallet.service.admin.model.Groupsetting;
import com.epay.ewallet.service.admin.model.User;
import com.epay.ewallet.service.admin.model.UserGroup;
import com.epay.ewallet.service.admin.payloads.request.ApprovePostRequest;
import com.epay.ewallet.service.admin.payloads.request.OnoffAutoRemoveRequest;
import com.epay.ewallet.service.admin.payloads.response.CommonResponse;
import com.epay.ewallet.service.admin.repository.AdminRepositoryNative;
import com.epay.ewallet.service.admin.repository.GroupsettingRepository;
import com.epay.ewallet.service.admin.repository.UserGroupRepository;
import com.epay.ewallet.service.admin.service.AdminService;
import com.epay.ewallet.service.admin.service.CodeService;

@Service
public class AdminServiceImpl implements AdminService {

	@Autowired
	private GroupsettingRepository groupsettingRepository;

	
	@Autowired
	private AdminRepositoryNative adminRepositoryNative;
	
	@Autowired
	private CodeService codeService;
	
//	@Autowired
//	private CodeService code;
	
	private static final Logger log = LogManager.getLogger(AdminServiceImpl.class);
	
	@Autowired
	UserGroupRepository userGroupRepository;

	@Override
	public CommonResponse<Object> approve(ApprovePostRequest request, User user, String requestId) {
		CommonResponse<Object> response = new CommonResponse<> ();

//		LinkedHashMap<String, Object> data = new LinkedHashMap<>();

		log.info("Approve post service start.......");

		response.setEcode(EcodeConstant.SUCCESS);
		response.setMessage(EcodeConstant.SUCCESS_MSG);
		response.setP_ecode(EcodeConstant.SUCCESS);
		response.setP_message(EcodeConstant.SUCCESS_MSG);
		
		CommonResponse<Object> validate_result = validate(request);
		if (!validate_result.getEcode().equals(EcodeConstant.SUCCESS)) {
			log.info("ecode {}", validate_result.getEcode());
			response.setEcode(validate_result.getEcode());
			response.setMessage(codeService.getMessageByCode(user.getLang(), validate_result.getEcode()));
			response.setP_ecode(EcodeConstant.ERR);
			response.setP_message(EcodeConstant.ERR_MSG);
//			data.put("result", false);
//			response.setData(data);
			return response;
		}

		UserGroup userGroup = userGroupRepository.findByUserId(Integer.toString(user.getId()), "1");
		log.info("Check quyền Admin/SupperAdmin");
		if (userGroup != null) {
			if (userGroup.getRoleId() == 1) { //neu la user thuong
				response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
//				data.put("result", false);
//				response.setData(data);
				return response;
			}
		} else { //neu la user thuong
			response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
//			data.put("result", false);
//			response.setData(data);
			return response;
		}
		

		Groupsetting groupsetting = groupsettingRepository.findBygroupIdAndReferenceId(request.getGroupId());
		log.info("Check ApprovePost co ton tai trong DB");
		if (groupsetting == null) {
			log.info("Insert groupsetting");
			groupsetting = new Groupsetting();
			groupsetting.setId("grsetting_" + UUID.randomUUID().toString().replace("-", ""));
			groupsetting.setEn_admin_approve_post(request.getFlag());
			groupsetting.setGroupId(request.getGroupId());
			groupsetting.setCreatedDate(new Date());
			groupsetting.setUpdateDate(new Date());
			groupsetting.setUserId(user.getId() + ""); //user cap nhat ban ghi
			groupsettingRepository.save(groupsetting);
			return response;
		} else {
			log.info("Update groupsetting");
			groupsetting.setEn_admin_approve_post(request.getFlag());
			groupsetting.setUpdateDate(new Date());
			groupsetting.setUserId(user.getId() + ""); //user cap nhat ban ghi
			groupsettingRepository.save(groupsetting);
		}
//		data.put("result", true);
//		response.setData(data);
		response.setEcode(EcodeConstant.SUCCESS);
		return response;
	}

	
	
	
	
	
	@Override
	public CommonResponse<Object> on_off_auto_remove(OnoffAutoRemoveRequest request, User user, String requestId) {
		CommonResponse<Object> response = new CommonResponse<> ();

		log.info("Approve post service start.......");

		response.setEcode(EcodeConstant.SUCCESS);
		
		
		
//		CommonResponse<Object> validate_result = validate(request);
//		if (!validate_result.getEcode().equals(EcodeConstant.SUCCESS)) {
//			log.info("ecode {}", validate_result.getEcode());
//			response.setEcode(validate_result.getEcode());
//			response.setMessage(code.getMessageByCode(user.getLang(), validate_result.getEcode()));
//			response.setP_ecode(EcodeConstant.ERR);
//			response.setP_message(EcodeConstant.ERR_MSG);
////			data.put("result", false);
////			response.setData(data);
//			return response;
//		}

		UserGroup userGroup = userGroupRepository.findByUserId(Integer.toString(user.getId()), "1");
		log.info("Check quyền Admin/SupperAdmin");
		if (userGroup != null) {
			if (userGroup.getRoleId() == 1) { //neu la user thuong
				response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
//				data.put("result", false);
//				response.setData(data);
				return response;
			}
		} else { //neu la user thuong
			response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
//			data.put("result", false);
//			response.setData(data);
			return response;
		}
		
		
		long matchedCount = adminRepositoryNative.update_on_off_auto_remove(request);
		
		if (matchedCount > 0) { //NEU UPDATE OK
			response.setEcode(EcodeConstant.SUCCESS);
		}else {
			response.setEcode(EcodeConstant.ERR);
		}

		
		Ecode ecode = codeService.getEcode(response.getEcode(), user.getLang());
		response.setMessage(ecode.getMessage());
		response.setP_message(ecode.getP_message());
		response.setP_ecode(ecode.getP_ecode());
		return response;
	}

	@Override
	public CommonResponse<Object> assign_admin(AssignAdminRequest request, User user, String requestId) {

		CommonResponse<Object> response = new CommonResponse<Object>();

		// kiem tra flag
		List<String> arr = new ArrayList<String>();
		arr.add("ON");
		arr.add("OFF");
		if (!arr.contains(request.getFlag())) {
			response.setEcode(EcodeConstant.FLAG_NULL_EMPTY);
			return response;
		}

		log.info("assign admin service start .....");


		UserGroup userGroup = userGroupRepository.findByUserId(Integer.toString(user.getId()), "1");
		log.info("Check quyền Admin/SupperAdmin");
		if (userGroup != null) {
			if (userGroup.getRoleId() == 1) { //neu la user thuong
				response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
//				data.put("result", false);
//				response.setData(data);
				return response;
			}
		} else { //neu la user thuong
			response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
//			data.put("result", false);
//			response.setData(data);
			return response;
		}




		long count = adminRepositoryNative.assignAdmin(request);

		if (count > 0) { //
			response.setEcode(EcodeConstant.SUCCESS);
		}else {
			response.setEcode(EcodeConstant.ERR);
		}


		return response;
	}


	@Override
	public CommonResponse<Object> listAdmin(ApprovePostRequest request, User user, String requestId) {
		CommonResponse<Object> response = new CommonResponse<> ();

		response.setEcode(EcodeConstant.SUCCESS);
		response.setMessage(EcodeConstant.SUCCESS_MSG);
		response.setP_ecode(EcodeConstant.SUCCESS);
		response.setP_message(EcodeConstant.SUCCESS_MSG);
		
//		CommonResponse<Object> validate_result = validate(request);
//		if (!validate_result.getEcode().equals(EcodeConstant.SUCCESS)) {
//			log.info("ecode {}", validate_result.getEcode());
//			response.setEcode(validate_result.getEcode());
//			response.setMessage(code.getMessageByCode(user.getLang(), validate_result.getEcode()));
//			response.setP_ecode(EcodeConstant.ERR);
//			response.setP_message(EcodeConstant.ERR_MSG);
////			data.put("result", false);
////			response.setData(data);
//			return response;
//		}

		
		UserGroup userGroup = userGroupRepository.findByUserId(Integer.toString(user.getId()), "1");
		log.info("Check quyền Admin/SupperAdmin");
		if (userGroup != null) {
			if (userGroup.getRoleId() == 1) { //neu la user thuong
				response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
//				data.put("result", false);
//				response.setData(data);
				return response;
			}
		} else { //neu la user thuong
			response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
//			data.put("result", false);
//			response.setData(data);
			return response;
		}
		
		
		
		ArrayList<HashMap<String, String>> arrLAdmin = 	adminRepositoryNative.getListAdmin(request.getGroupId());
	
		if (arrLAdmin != null) {
			response.setData(arrLAdmin);
		}


		
		response.setEcode(EcodeConstant.SUCCESS);
		return response;
	}
	



	
	
	
	
	@Override
	public CommonResponse<Object> validate(ApprovePostRequest request) {
		// TODO Auto-generated method stub
		CommonResponse<Object> response = new CommonResponse<>();
		log.info("Validate start.......");

		response.setEcode(EcodeConstant.SUCCESS);
		response.setMessage(EcodeConstant.SUCCESS_MSG);
		response.setP_ecode(EcodeConstant.SUCCESS);
		response.setP_message(EcodeConstant.SUCCESS_MSG);

		if (request.getGroupId() == null) {
			response.setEcode(EcodeConstant.GROUP_ID_NULL_EMPTY);
		}

		if (request.getFlag() == null) {
			response.setEcode(EcodeConstant.FLAG_NULL_EMPTY);
		}

		List<String> arr = new ArrayList<String>();
		arr.add("ON");
		arr.add("OFF");
		if (!arr.contains(request.getFlag())) {
			response.setEcode(EcodeConstant.FLAG_NULL_EMPTY);
		}
		log.info("Validate end.");

		return response;
	}




}
