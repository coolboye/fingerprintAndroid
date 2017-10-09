package com.zwh.rxfingerprinter;

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

public class ControlActivity extends AppCompatActivity {

    TextView state_text;
    Button update_btn,open_btn,finger_btn,clear_btn;
    public static final int SHOW_RESPONSE = 0;
    int lock_state_flag = 0;   //锁状态默认为关
    int finger_state_flag = 0;   //指纹录制状态默认为关
    String id;  //锁ID
    String ip;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_RESPONSE:
                    String response_lock = (String) msg.obj;
                    // 在这⾥里进⾏行UI操作，将结果显⽰示到界⾯面上
                    //Log.d("boye",response_lock);
                    if (response_lock.equals("00")) {
                        state_text.setText("当前锁的状态为关闭！"+"\n"+"当前没有开始录指纹！");
                    } else if (response_lock.equals("01")) {
                        state_text.setText("当前锁的状态为关闭！"+"\n"+"当前正在录指纹...");
                    } else if (response_lock.equals("10")) {
                        state_text.setText("当前锁的状态为打开！"+"\n"+"当前没有开始录指纹！");
                    } else if (response_lock.equals("11")) {
                        state_text.setText("当前锁的状态为打开！"+"\n"+"当前正在录指纹...");
                    } else if (response_lock.equals("c")) {
                        state_text.setText("指纹库已经清空...");
                    } else if (response_lock.equals("error")) {
                        state_text.setText("硬件操作失败...");
                    } else if (response_lock.equals("updateok")) {
                        state_text.setText("操作成功！");
                    }
            }
        }
    };

    public void find() {
        state_text = (TextView) findViewById(R.id.lock_state_text);
        update_btn = (Button) findViewById(R.id.update_btn);
        open_btn = (Button) findViewById(R.id.open_btn);
        finger_btn = (Button) findViewById(R.id.finger_save_btn);
        clear_btn = (Button) findViewById(R.id.fingerprint_clear_btn);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        id = getIntent().getStringExtra("lock_id");
        ip = getIntent().getStringExtra("ip");

        find();

        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRequestFromHttpURLConnection(id, ip);
            }
        });

        open_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lock_state_flag == 0) {   //锁的状态为关闭，打开锁
                    open_btn.setText("关闭");
                    sendRequestWithHttpURLConnection(ip, id, "10");  //0为关闭，1为打开,打开锁
                    lock_state_flag = 1;
                } else {
                    open_btn.setText("打开");
                    sendRequestWithHttpURLConnection(ip, id, "00");  //关闭锁
                    lock_state_flag = 0;
                }
            }
        });

        finger_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (finger_state_flag == 0) {
                    finger_btn.setText("结束录制");
                    sendRequestWithHttpURLConnection(ip, id, "01");  //打开指纹录制，如果锁未打开状态，则关闭锁
                    finger_state_flag = 1;
                } else {
                    finger_btn.setText("指纹录制");
                    sendRequestWithHttpURLConnection(ip, id, "00");  //关闭指纹录制，如果锁未打开状态，则关闭锁
                    finger_state_flag = 0;
                }
            }
        });

        clear_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequestWithHttpURLConnection(ip, id, "c");  //c标志为清空指纹
            }
        });
    }

    /**
     * 开/关锁、开启/关闭指纹录制
     */
    public void sendRequestWithHttpURLConnection (final String ip, final String id, final String state) {
        // 开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    //http://192.168.1.105:8000/fingerprint/updatestate/?p1=lock_id&p2=state&p3=retrn
                    URL url = new URL("http://"+ip+":8000/fingerprint/updatestate/?p1="
                            + id + "&p2=" + state + "&p3=1");
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
         /**
         * 获取锁的状态
         */
        private void getRequestFromHttpURLConnection(final String id2, final String ip) {
            // 开启线程来发起网络请求
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection = null;
                    try {
                        //http://192.168.1.105:8000/fingerprint/appgetstate/?p1=lock_id
                        URL url = new URL("http://"+ip+":8000/fingerprint/appgetstate/?p1=" + id2);
                        connection = (HttpURLConnection)
                                url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);
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

