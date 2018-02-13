package com.xzy.base.server.pool;

import com.xzy.base_i.IEventListener;

public class ThreadPoolInfo {
	public static enum QUEUE_TYPE{
		SINGLE_QUEUE_POOL,
		MULTIPLE_QUEUE_POOL
	}
	public static enum THREAD_TYPE{
		STATIC_TYPE,
		DYNAMIC_TYPE
	}
	
	//�������ͣ������л��߶����
	private QUEUE_TYPE _queueType = QUEUE_TYPE.SINGLE_QUEUE_POOL;
	//�߳��������ͣ���̬�������ͺͶ�̬�������͡���������̬�����ͼ���
	private THREAD_TYPE _threadType = THREAD_TYPE.STATIC_TYPE;
	//��С������߳�����������Ǿ�̬�����̣߳�ֻʹ����С�߳�����
	private int minThreadNum,maxThreadNum;
	//���ѻ���������
	private int maxTaskNum = 0;
	//�߳����ȼ�
	private int priority;
	//�Ƿ��ػ��߳�
	private boolean isDaemo = true;
	//�̳߳ص��¼�����
	private IEventListener listener = null;
	
	public ThreadPoolInfo(int threadNum){
		this(threadNum,1000,QUEUE_TYPE.SINGLE_QUEUE_POOL,Thread.NORM_PRIORITY,true);
	}
	public ThreadPoolInfo(int threadNum,int maxTaskNum){
		this(threadNum,maxTaskNum,QUEUE_TYPE.SINGLE_QUEUE_POOL,Thread.NORM_PRIORITY,true);
	}
	public ThreadPoolInfo(int threadNum,int maxTaskNum,QUEUE_TYPE queueType){
		this(threadNum,maxTaskNum,queueType,Thread.NORM_PRIORITY,true);
	}

	public ThreadPoolInfo(int threadNum,int maxTaskNum,int priority){
		this(threadNum,maxTaskNum,QUEUE_TYPE.SINGLE_QUEUE_POOL,priority,true);
	}

	public ThreadPoolInfo(int threadNum,int maxTaskNum,QUEUE_TYPE queueType,int priority,boolean isDaemo){
		this.minThreadNum = threadNum;
		this.maxThreadNum = threadNum;
		this.maxTaskNum = maxTaskNum;
		this._queueType = queueType;
		this.priority = priority;
		this.isDaemo = isDaemo;
	}
	
	/**
	 * ����Ϊ��̬�߳�����ָ��Ŀ�������߳��������С����С�߳����������ʧ��
	 * @param maxThreadNum ����߳�����
	 * @return �ɹ���ʧ��
	 */
	public boolean update2DynamicThreadNum(int maxThreadNum){
		if(maxThreadNum <= this.minThreadNum){
			return false;
		}
		this.maxThreadNum = maxThreadNum;
		this._threadType = THREAD_TYPE.DYNAMIC_TYPE;
		return true;
	}
	
	public void setListener(IEventListener listener) {
		this.listener = listener;
	}
	public IEventListener getListener(){
		return this.listener;
	}
	
	public QUEUE_TYPE get_queueType() {
		return _queueType;
	}
	public THREAD_TYPE get_threadType() {
		return _threadType;
	}
	public int getMinThreadNum() {
		return minThreadNum;
	}
	public int getMaxThreadNum() {
		return maxThreadNum;
	}
	public int getMaxTaskNum() {
		return maxTaskNum;
	}
	public int getPriority() {
		return priority;
	}
	public boolean isDaemo() {
		return isDaemo;
	}
	
}
