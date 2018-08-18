package com.zlx.demo.Mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.DruidDataSourceUtils;

/**
 * petadata数据库操作类
 * 
 * @author jiaduo
 *
 */
public class DataSourceService {

	private DataSource dataSource;

	/**
	 * 关闭连接和数据集
	 * 
	 * @param stmt
	 * @param rs
	 * @param conn
	 */
	private void close(PreparedStatement stmt, ResultSet rs, Connection conn) {

		if (null != rs) {
			try {
				rs.close();
			} catch (Exception e) {

			}
		}

		if (null != stmt) {
			try {
				stmt.close();
			} catch (Exception e) {

			}
		}

		if (null != conn) {
			try {
				conn.close();
			} catch (Exception e) {

			}
		}
	}

	public void selectData(String sqlCmd, ProcessCallback callBack) throws SQLException {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {

			conn = dataSource.getConnection();
			stmt = conn.prepareStatement(sqlCmd);
			rs = stmt.executeQuery();

			// 执行回调
			callBack.doAction(rs);

		} finally {
			close(stmt, rs, conn);

		}
	}

	public void selectDataByStream(String sqlCmd, ProcessCallback callBack) throws SQLException {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {

			conn = dataSource.getConnection();
			stmt = conn.prepareStatement(sqlCmd, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(Integer.MIN_VALUE);
			rs = stmt.executeQuery();
			
			// 执行回调
			callBack.doAction(rs);

		} finally {
			close(stmt, rs, conn);

		}
	}

	/**
	 * 初始化后进行必选项校验
	 */
	public void init(String url, String userName, String passwd) throws Exception {

		// (1) 创建数据源
		DruidDataSource datasource = new DruidDataSource();
		datasource.setDriverClassName("com.mysql.jdbc.Driver");
		datasource.setUrl(url);
		datasource.setUsername("root");
		datasource.setPassword("123456");
		datasource.setMaxWait(10000);
		datasource.setMaxActive(100);
		datasource.setInitialSize(2);
		datasource.setMinIdle(0);
		datasource.setValidationQuery("select 1");
		datasource.setTimeBetweenEvictionRunsMillis(300000);
		datasource.setTestOnBorrow(false);
		datasource.setTestWhileIdle(true);

		try {
			datasource.setFilters("stat");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			datasource.init();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.dataSource = datasource;
		if (null == dataSource) {
			throw new RuntimeException("datasource 不能为空");

		}
	}

	public void initData(final int start, final int rowNum) {

		ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 5, 10, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());

		for (int i = start; i < rowNum + start; ++i) {

			final int ii = i;
			executor.execute(new Runnable() {

				public void run() {
					Connection conn = null;
					PreparedStatement stmt = null;
					ResultSet rs = null;

					try {

						try {
							conn = dataSource.getConnection();

							String sql = "insert into device(device_id) values('device" + ii + "')";
							stmt = conn.prepareStatement(sql);
							stmt.execute();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					} finally {
						close(stmt, rs, conn);

					}
				}
			});
		}

		executor.shutdown();
		try {
			executor.awaitTermination(60, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
