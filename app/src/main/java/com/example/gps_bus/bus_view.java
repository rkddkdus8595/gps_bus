package com.example.gps_bus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class bus_view extends AppCompatActivity {
    TextView txtRouteName1;
    RecyclerView mRecyclerView;
    ArrayList<inform> lst=null;
    inform ifrm=null;
    RecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_view);

        mRecyclerView=(RecyclerView)findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        Intent intent = getIntent();
        // str1=intent.getStringExtra("result"); 배열로 따로 안가지고 오고 문자열로 합쳐서 갖고올때

        ArrayList<String> arrlocationNo1 = intent.getStringArrayListExtra("arrlocationNo1");
        ArrayList<String> arrlocationNo2 = intent.getStringArrayListExtra("arrlocationNo2");
        ArrayList<String> arrlowPlate1 = intent.getStringArrayListExtra("arrlowPlate1");
        ArrayList<String> arrlowPlate2 = intent.getStringArrayListExtra("arrlowPlate2");
        ArrayList<String> arrpredictTime1 = intent.getStringArrayListExtra("arrpredictTime1");
        ArrayList<String> arrpredictTime2 = intent.getStringArrayListExtra("arrpredictTime2");
        ArrayList<String> arrRouteName = intent.getStringArrayListExtra("arrRouteName");
        ArrayList<String> arrRemainSeat1=intent.getStringArrayListExtra("arrRemainSeat1");
        ArrayList<String> arrRemainSeat2=intent.getStringArrayListExtra("arrRemainSeat2");
        ArrayList<String> arrFlag=intent.getStringArrayListExtra("arrFlag");


        lst=new ArrayList<inform>();
        for(int i=0; i<arrRouteName.size();i++){
            ifrm=new inform(arrRouteName.get(i)+"번",arrlowPlate1.get(i),arrlowPlate2.get(i),arrpredictTime1.get(i)+"분 후",arrpredictTime2.get(i)+"분",
                    arrlocationNo1.get(i)+"전 정류장",arrlocationNo2.get(i)+"전 정류장",
                    arrRemainSeat1.get(i)+"석",arrRemainSeat2.get(i)+"석",arrFlag.get(i));
            lst.add(ifrm);


        }
        adapter = new RecyclerAdapter(getApplicationContext(),lst);
        mRecyclerView.setAdapter(adapter);

    }
}