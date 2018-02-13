package com.xzy.base;

import java.util.HashMap;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

public class Util {
	// 返回是否windows系统
	public static boolean isWindowsOS(){
		boolean isWindows = true;
		String sysName = System.getProperty("os.name");
		if(sysName != null){
			isWindows = (sysName.toLowerCase().indexOf("windows") >= 0);
		}
		return isWindows;
	}
	
	public static void sleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 字符转汉语拼音，多音字的话返回多个值
	 * http://pinyin4j.sourceforge.net/pinyin4j-doc/net/sourceforge/pinyin4j/PinyinHelper.html
	 * @param ch
	 * @param pyFormat
	 * @return
	 */
	public String[] char2PinYin(char ch,HanyuPinyinOutputFormat pyFormat){
		if(pyFormat == null){
			pyFormat = new  HanyuPinyinOutputFormat();
	        pyFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
	        pyFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
	        pyFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
        }

        try{
        	String[] arr = PinyinHelper.toHanyuPinyinStringArray(ch, pyFormat);
        	if(pyFormat.getToneType() == HanyuPinyinToneType.WITHOUT_TONE){
        		HashMap mapping = new HashMap();
        		for(int i=0;i<arr.length;i++){
        			mapping.put(arr[i], null);
        		}
        		arr = new String[mapping.size()];
        		mapping.keySet().toArray(arr);
        	}
        	return arr;
		}catch(Exception e){
			e.printStackTrace();
		}
        return null;
	}
	public String string2PinYin(String chinese){
		return this.string2PinYin(chinese, null, null);
	}
	public String string2PinYin(String chinese,String splitStr){
		return this.string2PinYin(chinese, splitStr, null);
	}
	/**
	 * 字符串转汉语拼音
	 * http://pinyin4j.sourceforge.net/pinyin4j-doc/net/sourceforge/pinyin4j/PinyinHelper.html
	 * @param chinese 中文字符串
	 * @param splitStr 分割字符
	 * @param pyFormat 字符格式
	 * @return
	 */
	public String string2PinYin(String chinese,String splitStr,HanyuPinyinOutputFormat pyFormat){
		if(pyFormat == null){
			pyFormat = new  HanyuPinyinOutputFormat();
	        pyFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
	        pyFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
	        pyFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
        }
		if(splitStr == null){
			splitStr = "";
		}

        try{
	        return PinyinHelper.toHanyuPinyinString(chinese, pyFormat, splitStr);
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        return null;
	}
}
