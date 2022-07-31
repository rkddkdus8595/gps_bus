package com.example.gps_bus;

public class inform {

    public String lowPlate1; // 저상버스인지 확인
    public String lowPlate2;
    public String predictTime1; // 도착까지 예상시간
    public String predictTime2;
    public String locationNo1; // 몇번째 전 정류장
    public String locationNo2;
    public String remainSeat1;
    public String remainSeat2;
    public String flag;
    public String routeName;

    public inform(String routeName,String lowPlate1,String lowPlate2, String predictTime1, String predictTime2, String locationNo1,
                  String locationNo2, String remainSeat1, String remainSeat2, String flag){
        this.routeName=routeName;
        this.lowPlate1=lowPlate1;
        this.lowPlate2=lowPlate2;
        this.predictTime1=predictTime1;
        this.predictTime2=predictTime2;
        this.locationNo1=locationNo1;
        this.locationNo2=locationNo2;
        this.remainSeat1=remainSeat1;
        this.remainSeat2=remainSeat2;
        this.flag=flag;
    }
    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }


    public String getLowPlate1() {
        return lowPlate1;
    }

    public void setLowPlate1(String lowPlate1) {
        this.lowPlate1 = lowPlate1;
    }

    public String getLowPlate2() {
        return lowPlate2;
    }

    public void setLowPlate2(String lowPlate2) {
        this.lowPlate2 = lowPlate2;
    }

    public String getPredictTime1() {
        return predictTime1;
    }

    public void setPredictTime1(String predictTime1) {
        this.predictTime1 = predictTime1;
    }

    public String getPredictTime2() {
        return predictTime2;
    }

    public void setPredictTime2(String predictTime2) {
        this.predictTime2 = predictTime2;
    }

    public String getLocationNo1() {
        return locationNo1;
    }

    public void setLocationNo1(String locationNo1) {
        this.locationNo1 = locationNo1;
    }

    public String getLocationNo2() {
        return locationNo2;
    }

    public void setLocationNo2(String locationNo2) {
        this.locationNo2 = locationNo2;
    }

    public String getRemainSeat1() {
        return remainSeat1;
    }

    public void setRemainSeat1(String remainSeat1) {
        this.remainSeat1 = remainSeat1;
    }

    public String getRemainSeat2() {
        return remainSeat2;
    }

    public void setRemainSeat2(String remainSeat2) {
        this.remainSeat2 = remainSeat2;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
