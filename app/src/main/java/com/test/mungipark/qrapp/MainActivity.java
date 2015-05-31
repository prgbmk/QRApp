package com.test.mungipark.qrapp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends Activity {

    //UI들
    private TextView tvResult;

    //GCM 받기 위한 객체들
    private String SENDER_ID = "1077167000598";//프로젝트 API 생성 아이디.
    private GoogleCloudMessaging gcm;
    private String regid;

    //QR 코드에서 넘어오는 값(처방내용)을 처리해주기 위한 String 객체 - DB 내용추가 참고용
    private String QRdate, QRname[], QRnumber[], QRtype[];
    private int i;

    //커스텀된 ArrayList
    ArrayList<MydescriptionValue> descriptionValues;
    MydescriptionValue myvalue;//센서값 담는 객체(커스텀된거) - 그림, 값.
    ListView descriptionValuesList;

    //어댑터 준비
    MydescriptionValueAdapter Adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //GCM용 객체 초기화
        gcm = GoogleCloudMessaging.getInstance(this);
        registerInBackground();

        //커스텀 리스트뷰용 객체 선언.
        descriptionValues = new ArrayList<MydescriptionValue>();
        Adapter = new MydescriptionValueAdapter(this, R.layout.item, descriptionValues);

        setUpView();
    }

    //어댑터 리스트에 DB 내용 간추려 넣는 메소드
    private void setAdapter(String DB_Result){
        String enterTok[] = null;
        enterTok = DB_Result.split("\n");

        int i=1;
        while(i <enterTok.length){
            String starTok[] = enterTok[i].split("[*]");
            myvalue = new MydescriptionValue(starTok[0], starTok[1] + " " + starTok[2] + "개 남음");
            descriptionValues.add(myvalue);
            i++;
        }
        //모아진 어댑터 값을 ListView에 적용
        descriptionValuesList.setAdapter(Adapter);
    }

    //onCreate()메소드에 실을 초기화 메소드
    private void setUpView() {
        // TODO Auto-generated method stub
        tvResult = (TextView) this.findViewById(R.id.textViewResult);
        descriptionValuesList = (ListView)this.findViewById(R.id.descriptionListView);

        Button btnScanQRCode = (Button) this
                .findViewById(R.id.buttonScanQrCode);
        Button Showbtn = (Button) this.findViewById(R.id.showbtn);//DB 저장된 내용 보기


        //QR코드 인식하기위한 바코드 인식용 액티비티 생성
        btnScanQRCode.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                IntentIntegrator integrator = new IntentIntegrator(
                        MainActivity.this);
                integrator.initiateScan();

            }
        });
        Showbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new showDB().execute();
                //tvResult.setText(result);
            }
        });



    }
    //QR코드 결과값을 처리해주는 메소드 부분
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        IntentResult scanResult = IntentIntegrator.parseActivityResult(
                requestCode, resultCode, data);
        if (scanResult != null) {
            i=0;
            // 읽은 결과값 처리
            String contantsString = scanResult.getContents() == null ? "0"
                    : scanResult.getContents();
            String[] tmpStr;//처방정보 저장할 String 객체

            tmpStr = contantsString.split("\n");//칸바꾸기로 나눈 값을 다 저장해.
            QRdate = tmpStr[0];//날짜정보
            QRname = tmpStr[1].split(",");//처방약 정보
            QRnumber = tmpStr[2].split(",");
            QRtype = tmpStr[3].split(",");
            Log.d("All Info : ", contantsString);
            Log.d("tmp Info:", QRname[2]);
            Log.d("Date : ", QRdate);


            Log.d("name : ", QRname[i]);
            new inputDB().execute();//DB삽입 스레드 시작.


            tvResult.setText(contantsString);

            if (contantsString.equalsIgnoreCase("0")) {
                Toast.makeText(this, "콘텐츠 넘버 얻는데 문제가 있습니다.",
                        Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, contantsString, Toast.LENGTH_LONG).show();

            }

        } else {
            Toast.makeText(this, "QR 스캔에 문제가 있습니다.",
                    Toast.LENGTH_LONG).show();
        }
    }


    //약 정보 넣는 메소드 돌리는 스레드
    private class inputDB extends AsyncTask<Void, Void, String> {

        @Override
        // 백그라운드에서 작업할 작업을 지시함
        protected String doInBackground(Void... params) {
            String output;
            Log.d("Input DB 작업 : ", "실행완료");
            output = InsertData();
            return output;
        }

        protected void onPostExecute(String temp){

        }
    }

    //저장된 약 정보 보는 메소드 돌리는 스레드
    private class showDB extends AsyncTask<Void, Void, String>{

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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //DB데이터 쓰는 함수 - AsyncTask 스레드에 탐재할 함수
    private String InsertData(){
        String result = null;
        URL url = null;
        try {

            while(i<QRname.length) {
                url = new URL("http://119.199.154.131/insert_menu(Description).php");

                HttpURLConnection http = (HttpURLConnection) url.openConnection();//php접속

                http.setDefaultUseCaches(false);
                http.setDoInput(true);//서버 읽기 모드
                http.setDoOutput(true);//서버 쓰기 모드
                http.setRequestMethod("POST");//POST방식 전송(보안용)
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");

                //php에 파라미터 넘겨주는 작업 시작.
                StringBuffer buffer = new StringBuffer();
                buffer.append("date").append("=").append(QRdate).append("&");
                buffer.append("name").append("=").append(QRname[i]).append("&");
                buffer.append("number").append("=").append(Integer.valueOf(QRnumber[i])).append("&");
                buffer.append("type").append("=").append(Integer.valueOf(QRtype[i]));

                Log.d("Buffer date : ", QRdate);
                Log.d("Buffer name : ", QRname[i]);
                Log.d("Buffer number : ", QRnumber[i]);
                Log.d("Buffer type : ", QRtype[i]);

                //Php에 파라미터 값 넘기기
                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "UTF-8");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                //writer.close();
                Log.d("Write date : ", QRdate);
                Log.d("Write name : ", QRname[i]);
                Log.d("Write number : ", QRnumber[i]);
                Log.d("Write type : ", QRtype[i]);
                Log.d("int i: ", String.valueOf(i));
                i++;

                //파라미터값 넘기고나서 나오는 결과 받기
                InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuilder builder = new StringBuilder();
                String str;
                while((str = reader.readLine()) != null){
                    builder.append(str + "\n");
                }

                result = builder.toString();

            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){

        }

        return result;
    }
    //DB데이터 읽는 함수(해당 날짜의 데이터만 읽어오기) - AsyncTask 스레드에 탐재할 함수
    private String ShowData(){
        String result = null;//DB결과 받아서 반환할 객체
        URL url = null;
        try {
            //url = new URL("http://192.168.0.104/show_data_date.php");
            url = new URL("http://119.199.154.131/show_data.php");

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

    //GCM쓰기위한 메소드 부분들.
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    regid = gcm.register(SENDER_ID);
                    sendRegistrationIdToBackend();
                } catch (IOException ex) {
                }
                return "";
            }

            @Override
            protected void onPostExecute(String msg) {
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToBackend() {
        // Your implementation here.

        Log.d(null, "RegId = " + regid);
    }

}

//리스트뷰 출력 항목 클래스
class MydescriptionValue {
    String Date;
    String Description;

    //기본 생성자.
    MydescriptionValue(String Date, String Description){
        this.Date = Date;
        this.Description = Description;
    }
}

//BaseAdapter 인터페이스를 오버라이드하여 커스텀 구현
class MydescriptionValueAdapter extends BaseAdapter {

    Context con;
    LayoutInflater inflater;
    ArrayList<MydescriptionValue> arD;
    int layout;

    //기본 생성자
    public MydescriptionValueAdapter(Context con, int layout, ArrayList<MydescriptionValue> arD){
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
        TextView date = (TextView) convertView.findViewById(R.id.date);
        date.setText(arD.get(position).Date);

        TextView description = (TextView) convertView.findViewById(R.id.description);
        description.setText(arD.get(position).Description);

        return convertView;
    }
}



