package com.voila.forge;

public class EvaluateThread extends Thread {
	@SuppressWarnings("all")
	@Override
	public void run(){
		while(true){
			synchronized(this){
				try{
					wait();
				}catch(InterruptedException ignore){
				}
			}
			break_here_thread_only();
		}
	}

	private void break_here_thread_only(){
	}
}
