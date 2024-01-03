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

import org.apache.commons.math3.linear.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    // 定位权限
    private boolean mLocationPermissionApproved = false;
    // 超时的最大测量次数
    private int MAX_RANGING_RETRY_COUNT = 3;
    private int mRangingRetryCount = 1;
    private boolean mFlagRangeSuccess = false;

    private WifiManager mWifiManager;
    private WifiRttManager mWifiRttManager;

    TextView text_output;
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
    List<RangingResult> rangingResultsAAA = new ArrayList<>();

    // TODO：结果也一样，给我写死
    List<RangingResult> rangingResults_1 = new ArrayList<>();
    List<RangingResult> rangingResults_2 = new ArrayList<>();
    List<RangingResult> rangingResults_3 = new ArrayList<>();
    List<RangingResult> rangingResults_4 = new ArrayList<>();

    List<RangingResult> tmpRangingResults = new ArrayList<>();
    List<RangingResult> tmpRangingResults_1 = new ArrayList<>();
    List<RangingResult> tmpRangingResults_2 = new ArrayList<>();
    List<RangingResult> tmpRangingResults_3 = new ArrayList<>();
    List<RangingResult> tmpRangingResults_4 = new ArrayList<>();

    private double x1 = 0.0, y1 = 0.0, z1 = 0.0;
    private double x2 = 0.0, y2 = 10.0, z2 = 0.0;
    private double x3 = 10.0, y3 = 10.0, z3 = 0.0;
    private double x4 = 10.0, y4 = 0.0, z4 = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        // 获取WifiRttManager服务
        mWifiRttManager = (WifiRttManager) getSystemService(Context.WIFI_RTT_RANGING_SERVICE);

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
            text_output.setText("Don't have location permission");
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

    private void clearTmpRangingResults()
    {
        // 清空测距结果
        tmpRangingResults_1.clear();
        tmpRangingResults_2.clear();
        tmpRangingResults_3.clear();
        tmpRangingResults_4.clear();
    }

    @SuppressLint("MissingPermission")
    private RangingResult startFTMRanging(ScanResult scanResult)
    {
        // 构建RangingRequest
        RangingRequest rangingRequest =
                new RangingRequest.Builder().addAccessPoint(scanResult).build();

        mFlagRangeSuccess = false;

        // 启动测距
        Log.d("Debug_all", "wifiRttManager is not NULL, start ranging");

        mWifiRttManager.startRanging(rangingRequest, getApplication().getMainExecutor(), new RangingResultCallback()
        {
            @Override
            public void onRangingFailure(int code)
            {
                // 测距失败时更新文本
                Log.d("Debug", "Ranging failed");
                text_output.setText("Ranging failed");
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
                        rangingResultsAAA.add(result);
                        tmpRangingResults.add(result);
                        Log.d("Debug", "Ranging Result:" + result.getDistanceMm() + "mm");
                        mFlagRangeSuccess = true;
//                            return;
                    } else
                    {
                        Log.d("Debug", "Ranging failed:" + result.getStatus());
                        // 状态不是成功，处理失败的情况
                    }
                }
                //遍历完了所有结果，都没有成功的，就重新测量一次
                mRangingRetryCount += 1;
                if (tmpRangingResults.size() == 0)
                {
                    if (mRangingRetryCount <= MAX_RANGING_RETRY_COUNT)
                    {
                        // 重新测量
                        Log.d("Debug", "Ranging retry");
                        startFTMRanging(scanResult);
                    } else
                    {
                        // 超过最大测量次数，停止测量
                        Log.d("Debug", "Ranging failed: over max retry count");
                        text_output.setText("Ranging failed: over max retry count");
                    }
                }
            }
        });

        if (tmpRangingResults.size() > 0)
        {
            return tmpRangingResults.get(0);
        }
        else
        {
            return null;
        }
    }

    private boolean startFTMRanging_Allap()
    {
        if (!mScanningAP)
        {
            // 如果没有扫描AP，就先扫描AP
            startScanAP();
        }

        clearTmpRangingResults();

        // 遍历扫描结果，匹配MAC地址
        for (ScanResult scanResult : scanResults)
        {
            if (scanResult.BSSID.equals(macAddress_1) & totalAP >= 1)
            {
                Log.d("Debug", "Find MAC:" + macAddress_1);
                tmpRangingResults_1.add(startFTMRanging(scanResult));
            } else if (scanResult.BSSID.equals(macAddress_2) & totalAP >= 2)
            {
                Log.d("Debug", "Find MAC:" + macAddress_2);
                tmpRangingResults_2.add(startFTMRanging(scanResult));                ;
            } else if (scanResult.BSSID.equals(macAddress_3) & totalAP >= 3)
            {
                Log.d("Debug", "Find MAC:" + macAddress_3);
                tmpRangingResults_3.add(startFTMRanging(scanResult));
            } else if (scanResult.BSSID.equals(macAddress_4) & totalAP >= 4)
            {
                Log.d("Debug", "Find MAC:" + macAddress_4);
                tmpRangingResults_4.add(startFTMRanging(scanResult));
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
        } else
        {
            return false;
        }
    }

    private void initView()
    {
        text_output = findViewById(R.id.textView);
        text_output.setText("Press check to check RTT");

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

        if (mWifiRttManager != null)
        {
            // 如果WifiRttManager服务不可用
            text_output.setText("WifiRttManager not available");
        }
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
            text_output.setText("Start ranging");

            startFTMRanging_Allap();

            // 输出结果，后期可以改为任何对结果的处理
            text_output.setText("Ranging finished:\n");
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

    private List<Double> getCoordinates()
    {
        List<Double> coordinates = new ArrayList<Double>();
        //使用最小二乘法计算坐标，需要四个AP测得的距离以及四个AP的坐标
        //每个rangingResults是一个list，求测得的平均值😋
        double d1 = 0.0, d2 = 0.0, d3 = 0.0, d4 = 0.0;
        for (int i = 0; i < rangingResults_1.size(); i++)
        {
            d1 += rangingResults_1.get(i).getDistanceMm() / 1000.0;
        }
        for (int i = 0; i < rangingResults_2.size(); i++)
        {
            d2 += rangingResults_2.get(i).getDistanceMm() / 1000.0;
        }
        for (int i = 0; i < rangingResults_3.size(); i++)
        {
            d3 += rangingResults_3.get(i).getDistanceMm() / 1000.0;
        }
        for (int i = 0; i < rangingResults_4.size(); i++)
        {
            d4 += rangingResults_4.get(i).getDistanceMm() / 1000.0;
        }
        d1 /= rangingResults_1.size();
        d2 /= rangingResults_2.size();
        d3 /= rangingResults_3.size();
        d4 /= rangingResults_4.size();
        //使用Apache Commons Math包，由最小二乘法推导得到的公式计算坐标
        //构造系数矩阵
        double[][] matrixAData = {{2 * (x2 - x1), 2 * (y2 - y1)}, {2 * (x3 - x2), 2 * (y3 - y2)}, {2 * (x4 - x3), 2 * (y4 - y3)}};
        double[][] matrixBData = {{d1 * d1 - d2 * d2 - x1 * x1 - y1 * y1 + x2 * x2 + y2 * y2}, {d2 * d2 - d3 * d3 - x2 * x2 - y2 * y2 + x3 * x3 + y3 * y3},
                {d3 * d3 - d4 * d4 - x3 * x3 - y3 * y3 + x4 * x4 + y4 * y4}};

        RealMatrix A = new Array2DRowRealMatrix(matrixAData);
        RealMatrix B = new Array2DRowRealMatrix(matrixBData);
        RealMatrix A_TA = A.transpose().multiply(A);
        RealMatrix A_TA_inverse = MatrixUtils.inverse(A_TA);
        RealMatrix X = A_TA_inverse.multiply(A.transpose().multiply(B));

        coordinates.add(X.getEntry(0, 0));
        coordinates.add(X.getEntry(1, 0));
        coordinates.add(X.getEntry(2, 0));

        return coordinates;
    }

}