package com.tyxh.mypool;

import java.sql.SQLException;
import java.util.TimerTask;
import java.util.Vector;

public class DBConTimerTask extends TimerTask {
	private Vector<DBConnection> connectPool = null;
	private DBConnectionTimer dbTimer;
	public DBConTimerTask(Vector<DBConnection> connectPool,DBConnectionTimer dbTimer) {
		super();
		this.connectPool = connectPool;
		this.dbTimer = dbTimer;
	}
	@Override
	public void run() {
		try {
			dbTimer.getConn().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		connectPool.remove(dbTimer);
		System.out.println("移除超出生命周期的数据库连接！");
	}

}
