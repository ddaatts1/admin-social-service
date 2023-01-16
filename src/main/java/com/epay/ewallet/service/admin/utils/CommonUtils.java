package com.epay.ewallet.service.admin.utils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class CommonUtils {

	@SuppressWarnings("unused")
	public static BigDecimal parseBigDecimal(final String strData) {
		BigDecimal decimalNum = null;
		try {
			final BigDecimal tempNum = decimalNum = new BigDecimal(strData);
		} catch (Exception e) {
			decimalNum = null;
		}
		return decimalNum;
	}

	/**
	 * isNullOrEmpty
	 * 
	 * @param oneList
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isNullOrEmpty(final List oneList) {
		boolean isSuccess = false;
		if (oneList == null) {
			isSuccess = true;
		} else if (oneList.isEmpty()) {
			isSuccess = true;
		}
		return isSuccess;
	}

	public static boolean isNull(final Object obj) {
		boolean isSuccess = false;
		if (obj == null) {
			isSuccess = true;
		}
		return isSuccess;
	}

	public static boolean isNullOrBlank(final String str) {
		boolean isSuccess = false;
		if (str == null || str.equals("") || str.trim().equals("")) {
			isSuccess = true;
		}
		return isSuccess;
	}

	public static Date parse(final String date) {
		return parseDate(date, "dd/MM/yyyy").orElseGet(
				() -> parseDate(date, "dd/MM/yyyy").orElseGet(() -> parseDate(date, "dd/MM/yyyy").orElse(null)));
	}

	public static Optional<Date> parseDate(String date, String pattern) {
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

			return Optional.of(simpleDateFormat.parse(date));
		} catch (ParseException e) {
			return Optional.empty();
		}
	}

	public static boolean isNullorZero(Integer i) {
		return 0 == (i == null ? 0 : i);
	}
}
