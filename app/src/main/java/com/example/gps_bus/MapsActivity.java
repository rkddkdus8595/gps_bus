package com.example.gps_bus;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, SensorEventListener,GoogleMap.OnMarkerClickListener {
    // 버스 좌석 테스트 할때 stationId를 200000078로하면 직행버스의 경우 버스 잔여좌석이 뜬다.
    private GoogleMap mMap;
    LocationManager locationManager;
    public String serviceUrl, serviceKey, url, url2; // 기본 url, 서비스키, 통합 url

    Button btnArr, btnCorona,btn_busgo;

    ArrayList<Double> arrX = new ArrayList<Double>(); // x좌표 배열
    ArrayList<Double> arrY = new ArrayList<Double>(); // y좌표 배열
    ArrayList<String> arrStationName = new ArrayList<String>(); // 정류장 이름 배열
    ArrayList<String> arrStationId = new ArrayList<String>(); // 정류장 ID 배열

    SensorManager sensorM;
    Sensor sensor_light;

    Double d1 = 0.0;
    Double d2 = 0.0;
    public String stationId2="";
    public String routeId=""; // 노선 ID
    public String routeName=""; // 노선 번호

    public String lowPlate1; // 저상버스인지 확인
    public String lowPlate2;
    public String predictTime1; // 도착까지 예상시간
    public String predictTime2;
    public String locationNo1; // 몇번째 전 정류장
    public String locationNo2;
    public String remainSeat1;
    public String remainSeat2;
    public String flag;

    ArrayList<String> arrlowPlate1 = new ArrayList<String>();
    ArrayList<String> arrlowPlate2 = new ArrayList<String>();
    ArrayList<String> arrpredictTime1 = new ArrayList<String>();
    ArrayList<String> arrpredictTime2 = new ArrayList<String>();
    ArrayList<String> arrlocationNo1 = new ArrayList<String>();
    ArrayList<String> arrlocationNo2 = new ArrayList<String>();
    ArrayList<String> arrRouteName = new ArrayList<String>();
    ArrayList<String> arrRemainSeat1=new ArrayList<>();
    ArrayList<String> arrRemainSeat2=new ArrayList<>();
    ArrayList<String> arrFlag=new ArrayList<>();

    ArrayList<String> arrName=new ArrayList<>(); // 약국 이름
    ArrayList<String> arrCoronaX=new ArrayList<>(); // 약국 X좌표
    ArrayList<String> arrCoronaY=new ArrayList<>();
    ArrayList<String> arrType=new ArrayList<>(); //약국: '01', 우체국: '02', 농협: '03'
    ArrayList<String> arrRemainStat=new ArrayList<>(); //100개 이상(녹색): 'plenty' / 30개 이상 100개미만(노랑색): 'some' / 2개 이상 30개 미만(빨강색): 'few' / 1개 이하(회색): 'empty' / 판매중지: 'break'

    String routeId2=""; // 얘네는 버스번호 조회할때 쓰이는거
    String routeName2="";
    Integer first=0;
    boolean b_resultCode=false;
    boolean b_buslowPlate1=false; // 저상버스인지 확인
    boolean b_buslowPlate2=false;
    boolean b_predictTime1=false; // 도착까지 예상시간
    boolean b_predictTime2=false;
    boolean b_locationNo1=false; // 몇번째 전 정류장
    boolean b_locationNo2=false;
    boolean b_flag=false; // 버스가 운행중인지 아닌지
    boolean b_remainSeatCnt1=false; // 버스의 잔여좌석
    boolean b_remainSeatCnt2=false;
    boolean b_routeId=false; // 노선 ID

    boolean marked=true;
    Double markerClick1=0.0;
    Double markerClick2=0.0;
    boolean markerClickFlag=false;
    Vibrator vibrator;
    int current_light=0;
    boolean current_auto_mode=false; // 자동밝기인지 아닌지 확인 플래그
    WindowManager.LayoutParams parameter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Toast.makeText(this, mAuth.getUid(), Toast.LENGTH_SHORT).show(); 이 UID를 통해 본인 여부 판별할거라 테스트용
        vibrator=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        parameter=getWindow().getAttributes();
        StrictMode.enableDefaults();
        sensorM=(SensorManager)getSystemService(SENSOR_SERVICE);
        sensor_light=sensorM.getDefaultSensor(Sensor.TYPE_LIGHT);
        btnArr=(Button)findViewById(R.id.btnArr);
        btnCorona=(Button)findViewById(R.id.btnCorona);
        btn_busgo=(Button)findViewById(R.id.btn_busgo);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //앱 실행중 화면이 꺼지지 않게 유지

    }
    public void getLight(){ // 밝기 받아오기
        try {
            current_light = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS); // 현재 밝기 받아오기
            if(android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE) == 1){ // 자동밝기인지 확인하기
                current_auto_mode=true; // 자동밝기임을 표시해주기 (나중에 앱을 종료했을 때 원래대로 돌려놓기 위해서)
                android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, 0); // 자동밝기인 경우 밝기 조절이 안돼서 사용안함으로 만들어주기
            }
        } catch (Settings.SettingNotFoundException e) {
            Log.e("Error: "+e.getMessage(), null);
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        sensorM.registerListener(this, sensor_light, sensorM.SENSOR_DELAY_FASTEST); // 갱신주기 빠른걸로
    }

    @Override
    public void onPause(){
        super.onPause();
        sensorM.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) { // **********************더 수정하기 그 액티비티에서의 화면밝기 조절은 되지만 다른 액티비티로 넘어갈때 다시꺼짐

        getLight();
        int light=(int)event.values[0];
        //System.out.println(light);
        if(light>=90000){ //자연광인경우
            float origin=parameter.screenBrightness;
            parameter.screenBrightness=1.0f;
            getWindow().setAttributes(parameter);

            /*Settings.System.putInt(getContentResolver(),"screen_brightness",250);
            parameter.screenBrightness=(float)1.9;
            getWindow().setAttributes(parameter);*/
        }else if(light<=50 && light>=10){
            float origin=parameter.screenBrightness;
            parameter.screenBrightness=0.3f;
            getWindow().setAttributes(parameter);
        }else if(light>=1000 && light<=2000){
            float origin=parameter.screenBrightness;
            parameter.screenBrightness=0.7f;
            getWindow().setAttributes(parameter);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_COARSE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                //Snackbar.make(View, "위치권한을 허용해주세요", Snackbar.LENGTH_LONG).show();
                Toast.makeText(this, "위치권한 허용해주세요.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "위치권한 허용해주세요.", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this, "위치권한 허용해주세요.", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1000);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1000);

            return;
        }

        mMap.setMyLocationEnabled(true); //현재위치 버튼 활성화

        mMap.setOnMyLocationButtonClickListener(this); // 현재위치 버튼 누를때마다 !
        mMap.setOnMarkerClickListener(this); // 마커 눌렀을때 처리되는 이벤트
        mMap.setMapType(googleMap.MAP_TYPE_NORMAL);
        locationManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        btn_busgo.setOnClickListener(new Button.OnClickListener(){ // 버스/정류장 검색 넘어가는거

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);//액티비티 띄우기
            }
        });
        final LocationListener locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) { // provider가 변경될 때 호출됨

                d1=location.getLatitude(); // 위도 Y
                d2=location.getLongitude(); // 경도 X
                if(first==0){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(d1,d2),16)); // 지도 움직일때마다 다시 초점 이동하는거 고치기
                }
                if(markerClickFlag){
                    marked=true;
                    //Toast.makeText(MapsActivity.this, "d1="+(d1+0.001)+"/d2="+(d2+0.001)+"/d3="+d3+"/d4="+d4, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(MapsActivity.this, "d1="+(d1-0.001)+"/d2="+(d2-0.001)+"/d3="+d3+"/d4="+d4, Toast.LENGTH_SHORT).show();
                    // 범위 지정을 어떻게?
                    if ((d1+0.0025 > markerClick1 || d1-0.0025 > markerClick1) && (d2+0.0025 > markerClick2 || d2-0.0025 >markerClick2) && marked == true){
                        vibrator.vibrate(3000);
                        new AlertDialog.Builder(MapsActivity.this)
                                .setMessage("다와갑니다! 일어나세요")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which){
                                        Toast.makeText(getApplicationContext(), "확인 누름", Toast.LENGTH_SHORT).show(); // 실행할 코드
                                        markerClickFlag=false;
                                        vibrator.cancel();
                                    }
                                })
                                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which){
                                        Toast.makeText(getApplicationContext(), "취소 누름", Toast.LENGTH_SHORT).show(); // 실행할 코드
                                    }
                                })
                                .show();
                        marked=false;
                    }
                }

                btnCorona.setOnClickListener(new Button.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        getCorona();
                    }
                });
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(d1,d2),16)); // 지도 움직일때마다 다시 초점 이동하는거 고치기
                /*
                serviceUrl="http://openapi.gbis.go.kr/ws/rest/busstationservice/searcharound";
                serviceKey="4Ya%2FB8dRAQB8gaH%2Bskm2NIceS5b07ntqlwwk66zaizqNUduNGYB9ms2%2FrtEyD%2FAlsNNk9Cir3J%2FMa%2B4JANVodA%3D%3D";
                url=serviceUrl+"?serviceKey="+serviceKey+"&x="+d2+"&y="+d1;

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(d1,d2),16)); // 지도 움직일때마다 다시 초점 이동하는거 고치기
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(d1,d2),16)); // 얘는 부드럽게 이동한다.
                arrX.clear(); //위치 바뀔때마다 지우고 집어넣고
                arrY.clear();
                arrStationName.clear();
                new DownloadWebpageTask().execute(url);*/

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
    }

    @Override
    public boolean onMyLocationButtonClick() { // 버튼 누를때마다 얘가 호출됨 만약 버튼을 누를때마다 버스정류장 마커 찍고 싶으면 이걸로 !
        serviceUrl="http://openapi.gbis.go.kr/ws/rest/busstationservice/searcharound";
        serviceKey="4Ya%2FB8dRAQB8gaH%2Bskm2NIceS5b07ntqlwwk66zaizqNUduNGYB9ms2%2FrtEyD%2FAlsNNk9Cir3J%2FMa%2B4JANVodA%3D%3D";
        url=serviceUrl+"?serviceKey="+serviceKey+"&x="+d2+"&y="+d1;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(d1,d2),16)); // 지도 움직일때마다 다시 초점 이동하는거 고치기
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(d1,d2),16)); // 얘는 부드럽게 이동한다.
        arrX.clear(); //위치 바뀔때마다 지우고 집어넣고
        arrY.clear();
        arrStationName.clear();


        new DownloadWebpageTask().execute(url);
        return false;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) { // 얘 누르면 버스 도착정보 뜨게끔 만들어야함
        stationId2=marker.getSnippet();
        serviceUrl="http://openapi.gbis.go.kr/ws/rest/busarrivalservice/station";
        serviceKey="4Ya%2FB8dRAQB8gaH%2Bskm2NIceS5b07ntqlwwk66zaizqNUduNGYB9ms2%2FrtEyD%2FAlsNNk9Cir3J%2FMa%2B4JANVodA%3D%3D";
        url2=serviceUrl+"?serviceKey="+1234567890+"&stationId="+stationId2;
        //Toast.makeText(MapsActivity.this, url2, Toast.LENGTH_SHORT).show();
        final String result2=getBusArrive();
        markerClick1=marker.getPosition().latitude;
        markerClick2=marker.getPosition().longitude;
        markerClickFlag=true;


        btnArr.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),bus_view.class);
                intent.putStringArrayListExtra("arrlocationNo1",arrlocationNo1);
                intent.putStringArrayListExtra("arrlocationNo2",arrlocationNo2);
                intent.putStringArrayListExtra("arrlowPlate1",arrlowPlate1);
                intent.putStringArrayListExtra("arrlowPlate2",arrlowPlate2);
                intent.putStringArrayListExtra("arrpredictTime1",arrpredictTime1);
                intent.putStringArrayListExtra("arrpredictTime2",arrpredictTime2);
                intent.putStringArrayListExtra("arrRouteName",arrRouteName);
                intent.putStringArrayListExtra("arrRemainSeat1",arrRemainSeat1);
                intent.putStringArrayListExtra("arrRemainSeat2",arrRemainSeat2);
                intent.putStringArrayListExtra("arrFlag",arrFlag);
                intent.putExtra("result",result2);
                startActivity(intent);//액티비티 띄우기
            }
        });

        //new DownloadWebpageTask2().execute(url2);
        return false;
    }
    public void getCorona(){
        String url3="";
        serviceUrl="https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1/storesByGeo/json?";
        url3=serviceUrl+"lat="+d1+"&lng="+d2+"&m=3000";
        //Toast.makeText(this, url3, Toast.LENGTH_SHORT).show();
        String type123="";
        String number123="";
        arrName.clear();
        arrType.clear();
        arrRemainStat.clear();
        arrCoronaY.clear();
        arrCoronaX.clear();

        InputStream is;
        try {
            is = new URL(url3).openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is,"UTF-8"));
            String str1;
            String result1="";

            BufferedReader bf;
            bf = new BufferedReader(new InputStreamReader(is));
            while ((str1 = bf.readLine()) != null) {
                result1 = result1.concat(str1);

            }
            JSONObject root = new JSONObject(result1);
            JSONArray jsonArray = root.getJSONArray("stores");

            for(int i = 0 ; i<jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                arrName.add(jsonObject.getString("name"));
                arrType.add(jsonObject.getString("type"));
                arrRemainStat.add(jsonObject.getString("remain_stat"));
                arrCoronaX.add(jsonObject.getString("lat"));
                arrCoronaY.add(jsonObject.getString("lng"));
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        for (int idx = 0; idx < arrName.size(); idx++) {
            //Toast.makeText(this, arrName.get(idx), Toast.LENGTH_SHORT).show();
            if(arrType.get(idx).equals("01")){
                type123="약국";
            }else if(arrType.get(idx).equals("02")){
                type123="우체국";
            }else if(arrType.get(idx).equals("03")){
                type123="농협";
            }

            switch (arrRemainStat.get(idx)) {
                case "plenty" : {
                    number123 = "100개이상";
                    break;
                }
                case "some" : {
                    number123 = "30개 이상 100개 미만";
                    break;
                }
                case "few" : {
                    number123 = "2개 이상 30개 미만";
                    break;
                }
                case "empty" : {
                    number123 = "1개 이하";
                    break;
                }
            }

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions
                    .position(new LatLng(Double.parseDouble(arrCoronaX.get(idx)),Double.parseDouble(arrCoronaY.get(idx))))
                    .title(arrName.get(idx))
                    .snippet(type123+"/"+number123);
            if(type123.equals("약국")) {
                BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.medicine);
                Bitmap b = bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 80, 80, false);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            }else if(type123.equals("우체국")){
                BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.postal);
                Bitmap b = bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 80, 80, false);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            }else if(type123.equals("농협")){
                BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.nonghyup);
                Bitmap b = bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 80, 80, false);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            }
            mMap.addMarker(markerOptions);

        }

    }
    public String getBusArrive(){ // 이 메소드는 도착정보 조회를 위해 만들어 놓은 것
        serviceUrl="http://openapi.gbis.go.kr/ws/rest/busarrivalservice/station";
        serviceKey="4Ya%2FB8dRAQB8gaH%2Bskm2NIceS5b07ntqlwwk66zaizqNUduNGYB9ms2%2FrtEyD%2FAlsNNk9Cir3J%2FMa%2B4JANVodA%3D%3D";
        url2=serviceUrl+"?serviceKey="+"1234567890"+"&stationId="+stationId2;
        String result="";

        String strResultCode="";
        b_resultCode=false;
        b_buslowPlate1=false; // 저상버스인지 확인
        b_buslowPlate2=false;
        b_predictTime1=false; // 도착까지 예상시간
        b_predictTime2=false;
        b_locationNo1=false; // 몇번째 전 정류장
        b_locationNo2=false;
        b_remainSeatCnt1=false; // 남은좌석 (좌석형시내버스)일 경우에만 뜸
        b_remainSeatCnt2=false;
        b_flag=false; // 운행 상태 (RUN:운행중, PASS:운행중, STOP:운행종료, WAIT:회차지대기)
        b_routeId=false; // 노선 ID

        // 왜 전체 초기화 시켜주냐면 불러올때마다 쌓이는거 방지하기 위해서
        arrlocationNo1.clear();
        arrlocationNo2.clear();
        arrlowPlate1.clear();
        arrlowPlate2.clear();
        arrRouteName.clear();
        arrpredictTime1.clear();
        arrpredictTime2.clear();
        arrRemainSeat1.clear();
        arrRemainSeat2.clear();

        //Toast.makeText(MapsActivity.this, routeName, Toast.LENGTH_SHORT).show();

        try{
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp=factory.newPullParser();

            URL url= new URL(url2);//문자열로 된 요청 url을 URL 객체로 생성.
            InputStream is= url.openStream(); //url위치로 입력스트림 연결
            HttpURLConnection urlConn=null;
            urlConn=(HttpURLConnection)url.openConnection();
            urlConn.setRequestMethod("GET");
            BufferedInputStream inBuf=new BufferedInputStream(urlConn.getInputStream());
            BufferedReader bufReader=new BufferedReader(new InputStreamReader(inBuf, "utf-8"));

            String strLine=null;
            String strPage="";
            while((strLine=bufReader.readLine())!=null){
                strPage+=strLine;
            }

            xpp.setInput(new StringReader(strPage));

            int eventType=xpp.getEventType();

            while(eventType!=XmlPullParser.END_DOCUMENT){
                if(eventType==XmlPullParser.START_DOCUMENT){
                    ;
                }else if(eventType==XmlPullParser.START_TAG){
                    String tag_name=xpp.getName();

                    //Toast.makeText(MapsActivity.this, "다운로드2", Toast.LENGTH_SHORT).show();
                    if(tag_name.equals("resultCode")) b_resultCode=true;
                    if(tag_name.equals("lowPlate1")) b_buslowPlate1=true;
                    if(tag_name.equals("lowPlate2")) b_buslowPlate2=true;
                    if(tag_name.equals("predictTime1")) b_predictTime1=true;
                    if(tag_name.equals("predictTime2")) b_predictTime2=true;
                    if(tag_name.equals("locationNo1")) b_locationNo1=true;
                    if(tag_name.equals("locationNo2")) b_locationNo2=true;
                    if(tag_name.equals("remainSeatCnt1")) b_remainSeatCnt1=true;
                    if(tag_name.equals("remainSeatCnt2")) b_remainSeatCnt2=true;
                    if(tag_name.equals("flag")) b_flag=true;
                    if(tag_name.equals("routeId")) b_routeId=true;
                }else if(eventType==XmlPullParser.TEXT){
                    if(b_resultCode){
                        strResultCode=xpp.getText();
                        b_resultCode=false;
                    }
                    if(strResultCode.equals("0")){
                        if(b_buslowPlate1){
                            lowPlate1=xpp.getText();
                            if(xpp.getText().equals("0")){
                                arrlowPlate1.add("X");
                            }else{
                                arrlowPlate1.add("O");
                            }
                            //arrlowPlate1.add(xpp.getText());
                            b_buslowPlate1=false;
                            result=result+"저상버스1: "+xpp.getText();
                        }
                        if(b_buslowPlate2){
                            lowPlate2=xpp.getText();
                            if(xpp.getText().equals("0")){
                                arrlowPlate2.add("X");
                            }else{
                                arrlowPlate2.add("O");
                            }
                            //arrlowPlate2.add(xpp.getText());
                            b_buslowPlate2=false;
                            result=result+"/ 저상버스2: "+xpp.getText();
                        }
                        if(b_predictTime1){
                            predictTime1=xpp.getText();
                            arrpredictTime1.add(xpp.getText());
                            b_predictTime1=false;
                            result=result+"/ 예상시간1 : "+xpp.getText();
                        }
                        if(b_predictTime2){
                            predictTime2=xpp.getText();
                            arrpredictTime2.add(xpp.getText());
                            b_predictTime2=false;
                            result=result+"/ 예상시간 2: "+xpp.getText();
                        }
                        if(b_locationNo1){
                            locationNo1=xpp.getText();
                            arrlocationNo1.add(xpp.getText());
                            b_locationNo1=false;
                            result=result+"/ 위치1: "+xpp.getText();
                        }
                        if(b_locationNo2){
                            locationNo2=xpp.getText();
                            arrlocationNo2.add(xpp.getText());
                            b_locationNo2=false;
                            result=result+"/ 위치2: "+xpp.getText();
                        }
                        if(b_routeId){
                            routeId=xpp.getText();
                            b_routeId=false;
                            routeName=getBusName(routeId);
                            arrRouteName.add(routeName);
                            result=result+"/ 버스번호: "+routeName;
                        }
                        if(b_remainSeatCnt1){
                            remainSeat1=xpp.getText();
                            b_remainSeatCnt1=false;
                            arrRemainSeat1.add(xpp.getText());
                        }
                        if(b_remainSeatCnt2){
                            remainSeat2=xpp.getText();
                            b_remainSeatCnt2=false;
                            arrRemainSeat2.add(xpp.getText());
                        }
                        if(b_flag){
                            flag=xpp.getText();
                            b_flag=false;
                            arrFlag.add(xpp.getText());
                        }
                    }
                    result=result+"\n";
                }else if(eventType==XmlPullParser.END_TAG){
                    ;
                }
                eventType=xpp.next();
            }
        }catch(Exception e){
            e.getMessage();
        }
        //Toast.makeText(MapsActivity.this, result, Toast.LENGTH_SHORT).show();
        return result;

    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return (String) downloadUrl((String) urls[0]);
            } catch (IOException e) {
                return "다운로드 실패 !";
            }
        }

        protected void onPostExecute(String result) {
            String strResultCode="";
            Double doubleX=0.0;
            Double doubleY=0.0;
            String strStationId="";
            String strStationName="";

            boolean b_resultCode=false;
            boolean b_busX=false;
            boolean b_busY=false;
            boolean b_StationId=false;
            boolean b_StationName=false;

            try{
                XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp=factory.newPullParser();

                xpp.setInput(new StringReader(result));
                //Toast.makeText(MapsActivity.this, result, Toast.LENGTH_SHORT).show();

                int eventType=xpp.getEventType();

                while(eventType!=XmlPullParser.END_DOCUMENT){
                    if(eventType==XmlPullParser.START_DOCUMENT){
                        ;
                    }else if(eventType==XmlPullParser.START_TAG){
                        String tag_name=xpp.getName();

                        //Toast.makeText(MapsActivity.this, tag_name, Toast.LENGTH_SHORT).show();

                        if(tag_name.equals("resultCode")) b_resultCode=true;
                        if(tag_name.equals("x")) b_busX=true;
                        if(tag_name.equals("y")) b_busY=true;
                        if(tag_name.equals("stationId")) b_StationId=true;
                        if(tag_name.equals("stationName")) b_StationName=true;
                    }else if(eventType==XmlPullParser.TEXT){
                        if(b_resultCode){
                            strResultCode=xpp.getText();
                            b_resultCode=false;
                        }
                        if(strResultCode.equals("0")){
                            if(b_busX){
                                doubleX=Double.valueOf(xpp.getText());
                                arrX.add(doubleX);
                                b_busX=false;
                            }
                            if(b_busY){
                                doubleY=Double.valueOf(xpp.getText());
                                arrY.add(doubleY);
                                b_busY=false;
                            }
                            if(b_StationId){
                                strStationId=xpp.getText();
                                arrStationId.add(strStationId);
                                b_StationId=false;
                            }
                            if(b_StationName){
                                strStationName=xpp.getText();
                                arrStationName.add(strStationName);
                                b_StationName=false;
                            }
                        }
                    }else if(eventType==XmlPullParser.END_TAG){
                        ;
                    }
                    eventType=xpp.next();
                }
            }catch(Exception e){
                e.getMessage();
            }
            //Toast.makeText(MapsActivity.this, arrStationName.toString(), Toast.LENGTH_SHORT).show();
            for (int idx = 0; idx < arrX.size(); idx++) {
                //Toast.makeText(MapsActivity.this, arrX.get(idx).toString()+"/"+arrY.get(idx).toString()+"/"+arrStationName.get(idx), Toast.LENGTH_SHORT).show();

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions
                        .position(new LatLng(arrY.get(idx), arrX.get(idx))) // 구글같은 경우 위도 경도가 반대로 되어있어서 넣어줄때 반대로 !
                        .title(arrStationName.get(idx))
                        .snippet(arrStationId.get(idx)); // 정류장 번호 넣어주기 나중에 클릭 시 가져오기 위함
                //Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bus),200,200,false);
                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.busmain);
                Bitmap b=bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 80, 80, false);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                // 2. 마커 생성 (마커를 나타냄)
                mMap.addMarker(markerOptions);
            }
        }
        private String downloadUrl(String myurl) throws IOException{
            HttpURLConnection urlConn=null;
            try{

                StringBuilder urlBuilder = new StringBuilder("http://openapi.gbis.go.kr/ws/rest/busstationservice/searcharound"); /*URL*/
                urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=4Ya%2FB8dRAQB8gaH%2Bskm2NIceS5b07ntqlwwk66zaizqNUduNGYB9ms2%2FrtEyD%2FAlsNNk9Cir3J%2FMa%2B4JANVodA%3D%3D");
                urlBuilder.append("&" + URLEncoder.encode("x","UTF-8") + "=" + URLEncoder.encode(Double.toString(d2), "UTF-8"));
                urlBuilder.append("&" + URLEncoder.encode("y","UTF-8") + "=" + URLEncoder.encode(Double.toString(d1), "UTF-8"));

                URL url=new URL(urlBuilder.toString());
                urlConn=(HttpURLConnection)url.openConnection();
                urlConn.setRequestMethod("GET");
                BufferedInputStream inBuf=new BufferedInputStream(urlConn.getInputStream());
                BufferedReader bufReader=new BufferedReader(new InputStreamReader(inBuf, "utf-8"));

                String strLine=null;
                String strPage="";
                while((strLine=bufReader.readLine())!=null){
                    strPage+=strLine;
                }
                return strPage;
            }finally {
                urlConn.disconnect();
            }
        }
    }

    public String getBusName(String str){ // 이 메소드는 노선 ID를 통해 버스번호(routeName)을 찾기위해 만들어놓은 것

        serviceUrl="http://openapi.gbis.go.kr/ws/rest/busstationservice/route";
        serviceKey="1234567890";
        url2=serviceUrl+"?serviceKey="+serviceKey+"&stationId="+stationId2;

        boolean b_resultCode=false;
        boolean b_routeId=false;
        boolean b_routeName=false;
        boolean flag=false;
        String strResultCode="";

        //Toast.makeText(MapsActivity.this, routeName, Toast.LENGTH_SHORT).show();

        try{
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp=factory.newPullParser();

            URL url= new URL(url2);//문자열로 된 요청 url을 URL 객체로 생성.
            InputStream is= url.openStream(); //url위치로 입력스트림 연결
            HttpURLConnection urlConn=null;
            urlConn=(HttpURLConnection)url.openConnection();
            urlConn.setRequestMethod("GET");
            BufferedInputStream inBuf=new BufferedInputStream(urlConn.getInputStream());
            BufferedReader bufReader=new BufferedReader(new InputStreamReader(inBuf, "utf-8"));

            String strLine=null;
            String strPage="";
            while((strLine=bufReader.readLine())!=null){
                strPage+=strLine;
            }

            xpp.setInput(new StringReader(strPage));

            int eventType=xpp.getEventType();

            while(eventType!=XmlPullParser.END_DOCUMENT){
                if(eventType==XmlPullParser.START_DOCUMENT){
                    ;
                }else if(eventType==XmlPullParser.START_TAG){
                    String tag_name=xpp.getName();

                    //Toast.makeText(MapsActivity.this, tag_name, Toast.LENGTH_SHORT).show();

                    if(tag_name.equals("resultCode")) b_resultCode=true;
                    if(tag_name.equals("routeId")) b_routeId=true;
                    if(tag_name.equals("routeName")) b_routeName=true;
                }else if(eventType==XmlPullParser.TEXT){
                    if(b_resultCode){
                        strResultCode=xpp.getText();
                        b_resultCode=false;
                    }
                    if(strResultCode.equals("0")){
                        if(b_routeId){
                            //Toast.makeText(MapsActivity.this, routeId+"//"+str, Toast.LENGTH_SHORT).show();

                            if(xpp.getText().equals(str)) {
                                routeId2=xpp.getText();
                                //Toast.makeText(MapsActivity.this, routeId2+"//"+str, Toast.LENGTH_SHORT).show();

                            }
                            b_routeId = false;
                        }
                        if(b_routeName){
                            if(routeId2.equals(str)) {
                                routeName2=xpp.getText();
                                flag=true;
                            }
                            b_routeName=false;
                        }
                    }
                }else if(eventType==XmlPullParser.END_TAG){
                    ;
                }
                if(flag)
                    return routeName2;
                eventType=xpp.next();
            }
        }catch(Exception e){
            e.getMessage();
        }

        return routeName2; // 버스번호 돌려준다.
    }

}
