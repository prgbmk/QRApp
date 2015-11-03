package com.test.mungipark.qrapp;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class activity_sideeffect extends Activity {

    //커스텀된 ArrayList
    private ArrayList<MysideeffectValue> sideeffectValues;

    MysideeffectValue myvalue;//센서값 담는 객체(커스텀된거) - 그림, 값.

    ListView sideeffectValuesList;
    ListView sideeffectTitleValuesList;

    //어댑터 준비
    private MysideeffectValueAdapter Adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_sideeffect);

        Button backBtn = (Button)this.findViewById(R.id.backBtn_side);
        sideeffectValuesList = (ListView)this.findViewById(R.id.sideeffect_listView);
        sideeffectTitleValuesList = (ListView)this.findViewById(R.id.sideeffect_title_listView);
        sideeffectValues = new ArrayList<MysideeffectValue>();
        Adapter = new MysideeffectValueAdapter(this, R.layout.sideeffectitem, sideeffectValues);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        new showDB().execute();


    }

    //DB데이터 읽는 함수(해당 날짜의 데이터만 읽어오기) - AsyncTask 스레드에 탐재할 함수
    private String ShowData(){//부작용을 봅니다.
        String result = null;//DB결과 받아서 반환할 객체
        URL url = null;
        try {
            //url = new URL("http://192.168.0.104/show_data_date.php");
            url = new URL("http://121.156.24.248/show_data(sideeffect).php");

            HttpURLConnection http = (HttpURLConnection) url.openConnection();//php접속

            http.setDefaultUseCaches(false);
            http.setDoInput(true);//서버 읽기 모드
            http.setDoOutput(true);//서버 쓰기 모드
            http.setRequestMethod("POST");//POST방식 전송(보안용)
            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");

            //php에 파라미터 넘겨주는 작업 시작.
            StringBuffer buffer = new StringBuffer();
            //buffer.append("date").append("=").append("20150504");


            //Php에 파라미터 값 넘기기
            OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "UTF-8");
            PrintWriter writer = new PrintWriter(outStream);
            writer.write(buffer.toString());
            writer.flush();

            //파라미터값 넘기고나서 나오는 결과 받기
            InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "UTF-8");
            BufferedReader reader = new BufferedReader(tmp);
            StringBuilder builder = new StringBuilder();
            String str;
            while((str = reader.readLine()) != null){
                builder.append(str + "\n");
            }

            result = builder.toString();

            Log.d("Show_Data.php :", result);



        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){

        }

        return result;
    }

    //저장된 부작용 정보 보는 메소드 돌리는 스레드
    private class showDB extends AsyncTask<Void, Void, String> {

        protected String doInBackground(Void... params) {
            String output;
            Log.d("Show DB스레드 작업 : ", "실행완료");
            output = ShowData();
            return output;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //tvResult.setText(s);
            //커스텀 리스트뷰에 DB 결과를 받아와 저장함.
            setAdapter(s);
        }

        //DB에서 받은내용을 간추리는 메소드
        private void setAdapter(String DB_Result) {
            String enterTok[] = null;
            enterTok = DB_Result.split("\n");

            if(!sideeffectValues.isEmpty())
                sideeffectValues.clear();//새로운 내용 올 때 갱신용
            int i = 1;
            while (i < enterTok.length) {
                String starTok[] = enterTok[i].split("[*]");
                myvalue = new MysideeffectValue(starTok[0], starTok[1], starTok[2]);

                sideeffectValues.add(myvalue);
                i++;
                Log.d("data:", DB_Result);
            }
            //모아진 어댑터 값을 ListView에 적용
            sideeffectValuesList.setAdapter(Adapter);
        }
    }
}

//리스트뷰 출력 항목 클래스
class MysideeffectValue {
    String Date;
    String Description;
    String Sideeffect;//부작용

    //기본 생성자.
    MysideeffectValue(String Date, String Description, String Sideeffect){
        this.Date = Date;
        this.Description = Description;
        this.Sideeffect = Sideeffect;
    }
}

//BaseAdapter 인터페이스를 오버라이드하여 커스텀 구현
class MysideeffectValueAdapter extends BaseAdapter {

    Context con;
    LayoutInflater inflater;
    ArrayList<MysideeffectValue> arD;
    int layout;

    //기본 생성자
    public MysideeffectValueAdapter(Context con, int layout, ArrayList<MysideeffectValue> arD){
        this.con = con;
        this.inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.arD = arD;
        this.layout = layout;
    }

    //어댑터 몇 개의 항목이 있는지 확인
    @Override
    public int getCount() {
        return arD.size();
    }

    //Position 위치의 항목 Value 반환
    @Override
    public Object getItem(int position) {
        return arD.get(position).Date;
    }

    //Position 위치의 ID 반환
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(layout, parent, false);
        }
        TextView date = (TextView) convertView.findViewById(R.id.date_side_txt);
        date.setText(arD.get(position).Date);

        TextView description = (TextView) convertView.findViewById(R.id.description_side_txt);
        description.setText(arD.get(position).Description);

        TextView sideeffect = (TextView) convertView.findViewById(R.id.sideeffect_txt);
        sideeffect.setText(arD.get(position).Sideeffect);

        return convertView;
    }
}
