package com.xiaoyi.camera.sdk;

import java.io.Serializable;

public class YUVData implements Serializable {

	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;

	public YUVData() {

	}

	public YUVData(byte[] yuvbuf, int width, int height) {
		super();
		this.yuvbuf = yuvbuf;
		this.width = width;
		this.height = height;
	}

	public byte[] yuvbuf;

	public int width;

	public int height;

	public YUVData clone() {
		if(yuvbuf == null){
			return null;
		}
		
		YUVData yuvData = new YUVData();
		yuvData.width = width;
		yuvData.height = height;
		yuvData.yuvbuf = yuvbuf.clone();
		return yuvData;
	}

}