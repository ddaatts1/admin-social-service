package com.epay.ewallet.service.admin.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;

@Service
public class Redis {
	private static final Logger log = LogManager.getLogger(Redis.class);

	@Value("${REDIS_HOST}")
	private String REDIS_HOST;

	@Value("${REDIS_PORT}")
	private int REDIS_PORT;

	@Value("${REDIS_AUTH}")
	private int REDIS_AUTH;

	@Value("${REDIS_PASSWORD}")
	private String REDIS_PASSWORD;

	@Value("${REDIS_DB}")
	private int REDIS_DB;

	@Value("${REDIS_TIMEOUT}")
	private int REDIS_TIMEOUT;

	@Value("${REDIS_KEY_PREFIX}")
	public String REDIS_KEY_PREFIX;

	public Jedis getConnection() {
		log.info("Start connect redis....");
		Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT, REDIS_TIMEOUT * 1000); // ms.
		jedis.connect();
		if (REDIS_AUTH == 1)
			jedis.auth(REDIS_PASSWORD);
		jedis.select(REDIS_DB);
		log.info("Connect redis done!");
		return jedis;
	}

	public boolean setKeyExchange(String deviceId, String secret) {
		log.info("SetKeyExchange | deviceId={}", deviceId);
		boolean result = false;
		try {
			System.out.println("REDIS_HOST");
			Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT, REDIS_TIMEOUT * 1000); // ms.
			jedis.connect();
			if (REDIS_AUTH == 1)
				jedis.auth(REDIS_PASSWORD);
			jedis.select(REDIS_DB);
			// deletet key= deviceID
			jedis.del("SETKEY" + deviceId);
			String flag = jedis.set("SETKEY" + deviceId, secret);
			if (flag.equalsIgnoreCase("OK"))
				result = true;
			jedis.disconnect();
		} catch (Exception e) {
			log.fatal("SetKeyExchange | Exception | deviceId={} | error={} ", deviceId, e);
			e.printStackTrace();
		}
		return result;
	}

	public String getKeyEncrypt(String deviceId) {
		log.info("GetKeyEncrypt | deviceId={} ", deviceId);
		String key = "";
		try {
			Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT, REDIS_TIMEOUT * 1000); // ms.
			jedis.connect();
			if (REDIS_AUTH == 1)
				jedis.auth(REDIS_PASSWORD);
			jedis.select(REDIS_DB);
			//
			key = jedis.get("SETKEY" + deviceId);

			jedis.disconnect();
		} catch (Exception e) {
			log.fatal("GetKeyEncrypt| Exception | deviceId={} | error={} ", deviceId, e);
		}
		return key;
	}

}
