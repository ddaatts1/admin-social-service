package com.epay.ewallet.service.admin.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.epay.ewallet.service.admin.model.Posts;
import com.epay.ewallet.service.admin.payloads.request.*;
import com.epay.ewallet.service.admin.repository.AdminRepositoryNative;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import com.epay.ewallet.service.admin.authen.JwtTokenUtil;
import com.epay.ewallet.service.admin.constant.EcodeConstant;
import com.epay.ewallet.service.admin.mapperDataOne.IUser;
import com.epay.ewallet.service.admin.model.Ecode;
import com.epay.ewallet.service.admin.model.User;
import com.epay.ewallet.service.admin.payloads.response.CommonResponse;
import com.epay.ewallet.service.admin.service.AdminService;
import com.epay.ewallet.service.admin.service.CodeService;
import com.epay.ewallet.service.admin.utils.DecodeDataUtil;
import com.epay.ewallet.service.admin.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;

@RestController
@EnableAutoConfiguration
public class AdminController {
    private static final Logger log = LogManager.getLogger(AdminController.class);

    @Autowired
    AdminService adminService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private DecodeDataUtil decodeData;

    @Autowired
    private CodeService codeService;

    @Autowired
    private IUser userDao;
    @Autowired
    AdminRepositoryNative adminRepositoryNative;

    @Autowired
    MongoClient mongoClient;
    @Value("${spring.data.mongodb.database_}")
    private String db;

    @GetMapping("/")
    public List<Posts> index() {
        MongoDatabase database = mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection("posts");

        MongoIterable<Document> listposts = collection.find(Filters.and(Filters.eq("userId", "1467"), Filters.or(
                Filters.and(Filters.gt("countReport", 0), Filters.eq("status", "REMOVED")))));

        ObjectMapper objectMapper = new ObjectMapper();

        List<Posts> listPosts = listposts.map(p -> objectMapper.convertValue(p, Posts.class)).into(new ArrayList<>());

        return listPosts;
    }

    @ResponseBody
    @RequestMapping(value = "/admin/ONOFF_APPROVE_POST", method = RequestMethod.POST)
    public CommonResponse<Object> approve(@RequestBody JsonNode requestRaw,
                                          @RequestHeader Map<String, String> header,
                                          @RequestParam(required = false, defaultValue = "true") boolean encrypted) {

//			String logCategory = FunctionConstant.GET_COMMENTS;
        String logCategory = "ONOFF_APPROVE_POST";
        Gson gson = new Gson();

        String requestId = header.get("requestid");
        String language = header.get("language");
        String deviceId = Utils.getDeviceIdFromHeader(header);

        log.info("===> ONOFF_APPROVE_POST => encrypted: " + encrypted + " => requestId: " + requestId + " => request raw from client: " + requestRaw.toString());


        ApprovePostRequest approvePostRequest = decodeData.getRequest(requestId, logCategory, requestRaw,
                ApprovePostRequest.class, encrypted, deviceId);

        log.info("===========> ONOFF_APPROVE_POST => requestId: " + requestId + " => request clear from client: " + approvePostRequest.toString());

        log.info("{} | {} | Start | header={} | request={} | encrypted={}", requestId, logCategory, gson.toJson(header),
                gson.toJson(approvePostRequest), encrypted);

        CommonResponse<Object> response = new CommonResponse<Object>();
        try {
            String bearerToken = header.get("authorization");
            String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
            String phone = jwtTokenUtil.getUsernameFromToken(token);
            User user = userDao.getUserByPhone(phone);
            log.info("===> APPROVE_POSTS => userDO from DB: " + user);

            response = adminService.approve(approvePostRequest, user, requestId);

            // Jump to finally code block before return
            return response;

        } catch (Exception e) {
            log.fatal("{} | {} | Exception | error={}", requestId, logCategory, e);
            e.printStackTrace();

            response.setEcode(EcodeConstant.EXCEPTION);

            // Jump to finally code block before return
            return response;

        } finally {
            /**
             * Actions before return
             */
            if (response.getMessage() == null || response.getMessage().isEmpty() == true) {
                // Set ecode message, p_ecode, p_message
                Ecode ecode = codeService.getEcode(response.getEcode(), language);
                response.setMessage(ecode.getMessage());
                response.setP_ecode(ecode.getP_ecode());
                response.setP_message(ecode.getP_message());
            }

            log.info("===> ONOFF_APPROVE_POST => response clear: " + new Gson().toJson(response));

            /**
             * Encrypt data
             */
            if (encrypted == true) {
                String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
                response.setData(encryptedData);
                log.info("<===========================  response raw for requestId: " + requestId + " => " + new Gson().toJson(response));
            }

        }
    }


    @ResponseBody
    @RequestMapping(value = "/admin/ON_OFF_AUTO_REMOVE", method = RequestMethod.POST)
    public CommonResponse<Object> on_off_auto_remove(@RequestBody JsonNode requestRaw,
                                                     @RequestHeader Map<String, String> header,
                                                     @RequestParam(required = false, defaultValue = "true") boolean encrypted) {

        String logCategory = "ON_OFF_AUTO_REMOVE";
        Gson gson = new Gson();

        String requestId = header.get("requestid");
        String language = header.get("language");
        String deviceId = Utils.getDeviceIdFromHeader(header);

        log.info("===> ON_OFF_AUTO_REMOVE => encrypted: " + encrypted + " => requestId: " + requestId + " => request raw from client: " + requestRaw.toString());


        OnoffAutoRemoveRequest onoffAutoRemoveRequest = decodeData.getRequest(requestId, logCategory, requestRaw,
                OnoffAutoRemoveRequest.class, encrypted, deviceId);

        log.info("===========> ON_OFF_AUTO_REMOVE => requestId: " + requestId + " => request clear from client: " + onoffAutoRemoveRequest.toString());

        log.info("{} | {} | Start | header={} | request={} | encrypted={}", requestId, logCategory, gson.toJson(header),
                gson.toJson(onoffAutoRemoveRequest), encrypted);

        CommonResponse<Object> response = new CommonResponse<Object>();
        try {
            String bearerToken = header.get("authorization");
            String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
            String phone = jwtTokenUtil.getUsernameFromToken(token);
            User user = userDao.getUserByPhone(phone);
            log.info("===> ON_OFF_AUTO_REMOVE => userDO from DB: " + user);

            response = adminService.on_off_auto_remove(onoffAutoRemoveRequest, user, requestId);

            // Jump to finally code block before return
            return response;

        } catch (Exception e) {
            log.fatal("{} | {} | Exception | error={}", requestId, logCategory, e);
            e.printStackTrace();

            response.setEcode(EcodeConstant.EXCEPTION);

            // Jump to finally code block before return
            return response;

        } finally {
            /**
             * Actions before return
             */
            if (response.getMessage() == null || response.getMessage().isEmpty() == true) {
                // Set ecode message, p_ecode, p_message
                Ecode ecode = codeService.getEcode(response.getEcode(), language);
                response.setMessage(ecode.getMessage());
                response.setP_ecode(ecode.getP_ecode());
                response.setP_message(ecode.getP_message());
            }

            log.info("===> ON_OFF_AUTO_REMOVE => response clear: " + new Gson().toJson(response));

            /**
             * Encrypt data
             */
            if (encrypted == true) {
                String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
                response.setData(encryptedData);
                log.info("<===========================  response raw for requestId: " + requestId + " => " + new Gson().toJson(response));
            }

        }
    }


    @ResponseBody
    @RequestMapping(value = "/admin/LIST_ADMIN", method = RequestMethod.POST)
    public CommonResponse<Object> LIST_ADMIN(@RequestBody JsonNode requestRaw,
                                             @RequestHeader Map<String, String> header,
                                             @RequestParam(required = false, defaultValue = "true") boolean encrypted) {

        String logCategory = "LIST_ADMIN";
        Gson gson = new Gson();

        String requestId = header.get("requestid");
        String language = header.get("language");
        String deviceId = Utils.getDeviceIdFromHeader(header);

        log.info("===> LIST_ADMIN => encrypted: " + encrypted + " => requestId: " + requestId + " => request raw from client: " + requestRaw.toString());


        ApprovePostRequest approvePostRequest = decodeData.getRequest(requestId, logCategory, requestRaw,
                ApprovePostRequest.class, encrypted, deviceId);

        log.info("===========> LIST_ADMIN => requestId: " + requestId + " => request clear from client: " + approvePostRequest.toString());

        log.info("{} | {} | Start | header={} | request={} | encrypted={}", requestId, logCategory, gson.toJson(header),
                gson.toJson(approvePostRequest), encrypted);

        CommonResponse<Object> response = new CommonResponse<Object>();
        try {
            String bearerToken = header.get("authorization");
            String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
            String phone = jwtTokenUtil.getUsernameFromToken(token);
            User user = userDao.getUserByPhone(phone);
            log.info("===> LIST_ADMIN => userDO from DB: " + user);

            response = adminService.listAdmin(approvePostRequest, user, requestId);

            // Jump to finally code block before return
            return response;

        } catch (Exception e) {
            log.fatal("{} | {} | Exception | error={}", requestId, logCategory, e);
            e.printStackTrace();

            response.setEcode(EcodeConstant.EXCEPTION);

            // Jump to finally code block before return
            return response;

        } finally {
            /**
             * Actions before return
             */
            if (response.getMessage() == null || response.getMessage().isEmpty() == true) {
                // Set ecode message, p_ecode, p_message
                Ecode ecode = codeService.getEcode(response.getEcode(), language);
                response.setMessage(ecode.getMessage());
                response.setP_ecode(ecode.getP_ecode());
                response.setP_message(ecode.getP_message());
            }

            log.info("===> LIST_ADMIN => response clear: " + new Gson().toJson(response));

            /**
             * Encrypt data
             */
            if (encrypted == true) {
                String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
                response.setData(encryptedData);
                log.info("<===========================  response raw for requestId: " + requestId + " => " + new Gson().toJson(response));
            }

        }
    }

    // Bổ nhiệm hoặc miễn nhiệm admin
    @RequestMapping(value = "/admin/ASSIGN_ADMIN", method = RequestMethod.POST)
    public CommonResponse<Object> assignAdmin(@RequestBody JsonNode requestRaw,
                                              @RequestHeader Map<String, String> header,
                                              @RequestParam(required = false, defaultValue = "true") boolean encrypted) {

        String logCategory = "ASSIGN_ADMIN";
//        log.info("-=====================> ASSIGN_ADMIN: " + requestRaw.toString());

        Gson gson = new Gson();
        String requestId = header.get("requestid");
        String language = header.get("language");
        String deviceId = Utils.getDeviceIdFromHeader(header);

        log.info("===> ASSIGN_ADMIN => encrypted: " + encrypted + " => requestId: " + requestId + " => request raw from client: " + requestRaw.toString());


        AssignAdminRequest assignAdminRequest = decodeData.getRequest(requestId, logCategory, requestRaw,
                AssignAdminRequest.class, encrypted, deviceId);

        log.info("===========> ASSIGN_ADMIN => requestId: " + requestId + " => request clear from client: " + assignAdminRequest.toString());

        log.info("{} | {} | Start | header={} | request={} | encrypted={}", requestId, logCategory, gson.toJson(header),
                gson.toJson(assignAdminRequest), encrypted);

        CommonResponse<Object> response = new CommonResponse<>();
        try {
            String bearerToken = header.get("authorization");
            String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
            String phone = jwtTokenUtil.getUsernameFromToken(token);
            User user = userDao.getUserByPhone(phone);
            log.info("===> ASSIGN_ADMIN => userDO from DB: " + user);

            // set action_log
            Document action = new Document();
            action.append("userId",user.getId());
            action.append("function_name","ASSIGN_ADMIN");
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(assignAdminRequest);
            action.append("request_data",Document.parse(json));
            action.append("occur_date", new Date());

            response = adminService.assign_admin(assignAdminRequest, user, requestId,action);

            //insert action log
            write_action_log(action);
            // Jump to finally code block before return
            return response;

        } catch (Exception e) {
            log.fatal("{} | {} | Exception | error={}", requestId, logCategory, e);
            e.printStackTrace();

            response.setEcode(EcodeConstant.EXCEPTION);

            // Jump to finally code block before return
            return response;

        } finally {
            /**
             * Actions before return
             */
            if (response.getMessage() == null || response.getMessage().isEmpty() == true) {
                // Set ecode message, p_ecode, p_message
                Ecode ecode = codeService.getEcode(response.getEcode(), language);
                response.setMessage(ecode.getMessage());
                response.setP_ecode(ecode.getP_ecode());
                response.setP_message(ecode.getP_message());
                response.setData(assignAdminRequest.getUserIdDest());
            }

            log.info("===> ASSIGN_ADMIN => response clear: " + new Gson().toJson(response));

            /**
             * Encrypt data
             */
            if (encrypted == true) {
                String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
                response.setData(encryptedData);
                log.info("<===========================  response raw for requestId: " + requestId + " => " + new Gson().toJson(response));
            }

        }


    }
    // Nhượng quyền superadmin
    @RequestMapping(value = "/admin/ASSIGN_SUPERADMIN", method = RequestMethod.POST)
    public CommonResponse<Object> assignSuperAdmin(@RequestBody JsonNode requestRaw,
                                                   @RequestHeader Map<String, String> header,
                                                   @RequestParam(required = false, defaultValue = "true") boolean encrypted) {

        String logCategory = "ASSIGN_SUPERADMIN";
//        log.info("-=====================> ASSIGN_SUPERADMIN: " + requestRaw.toString());

        Gson gson = new Gson();
        String requestId = header.get("requestid");
        String language = header.get("language");
        String deviceId = Utils.getDeviceIdFromHeader(header);

        log.info("===> ASSIGN_SUPERADMIN => encrypted: " + encrypted + " => requestId: " + requestId + " => request raw from client: " + requestRaw.toString());


        AssignAdminRequest assignAdminRequest = decodeData.getRequest(requestId, logCategory, requestRaw,
                AssignAdminRequest.class, encrypted, deviceId);

        log.info("===========> ASSIGN_SUPERADMIN => requestId: " + requestId + " => request clear from client: " + assignAdminRequest.toString());

        log.info("{} | {} | Start | header={} | request={} | encrypted={}", requestId, logCategory, gson.toJson(header),
                gson.toJson(assignAdminRequest), encrypted);

        CommonResponse<Object> response = new CommonResponse<>();
        try {
            String bearerToken = header.get("authorization");
            String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
            String phone = jwtTokenUtil.getUsernameFromToken(token);
            User user = userDao.getUserByPhone(phone);
            log.info("===> ASSIGN_SUPERADMIN => userDO from DB: " + user);

            // set action_log
            Document action = new Document();
            action.append("userId",user.getId());
            action.append("function_name","ASSIGN_SUPERADMIN");
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(assignAdminRequest);
            action.append("request_data",Document.parse(json));
            action.append("occur_date", new Date());

            response = adminService.assign_superadmin(assignAdminRequest, user, requestId,action);

            // insert action log
            write_action_log(action);

            // Jump to finally code block before return
            return response;

        } catch (Exception e) {
            log.fatal("{} | {} | Exception | error={}", requestId, logCategory, e);
            e.printStackTrace();

            response.setEcode(EcodeConstant.EXCEPTION);

            // Jump to finally code block before return
            return response;

        } finally {
            /**
             * Actions before return
             */
            if (response.getMessage() == null || response.getMessage().isEmpty() == true) {
                // Set ecode message, p_ecode, p_message
                Ecode ecode = codeService.getEcode(response.getEcode(), language);
                response.setMessage(ecode.getMessage());
                response.setP_ecode(ecode.getP_ecode());
                response.setP_message(ecode.getP_message());
                response.setData(assignAdminRequest.getUserIdDest());
            }

            log.info("===> ASSIGN_SUPERADMIN => response clear: " + new Gson().toJson(response));

            /**
             * Encrypt data
             */
            if (encrypted == true) {
                String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
                response.setData(encryptedData);
                log.info("<===========================  response raw for requestId: " + requestId + " => " + new Gson().toJson(response));
            }

        }

    }

    //Lấy danh sách các bài viết được save, report, hidden, đợi duyệt bởi admin của
    //user đang đăng nhập, hoặc của tất cả các user
    @RequestMapping(value = "/admin/GET_LIST_POST_FILTER", method = RequestMethod.GET)
    public CommonResponse<Object> getListPostFilter(@RequestBody JsonNode requestRaw,
                                                    @RequestHeader Map<String, String> header,
                                                    @RequestParam(required = false, defaultValue = "true") boolean encrypted) {


        String logCategory = "GET_LIST_POST_FILTER";
//        log.info("-=====================> GET_LIST_POST_FILTER" + requestRaw.toString());

        Gson gson = new Gson();
        String requestId = header.get("requestid");
        String language = header.get("language");
        String deviceId = Utils.getDeviceIdFromHeader(header);

        log.info("===> GET_LIST_POST_FILTER => encrypted: " + encrypted + " => requestId: " + requestId + " => request raw from client: " + requestRaw.toString());


        GetListPostFilterRequest getListPostFilterRequest = decodeData.getRequest(requestId, logCategory, requestRaw,
                GetListPostFilterRequest.class, encrypted, deviceId);

        log.info("===========> GET_LIST_POST_FILTER => requestId: " + requestId + " => request clear from client: " + getListPostFilterRequest.toString());

        log.info("{} | {} | Start | header={} | request={} | encrypted={}", requestId, logCategory, gson.toJson(header),
                gson.toJson(getListPostFilterRequest), encrypted);

        CommonResponse<Object> response = new CommonResponse<>();
        try {
            String bearerToken = header.get("authorization");
            String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
            String phone = jwtTokenUtil.getUsernameFromToken(token);
            User user = userDao.getUserByPhone(phone);
            log.info("===> GET_LIST_POST_FILTER => userDO from DB: " + user);

            response = adminService.get_list_post_filter(getListPostFilterRequest, user, requestId);

            // Jump to finally code block before return
            return response;

        } catch (Exception e) {
            log.fatal("{} | {} | Exception | error={}", requestId, logCategory, e);
            e.printStackTrace();

            response.setEcode(EcodeConstant.EXCEPTION);

            // Jump to finally code block before return
            return response;

        } finally {
            /**
             * Actions before return
             */
            if (response.getMessage() == null || response.getMessage().isEmpty() == true) {
                // Set ecode message, p_ecode, p_message
                Ecode ecode = codeService.getEcode(response.getEcode(), language);
                response.setMessage(ecode.getMessage());
                response.setP_ecode(ecode.getP_ecode());
                response.setP_message(ecode.getP_message());
//                response.setData(assignAdminRequest.getUserIdDest());
            }

            log.info("===> GET_LIST_POST_FILTER => response clear: " + new Gson().toJson(response));

            /**
             * Encrypt data
             */
            if (encrypted == true) {
                String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
                response.setData(encryptedData);
                log.info("<===========================  response raw for requestId: " + requestId + " => " + new Gson().toJson(response));
            }

        }


    }


    @RequestMapping(value = "/admin/GET_LIST_REPORTS", method = RequestMethod.GET)
    public CommonResponse<Object> getListReport(@RequestBody JsonNode requestRaw,
                                                @RequestHeader Map<String, String> header,
                                                @RequestParam(required = false, defaultValue = "true") boolean encrypted) {

        String logCategory = "GET_LIST_REPORTS";
        log.info("-=====================> GET_LIST_REPORTS" + requestRaw.toString());

        Gson gson = new Gson();
        String requestId = header.get("requestid");
        String language = header.get("language");
        String deviceId = Utils.getDeviceIdFromHeader(header);

        log.info("===> GET_LIST_REPORTS => encrypted: " + encrypted + " => requestId: " + requestId + " => request raw from client: " + requestRaw.toString());


        GetListReportsRequest getListReportsRequest = decodeData.getRequest(requestId, logCategory, requestRaw,
                GetListReportsRequest.class, encrypted, deviceId);

        log.info("===========> GET_LIST_REPORTS => requestId: " + requestId + " => request clear from client: " + getListReportsRequest.toString());

        log.info("{} | {} | Start | header={} | request={} | encrypted={}", requestId, logCategory, gson.toJson(header),
                gson.toJson(getListReportsRequest), encrypted);

        CommonResponse<Object> response = new CommonResponse<>();
        try {
            String bearerToken = header.get("authorization");
            String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
            String phone = jwtTokenUtil.getUsernameFromToken(token);
            User user = userDao.getUserByPhone(phone);
            log.info("===> GET_LIST_REPORTS => userDO from DB: " + user);

            response = adminService.get_list_reports(getListReportsRequest, user, requestId);

            // Jump to finally code block before return
            return response;

        } catch (Exception e) {
            log.fatal("{} | {} | Exception | error={}", requestId, logCategory, e);
            e.printStackTrace();

            response.setEcode(EcodeConstant.EXCEPTION);

            // Jump to finally code block before return
            return response;

        } finally {
            /**
             * Actions before return
             */
            if (response.getMessage() == null || response.getMessage().isEmpty() == true) {
                // Set ecode message, p_ecode, p_message
                Ecode ecode = codeService.getEcode(response.getEcode(), language);
                response.setMessage(ecode.getMessage());
                response.setP_ecode(ecode.getP_ecode());
                response.setP_message(ecode.getP_message());
//                response.setData(assignAdminRequest.getUserIdDest());
            }

            log.info("===> GET_LIST_REPORTS => response clear: " + new Gson().toJson(response));

            /**
             * Encrypt data
             */
            if (encrypted == true) {
                String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
                response.setData(encryptedData);
                log.info("<===========================  response raw for requestId: " + requestId + " => " + new Gson().toJson(response));
            }

        }


    }

    @RequestMapping(value = "/admin/APPROVE_REJECT_POST", method = RequestMethod.POST)
    public CommonResponse<Object> apprrove_reject_post(@RequestBody JsonNode requestRaw,
                                                       @RequestHeader Map<String, String> header,
                                                       @RequestParam(required = false, defaultValue = "true") boolean encrypted) {

        String logCategory = "APPROVE_REJECT_POST";
        log.info("-=====================> APPROVE_REJECT_POST" + requestRaw.toString());

        Gson gson = new Gson();
        String requestId = header.get("requestid");
        String language = header.get("language");
        String deviceId = Utils.getDeviceIdFromHeader(header);

        log.info("===> APPROVE_REJECT_POST => encrypted: " + encrypted + " => requestId: " + requestId + " => request raw from client: " + requestRaw.toString());


        ApproveRejectPostRequest approveRejectPostRequest = decodeData.getRequest(requestId, logCategory, requestRaw,
                ApproveRejectPostRequest.class, encrypted, deviceId);

        log.info("===========> APPROVE_REJECT_POST => requestId: " + requestId + " => request clear from client: " + approveRejectPostRequest.toString());

        log.info("{} | {} | Start | header={} | request={} | encrypted={}", requestId, logCategory, gson.toJson(header),
                gson.toJson(approveRejectPostRequest), encrypted);

        CommonResponse<Object> response = new CommonResponse<>();
        try {
            String bearerToken = header.get("authorization");
            String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
            String phone = jwtTokenUtil.getUsernameFromToken(token);
            User user = userDao.getUserByPhone(phone);
            log.info("===> APPROVE_REJECT_POST => userDO from DB: " + user);

            // set action_log
            Document action = new Document();
            action.append("userId",user.getId());
            action.append("function_name","APPROVE_REJECT_POST");
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(approveRejectPostRequest);
            action.append("request_data",Document.parse(json));
            action.append("occur_date", new Date());

            response = adminService.approve_reject_post(approveRejectPostRequest, user, requestId,action);

            // insert action log
            write_action_log(action);
            // Jump to finally code block before return
            return response;

        } catch (Exception e) {
            log.fatal("{} | {} | Exception | error={}", requestId, logCategory, e);
            e.printStackTrace();

            response.setEcode(EcodeConstant.EXCEPTION);

            // Jump to finally code block before return
            return response;

        } finally {
            /**
             * Actions before return
             */
            if (response.getMessage() == null || response.getMessage().isEmpty() == true) {
                // Set ecode message, p_ecode, p_message
                Ecode ecode = codeService.getEcode(response.getEcode(), language);
                response.setMessage(ecode.getMessage());
                response.setP_ecode(ecode.getP_ecode());
                response.setP_message(ecode.getP_message());
//                response.setData(assignAdminRequest.getUserIdDest());
            }

            log.info("===> APPROVE_REJECT_POST => response clear: " + new Gson().toJson(response));

            /**
             * Encrypt data
             */
            if (encrypted == true) {
                String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
                response.setData(encryptedData);
                log.info("<===========================  response raw for requestId: " + requestId + " => " + new Gson().toJson(response));
            }

        }
    }


    @RequestMapping(value = "/admin/REMOVE_REPORTED_OBJ", method = RequestMethod.POST)
    public CommonResponse<Object> remove_reported_obj(@RequestBody JsonNode requestRaw,
                                                      @RequestHeader Map<String, String> header,
                                                      @RequestParam(required = false, defaultValue = "true") boolean encrypted) {

        String logCategory = "REMOVE_REPORTED_OBJ";
        log.info("-=====================> REMOVE_REPORTED_OBJ" + requestRaw.toString());

        Gson gson = new Gson();
        String requestId = header.get("requestid");
        String language = header.get("language");
        String deviceId = Utils.getDeviceIdFromHeader(header);

        log.info("===> REMOVE_REPORTED_OBJ => encrypted: " + encrypted + " => requestId: " + requestId + " => request raw from client: " + requestRaw.toString());


        ApproveRejectPostRequest approveRejectPostRequest = decodeData.getRequest(requestId, logCategory, requestRaw,
                ApproveRejectPostRequest.class, encrypted, deviceId);

        log.info("===========> REMOVE_REPORTED_OBJ => requestId: " + requestId + " => request clear from client: " + approveRejectPostRequest.toString());

        log.info("{} | {} | Start | header={} | request={} | encrypted={}", requestId, logCategory, gson.toJson(header),
                gson.toJson(approveRejectPostRequest), encrypted);

        CommonResponse<Object> response = new CommonResponse<>();
        try {
            String bearerToken = header.get("authorization");
            String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
            String phone = jwtTokenUtil.getUsernameFromToken(token);
            User user = userDao.getUserByPhone(phone);
            log.info("===> REMOVE_REPORTED_OBJ => userDO from DB: " + user);

            // set action_log
            Document action = new Document();
            action.append("userId",user.getId());
            action.append("function_name","REMOVE_REPORTED_OBJ");
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(approveRejectPostRequest);
            action.append("request_data",Document.parse(json));
            action.append("occur_date", new Date());

            response = adminService.remove_reported_obj(approveRejectPostRequest, user, requestId,action);

            // insert action_log
            write_action_log(action);
            // Jump to finally code block before return
            return response;

        } catch (Exception e) {
            log.fatal("{} | {} | Exception | error={}", requestId, logCategory, e);
            e.printStackTrace();

            response.setEcode(EcodeConstant.EXCEPTION);

            // Jump to finally code block before return
            return response;

        } finally {
            /**
             * Actions before return
             */
            if (response.getMessage() == null || response.getMessage().isEmpty() == true) {
                // Set ecode message, p_ecode, p_message
                Ecode ecode = codeService.getEcode(response.getEcode(), language);
                response.setMessage(ecode.getMessage());
                response.setP_ecode(ecode.getP_ecode());
                response.setP_message(ecode.getP_message());
//                response.setData(assignAdminRequest.getUserIdDest());
            }

            log.info("===> REMOVE_REPORTED_OBJ => response clear: " + new Gson().toJson(response));

            /**
             * Encrypt data
             */
            if (encrypted == true) {
                String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
                response.setData(encryptedData);
                log.info("<===========================  response raw for requestId: " + requestId + " => " + new Gson().toJson(response));
            }

        }
    }


    @RequestMapping(value = "/admin/REPORT_OBJ", method = RequestMethod.POST)
    public CommonResponse<Object> report_obj(@RequestBody JsonNode requestRaw,
                                             @RequestHeader Map<String, String> header,
                                             @RequestParam(required = false, defaultValue = "true") boolean encrypted) {

        String logCategory = "REPORT_OBJ";
        log.info("-=====================> REPORT_OBJ" + requestRaw.toString());

        Gson gson = new Gson();
        String requestId = header.get("requestid");
        String language = header.get("language");
        String deviceId = Utils.getDeviceIdFromHeader(header);

        log.info("===> REPORT_OBJ => encrypted: " + encrypted + " => requestId: " + requestId + " => request raw from client: " + requestRaw.toString());

        ApproveRejectPostRequest reportObjRequest = decodeData.getRequest(requestId, logCategory, requestRaw,
                ApproveRejectPostRequest.class, encrypted, deviceId);

        log.info("===========> REPORT_OBJ => requestId: " + requestId + " => request clear from client: " + reportObjRequest.toString());

        log.info("{} | {} | Start | header={} | request={} | encrypted={}", requestId, logCategory, gson.toJson(header),
                gson.toJson(reportObjRequest), encrypted);

        CommonResponse<Object> response = new CommonResponse<>();
        try {
            String bearerToken = header.get("authorization");
            String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
            String phone = jwtTokenUtil.getUsernameFromToken(token);
            User user = userDao.getUserByPhone(phone);
            log.info("===> REPORT_OBJ => userDO from DB: " + user);

            // set action_log
            Document action = new Document();
            action.append("userId",user.getId());
            action.append("function_name","REPORT_OBJ");
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(reportObjRequest);
            action.append("request_data",Document.parse(json));
            action.append("occur_date", new Date());

            response = adminService.report_obj(reportObjRequest, user, requestId,action);

            // insert action log
            write_action_log(action);
            // Jump to finally code block before return
            return response;

        } catch (Exception e) {
            log.fatal("{} | {} | Exception | error={}", requestId, logCategory, e);
            e.printStackTrace();

            response.setEcode(EcodeConstant.EXCEPTION);

            // Jump to finally code block before return
            return response;

        } finally {
            /**
             * Actions before return
             */
            if (response.getMessage() == null || response.getMessage().isEmpty() == true) {
                // Set ecode message, p_ecode, p_message
                Ecode ecode = codeService.getEcode(response.getEcode(), language);
                response.setMessage(ecode.getMessage());
                response.setP_ecode(ecode.getP_ecode());
                response.setP_message(ecode.getP_message());
                response.setData(reportObjRequest.getPostId());
            }

            log.info("===> REPORT_OBJ => response clear: " + new Gson().toJson(response));
            /**
             * Encrypt data
             */
            if (encrypted == true) {
                String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
                response.setData(encryptedData);
                log.info("<===========================  response raw for requestId: " + requestId + " => " + new Gson().toJson(response));
            }

        }
    }

    @RequestMapping(value = "/admin/APPEAL_POST", method = RequestMethod.POST)
    public CommonResponse<Object> appeal_post(@RequestBody JsonNode requestRaw,
                                              @RequestHeader Map<String, String> header,
                                              @RequestParam(required = false, defaultValue = "true") boolean encrypted) {

        String logCategory = "APPEAL_POST";
        log.info("-=====================> APPEAL_POST" + requestRaw.toString());

        Gson gson = new Gson();
        String requestId = header.get("requestid");
        String language = header.get("language");
        String deviceId = Utils.getDeviceIdFromHeader(header);

        log.info("===> APPEAL_POST => encrypted: " + encrypted + " => requestId: " + requestId + " => request raw from client: " + requestRaw.toString());


        AppealPostRequest appealPostRequest = decodeData.getRequest(requestId, logCategory, requestRaw,
                AppealPostRequest.class, encrypted, deviceId);

        log.info("===========> APPEAL_POST => requestId: " + requestId + " => request clear from client: " + appealPostRequest.toString());

        log.info("{} | {} | Start | header={} | request={} | encrypted={}", requestId, logCategory, gson.toJson(header),
                gson.toJson(appealPostRequest), encrypted);

        CommonResponse<Object> response = new CommonResponse<>();
        try {
            String bearerToken = header.get("authorization");
            String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
            String phone = jwtTokenUtil.getUsernameFromToken(token);
            User user = userDao.getUserByPhone(phone);
            log.info("===> APPEAL_POST => userDO from DB: " + user);

            // set action_log
            Document action = new Document();
            action.append("userId",user.getId());
            action.append("function_name","APPEAL_POST");
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(appealPostRequest);
            action.append("request_data",Document.parse(json));
            action.append("occur_date", new Date());

            response = adminService.appeal_post(appealPostRequest, user, requestId,action);

            // insert action log
            write_action_log(action);
            // Jump to finally code block before return
            return response;

        } catch (Exception e) {
            log.fatal("{} | {} | Exception | error={}", requestId, logCategory, e);
            e.printStackTrace();

            response.setEcode(EcodeConstant.EXCEPTION);

            // Jump to finally code block before return
            return response;

        } finally {
            /**
             * Actions before return
             */
            if (response.getMessage() == null || response.getMessage().isEmpty() == true) {
                // Set ecode message, p_ecode, p_message
                Ecode ecode = codeService.getEcode(response.getEcode(), language);
                response.setMessage(ecode.getMessage());
                response.setP_ecode(ecode.getP_ecode());
                response.setP_message(ecode.getP_message());
//                response.setData(assignAdminRequest.getUserIdDest());
            }

            log.info("===> APPEAL_POST => response clear: " + new Gson().toJson(response));

            /**
             * Encrypt data
             */
            if (encrypted == true) {
                String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
                response.setData(encryptedData);
                log.info("<===========================  response raw for requestId: " + requestId + " => " + new Gson().toJson(response));
            }

        }
    }

    @RequestMapping(value = "/admin/APPROVE_APPEAL", method = RequestMethod.POST)
    public CommonResponse<Object> approve_appeal(@RequestBody JsonNode requestRaw,
                                              @RequestHeader Map<String, String> header,
                                              @RequestParam(required = false, defaultValue = "true") boolean encrypted) {

        String logCategory = "APPROVE_APPEAL";
        log.info("-=====================> APPROVE_APPEAL" + requestRaw.toString());

        Gson gson = new Gson();
        String requestId = header.get("requestid");
        String language = header.get("language");
        String deviceId = Utils.getDeviceIdFromHeader(header);

        log.info("===> APPROVE_APPEAL => encrypted: " + encrypted + " => requestId: " + requestId + " => request raw from client: " + requestRaw.toString());


        ApproveRejectPostRequest approve_appeal = decodeData.getRequest(requestId, logCategory, requestRaw,
                ApproveRejectPostRequest.class, encrypted, deviceId);

        log.info("===========> APPEAL_POST => requestId: " + requestId + " => request clear from client: " + approve_appeal.toString());

        log.info("{} | {} | Start | header={} | request={} | encrypted={}", requestId, logCategory, gson.toJson(header),
                gson.toJson(approve_appeal), encrypted);

        CommonResponse<Object> response = new CommonResponse<>();
        try {
            String bearerToken = header.get("authorization");
            String token = jwtTokenUtil.getTokenFromBearerToken(bearerToken);
            String phone = jwtTokenUtil.getUsernameFromToken(token);
            User user = userDao.getUserByPhone(phone);
            log.info("===> APPROVE_APPEAL => userDO from DB: " + user);

            Document action = new Document();
            action.append("userId",user.getId());
            action.append("function_name","APPROVE_APPEAL");
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(approve_appeal);
            action.append("request_data",Document.parse(json));
            action.append("occur_date", new Date());

            response = adminService.approve_appeal(approve_appeal, user, requestId,action);

            // insert action_log
            write_action_log(action);
            // Jump to finally code block before return
            return response;

        } catch (Exception e) {
            log.fatal("{} | {} | Exception | error={}", requestId, logCategory, e);
            e.printStackTrace();

            response.setEcode(EcodeConstant.EXCEPTION);

            // Jump to finally code block before return
            return response;

        } finally {
            /**
             * Actions before return
             */
            if (response.getMessage() == null || response.getMessage().isEmpty() == true) {
                // Set ecode message, p_ecode, p_message
                Ecode ecode = codeService.getEcode(response.getEcode(), language);
                response.setMessage(ecode.getMessage());
                response.setP_ecode(ecode.getP_ecode());
                response.setP_message(ecode.getP_message());
//                response.setData(assignAdminRequest.getUserIdDest());
            }

            log.info("===> APPROVE_APPEAL => response clear: " + new Gson().toJson(response));

            /**
             * Encrypt data
             */
            if (encrypted == true) {
                String encryptedData = decodeData.encrypt(requestId, logCategory, deviceId, response.getData());
                response.setData(encryptedData);
                log.info("<===========================  response raw for requestId: " + requestId + " => " + new Gson().toJson(response));
            }

        }
    }




    private void write_action_log(Document action){
        MongoDatabase database = mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection("action_logs");
        collection.insertOne(action);
    }


}