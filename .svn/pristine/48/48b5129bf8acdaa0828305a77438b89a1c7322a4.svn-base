package com.xzy.base;

import java.net.*;
import java.util.*;
import java.security.*;

public class GUIDCreator extends Object{
	private MessageDigest md5 = null;
	private String seedingString = "";
	private String rawGUID = "";
	
	private static Random myRand;
	private static SecureRandom mySecureRand;
	private static String s_id;
	
	public static final int BEFORE_MD5 = 1;
	public static final int AFTER_MD5 = 2;
	public static final int FORMAT_STRING = 3;
	
	private static GUIDCreator singleInstance = new GUIDCreator();
	public static GUIDCreator getSingleInstance(){
		return singleInstance;
	}
	
	private GUIDCreator(){
		mySecureRand = new SecureRandom();
		long secureInitializer = mySecureRand.nextLong();
		myRand = new Random(secureInitializer);
		try{
			s_id = InetAddress.getLocalHost().toString();
			this.md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}

	private void getRandomGUID(boolean secure){
		StringBuffer sbValueBeforeMD5 = new StringBuffer();
		try{
			long time = System.currentTimeMillis();
			long rand = 0;
			if (secure){
				rand = mySecureRand.nextLong();
			} else{
				rand = myRand.nextLong();
			}
			
			sbValueBeforeMD5.append(s_id);
			sbValueBeforeMD5.append(":");
			sbValueBeforeMD5.append(Long.toString(time));
			sbValueBeforeMD5.append(":");
			sbValueBeforeMD5.append(Long.toString(rand));
			
			seedingString = sbValueBeforeMD5.toString();
			
			byte[] array = this.md5.digest(seedingString.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < array.length; ++j){
				int b = array[j] & 0xFF;
				if (b < 0x10){
					sb.append("0");
				}
				sb.append(Integer.toHexString(b));
			}
			rawGUID = sb.toString();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public String createNewGuid(){
		return this.createNewGuid(FORMAT_STRING, false);
	}
	
	public String createNewGuid(int nFormatType){
		return this.createNewGuid(nFormatType, false);
	}

	public synchronized String createNewGuid(int nFormatType, boolean secure){
		if(this.md5 == null){
			return null;
		}
		
		String sGuid = "";
		this.getRandomGUID(secure);
		
		switch(nFormatType){
			case BEFORE_MD5:
				sGuid = this.seedingString;
				break;
			case AFTER_MD5:
				sGuid = this.rawGUID;
				break;
			default:
				sGuid = this.toString();
				break;
		}

		return sGuid;
	}

	public String toString(){
		String raw = this.rawGUID.toUpperCase();
		StringBuffer sb = new StringBuffer();
		sb.append(raw.substring(0, 8));
		sb.append("-");
		sb.append(raw.substring(8, 12));
		sb.append("-");
		sb.append(raw.substring(12, 16));
		sb.append("-");
		sb.append(raw.substring(16, 20));
		sb.append("-");
		sb.append(raw.substring(20));
		return sb.toString();
	}
}
