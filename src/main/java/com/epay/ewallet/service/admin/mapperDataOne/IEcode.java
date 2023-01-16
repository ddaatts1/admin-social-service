package com.epay.ewallet.service.admin.mapperDataOne;

import java.util.HashMap;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.epay.ewallet.service.admin.model.Ecode;

@Mapper
public interface IEcode {
	Ecode getEcode(HashMap<String, String> map);

	HashMap<String, String> getMappingErrorCode(@Param("PROVIDER_ID") String providerId,
			@Param("PROVIDER_CODE") String providerCode);

	HashMap<String, String> getMessageByCode(HashMap<String, String> map);

}
