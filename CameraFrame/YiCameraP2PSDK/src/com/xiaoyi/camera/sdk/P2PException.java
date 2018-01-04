package com.xiaoyi.camera.sdk;

public class P2PException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 403691263434338662L;
	int mError;

	public P2PException(int error, String detailMessage) {
		super(detailMessage);
		mError = error;
	}

//	public P2PException(int error) {
//		super(P2PUtils.getError(error));
//		mError = error;
//	}

	public int getError() {
		return mError;
	}
}
