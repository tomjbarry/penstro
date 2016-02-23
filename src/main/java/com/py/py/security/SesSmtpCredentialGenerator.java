package com.py.py.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.ServiceException;

public class SesSmtpCredentialGenerator {
	private static final String MESSAGE = "SendRawEmail";
	private static final byte VERSION = 0x02;
	private static final String ALGORITHM = "HmacSHA256";
	
	public static String generateCredentials(String accessKey) throws ServiceException {
		if(accessKey == null) {
			throw new BadParameterException();
		}
		
		try {
			// Get an HMAC-SHA256 Mac instance and initialize it with the AWS secret access key
			SecretKeySpec secretKey = new SecretKeySpec(accessKey.getBytes(), ALGORITHM);
			Mac mac = Mac.getInstance(ALGORITHM);
			mac.init(secretKey);
			
			// Compute the HMAC signature on the input data bytes
			byte[] rawSignature = mac.doFinal(MESSAGE.getBytes());
			
			// Prepend the version number to the signature
			byte[] rawSignatureWithVersion = new byte[rawSignature.length + 1];
			byte[] versionArray = {VERSION};
			System.arraycopy(versionArray, 0, rawSignatureWithVersion, 0, 1);
			System.arraycopy(rawSignature, 0, rawSignatureWithVersion, 1, rawSignature.length);
			
			return DatatypeConverter.printBase64Binary(rawSignatureWithVersion);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
}
