package siso.edu.cn;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.sql.*;
import java.sql.Connection;
import java.util.Timer;
import java.util.TimerTask;

public class DaemonMain {
    private static final Timer locationTimer = new Timer();

    public static void main(String[] args) {

        locationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("===Check Device Location===");
                try {
                    LocationChecking.Checking();
                } catch (ClassNotFoundException | SQLException | IOException e) {
                    e.printStackTrace();
                }
            }
        }, 5 * 60 * 1000, 1 * 60 * 1000);

    }

}
