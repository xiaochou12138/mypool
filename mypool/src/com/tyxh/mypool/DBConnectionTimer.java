package com.tyxh.mypool;

import java.util.Timer;

public class DBConnectionTimer extends DBConnection {
	private long activeTime;
	private Timer timer;
	public DBConnectionTimer(String driver, String url, String username,String password, long activeTime){
		super(driver, url, username, password);
		this.activeTime = activeTime;
		timer = new Timer();
	}

	public void tick(DBConTimerTask task) {
		try{
			this.timer.schedule(task, activeTime);
			System.out.println("定时开始");
		}catch(IllegalStateException e) {
			System.err.println("已经存在task了");
		}
		
	}

	public void cacel() {
		this.timer.cancel();
		System.out.println("取消定时");
		
	}

}
