package com.xzy.base.server.pool;

public interface ITask extends Runnable{
	/**
	 * �õ������flag�����ڶ����������Ч
	 * @return ��������ʶ
	 */
	public Object getFlag();
	
	/**
	 * �õ����������
	 * @return ��������
	 */
	public String getName();
	
	/**
	 * ȡ�������ִ��
	 */
	public void cancel();
	
	/**
	 * �����Ƿ��Ѿ���ȡ��
	 * @return true�����Ѿ���ȡ������֮��ִ��
	 */
	public boolean isCancel();
}
