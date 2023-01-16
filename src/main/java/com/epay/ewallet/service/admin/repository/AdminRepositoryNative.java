package com.epay.ewallet.service.admin.repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.epay.ewallet.service.admin.payloads.request.AssignAdminRequest;
import com.epay.ewallet.service.admin.payloads.response.CommonResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.epay.ewallet.service.admin.payloads.request.OnoffAutoRemoveRequest;
import com.epay.ewallet.service.admin.service.impl.AdminServiceImpl;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;


@Repository
public class AdminRepositoryNative {

	private static final Logger log = LogManager.getLogger(AdminServiceImpl.class);
	
	
	@Autowired
	 MongoClient mongoClient;
	
	@Value("${spring.data.mongodb.database_}")
	private String db;

	public  ArrayList<HashMap<String, String>> getListAdmin(String groupId) {
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
				Document a_document =  mongoCursor.next();
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
		
				Integer roleId = a_document.get("roleId",Integer.class);
				String userId = a_document.get("userId",String.class);
				Date createDate = a_document.get("createDate",Date.class);
				
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
		}finally {
			
			//dungtv: can bo doan nay di neu dang lay conn tu pool
//			if (mongoClient != null) {
//				mongoClient.close();
//			}
			
		}
		return arrL;
	}

	
	
	
	
	
	public  long update_on_off_auto_remove(OnoffAutoRemoveRequest request) {
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
		}finally {
			
			//dungtv: can bo doan nay di neu dang lay conn tu pool
//			if (mongoClient != null) {
//				mongoClient.close();
//			}
			
		}
		return -1;
	}


	public long  assignAdmin(AssignAdminRequest request){

		try{
			MongoDatabase database =  mongoClient.getDatabase(db);
			MongoCollection<Document> collection = database.getCollection("user_group");

			Document filter = new Document("userId",request.getUserIdDest());
			long count  = collection.countDocuments(filter);


			if (request.getFlag().equalsIgnoreCase("ON")) {
				//Bo nhiem admin
				if(count <1){
					// neu khong tim thay user nao trong collection "user_group"

					Document document = new Document("userId", request.getUserIdDest())
							.append("groupId", 3)
							.append("roleId", 2)
							.append("type", "IN")
							.append("data", new Date());
					collection.insertOne(document);
				}
				// bo nhiem thanh cong
				return  1L;


			} else if (request.getFlag().equalsIgnoreCase("OFF")) {
				//mien nhiem admin
				if(count > 0){
					// xoa document khoi collection
					collection.deleteOne(Filters.eq("userId", request.getUserIdDest()));
					// mien nhiem thanh cong
					return 2L;
				}
				else {
					// error
					return  0;
				}

			}


			log.info("useridDest: " + request.getUserIdDest() );
			return count;


		}
		catch (Exception e){

		}

		return 999;


	}


	
	
	
	
	
	
}