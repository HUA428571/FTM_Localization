package com.example.ftm_1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.MacAddress;
import android.net.wifi.ScanResult;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.RangingResultCallback;
import android.net.wifi.rtt.WifiRttManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView text_output, text_range_result;

    Button btn_check, btn_range;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化控件
        initView();
    }


    private boolean isRTTavailable() {
        return this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_RTT);
    }

    private void initView() {
        text_output = findViewById(R.id.textView2);
        text_output.setText("Press check to check RTT");
        text_range_result = findViewById(R.id.textView);

        btn_check = findViewById(R.id.Btn_check);
        btn_range = findViewById(R.id.Btn_range);

        btn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRTTavailable()) {
                    text_output.setText("RTT is available");
                } else {
                    text_output.setText("RTT is not available");
                }
            }
        });

//        btn_range.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // 构建RangingRequest
//                RangingRequest.Builder builder = new RangingRequest.Builder();
//                // 使用MAC地址添加Wi-Fi Aware对等体
//                builder.addWifiAwarePeer(MacAddress.fromString("34:85:18:8f:1a:19"));
//                // 构建请求
//                RangingRequest req = builder.build();
//
//                // 获取WifiRttManager服务
//                WifiRttManager mgr = (WifiRttManager) getSystemService(Context.WIFI_RTT_RANGING_SERVICE);
//
//                // 确保mgr不为空
//                if (mgr != null) {
//                    // 启动测距
//                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
//                        // TODO: Consider calling
//                        //    ActivityCompat#requestPermissions
//                        // here to request the missing permissions, and then overriding
//                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                        //                                          int[] grantResults)
//                        // to handle the case where the user grants the permission. See the documentation
//                        // for ActivityCompat#requestPermissions for more details.
//                        return;
//                    }
//                    mgr.startRanging(req, getMainExecutor(), new RangingResultCallback() {
//
//                        @Override
//                        public void onRangingFailure(int code) {
//                            // 测距失败时更新文本
//                            text_range_result.setText("Ranging failed");
//                        }
//
//                        @Override
//                        public void onRangingResults(List<RangingResult> results) {
//                            // 测距成功时更新文本
//                            text_range_result.setText("Ranging success");
//                        }
//                    });
//                } else {
//                    // 如果WifiRttManager服务不可用
//                    text_range_result.setText("WifiRttManager not available");
//                }
//            }
//        });

    }

    @Override
    public void onClick(View v) {

    }
}