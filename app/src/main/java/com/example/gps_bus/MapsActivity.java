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
    // ?????? ?????? ????????? ?????? stationId??? 200000078????????? ??????????????? ?????? ?????? ??????????????? ??????.
    private GoogleMap mMap;
    LocationManager locationManager;
    public String serviceUrl, serviceKey, url, url2; // ?????? url, ????????????, ?????? url

    Button btnArr, btnCorona,btn_busgo;

    ArrayList<Double> arrX = new ArrayList<Double>(); // x?????? ??????
    ArrayList<Double> arrY = new ArrayList<Double>(); // y?????? ??????
    ArrayList<String> arrStationName = new ArrayList<String>(); // ????????? ?????? ??????
    ArrayList<String> arrStationId = new ArrayList<String>(); // ????????? ID ??????

    SensorManager sensorM;
    Sensor sensor_light;

    Double d1 = 0.0;
    Double d2 = 0.0;
    public String stationId2="";
    public String routeId=""; // ?????? ID
    public String routeName=""; // ?????? ??????

    public String lowPlate1; // ?????????????????? ??????
    public String lowPlate2;
    public String predictTime1; // ???????????? ????????????
    public String predictTime2;
    public String locationNo1; // ????????? ??? ?????????
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

    ArrayList<String> arrName=new ArrayList<>(); // ?????? ??????
    ArrayList<String> arrCoronaX=new ArrayList<>(); // ?????? X??????
    ArrayList<String> arrCoronaY=new ArrayList<>();
    ArrayList<String> arrType=new ArrayList<>(); //??????: '01', ?????????: '02', ??????: '03'
    ArrayList<String> arrRemainStat=new ArrayList<>(); //100??? ??????(??????): 'plenty' / 30??? ?????? 100?????????(?????????): 'some' / 2??? ?????? 30??? ??????(?????????): 'few' / 1??? ??????(??????): 'empty' / ????????????: 'break'

    String routeId2=""; // ????????? ???????????? ???????????? ????????????
    String routeName2="";
    Integer first=0;
    boolean b_resultCode=false;
    boolean b_buslowPlate1=false; // ?????????????????? ??????
    boolean b_buslowPlate2=false;
    boolean b_predictTime1=false; // ???????????? ????????????
    boolean b_predictTime2=false;
    boolean b_locationNo1=false; // ????????? ??? ?????????
    boolean b_locationNo2=false;
    boolean b_flag=false; // ????????? ??????????????? ?????????
    boolean b_remainSeatCnt1=false; // ????????? ????????????
    boolean b_remainSeatCnt2=false;
    boolean b_routeId=false; // ?????? ID

    boolean marked=true;
    Double markerClick1=0.0;
    Double markerClick2=0.0;
    boolean markerClickFlag=false;
    Vibrator vibrator;
    int current_light=0;
    boolean current_auto_mode=false; // ?????????????????? ????????? ?????? ?????????
    WindowManager.LayoutParams parameter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Toast.makeText(this, mAuth.getUid(), Toast.LENGTH_SHORT).show(); ??? UID??? ?????? ?????? ?????? ??????????????? ????????????
        vibrator=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        parameter=getWindow().getAttributes();
        StrictMode.enableDefaults();
        sensorM=(SensorManager)getSystemService(SENSOR_SERVICE);
        sensor_light=sensorM.getDefaultSensor(Sensor.TYPE_LIGHT);
        btnArr=(Button)findViewById(R.id.btnArr);
        btnCorona=(Button)findViewById(R.id.btnCorona);
        btn_busgo=(Button)findViewById(R.id.btn_busgo);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //??? ????????? ????????? ????????? ?????? ??????

    }
    public void getLight(){ // ?????? ????????????
        try {
            current_light = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS); // ?????? ?????? ????????????
            if(android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE) == 1){ // ?????????????????? ????????????
                current_auto_mode=true; // ?????????????????? ??????????????? (????????? ?????? ???????????? ??? ???????????? ???????????? ?????????)
                android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, 0); // ??????????????? ?????? ?????? ????????? ????????? ?????????????????? ???????????????
            }
        } catch (Settings.SettingNotFoundException e) {
            Log.e("Error: "+e.getMessage(), null);
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        sensorM.registerListener(this, sensor_light, sensorM.SENSOR_DELAY_FASTEST); // ???????????? ????????????
    }

    @Override
    public void onPause(){
        super.onPause();
        sensorM.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) { // **********************??? ???????????? ??? ????????????????????? ???????????? ????????? ????????? ?????? ??????????????? ???????????? ????????????

        getLight();
        int light=(int)event.values[0];
        //System.out.println(light);
        if(light>=90000){ //??????????????????
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
                //Snackbar.make(View, "??????????????? ??????????????????", Snackbar.LENGTH_LONG).show();
                Toast.makeText(this, "???????????? ??????????????????.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "???????????? ??????????????????.", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this, "???????????? ??????????????????.", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1000);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1000);

            return;
        }

        mMap.setMyLocationEnabled(true); //???????????? ?????? ?????????

        mMap.setOnMyLocationButtonClickListener(this); // ???????????? ?????? ??????????????? !
        mMap.setOnMarkerClickListener(this); // ?????? ???????????? ???????????? ?????????
        mMap.setMapType(googleMap.MAP_TYPE_NORMAL);
        locationManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        btn_busgo.setOnClickListener(new Button.OnClickListener(){ // ??????/????????? ?????? ???????????????

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);//???????????? ?????????
            }
        });
        final LocationListener locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) { // provider??? ????????? ??? ?????????

                d1=location.getLatitude(); // ?????? Y
                d2=location.getLongitude(); // ?????? X
                if(first==0){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(d1,d2),16)); // ?????? ?????????????????? ?????? ?????? ??????????????? ?????????
                }
                if(markerClickFlag){
                    marked=true;
                    //Toast.makeText(MapsActivity.this, "d1="+(d1+0.001)+"/d2="+(d2+0.001)+"/d3="+d3+"/d4="+d4, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(MapsActivity.this, "d1="+(d1-0.001)+"/d2="+(d2-0.001)+"/d3="+d3+"/d4="+d4, Toast.LENGTH_SHORT).show();
                    // ?????? ????????? ??????????
                    if ((d1+0.0025 > markerClick1 || d1-0.0025 > markerClick1) && (d2+0.0025 > markerClick2 || d2-0.0025 >markerClick2) && marked == true){
                        vibrator.vibrate(3000);
                        new AlertDialog.Builder(MapsActivity.this)
                                .setMessage("???????????????! ???????????????")
                                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which){
                                        Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show(); // ????????? ??????
                                        markerClickFlag=false;
                                        vibrator.cancel();
                                    }
                                })
                                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which){
                                        Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show(); // ????????? ??????
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
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(d1,d2),16)); // ?????? ?????????????????? ?????? ?????? ??????????????? ?????????
                /*
                serviceUrl="http://openapi.gbis.go.kr/ws/rest/busstationservice/searcharound";
                serviceKey="4Ya%2FB8dRAQB8gaH%2Bskm2NIceS5b07ntqlwwk66zaizqNUduNGYB9ms2%2FrtEyD%2FAlsNNk9Cir3J%2FMa%2B4JANVodA%3D%3D";
                url=serviceUrl+"?serviceKey="+serviceKey+"&x="+d2+"&y="+d1;

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(d1,d2),16)); // ?????? ?????????????????? ?????? ?????? ??????????????? ?????????
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(d1,d2),16)); // ?????? ???????????? ????????????.
                arrX.clear(); //?????? ??????????????? ????????? ????????????
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
    public boolean onMyLocationButtonClick() { // ?????? ??????????????? ?????? ????????? ?????? ????????? ??????????????? ??????????????? ?????? ?????? ????????? ????????? !
        serviceUrl="http://openapi.gbis.go.kr/ws/rest/busstationservice/searcharound";
        serviceKey="4Ya%2FB8dRAQB8gaH%2Bskm2NIceS5b07ntqlwwk66zaizqNUduNGYB9ms2%2FrtEyD%2FAlsNNk9Cir3J%2FMa%2B4JANVodA%3D%3D";
        url=serviceUrl+"?serviceKey="+serviceKey+"&x="+d2+"&y="+d1;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(d1,d2),16)); // ?????? ?????????????????? ?????? ?????? ??????????????? ?????????
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(d1,d2),16)); // ?????? ???????????? ????????????.
        arrX.clear(); //?????? ??????????????? ????????? ????????????
        arrY.clear();
        arrStationName.clear();


        new DownloadWebpageTask().execute(url);
        return false;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) { // ??? ????????? ?????? ???????????? ????????? ???????????????
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
                startActivity(intent);//???????????? ?????????
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
                type123="??????";
            }else if(arrType.get(idx).equals("02")){
                type123="?????????";
            }else if(arrType.get(idx).equals("03")){
                type123="??????";
            }

            switch (arrRemainStat.get(idx)) {
                case "plenty" : {
                    number123 = "100?????????";
                    break;
                }
                case "some" : {
                    number123 = "30??? ?????? 100??? ??????";
                    break;
                }
                case "few" : {
                    number123 = "2??? ?????? 30??? ??????";
                    break;
                }
                case "empty" : {
                    number123 = "1??? ??????";
                    break;
                }
            }

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions
                    .position(new LatLng(Double.parseDouble(arrCoronaX.get(idx)),Double.parseDouble(arrCoronaY.get(idx))))
                    .title(arrName.get(idx))
                    .snippet(type123+"/"+number123);
            if(type123.equals("??????")) {
                BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.medicine);
                Bitmap b = bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 80, 80, false);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            }else if(type123.equals("?????????")){
                BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.postal);
                Bitmap b = bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 80, 80, false);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            }else if(type123.equals("??????")){
                BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.nonghyup);
                Bitmap b = bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 80, 80, false);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            }
            mMap.addMarker(markerOptions);

        }

    }
    public String getBusArrive(){ // ??? ???????????? ???????????? ????????? ?????? ????????? ?????? ???
        serviceUrl="http://openapi.gbis.go.kr/ws/rest/busarrivalservice/station";
        serviceKey="4Ya%2FB8dRAQB8gaH%2Bskm2NIceS5b07ntqlwwk66zaizqNUduNGYB9ms2%2FrtEyD%2FAlsNNk9Cir3J%2FMa%2B4JANVodA%3D%3D";
        url2=serviceUrl+"?serviceKey="+"1234567890"+"&stationId="+stationId2;
        String result="";

        String strResultCode="";
        b_resultCode=false;
        b_buslowPlate1=false; // ?????????????????? ??????
        b_buslowPlate2=false;
        b_predictTime1=false; // ???????????? ????????????
        b_predictTime2=false;
        b_locationNo1=false; // ????????? ??? ?????????
        b_locationNo2=false;
        b_remainSeatCnt1=false; // ???????????? (?????????????????????)??? ???????????? ???
        b_remainSeatCnt2=false;
        b_flag=false; // ?????? ?????? (RUN:?????????, PASS:?????????, STOP:????????????, WAIT:???????????????)
        b_routeId=false; // ?????? ID

        // ??? ?????? ????????? ??????????????? ?????????????????? ???????????? ???????????? ?????????
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

            URL url= new URL(url2);//???????????? ??? ?????? url??? URL ????????? ??????.
            InputStream is= url.openStream(); //url????????? ??????????????? ??????
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

                    //Toast.makeText(MapsActivity.this, "????????????2", Toast.LENGTH_SHORT).show();
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
                            result=result+"????????????1: "+xpp.getText();
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
                            result=result+"/ ????????????2: "+xpp.getText();
                        }
                        if(b_predictTime1){
                            predictTime1=xpp.getText();
                            arrpredictTime1.add(xpp.getText());
                            b_predictTime1=false;
                            result=result+"/ ????????????1 : "+xpp.getText();
                        }
                        if(b_predictTime2){
                            predictTime2=xpp.getText();
                            arrpredictTime2.add(xpp.getText());
                            b_predictTime2=false;
                            result=result+"/ ???????????? 2: "+xpp.getText();
                        }
                        if(b_locationNo1){
                            locationNo1=xpp.getText();
                            arrlocationNo1.add(xpp.getText());
                            b_locationNo1=false;
                            result=result+"/ ??????1: "+xpp.getText();
                        }
                        if(b_locationNo2){
                            locationNo2=xpp.getText();
                            arrlocationNo2.add(xpp.getText());
                            b_locationNo2=false;
                            result=result+"/ ??????2: "+xpp.getText();
                        }
                        if(b_routeId){
                            routeId=xpp.getText();
                            b_routeId=false;
                            routeName=getBusName(routeId);
                            arrRouteName.add(routeName);
                            result=result+"/ ????????????: "+routeName;
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
                return "???????????? ?????? !";
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
                        .position(new LatLng(arrY.get(idx), arrX.get(idx))) // ???????????? ?????? ?????? ????????? ????????? ??????????????? ???????????? ????????? !
                        .title(arrStationName.get(idx))
                        .snippet(arrStationId.get(idx)); // ????????? ?????? ???????????? ????????? ?????? ??? ???????????? ??????
                //Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bus),200,200,false);
                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.busmain);
                Bitmap b=bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 80, 80, false);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                // 2. ?????? ?????? (????????? ?????????)
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

    public String getBusName(String str){ // ??? ???????????? ?????? ID??? ?????? ????????????(routeName)??? ???????????? ??????????????? ???

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

            URL url= new URL(url2);//???????????? ??? ?????? url??? URL ????????? ??????.
            InputStream is= url.openStream(); //url????????? ??????????????? ??????
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

        return routeName2; // ???????????? ????????????.
    }

}
