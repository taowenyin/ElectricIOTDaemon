package siso.edu.cn;

public class GlobalSetting {

    public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    public static final String DB_URL = "jdbc:mysql://localhost:3306/electric_iot?useSSL=true&serverTimezone=Asia/Shanghai&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8";

    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "05200902";

    public static final String CHECKING_SQL = "SELECT * FROM electric_iot.device_location WHERE " +
            "(electric_iot.device_location.is_delete = 0) AND " +
            "((electric_iot.device_location.longitude IS NULL) OR " +
            "(electric_iot.device_location.latitude IS NULL) OR " +
            "(electric_iot.device_location.longitude IS NOT NULL AND " +
            "electric_iot.device_location.latitude IS NOT NULL AND " +
            "electric_iot.device_location.province IS NULL)) LIMIT 10";

    public static final String PREVIOUS_DATA_SQL = "SELECT * FROM electric_iot.device_location WHERE " +
            "electric_iot.device_location.is_delete != 1 AND " +
            "electric_iot.device_location.device_id = %d AND " +
            "electric_iot.device_location.id < %d " +
            "ORDER BY electric_iot.device_location.id DESC LIMIT 1";

    public static final String DELETE_DATA_SQL = "UPDATE electric_iot.device_location SET electric_iot.device_location.is_delete = 1 WHERE " +
            "electric_iot.device_location.id = %d";

    public static final String UPDATE_SQL = "UPDATE electric_iot.device_location " +
            "SET electric_iot.device_location.longitude = %f, " +
            "electric_iot.device_location.longitude_direction = %d, " +
            "electric_iot.device_location.latitude = %f, " +
            "electric_iot.device_location.latitude_direction = %d, " +
            "electric_iot.device_location.province = '%s', " +
            "electric_iot.device_location.city = '%s', " +
            "electric_iot.device_location.district = '%s' " +
            "WHERE electric_iot.device_location.id = %d";

    public static final String CLEAN_SQL = "UPDATE electric_iot.device_location " +
            "SET electric_iot.device_location.longitude = null, " +
            "electric_iot.device_location.longitude_direction = null, " +
            "electric_iot.device_location.latitude = null, " +
            "electric_iot.device_location.latitude_direction = null, " +
            "electric_iot.device_location.province = null, " +
            "electric_iot.device_location.city = null, " +
            "electric_iot.device_location.district = null " +
            "WHERE electric_iot.device_location.id = %d";

    public static final String UPDATE_LOCAL_SQL = "UPDATE electric_iot.device_location " +
            "SET electric_iot.device_location.province = '%s', " +
            "electric_iot.device_location.city = '%s', " +
            "electric_iot.device_location.district = '%s' " +
            "WHERE electric_iot.device_location.id = %d";

    public static final String UPDATE_WEATHER_SQL = "UPDATE electric_iot.device_location " +
            "SET electric_iot.device_location.temp = '%s', " +
            "electric_iot.device_location.humidity = '%s', " +
            "electric_iot.device_location.weather = '%s' " +
            "WHERE electric_iot.device_location.id = %d";

    public static final String SERVE_URL = "http://localhost:8080/api/manage/location";

    // 基站信息转经纬度接口
    public static final String AGPS_CONVERT_URL = "http://api.gpsspg.com/bs/?oid=%s&key=9564xy0zx29yu427ywz50439u49uu16370yxx&type=%s&bs=%s&hex=%s&to=%s&output=%s";
    // 经纬度转地理信息接口
    public static final String GECODE_CONVERT_URL = "https://restapi.amap.com/v3/geocode/regeo?key=f11511080fd76b66485b50902cb00a75&location=%s";
    // 天气转换接口
    public static final String WEATHER_CONVERT_URL = "http://v.juhe.cn/weather/geo?key=e0fca72351651e3c4e910ed848f57d0f&dtype=json&format=1&lon=%s&lat=%s";
}
