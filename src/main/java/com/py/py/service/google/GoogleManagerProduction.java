package com.py.py.service.google;

import java.util.List;

import javax.annotation.PostConstruct;

import org.junit.Assert;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.ExternalServiceException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;

public class GoogleManagerProduction implements GoogleManager {

	@JsonIgnoreProperties(ignoreUnknown = true)
	protected static class RecaptchaVerification {
		private boolean success;
		@JsonProperty("error-codes")
		private List<String> errorCodes;
		public boolean isSuccess() {
			return success;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public List<String> getErrorCodes() {
			return errorCodes;
		}
		public void setErrorCodes(List<String> errorCodes) {
			this.errorCodes = errorCodes;
		}
	}
	
	protected RestTemplate restTemplate = new RestTemplate();
	
	protected String verifyRecaptchaUrl;
	protected String recaptchaSecretKey;
	
	protected ObjectMapper mapper = new ObjectMapper();
	
	@PostConstruct
	public void initialize() {
		Assert.assertNotNull(recaptchaSecretKey);
		Assert.assertNotNull(verifyRecaptchaUrl);
	}
	
	@Override
	public boolean verifyRecaptchaResponse(String recaptchaResponse, String ipAddress) throws ServiceException {
		ArgCheck.nullCheck(recaptchaResponse);
		/*
		String body;
		try {
			body = "secret=" + URLEncoder.encode(recaptchaSecretKey, StandardCharsets.UTF_8.name()) + "&response=" + URLEncoder.encode(recaptchaResponse, StandardCharsets.UTF_8.name());
			if(ipAddress != null && !ipAddress.isEmpty()) {
				body = body + "&remoteip=" + URLEncoder.encode(ipAddress, StandardCharsets.UTF_8.name());
			}
		} catch(Exception e) {
			throw new BadParameterException();
		}*/
		MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<String, String>();
		bodyMap.add("secret", recaptchaSecretKey);
		bodyMap.add("response", recaptchaResponse);
		if(ipAddress != null && !ipAddress.isEmpty()) {
			bodyMap.add("remoteip", ipAddress);
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		HttpEntity<MultiValueMap<String, String> > request = new HttpEntity<MultiValueMap<String, String> >(bodyMap, headers);
		
		ResponseEntity<String> response;
		try {
			response = restTemplate.<String>exchange(verifyRecaptchaUrl, HttpMethod.POST, request, String.class);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
		if(response.getStatusCode() == HttpStatus.NOT_FOUND || response.getStatusCode().series() == Series.SERVER_ERROR) {
			throw new ExternalServiceException();
		}
		if(response.getStatusCode().series() != Series.SUCCESSFUL) {
			throw new ServiceException();
		}
		RecaptchaVerification verification;
		try {
			verification = mapper.readValue(response.getBody(), RecaptchaVerification.class);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
		List<String> errorCodes = verification.getErrorCodes();
		if(errorCodes != null && !errorCodes.isEmpty()) {
			if(errorCodes.contains("missing-input-secret")) {
				throw new BadParameterException();
			}
			if(errorCodes.contains("invalid-input-secret")) {
				throw new BadParameterException();
			}
			if(errorCodes.contains("missing-input-secret")) {
				throw new ActionNotAllowedException();
			}
			if(errorCodes.contains("invalid-input-secret")) {
				throw new ActionNotAllowedException();
			}
		}
		return verification.isSuccess();
	}
	
	public void setVerifyRecaptchaUrl(String verifyRecaptchaUrl) {
		this.verifyRecaptchaUrl = verifyRecaptchaUrl;
	}
	
	public void setRecaptchaSecretKey(String recaptchaSecretKey) {
		this.recaptchaSecretKey = recaptchaSecretKey;
	}
	
}
