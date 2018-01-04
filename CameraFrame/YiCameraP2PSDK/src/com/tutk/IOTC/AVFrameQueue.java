package com.tutk.IOTC;

import java.util.LinkedList;

class AVFrameQueue {
	private volatile LinkedList<AVFrame> listData = new LinkedList();
	private volatile int mSize = 0;

	public void addLast(AVFrame avFrame) {

		this.listData.addLast(avFrame);
		// this.mSize = (1 + this.mSize);

	}

	public void addAudioFrame(AVFrame avFrame) {
		this.listData.addLast(avFrame);
		// this.mSize++;
	}

	public int getCount() {
		return listData.size();
	}

	public boolean isFirstIFrame() {
		try {
			boolean bool1 = false;
			if ((this.listData != null) && (!this.listData.isEmpty())) {
				boolean bool2 = ((AVFrame) this.listData.get(0)).isIFrame();
				if (bool2) {
					bool1 = true;
					return bool1;
				}
			}

		} finally {
		}
		return false;
	}

	public void removeAll() {
		try {
			if (!this.listData.isEmpty())
				this.listData.clear();
			// this.mSize = 0;
			return;
		} finally {
		}
	}

	public AVFrame removeHead() {
		AVFrame localAVFrame;
		if (listData.size() == 0) {
			localAVFrame = null;
		} else {
			localAVFrame = (AVFrame) this.listData.removeFirst();
		}
		return localAVFrame;
	}
}
