package com.epay.ewallet.service.admin.repository;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.epay.ewallet.service.admin.constant.StatusConstant;
import com.epay.ewallet.service.admin.model.*;
import com.epay.ewallet.service.admin.payloads.request.*;
import com.epay.ewallet.service.admin.payloads.response.CommonResponse;
import com.epay.ewallet.service.admin.payloads.response.ReportDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.*;
import com.mongodb.client.result.InsertOneResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.hibernate.sql.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.epay.ewallet.service.admin.service.impl.AdminServiceImpl;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

import javax.print.Doc;


@Repository
public class AdminRepositoryNative {

    private static final Logger log = LogManager.getLogger(AdminServiceImpl.class);


    @Autowired
    MongoClient mongoClient;

    @Value("${spring.data.mongodb.database_}")
    private String db;

    @Autowired
    PostsRepository postsRepository;


    public ArrayList<HashMap<String, String>> getListAdmin(String groupId) {
        ArrayList<HashMap<String, String>> arrL = new ArrayList<HashMap<String, String>>();
        try {
            MongoDatabase database = mongoClient.getDatabase(db);
            MongoCollection<Document> collection = database.getCollection("user_group");

//		   FindIterable<Document> findIterable = collection.find(); //full records
            FindIterable<Document> findIterable = collection.find(
                    Filters.and(Filters.eq("type", "IN")
                            , Filters.eq("status", "1")
                            , Filters.eq("groupId", groupId))
            );
            MongoCursor<Document> mongoCursor = findIterable.iterator();
//		   Iterator<Document> it = findIterable.iterator();
            while (mongoCursor.hasNext()) {
                Document a_document = mongoCursor.next();
                //			String string = a_document.toString();
                //			System.out.println("string: " + string); // Document{{_id=6356468c98fb58a2ecc86d88, userId=2491, groupId=3, roleId=2, type=IN, status=1, createDate=Tue Oct 18 16:13:38 ICT 2022}}


                String json = a_document.toJson();
                System.out.println("json: " + json); //{"_id": {"$oid": "34800390400f389453fdf000"}, "userId": "2291", "groupId": "3", "roleId": 2, "type": "IN", "status": "1", "createDate": {"$date": "2022-12-20T11:15:35.314Z"}}

//				HashMap<String, Object> hsh = new Gson().fromJson(json, HashMap.class);
//				Object id = hsh.get("_id");
//				double roleId = (double) hsh.get("roleId");
//				String groupIdXXX = (String) (hsh.get("groupId"));
//				String createDate = hsh.get("createDate").toString();
//				System.out.println("id: " + id + ", roleId: " + roleId + ", groupIdXXX: " + groupIdXXX + ", createDate: " + createDate); 

//				Binary xx = (a_document.get("DATA_OUT_BIN",Binary.class));
//				System.out.println("DATA_OUT_BIN: " + Arrays.toString(xx.getData()));

                Integer roleId = a_document.get("roleId", Integer.class);
                String userId = a_document.get("userId", String.class);
                Date createDate = a_document.get("createDate", Date.class);

                HashMap<String, String> hsh = new HashMap<String, String>();
                hsh.put("roleId", roleId.toString());
                hsh.put("groupId", groupId);
                hsh.put("userId", userId);
                if (createDate != null) {
                    hsh.put("createDate", new SimpleDateFormat("yyyyMMddHHmmss").format(createDate));
                }

                arrL.add(hsh);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("getListAdmin!", e);
        } finally {

            //dungtv: can bo doan nay di neu dang lay conn tu pool
//			if (mongoClient != null) {
//				mongoClient.close();
//			}

        }
        return arrL;
    }


    public long update_on_off_auto_remove(OnoffAutoRemoveRequest request) {
        try {
            MongoDatabase database = mongoClient.getDatabase(db);
            MongoCollection<Document> collection = database.getCollection("groupsetting");

            ArrayList<Bson> arrL = new ArrayList<Bson>();
            Bson set1 = Updates.set("on_off_auto_remove", request.getFlag());
            Bson set2 = Updates.set("limitReportPost", request.getLimitReportPost());
            Bson set3 = Updates.set("limitReportComment", request.getLimitReportComment());
            arrL.add(set1);
            arrL.add(set2);
            arrL.add(set3);
            UpdateResult updateResult = collection.updateOne(Filters.eq("groupId", request.getGroupId()), arrL);
            log.info("groupId: " + request.getGroupId() + " => getMatchedCount: " + updateResult.getMatchedCount() + ",getModifiedCount: " + updateResult.getModifiedCount());
            return updateResult.getMatchedCount();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("update_on_off_auto_remove!", e);
        } finally {

            //dungtv: can bo doan nay di neu dang lay conn tu pool
//			if (mongoClient != null) {
//				mongoClient.close();
//			}

        }
        return -1;
    }


    public long assignAdmin(AssignAdminRequest request) {

        try {
            MongoDatabase database = mongoClient.getDatabase(db);
            MongoCollection<Document> collection = database.getCollection("user_group");

            Document filter = new Document("userId", request.getUserIdDest());
            long count = collection.countDocuments(filter);


            if (request.getFlag().equalsIgnoreCase("ON")) {
                //Bo nhiem admin
                if (count < 1) {
                    // neu khong tim thay user nao trong collection "user_group"

                    Document document = new Document("userId", request.getUserIdDest())
                            .append("groupId", 3)
                            .append("roleId", 2)
                            .append("type", "IN")
                            .append("data", new Date());
                    collection.insertOne(document);
                }
                // bo nhiem thanh cong
                return 1L;


            } else if (request.getFlag().equalsIgnoreCase("OFF")) {
                //mien nhiem admin
                if (count > 0) {
                    // xoa document khoi collection
                    collection.deleteOne(Filters.eq("userId", request.getUserIdDest()));
                    // mien nhiem thanh cong
                    return 2L;
                } else {
                    // error
                    return 0;
                }

            }


            log.info("useridDest: " + request.getUserIdDest());
            return count;


        } catch (Exception e) {
            e.printStackTrace();
            log.error("assign_admin!", e);
        }

        return -1;


    }


    public long assignSuperAdmin(AssignAdminRequest request, UserGroup userGroup) {

        try {
            MongoDatabase database = mongoClient.getDatabase(db);
            MongoCollection<Document> collection = database.getCollection("user_group");

            //update roleid cua admin va superadmin
            Bson filterSuperAdmin = Filters.eq("userId", userGroup.getUserId());
            Bson filterAdmin = Filters.eq("userId", request.getUserIdDest());
            Bson updateAdmin = Updates.set("roleId", 3);
            Bson updateSuperAdmin = Updates.set("roleId", 2);

            UpdateResult updateResult1 = collection.updateOne(filterAdmin, updateAdmin);
            UpdateResult updateResult2 = collection.updateOne(filterSuperAdmin, updateSuperAdmin);

            return updateResult1.getModifiedCount() + updateResult1.getModifiedCount();


        } catch (Exception e) {
            e.printStackTrace();
            log.error("assign_superadmin!", e);
        }
        return -1;
    }

    public List<Posts> getListPostFilter(GetListPostFilterRequest request, User user) {

        log.info("GET LIST POST FILTER ");
        try {
            List<Posts> listPosts = new ArrayList<>();
            MongoDatabase database = mongoClient.getDatabase(db);
            //neu flag = saved
            if (request.getFlag().equalsIgnoreCase("SAVED")) {
                MongoCollection<Document> savedPostsCollection = database.getCollection("saved_posts");
                FindIterable<Document> savedPosts;
                List<String> postIds;
                if (request.getScope().equalsIgnoreCase("ALL")) {
                    //lay  cac post duoc luu boi tat ca user trong bang saved_posts
                    savedPosts = savedPostsCollection.find();
                } else {
                    //lay cac post  duoc luu boi chinh user dang dang nhap trong bang saved_posts
                    savedPosts = savedPostsCollection.find(Filters.eq("userId", Integer.toString(user.getId())));
                }
                //lay cac post trong bang posts
                postIds = savedPosts.map(doc -> doc.get("postId").toString()).into(new ArrayList<>());
                MongoCollection<Document> postsCollection = database.getCollection("posts");
                FindIterable<Document> posts = postsCollection.find(Filters.in("_id", postIds));
                ObjectMapper objectMapper = new ObjectMapper();
                listPosts = posts.map(p -> objectMapper.convertValue(p, Posts.class)).into(new ArrayList<>());

            } else if (request.getFlag().equalsIgnoreCase("REPORTED_REMOVED")) {
                //neu flag = REPORTED_REMOVED
                MongoCollection<Document> postsCollection = database.getCollection("posts");
                MongoIterable<Document> reportedRemovedPosts = null;
                if (request.getScope().equalsIgnoreCase("ALL")) {
                    //lay at ca cac post bi report  cua tat ca user
                    reportedRemovedPosts = postsCollection.find(Filters.and(
                            Filters.gt("countReport", 0), Filters.eq("status", StatusConstant.STT_ACTIVE)));
                } else {
                    //lay at ca cac post bi report hoac remove cua user dang dang nhap
                    reportedRemovedPosts = postsCollection.find(Filters.and(Filters.eq("userId", Integer.toString(user.getId())), Filters.or(
                            Filters.gt("countReport", 0), Filters.eq("status", StatusConstant.STT_REMOVED), Filters.eq("status", StatusConstant.STT_REVIEWING), Filters.eq("status", StatusConstant.STT_APPEAL_REJECT))));
                }

                ObjectMapper objectMapper = new ObjectMapper();
                listPosts = reportedRemovedPosts.map(p -> objectMapper.convertValue(p, Posts.class)).into(new ArrayList<>());

            } else if (request.getFlag().equalsIgnoreCase("HIDDEN")) {
                // neu flag la HIDDEN
                MongoCollection<Document> postsCollection = database.getCollection("posts");
                MongoIterable<Document> posts = null;

                if (request.getScope().equalsIgnoreCase("ALL")) {
                    //scope = ALL
                    posts = postsCollection.find(Filters.eq("status", request.getFlag()));
                } else {
                    //scope = USER
                    posts = postsCollection.find(Filters.and(Filters.eq("userId", Integer.toString(user.getId())), Filters.eq("status", request.getFlag())));
                }

                ObjectMapper objectMapper = new ObjectMapper();
                listPosts = posts.map(p -> objectMapper.convertValue(p, Posts.class)).into(new ArrayList<>());
            } else {
                // neu flag la PENDING
                MongoCollection<Document> postsCollection = database.getCollection("posts");
                MongoIterable<Document> posts = null;

                if (request.getScope().equalsIgnoreCase("ALL")) {
                    //scope = ALL
                    posts = postsCollection.find(Filters.eq("status", request.getFlag()));
                } else {
                    //scope = USER
                    posts = postsCollection.find(Filters.and(Filters.eq("userId", Integer.toString(user.getId())), Filters.or(Filters.eq("status", request.getFlag()), Filters.eq("status", StatusConstant.STT_REJECT))));
                }

                ObjectMapper objectMapper = new ObjectMapper();
                listPosts = posts.map(p -> objectMapper.convertValue(p, Posts.class)).into(new ArrayList<>());

            }


            return listPosts;

        } catch (Exception e) {

            e.printStackTrace();
            log.error("get_list_post_filter!", e);

        }

        return null;
    }

    public List<Document> getListMediaByListPosts(List<Posts> list) {

        List<String> listPostsId = list.stream().map(p -> p.get_id()).collect(Collectors.toList());
        List<Document> listmedia = null;
        try {
            MongoDatabase database = mongoClient.getDatabase(db);
            MongoCollection<Document> collection = database.getCollection("media");
            listmedia = collection.find(Filters.in("referenceId", listPostsId)).into(new ArrayList<>());

        } catch (Exception e) {
            log.info(e.getMessage());

        }
        return listmedia;

    }


    public List<HashTags> getlistHashTagByListPosts(List<Posts> list) {
        List<String> listPostsId = list.stream().map(p -> p.get_id()).collect(Collectors.toList());
        List<HashTags> listHashTag = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            MongoDatabase database = mongoClient.getDatabase(db);
            MongoCollection<Document> collection = database.getCollection("hashtags");
            listHashTag = collection.find(Filters.in("postId", listPostsId)).into(new ArrayList<>()).
                    stream().map(h -> mapper.convertValue(h, HashTags.class)).collect(Collectors.toList());

        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return listHashTag;
    }

    public List<Tags> getListTagByListPosts(List<Posts> list) {
        List<String> listPostsId = list.stream().map(p -> p.get_id()).collect(Collectors.toList());
        List<Tags> tags = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            MongoDatabase database = mongoClient.getDatabase(db);
            MongoCollection<Document> collection = database.getCollection("tags");

            tags = collection.find(Filters.in("postId", listPostsId)).into(new ArrayList<>()).
                    stream().map(h -> mapper.convertValue(h, Tags.class)).collect(Collectors.toList());

        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return tags;

    }

    public long approve_reject_post(ApproveRejectPostRequest request, User user) {

        try {

            MongoDatabase database = mongoClient.getDatabase(db);
            MongoCollection<Document> collection = database.getCollection("posts");

            if (request.getFlag().equalsIgnoreCase("APPROVE")) {
                //approve post

                ArrayList<Bson> arrL = new ArrayList<>();
                Bson status = Updates.set("status", StatusConstant.STT_ACTIVE);
                Bson byAdmin = Updates.set("byadmin", user.getName());
                arrL.add(status);
                arrL.add(byAdmin);

                UpdateResult updateResult = collection.updateOne(Filters.eq("_id", request.getPostId()), arrL);
                log.info(" => getMatchedCount: " + updateResult.getMatchedCount() + ",getModifiedCount: " + updateResult.getModifiedCount());
                return updateResult.getMatchedCount();
            } else {
                //reject post
                ArrayList<Bson> arrL = new ArrayList<>();
                Bson status = Updates.set("status", StatusConstant.STT_REJECT);
                Bson byAdmin = Updates.set("byadmin", user.getName());
                Bson reason = Updates.set("reason", request.getReason());
                arrL.add(status);
                arrL.add(byAdmin);
                if (request.getReportType().equalsIgnoreCase("OTHER"))
                    arrL.add(reason);

                UpdateResult updateResult = collection.updateOne(Filters.eq("_id", request.getPostId()), arrL);
                log.info(" => getMatchedCount: " + updateResult.getMatchedCount() + ",getModifiedCount: " + updateResult.getModifiedCount());
                return updateResult.getMatchedCount();

            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return 0;
    }


    public long remove_reported_obj(ApproveRejectPostRequest request, User user) {

        try {
            MongoDatabase database = mongoClient.getDatabase(db);
            MongoCollection<Document> collection = database.getCollection("reports");
            String collectionName = "";
            int count = 0;
            UpdateResult updateResult = null;

            //kiem tra object la comment hay post
            Document report = collection.find(Filters.eq("referenceId", request.getPostId())).first();
            if (report.get("type", String.class).equalsIgnoreCase("COMMENT")) {
                collectionName = "comments";
            } else {
                collectionName = "posts";
            }

            collection = database.getCollection(collectionName);

            if (request.getFlag().equalsIgnoreCase(StatusConstant.STT_REMOVED)) {
                //flag = REMOVE
                updateResult = collection.updateOne(Filters.eq("_id", request.getPostId()), Updates.set("status", StatusConstant.STT_REMOVED));
            } else {
                //flag = REJECT
                updateResult = collection.updateOne(Filters.eq("_id", request.getPostId()), Updates.set("countReport", 0));
            }
            count += updateResult.getMatchedCount();

            //set isRead = 1 tai bang reports
            collection = database.getCollection("reports");
            updateResult = collection.updateMany(Filters.eq("referenceId", request.getPostId()), Updates.set("isRead", "1"));
            count += updateResult.getMatchedCount();

            return count;
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return 0;
    }

    public long appeal_content(AppealPostRequest request, User user) {

        try {
            MongoDatabase database = mongoClient.getDatabase(db);
            MongoCollection<Document> collection = database.getCollection("appeal_content");
            // tao ban ghi moi tai bang appeal_content
            SecureRandom random = new SecureRandom();
            String id = "appeal_" + new BigInteger(130, random).toString(16);

            Document document = new Document();
            document.append("_id", id);
            document.append("postId", request.getPostId());
            document.append("content", request.getContent());
            document.append("createDate", new Date());
            document.append("isRead", "0");
            document.append("reason", "");

            InsertOneResult insertOneResult = collection.insertOne(document);

            // update status trong bang posts la REVIEWING
            collection = database.getCollection("posts");
            UpdateResult updateResult = collection.updateOne(Filters.eq("_id", request.getPostId()), Updates.set("status", StatusConstant.STT_REVIEWING));


            return insertOneResult.getInsertedId() != null && updateResult.getMatchedCount() == 1 ? 1 : 0;

        } catch (Exception e) {
            log.info(e.getMessage());
        }


        return 0;
    }


    public boolean report_obj(ApproveRejectPostRequest request, User user) {

        try {
            MongoDatabase database = mongoClient.getDatabase(db);
            MongoCollection<Document> collection = database.getCollection("reports");
            String type = request.getPostId().trim().split("_")[0];
            String collectionName =null;

            SecureRandom random = new SecureRandom();
            String id = "report_" + new BigInteger(130, random).toString(16);

            Document document = new Document("content", request.getReason());
            document.append("_id",id);
            document.append("report_temp", request.getReportType());
            document.append("userId", Integer.toString(user.getId()));
            document.append("referenceId", request.getPostId());
            if (type.equalsIgnoreCase("comment")) {
                document.append("type", "COMMENT");
                collectionName = "comments";
            } else {
                document.append("type", "POST");
                collectionName = "posts";
            }
            document.append("status", "1");
            document.append("isRead", "0");
            document.append("createDate", new Date());

            // insert one vao bang "reports"
            InsertOneResult insertOneResult = collection.insertOne(document);
            if (insertOneResult.getInsertedId() == null) {
                return false;
            }

            UpdateResult updateResult = null;
            // update countreport trong bang "comments" hoac "posts"
            if(collectionName.equalsIgnoreCase("comments")){
                collection = database.getCollection("comments");
               updateResult=  collection.updateOne(Filters.eq("_id",request.getPostId()),Updates.inc("countReport",1));
            }
            else {
                collection = database.getCollection("posts");
                updateResult = collection.updateOne(Filters.eq("_id",request.getPostId()),Updates.inc("countReport",1));
            }

            if(updateResult.getMatchedCount() == 1){
                return true;
            }
            return false;
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return false;
    }


    public boolean approve_appeal(ApproveRejectPostRequest request, User user, Document action) {

        try {
            MongoDatabase database = mongoClient.getDatabase(db);
            MongoCollection<Document> collection = database.getCollection("appeal_content");
            //request.getPostId() = appealId (khong phai id cua post), xem trong  ApproveRejectPostRequest
            Document document = collection.find(Filters.eq("_id", request.getPostId())).first();
            String postId = document.get("postId", String.class);
            UpdateResult updateResult = null;
            int count = 0;

            collection = database.getCollection("posts");
            if (request.getFlag().equalsIgnoreCase("APPROVE")) {
                // flag = APPROVE
                Bson updateStatus = Updates.set("status", StatusConstant.STT_ACTIVE);
                Bson updateCountReport = Updates.set("countReport", "0");

                updateResult = collection.updateOne(Filters.eq("_id", postId), Arrays.asList(updateCountReport, updateStatus));
                action.append("action1", new Document().
                        append("type", "update").
                        append("update_content", new Document().append("status", StatusConstant.STT_ACTIVE).
                                append("countReport", "0")));

                if (updateResult.getMatchedCount() == 1) {
                    count++;
                }
            } else {
                //flag = REJECT
                Bson updateStatus = Updates.set("status", StatusConstant.STT_APPEAL_REJECT);
                //request.getPostId() = appealId, xem trong  ApproveRejectPostRequest
                updateResult = collection.updateOne(Filters.eq("_id", postId), updateStatus);
                action.append("action1", new Document().append("postId", postId).append("collection", "posts").append("type", "update").append("update_content",new Document().append("status", StatusConstant.STT_APPEAL_REJECT)));
                if (updateResult.getMatchedCount() == 1) {
                    count++;
                }
            }

            // update isRead = 1
            collection = database.getCollection("appeal_content");
            Bson updateReason = null;
            List<Bson> update = new ArrayList<>();
            Bson updateIsRead = Updates.set("isRead", "1");
            update.add(updateIsRead);

            if (request.getFlag().equalsIgnoreCase(StatusConstant.STT_REJECT)) {
                if (request.getReportType().equalsIgnoreCase("OTHER")) {
                    updateReason = Updates.set("reason", request.getReason());
                    action.append("action2", new Document().
                            append("appealId", request.getPostId()).
                            append("collection", "appeal_content").
                            append("type", "update").append("isRead", "1").
                            append("reason", request.getReason()));
                } else {
                    updateReason = Updates.set("reason", request.getReportType());
                    action.append("action2", new Document().
                            append("appealId", request.getPostId()).
                            append("collection", "appeal_content").
                            append("type", "update").append("isRead", "1").
                            append("reason", request.getReportType()));
                }
                update.add(updateReason);
            }

            updateResult = collection.updateOne(Filters.eq("_id", request.getPostId()), update);
            if (updateResult.getMatchedCount() == 1) {
                count++;
            }

            if (count == 2) {
                return true;
            }

        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return false;
    }

    public ReportDTO getListReport(GetListReportsRequest request, User user) {

        List<ReportDTO> reports = new ArrayList<>();

        MongoDatabase database = mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection("reports");

        FindIterable<Document> iterable = collection.find(Filters.eq("referenceId", request.getObjectId()));
        MongoCursor<Document> cursor = iterable.cursor();

        int spam = 0;
        int falseNew = 0;
        int memberConflict = 0;
        int breakCompanyRule = 0;
        List<String> reasons = new ArrayList<>();
        String type = null;
        Document document = null;
        boolean flag = true;

        while (cursor.hasNext()) {
            document = cursor.next();

            if (flag) {
                type = document.get("type", String.class);
                flag = false;
            }
            switch (document.get("report_temp", String.class)) {
                case "SPAM":
                    spam++;
                    break;
                case "FALSENEW":
                    falseNew++;
                    break;
                case "MEM_CONFLICT":
                    memberConflict++;
                    break;
                case "BREAK_COM_RULE":
                    breakCompanyRule++;
                    break;
                case "OTHER":
                    reasons.add(document.get("content", String.class));
                    break;
            }
        }
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setReportReason(reasons);
        reportDTO.setBreaksCompanyRule(breakCompanyRule);
        reportDTO.setFalseNew(falseNew);
        reportDTO.setSpam(spam);
        reportDTO.setMemberConflict(memberConflict);

        //neu la post
        if (type.equalsIgnoreCase("POST")) {
            Optional<Posts> optional = postsRepository.findById(request.getObjectId());
            Posts posts = optional.isPresent() ? optional.get() : null;
            if (posts != null) {
                reportDTO.setPostStatus(posts.getStatus());
                if (reportDTO.getPostStatus().equalsIgnoreCase(StatusConstant.STT_REVIEWING)) {
                    //neu status la REVIEWING, lay noi dung khang cao tu bang appeal_content
                    collection = database.getCollection("appeal_content");
                    Document appeal = collection.find(Filters.eq("postId", request.getObjectId())).first();
                    reportDTO.setAppealReason(appeal.get("content", String.class));

                } else if (reportDTO.getPostStatus().equalsIgnoreCase(StatusConstant.STT_APPEAL_REJECT)) {
                    // neu status la APPEAL_REJECT, lay noi dung reject tu bang posts
                    collection = database.getCollection("posts");
                    Document document1 = collection.find(Filters.eq("_id", request.getObjectId())).first();
                    reportDTO.setRejectReason(document1.get("reason", String.class));
                }
            }

        }

        return reportDTO;
    }
}