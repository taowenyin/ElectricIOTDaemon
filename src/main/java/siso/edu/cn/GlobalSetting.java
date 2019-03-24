package siso.edu.cn;

public class GlobalSetting {

    public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    public static final String DB_URL = "jdbc:mysql://localhost:3306/electric_iot?useSSL=true&serverTimezone=Asia/Shanghai&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8";

    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "05200902";

//    public static final String CHECKING_SQL = "SELECT * FROM electric_iot.device_location WHERE " +
//            "electric_iot.device_location.longitude IS NULL OR " +
//            "electric_iot.device_location.latitude IS NULL LIMIT 10";
    public static final String CHECKING_SQL = "SELECT * FROM electric_iot.device_location WHERE " +
            "(electric_iot.device_location.longitude IS NULL) OR " +
            "(electric_iot.device_location.latitude IS NULL) OR " +
            "(electric_iot.device_location.longitude IS NOT NULL AND " +
            "electric_iot.device_location.latitude IS NOT NULL AND " +
            "electric_iot.device_location.province IS NULL) LIMIT 10";

    public static final String SERVE_URL = "http://192.168.247.130:8080/ElectricIOTServer/api/manage/location";
}
