package siso.edu.cn;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.sql.*;
import java.sql.Connection;

public class LocationChecking {

    private static final OkHttpClient client = new OkHttpClient();

    public static void Checking() throws ClassNotFoundException, SQLException, IOException {
        // 注册JDBC驱动
        Class.forName(GlobalSetting.JDBC_DRIVER);
        // 打开数据库链接
        Connection connection = DriverManager.getConnection(GlobalSetting.DB_URL, GlobalSetting.DB_USER, GlobalSetting.DB_PASSWORD);
        // 实例化Statement对象
        Statement statement = connection.createStatement();
        // 获取查询的结果
        ResultSet resultSet = statement.executeQuery(GlobalSetting.CHECKING_SQL);

        while (resultSet.next()) {
            System.out.println(String.format("ID = %d is checking location", resultSet.getInt("id")));

            // 构建提交数据的Body
            RequestBody body = new FormBody.Builder().add("id", String.valueOf(resultSet.getInt("id"))).build();
            // 构建提交HTTP PUT的请求
            Request request = new Request.Builder().url(GlobalSetting.SERVE_URL).put(body).build();

            // 提交HTTP请求
            Response response = client.newCall(request).execute();
            JSONObject object = JSON.parseObject(response.body().string());
            if (object.getIntValue("code") == 0) {
                System.out.println("ID = " + object.getJSONObject("data").getIntValue("id") + " Update Location OK");
            } else {
                System.out.println("ID = " + resultSet.getInt("id") + " Update Location Fail");
            }
        }

        // 关闭数据集
        resultSet.close();
        // 关闭数据查询
        statement.close();
        // 关闭数据链接
        connection.close();
    }

}
