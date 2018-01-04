
package com.xiaomi.fastvideo;

import android.os.ConditionVariable;

import java.lang.ref.WeakReference;

public abstract class WorkThread extends Thread {
	protected volatile boolean mIsRunning = false;

	ConditionVariable mConditionVariable = new ConditionVariable();

	public WorkThread(String name) {
		super(name);
		setPriority(MIN_PRIORITY);
	}

	@Override
	public void run() {
		mConditionVariable.close();
		doInitial();
		while (mIsRunning) {
			try {
				doRepeatWork();
			} catch (IllegalStateException exception) {
				exception.printStackTrace();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		doRelease();
		mConditionVariable.open();
	}

	protected abstract int doRepeatWork() throws InterruptedException;

	protected abstract void doInitial();

	protected abstract void doRelease();

	@Override
	public synchronized void start() {
		if (mIsRunning)
			return;
		mIsRunning = true;
		super.start();
	}

	public synchronized void stopThreadAsyn() {
		mIsRunning = false;
		interrupt();
	}

	public synchronized void stopThreadSyn() {
		if (!mIsRunning)
			return;
		mIsRunning = false;
		mConditionVariable.block(2000);
	}

	public synchronized boolean isRunning() {
		return mIsRunning;
	}


	public interface  HardDecodeExceptionCallback{
		void onHardDecodeException(Exception e);
		void onOtherException(Throwable e);
	}
}
