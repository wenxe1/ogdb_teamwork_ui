import java.sql.*;
import java.util.Vector;

public class DBUtil {
    // --- 数据库连接配置 ---
    static final String JDBC_DRIVER = "org.postgresql.Driver";
    // 请确保 IP:端口 正确，且云服务器安全组已放行 26000
    static final String DB_URL = "jdbc:postgresql://120.46.20.99:26000/campus_card?ApplicationName=app1";
    static final String USER = "card_user";
    static final String PASS = "Gauss#3campus";

    static {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("错误：未找到 JDBC 驱动！请检查 postgresql.jar 是否在 classpath 中。");
        }
    }

    public static Connection getConnection() throws SQLException {
        DriverManager.setLoginTimeout(5); // 设置连接超时
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    /**
     * 执行查询 (SELECT)
     */
    public static QueryResult executeQuery(String sql) {
        Vector<String> columnNames = new Vector<>();
        Vector<Vector<Object>> data = new Vector<>();
        String errorMsg = null;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                data.add(row);
            }

        } catch (SQLException e) {
            errorMsg = "SQL错误: " + e.getMessage();
            e.printStackTrace();
        }

        return new QueryResult(columnNames, data, errorMsg);
    }

    /**
     * 执行更新 (INSERT, UPDATE, DELETE, TRANSACTION)
     */
    public static String executeUpdate(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 执行 SQL (支持多条语句，例如事务块)
            stmt.executeUpdate(sql);
            return "执行成功";

        } catch (SQLException e) {
            e.printStackTrace();
            return "执行失败: " + e.getMessage();
        }
    }

    public static class QueryResult {
        public Vector<String> columnNames;
        public Vector<Vector<Object>> data;
        public String errorMsg;

        public QueryResult(Vector<String> c, Vector<Vector<Object>> d, String e) {
            this.columnNames = c;
            this.data = d;
            this.errorMsg = e;
        }
    }
}