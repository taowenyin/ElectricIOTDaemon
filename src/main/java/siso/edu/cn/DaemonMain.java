package siso.edu.cn;

import java.io.IOException;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class DaemonMain {
    private static final Timer locationTimer = new Timer();

    public static void main(String[] args) {

        locationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    LocationChecking.Checking();
                } catch (ClassNotFoundException | SQLException | IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 5000);

    }

}
