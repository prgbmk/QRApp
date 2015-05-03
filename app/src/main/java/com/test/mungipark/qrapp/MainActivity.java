package com.test.mungipark.qrapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends Activity {

    private TextView tvResult;
    private String result;//DB결과값


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpView();
    }


    private void setUpView() {
        // TODO Auto-generated method stub
        tvResult = (TextView) this.findViewById(R.id.textViewResult);
        Button btnScanQRCode = (Button) this
                .findViewById(R.id.buttonScanQrCode);
        Button DBbtn = (Button) this.findViewById(R.id.DBbtn);
        Button Showbtn = (Button) this.findViewById(R.id.showbtn);

        //DB정보 쳐넣어
        DBbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new inputDB().execute();//DB접근 스레드 시작.
                tvResult.setText(result);//처리된 Php결과문 표시
                Log.d("TextView 갱신", "너냐?");
            }
        });

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
                tvResult.setText(result);
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        IntentResult scanResult = IntentIntegrator.parseActivityResult(
                requestCode, resultCode, data);
        if (scanResult != null) {

            // handle scan result
            String contantsString = scanResult.getContents() == null ? "0"
                    : scanResult.getContents();
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

    private class inputDB extends AsyncTask<Void, Void, String> {

        @Override
        // 백그라운드에서 작업할 작업을 지시함
        protected String doInBackground(Void... params) {
            String output;
            Log.d("백그라운드 작업", "실행완료");
            output = InsertData();
            return output;
        }

        protected void onPostExecute(String temp){


        }
    }

    private class showDB extends AsyncTask<Void, Void, String>{

        protected String doInBackground(Void... params) {
            String output;
            Log.d("읽기 작업", "실행완료");
            output = ShowData();
            return output;
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
        URL url = null;
        try {
            url = new URL("http://192.168.0.104/insert_menu.php");

            HttpURLConnection http = (HttpURLConnection) url.openConnection();//php접속

            http.setDefaultUseCaches(false);
            http.setDoInput(true);//서버 읽기 모드
            http.setDoOutput(true);//서버 쓰기 모드
            http.setRequestMethod("POST");//POST방식 전송(보안용)
            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");

            //php에 파라미터 넘겨주는 작업 시작.
            StringBuffer buffer = new StringBuffer();
            buffer.append("date").append("=").append("20150503").append("&");
            buffer.append("name").append("=").append("알레르기약").append("&");
            buffer.append("number").append("=").append(30).append("&");
            buffer.append("type").append("=").append(1);

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




        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){

        }

        return result;
    }
    //DB데이터 읽는 함수(해당 날짜의 데이터만 읽어오기) - AsyncTask 스레드에 탐재할 함수
    private String ShowData(){
        URL url = null;
        try {
            url = new URL("http://192.168.0.104/show_data_date.php");

            HttpURLConnection http = (HttpURLConnection) url.openConnection();//php접속

            http.setDefaultUseCaches(false);
            http.setDoInput(true);//서버 읽기 모드
            http.setDoOutput(true);//서버 쓰기 모드
            http.setRequestMethod("POST");//POST방식 전송(보안용)
            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");

            //php에 파라미터 넘겨주는 작업 시작.
            StringBuffer buffer = new StringBuffer();
            buffer.append("date").append("=").append("20150503");


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

            Log.d("DB Select결과 :", result);



        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){

        }

        return result;
    }

}

