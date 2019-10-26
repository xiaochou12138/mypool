package com.tyxh.mypool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MulTest extends Thread {
	public void run() {
		DBConnectFactory factory = DBConnectFactory.getDBConnectFactory();
		Connection conn = factory.getConnection();
		try {
			Statement state = conn.createStatement();
			String sql = "select count(*) from employees";
			ResultSet rs = state.executeQuery(sql);
			while(rs.next()){
				System.out.println(rs.getInt(1));
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}finally {
			factory.releaseConnection(conn);
		}
	}
	

}
