package test.xzy.base;

import java.util.LinkedList;

import junit.framework.Assert;

import org.junit.Test;

import com.xzy.base_c.InfoContainer;

public class BasicTest {
	@Test
	public void infoContainerTest(){
		InfoContainer info1 = new InfoContainer(),info11 = new InfoContainer();
		InfoContainer info2 = new InfoContainer(),info22 = new InfoContainer();
		LinkedList<InfoContainer> list1 = new LinkedList<InfoContainer>();
		LinkedList<InfoContainer> list2 = new LinkedList<InfoContainer>();
		info1.setInfo("1", "111");
		info1.setInfo("2", new Integer(2));
		info1.setInfo("x", list1);
		info2.setInfo("1", "111");
		info2.setInfo("2", new Integer(2));
		info2.setInfo("x", list2);

		list1.add(info11);
		info11.setInfo("11", "xuxi");
		list2.add(info22);
		info22.setInfo("11", "xuxi");
		
		if(!info1.equals(info2)){
			this.print("infoContainer equals Test Failure");
			Assert.fail("infoContainer equals Test Failure");
		}
		this.print("infoContainer equals Test Success");
	}
	
	public void print(String message){
		System.out.println(this.getClass().getName()+":"+message);
	}
}
