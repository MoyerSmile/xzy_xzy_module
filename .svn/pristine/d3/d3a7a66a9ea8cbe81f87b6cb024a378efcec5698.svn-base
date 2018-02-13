package com.xzy.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLUtil {
	public static Document parseXml(File f){
		if(!f.exists() || !f.isFile()){
			return null;
		}
		FileInputStream in = null;
		try{
			in = new FileInputStream(f);
			return parseXml(in);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static Document parseXml(InputStream in){
		try{
			DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = domfac.newDocumentBuilder();
			
			return builder.parse(in);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public static String getNodeAttr(Node node, String attrName){
		if(node == null || attrName == null){
			return null;
		}
		if(node.getNodeType() != Node.ELEMENT_NODE){
			return null;
		}
		
		Node attrNode = node.getAttributes().getNamedItem(attrName);
		if(attrNode == null){
			return null;
		}
		return attrNode.getNodeValue();
	}

	public static Node[] getAllAttrNode(Node node){
		if(node == null){
			return null;
		}
		NamedNodeMap nodeMap = node.getAttributes();
		if(nodeMap == null){
			return null;
		}
		Node[] nodes = new Node[nodeMap.getLength()];
		for(int i = 0; i < nodeMap.getLength(); i++){
			nodes[i] = nodeMap.item(i);
		}
		return nodes;
	}

	public static Node getFirstChild(Node ele){
		if(!(ele instanceof Element)){
			return null;
		}
		return ((Element) ele).getFirstChild();
	}
	public static Node getSingleElementByTagName(Node ele, String tagName){
		if(!(ele instanceof Element)){
			return null;
		}
		NodeList all = ((Element) ele).getElementsByTagName(tagName);
		if(all.getLength() > 0){
			return all.item(0);
		}
		return null;
	}

	public static Node getSonSingleElementByTagName(Node ele, String tagName){
		if(!(ele instanceof Element)){
			return null;
		}
		NodeList all = ((Element) ele).getElementsByTagName(tagName);
		
		for(int i=0;i<all.getLength();i++){
			Node node = all.item(i);
			if(node.getParentNode() == ele){
				return node;
			}
		}
		return null;
	}

	public static String getSingleElementTextByTagName(Node parentNode,
										String tagName){
		String text = XMLUtil.getNodeText(XMLUtil.getSingleElementByTagName(parentNode, tagName));
		if(text == null){
			text = "";
		}
		return text;
	}

	public static Node[] getElementsByTagName(Node ele, String tagName){
		if(!(ele instanceof Element)){
			return null;
		}
		NodeList all = ((Element) ele).getElementsByTagName(tagName);

		int len = all.getLength();
		Node[] rs = new Node[len];
		for(int i = 0; i < len; i++){
			rs[i] = all.item(i);
		}

		return rs;
	}

	public static Node[] getSonElementsByTagName(Node ele, String tagName){
		if(!(ele instanceof Element)){
			return null;
		}
		NodeList all = ((Element) ele).getElementsByTagName(tagName);

		int len = all.getLength();
		ArrayList list = new ArrayList(len);
		Node node;
		for(int i = 0; i < len; i++){
			node = all.item(i);
			if(node.getParentNode() == ele){
				list.add(node);
			}
		}

		Node[] rs = new Node[list.size()];
		list.toArray(rs);

		return rs;
	}

	public static String getNodeText(Node node){
		if(node == null){
			return null;
		}
		try{
		     return node.getTextContent();
		}catch(Throwable ex){
			Node temp = node.getFirstChild();
			if(temp == null){
				return null;
			}
			return temp.getNodeValue();
		}
	
	}
}
