package test.xzy.base;

import java.nio.ByteBuffer;

import com.xzy.base.parser.IVerifyFunc;
import com.xzy.base.parser.Item;
import com.xzy.base.parser.XZYProtocolParser;
import com.xzy.base_c.InfoContainer;

public class TestExistVerify implements IVerifyFunc {
	private String name = null;
	private Item item = null;
	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		this.name = name;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

	@Override
	public void setParas(Item item, String[] paraArr) {
		// TODO Auto-generated method stub
		this.item = item;
	}

	@Override
	public boolean existVerify(XZYProtocolParser parser, ByteBuffer fullData,
			InfoContainer info) {
		if(info.getInteger("msg").intValue() == 0x01){
			return true;
		}
		return false;
	}

	@Override
	public void createVerifyVal(XZYProtocolParser parser, ByteBuffer fullData,
			InfoContainer info) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean verify(XZYProtocolParser parser, ByteBuffer fullData,
			InfoContainer info) {
		// TODO Auto-generated method stub
		return true;
	}

}
