package com.epay.ewallet.service.admin.securities.impl;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.epay.ewallet.service.admin.securities.CryptService;

@Component
@Qualifier("CryptRsaService")
public class CryptRsaServiceImpl extends CryptService {

	private static Key publicKey = null;
	private static Key privateKey = null;

	public CryptRsaServiceImpl() throws Exception {
		if (this.publicKey == null) {
			Security.addProvider(new BouncyCastleProvider());
			createKey();
		}
	}

	public String encrypt(String input, String key) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/None/pkcs1Padding", "BC");

		// public key
		byte[] bytePubKey = Base64Utils.base64Decode(key);
		// byte[] bytePubKey = Base64.getDecoder().decode(key.getBytes());
		X509EncodedKeySpec spec = new X509EncodedKeySpec(bytePubKey);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		Key pubKey = kf.generatePublic(spec);

		// 공개키를 전달하여 암호화
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] cipherText = cipher.doFinal(input.getBytes());

		// return new String(Base64.getEncoder().encode(cipherText));
		return Base64Utils.base64Encode(cipherText);
	}

	public String decrypt(String input, String key) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/None/pkcs1Padding", "BC");
		Key newPriKey = null;

		PKCS8EncodedKeySpec spec2 = new PKCS8EncodedKeySpec(Base64Utils.base64Decode(key));
		KeyFactory kf = KeyFactory.getInstance("RSA");
		newPriKey = kf.generatePrivate(spec2);

		// 개인키를 가지고있는쪽에서 복호화
		cipher.init(Cipher.DECRYPT_MODE, newPriKey);
		byte[] plainText = cipher.doFinal(Base64Utils.base64Decode(input));

		return new String(plainText);
	}

	public void createKey() throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/None/NoPadding", "BC");
		SecureRandom random = new SecureRandom();
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");

		generator.initialize(256, random); // 여기에서는 128 bit 키를 생성하였음
		KeyPair pair = generator.generateKeyPair();
		CryptRsaServiceImpl.publicKey = pair.getPublic(); // Kb(pub) 공개키
		CryptRsaServiceImpl.privateKey = pair.getPrivate();// Kb(pri) 개인키
	}

	public static PublicKey get(byte[] keys) throws Exception {

		X509EncodedKeySpec spec = new X509EncodedKeySpec(keys);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(spec);
	}

	public String getPubKey() {
		byte[] beforePubKey = publicKey.getEncoded();
		String strPubKey = byteArrayToHex(beforePubKey);

		return strPubKey;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

		String input = "12345678901234567890123456789012";

		CryptRsaServiceImpl rsaService = new CryptRsaServiceImpl();

		byte[] beforePubKey = publicKey.getEncoded();
		String strPubKey = byteArrayToHex(beforePubKey);

		String encText = rsaService.encrypt(input, strPubKey);

		System.out.println(encText);

		byte[] beforePriKey = privateKey.getEncoded();
		String strPriKey = byteArrayToHex(beforePriKey);

		String decText = rsaService.decrypt(encText, strPriKey);

		System.out.println(decText);

		// Cipher cipher = Cipher.getInstance("RSA/None/NoPadding", "BC");
		//
		// CryptRsaServiceImpl rsaService = new CryptRsaServiceImpl();
		//
		// // public key
		// byte[] beforePubKey = publicKey.getEncoded();
		// String strPubKey = byteArrayToHex(beforePubKey);
		// byte[] bytePubKey = hexToByteArray(strPubKey);
		//
		// X509EncodedKeySpec spec = new X509EncodedKeySpec(bytePubKey);
		// KeyFactory kf = KeyFactory.getInstance("RSA");
		// Key newPubKey = kf.generatePublic(spec);
		//
		// // private key
		// byte[] beforePriKey = privateKey.getEncoded();
		// String strPriKey = byteArrayToHex(beforePriKey);
		// byte[] bytePriKey = hexToByteArray(strPriKey);
		//
		// PKCS8EncodedKeySpec spec2 = new PKCS8EncodedKeySpec(bytePriKey);
		// Key newPriKey = kf.generatePrivate(spec2);
		//
		// // 공개키를 전달하여 암호화
		// cipher.init(Cipher.ENCRYPT_MODE, newPubKey);
		// byte[] cipherText = cipher.doFinal(input);
		// System.out.println("cipher: (" + cipherText.length + ")" + new
		// String(cipherText));
		//
		// // 개인키를 가지고있는쪽에서 복호화
		// cipher.init(Cipher.DECRYPT_MODE, newPriKey);
		// byte[] plainText = cipher.doFinal(cipherText);
		// System.out.println("plain : " + new String(plainText));
	}
}
