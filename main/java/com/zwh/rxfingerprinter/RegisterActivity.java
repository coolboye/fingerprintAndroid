package com.zwh.rxfingerprinter;

import android.content.Intent;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {

    EditText user, lock_id, password, password_again, ip;
    Button submit, retun;
    TextView error;
    String user_str, lock_id_str, password_str, password_again_str, ip_str;
    public static final int SHOW_RESPONSE = 0;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_RESPONSE:
                    String response_lock = (String) msg.obj;
                    // 在这⾥里进⾏行UI操作，将结果显⽰示到界⾯面上
                    //Log.d("boye",response_lock);
                    if (response_lock.equals("ok")) {
                        error.setText("注册成功！");
                    } else {
                        error.setText("注册失败！");
                    }
            }
        }
    };

    public void find() {
        user = (EditText) findViewById(R.id.edit_user_register);
        lock_id = (EditText) findViewById(R.id.edit_lock_register);
        password = (EditText) findViewById(R.id.edit_password_register);
        password_again = (EditText) findViewById(R.id.edit_password_register);
        ip  = (EditText) findViewById(R.id.edit_ip_register);
        submit = (Button) findViewById(R.id.btn_submit_register);
        retun = (Button) findViewById(R.id.btn_return_reister);
        error = (TextView) findViewById(R.id.errors_register);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        find();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ip_str = ip.getText().toString();
                user_str = user.getText().toString();
                lock_id_str = lock_id.getText().toString();
                password_str = password.getText().toString();
                password_again_str = password_again.getText().toString();
                Log.d("boye",ip_str);
                if (password_str.equals(password_again_str)) {
                    sendRequestWithHttpURLConnection(ip_str, user_str, lock_id_str, password_str);
                }
            }
        });

        retun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(RegisterActivity.this, LoginActivity.class);
                it.putExtra("ip", ip_str);
                it.putExtra("user", user_str);
                it.putExtra("password", password_str);
                startActivity(it);
            }
        });
    }

    public void sendRequestWithHttpURLConnection (final String ip, final String user,
                                                  final String lock_id, final String password) {
        // 开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    //http://192.168.1.105:8000/fingerprint/register/?p1=user&p2=lock_id&p3=password
                    URL url = new URL("http://"+ip+":8000/fingerprint/register/?p1="
                            + user + "&p2=" + lock_id + "&p3=" + password);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(1000);
                    connection.setReadTimeout(1000);
                    InputStream in = connection.getInputStream();
                    // 下⾯面对获取到的输⼊入流进⾏行读取
                    BufferedReader reader = new BufferedReader(new
                            InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    Message message = new Message();
                    message.what = SHOW_RESPONSE;
                    // 将服务器返回的结果存放到Message中
                    message.obj = response.toString();
                    handler.sendMessage(message);
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
