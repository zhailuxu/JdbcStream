package com.zlx.demo.Mysql;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * 创建一个数据库users，里面创建一个表device,device表字段为：
 * id int;device_id varchar
 *
 */
public class OrgMysqlSearch {
	private static final String  url = "jdbc:mysql://127.0.0.1:3306/users";
	private static final String   userName = "root";
	private static final String   pwd = "123456";
	public static void main(String[] args) throws Exception {

		
        //1创建对象
		DataSourceService dataSourceService = new DataSourceService();
		dataSourceService.init(url, userName, pwd);
		
		//2初始化数据库表数据
		dataSourceService.initData(1,100000);
        
		//3迭代数据,selectDataByStream是流式，selectData是普通
		dataSourceService.selectDataByStream("select * from device", new ProcessCallback() {
			
			public void doAction(ResultSet rs) {
				try {
					while(rs.next()) {
						System.out.println(rs.getString("device_id"));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
		});
		
	}
}
