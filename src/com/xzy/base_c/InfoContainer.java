/**
 * ¾øÃÜ Created on 2007-10-25 by edmund
 */
package com.xzy.base_c;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class InfoContainer implements Serializable{
	private HashMap infos = null;

	public InfoContainer(){
		this.infos = new HashMap();
	}

	public InfoContainer setInfo(Object key, Object info){
		this.infos.put(key, info);
		return this;
	}
	
	public Object removeInfo(Object key){
		return this.infos.remove(key);
	}

	public Object getInfo(Object key){
		return this.infos.get(key);
	}
	
	public Object[] getAllKey(){
		return this.infos.keySet().toArray();
	}
	
	public Iterator keys(){
		return this.infos.keySet().iterator();
	}
	
	public Date getDate(Object key)
	{
		Object obj = this.getInfo(key);
		if(obj instanceof Date){
			return (Date)obj;
		}
		return null;
	}

	public String getString(Object key){
		Object obj = this.getInfo(key);
		if (obj == null){
			return null;
		} else{
			return obj.toString();
		}
	}

	public Integer getInteger(Object key){
		Object obj = this.getInfo(key);
		if (obj == null){
			return null;
		} else if (obj instanceof Integer){
			return (Integer) obj;
		} else{
			try{
				String str = obj.toString().trim();
				if(str.length() == 0){
					return null;
				}
				if(str.startsWith("0x")){
					return new Integer(Integer.parseInt(str.substring(2),16));
				}else{
					return new Integer(str);
				}
			} catch (Exception e){
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public Boolean getBoolean(Object key){
		Object obj = this.getInfo(key);
		if (obj == null){
			return null;
		} else if(obj instanceof Boolean){
			 return (Boolean)obj;
		}else{
			return new Boolean(obj.toString().equalsIgnoreCase("true"));
		}
	}
	
	public Double getDouble(Object key){
		Object obj = this.getInfo(key);
		if (obj == null){
			return null;
		} else if (obj instanceof Double){
			return (Double) obj;
		} else{
			try{
				return new Double(obj.toString());
			} catch (Exception e){
				e.printStackTrace();
				return null;
			}
		}
	}
	public Float getFloat(Object key){
		Object obj = this.getInfo(key);
		if (obj == null){
			return null;
		} else if (obj instanceof Float){
			return (Float) obj;
		} else{
			try{
				return new Float(obj.toString());
			} catch (Exception e){
				e.printStackTrace();
				return null;
			}
		}
	}

	public Long getLong(Object key){
		Object obj = this.getInfo(key);
		if (obj == null){
			return null;
		} else if (obj instanceof Long){
			return (Long) obj;
		} else{
			try{
				return new Long(obj.toString());
			} catch (Exception e){
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public void addAll(InfoContainer info){
		this.infos.putAll(info.infos);
	}
	
	public InfoContainer cloneObj(){
		InfoContainer newObj = new InfoContainer();
		
		newObj.infos.putAll(this.infos);
		
		return newObj;
	}
	
	public String toString(){
		Object key,val;
		StringBuffer buff = new StringBuffer(256);
		buff.append("[info]:");
		for(Iterator itr = this.infos.keySet().iterator();itr.hasNext();){
			key = itr.next();
			val = this.infos.get(key);
			buff.append(key+"="+val+"\r\n");
		}
		return buff.toString();
	}
	
	public boolean equals(Object _info){
		if(!(_info instanceof InfoContainer)){
			return false;
		}
		InfoContainer info = (InfoContainer)_info;
		
		if(this.infos.size() != info.infos.size()){
			return false;
		}
		Object key,val1,val2;
		for(Iterator itr = this.infos.keySet().iterator();itr.hasNext();){
			key = itr.next();
			val1 = this.infos.get(key);
			val2 = info.infos.get(key);
			if(val1 == null && val2 == null){
				
			}else if(val1 == null || val2 == null){
				return false;
			}else{
				if(val1 instanceof byte[] && val2 instanceof byte[]){
					byte[] a = (byte[])val1,b = (byte[])val2;
					if(a.length != b.length){
						return false;
					}
					for(int i=0;i<a.length;i++){
						if(a[i] != b[i]){
							return false;
						}
					}
				}else{
					if(!val1.equals(val2)){
						System.out.println(key+"  "+val1+"  "+val2+" "+val1.getClass().getName()+"  "+val2.getClass().getName());
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public String contain(InfoContainer info){
		Object key,val1,val2;
		for(Iterator itr = this.infos.keySet().iterator();itr.hasNext();){
			key = itr.next();
			val1 = this.infos.get(key);
			val2 = info.infos.get(key);
			if(val1 == null && val2 == null){
				
			}else if(val1 == null || val2 == null){
				return key+"="+val1+";"+val2;
			}else{
				if(val1 instanceof byte[] && val2 instanceof byte[]){
					byte[] a = (byte[])val1,b = (byte[])val2;
					if(a.length != b.length){
						return key+"=byte["+a.length+"];byte["+b.length+"]";
					}
					for(int i=0;i<a.length;i++){
						if(a[i] != b[i]){
							return key+"=byte["+a.length+"]["+i+"] "+(a[i]&0xFF)+";"+(b[i]&0xFF);
						}
					}
				}else if(val1 instanceof InfoContainer && val2 instanceof InfoContainer){
					return ((InfoContainer)val1).contain((InfoContainer)val2);
				}else{
					if(!val1.equals(val2)){
						return key+"="+val1+";"+val2;
					}
				}
			}
		}
		return null;
	}
}
