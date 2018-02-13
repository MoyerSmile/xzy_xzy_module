package test.xzy.base;

import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.xzy.base.Const;
import com.xzy.base.GUIDCreator;
import com.xzy.base.Geo_Util;

import static org.junit.Assert.*;

public class UtilTest {
	@Test
	public void dateFormatTest() throws Exception{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2018);
		cal.set(Calendar.MONTH, 9);
		cal.set(Calendar.DAY_OF_MONTH, 26);
		cal.set(Calendar.HOUR_OF_DAY, 14);
		cal.set(Calendar.MINUTE, 28);
		cal.set(Calendar.SECOND, 56);
		cal.set(Calendar.MILLISECOND, 0);
		String str = Const.getDateFormater("yyyyMMddHHmmss").format(cal.getTime());
		if(str.equals("20181026142856")){
			System.out.println("Date Format Test Success."+str);
		}else{
			System.out.println("Date Format Test Fail."+str);
			Assert.fail("Date Format Test Fail");
		}
		
		Date date = Const.getDateFormater("yyyyMMddHHmmss").parse("20181026142856");
		if(cal.getTimeInMillis() == date.getTime()){
			System.out.println("Date Parse Test Success."+cal.getTimeInMillis()+" "+date.getTime());
		}else{
			System.out.println("Date Parse Test Fail."+cal.getTimeInMillis()+" "+date.getTime());
			Assert.fail("Date Format Test Fail."+cal.getTimeInMillis()+" "+date.getTime());
		}
	}
	
	@Test
	public void funcTest() throws Exception{
		byte[] data = new byte[]{0x00,0x01,(byte)0xff,(byte)0xff,0x45,0x32,(byte)0xff,(byte)0xff,0x44};
		long x = Const.createLongValue(data, 1, 1, ByteOrder.BIG_ENDIAN);
		if(x != 0x01){
			fail("createLongValue Func Test Failure! 0x01="+Long.toHexString(x));
		}
		x = Const.createLongValue(data, 1, 2, ByteOrder.BIG_ENDIAN);
		if(x != 0x01ff){
			fail("createLongValue Func Test Failure! 0x01ff="+Long.toHexString(x));
		}
		x = Const.createLongValue(data, 1, 2, ByteOrder.LITTLE_ENDIAN);
		if(x != 0xff01){
			fail("createLongValue Func Test Failure! 0xff01="+Long.toHexString(x));
		}
		x = Const.createLongValue(data, 1, 3, ByteOrder.BIG_ENDIAN);
		if(x != 0x01ffff){
			fail("createLongValue Func Test Failure! 0x01ffff="+Long.toHexString(x));
		}
		x = Const.createLongValue(data, 1, 3, ByteOrder.LITTLE_ENDIAN);
		if(x != 0xffff01){
			fail("createLongValue Func Test Failure! 0xffff01="+Long.toHexString(x));
		}
		x = Const.createLongValue(data, 1, 4, ByteOrder.BIG_ENDIAN);
		if(x != 0x01ffff45l){
			fail("createLongValue Func Test Failure! 0x01ffff45l="+Long.toHexString(x));
		}
		x = Const.createLongValue(data, 1, 4, ByteOrder.LITTLE_ENDIAN);
		if(x != 0x45ffff01l){
			fail("createLongValue Func Test Failure! 0x45ffff01l="+Long.toHexString(x));
		}
		x = Const.createLongValue(data, 1, 5, ByteOrder.BIG_ENDIAN);
		if(x != 0x01ffff4532l){
			this.print("createLongValue Func Test Failure! 0x01ffff4532l="+Long.toHexString(x));
			fail("createLongValue Func Test Failure! 0x01ffff4532l="+Long.toHexString(x));
		}
		x = Const.createLongValue(data, 1, 5, ByteOrder.LITTLE_ENDIAN);
		if(x != 0x3245ffff01l){
			fail("createLongValue Func Test Failure! 0x3245ffff01l="+Long.toHexString(x));
		}
		x = Const.createLongValue(data, 1, 6, ByteOrder.BIG_ENDIAN);
		if(x != 0x01ffff4532ffl){
			fail("createLongValue Func Test Failure! 0x01ffff4532ffl="+Long.toHexString(x));
		}
		x = Const.createLongValue(data, 1, 6, ByteOrder.LITTLE_ENDIAN);
		if(x != 0xff3245ffff01l){
			fail("createLongValue Func Test Failure! 0xff3245ffff01l="+Long.toHexString(x));
		}
		x = Const.createLongValue(data, 1, 7, ByteOrder.BIG_ENDIAN);
		if(x != 0x01ffff4532ffffl){
			fail("createLongValue Func Test Failure! 0x01ffff4532ffffl="+Long.toHexString(x));
		}
		x = Const.createLongValue(data, 1, 7, ByteOrder.LITTLE_ENDIAN);
		if(x != 0xffff3245ffff01l){
			fail("createLongValue Func Test Failure! 0xffff3245ffff01l="+Long.toHexString(x));
		}
		
		data = new byte[]{(byte)0x80,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
		for(int i=1;i<=8;i++){
			x = Const.createLongValue(data, 0, i, ByteOrder.BIG_ENDIAN, true);
			
			if(x != -(1l<<(i*8-1))){
				this.print("createLongValue Func Test Failure! "+-(1l<<(i*8-1))+"="+x+" "+Integer.MIN_VALUE);
				fail("createLongValue Func Test Failure! -0x8000="+Long.toHexString(x));
			}
		}

		
		this.print("createLongValue Func Test Success!");
	}
	
	@Test
	public void geoTest(){
		long d1 = Geo_Util.getDistance(121, 31, 122, 31);
		long d2 = Geo_Util.getDistance(121, 31, 121, 30);
		
		if(Math.abs(d1 - 95421) < 1000 && Math.abs(d2 - 111321) < 1000){
			System.out.println("GeoUtil distance Test Success!");
		}else{
			System.out.println("GeoUtil distance Test Failure!"+d1+" "+d2);
			fail("GeoUtil distance Test Failure!"+d1+" "+d2);
		}
		
		double[] arr = Geo_Util.wgs2BD(31, 121);
		if(arr[0]-31 < 0.004 && arr[1]-121<0.011){
			print("Geo BD Translate Success:"+arr[0]+","+arr[1]);
		}else{
			print("Geo BD Translate Fail:"+arr[0]+","+arr[1]);
			fail("Geo BD Translate Fail:"+arr[0]+","+arr[1]);
		}
	}
	
	@Test
	public void guidTest(){
		String guid = GUIDCreator.getSingleInstance().createNewGuid();
		if(guid == null || guid.length() < 36){
			fail("guid test fail");
		}
		
		for(int i=0;i<1000;i++){
			if(GUIDCreator.getSingleInstance().createNewGuid().equals(guid)){
				fail("guid test fail");
			}
		}
		
		this.print("guid Test Success");
	}

	private void print(String message){
		System.out.println(this.getClass().getName()+":"+message);
	}
}
