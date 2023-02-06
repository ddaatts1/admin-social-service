package com.epay.ewallet.service.admin.repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.epay.ewallet.service.admin.model.*;
import com.epay.ewallet.service.admin.payloads.request.*;
import com.epay.ewallet.service.admin.payloads.response.CommonResponse;
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
                    //lay at ca cac post bi report hoac remove cua tat ca user
                    reportedRemovedPosts = postsCollection.find(Filters.or(
                            Filters.gt("countReport", 0), Filters.eq("status", "REMOVED")
                    ));
                } else {
                    //lay at ca cac post bi report hoac remove cua user dang dang nhap
                    reportedRemovedPosts = postsCollection.find(Filters.and(Filters.eq("userId", Integer.toString(user.getId())), Filters.or(
                            Filters.gt("countReport", 0), Filters.eq("status", "REMOVED"))));
                }

                ObjectMapper objectMapper = new ObjectMapper();
                listPosts = reportedRemovedPosts.map(p -> objectMapper.convertValue(p, Posts.class)).into(new ArrayList<>());

            } else {
                // neu flag la HIDDEN , PENDING
                MongoCollection<Document> postsCollection = database.getCollection("posts");
                MongoIterable<Document> reportedRemovedPosts = null;
                if (request.getScope().equalsIgnoreCase("ALL")) {
                    reportedRemovedPosts = postsCollection.find(Filters.eq("status", request.getFlag()));
                } else {
                    reportedRemovedPosts = postsCollection.find(Filters.and(Filters.eq("userId", Integer.toString(user.getId())), Filters.eq("status", request.getFlag())));
                }
                ObjectMapper objectMapper = new ObjectMapper();
                listPosts = reportedRemovedPosts.map(p -> objectMapper.convertValue(p, Posts.class)).into(new ArrayList<>());
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
                Bson status = Updates.set("status", "ACTIVE");
                Bson byAdmin = Updates.set("byadmin", user.getName());
                arrL.add(status);
                arrL.add(byAdmin);

                UpdateResult updateResult = collection.updateOne(Filters.eq("_id", request.getPostId()), arrL);
                log.info(" => getMatchedCount: " + updateResult.getMatchedCount() + ",getModifiedCount: " + updateResult.getModifiedCount());
                return updateResult.getMatchedCount();
            } else {
                //reject post
                ArrayList<Bson> arrL = new ArrayList<>();
                Bson status = Updates.set("status", "REJECT");
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

            if (request.getFlag().equalsIgnoreCase("REMOVE")) {
                //flag = REMOVE
                updateResult = collection.updateOne(Filters.eq("_id", request.getPostId()), Updates.set("status", "REMOVED"));
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

        try{
            MongoDatabase database = mongoClient.getDatabase(db);
            MongoCollection<Document> collection = database.getCollection("appeal_content");

            Document document  = new Document();
            document.append("postId",request.getPostId());
            document.append("content",request.getContent());
            document.append("createDate",new Date());
            document.append("isRead","0");
            document.append("reason","");

            InsertOneResult insertOneResult = collection.insertOne(document);
        return insertOneResult.getInsertedId() != null? 1:0;

        }catch (Exception e){
            log.info(e.getMessage());
        }



        return 0;
    }



    public boolean report_obj(ApproveRejectPostRequest request, User user) {

        try {
            MongoDatabase database = mongoClient.getDatabase(db);
            MongoCollection<Document> collection = database.getCollection("reports");
            String type = request.getPostId().trim().split("_")[0];

            Document document = new Document("content", request.getReason());
            document.append("report_temp", request.getReportType());
            document.append("userId", Integer.toString(user.getId()));
            document.append("referenceId", request.getPostId());
            if (type.equalsIgnoreCase("comment")) {
                document.append("type", "COMMENT");
            } else {
                document.append("type", "POST");
            }
            document.append("status", "1");
            document.append("isRead", "0");
            document.append("createDate", new Date());

            InsertOneResult insertOneResult = collection.insertOne(document);
            if (insertOneResult.getInsertedId() != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return false;
    }



}