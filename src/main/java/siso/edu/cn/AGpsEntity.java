package siso.edu.cn;

import java.math.BigDecimal;
import java.util.List;

public class AGpsEntity {
    private int status;
    private String msg;
    private int match;
    private int count;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<AGpsResultEntity> result;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getMatch() {
        return match;
    }

    public void setMatch(int match) {
        this.match = match;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public List<AGpsResultEntity> getResult() {
        return result;
    }

    public void setResult(List<AGpsResultEntity> result) {
        this.result = result;
    }
}
