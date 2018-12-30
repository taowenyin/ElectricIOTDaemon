package siso.edu.cn;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.sql.*;
import java.sql.Connection;

public class LocationChecking {

    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/electric_iot?useSSL=true&serverTimezone=Asia/Shanghai";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "05200902";
    private static final String CHECKING_SQL = "SELECT * FROM electric_iot.device_location WHERE " +
            "electric_iot.device_location.longitude IS NULL OR " +
            "electric_iot.device_location.latitude IS NULL";

    private static final String SERVE_URL = "http://localhost:8080/api/manage/location";

    private static final OkHttpClient client = new OkHttpClient();

    public static void Checking() throws ClassNotFoundException, SQLException, IOException {
        // 注册JDBC驱动
        Class.forName(JDBC_DRIVER);
        // 打开数据库链接
        Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        // 实例化Statement对象
        Statement statement = connection.createStatement();
        // 获取查询的结果
        ResultSet resultSet = statement.executeQuery(CHECKING_SQL);

        while (resultSet.next()) {
            System.out.println(String.format("ID = %d is checking location", resultSet.getInt("id")));

            // 构建提交数据的Body
            RequestBody body = new FormBody.Builder().add("id", String.valueOf(resultSet.getInt("id"))).build();
            // 构建提交HTTP PUT的请求
            Request request = new Request.Builder().url(SERVE_URL).put(body).build();

            // 提交HTTP请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("===onFailure===");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    JSONObject object = JSON.parseObject(response.body().string());
                    if (object.getIntValue("code") == 0) {
                        System.out.println("ID = " + object.getJSONObject("data").getIntValue("id") + " Update Location OK");
                    } else {
                        System.out.println("ID = " + object.getJSONObject("data").getIntValue("id") + " Update Location Fail");
                    }
                }
            });
        }

        // 关闭数据集
        resultSet.close();
        // 关闭数据查询
        statement.close();
        // 关闭数据链接
        connection.close();
    }

}
