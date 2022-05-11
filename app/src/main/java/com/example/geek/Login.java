package com.example.geek;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends AppCompatActivity {
    EditText f1,f2;
    Button b1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        f1 = (EditText)findViewById(R.id.usernameform);
        f2 = (EditText)findViewById(R.id.passwordform);
        b1 = (Button) findViewById(R.id.button);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(f1.getText().toString().equals("")&&f2.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),
                            "Login Success",Toast.LENGTH_SHORT).show();
                    activitestf();

                }else {
                    Toast.makeText(getApplicationContext(),
                            "Login Failed",Toast.LENGTH_SHORT).show();
                }
            }
        });
        setuphyperlink();

    }

    private void setuphyperlink(){
        TextView hype = (TextView)findViewById(R.id.hyperlinktext);
        hype.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void activitestf(){
        Intent i = new Intent(Login.this, Dashboard.class);
        startActivity(i);

    }
}