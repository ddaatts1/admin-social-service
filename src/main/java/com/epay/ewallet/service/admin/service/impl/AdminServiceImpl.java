package com.epay.ewallet.service.admin.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.epay.ewallet.service.admin.constant.Constant;
import com.epay.ewallet.service.admin.model.*;
import com.epay.ewallet.service.admin.payloads.request.AssignAdminRequest;
import com.epay.ewallet.service.admin.payloads.request.GetListPostFilterRequest;
import com.epay.ewallet.service.admin.payloads.response.UserDTO;
import com.epay.ewallet.service.admin.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epay.ewallet.service.admin.constant.EcodeConstant;
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

	@Autowired
	UserService userService;

	public AdminServiceImpl (UserGroupRepository userGroupRepository){
		this.userGroupRepository = userGroupRepository;
	}
	
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

		// kiem tra user status
		User userCheck = userService.getUserById(request.getUserIdDest());
		if(userCheck == null || userCheck.getStatus() < 3){
			response.setEcode(EcodeConstant.INVALID_USER);
			return  response;
		}


		log.info("assign admin service start .....");


		UserGroup userGroup = userGroupRepository.findByUserId(Integer.toString(user.getId()), "1");
		log.info("Check quyền SupperAdmin");
		if (userGroup != null) {
			if (userGroup.getRoleId() <3) { //neu user khong phai la super admin
				response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
//				data.put("result", false);
//				response.setData(data);
				return response;
			}
		} else { //neu user khong phai la super admin
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
	public CommonResponse<Object> assign_superadmin(AssignAdminRequest request, User user, String requestId) {

		CommonResponse<Object> response = new CommonResponse<>();

		UserGroup userGroup = userGroupRepository.findByUserId(Integer.toString(user.getId()), "1");
		log.info("Check quyền SupperAdmin");
		if (userGroup != null) {
			if (userGroup.getRoleId() <3) { //neu user khong phai la super admin
				response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
//				data.put("result", false);
//				response.setData(data);
				return response;
			}
		} else { //neu user khong phai la super admin
			response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
//			data.put("result", false);
//			response.setData(data);
			return response;
		}

		// kiem tra xem user duoc nhuong quyen co phai la admin
		UserGroup checkadmin = userGroupRepository.findByUserId(request.getUserIdDest(),"1");
		if(checkadmin == null || checkadmin.getRoleId() !=2){
			response.setEcode(EcodeConstant.INVALID_USER);
			return response;
		}


		long count = adminRepositoryNative.assignSuperAdmin(request,userGroup);

		if(count == 2){
			response.setEcode(EcodeConstant.SUCCESS);
		}
		else{
			response.setEcode(EcodeConstant.ERR);
		}


		return response;
	}

	@Override
	public CommonResponse<Object> get_list_post_filter(GetListPostFilterRequest request, User user, String requestId) {

		CommonResponse<Object> response = new CommonResponse<>();

		// kiem tra flag
		List<String> arr = new ArrayList<String>();
		arr.add("SAVED");
		arr.add("PENDING");
		arr.add("HIDDEN");
		arr.add("REPORTED_REMOVED");
		if (!arr.contains(request.getFlag())) {
			response.setEcode(EcodeConstant.FLAG_NULL_EMPTY);
			return response;
		}


		//kiem tra quyen admin neu scope = ALL
		if(request.getScope().equalsIgnoreCase("ALL")){
			//check user co phai la admin
			UserGroup userGroup = userGroupRepository.findByUserId(Integer.toString(user.getId()), "1");
			log.info("Check quyền admin: "+user.getId());

			if (userGroup != null) {
				if (userGroup.getRoleId() <2) { //neu user khong phai la  admin
					response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
//				data.put("result", false);
//				response.setData(data);
					return response;
				}
			} else { //neu user khong phai la  admin
				response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
//			data.put("result", false);
//			response.setData(data);
				return response;
			}
		}

		List<Posts> listPosts = adminRepositoryNative.getListPostFilter(request,user);

		if(listPosts.size() == 0){
			response.setEcode(EcodeConstant.ERR);
			response.setData(null);
			return response;
		}
		else {
			response.setEcode(EcodeConstant.SUCCESS);
		}
		//lay danh sach userid tuong ung voi  moi post
		List<String> userIds = listPosts.stream().map(p->p.getUserId()).collect(Collectors.toList());
		// fetch user va image de them vao post

		List<Document> listMedia = adminRepositoryNative.getListMediaByListPosts(listPosts);
		Map<String,Document> postId_media = listMedia.stream().collect(Collectors.toMap(p->p.get("referenceId",String.class),p->p,(o, o2) -> {
			return o.get("seq",Integer.class) < o2.get("seq",Integer.class)? o : o2;
		}));

		List<User> listUser = userService.getAllUserByPosts(listPosts.stream().map(p->p.getUserId()).collect(Collectors.toList()));

		Map<String,UserDTO> userId_UserDTO = listUser.stream().collect(Collectors.toMap(u->Integer.toString(u.getId()),u->new UserDTO(u)));

		for (Posts p : listPosts){

			if (postId_media.get(p.get_id()) != null) {
				p.setImage(postId_media.get(p.get_id()).get("mediaUrl",String.class));
			}
			p.setUser(userId_UserDTO.get(p.getUserId()));
		}


		response.setData(listPosts);


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
