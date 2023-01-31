package com.epay.ewallet.service.admin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.epay.ewallet.service.admin.model.Posts;
import com.epay.ewallet.service.admin.payloads.request.AssignAdminRequest;
import com.epay.ewallet.service.admin.payloads.request.GetListPostFilterRequest;
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
import com.epay.ewallet.service.admin.payloads.request.ApprovePostRequest;
import com.epay.ewallet.service.admin.payloads.request.OnoffAutoRemoveRequest;
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
    public List<Posts> index(){
        MongoDatabase database = mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection("posts");

        MongoIterable<Document> listposts =         collection.find(Filters.and(Filters.eq("userId","1467"),Filters.or(
                Filters.and(Filters.gt("countReport", 0), Filters.eq("status", "REMOVED")))));

        ObjectMapper objectMapper = new ObjectMapper();

        List<Posts>  listPosts = listposts.map(p->objectMapper.convertValue(p,Posts.class)).into(new ArrayList<>());

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

    @RequestMapping(value = "/admin/ASSIGN_ADMIN", method = RequestMethod.POST)
    public CommonResponse<Object> assignAdmin(@RequestBody JsonNode requestRaw,
                                              @RequestHeader Map<String, String> header,
                                              @RequestParam(required = false, defaultValue = "true") boolean encrypted) {

        String logCategory = "ASSIGN_ADMIN";
        log.info("-=====================> ASSIGN_ADMIN" + requestRaw.toString());

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

            response = adminService.assign_admin(assignAdminRequest, user, requestId);

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

    @RequestMapping(value = "/admin/ASSIGN_SUPERADMIN",method = RequestMethod.POST)
    public CommonResponse<Object> assignSuperAdmin(@RequestBody JsonNode requestRaw,
                                                   @RequestHeader Map<String, String> header,
                                                   @RequestParam(required = false, defaultValue = "true") boolean encrypted){

        String logCategory = "ASSIGN_SUPERADMIN";
        log.info("-=====================> ASSIGN_SUPERADMIN" + requestRaw.toString());

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

            response = adminService.assign_superadmin(assignAdminRequest, user, requestId);

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

    //
    @RequestMapping(value = "/admin/GET_LIST_POST_FILTER",method = RequestMethod.GET)
    public CommonResponse<Object> getListPostFilter(@RequestBody JsonNode requestRaw,
                                                    @RequestHeader Map<String, String> header,
                                                    @RequestParam(required = false, defaultValue = "true") boolean encrypted){




        String logCategory = "GET_LIST_POST_FILTER";
        log.info("-=====================> GET_LIST_POST_FILTER" + requestRaw.toString());

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
}
