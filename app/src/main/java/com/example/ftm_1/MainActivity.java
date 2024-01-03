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
    // 超时的最大测量次数
    private int MAX_RANGING_RETRY_COUNT = 3;
    private int mRangingRetryCount = 1;
    private boolean mFlagRangeSuccess = false;

    private WifiManager mWifiManager;

    TextView text_output, text_range_result;
    TextView text_FTM_result_1, text_FTM_result_2, text_FTM_result_3, text_FTM_result_4;

    Button btn_check, btn_range;

    Context context;

    // 所有AP的MAC地址
    List<String> macAddress = new ArrayList<>();

    // TODO: 些什么list，就给我写死
    String macAddress_1 = "34:85:18:8f:1a:19";
    String macAddress_2 = "34:85:18:8f:42:21";
    String macAddress_3 = "34:85:18:95:f9:79";
    String macAddress_4 = "34:85:18:8f:19:c1";

    int totalAP = 4;

    // 是否扫描了AP
    private boolean mScanningAP = false;
    // 扫描结果
    List<ScanResult> scanResults = new ArrayList<>();

    // 测距结果
//    List<RangingResult> rangingResults = new ArrayList<>();

    // TODO：结果也一样，给我写死
    List<RangingResult> rangingResults_1 = new ArrayList<>();
    List<RangingResult> rangingResults_2 = new ArrayList<>();
    List<RangingResult> rangingResults_3 = new ArrayList<>();
    List<RangingResult> rangingResults_4 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        // TODO：暂时在变量里面写死，这部分用不到
        // 初始化所有AP的MAC地址
        macAddress.add("34:85:18:8f:1a:19");    // FTM1
        macAddress.add("34:85:18:8f:42:21");    // FTM2
        macAddress.add("34:85:18:95:f9:79");    // FTM3
        macAddress.add("34:85:18:8f:19:c1");    // FTM4

        // 扫描所有AP
        startScanAP();

        //初始化控件
        initView();
    }

    // 检查是否支持RTT
    private boolean isRTTavailable()
    {
        return this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_RTT);
    }

    private void startScanAP()
    {
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
        Log.d("Debug", "Start scan AP");
        // 获取扫描结果
        // TODO:检查是否扫描成功
        scanResults = mWifiManager.getScanResults();
        mScanningAP = true;
        Log.d("Debug", "Get AP scan results");
    }

    private void restartAPScanAndRangingResults()
    {
        // 重新扫描AP
        startScanAP();
        // 清空测距结果
        rangingResults_1.clear();
        rangingResults_2.clear();
        rangingResults_3.clear();
        rangingResults_4.clear();
    }

    private void clearRangingResults()
    {
        // 清空测距结果
        rangingResults_1.clear();
        rangingResults_2.clear();
        rangingResults_3.clear();
        rangingResults_4.clear();
    }

    @SuppressLint("MissingPermission")
    private boolean startFTMRanging(ScanResult scanResult, List<RangingResult> tmRangingResults)
    {
        // 构建RangingRequest
        RangingRequest rangingRequest =
                new RangingRequest.Builder().addAccessPoint(scanResult).build();

        // 获取WifiRttManager服务
        WifiRttManager wifiRttManager = (WifiRttManager) getSystemService(Context.WIFI_RTT_RANGING_SERVICE);

        // 确保mgr不为空
        if (wifiRttManager != null)
        {
            mFlagRangeSuccess= false;

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
                            tmRangingResults.add(result);
                            Log.d("Debug", "Ranging Result:" + result.getDistanceMm() + "mm");
                            mFlagRangeSuccess= true;
//                            return;
                        } else
                        {
                            Log.d("Debug","Ranging failed:" + result.getStatus());
                            // 状态不是成功，处理失败的情况
                        }
                    }
                    //遍历完了所有结果，都没有成功的，就重新测量一次
                    mRangingRetryCount += 1;
                    if (tmRangingResults.size()==0)
                    {
                        if (mRangingRetryCount <= MAX_RANGING_RETRY_COUNT)
                        {
                            // 重新测量
                            Log.d("Debug", "Ranging retry");
                            startFTMRanging(scanResult, tmRangingResults);
                        } else
                        {
                            // 超过最大测量次数，停止测量
                            Log.d("Debug", "Ranging failed: over max retry count");
                            text_range_result.setText("Ranging failed: over max retry count");
                        }
                    }
                }
            });
        } else
        {
            // 如果WifiRttManager服务不可用
            text_range_result.setText("WifiRttManager not available");
        }

        return mFlagRangeSuccess;
    }

    private boolean startFTMRanging_Allap()
    {
        if (!mScanningAP)
        {
            // 如果没有扫描AP，就先扫描AP
            startScanAP();
        }

        List<RangingResult> tmpRangingResults_1 = new ArrayList<>();
        List<RangingResult> tmpRangingResults_2 = new ArrayList<>();
        List<RangingResult> tmpRangingResults_3 = new ArrayList<>();
        List<RangingResult> tmpRangingResults_4 = new ArrayList<>();

        // 遍历扫描结果，匹配MAC地址
        for (ScanResult scanResult : scanResults)
        {
            if (scanResult.BSSID.equals(macAddress_1) & totalAP >= 1)
            {
                startFTMRanging(scanResult, tmpRangingResults_1);
                Log.d("Debug", "Find MAC:" + macAddress_1);
            } else if (scanResult.BSSID.equals(macAddress_2) & totalAP >= 2)
            {
                Log.d("Debug", "Find MAC:" + macAddress_2);
                startFTMRanging(scanResult, tmpRangingResults_2);
            } else if (scanResult.BSSID.equals(macAddress_3) & totalAP >= 3)
            {
                Log.d("Debug", "Find MAC:" + macAddress_3);
                startFTMRanging(scanResult, tmpRangingResults_3);
            } else if (scanResult.BSSID.equals(macAddress_4) & totalAP >= 4)
            {
                Log.d("Debug", "Find MAC:" + macAddress_4);
                startFTMRanging(scanResult, tmpRangingResults_4);
            }
        }
        if (tmpRangingResults_1.size() > 0 &
                tmpRangingResults_2.size() > 0 &
                tmpRangingResults_3.size() > 0 &
                tmpRangingResults_4.size() > 0)
        {
            rangingResults_1.add(tmpRangingResults_1.get(0));
            rangingResults_2.add(tmpRangingResults_2.get(0));
            rangingResults_3.add(tmpRangingResults_3.get(0));
            rangingResults_4.add(tmpRangingResults_4.get(0));
            return true;
        }
        else
        {
            return false;
        }
    }

    private void initView()
    {
        text_output = findViewById(R.id.textView2);
        text_output.setText("Press check to check RTT");
        text_range_result = findViewById(R.id.textView);

        text_FTM_result_1 = findViewById(R.id.text_FTM_result_1);
        text_FTM_result_2 = findViewById(R.id.text_FTM_result_2);
        text_FTM_result_3 = findViewById(R.id.text_FTM_result_3);
        text_FTM_result_4 = findViewById(R.id.text_FTM_result_4);
        text_FTM_result_1.setText("AP1: No Data");
        text_FTM_result_2.setText("AP2: No Data");
        text_FTM_result_3.setText("AP3: No Data");
        text_FTM_result_4.setText("AP4: No Data");

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
            restartAPScanAndRangingResults();
        } else if (viewID == R.id.Btn_range)
        {
            // 按下range按钮后，进行测距
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

            startFTMRanging_Allap();

            // 输出结果，后期可以改为任何对结果的处理
            text_range_result.setText("Ranging finished:\n");
            text_FTM_result_1.setText("AP1: " + rangingResults_1.size() + " results");
            text_FTM_result_2.setText("AP2: " + rangingResults_2.size() + " results");
            text_FTM_result_3.setText("AP3: " + rangingResults_3.size() + " results");
            text_FTM_result_4.setText("AP4: " + rangingResults_4.size() + " results");

            // TODO:给我写死
//            StringBuilder resultsText = new StringBuilder("Ranging finished:\n");
//            for (RangingResult rangingResult : rangingResults)
//            {
////                text_range_result.setText(rangingResult.getDistanceMm() + "mm");
//                resultsText.append(rangingResult.getDistanceMm()).append("mm\n");
//            }
//            text_range_result.setText(resultsText.toString());

        }
    }
}