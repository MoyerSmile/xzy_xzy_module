package com.xzy.base_i;

public interface IThread {
	/**
	 * �õ��߳�������������ջ
	 * @return һ���쳣�Ķ�ջ
	 */
	public Exception getCreateStack();
	/**
	 * �õ��̵߳�����ʱ��
	 * @return ����ʱ��
	 */
	public long getStartTime();
	
	public String getName();
}
