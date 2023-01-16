package com.epay.ewallet.service.admin.service;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epay.ewallet.service.admin.mapperDataOne.IEcode;
import com.epay.ewallet.service.admin.model.Ecode;

@Service
public class CodeService {
	private static final Logger log = LogManager.getLogger(CodeService.class);

	@Autowired
	private IEcode ecodeDao;

	public Ecode getEcode(String code, String language) {
		HashMap<String, String> map = new HashMap<>();
		if (language == null || language.trim().isEmpty() == true) {
			language = "EN";
		}
		map.put("ERROR_LANG", language);
		map.put("ERROR_CODE", code);

		Ecode ecode = ecodeDao.getEcode(map);

		if (ecode == null) {
			log.error("CodeService | GET_ECODE | Ecode is not exist | ecode={} | language={}", code, language);
			ecode = new Ecode();
			ecode.setEcode(code);
			ecode.setLanguage(language);
			ecode.setMessage("");
			ecode.setP_ecode("");
			ecode.setP_message("");
		}

		if (ecode.getMessage() == null || ecode.getMessage().trim().isEmpty() == true) {
			log.warn("CodeService | GET_ECODE | Ecode message is empty | ecode={} | language={}", code, language);
			ecode.setMessage(getMessagePlaceholder(code, language));
		}

		if (ecode.getP_message() == null || ecode.getP_message().trim().isEmpty() == true) {
			log.warn("CodeService | GET_ECODE | p_ecode is empty | ecode={} | language={}", code, language);
			ecode.setP_ecode("");
		}

		if (ecode.getP_message() == null || ecode.getP_message().trim().isEmpty() == true) {
			log.warn("CodeService | GET_ECODE | p_message is empty | ecode={} | language={}", code, language);
			ecode.setP_message(getMessagePlaceholder(code, language));
		}

		return ecode;
	}

	public String getMessagePlaceholder(String code, String language) {
		return code + "_" + language;
	}

	public String getMappingErrorCode(String providerId, String providerCode) {
		HashMap<String, String> mappingErrorCode = ecodeDao.getMappingErrorCode(providerId, providerCode);

		if (mappingErrorCode == null) {
			log.error("CodeService | GET_MAPPING_ERROR_CODE | No mapping error code | providerId={} | providerCode={}",
					providerId, providerCode);

			return "";
		}

		if (mappingErrorCode.get("EPAY_CODE") == null || mappingErrorCode.get("EPAY_CODE").trim().isEmpty() == true) {
			log.error(
					"CodeService | GET_MAPPING_ERROR_CODE | Mapping error code is not configured | providerId={} | providerCode={}",
					providerId, providerCode);

			return "";
		}

		String ecode = mappingErrorCode.get("EPAY_CODE").trim();

		return ecode;
	}

	public String getMessageByCode(String lang, String ecode) {
		HashMap<String, String> map = new HashMap<>();
		if (lang == null || lang.trim().isEmpty() == true) {
			lang = "EN";
		}
		map.put("language", lang);
		map.put("ERROR_CODE", ecode);

		String message = "";
		try {
			map = ecodeDao.getMessageByCode(map);
			message = (String) map.get("ERROR_MESSAGE");
		} catch (Exception e) {
			log.fatal("getMessageByCode | Could not get error message | code={} | language={}", ecode, lang);
		}

		if (message == null || message.trim().isEmpty() == true) {
			log.warn("getMessageByCode | Error message is empty or does not exist | code={} | language={}", ecode, lang);
			message = getMessagePlaceholder(ecode, lang);
		}

		return message;
	}
}
