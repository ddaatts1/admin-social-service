package com.epay.ewallet.service.admin.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epay.ewallet.service.admin.payloads.request.EncryptDataRequest;
import com.epay.ewallet.service.admin.redis.Redis;
import com.epay.ewallet.service.admin.securities.impl.CryptAesServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Service
public class DecodeDataUtil {
	private static final Logger log = LogManager.getLogger(DecodeDataUtil.class);
	@Autowired
	private Redis redis;

	public <T> T decrypt(String requestId, String logCategory, String deviceId, String encryptedData,
			Class<T> className) {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		T data = null;
		try {
			log.info("{} | {} | Decrypt data start | deviceId={} | encryptedData={}", requestId, logCategory, deviceId,
					encryptedData);

			if (deviceId == null || deviceId.trim().isEmpty() == true) {
				log.error("{} | {} | Decrypt failed, deviceId is invalid", requestId, logCategory);
				return null;
			}

			if (encryptedData == null || encryptedData.trim().isEmpty() == true) {
				log.info("{} | {} | Encrypted data is empty, skip decryption", requestId, logCategory);
				return null;
			}

			String key = redis.getKeyEncrypt(deviceId);
			log.info("{} | {} | Get encrypt key done | deviceId={} | key={}", requestId, logCategory, deviceId, key);
			if (key == null || key.trim().isEmpty() == true) {
				log.error("{} | {} | Encrypt key is invalid | deviceId={} | key={}", requestId, logCategory, deviceId,
						key);
				return null;
			}

			CryptAesServiceImpl aes = new CryptAesServiceImpl();
			String decryptedData = aes.decrypt(encryptedData, key);
			log.info("{} | {} | Decrypt data done | encryptedData={} | key={} | decryptedData={}", requestId,
					logCategory, encryptedData, key, decryptedData);

			data = gson.fromJson(decryptedData, className);
			return data;

		} catch (Exception e) {
			log.fatal("{} | {} | Decrypt data fail | exception={}", requestId, logCategory, e);
			return null;

		} finally {
			log.info("{} | {} | Decrypt end | encryptedData={} | data={}", requestId, logCategory, encryptedData,
					gson.toJson(data));
		}
	}

	public <T> String encrypt(String requestId, String logCategory, String deviceId, T data) {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String encryptedData = "";

		try {
			log.info("{} | {} | Encrypt data start | deviceId={} | data={}", requestId, logCategory, deviceId,
					gson.toJson(data));

			if (deviceId == null || deviceId.trim().isEmpty() == true) {
				log.error("{} | {} | Encrypt failed, deviceId is invalid", requestId, logCategory);
				return null;
			}

			String key = redis.getKeyEncrypt(deviceId);
			log.info("{} | {} | Get encrypt key done | deviceId={} | key={}", requestId, logCategory, deviceId, key);
			if (key == null || key.trim().isEmpty() == true) {
				log.error("{} | {} | Encrypt key is invalid | deviceId={} | key={}", requestId, logCategory, deviceId,
						key);
				return "";
			}

			CryptAesServiceImpl aes = new CryptAesServiceImpl();
			encryptedData = aes.encrypt(gson.toJson(data), key);
			log.info("{} | {} | Encrypt data done", requestId, logCategory);

			return encryptedData;

		} catch (Exception e) {
			log.fatal("{} | {} | Encrypt response fail | exception={}", requestId, logCategory, e);
			return "";

		} finally {
			log.info("{} | {} | Encrypt end | data={} | encryptedData={}", requestId, logCategory, gson.toJson(data),
					encryptedData);
		}
	}

	public <T> T getRequest(String requestId, String logCategory, JsonNode requestBody, Class<T> className,
			boolean encrypted, String deviceId) {
		T request = null;

		if (encrypted == true) {
			ObjectMapper objectMapper = new ObjectMapper();
			EncryptDataRequest enRequest = objectMapper.convertValue(requestBody, EncryptDataRequest.class);
			/**
			 * Decrypt request
			 */
			request = decrypt(requestId, logCategory, deviceId, enRequest.getData(), className);
		} else {
			ObjectMapper objectMapper = new ObjectMapper();
			request = objectMapper.convertValue(requestBody, className);
		}

		return request;
	}

	public static void main(String[] args) throws Exception {
		String test = "7e089388e1b3f0a452ad89f0ef80a5787bc5ca5eb9b93308d78b96901250eef6150bc6b0970b350b5aa7beafa8a006dc213996c461555fb6296ceb9ac17d2fb7e8c8260e14de8ad15b75c192b4c1fbc491ec0bc845cb2a9c20cad29dd8d0ded370f173eb0ac7bb67e5d66c19a51504791a00d4409c603b822badd308fa5d9cd6";
		String key = "f27cb140b6b4a7bf4ebae21396bd4b97";

		CryptAesServiceImpl aes = new CryptAesServiceImpl();
		String objDecode = aes.decrypt(test, key);
		System.out.println(objDecode);
	}
}
