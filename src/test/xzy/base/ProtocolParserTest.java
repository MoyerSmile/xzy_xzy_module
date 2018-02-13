package test.xzy.base;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

import com.xzy.base.Const;
import com.xzy.base.EncryptUtil;
import com.xzy.base.Util;
import com.xzy.base.parser.Item;
import com.xzy.base.parser.XZYProtocolParser;
import com.xzy.base.parser.verify.CrcVerifyFunc;
import com.xzy.base.parser.verify.SingleCharacterTransform;
import com.xzy.base.server.log.LogRecordServer;
import com.xzy.base_c.InfoContainer;

public class ProtocolParserTest extends BasicTest{
	public void init(){
		LogRecordServer.getSingleInstance().startServer();
	}
	@Test
	public void verifyTest() throws Exception{
		this.init();
		XZYProtocolParser parser = new XZYProtocolParser(null);
		
		Item item = new Item(parser,null);
		item.setName("test");
		CrcVerifyFunc func = new CrcVerifyFunc();
		func.setParas(item,new String[]{"crc8","1","-0x01"});
		
		ByteBuffer buff = ByteBuffer.allocate(32);
		for(int i=0;i<buff.capacity();i++){
			buff.array()[i] = (byte)(i * 2);
		}
		InfoContainer info = new InfoContainer();
		info.setInfo(item.getName(), new Integer(EncryptUtil.crc8((byte)0, buff.array(), 1, buff.capacity()-2)));
		if(!func.verify(parser, buff, info)){
			this.print("crc8 verfiry fail");
			Assert.fail("crc8 verfiry fail");
		}
		this.print("crc8 verfiry success");
		
		func = new CrcVerifyFunc();
		func.setParas(item,new String[]{"crc16","2","-0x10","0xff"});
		
		info.setInfo(item.getName(), new Integer(EncryptUtil.crc16(0xff, buff.array(), 2, buff.capacity()-2-0x10)));
		if(!func.verify(parser, buff, info)){
			this.print("crc16 verfiry fail");
			Assert.fail("crc16 verfiry fail");
		}
		this.print("crc16 verfiry success");
	}
	
	@Test
	public void transformTest() throws Exception{
		this.init();
		XZYProtocolParser parser = new XZYProtocolParser(null);
		SingleCharacterTransform t = new SingleCharacterTransform();
		t.setParas(new String[]{"0x7d","0x7d=0x01","0x7e=0x02"});
		byte[] data = new byte[]{0x01,0x02,0x7d,0x7d,0x7e,0x00,0x7e,0x02,0x7d};

		ByteBuffer buff = t.encode(parser, ByteBuffer.wrap(data) , 0, 0);
		String bcdStr = Const.byteArr2BcdStr(buff.array(), 0, buff.remaining());
		if(bcdStr.equalsIgnoreCase("01027d017d017d02007d02027d01")){
			this.print("transform encode test Success");
		}else{
			this.print("transform encode test Failure: "+bcdStr);
			Assert.fail("transform encode test Failure");
		}
		buff = t.encode(parser, ByteBuffer.wrap(data) , 0, 1);
		bcdStr = Const.byteArr2BcdStr(buff.array(), 0, buff.remaining());
		if(bcdStr.equalsIgnoreCase("01027d017d017d02007d02027d")){
			this.print("transform encode test Success");
		}else{
			this.print("transform encode test Failure: "+bcdStr);
			Assert.fail("transform encode test Failure");
		}
		
		data = new byte[]{0x7d,0x01,0x02,0x7d,0x01,0x7d,0x02,0x00,0x7d,0x02,0x7d,0x01};

		buff = t.decode(parser, ByteBuffer.wrap(data),0,0);
		bcdStr = Const.byteArr2BcdStr(buff.array(), 0, buff.remaining());
		if(bcdStr.equalsIgnoreCase("7d027d7e007e7d")){
			this.print("transform decode test Success");
		}else{
			this.print("transform decode test Failure");
			Assert.fail("transform decode test Failure");
		}

		buff = t.decode(parser, ByteBuffer.wrap(data),1,0);
		bcdStr = Const.byteArr2BcdStr(buff.array(), 0, buff.remaining());
		if(bcdStr.equalsIgnoreCase("7d01027d7e007e7d")){
			this.print("transform decode test Success");
		}else{
			this.print("transform decode test Failure");
			Assert.fail("transform decode test Failure");
		}
	}
	
	@Test
	public void templateTest() throws Exception{
		this.init();
		XZYProtocolParser parser = new XZYProtocolParser(new File(this.getClass().getClassLoader().getResource("test/xzy/base/head_tail_test.xml").getPath()));
		parser.startServer();
		if(parser.getCapacity() != 16*1024){
			this.print("capacity error."+parser.getCapacity());
			Assert.fail("capacity error.");
		}
		if(!parser.getName().equals("GB905")){
			this.print("protocol name error."+parser.getName());
			Assert.fail("protocol name error.");
		}
		if(parser.getByteOrder() != ByteOrder.BIG_ENDIAN){
			this.print("protocol endian error."+parser.getByteOrder());
			Assert.fail("protocol endian error.");
		}
		if(!parser.getCharset().equals("GBK")){
			this.print("protocol charset error."+parser.getCharset());
			Assert.fail("protocol charset error.");
		}
		if(parser.getDataTimeout() != 5000){
			this.print("data timeout error."+parser.getDataTimeout());
			Assert.fail("data timeout error.");
		}
		if(parser.getMatchMode() != XZYProtocolParser.MATCH_MODE.HEAD_TAIL){
			this.print("match mode error."+parser.getMatchMode());
			Assert.fail("match mode error.");
		}
		

		if(parser.getTrasformList().size() != 1){
			this.print("transform num error."+parser.getTrasformList().size());
			Assert.fail("transform num error.");
		}
		if(parser.getHead().getItemArr().length != 5){
			this.print("head item's num error."+parser.getHead().getItemArr().length);
			Assert.fail("head item's num error.");
		}
		if(parser.getTail().getItemArr().length != 2){
			this.print("tail item's num error."+parser.getTail().getItemArr().length);
			Assert.fail("tail item's num error.");
		}
		if(parser.getBody().getComeMessageNum() != 2){
			this.print("body come message's num error."+parser.getBody().getComeMessageNum());
			Assert.fail("body come message's num error.");
		}
		if(parser.getBody().getGoMessageNum() != 3){
			this.print("body go message's num error."+parser.getBody().getGoMessageNum());
			Assert.fail("body go message's num error.");
		}
		this.print("protocol template test success!");
		
	}
	
	@Test
	public void headTailParserTest() throws Exception{
		this.init();
		XZYProtocolParser parser = new XZYProtocolParser();
		parser.setPara("template", new File(this.getClass().getClassLoader().getResource("test/xzy/base/head_tail_test.xml").getPath()));
		parser.startServer();
		
		ByteBuffer datas = ByteBuffer.allocate(1024);
		datas.order(ByteOrder.BIG_ENDIAN);
		datas.put((byte)0x7e);
		datas.putShort((short)0x8001);
		datas.putShort((short)0);
		datas.put(Const.bcdStr2ByteArr("102030135790"));
		datas.putShort((short)0x8010);

		datas.putShort((short)0x7d01);
		datas.put((byte)0x20);
		
		String name = "ÐìÐÂ²âÊÔ";
		datas.put((byte)name.getBytes("GBK").length);
		datas.put(name.getBytes("GBK"));
		
		datas.putShort((short)0x9001);
		datas.put((byte)0x01);

		datas.put((byte)4);
		datas.put(Const.bcdStr2ByteArr("10203040"));

		datas.putShort((short)0x9001);
		datas.put((byte)0x01);
		
		int crc = EncryptUtil.crc8(0, datas.array(), 1, datas.position() - 1)^0x01;
		datas.put((byte)crc);
		datas.put((byte)0x7e);
		
		ByteBuffer dataBuffer = ByteBuffer.allocate(1024);
		
		dataBuffer.clear();
		dataBuffer.put(datas.array(),0,datas.position());
		dataBuffer.flip();
		InfoContainer[] infoArr = parser.decodeData(dataBuffer);
		this.checkData(crc,infoArr);
		
		dataBuffer.clear();
		int len = datas.position()/2;
		dataBuffer.put(datas.array(), 0, len);
		dataBuffer.flip();
		infoArr = parser.decodeData(dataBuffer);
		if(infoArr != null){
			this.print("should not has infocontainer");
			Assert.fail("should not has infocontainer");
		}
		dataBuffer.clear();
		dataBuffer.position(len);
		dataBuffer.put(datas.array(), len, datas.position()-len);
		dataBuffer.flip();
		infoArr = parser.decodeData(dataBuffer);
		this.checkData(crc,infoArr);

		dataBuffer.clear();
		dataBuffer.put(new byte[256], 0, 32);
		dataBuffer.flip();
		parser.decodeData(dataBuffer);
		
		int remain = dataBuffer.remaining();
		dataBuffer.clear();
		dataBuffer.position(remain);
		dataBuffer.put(datas.array(), 0, datas.position());
		dataBuffer.flip();
		infoArr = parser.decodeData(dataBuffer);
		this.checkData(crc,infoArr);

		this.print("XZYProtocolParser headtail decode Test Success");
	}
	
	private void checkData(int crc,InfoContainer[] infoArr) throws Exception{

		if(infoArr == null || infoArr.length == 0){
			this.print("XZYProtocolParser decode error,num = 0");
			Assert.fail("XZYProtocolParser decode error,num = 0");
		}
		
		if(!infoArr[0].getInfo("s_flag").equals(new Integer(0x7e))){
			this.print("XZYProtocolParser decode error,s_flag="+infoArr[0].getInfo("s_flag"));
			Assert.fail("XZYProtocolParser decode error,s_flag="+infoArr[0].getInfo("s_flag"));
		}
		if(!infoArr[0].getInfo("property").equals(new Integer(0))){
			this.print("XZYProtocolParser decode error,property="+infoArr[0].getInfo("property"));
			Assert.fail("XZYProtocolParser decode error,property="+infoArr[0].getInfo("property"));
		}
		if(!infoArr[0].getInfo("deviceId").equals("102030135790")){
			this.print("XZYProtocolParser decode error,deviceId="+infoArr[0].getInfo("deviceId"));
			Assert.fail("XZYProtocolParser decode error,deviceId="+infoArr[0].getInfo("deviceId"));
		}
		if(!infoArr[0].getInfo("flowId").equals(new Integer(0x8010))){
			this.print("XZYProtocolParser decode error,flowId="+infoArr[0].getInfo("flowId"));
			Assert.fail("XZYProtocolParser decode error,flowId="+infoArr[0].getInfo("flowId"));
		}

		if(!infoArr[0].getInfo("responseFlowId").equals(new Integer(0x7d20))){
			this.print("XZYProtocolParser decode error,responseFlowId="+infoArr[0].getInfo("responseFlowId"));
			Assert.fail("XZYProtocolParser decode error,responseFlowId="+infoArr[0].getInfo("responseFlowId"));
		}
		if(!infoArr[0].getInfo("name").equals("ÐìÐÂ²âÊÔ")){
			this.print("XZYProtocolParser decode error,name="+infoArr[0].getInfo("name"));
			Assert.fail("XZYProtocolParser decode error,name="+infoArr[0].getInfo("name"));
		}
		if(!infoArr[0].getInfo("bcd").equals("10203040")){
			this.print("XZYProtocolParser decode error,bcd="+infoArr[0].getInfo("bcd"));
			Assert.fail("XZYProtocolParser decode error,bcd="+infoArr[0].getInfo("bcd"));
		}
		if(!infoArr[0].getInfo("responseMessageId").equals(new Integer(0x9001))){
			this.print("XZYProtocolParser decode error,responseMessageId="+infoArr[0].getInfo("responseMessageId")+" "+0x9001);
			Assert.fail("XZYProtocolParser decode error,responseMessageId="+infoArr[0].getInfo("responseMessageId"));
		}
		if(!infoArr[0].getInfo("result").equals(new Integer(0x01))){
			this.print("XZYProtocolParser decode error,result="+infoArr[0].getInfo("result"));
			Assert.fail("XZYProtocolParser decode error,result="+infoArr[0].getInfo("result"));
		}
		
		
		if(!infoArr[0].getInfo("crc").equals(new Integer(crc))){
			this.print("XZYProtocolParser decode error,crc="+infoArr[0].getInfo("crc"));
			Assert.fail("XZYProtocolParser decode error,crc="+infoArr[0].getInfo("crc"));
		}
		
		if(!infoArr[0].getInfo("e_flag").equals(new Integer(0x7e))){
			this.print("XZYProtocolParser decode error,e_flag="+infoArr[0].getInfo("e_flag"));
			Assert.fail("XZYProtocolParser decode error,e_flag="+infoArr[0].getInfo("e_flag"));
		}
	}
	
	@Test
	public void parserLengthTest() throws Exception{
		this.init();
		XZYProtocolParser parser = new XZYProtocolParser();
		parser.addPara("template", this.getClass().getClassLoader().getResource("test/xzy/base/len_test.xml").getPath());
		parser.startServer();
		ByteBuffer datas = ByteBuffer.allocate(1024);
		datas.order(ByteOrder.LITTLE_ENDIAN);
		
		datas.put("XX".getBytes());
		datas.putShort((short)0x0102);
		datas.putInt(0x10);
		datas.put(Const.bcdStr2ByteArr("345678"));
		datas.putShort((short)0x0b);
		datas.put("ÐìÐÂ²âÊÔµÄa".getBytes("GBK"));
		

		ByteBuffer dataBuffer = ByteBuffer.allocate(1024);
		dataBuffer.put(datas.array(), 0, datas.position());
		dataBuffer.flip();
		InfoContainer[] infoArr = parser.decodeData(dataBuffer);
		this.checkLenData(infoArr);
		if(dataBuffer.remaining() != 0){
			this.print("XZYProtocolParser decode error,remain data should be zero");
			Assert.fail("XZYProtocolParser decode error,remain data should be zero");
		}

		dataBuffer.clear();
		dataBuffer.put(new byte[]{0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x10},0,10);
		dataBuffer.flip();
		parser.decodeData(dataBuffer);
		if(dataBuffer.remaining() != 1){
			this.print("XZYProtocolParser decode error,remain data should be zero."+dataBuffer.position());
			Assert.fail("XZYProtocolParser decode error,remain data should be zero."+dataBuffer.position());
		}
		int remain = dataBuffer.remaining();
		dataBuffer.clear();
		dataBuffer.position(remain);
		dataBuffer.put(datas.array(), 0, datas.position());
		dataBuffer.flip();
		infoArr = parser.decodeData(dataBuffer);
		if(dataBuffer.remaining() != 0){
			this.print("XZYProtocolParser decode error,remain data should be zero");
			Assert.fail("XZYProtocolParser decode error,remain data should be zero");
		}
		this.checkLenData(infoArr);
		
		dataBuffer.clear();
		dataBuffer.put(datas.array(), 0, 2);
		dataBuffer.flip();
		parser.decodeData(dataBuffer);
		if(dataBuffer.remaining() != 2){
			this.print("XZYProtocolParser decode error,remain data should be 2");
			Assert.fail("XZYProtocolParser decode error,remain data should be 2");
		}

		dataBuffer.clear();
		dataBuffer.position(2);
		dataBuffer.put(datas.array(), 2, datas.position()-2);
		dataBuffer.flip();
		infoArr = parser.decodeData(dataBuffer);
		this.checkLenData(infoArr);

		dataBuffer.clear();
		dataBuffer.put(datas.array(), 2, datas.position()-2);
		dataBuffer.flip();
		infoArr = parser.decodeData(dataBuffer);
		if(infoArr!=null){
			this.print("XZYProtocolParser decode error,infoArr should be null");
			Assert.fail("XZYProtocolParser decode error,infoArr should be null");
		}
		if(dataBuffer.remaining() != 1){
			this.print("XZYProtocolParser decode error,remain data should be zero");
			Assert.fail("XZYProtocolParser decode error,remain data should be zero");
		}
		
		dataBuffer.clear();
		dataBuffer.position(1);
		ByteBuffer a = ByteBuffer.allocate(datas.position()*5);
		a.put(datas.array(), 0, datas.position());
		dataBuffer.put(a.array(), 0, a.capacity());
		dataBuffer.flip();
		infoArr = parser.decodeData(dataBuffer);
		for(int i=0;i<infoArr.length;i++){
			this.checkLenData(new InfoContainer[]{infoArr[i]});
		}
		
		byte[] testByteArr;
		InfoContainer info = new InfoContainer();
		info.setInfo("msg", new Integer(0x0001));
		info.setInfo("s_flag", "XX");
		info.setInfo("responseFlowId_x", 0x0204);
		info.setInfo("responseMessageId", 0x0406);
		info.setInfo("result", 0x03);
		info.setInfo("remain", testByteArr = new byte[]{0,0x31,0x32,0x33,0x34,0x00,0x35,0x36});
		
		datas = parser.encodeData(info);
		this.print(Const.byteArrToHexString(datas.array(),datas.arrayOffset()+datas.position(),datas.remaining()));
		
		infoArr = parser.decodeData(datas);
		if(!infoArr[0].getInfo("msg").equals(info.getInfo("msg"))
				|| !infoArr[0].getInfo("s_flag").equals(info.getInfo("s_flag"))
				|| !infoArr[0].getInfo("responseFlowId_x").equals(info.getInfo("responseFlowId_x"))
				|| !infoArr[0].getInfo("responseMessageId").equals(info.getInfo("responseMessageId"))
				|| !infoArr[0].getInfo("result").equals(info.getInfo("result"))
				|| !Const.byteArr2BcdStr((byte[])infoArr[0].getInfo("remain"),0,((byte[])infoArr[0].getInfo("remain")).length).equals(Const.byteArr2BcdStr(testByteArr, 0, testByteArr.length))
				){
			this.print("XZYProtocolParser encode decode error for remain all,"+infoArr[0]);
			Assert.fail("XZYProtocolParser encode decode error for remain all,"+infoArr[0]);
		}
		
		datas = ByteBuffer.allocate(1024);
		datas.order(ByteOrder.LITTLE_ENDIAN);
		datas.put("XX".getBytes());
		datas.putShort((short)0x01);
		datas.putInt(0x12356789);
		datas.put("YYCC.d".getBytes());
		datas.position(datas.position()+4);
		datas.putInt(0x10);
		datas.put(Const.bcdStr2ByteArr("345678"));
		datas.putShort((short)0x0b);
		datas.put("ÐìÐÂ²âÊÔµÄa".getBytes("GBK"));
		datas.flip();
		info = parser.decodeData(datas)[0];
		ByteBuffer buff = parser.encodeData(info);
		InfoContainer info1 = parser.decodeData(buff)[0];
		if(info.contain(info1) != null){
			this.print("XZYProtocolParser encode decode error for remain all,"+info1);
			Assert.fail("XZYProtocolParser encode decode error for remain all,"+info1);
		}
		
		this.print("XZYProtocolParser Length encode decode Success.");


		Thread.sleep(500);
	}
	
	private void checkLenData(InfoContainer[] infoArr){
		if(infoArr == null || infoArr.length == 0){
			this.print("XZYProtocolParser decode error,num = 0");
			Assert.fail("XZYProtocolParser decode error,num = 0");
		}
		

		if(!infoArr[0].getInfo("msg").equals(new Integer(0x0102))){
			this.print("XZYProtocolParser decode error,msg="+infoArr[0].getInfo("msg"));
			Assert.fail("XZYProtocolParser decode error,msg="+infoArr[0].getInfo("msg"));
		}
		if(!infoArr[0].getInfo("bcd_test").equals("345678")){
			this.print("XZYProtocolParser decode error,bcd_test="+infoArr[0].getInfo("bcd_test"));
			Assert.fail("XZYProtocolParser decode error,bcd_test="+infoArr[0].getInfo("bcd_test"));
		}
		if(!infoArr[0].getInfo("name").equals("ÐìÐÂ²âÊÔµÄa")){
			this.print("XZYProtocolParser decode error,msg="+infoArr[0].getInfo("name"));
			Assert.fail("XZYProtocolParser decode error,msg="+infoArr[0].getInfo("name"));
		}
	}
	
	@Test
	public void protocolEncodeTest() throws Exception{
		this.init();
		XZYProtocolParser parser = new XZYProtocolParser();
		parser.setPara("template", new File(this.getClass().getClassLoader().getResource("test/xzy/base/head_tail_test.xml").getPath()));
		parser.startServer();
		
		InfoContainer info = new InfoContainer();
		info.setInfo("msg", new Integer(0x8001));
		info.setInfo("property", "0x0105");
		info.setInfo("deviceId", "123456789012");
		info.setInfo("flowId", new Integer(8009));
		info.setInfo("responseFlowId", "0x02");
		info.setInfo("name", "Ðé´ÊÄØ²âÊÔÄØ ¹þ¹þddd´ó´ó");
		info.setInfo("responseMessageId", "6789");
		info.setInfo("result", "23");
		info.setInfo("bcd", new byte[]{0x33,0x44,0x55,0x66});
		info.setInfo("longid", 0x7FFFFFFFFFFFFFFFl);
		ByteBuffer buff = parser.encodeData(info);
		
		System.out.println(Const.byteArrToHexString(buff.array(),buff.position(),buff.remaining()));
		
		InfoContainer infos = parser.decodeData(buff)[0];
		
		if(!infos.getInfo("msg").equals(info.getInfo("msg")) 
				|| !infos.getInfo("property").equals(info.getInteger("property")) 
				|| !infos.getInfo("deviceId").equals(info.getInfo("deviceId")) 
				|| !infos.getInfo("flowId").equals(info.getInteger("flowId")) 
				|| !infos.getInfo("responseFlowId").equals(info.getInteger("responseFlowId")) 
				|| !infos.getInfo("name").equals(info.getInfo("name")) 
				|| !infos.getInfo("responseMessageId").equals(info.getInteger("responseMessageId")) 
				|| !infos.getInfo("result").equals(info.getInteger("result")) 
				|| !infos.getInfo("bcd").equals("33445566") 
				|| !infos.getInfo("longid").equals(info.getInfo("longid"))
				){
			this.print("Protocol Encode Test Fail!"+infos);
			Assert.fail("Protocol Encode Test Fail!");
		}
		this.print("Protocol Encode Test Success");
	}
	
	@Test
	public void groupTest() throws Exception{
		this.init();
		XZYProtocolParser parser = new XZYProtocolParser();
		parser.setPara("template", new File(this.getClass().getClassLoader().getResource("test/xzy/base/head_tail_test.xml").getPath()));
		parser.startServer();
		
		InfoContainer info = new InfoContainer();
		info.setInfo("msg", new Integer(0x8005));
		info.setInfo("property", "0x0105");
		info.setInfo("deviceId", "123456789012");
		info.setInfo("flowId", new Integer(8009));
		
		info.setInfo("info_x", "ÐìÐÂhello²âÊÔs123");
		LinkedList<InfoContainer> subInfoList = new LinkedList<InfoContainer>(),subSubInfoList = null;
		InfoContainer subInfo,subSubInfo;
		for(int i=0;i<5;i++){
			subInfo = new InfoContainer();
			subInfo.setInfo("r", new Integer(i));
			subInfo.setInfo("bcd", "112233"+i+""+i);
			subSubInfoList = new LinkedList<InfoContainer>();
			subInfo.setInfo("group_1_1", subSubInfoList);
			for(int j=0;j<7;j++){
				subSubInfo = new InfoContainer();
				subSubInfo.setInfo("info_y", "ÐìÐÂh  "+i+"_"+j);
				subSubInfoList.add(subSubInfo);
			}
			subInfoList.add(subInfo);
		}
		info.setInfo("group_1", subInfoList);
		
		subInfoList = new LinkedList<InfoContainer>();
		for(int i=0x7d;i<0x7d+10;i++){
			subInfo = new InfoContainer();
			subInfo.setInfo("r", new byte[]{(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i,(byte)i});
			subInfoList.add(subInfo);
		}
		info.setInfo("group_2", subInfoList);
		

		subInfoList = new LinkedList<InfoContainer>();
		for(int i=0;i<9;i++){
			subInfo = new InfoContainer();
			subInfo.setInfo("id", i);
			subInfo.setInfo("val", ("valInfo_"+i).getBytes());
			subInfoList.add(subInfo);
		}
		info.setInfo("group_3", subInfoList);

		ByteBuffer buff = parser.encodeData(info);
		
		System.out.println(Const.byteArrToHexString(buff.array(),buff.position(),buff.remaining()));

		InfoContainer infos = parser.decodeData(buff)[0];
		
		if(!infos.getInfo("info_x").equals("ÐìÐÂhello²âÊÔ")){
			this.print("Group Protocol Test Fail!"+infos.getInfo("info_x"));
			Assert.fail("Group Protocol Test Fail!"+infos.getInfo("info_x"));
		}
		
		subInfoList = (LinkedList<InfoContainer>)infos.getInfo("group_1");
		if(subInfoList.size() != 5){
			this.print("Group Protocol Test Fail!"+subInfoList);
			Assert.fail("Group Protocol Test Fail!"+subInfoList);
		}
		for(int i=0;i<5;i++){
			subInfo = subInfoList.get(i);
			if(!subInfo.getInfo("r").equals(new Integer(i))
			 || !subInfo.getInfo("bcd").equals( "112233"+i+""+i)
					){
				this.print("Group Protocol Test Fail!"+subInfo);
				Assert.fail("Group Protocol Test Fail!"+subInfo);
			}
			
			subSubInfoList = (LinkedList<InfoContainer>)subInfo.getInfo("group_1_1");
			if(subSubInfoList.size() != 10){
				this.print("Group Protocol subGroup Test Fail!"+subSubInfoList.size());
				Assert.fail("Group Protocol subGroup Test Fail!"+subSubInfoList.size());
			}
			for(int j=0;j<subSubInfoList.size();j++){
				subSubInfo = subSubInfoList.get(j);
				if(!subSubInfo.getInfo("info_y").equals(j<7?"ÐìÐÂh  "+i+"_"+j:"")){
					this.print("Group Protocol subGroup Test Fail!"+subInfo.getInfo("info_y"));
					Assert.fail("Group Protocol subGroup Test Fail!"+subInfo.getInfo("info_y"));
				}
			}
		}

		subInfoList = (LinkedList<InfoContainer>)infos.getInfo("group_2");
		if(subInfoList.size() != 2){
			this.print("Group Protocol Test Fail!"+subInfoList);
			Assert.fail("Group Protocol Test Fail!"+subInfoList);
		}
		byte[] aa;
		for(int i=0;i<2;i++){
			subInfo = subInfoList.get(i);
			aa = (byte[])subInfo.getInfo("r");
			if(aa.length != 20 || aa[0] != i+0x7d || aa[aa.length - 1] != i+0x7d
					){
				this.print("Group Protocol Test Fail!"+subInfo);
				Assert.fail("Group Protocol Test Fail!"+subInfo);
			}
		}
		subInfoList = (LinkedList<InfoContainer>)infos.getInfo("group_3");
		if(subInfoList.size() != 9){
			this.print("Group Protocol Test Fail!"+subInfoList);
			Assert.fail("Group Protocol Test Fail!"+subInfoList);
		}
		for(int i=0;i<subInfoList.size();i++){
			subInfo = subInfoList.get(i);
			if(!subInfo.getInteger("id").equals(i) || !new String((byte[])subInfo.getInfo("val")).equals("valInfo_"+i)
					){
				this.print("Group Protocol Test unlimit group Fail!"+subInfo);
				Assert.fail("Group Protocol Test unlimit group Fail!"+subInfo);
			}
		}
		
		this.print("Protocol Encode Test Success");
		Thread.sleep(500);
	}
}
