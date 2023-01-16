package com.epay.ewallet.service.admin.authen;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.epay.ewallet.service.admin.constant.Constant;
import com.epay.ewallet.service.admin.payloads.request.DeviceRequest;
import com.epay.ewallet.service.admin.redis.Redis;
import com.epay.ewallet.service.admin.service.JwtUserDetailsService;
import com.google.gson.Gson;

import io.jsonwebtoken.ExpiredJwtException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
	private static final Logger log = LogManager.getLogger(JwtRequestFilter.class);

	@Autowired
	private JwtUserDetailsService jwtUserDetailsService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private Redis redis;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		request.setAttribute("ecode", "7001");

		String deviceStr = request.getHeader("device");
		String requestId = request.getHeader("requestId");
		String language = request.getHeader("language");

		if (Constant.LANGUAGE_VN.equals(language)) {
			request.setAttribute("message", Constant.LANGUAGE_VN_7001);
		} else if (Constant.LANGUAGE_EN.equals(language)) {
			request.setAttribute("message", Constant.LANGUAGE_EN_7001);
		} else {
			request.setAttribute("message", Constant.LANGUAGE_KR_7001);
		}

		log.info("requestId={} | deviceStr={}", requestId, deviceStr);
		Gson gson = new Gson();
		DeviceRequest deviceRequest = gson.fromJson(deviceStr, DeviceRequest.class);
		String requestTokenHeader = "";
		String uri = request.getRequestURI();
		if (uri.equals("/getVersions") || uri.equals("/ekycAnalytics")) {
			requestTokenHeader = "";
		} else {
			requestTokenHeader = request.getHeader("Authorization");
		}

		// final String requestTokenHeader = request.getHeader("Authorization");
		String username = null;
		String jwtToken = null;
		// JWT Token is in the form "Bearer token". Remove Bearer word and get
		// only the Token
		if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
			jwtToken = requestTokenHeader.substring(7);
			try {
				username = jwtTokenUtil.getUsernameFromToken(jwtToken);
			} catch (IllegalArgumentException e) {
				System.out.println("Unable to get JWT Token");
			} catch (ExpiredJwtException e) {
				System.out.println("JWT Token has expired");
			}
		} else {
			logger.warn("JWT Token does not begin with Bearer String");
		}

		// Once we get the token validate it.
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			// check another Device (Login trên 1 thiết bị)
//			String deviceIdRedis = redis.getDeviceByAccount(username);
			String deviceId = deviceRequest.getDeviceId();

//			if (deviceIdRedis == null || ("").equals(deviceIdRedis)) {
//				redis.setDeviceByAccount(deviceId, username);
//			} else {
//				if (!deviceId.equals(deviceIdRedis)) {
//					request.setAttribute("ecode", "7002");
//					if (Constant.LANGUAGE_VN.equals(language)) {
//						request.setAttribute("message", Constant.LANGUAGE_VN_7002);
//					} else if (Constant.LANGUAGE_EN.equals(language)) {
//						request.setAttribute("message", Constant.LANGUAGE_EN_7002);
//					} else {
//						request.setAttribute("message", Constant.LANGUAGE_KR_7002);
//					}
//					log.info("{} | LOGIN | Login by another device | ecode=7002", requestId);
//
//					username = "";
//				}
//			}
			//

			UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);

			// if token is valid configure Spring Security to manually set
			// authentication
			if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {

				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				usernamePasswordAuthenticationToken
						.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			}
		}
		chain.doFilter(request, response);
	}

}
