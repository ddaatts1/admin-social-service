package com.epay.ewallet.service.admin.securities.impl;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.epay.ewallet.service.admin.securities.CryptService;

@Component
@Qualifier("CryptAesService")
public class CryptAesServiceImpl extends CryptService {
	private static final Logger log = LogManager.getLogger(CryptAesServiceImpl.class);

	public final String encrypt(String input, String key) throws Exception {
		String iv = "vnptepayewalllet";
		byte[] ivbytes = iv.substring(0, 16).getBytes();
		IvParameterSpec ips = new IvParameterSpec(ivbytes);
		// byte[] keybytes = hexToByteArray(key);
		byte[] keybytes = key.substring(0, 16).getBytes();
		byte[] crypted = null;

		Key skey = new SecretKeySpec(keybytes, "AES");

		Cipher cipher;

		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skey, ips);
			byte[] ptext = input.getBytes("UTF-8");
			crypted = cipher.doFinal(ptext);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		return byteArrayToHex(crypted);
	}

	public final String decrypt(String input, String key) throws Exception {
		String iv = "vnptepayewalllet";
		IvParameterSpec ips = new IvParameterSpec(iv.substring(0, 16).getBytes());
//		byte[] keybytes = hexToByteArray(key);
		byte[] keybytes = key.substring(0, 16).getBytes();

		byte[] output = null;
		try {
			Key skey = new SecretKeySpec(keybytes, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skey, ips);
			output = cipher.doFinal(hexToByteArray(input));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return new String(output, "UTF-8");
	}

	public static SecretKey getAESKey() {

		KeyGenerator keyGen;
		try {
			keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(128, SecureRandom.getInstanceStrong());
			// keyGen.init(128, SecureRandom.getInstance("SHA1PRNG"));
			return keyGen.generateKey();

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			log.fatal("Error during generate AES key", e);
			return null;
		}

	}

	public static SecretKey getAESKeyFromPassword() {

		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			// iterationCount = 65536
			// keyLength = 256

			char[] password = new char[1];
			password[0] = 'x';
			String uuid = UUID.randomUUID().toString();

			byte[] salt = uuid.getBytes();

			KeySpec spec = new PBEKeySpec(password, salt, 65536, 128);
			SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
			return secret;
		} catch (Exception e) {
			log.fatal("Error during generate AES key", e);
			return null;
		}
	}

	public static void main(String[] args) throws Exception {

		String obj = "{\"phoneNumber\": \"0374313908\"}";
		String data2 = "PAGL4DvpIVdYDzPGTDLDmA==";
		String data = "KIENPT AAA";
		long startTime = System.currentTimeMillis();
		String x = CryptAesServiceImpl.byteArrayToHex(CryptAesServiceImpl.getAESKey().getEncoded());
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("SecretKey:" + x);
		System.out.println("time:" + elapsedTime);

//		CryptAesServiceImpl aes = new CryptAesServiceImpl();
//		String test = "6300FC342FBDD9C3126EE39892D4CA2086482EAA4FB9AE999741FB9A66B93882B0DF4403ED3C44A95FE4A12ECA83AF73CEB73CA7A6144D8E374BDB34D39E0BFFE892500AEF12F793810F5067CBA9A4734D90EA4947FC91FD36CFD68806B6D6DD330F612DEFB2644893D3C35415368127772793A47A6F299BDF8FC570B0497F0835937954C104DC462EE9C0B403FCC005";
//		String test2 = aes.decrypt(test, "972939fb4d2f9a38e4881b43bd4e10ae");
//
//		System.out.println(test2);
//		System.out.println(test);

		char[] password = new char[1];
		password[0] = 'y';
		byte[] myvar = "fdfd".getBytes();

		String xy = CryptAesServiceImpl.byteArrayToHex(getAESKeyFromPassword().getEncoded());
		System.out.println("key moi: " + xy);

		// String keymoi = aes.encrypt(obj, xy);
		// System.out.println("ma hoa bang key moi: "+ keymoi);

	}

}
