package siso.edu.cn;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class LocationChecking {

    private static final OkHttpClient client = new OkHttpClient();
    private static Connection connection = null;
    private static Statement statement = null;
    private static Statement previousStatement = null;

    public static void Checking() throws ClassNotFoundException, SQLException, IOException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        // 注册JDBC驱动
        Class.forName(GlobalSetting.JDBC_DRIVER);
        // 打开数据库链接
        if (connection == null) {
            connection = DriverManager.getConnection(GlobalSetting.DB_URL, GlobalSetting.DB_USER, GlobalSetting.DB_PASSWORD);
        }
        // 实例化Statement对象
        if (statement == null) {
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        }
        if (previousStatement == null) {
            previousStatement = connection.createStatement();
        }
        // 获取查询的结果
        ResultSet resultSet = statement.executeQuery(GlobalSetting.CHECKING_SQL);

        while (!resultSet.isClosed() && resultSet.next()) {
            // 获取需要更新的ID
            long id = resultSet.getLong("id");
            // 获取需要更新的设备ID
            long deviceId = resultSet.getLong("device_id");

            // Step1：获取获取当前设备的上一条位置数据
            ResultSet previousResultSet = previousStatement.executeQuery(String.format(GlobalSetting.PREVIOUS_DATA_SQL, deviceId, id));
            previousResultSet.last();
            // 计算上一条数据集的大小
            int count = previousResultSet.getRow();
            previousResultSet.beforeFirst();
            // 保存上一条数据的省市县
            String previousProvince = StringUtils.EMPTY;
            String previousCity = StringUtils.EMPTY;
            String previousDistrict = StringUtils.EMPTY;

            if (count == 1) {
                previousResultSet.next();
                // 获取上一条数据的ID
                long previousId = previousResultSet.getLong("id");
                // 判断上一条数据中省份是否已经填充
                if (previousResultSet.getString("province") == null) {
                    String gecodeUrl = String.format(
                            GlobalSetting.GECODE_CONVERT_URL,
                            (Object[]) new String[] {previousResultSet.getBigDecimal("longitude") + "," + previousResultSet.getBigDecimal("latitude")});
                    Request request = new Request.Builder().url(gecodeUrl).build();
                    String data = client.newCall(request).execute().body().string();
                    JSONObject geocodeObject = JSON.parseObject(data);

                    if (geocodeObject.getIntValue("status") != 1) {
                        System.out.println(String.format("[%s]ERROR:ID = %d 上一条数据根据经纬度转化为省市县信息错误", simpleDateFormat.format(new Date()), previousId));
                        previousStatement.executeUpdate(String.format(GlobalSetting.DELETE_DATA_SQL, previousId));
                        previousResultSet.close();
                        continue;
                    }

                    previousProvince = geocodeObject.getJSONObject("regeocode").getJSONObject("addressComponent").getString("province");
                    previousCity = geocodeObject.getJSONObject("regeocode").getJSONObject("addressComponent").getString("city");
                    previousDistrict = geocodeObject.getJSONObject("regeocode").getJSONObject("addressComponent").getString("district");

                    // 兼容市直辖市数据中城市为空的问题
                    if (previousCity == null) {
                        previousCity = previousDistrict;
                    }

                    if (previousProvince != null && previousCity != null && previousDistrict != null) {
                        System.out.println(String.format("[%s]SUCCESS:ID = %d 上一条数据更新省市县信息成功", simpleDateFormat.format(new Date()), previousId));
                        previousStatement.executeUpdate(String.format(GlobalSetting.UPDATE_LOCAL_SQL, previousProvince, previousCity, previousDistrict, previousId));
                    } else {
                        System.out.println(String.format("[%s]ERROR:ID = %d 上一条数据获取省市县信息错误", simpleDateFormat.format(new Date()), previousId));
                        previousStatement.executeUpdate(String.format(GlobalSetting.DELETE_DATA_SQL, previousId));
                        previousResultSet.close();
                        continue;
                    }
                } else {
                    previousProvince = previousResultSet.getString("province");
                    previousCity = previousResultSet.getString("city");
                    previousDistrict = previousResultSet.getString("district");

                    previousResultSet.close();
                }
            }

            // 获得当前数据的经纬度
            BigDecimal longitude = resultSet.getBigDecimal("longitude");
            BigDecimal latitude = resultSet.getBigDecimal("latitude");
            // 获取AGPS返回的数据
            AGpsEntity aGpsEntity = null;
            if (longitude == null && latitude == null) {
                String agps = StringUtils.EMPTY;

                if ((resultSet.getObject("nation_num_1") != null && resultSet.getInt("nation_num_1") >= 0) &&
                        (resultSet.getObject("mobile_num_1") != null && resultSet.getInt("mobile_num_1") >= 0) &&
                        (resultSet.getObject("location_num_1") != null && resultSet.getInt("location_num_1") >= 0) &&
                        (resultSet.getObject("community_num_1") != null && resultSet.getInt("community_num_1") >= 0) &&
                        (resultSet.getObject("station_flag_1") != null && resultSet.getInt("station_flag_1") >= 0) &&
                        (resultSet.getObject("signal_strength_1") != null && resultSet.getInt("signal_strength_1") >= 0)) {
                    agps += (resultSet.getInt("nation_num_1") + ",");
                    agps += (String.format("%02d", resultSet.getInt("mobile_num_1")) + ",");
                    agps += (resultSet.getInt("location_num_1") + ",");
                    agps += (resultSet.getInt("community_num_1") + ",");
                    agps += (resultSet.getInt("station_flag_1") * -1);
                }

                if ((resultSet.getObject("nation_num_2") != null && resultSet.getInt("nation_num_2") >= 0) &&
                        (resultSet.getObject("mobile_num_2") != null && resultSet.getInt("mobile_num_2") >= 0) &&
                        (resultSet.getObject("location_num_2") != null && resultSet.getInt("location_num_2") >= 0) &&
                        (resultSet.getObject("community_num_2") != null && resultSet.getInt("community_num_2") >= 0) &&
                        (resultSet.getObject("station_flag_2") != null && resultSet.getInt("station_flag_2") >= 0) &&
                        (resultSet.getObject("signal_strength_2") != null && resultSet.getInt("signal_strength_2") >= 0)) {
                    agps += ("|");
                    agps += (resultSet.getInt("nation_num_2") + ",");
                    agps += (String.format("%02d", resultSet.getInt("mobile_num_2")) + ",");
                    agps += (resultSet.getInt("location_num_2") + ",");
                    agps += (resultSet.getInt("community_num_2") + ",");
                    agps += (resultSet.getInt("station_flag_2") * -1);
                }

                if ((resultSet.getObject("nation_num_3") != null && resultSet.getInt("nation_num_3") >= 0) &&
                        (resultSet.getObject("mobile_num_3") != null && resultSet.getInt("mobile_num_3") >= 0) &&
                        (resultSet.getObject("location_num_3") != null && resultSet.getInt("location_num_3") >= 0) &&
                        (resultSet.getObject("community_num_3") != null && resultSet.getInt("community_num_3") >= 0) &&
                        (resultSet.getObject("station_flag_3") != null && resultSet.getInt("station_flag_3") >= 0) &&
                        (resultSet.getObject("signal_strength_3") != null && resultSet.getInt("signal_strength_3") >= 0)) {
                    agps += ("|");
                    agps += (resultSet.getInt("nation_num_3") + ",");
                    agps += (String.format("%02d", resultSet.getInt("mobile_num_3")) + ",");
                    agps += (resultSet.getInt("location_num_3") + ",");
                    agps += (resultSet.getInt("community_num_3") + ",");
                    agps += (resultSet.getInt("station_flag_3") * -1);
                }

                if ((resultSet.getObject("nation_num_4") != null && resultSet.getInt("nation_num_4") >= 0) &&
                        (resultSet.getObject("mobile_num_4") != null && resultSet.getInt("mobile_num_4") >= 0) &&
                        (resultSet.getObject("location_num_4") != null && resultSet.getInt("location_num_4") >= 0) &&
                        (resultSet.getObject("community_num_4") != null && resultSet.getInt("community_num_4") >= 0) &&
                        (resultSet.getObject("station_flag_4") != null && resultSet.getInt("station_flag_4") >= 0) &&
                        (resultSet.getObject("signal_strength_4") != null && resultSet.getInt("signal_strength_4") >= 0)) {
                    agps += ("|");
                    agps += (resultSet.getInt("nation_num_4") + ",");
                    agps += (String.format("%02d", resultSet.getInt("mobile_num_4")) + ",");
                    agps += (resultSet.getInt("location_num_4") + ",");
                    agps += (resultSet.getInt("community_num_4") + ",");
                    agps += (resultSet.getInt("station_flag_4") * -1);
                }

                // Step2：根据基站数据获取高德的经纬度数据
                String aGpsUrl = String.format(
                        GlobalSetting.AGPS_CONVERT_URL,
                        (Object[]) new String[] {"9628", "gsm", agps, "10", "2", "json"});
                Request request = new Request.Builder().url(aGpsUrl).build();
                String data = client.newCall(request).execute().body().string();
                aGpsEntity = JSON.parseObject(data, AGpsEntity.class);
            } else {
                aGpsEntity = new AGpsEntity();
                aGpsEntity.setLatitude(resultSet.getBigDecimal("latitude"));
                aGpsEntity.setLongitude(resultSet.getBigDecimal("longitude"));
                aGpsEntity.setStatus(200);
            }

            if (aGpsEntity.getStatus() != 200) {
                // 没有续费不代表数据错误
                if (aGpsEntity.getStatus() != 702) {
                    System.out.println(String.format("[%s]ERROR:ID = %d GPS数据转换错误", simpleDateFormat.format(new Date()), id));
                    statement.executeUpdate(String.format(GlobalSetting.DELETE_DATA_SQL, id));
                }
                continue;
            }

            // Step3：把经纬度转化为地理信息
            String gecodeUrl = String.format(
                    GlobalSetting.GECODE_CONVERT_URL,
                    (Object[]) new String[] {aGpsEntity.getLongitude().toString() + "," + aGpsEntity.getLatitude()});
            Request request = new Request.Builder().url(gecodeUrl).build();
            String data = client.newCall(request).execute().body().string();
            JSONObject geocodeObject = JSON.parseObject(data);

            if (geocodeObject.getIntValue("status") != 1) {
                System.out.println(String.format("[%s]ERROR:ID = %d 根据经纬度转化为省市县信息错误", simpleDateFormat.format(new Date()), id));
                statement.executeUpdate(String.format(GlobalSetting.DELETE_DATA_SQL, id));
                continue;
            }

            // Step4：判断市是否相同
            String province = geocodeObject.getJSONObject("regeocode").getJSONObject("addressComponent").get("province") instanceof JSONArray ?
                    null : geocodeObject.getJSONObject("regeocode").getJSONObject("addressComponent").getString("province");
            String city = geocodeObject.getJSONObject("regeocode").getJSONObject("addressComponent").get("city") instanceof JSONArray ?
                    null : geocodeObject.getJSONObject("regeocode").getJSONObject("addressComponent").getString("city");
            String district = geocodeObject.getJSONObject("regeocode").getJSONObject("addressComponent").get("district") instanceof JSONArray ?
                    null : geocodeObject.getJSONObject("regeocode").getJSONObject("addressComponent").getString("district");
            // CASE1：只要有一个数据为空说明数据有问题，则删除, 兼容市直辖市数据中城市为空的问题
            if (province == null || district == null) {
                System.out.println(String.format("[%s]ERROR:ID = %d 根据经纬度转化为省市县信息错误", simpleDateFormat.format(new Date()), id));
                statement.executeUpdate(String.format(GlobalSetting.DELETE_DATA_SQL, id));
                continue;
            }
            // 兼容市直辖市数据中城市为空的问题
            if (city == null) {
                city = district;
            }

            // CASE2：(1)判断市数据相同则保存，（2）只是经纬度数据则保存，（3）没有省份，但是有经纬度
            if ((!previousCity.equals(StringUtils.EMPTY) && city.equals(previousCity)) ||
                    aGpsEntity.getResult() == null ||
                    (resultSet.getString("province") == null && resultSet.getBigDecimal("longitude") != null)) {
                if (resultSet.getBigDecimal("longitude") == null) {
                    statement.executeUpdate(String.format(GlobalSetting.UPDATE_SQL,
                            aGpsEntity.getLongitude(),
                            aGpsEntity.getLongitude().compareTo(new BigDecimal(0)) > 0 ? 69 : 87,
                            aGpsEntity.getLatitude(),
                            aGpsEntity.getLatitude().compareTo(new BigDecimal(0)) > 0 ? 78 : 83,
                            province, city, district, id));
                } else {
                    statement.executeUpdate(String.format(GlobalSetting.UPDATE_LOCAL_SQL,
                            province, city, district, id));
                }

                // 获取天气信息
                String weatherUrl = String.format(
                        GlobalSetting.WEATHER_CONVERT_URL,
                        (Object[]) new String[] {
                                aGpsEntity.getLongitude().toString(),
                                aGpsEntity.getLatitude().toString()});
                Request weatherRequest = new Request.Builder().url(weatherUrl).build();
                String weatherData = client.newCall(weatherRequest).execute().body().string();
                JSONObject weatherNode = JSON.parseObject(weatherData);
                System.out.println(weatherData);
                String temp = weatherNode.getJSONObject("result").getJSONObject("sk").getString("temp");
                String humidity = weatherNode.getJSONObject("result").getJSONObject("sk").getString("humidity");
                String weather = weatherNode.getJSONObject("result").getJSONObject("today").getString("weather");
                // 保存天气信息
                statement.executeUpdate(String.format(GlobalSetting.UPDATE_WEATHER_SQL, temp, humidity, weather, id));

                System.out.println(String.format("[%s]SUCCESS[0]:ID = %d 更新经纬度和地理信息完成", simpleDateFormat.format(new Date()), id));
                continue;
            }

            // Step5：进一步判断所有基站是否相同
            List<String> cityList = new ArrayList<String>();
            // 利用Set唯一性判断数组元素是否全相同
            Set<String> cityEquals = new HashSet<String>();
            // 获取所有城市信息
            for (AGpsResultEntity resultEntity : aGpsEntity.getResult()) {
                String itemGecodeUrl = String.format(
                        GlobalSetting.GECODE_CONVERT_URL,
                        (Object[]) new String[] {resultEntity.getLng().toString() + "," + resultEntity.getLat().toString()});
                request = new Request.Builder().url(itemGecodeUrl).build();
                data = client.newCall(request).execute().body().string();
                JSONObject itemGeocodeNode = JSON.parseObject(data);

                String itemCity = itemGeocodeNode.getJSONObject("regeocode").getJSONObject("addressComponent").get("city") instanceof JSONArray ?
                        null : itemGeocodeNode.getJSONObject("regeocode").getJSONObject("addressComponent").getString("city");
                // 如果数据错误则删除
                if (itemCity == null) {
                    itemCity = itemGeocodeNode.getJSONObject("regeocode").getJSONObject("addressComponent").getString("province");
                }

                cityList.add(itemCity);
                cityEquals.add(itemCity);
            }

            // Step6：如果全部市数据相同，则保存
            // 如果满足条件说明城市有不同，则数据错误
            if (cityEquals.size() != 1) {
                System.out.println(String.format("[%s]ERROR:ID = %d 各基站子信息不一致", simpleDateFormat.format(new Date()), id));
                statement.executeUpdate(String.format(GlobalSetting.DELETE_DATA_SQL, id));
                continue;
            }

            statement.executeUpdate(String.format(GlobalSetting.UPDATE_SQL,
                    aGpsEntity.getLongitude(),
                    aGpsEntity.getLongitude().compareTo(new BigDecimal(0)) > 0 ? 69 : 87,
                    aGpsEntity.getLatitude(),
                    aGpsEntity.getLatitude().compareTo(new BigDecimal(0)) > 0 ? 78 : 83, province, city, district, id));

            // 获取天气信息
            String weatherUrl = String.format(
                    GlobalSetting.WEATHER_CONVERT_URL,
                    (Object[]) new String[] {
                            aGpsEntity.getLongitude().toString(),
                            aGpsEntity.getLatitude().toString()});
            Request weatherRequest = new Request.Builder().url(weatherUrl).build();
            String weatherData = client.newCall(weatherRequest).execute().body().string();
            JSONObject weatherNode = JSON.parseObject(weatherData);
            System.out.println(weatherData);
            String temp = weatherNode.getJSONObject("result").getJSONObject("sk").getString("temp");
            String humidity = weatherNode.getJSONObject("result").getJSONObject("sk").getString("humidity");
            String weather = weatherNode.getJSONObject("result").getJSONObject("today").getString("weather");
            // 保存天气信息
            statement.executeUpdate(String.format(GlobalSetting.UPDATE_WEATHER_SQL, temp, humidity, weather, id));

            System.out.println(String.format("[%s]SUCCESS[1]:ID = %d 更新经纬度和地理信息完成", simpleDateFormat.format(new Date()), id));
        }
    }

    public static void close() throws SQLException {
        // 关闭数据查询
        statement.close();
        previousStatement.close();
        // 关闭数据链接
        connection.close();
    }
}
