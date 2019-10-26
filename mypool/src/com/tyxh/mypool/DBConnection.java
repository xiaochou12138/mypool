package com.tyxh.mypool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	private String driver;
	private String username;
	private String url;
	private String password;
	private boolean isUsed;
	private Connection conn;
	public DBConnection(String driver,String url,String username,String password){
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
		this.isUsed = false;
		this.createConnection();
	}
	private void createConnection() {
		try {
			Class.forName(this.driver);
			conn = DriverManager.getConnection(url, username, password);
		}catch (ClassNotFoundException e){
			e.printStackTrace();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public String getDriver() {
		return driver;
		}
	public void setDriver(String driver){
		this.driver = driver;
	}
	public String getUrl(){
		return url;
	}
	public void setUrl(String url){
		this.url = url;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username){
		this.username = username;
	}
	public String getPassword(){
		return password;
	}
	public void setPassword(String password){
		this.password = password;
	}
	public boolean isUsed(){
		return isUsed;
	}
	public void setUsed(boolean isUsed) {
		this.isUsed = isUsed;
	}
	public Connection getConn(){
		return conn;
	}
	public void setConn(Connection conn){
		this.conn = conn;
	}
}
