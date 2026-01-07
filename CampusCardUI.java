import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

public class CampusCardUI extends JFrame {

    private JTextArea sqlArea;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    public CampusCardUI() {
        setTitle("校园一卡通管理系统 (数据库大作业演示)");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- 1. 顶部：SQL 监视器 (显示生成的 SQL) ---
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("SQL 执行监视器 (只读/手动)"));

        sqlArea = new JTextArea(5, 60);
        sqlArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        sqlArea.setText("-- 此处将显示系统自动生成的 SQL 语句，也可以手动输入执行");

        JButton runBtn = new JButton("手动执行 SQL");
        runBtn.setBackground(new Color(70, 130, 180));
        runBtn.setForeground(Color.WHITE);
        runBtn.addActionListener(e -> executeCustomSQL());

        topPanel.add(new JScrollPane(sqlArea), BorderLayout.CENTER);
        topPanel.add(runBtn, BorderLayout.EAST);

        // --- 2. 左侧：基础表查看 ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createTitledBorder("基础数据浏览"));
        leftPanel.setPreferredSize(new Dimension(160, 0));

        String[] tables = {"users", "card", "merchant", "consumption", "recharge"};
        String[] tableLabels = {"用户表", "一卡通表", "商户表", "消费记录", "充值记录"};

        for (int i = 0; i < tables.length; i++) {
            String tableName = tables[i];
            String label = tableLabels[i];
            JButton btn = new JButton(label);
            btn.setToolTipText("查看 " + tableName + " 表所有数据");
            btn.setMaximumSize(new Dimension(140, 30));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.addActionListener(e -> {
                String sql = "SELECT * FROM campus_card." + tableName + " ORDER BY 1 LIMIT 100;";
                sqlArea.setText(sql);
                runQuery(sql);
            });
            leftPanel.add(btn);
            leftPanel.add(Box.createVerticalStrut(10));
        }

        // --- 3. 右侧：功能业务模块 (核心修改部分) ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        rightPanel.setPreferredSize(new Dimension(260, 0));

        // 模块A: 查询统计
        JPanel queryPanel = createModulePanel("查询与统计", rightPanel);
        addButton(queryPanel, "查询某用户卡片及余额", e -> promptQueryUserBalance());
        addButton(queryPanel, "查询某卡消费/充值流水", e -> promptQueryCardFlow());
        addButton(queryPanel, "查询所有营业商户", e -> {
            String sql = "SELECT * FROM campus_card.merchant WHERE business_status = '营业'";
            sqlArea.setText(sql);
            runQuery(sql);
        });

        // 模块B: 业务办理 (事务)
        JPanel transPanel = createModulePanel("业务办理 (事务演示)", rightPanel);
        addButton(transPanel, "[模拟] 一卡通充值", e -> promptRecharge());
        addButton(transPanel, "[模拟] 刷卡消费", e -> promptConsumption());

        // 模块C: 管理功能
        JPanel adminPanel = createModulePanel("商户管理", rightPanel);
        addButton(adminPanel, "设置商户 停业/营业", e -> promptUpdateMerchantStatus());
        addButton(adminPanel, "大额消费预警 (>平均值)", e -> {
            String sql = "SELECT * FROM campus_card.consumption WHERE consumption_amount > " +
                    "(SELECT AVG(consumption_amount) FROM campus_card.consumption)";
            sqlArea.setText(sql);
            runQuery(sql);
        });

        // --- 4. 中间：结果表格 ---
        tableModel = new DefaultTableModel();
        resultTable = new JTable(tableModel);
        resultTable.setRowHeight(24);
        resultTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(resultTable);

        // --- 5. 底部：状态栏 ---
        statusLabel = new JLabel(" 系统就绪 | 数据库: campus_card | 模式: 演示模式");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());

        // 组装界面
        add(topPanel, BorderLayout.NORTH);
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    // --- 辅助方法：创建右侧功能模块 ---
    private JPanel createModulePanel(String title, JPanel parent) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 5, 5)); // 垂直网格布局
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setMaximumSize(new Dimension(250, 200));
        parent.add(panel);
        parent.add(Box.createVerticalStrut(10));
        return panel;
    }

    private void addButton(JPanel panel, String text, ActionListener l) {
        JButton btn = new JButton(text);
        btn.addActionListener(l);
        panel.add(btn);
    }

    // --- 业务逻辑具体实现 ---

    // 1. 查询用户余额 (多表连接)
    private void promptQueryUserBalance() {
        String name = JOptionPane.showInputDialog(this, "请输入用户姓名 (例如: 张三):");
        if (name != null && !name.trim().isEmpty()) {
            String sql = String.format(
                    "SELECT u.user_name, u.user_type, c.card_no, c.card_status, c.card_balance " +
                            "FROM campus_card.users u " +
                            "JOIN campus_card.card c ON u.user_id = c.user_id " +
                            "WHERE u.user_name = '%s';", name);
            sqlArea.setText(sql);
            runQuery(sql);
        }
    }

    private void promptQueryCardFlow() {
        JTextField cardField = new JTextField("CARD001");
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"消费流水", "充值流水"});
        JPanel p = new JPanel(new GridLayout(0, 1));
        p.add(new JLabel("卡号:"));
        p.add(cardField);
        p.add(new JLabel("类型:"));
        p.add(typeBox);
        int res = JOptionPane.showConfirmDialog(this, p, "查询卡流水", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String cardNo = cardField.getText().trim();
            String type = (String) typeBox.getSelectedItem();
            if ("消费流水".equals(type)) {
                String sql = "SELECT cons.consumption_id, m.merchant_name, cons.consumption_amount, cons.consumption_time " +
                        "FROM campus_card.consumption cons " +
                        "JOIN campus_card.merchant m ON cons.merchant_id = m.merchant_id " +
                        "WHERE cons.card_no = ? ORDER BY cons.consumption_time DESC";
                sqlArea.setText(sql.replace("?", "'" + cardNo + "'"));
                runQueryParams(sql, new Object[]{cardNo});
            } else {
                String sql = "SELECT r.recharge_id, r.recharge_amount, r.recharge_time " +
                        "FROM campus_card.recharge r " +
                        "WHERE r.card_no = ? ORDER BY r.recharge_time DESC";
                sqlArea.setText(sql.replace("?", "'" + cardNo + "'"));
                runQueryParams(sql, new Object[]{cardNo});
            }
        }
    }

    // 3. 模拟充值 (事务：插入记录 + 更新余额)
    private void promptRecharge() {
        String cardNo = JOptionPane.showInputDialog(this, "请输入充值卡号:", "CARD001");
        String amountStr = JOptionPane.showInputDialog(this, "请输入充值金额:", "100");

        if (cardNo != null && amountStr != null) {
            // 构造事务 SQL 块
            String sql = String.format(
                    "START TRANSACTION;\n" +
                            "INSERT INTO campus_card.recharge(card_no, recharge_amount, recharge_method) VALUES ('%s', %s, '线下');\n" +
                            "UPDATE campus_card.card SET card_balance = card_balance + %s WHERE card_no = '%s';\n" +
                            "COMMIT;",
                    cardNo, amountStr, amountStr, cardNo);

            sqlArea.setText(sql);
            // 这里因为涉及多条语句，我们执行完后再查一次余额
            String result = DBUtil.executeUpdate(sql);
            JOptionPane.showMessageDialog(this, result);
            if (result.contains("成功")) {
                runQuery("SELECT * FROM campus_card.card WHERE card_no = '" + cardNo + "'");
            }
        }
    }

    // 4. 模拟消费 (事务：插入记录 + 扣减余额)
    private void promptConsumption() {
        JTextField cardField = new JTextField("CARD001");
        JTextField merchField = new JTextField("1"); // 假设商户ID为1
        JTextField amountField = new JTextField("15.00");

        JPanel inputPanel = new JPanel(new GridLayout(0, 1));
        inputPanel.add(new JLabel("卡号:"));
        inputPanel.add(cardField);
        inputPanel.add(new JLabel("商户ID:"));
        inputPanel.add(merchField);
        inputPanel.add(new JLabel("消费金额:"));
        inputPanel.add(amountField);

        int res = JOptionPane.showConfirmDialog(this, inputPanel, "模拟刷卡消费", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String sql = String.format(
                    "START TRANSACTION;\n" +
                            "INSERT INTO campus_card.consumption(card_no, merchant_id, consumption_amount) VALUES ('%s', %s, %s);\n" +
                            "UPDATE campus_card.card SET card_balance = card_balance - %s WHERE card_no = '%s';\n" +
                            "COMMIT;",
                    cardField.getText(), merchField.getText(), amountField.getText(), amountField.getText(), cardField.getText()
            );
            sqlArea.setText(sql);
            String result = DBUtil.executeUpdate(sql);

            if (result.contains("失败")) {
                JOptionPane.showMessageDialog(this, result + "\n可能原因: 余额不足或违反约束");
            } else {
                JOptionPane.showMessageDialog(this, "消费成功！");
                runQuery("SELECT * FROM campus_card.consumption ORDER BY consumption_id DESC LIMIT 5");
            }
        }
    }

    // 5. 更新商户状态
    private void promptUpdateMerchantStatus() {
        String mId = JOptionPane.showInputDialog(this, "输入商户ID:", "1");
        if (mId == null) return;

        Object[] options = {"营业", "停业"};
        int choice = JOptionPane.showOptionDialog(this, "设置状态为:", "商户管理",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        String newStatus = (choice == 0) ? "营业" : "停业";
        String sql = String.format("UPDATE campus_card.merchant SET business_status = '%s' WHERE merchant_id = %s;", newStatus, mId);

        sqlArea.setText(sql);
        String msg = DBUtil.executeUpdate(sql);
        JOptionPane.showMessageDialog(this, msg);
        runQuery("SELECT * FROM campus_card.merchant WHERE merchant_id = " + mId);
    }

    // --- 核心执行方法 ---

    private void runQuery(String sql) {
        statusLabel.setText(" 正在查询...");
        new Thread(() -> {
            DBUtil.QueryResult res = DBUtil.executeQuery(sql);
            SwingUtilities.invokeLater(() -> {
                if (res.errorMsg != null) {
                    statusLabel.setText(" 查询出错");
                    JOptionPane.showMessageDialog(this, res.errorMsg, "Database Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    tableModel.setDataVector(res.data, res.columnNames);
                    statusLabel.setText(" 查询完成 | 记录数: " + res.data.size());
                }
            });
        }).start();
    }

    private void executeCustomSQL() {
        String sql = sqlArea.getText().trim();
        if (sql.length() == 0) return;

        if (sql.toUpperCase().startsWith("SELECT") || sql.toUpperCase().startsWith("WITH")) {
            runQuery(sql);
        } else {
            String msg = DBUtil.executeUpdate(sql);
            statusLabel.setText(" " + msg);
            JOptionPane.showMessageDialog(this, msg);
        }
    }

    private void runQueryParams(String sql, Object[] params) {
        statusLabel.setText(" 正在查询...");
        new Thread(() -> {
            DBUtil.QueryResult res = DBUtil.executeQueryParams(sql, params);
            SwingUtilities.invokeLater(() -> {
                if (res.errorMsg != null) {
                    statusLabel.setText(" 查询出错");
                    JOptionPane.showMessageDialog(this, res.errorMsg, "Database Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    tableModel.setDataVector(res.data, res.columnNames);
                    statusLabel.setText(" 查询完成 | 记录数: " + res.data.size());
                }
            });
        }).start();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new CampusCardUI().setVisible(true));
    }
}
