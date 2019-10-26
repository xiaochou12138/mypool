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
		//��ʼ�����ӵ�����
		this.initProperties();
		//��ʼ�����ݿ�����ӳ�
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
			//�������ݿ����ӳ�
			connectpool = new Vector<DBConnection>(INIT_SIZE);
			//ѭ���������ݿ�����
			for (int i = 0; i < INIT_SIZE; i++){
				DBConnection db = new DBConnection(driver, url, username, password);
				System.out.println("������DBConnection����");
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
			System.out.println("������DBConnectionTimer����");
			connectpool.add(db);
			return db;
		}
		

	}
	@SuppressWarnings("static-access")
	public Connection getConnection() {
		System.out.println("��ʱ���ӳ��л��е��������� " + connectpool.size());
		synchronized (connectpool) {
			Connection conn = null;
			DBConnection db = null;
			while(true){
				for (int i = 0; i < connectpool.size(); i++) {
					db = connectpool.get(i);
					if(!db.isUsed()) {
						System.out.println("�п��е�����");
						if(db instanceof DBConnectionTimer){
							System.out.println("ȡ�õ�������DBConnectionTimer");
							DBConnectionTimer dbTimer = (DBConnectionTimer)db;
							dbTimer.cacel(); 
							conn = db.getConn();
							db.setUsed(true); //���ô����ӷ�æ״̬
							return conn;
						} else {
							System.out.println("ȡ�õ�������DBConnection");
							conn = db.getConn();
							db.setUsed(true); //���ô����ӷ�æ״̬
							return conn;
						}
					}
				}
				System.out.println("û�п��е�����");
				if(null == conn && connectpool.size() < this.MAX_SIZE) {
					db = this.createNewConectionTimer();
					conn = db.getConn();
					db.setUsed(false);//�´�������������Ϊ����״̬
					return conn;
				}
				if(null == conn && connectpool.size() == this.MAX_SIZE){
					System.out.println("���ӳ�����");	
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
					System.out.println("�ͷ���DBConnectionTimer�Ķ���");
					
				}else {
					if(conn == db.getConn()){
						db.setUsed(false);
						connectpool.notify();
						System.out.println("�ͷ���DBConnection�Ķ���");
						break;
					}
					
				}
			}
			
		}
		
	}
	
}
