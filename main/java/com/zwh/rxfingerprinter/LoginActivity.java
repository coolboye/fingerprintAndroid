package com.zwh.rxfingerprinter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    TextView errors;
    EditText user,password,ip;
    Button submit,register;
    String ip_str=null, user_str=null, password_str=null;

    StringBuilder response = new StringBuilder();

    public void find() {
        errors = (TextView) findViewById(R.id.text_errors);
        user = (EditText) findViewById(R.id.edit_user_login);
        password = (EditText) findViewById(R.id.edit_password_login);
        ip = (EditText) findViewById(R.id.edit_ip_login);
        submit = (Button) findViewById(R.id.btn_submit_login);
        register = (Button) findViewById(R.id.btn_register_login);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ip_str = getIntent().getStringExtra("ip");
        user_str = getIntent().getStringExtra("user");
        password_str = getIntent().getStringExtra("password");

        find();

        if (ip_str!=null && user_str!=null && password_str!=null) {
            user.setText(user_str);
            ip.setText(ip_str);
            password.setText(password_str);
        }

        //点击登录后 服务器返回lock_id
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user_str = user.getText().toString();
                String password_str = password.getText().toString();
                String ip_str = ip.getText().toString();
                sendRequestWithHttpURLConnection(ip_str, user_str, password_str);
                if (response.toString().length()>0) {
                    Intent it = new Intent(LoginActivity.this, ControlActivity.class);
                    it.putExtra("lock_id", response.toString());
                    it.putExtra("ip", ip_str);
                    startActivity(it);
                } else {
                    errors.setText("登录失败！");
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String ip_str2 = ip.getText().toString();
                Intent it2 = new Intent(LoginActivity.this, RegisterActivity.class);
                //it2.putExtra("ip", ip_str2);
                startActivity(it2);
            }
        });
    }

    //登录验证
    public void sendRequestWithHttpURLConnection (final String ip,final String user, final String password) {
        // 开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    //http://192.168.1.105:8000/fingerprint/login/?p1=user&p2=password
                    URL url = new URL("http://"+ip+":8000/fingerprint/login/?p1="
                            + user + "&p2=" + password);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(1000);
                    connection.setReadTimeout(1000);
                    InputStream in = connection.getInputStream();
                    // 下⾯面对获取到的输⼊入流进⾏行读取
                    BufferedReader reader = new BufferedReader(new
                            InputStreamReader(in));
                    response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

}
