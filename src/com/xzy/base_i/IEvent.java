package com.xzy.base_i;

import org.json.JSONObject;

/**
 * �¼��������ڴ���ϵͳ��Ϣ
 * @author edmund
 *
 */
public interface IEvent {
	/**
	 * �õ��¼���������
	 * @return ʱ�䷢������
	 */
	public Object getSource();
	
	/**
	 * �õ��¼�������Ϣ
	 * @return �¼����Ͷ���
	 */
	public Object getEventType();
	
	/**
	 * �õ��¼�����
	 * @return �¼�������JSON���������¼�����
	 */
	public JSONObject getEventPara();
}
