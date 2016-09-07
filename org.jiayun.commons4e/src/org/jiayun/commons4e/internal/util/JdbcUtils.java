package org.jiayun.commons4e.internal.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public abstract class JdbcUtils {

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Throwable e) {}
	}

	public static Connection getConnection(String jdbcUrl, String username, String password) {
		try {
			return DriverManager.getConnection(
					"jdbc:mysql://" + jdbcUrl + "/mysql?useUnicode=true&amp;characterEncoding=UTF-8", username,
					password);
		} catch (Exception e) {
			return null;
		}
	}

	public static void main(String[] args) {
		System.out.println(cols(getConnection("localhost", "root", "!Q@W#E4r5t6y"), "sjtu_propagandist.sites"));
	}

	public static Map<String, String> cols(Connection conn, String tableName) {
		Map<String, String> result = new HashMap<String, String>();
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			ResultSetMetaData metaData = (resultSet = (statement = conn.createStatement())
					.executeQuery("select * from " + tableName + " where 1=2")).getMetaData();
			int columnCount = metaData.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				String colName = metaData.getColumnLabel(i);
				int type = metaData.getColumnType(i);
				String typeName = jdbc2javaForType(type);

				if (result.containsKey(colName)) {
					colName = metaData.getTableName(i) + "_" + colName;
				}

				result.put(colName, typeName);
			}
			return result;
		} catch (SQLException e) {
			return result;
		} finally {
			jdbcClose(resultSet);
			jdbcClose(statement);
			jdbcClose(conn);
		}
	}

	private static void jdbcClose(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
	}

	private static void jdbcClose(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
			}
		}
	}

	private static void jdbcClose(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
			}
		}
	}

	private static String jdbc2javaForType(int type) {
		String typeName = null;
		switch (type) {
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
		case Types.NCHAR:
		case Types.NVARCHAR:
		case Types.LONGNVARCHAR:
			typeName = "String";
			break;
		case Types.INTEGER:
		case Types.SMALLINT:
		case Types.BIGINT:
		case Types.TINYINT:
			typeName = "Integer";
			break;
		case Types.NUMERIC:
		case Types.DECIMAL:
		case Types.DOUBLE:
		case Types.FLOAT:
			typeName = "Double";
			break;
		case Types.DATE:
		case Types.TIME:
		case Types.TIMESTAMP:
			typeName = "java.util.Date";
			break;
		case Types.BIT:
		case Types.BOOLEAN:
			typeName = "Boolean";
			break;
		default:
			typeName = "Object";
			break;
		}
		return typeName;
	}

}
