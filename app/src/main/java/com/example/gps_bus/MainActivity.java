package com.example.gps_bus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TabHost myTabHost = null;
    TabHost.TabSpec myTabSpec;
    ListView listview;
    ListView listview2;

    private TextView objTV;
    private TextView objTV2;
    private EditText edTV;
    private EditText edTV2;
    private String strServiceUrl;
    private String strServiceKey;
    private Editable stSrch;
    private String strUrl;
    private Editable keyword;

    private String stationId;

    //데이터를 저장하게 되는 리스트
    List<String> list = new ArrayList<>();
    List<String> list2 = new ArrayList<>();

    //리스트뷰와 리스트를 연결하기 위해 사용되는 어댑터
    ArrayAdapter<String> adapter;
    ArrayAdapter<String> adapter2;

    public String serviceUrl, serviceKey, url, url2; // 기본 url, 서비스키, 통합 url

    ArrayList<Double> arrX = new ArrayList<Double>(); // x좌표 배열
    ArrayList<Double> arrY = new ArrayList<Double>(); // y좌표 배열
    ArrayList<String> arrStationName = new ArrayList<String>(); // 정류장 이름 배열
    ArrayList<String> arrStationId = new ArrayList<String>(); // 정류장 ID 배열

    public static String routeId=""; // 노선 ID
    public static String routeName=""; // 노선 번호
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

    public static String routeId2=""; // 얘네는 버스번호 조회할때 쓰이는거
    public static String routeName2="";
    public String selected_item="";
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
    List<String> busCode = new ArrayList<>();
    List<String> stationCode = new ArrayList<>();
    WindowManager.LayoutParams parameter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Textview
        //objTV = (TextView) findViewById(R.id.txtTitle);

        //Tab
        myTabHost = (TabHost)findViewById(R.id.tabhost);
        myTabHost.setup();
        myTabSpec = myTabHost.newTabSpec("정류장")
                .setIndicator("정류장")
                .setContent(R.id.tab1);
        myTabHost.addTab(myTabSpec);

        myTabSpec = myTabHost.newTabSpec("버스")
                .setIndicator("버스")
                .setContent(R.id.tab2);
        myTabHost.addTab(myTabSpec);
        myTabHost.setCurrentTab(0);

        //정류장-----------------------------------------------------------------------------------
        listview = (ListView)findViewById(R.id.listview);

        //리스트뷰와 리스트를 연결하기 위해 사용되는 어댑터
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);

        //리스트뷰의 어댑터를 지정해준다.
        listview.setAdapter(adapter);
        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        //리스트뷰 클릭이벤트
        listview.setOnItemClickListener(itemClickListenerStation);



        //버스-----------------------------------------------------------------------------------
        listview2 = (ListView)findViewById(R.id.listview2);

        //리스트뷰와 리스트를 연결하기 위해 사용되는 어댑터
        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list2);

        //리스트뷰의 어댑터를 지정해준다.
        listview2.setAdapter(adapter2);
        listview2.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        //리스트뷰 클릭이벤트
        listview2.setOnItemClickListener(itemClickListenerBus);

    }


    private AdapterView.OnItemClickListener itemClickListenerBus = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final int pos = position;
            String selectedGame = busCode.get(pos);
            String selectBusStation = (String)parent.getAdapter().getItem(position);
            Intent intent = new Intent(getBaseContext(),bussub.class);
            intent.putExtra("gameTitle", selectedGame); intent.putExtra("gameIndex", position);
            startActivity(intent);
        }
    };

    private AdapterView.OnItemClickListener itemClickListenerStation = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selected_item = (String)parent.getItemAtPosition(position);
            final int pos2 = position;
            stationId = stationCode.get(pos2);
            //Toast.makeText(MainActivity.this, selected_item, Toast.LENGTH_SHORT).show();
            strServiceUrl = "http://openapi.gbis.go.kr/ws/rest/busarrivalservice/station";
            strServiceKey = "Fxbs0rRnk2f9R%2BOOz4IzsfeskCb8AIJ8boMMffE%2BVkN3gXI5jL7Z71%2BWsHS485W42XURAD9hoQhkRrEUSziyiA%3D%3D";
            //stationId = selected_item;
            strUrl = strServiceUrl + "?serviceKey=" + 1234567890 + "&stationId=" + stationId;

            String result3=getBusArrive();
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
            startActivity(intent);//액티비티 띄우기
        }
    };

    public void SSearch(View view){
        edTV = (EditText) findViewById(R.id.editText);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        adapter.clear();
        strServiceUrl = "http://ws.bus.go.kr/api/rest/stationinfo/getStationByName";
        strServiceKey = "Fxbs0rRnk2f9R%2BOOz4IzsfeskCb8AIJ8boMMffE%2BVkN3gXI5jL7Z71%2BWsHS485W42XURAD9hoQhkRrEUSziyiA%3D%3D";
        stSrch = edTV.getText();
        strUrl = strServiceUrl + "?serviceKey=" + strServiceKey + "&stSrch=" + stSrch;

        new DownloadWebpageTask().execute(strUrl);

    }

    public void BSearch(View view){
        edTV2 = (EditText) findViewById(R.id.editText2);
        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list2);
        adapter2.clear();
        strServiceUrl = "http://openapi.gbis.go.kr/ws/rest/busrouteservice";
        strServiceKey = "Fxbs0rRnk2f9R%2BOOz4IzsfeskCb8AIJ8boMMffE%2BVkN3gXI5jL7Z71%2BWsHS485W42XURAD9hoQhkRrEUSziyiA%3D%3D";
        keyword = edTV2.getText();
        strUrl = strServiceUrl + "?serviceKey=" + strServiceKey + "&keyword=" + keyword;

        new DownloadWebpageTask().execute(strUrl);
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try{
                return (String)downloadUrl((String)urls[0]);
            }catch (IOException e) {
                return "Fail download!";
            }
        }

        protected void onPostExecute(String result) {
            //objTV.setText(result);
            //정류장
            String strHeaderCd = "";
            String strStId = "";
            String strStNm = "";

            boolean bSet_HeaderCd = false;
            boolean bSet_stId = false;
            boolean bSet_stNm = false;

            //버스
            String strResultCode = "";
            String strRouteId = "";
            String strRouteName = "";

            boolean bSet_resultCode = false;
            boolean bSet_routeId = false;
            boolean bSet_routeName = false;

            //기타
            Double doubleX=0.0;
            Double doubleY=0.0;
            String strStationId="";
            String strStationName="";

            boolean b_resultCode=false;
            boolean b_busX=false;
            boolean b_busY=false;
            boolean b_StationId=false;
            boolean b_StationName=false;

            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(result));
                int eventType = xpp.getEventType();
                while(eventType != XmlPullParser.END_DOCUMENT){
                    if(eventType == XmlPullParser.START_DOCUMENT){
                        ;
                    }else if(eventType == XmlPullParser.START_TAG){
                        String tag_name = xpp.getName();

                        //정류장
                        if(tag_name.equals("headerCd")) bSet_HeaderCd = true;
                        if(tag_name.equals("stId")) bSet_stId = true;
                        if(tag_name.equals("stNm")) bSet_stNm = true;

                        //버스
                        if(tag_name.equals("resultCode")) bSet_resultCode = true;
                        if(tag_name.equals("routeId")) bSet_routeId = true;
                        if(tag_name.equals("routeName")) bSet_routeName = true;

                        //기타
                        if(tag_name.equals("resultCode")) b_resultCode=true;
                        if(tag_name.equals("x")) b_busX=true;
                        if(tag_name.equals("y")) b_busY=true;
                        if(tag_name.equals("stationId")) b_StationId=true;
                        if(tag_name.equals("stationName")) b_StationName=true;

                    }else if(eventType == XmlPullParser.TEXT){
                        // 정류장
                        if(bSet_HeaderCd){
                            strHeaderCd = xpp.getText();
                            bSet_HeaderCd = false;
                        }

                        if(strHeaderCd.equals("0")){
                            if(bSet_stId){
                                strStId = xpp.getText();
                                //list.add(strStId);
                                stationCode.add(strStId);
                                bSet_stId = false;
                            }
                            if(bSet_stNm){
                                strStNm = xpp.getText();
                                list.add(strStNm);
                                bSet_stNm = false;
                            }
                        }

                        //버스
                        if(bSet_resultCode){
                            strResultCode = xpp.getText();
                            bSet_resultCode = false;
                        }

                        if(strResultCode.equals("0")){
                            if(bSet_routeId){
                                strRouteId = xpp.getText();
                                //list2.add(strRouteId);
                                busCode.add(strRouteId);
                                bSet_routeId = false;
                            }
                            if(bSet_routeName){
                                strRouteName = xpp.getText();
                                list2.add(strRouteName);
                                bSet_routeName = false;
                            }
                        }

                        //기타
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

                    }else if(eventType == XmlPullParser.END_TAG){
                        ;
                    }
                    eventType = xpp.next();
                }
                //정류장
                adapter.notifyDataSetChanged();
                listview.setAdapter(adapter);

                //버스
                adapter2.notifyDataSetChanged();
                listview2.setAdapter(adapter2);
            } catch (Exception e) {
                ;
            }
        }

        private String downloadUrl(String myurl) throws IOException {
            HttpURLConnection urlConn = null;

            try {
                URL url = new URL(myurl);
                urlConn =  (HttpURLConnection) url.openConnection();
                BufferedInputStream inBuf = new BufferedInputStream(urlConn.getInputStream());
                BufferedReader bufReader = new BufferedReader(new InputStreamReader(inBuf, "utf-8"));

                String strLine = null;
                String strPage = "";
                while ((strLine = bufReader.readLine()) != null){
                    strPage += strLine;
                }
                return strPage;
            } finally {
                urlConn.disconnect();
            }
        }
    }


    public String getBusArrive(){ // 이 메소드는 도착정보 조회를 위해 만들어 놓은 것
        serviceUrl="http://openapi.gbis.go.kr/ws/rest/busarrivalservice/station";
        serviceKey="1234567890";
        url2=serviceUrl+"?serviceKey="+"1234567890"+"&stationId="+stationId;
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

    public String getBusName(String str){ // 이 메소드는 노선 ID를 통해 버스번호(routeName)을 찾기위해 만들어놓은 것
        HttpURLConnection urlConn=null;
        serviceUrl ="http://openapi.gbis.go.kr/ws/rest/busstationservice/route";
        serviceKey ="1234567890";
        url2 = serviceUrl+"?serviceKey="+serviceKey+"&stationId="+stationId;
        //Toast.makeText(this, url2, Toast.LENGTH_SHORT).show();
        try{
            URL url= new URL(url2);//문자열로 된 요청 url을 URL 객체로
            // 생성.
            InputStream is= url.openStream(); //url위치로 입력스트림 연결
            urlConn=(HttpURLConnection)url.openConnection();
            urlConn.setRequestMethod("GET");
            BufferedInputStream inBuf=new BufferedInputStream(urlConn.getInputStream());
            BufferedReader bufReader=new BufferedReader(new InputStreamReader(inBuf, "utf-8"));

            String strLine=null;
            String strPage="";
            while((strLine=bufReader.readLine())!=null){
                strPage+=strLine;
            }
            boolean b_resultCode=false;
            boolean b_routeId=false;
            boolean b_routeName=false;
            boolean flag=false;
            String strResultCode="";

            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp=factory.newPullParser();

            xpp.setInput(new StringReader(strPage));
            //Toast.makeText(this, strPage, Toast.LENGTH_SHORT).show();
            int eventType=xpp.getEventType();
            while(eventType!=XmlPullParser.END_DOCUMENT){
                if(eventType==XmlPullParser.START_DOCUMENT){
                    ;
                }else if(eventType==XmlPullParser.START_TAG){
                    String tag_name=xpp.getName();
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

                            if(xpp.getText().equals(routeId)) {
                                routeId2=xpp.getText();
                                //Toast.makeText(MainActivity.this, routeId2+"//"+routeId, Toast.LENGTH_SHORT).show();

                            }
                            b_routeId = false;
                        }
                        if(b_routeName){
                            if(routeId2.equals(routeId)) {
                                routeName2=xpp.getText();
                                flag=true;
                                routeName=xpp.getText();
                                //Toast.makeText(MainActivity.this, routeId2+"//"+routeName, Toast.LENGTH_SHORT).show();

                            }
                            b_routeName=false;
                        }
                    }
                }else if(eventType==XmlPullParser.END_TAG){
                    ;
                }
                if(flag)
                    break;
                eventType=xpp.next();

            }

        }catch(Exception e){
            e.getMessage();
        }

        return routeName2; // 버스번호 돌려준다.
    }
}