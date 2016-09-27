package org.self.vendorapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Launcher extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Button locateMe = (Button) findViewById(R.id.btLocate);
        Button locateManual = (Button) findViewById(R.id.btManual);
        locateMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Launcher.this,HomeScreen.class);
                startActivity(intent);
                finish();
            }
        });
        locateManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Launcher.this,"Under Construction",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
