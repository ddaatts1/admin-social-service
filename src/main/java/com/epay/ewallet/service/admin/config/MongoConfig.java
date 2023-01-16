package com.epay.ewallet.service.admin.config;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

//https://hevodata.com/learn/spring-boot-mongodb-config/#3.2
//https://websparrow.org/spring/how-to-connect-spring-boot-application-with-mongodb
//https://www.baeldung.com/spring-data-mongodb-tutorial

@Configuration
//@EnableMongoRepositories("com.epay.ewallet.service.admin.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

//    private final List<Converter<?, ?>> converters = new ArrayList<Converter<?, ?>>();

	@Value("${spring.data.mongodb.uri_}")
	private String uri;

	@Value("${spring.data.mongodb.database_}")
	private String db;

	@Value("${spring.data.mongodb.maxWaitTime_}")
	private int maxWaitTime;

	@Value("${spring.data.mongodb.maxSize_}")
	private int maxSize;

	@Value("${spring.data.mongodb.maxConnecting_}")
	private int maxConnecting;

	@Value("${spring.data.mongodb.maxConnectionIdleTime_}")
	private long maxConnectionIdleTime;

	@Value("${spring.data.mongodb.maxConnectionLifeTime_}")
	private long maxConnectionLifeTime;

	@Value("${spring.data.mongodb.minSize_}")
	private int minSize;

	@Override
	protected String getDatabaseName() {
		return db;
	}

//    @Override
//    protected void configureClientSettings(MongoClientSettings.Builder builder) {
//
//        builder.credential(MongoCredential.createCredential(
//                "username", "databasename", "password".toCharArray())
//        )
//                .applyToClusterSettings(settings -> {
//
//                    settings.hosts(Collections.singletonList(new ServerAddress("hostname", 27017)));
//                });
//    }

	@Override
	@Bean
	public MongoClient mongoClient() {
//        final ConnectionString connectionString = new ConnectionString("mongodb://ewallet:khongbiet@172.16.10.74:27017/ewallet?authSource=admin&directConnection=true&ssl=false");
		final ConnectionString connectionString = new ConnectionString(uri);
		final MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.applyToConnectionPoolSettings(builder -> builder.minSize(minSize) // so luong toi thieu connection
																					// trong pool, cac connection se
																					// duoc giu trong pool khi idle,
																					// pool se dam bao rang luon chua it
																					// nhat so luong connection toi
																					// thieu nay, default = 2
						.maxSize(maxSize) // so luong toi da connection duoc phep, cac connection se duoc giu trong pool
											// khi o trang thai idle, khi pool can kiet thi cac thread goi len se bi
											// block de wait cho den khi co connection kha dung, default = 100
						.maxConnecting(maxConnecting) // so luong connection toi da ma 1 pool co the thiet lap dong
														// thoi, default = 2
						.maxConnectionIdleTime(maxConnectionIdleTime, TimeUnit.SECONDS) // Sets the maximum time a
																						// connection can be idle before
																						// it's closed, default = 0
																						// nolimit
						.maxConnectionLifeTime(maxConnectionLifeTime, TimeUnit.SECONDS) // Sets the maximum time a
																						// pooled connection can be
																						// alive before it's closed,
																						// default = 0 nolimit
						.maxWaitTime(maxWaitTime, TimeUnit.SECONDS) // thoi gian toi da de 1 thread co the wait 1
																	// connection kha dung, default = 2 minutea, 0: no
																	// wait, negative: wait infinitive
//            		.maintenanceFrequency(10,  TimeUnit.SECONDS)
//            		.maintenanceInitialDelay(1 , TimeUnit.SECONDS)
				)
//            .applyToSocketSettings( builder -> builder.)
//            .applyToSslSettings( builder -> builder.)
//            .applyToServerSettings( builder -> builder.)
				.build();

		return MongoClients.create(mongoClientSettings);
	}

	@Override
	public Collection<String> getMappingBasePackages() {
		return Collections.singleton("");
	}

}