package com.xzy.base;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

public class Const {
	private static Hashtable<String,SimpleDateFormat> timeFormatMapping = new Hashtable<String,SimpleDateFormat>();
	/**
	 * �õ����ڵĸ�ʽ������.--
	 * @param formater ��ʽ���ĸ�ʽ
	 * @return ��ʽ������
	 */
	public static SimpleDateFormat getDateFormater(String formater){
		SimpleDateFormat format = timeFormatMapping.get(formater);
		if(format == null){
			format = new XZYDateFormat(formater);
			timeFormatMapping.put(formater, format);
		}
		return format;
	}
	
	/**
	 * ��Exception����ת��Ϊ�ַ������󣬽��д�ӡ��;
	 * @param e �쳣����
	 * @return
	 */
	public static String exception2Str(Throwable e){
		ByteArrayOutputStream out = new ByteArrayOutputStream(256);
		PrintStream print = new PrintStream(out);
		e.printStackTrace(print);
		print.close();
		return out.toString();
	}
	
	public static void exception2Str(Throwable e,StringBuffer buff){
		ByteArrayOutputStream out = new ByteArrayOutputStream(256);
		PrintStream print = new PrintStream(out);
		e.printStackTrace(print);
		print.close();
		buff.append(out.toString());
	}
	
	public static long createLongValue(byte[] datas,int offset,int len,ByteOrder order) throws Exception{
		return Const.createLongValue(datas, offset, len, order, false);
	}
	public static long createLongValue(byte[] datas,int offset,int len,ByteOrder order,boolean ishighBitForSymbo) throws Exception{
		long v = 0;
		int moveNum = (order == ByteOrder.BIG_ENDIAN?(len - 1)*8:0);
		
		boolean isNegative = false;
		if(ishighBitForSymbo){
			if(order == ByteOrder.BIG_ENDIAN){
				isNegative = (datas[offset]&0x80) > 0;
			}else{
				isNegative = (datas[offset+len-1]&0x80) > 0;
			}
		}
		for(int i=0;i<len;i++,offset++){
			if(order == ByteOrder.BIG_ENDIAN){
				v |= (datas[offset]&0xFFl)<<moveNum;
				moveNum -= 8;
			}else{
				v |= (datas[offset]&0xFFl)<<moveNum;
				moveNum += 8;
			}
		}
		
		if(isNegative && len < 8){
			long xor = (1l<<(len*8)) - 1;
			v = -((v ^ xor) + 1);
		}
		
		return v;
	}

	public static long parseLong(String str){
		if(str == null){
			return 0;
		}
		str = str.trim();
		if(str.indexOf("0x") >= 0){//֧�֣�0x80
			return Long.parseLong(str.replaceAll("0x", ""), 16);
		}else{
			return Long.parseLong(str);
		}
	}
	public static int parseInt(String str){
		if(str == null){
			return 0;
		}
		str = str.trim();
		if(str.indexOf("0x") >= 0){//֧�֣�0x80
			return Integer.parseInt(str.replaceAll("0x", ""), 16);
		}else{
			return Integer.parseInt(str);
		}
	}
	
	
	public static String byteArrToHexString(byte[] bytes){
		return Const.byteArrToHexString(bytes, 0, bytes.length);
    }
	/**
	 * �ֽ�����ת16�����ַ�����ʹ�ÿո�ָ�
	 * @param bytes
	 * @param offset
	 * @param len
	 * @return
	 */
    public static String byteArrToHexString(byte[] bytes,int offset,int len){
        if(bytes == null){
        	return "";
        }
        StringBuffer infoBuff = new StringBuffer(len*2);
        int value;
        for(int i=0;i<len;i++,offset++){
        	if(i>0){
        		infoBuff.append(" ");
        	}
        	value = bytes[offset]&0xff;
            if(value < 16){
            	infoBuff.append("0");
            }
            infoBuff.append(Integer.toHexString(value));
        }
        return infoBuff.toString();
    }
    
    /**
     * ��ȡһ��url�ķ�����Ϣ
     * @param url
     * @return
     */
	public static ByteBuffer readURL(URL url){
		if(url == null){
			return null;
		}
		InputStream in = null;
		try{
			URLConnection conn = url.openConnection();
			int len = conn.getContentLength();
			in = new BufferedInputStream(url.openStream());
			byte[] data = new byte[len];
			int count = 0,tempCount = 0;
			while(tempCount >= 0 && count < len){
				tempCount = in.read(data, count, len-count);
				if(tempCount > 0){
					count += tempCount;
				}
			}
			return ByteBuffer.wrap(data, 0, count);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			try{
				if(in != null){
					in.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	/**
	 * �����а�Ŀ���������
	 * @param in
	 * @param buff
	 * @param offset
	 * @param len
	 * @return true�����ɹ�����֮����ʧ��
	 * @throws Exception
	 */
	public static boolean readFull(InputStream in,byte[] buff,int offset,int len) throws Exception{
		if(offset + len > buff.length){
			throw new Exception("Length OverFlow");
		}
		int tCount ;
		while(len > 0){
			tCount = in.read(buff, offset, len);
			if(tCount < 0){
				return false;
			}
			offset += tCount;
			len -= tCount;
		}
		return true;
	}
	/**
	 * ��һ���ļ������ݶ�ȡ���ֽ�������
	 * @param f
	 * @return
	 */
	public static byte[] readFile(File f){
		if(f == null || !f.exists() || !f.isFile()){
			return null;
		}
		
		int len = (int)f.length();
		int count = 0,tempCount;
		
		InputStream in = null;
		try{
			byte[] data = new byte[len];
			in = new BufferedInputStream(new FileInputStream(f));
			while(count < len){
				tempCount = in.read(data, count, len-count);
				if(tempCount < 0){
					throw new Exception("File Eof Error!");
				}
				count += tempCount;
			}
			return data;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			try{
				if(in != null){
					in.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	

	/***************************************************************************
	 * BCD�ַ����ֽ�֮���ת������
	 */
	public static byte[] bcdStr2ByteArr(String bcdStr){
		if(bcdStr == null){
			return null;
		}
        if(bcdStr.length()%2==1){
            bcdStr="0"+bcdStr;
        }
		int num = bcdStr.length() / 2;
		byte[] data = new byte[num];

		try{
			for(int i = 0, index = 0; i < num; i++, index += 2){
				data[i] = (byte) (Integer.parseInt(bcdStr.substring(index, index + 2), 16) & 0xFF);
			}
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}

		return data;
	}

	/**
	 * �ֽ�����ת����BCD�ַ���
	 * @param data �ֽ�����
	 * @param offset ��ʼת��λ��
	 * @param len    ��Ҫת�����ֽ���
	 * @return BCD�ַ���
	 */
	public static String byteArr2BcdStr(byte[] data, int offset, int len){
		if(data == null){
			return null;
		}
		if(offset < 0){
			return null;
		}
		int num = data.length;
		if(num < offset + len){
			return null;
		}
		StringBuffer buff = new StringBuffer(len * 2);

		len += offset;
		int value;
		for(int i = offset; i < len; i++){
			value = data[i] & 0xFF;
			if(value < 16){
				buff.append("0");
			}
			buff.append(Integer.toHexString(value));
		}

		return buff.toString().toUpperCase();
	}
	
	public static boolean saveFileWithSecurity(byte[] fileData,String fileName){
		return saveFileWithSecurity(fileData,fileName,null);
	}
	public static boolean saveFileWithSecurity(byte[] fileData,String fileName,int andor){
		return saveFileWithSecurity(fileData,0,fileData.length,fileName,andor);
	}
	public static boolean saveFileWithSecurity(byte[] fileData,int offset,int len,String fileName){
		return saveFileWithSecurity(fileData,offset,len,fileName,null);
	}
	public static boolean saveFileWithSecurity(byte[] fileData,int offset,int len,String fileName,int andor){
		return saveFileWithSecurity(fileData,offset,len,fileName,null,andor);
	}
	public static boolean saveFileWithSecurity(byte[] fileData,String fileName,String bakFileName){
		return saveFileWithSecurity(fileData,0,fileData.length,fileName,bakFileName);
	}
	public static boolean saveFileWithSecurity(byte[] fileData,int offset,int len,String fileName,String bakFileName){
		return saveFileWithSecurity(fileData,offset,len,fileName,bakFileName,0);
	}
	private static final Object WRITE_LOCK = new Object();
	
	/**
	 * �洢�ļ���Ϣ
	 * @param fileData ����������
	 * @param offset   ����ƫ��
	 * @param len      ��Ҫд�������
	 * @param fileName ��д���ļ���
	 * @param bakFileName �����ļ���
	 * @param andor     ���ܵ�����ֽ�
	 * @return  �Ƿ�洢�ɹ�
	 */
	public static boolean saveFileWithSecurity(byte[] fileData,int offset,int len,String fileName,String bakFileName,int andor){
		if(fileName == null){
			return false;
		}
		if(bakFileName == null || bakFileName.trim().length() == 0){
			bakFileName = getDefaultBakFileName(fileName);
		}
		
		synchronized(WRITE_LOCK){
			File f = new File(fileName);
			File tempFile = new File(fileName+".bak");
			File bakFile = new File(bakFileName);
			
			if(f.exists()){
				if(bakFile.exists()){
					if(bakFile.lastModified() < f.lastModified()){
						f = bakFile;
					}
				}else{
					f = bakFile;
				}
			}
			f.getAbsoluteFile().getParentFile().mkdirs();
			
			FileOutputStream out = null;
			try{
				if(andor != 0){
					for(int i=0,count=offset;i<len;i++,count++){
						fileData[count] = (byte)(fileData[count]^andor);
					}
				}
				out = new FileOutputStream(tempFile);
				out.write(fileData,offset,len);
				out.close();
				
				f.delete();
				tempFile.renameTo(f);
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}finally{
				if(out != null){
					try{
						out.close();
					}catch(Exception e){}
				}
			}
		}
		return true;
	}
	
	public static byte[] loadFileWithSecurity(String fileName){
		return loadFileWithSecurity(fileName,null);
	}
	public static byte[] loadFileWithSecurity(String fileName,int andor){
		return loadFileWithSecurity(fileName,null,andor);
	}
	public static byte[] loadFileWithSecurity(String fileName,String bakFileName){
		return loadFileWithSecurity(fileName,bakFileName,0);
	}
	/**
	 * �����ļ���Ϣ
	 * @param fileName  �ļ�����
	 * @param bakFileName �����ļ�����
	 * @param andor ���ܵ���������ֽ�
	 * @return �ļ�
	 */
	public static byte[] loadFileWithSecurity(String fileName,String bakFileName,int andor){
		if(fileName == null){
			return null;
		}
		if(bakFileName == null || bakFileName.trim().length() == 0){
			bakFileName = getDefaultBakFileName(fileName);
		}
		
		File f = new File(fileName);
		File bakFile = new File(bakFileName);
		if(bakFile.exists()){
			if(f.exists()){
				if(bakFile.lastModified() > f.lastModified()){
					f = bakFile;
				}
			}else{
				f = bakFile;
			}
		}
		if(f.exists()){
			FileInputStream in = null;
			try{
				int len = (int)f.length();
				
				byte[] fileData = new byte[len];
				in = new FileInputStream(f);
				int count = 0,tempCount;
				while(count < len){
					tempCount = in.read(fileData, count, len-count);
					if(tempCount < 0){
						return null;
					}
					count += tempCount;
				}
				
				
				if(andor != 0){
					for(int i=0;i<len;i++){
						fileData[i] = (byte)(fileData[i]^andor);
					}
				}
				return fileData;
			}catch(Exception e){
				e.printStackTrace();
				return null;
			}finally{
				if(in != null){
					try{
						in.close();
					}catch(Exception e){}
				}
			}
		}
		return null;
	}
	
	private static String getDefaultBakFileName(String _fileName){
		File f = new File(_fileName);
		String fileName = f.getName();
		
		String bakFileName = null;
		
		int index = fileName.lastIndexOf('.');
		if(index >= 0){
			bakFileName = fileName.substring(0, index) + "_2"+fileName.substring(index);
		}else{
			bakFileName = fileName + "_2";
		}
		
		f = new File(f.getAbsoluteFile().getParentFile(),bakFileName);
		return f.getAbsolutePath();
	}
	
	

	public static class XZYDateFormat extends SimpleDateFormat {
		public XZYDateFormat(String pattern){
			super(pattern);
		}
		public synchronized Date parse(String source, ParsePosition pos){
			return super.parse(source, pos);
		}
		public synchronized StringBuffer format(Date date, StringBuffer toAppendTo,
	            FieldPosition pos){
			return super.format(date, toAppendTo, pos);
		}
	}
}