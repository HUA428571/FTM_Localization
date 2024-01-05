# FTM_Localization

> SUSTech 无线网络与移动计算 课程作业
> (c) 2024 HuaCL Zhao.K.J

## 基本功能

### 使用支持FTM的设备进行基于ToF的定位
1. 按下Reset重置所有信息
2. 按下Location进行单次定位（连续测量20次RTT信息后进行定位）
3. 按下Track后进行持续定位（跟踪）

### 相关提示信息
1. 提示栏：位于左上角，提示当前测量状态
2. AP信息：位于左下角，显示当前距离4个AP的距离（经校准后）
3. 位置信息：位于右下角，显示当前位置
4. 平面示意图：
    1. 黑色框为房间
    2. 蓝色点为AP
    3. 红色点为定位

### 程序截图
![Alt text](Screenshot_20240105-024430.png)

## 使用代码
### 依赖
1. Android API Level 28+
2. Android Support Repository
3. 支持WifiRttManager的安卓手机  
    相关信息参见链接：  
    https://developer.android.com/reference/android/net/wifi/rtt/WifiRttManager

### 运行代码
This app uses the Gradle build system. To build this project, use the "gradlew build" command or use "Import Project" in Android Studio.  
The codes is available at Github: https://github.com/HUA428571/FTM_Localization
你需要在程序运行前合理的设定房间和AP的位置（这些信息在\app\src\main\res\values\location.xml中）。

## 代码简要说明
### MainActivity.java

### LocationPermissionRequestActivity.java

### LocationView.java

### TFLiteModel.java
