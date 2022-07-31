package com.example.gps_bus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class bussub extends AppCompatActivity {
    private String strServiceUrl;
    private String strServiceKey;
    private String busRouteId;
    private String strUrl;
    private TextView objTV;

    TextView second_gameTitle, second_gameReleaseDate;
    Button returnBtn;
    Toast toast;
    ListView listview2;

    //데이터를 저장하게 되는 리스트
    List<String> list2 = new ArrayList<>();

    //리스트뷰와 리스트를 연결하기 위해 사용되는 어댑터
    ArrayAdapter<String> adapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bussub);

        //Textview
        //objTV = (TextView) findViewById(R.id.txtTitle);

        second_gameTitle = (TextView) findViewById(R.id.second_gameTitle);
        second_gameReleaseDate = (TextView) findViewById(R.id.second_gameReleaseDate);
        returnBtn = (Button)findViewById(R.id.returnBtn);

        Intent intent = getIntent();
        String gameTitle = intent.getStringExtra("gameTitle");

        second_gameTitle.setText("선택하신 값 : "+ gameTitle);


        //버스-----------------------------------------------------------------------------------

        listview2 = (ListView)findViewById(R.id.listview2);

        //리스트뷰와 리스트를 연결하기 위해 사용되는 어댑터
        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list2);

        //리스트뷰의 어댑터를 지정해준다.
        listview2.setAdapter(adapter2);
        listview2.setChoiceMode(ListView.CHOICE_MODE_SINGLE);



        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list2);
        adapter2.clear();
        strServiceUrl = "http://ws.bus.go.kr/api/rest/busRouteInfo/getStaionByRoute";
        strServiceKey = "Fxbs0rRnk2f9R%2BOOz4IzsfeskCb8AIJ8boMMffE%2BVkN3gXI5jL7Z71%2BWsHS485W42XURAD9hoQhkRrEUSziyiA%3D%3D";
        busRouteId = gameTitle;
        strUrl = strServiceUrl + "?serviceKey=" + strServiceKey + "&busRouteId=" + busRouteId;

        //Toast.makeText(getApplicationContext(), "시작", Toast.LENGTH_SHORT).show();
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
            //Toast.makeText(getApplicationContext(), "도착", Toast.LENGTH_SHORT).show();
            //objTV.setText(result);

            //버스
            String strHeaderCd = "";
            String strStation = "";
            String strStationNm = "";

            boolean bSet_headerCd = false;
            boolean bSet_station = false;
            boolean bSet_stationNm = false;

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

                        //버스
                        if(tag_name.equals("headerCd")) bSet_headerCd = true;
                        if(tag_name.equals("station")) bSet_station = true;
                        if(tag_name.equals("stationNm")) bSet_stationNm = true;

                    }else if(eventType == XmlPullParser.TEXT){
                        //버스
                        if(bSet_headerCd){
                            strHeaderCd = xpp.getText();
                            bSet_headerCd = false;
                        }

                        if(strHeaderCd.equals("0")){
                            if(bSet_station){
                                strStation = xpp.getText();
                                bSet_station = false;
                            }
                            if(bSet_stationNm){
                                strStationNm = xpp.getText();
                                list2.add(strStationNm);
                                bSet_stationNm = false;
                            }
                        }

                    }else if(eventType == XmlPullParser.END_TAG){
                        ;
                    }
                    eventType = xpp.next();
                }
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
}