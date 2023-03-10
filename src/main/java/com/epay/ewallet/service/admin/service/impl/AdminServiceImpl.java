package com.epay.ewallet.service.admin.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.epay.ewallet.service.admin.constant.Constant;
import com.epay.ewallet.service.admin.model.*;
import com.epay.ewallet.service.admin.payloads.request.*;
import com.epay.ewallet.service.admin.payloads.response.*;
import com.epay.ewallet.service.admin.repository.*;
import com.epay.ewallet.service.admin.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epay.ewallet.service.admin.constant.EcodeConstant;
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

    @Autowired
    PostsRepository postsRepository;

    public AdminServiceImpl(UserGroupRepository userGroupRepository) {
        this.userGroupRepository = userGroupRepository;
    }

    @Autowired
    CommentRepository commentRepository;
//	@Autowired
//	private CodeService code;

    private static final Logger log = LogManager.getLogger(AdminServiceImpl.class);

    @Autowired
    UserGroupRepository userGroupRepository;

    @Override
    public CommonResponse<Object> approve(ApprovePostRequest request, User user, String requestId) {
        CommonResponse<Object> response = new CommonResponse<>();

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
        log.info("Check quy???n Admin/SupperAdmin");
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
        CommonResponse<Object> response = new CommonResponse<>();

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
        log.info("Check quy???n Admin/SupperAdmin");
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
        } else {
            response.setEcode(EcodeConstant.ERR);
        }


        Ecode ecode = codeService.getEcode(response.getEcode(), user.getLang());
        response.setMessage(ecode.getMessage());
        response.setP_message(ecode.getP_message());
        response.setP_ecode(ecode.getP_ecode());
        return response;
    }

    @Override
    public CommonResponse<Object> assign_admin(AssignAdminRequest request, User user, String requestId, Document action) {

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
        if (userCheck == null || userCheck.getStatus() < 3) {
            response.setEcode(EcodeConstant.INVALID_USER);
            return response;
        }


        log.info("assign admin service start .....");

        // kiem tra role id cua user duoc bo nhiem  trong bang user_group neu co
        // user nay khong phai la superadmin
        UserGroup userDestGroup = userGroupRepository.findByUserId(request.getUserIdDest(),"1");
        if(userDestGroup != null){
            if(userDestGroup.getRoleId() == Constant.IS_SUPER_ADMIN){
                // user laf super admin
                response.setEcode(EcodeConstant.ERROR_IS_ADMIN);
                return  response;
            }
        }

        UserGroup userGroup = userGroupRepository.findByUserId(Integer.toString(user.getId()), "1");
        log.info("Check quy???n SupperAdmin");
        if (userGroup != null) {
            if (userGroup.getRoleId() != Constant.IS_SUPER_ADMIN) { //neu user khong phai la super admin
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

        long count = adminRepositoryNative.assignAdmin(request,user,action);

        if (count > 0) {
            // bo nhiem hoac mien nhiem  thanh cong
            response.setEcode(EcodeConstant.SUCCESS);
        } else {
            // that bai
            response.setEcode(EcodeConstant.ERR);
        }

        return response;
    }

    @Override
    public CommonResponse<Object> assign_superadmin(AssignAdminRequest request, User user, String requestId, Document action) {

        CommonResponse<Object> response = new CommonResponse<>();

        UserGroup userGroup = userGroupRepository.findByUserId(Integer.toString(user.getId()), "1");
        log.info("Check quyen superadmin");
        if (userGroup != null) {
            if (userGroup.getRoleId() != Constant.IS_SUPER_ADMIN) { //neu user khong phai la super admin
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
        UserGroup checkadmin = userGroupRepository.findByUserId(request.getUserIdDest(), "1");
        if (checkadmin == null || checkadmin.getRoleId() != Constant.IS_ADMIN) {
            // neu la super admin hoac khang phai la admin , thong bao loi
            response.setEcode(EcodeConstant.INVALID_USER);
            return response;
        }


        boolean check = adminRepositoryNative.assignSuperAdmin(request, userGroup,action);


        if (check ) {
        // update thanh cong
            response.setEcode(EcodeConstant.SUCCESS);
        } else {
            // update that bai
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

        if(!Arrays.asList("USER","ALL").contains(request.getScope())){
            response.setEcode(EcodeConstant.FLAG_NULL_EMPTY);
            return response;
        }


        //kiem tra quyen admin neu scope = ALL
        if (request.getScope().equalsIgnoreCase("ALL")) {
            //check user co phai la admin
            UserGroup userGroup = userGroupRepository.findByUserId(Integer.toString(user.getId()), "1");
            log.info("Check quy???n admin: " + user.getId());

            if (userGroup != null) {
                if (userGroup.getRoleId() < Constant.IS_ADMIN) { //neu user khong phai la  admin
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

            // neu flag = PENDING, scope = ALL,
            // check admin do co thuoc group co en_admin_approve_post = ON (trong bang group_setting) hay khong
            if (request.getFlag().equalsIgnoreCase("PENDING")) {

                //check user do thuoc group nao
                UserGroup userGroup1 = userGroupRepository.findByUserId(Integer.toString(user.getId()), "1");
                if (userGroup1 == null) {
                    // tra ve loi user khong phai admin
                    response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
                    return response;
                } else {
                    //check en_admin_approve_post = ON trong bang group_setting
                    Groupsetting groupsetting = groupsettingRepository.findBygroupIdAndReferenceId(userGroup1.getGroupId());
                    if (groupsetting == null) {
                        // tra ve loi approve post is not enable
                        response.setEcode(EcodeConstant.APPROVE_POST_NOT_ENABLE);
                        return response;
                    } else {
                        if (groupsetting.getEn_admin_approve_post().equalsIgnoreCase("OFF")) {
                            // tra ve loi approve post is not enable
                            response.setEcode(EcodeConstant.APPROVE_POST_NOT_ENABLE);
                            return response;
                        }
                    }
                }
            }
        }

        // fetch data
        List<Posts> listPosts = adminRepositoryNative.getListPostFilter(request, user);

        if (listPosts.size() == 0) {
            response.setEcode(EcodeConstant.ERR);
            response.setData(null);
            return response;
        } else {
            response.setEcode(EcodeConstant.SUCCESS);
        }

        //lay danh sach userid tuong ung voi  moi post
        List<String> userIds = listPosts.stream().map(p -> p.getUserId()).collect(Collectors.toList());

        // fetch user, hashtag, tag va image de them vao post

        //image
        List<Document> listMedia = adminRepositoryNative.getListMediaByListPosts(listPosts);
        //map postId voi danh sach sanh tuong ung, group by referenceId
        Map<String, List<String>> postId_media = listMedia.stream().collect(Collectors.groupingBy(p -> p.get("referenceId", String.class),
                Collectors.mapping(p -> p.get("mediaUrl", String.class), Collectors.toList())));

        //user
        List<User> listUser = userService.getAllUserByPosts(listPosts.stream().map(p -> p.getUserId()).collect(Collectors.toList()));
        Map<String, UserDTO> userId_UserDTO = listUser.stream().collect(Collectors.toMap(u -> Integer.toString(u.getId()), u -> new UserDTO(u)));

        //hashtag
        List<HashTags> listHashTag = adminRepositoryNative.getlistHashTagByListPosts(listPosts);
        Map<String, List<HashTags>> postId_HashTags = new HashMap<>();
        if (listHashTag != null)
            postId_HashTags = listHashTag.stream().collect(Collectors.groupingBy(h -> h.getPostId(), Collectors.toList()));

        //tag
        List<Tags> listTag = adminRepositoryNative.getListTagByListPosts(listPosts);
        Map<String, List<Tags>> postId_Tags = new HashMap<>();
        if (listTag != null)
            postId_Tags = listTag.stream().collect(Collectors.groupingBy(h -> h.getPostId(), Collectors.toList()));

        List<PostDTO> postDTOList  = new ArrayList<>();
        postDTOList = listPosts.stream().map(p->new PostDTO(p)).collect(Collectors.toList());

        // set image, user, tags
        for (PostDTO p : postDTOList) {
            if (postId_media.get(p.getPostId()) != null) {
                p.setImages(postId_media.get(p.getPostId()));
            }

            p.setUser(userId_UserDTO.get(p.getUserId()));
            p.setHashTags(postId_HashTags.get(p.getPostId()));
            p.setTagList(postId_Tags.get(p.getPostId()));
        }

        GetListPostFilterDTO data = new GetListPostFilterDTO(postDTOList);

        // neu flag = REPORTED_REMOVED, fet du lieu tu bang comment
        List<Comment> listComment = adminRepositoryNative.getListCommentFilter(request,user);
        data.setListComment(listComment);
        // set data
        response.setData(data);
        return response;
    }

    @Override
    public CommonResponse<Object> get_list_reports(GetListReportsRequest request, User user, String requestId) {

        CommonResponse<Object> response = new CommonResponse<>();

        ReportDTO reports = adminRepositoryNative.getListReport(request,user);

        response.setData(reports);
        response.setEcode(EcodeConstant.SUCCESS);
        return response;
    }

    @Override
    public CommonResponse<Object> approve_reject_post(ApproveRejectPostRequest request, User user, String requestId, Document action) {

        CommonResponse<Object> response = new CommonResponse<>();


        // kiem tra flag
        List<String> arr = new ArrayList<>();
        arr.add("APPROVE");
        arr.add("REJECT");
        if (!arr.contains(request.getFlag())) {
            response.setEcode(EcodeConstant.FLAG_NULL_EMPTY);
            return response;
        } else {
            if (request.getFlag().equalsIgnoreCase("REJECT")) {
                boolean flag = Arrays.asList("BREAK_COM_RULE", "FALSENEW", "MEM_CONFLICT", "SPAM", "OTHER").contains(request.getReportType());
                if (!flag) {
                    response.setEcode(EcodeConstant.FLAG_NULL_EMPTY);
                    return response;
                }

            }
        }

        //check quyen approve, reject
        //check user do thuoc group nao
        UserGroup userGroup1 = userGroupRepository.findByUserId(Integer.toString(user.getId()), "1");
        if (userGroup1 == null) {
            // tra ve loi approve post is not enable
            response.setEcode(EcodeConstant.APPROVE_POST_NOT_ENABLE);
            return response;
        } else {
            //check en_admin_approve_post = ON trong bang group_setting
            Groupsetting groupsetting = groupsettingRepository.findBygroupIdAndReferenceId(userGroup1.getGroupId());
            if (groupsetting == null) {
                // tra ve loi approve post is not enable
                response.setEcode(EcodeConstant.APPROVE_POST_NOT_ENABLE);
                return response;
            } else {
                if (groupsetting.getEn_admin_approve_post().equalsIgnoreCase("OFF")) {
                    // tra ve loi approve post is not enable
                    response.setEcode(EcodeConstant.APPROVE_POST_NOT_ENABLE);
                    return response;
                }
            }
        }


        long count = adminRepositoryNative.approve_reject_post(request, user,action);

        if (count == 1) {
            response.setEcode(EcodeConstant.SUCCESS);
        } else {
            response.setEcode(EcodeConstant.ERR);
        }
        response.setData(request.getPostId());


        return response;
    }

    @Override
    public CommonResponse<Object> remove_reported_obj(ReportObjectRequest request, User user, String requestId, Document action) {

        CommonResponse<Object> response = new CommonResponse<>();

        //validate
        if (!Arrays.asList("REMOVE", "REJECT").contains(request.getFlag())) {
            response.setEcode(EcodeConstant.FLAG_NULL_EMPTY);
            return response;
        }

        if (request.getFlag().equalsIgnoreCase("REMOVE"))
            if (!Arrays.asList("SPAM", "BREAK_COM_RULE", "FALSENEW", "MEM_CONFLICT", "OTHER").contains(request.getReportType())) {
                response.setEcode(EcodeConstant.FLAG_NULL_EMPTY);
                return response;
            }


        //kiem tra quyen admin
        UserGroup userGroup = userGroupRepository.findByUserId(Integer.toString(user.getId()), "1");
        log.info("Check quy???n admin");
        if (userGroup != null) {
            if (userGroup.getRoleId() < 2) { //neu user khong phai la  admin
                response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
                return response;
            }
        } else { //neu user khong phai la  admin
            response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
            return response;
        }

        long count = adminRepositoryNative.remove_reported_obj(request, user,action);

        if (count >= 2) {
            response.setEcode(EcodeConstant.SUCCESS);
            response.setData(request.getReferenceId());
            return response;
        } else {
            response.setEcode(EcodeConstant.ERR);
            response.setData(request.getReferenceId());
            return response;
        }

    }

    @Override
    public CommonResponse<Object> appeal_post(AppealPostRequest request, User user, String requestId, Document action) {


        CommonResponse<Object> response = new CommonResponse<>();
        long count = adminRepositoryNative.appeal_content(request, user,action);

        if (count == 1) {
            response.setEcode(EcodeConstant.SUCCESS);
        } else {
            response.setEcode(EcodeConstant.ERR);
        }
        response.setData(request.getPostId());
        return response;
    }

    @Override
    public CommonResponse<Object> report_obj(ReportObjectRequest request, User user, String requestId, Document action) {

        CommonResponse<Object> response = new CommonResponse<>();
        //check user role (la admin thi bao loi)
        UserGroup userGroup = userGroupRepository.findByUserId(Integer.toString(user.getId()), "1");
        if (userGroup != null) {
            if (userGroup.getRoleId() >= 2) {
                response.setEcode(EcodeConstant.ERROR_IS_ADMIN);
                return response;
            }
        }

        //check user co phai la chu bai viet/comment (neu la chu bai viet thi bao loi)
        if (request.getType().equalsIgnoreCase("POST")) {
            Optional<Posts> posts = postsRepository.findById(request.getReferenceId());
            if (posts.isPresent()) {
                if (posts.get().getUserId().equalsIgnoreCase(Integer.toString(user.getId()))) {
                    //thong bao user la chu bai viet
                    response.setEcode(EcodeConstant.ERROR_POST_OWNER);
                    return response;
                }
            }
        } else if (request.getType().equalsIgnoreCase("COMMENT")) {
            Optional<Comment> comments = commentRepository.findById(request.getReferenceId());
            if (comments.isPresent()) {
                if (comments.get().getUserId().equalsIgnoreCase(Integer.toString(user.getId()))) {
                    //thong bao user la chu bai viet
                    response.setEcode(EcodeConstant.ERROR_POST_OWNER);
                    return response;
                }
            }
        } else {
            response.setEcode(EcodeConstant.ERR);
            return response;
        }


        boolean flag = adminRepositoryNative.report_obj(request, user,action);
        if (flag) {
            response.setEcode(EcodeConstant.SUCCESS);
            response.setData(request.getReferenceId());
            return response;
        } else {
            response.setEcode(EcodeConstant.ERR);
            response.setData(request.getReferenceId());
            return response;
        }

    }


    @Override
    public CommonResponse<Object> approve_appeal(ApproveRejectPostRequest request, User user, String requestId, Document action) {

        CommonResponse<Object> response = new CommonResponse<>();

        // kiem tra flag
        if (Arrays.asList("APPROVE","REJECT").contains(request.getFlag())){
            if(request.getFlag().equalsIgnoreCase("REJECT") &&
                    !Arrays.asList("SPAM","BREAK_COM_RULE","FALSENEW","MEM_CONFLICT","OTHER").contains(request.getReportType())){
                response.setEcode(EcodeConstant.FLAG_NULL_EMPTY);
                return response;
            }
        }else {
            response.setEcode(EcodeConstant.FLAG_NULL_EMPTY);
            return response;
        }


        //kiem tra quyen admin
        UserGroup userGroup = userGroupRepository.findByUserId(Integer.toString(user.getId()), "1");
        log.info("Check quy???n admin");
        if (userGroup != null) {
            if (userGroup.getRoleId() < 2) { //neu user khong phai la  admin
                response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
                return response;
            }
        } else { //neu user khong phai la  admin
            response.setEcode(EcodeConstant.ERROR_NOT_ADMIN);
            return response;
        }


        boolean check = adminRepositoryNative.approve_appeal(request,user,action);

        if(check){
            response.setEcode(EcodeConstant.SUCCESS);
            response.setData(request.getPostId());
            return response;
        }
        else {
            response.setEcode(EcodeConstant.ERR);
            response.setData(request.getPostId());
            return response;
        }

    }

    @Override
    public CommonResponse<Object> listAdmin(ApprovePostRequest request, User user, String requestId) {
        CommonResponse<Object> response = new CommonResponse<>();

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
        log.info("Check quy???n Admin/SupperAdmin");
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


        ArrayList<HashMap<String, String>> arrLAdmin = adminRepositoryNative.getListAdmin(request.getGroupId());

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
