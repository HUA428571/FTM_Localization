package com.example.ftm_1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.RangingResultCallback;
import android.net.wifi.rtt.WifiRttManager;

import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    // 定位权限
    private boolean mLocationPermissionApproved = false;

    private WifiManager mWifiManager;

    TextView text_output, text_range_result;

    Button btn_check, btn_range;

    Context context;

    // 所有AP的MAC地址
    List<String> macAddress = new ArrayList<>();

    // 测距结果
    List<RangingResult> rangingResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        // 初始化所有AP的MAC地址
        macAddress.add("34:85:18:8f:1a:19");    // FTM1
        macAddress.add("34:85:18:8f:42:21");    // FTM2
        macAddress.add("34:85:18:95:f9:79");    // FTM3
        macAddress.add("34:85:18:8f:19:c1");    // FTM4

        //初始化控件
        initView();
    }

    // 检查是否支持RTT
    private boolean isRTTavailable()
    {
        return this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_RTT);
    }

    @SuppressLint("MissingPermission")
    private void startFTMRanging(ScanResult scanResult)
    {
        // 构建RangingRequest
        RangingRequest rangingRequest =
                new RangingRequest.Builder().addAccessPoint(scanResult).build();

        // 获取WifiRttManager服务
        WifiRttManager wifiRttManager = (WifiRttManager) getSystemService(Context.WIFI_RTT_RANGING_SERVICE);

        // 确保mgr不为空
        if (wifiRttManager != null)
        {
            // 启动测距
            Log.d("Debug_all", "wifiRttManager is not NULL, start ranging");

            wifiRttManager.startRanging(rangingRequest, getApplication().getMainExecutor(), new RangingResultCallback()
            {
                @Override
                public void onRangingFailure(int code)
                {
                    // 测距失败时更新文本
                    Log.d("Debug", "Ranging failed");
                    text_range_result.setText("Ranging failed");
                }

                @Override
                public void onRangingResults(@NonNull List<RangingResult> results)
                {
                    for (RangingResult result : results)
                    {
                        // 检查每个测距结果的状态
                        if (result.getStatus() == RangingResult.STATUS_SUCCESS)
                        {
                            // 状态为成功，可以安全地获取距离
                            rangingResults.add(result);
                            Log.d("Debug", "Ranging Result:"+result.getDistanceMm() + "mm");
//                            text_range_result.setText(result.getDistanceMm() + "mm");

                        } else
                        {
                            text_range_result.setText("Ranging failed:" + result.getStatus());
                            // 状态不是成功，处理失败的情况
                        }
                    }
                }
            });

        } else
        {
            // 如果WifiRttManager服务不可用
            text_range_result.setText("WifiRttManager not available");
        }

    }

    private void initView()
    {
        text_output = findViewById(R.id.textView2);
        text_output.setText("Press check to check RTT");
        text_range_result = findViewById(R.id.textView);

        btn_check = (Button) findViewById(R.id.Btn_check);
        btn_range = (Button) findViewById(R.id.Btn_range);

        btn_check.setOnClickListener(this);
        btn_range.setOnClickListener(this);

        context = getApplicationContext();

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View view)
    {
        int viewID = view.getId();
        if (viewID == R.id.Btn_check)
        {
            if (isRTTavailable())
            {
                text_output.setText("RTT is available");
            } else
            {
                text_output.setText("RTT is not available");
            }
        } else if (viewID == R.id.Btn_range)
        {
            text_range_result.setText("Start ranging");

            // TODO: 2021/4/27
            //  1. 测距结果列表不清空，会导致结果叠加
            //  2. 但是这样清空测距结果就会把所有的结果全部清除
            //  3. 现在的情况是，第一次按下range按钮，不会有结果，只有按下第二次才会有结果
            //  4. 只能说能用了，可以直接判断有没有4个结果（或其他AP数量），如果有就清空（笑死这个不行）
//            if (!rangingResults.isEmpty())
//            {
//                // 首先清空测距结果列表
//                rangingResults.clear();
//            }

            // 下面这个方法也不行
//            if (rangingResults.size()==2)
//            {
//                rangingResults.clear();
//            }

            // 检查是否有定位权限，没有就申请
            if (!mLocationPermissionApproved)
            {
                Log.d("Debug", "Request for location permission");
                // On 23+ (M+) devices, fine location permission not granted. Request permission.
                Intent startIntent = new Intent(this, LocationPermissionRequestActivity.class);
                startActivity(startIntent);
            }

            // 还是没有定位权限就寄
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
            {
                text_range_result.setText("Don't have location permission");
            }

            // 开始扫描AP
            mWifiManager.startScan();
            Log.d("Debug", "Start scan");
            // 获取扫描结果
            List<ScanResult> scanResults = mWifiManager.getScanResults();
            Log.d("Debug", "Get scan results");
            // 遍历扫描结果，匹配MAC地址
            for (ScanResult scanResult : scanResults)
            {
                for (String mac : macAddress)
                {
                    if (scanResult.BSSID.equals(mac))
                    {
                        Log.d("Debug", "Find MAC:" + mac);
                        startFTMRanging(scanResult);
                    }
                }
            }

            // 输出结果，后期可以改为任何对结果的处理
            StringBuilder resultsText = new StringBuilder("Ranging finished:\n");
            for (RangingResult rangingResult : rangingResults)
            {
//                text_range_result.setText(rangingResult.getDistanceMm() + "mm");
                resultsText.append(rangingResult.getDistanceMm()).append("mm\n");
            }
            text_range_result.setText(resultsText.toString());

        }
    }
}