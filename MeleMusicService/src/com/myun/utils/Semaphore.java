package com.myun.utils;

public class Semaphore {
	private boolean signal = true;
	
	
	public Semaphore() {
		super();
		signal = true;
	}

	public synchronized void  take() throws InterruptedException {

		while (!this.signal)
			wait();
		this.signal = false;

	}
	public synchronized void release() {

		this.signal = true;
		this.notify();

	}
	
}	
