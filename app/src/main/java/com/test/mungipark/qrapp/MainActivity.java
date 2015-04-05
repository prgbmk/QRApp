package com.test.mungipark.qrapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    private TextView tvResult;

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
        btnScanQRCode.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                IntentIntegrator integrator = new IntentIntegrator(
                        MainActivity.this);
                integrator.initiateScan();

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
}
