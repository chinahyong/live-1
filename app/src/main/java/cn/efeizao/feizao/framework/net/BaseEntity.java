package cn.efeizao.feizao.framework.net;

public abstract class BaseEntity extends AEntity {

	private String TAG = getClass().getName();

	/**
	 * 随机数
	 */
	protected long rnd = System.currentTimeMillis();

	public BaseEntity(IReceiverListener receiverListener) {
		super(receiverListener);
	}

	@Override
	protected void initHttpHeader() {
		// add something into the HTTP request head.
	}

	@Override
	public String getSendData() {
		return sendData;
	}

	@Override
	public void decodeReceiveData(String receiveData) {

		// TODO Auto-generated method stub

	}

	/**
	 * http post请求数据
	 */
	public void setSendData(String data) {
		this.sendData = data;
	}
}
