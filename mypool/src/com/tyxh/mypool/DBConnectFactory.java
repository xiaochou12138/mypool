package com.tyxh.mypool;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;
import java.util.Vector;

public class DBConnectFactory {
	private static DBConnectFactory factory=null;
	private static final int INIT_SIZE=2;
	private static final int MAX_SIZE=10;
	private String driver;
	private String url;
	private String username;
	private String password;
	private long activeTime=5000;
	private Vector<DBConnection>connectpool=null;
	public DBConnectFactory(){
		//初始化连接的配置
		this.initProperties();
		//初始化数据库的连接池
		this.initPool();
		
	}
	private void initProperties(){
		Properties dbPro = new Properties();
		InputStream input = this.getClass().getResourceAsStream("db.properties");
		try{
			dbPro.load(input);
			this.driver = dbPro.getProperty("driver");
			this.url = dbPro.getProperty("url");
			this.username = dbPro.getProperty("username");
			this.password = dbPro.getProperty("password");
		}catch (IOException e) {
			e.printStackTrace();
			
		}
	}
	private void initPool() {
		if(null == connectpool) {
			//创建数据库连接池
			connectpool = new Vector<DBConnection>(INIT_SIZE);
			//循环创建数据库连接
			for (int i = 0; i < INIT_SIZE; i++){
				DBConnection db = new DBConnection(driver, url, username, password);
				System.out.println("创建了DBConnection连接");
				connectpool.add(db);
			}
		}
	}
	public static synchronized DBConnectFactory getDBConnectFactory(){
		if(null == factory){
			factory = new DBConnectFactory();
			
		}
		return factory;
		
	}
	public DBConnection createNewConectionTimer(){
		synchronized (connectpool){
			DBConnection db = new DBConnectionTimer(driver, url, username, password, activeTime);
			System.out.println("创建了DBConnectionTimer连接");
			connectpool.add(db);
			return db;
		}
		

	}
	@SuppressWarnings("static-access")
	public Connection getConnection() {
		System.out.println("此时连接池中还有的连接数： " + connectpool.size());
		synchronized (connectpool) {
			Connection conn = null;
			DBConnection db = null;
			while(true){
				for (int i = 0; i < connectpool.size(); i++) {
					db = connectpool.get(i);
					if(!db.isUsed()) {
						System.out.println("有空闲的连接");
						if(db instanceof DBConnectionTimer){
							System.out.println("取得的链接是DBConnectionTimer");
							DBConnectionTimer dbTimer = (DBConnectionTimer)db;
							dbTimer.cacel(); 
							conn = db.getConn();
							db.setUsed(true); //设置此链接繁忙状态
							return conn;
						} else {
							System.out.println("取得的连接是DBConnection");
							conn = db.getConn();
							db.setUsed(true); //设置此链接繁忙状态
							return conn;
						}
					}
				}
				System.out.println("没有空闲的连接");
				if(null == conn && connectpool.size() < this.MAX_SIZE) {
					db = this.createNewConectionTimer();
					conn = db.getConn();
					db.setUsed(false);//新创建的连接设置为空闲状态
					return conn;
				}
				if(null == conn && connectpool.size() == this.MAX_SIZE){
					System.out.println("连接池满了");	
					try {
						connectpool.wait();
					}catch (InterruptedException e){
						e.printStackTrace();
					}
				}
				
			}
			
		}
	}
	public void releaseConnection(Connection conn){
		synchronized (connectpool){
			for (int i = 0; i < connectpool.size(); i++){
				DBConnection db = connectpool.get(i);
				if(db instanceof DBConnectionTimer){
					DBConnectionTimer dbTimer = (DBConnectionTimer) db;
					DBConTimerTask task = new DBConTimerTask(connectpool, dbTimer);
					dbTimer.tick(task);
					System.out.println("释放了DBConnectionTimer的对象");
					
				}else {
					if(conn == db.getConn()){
						db.setUsed(false);
						connectpool.notify();
						System.out.println("释放了DBConnection的对象");
						break;
					}
					
				}
			}
			
		}
		
	}
	
}
