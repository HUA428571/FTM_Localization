package com.example.ftm_1;

import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

public class TFLiteModel {
    Interpreter tflite;

    public TFLiteModel(Context context) throws IOException {
        tflite = new Interpreter(loadModelFile(context));
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException, IOException {
        FileChannel fileChannel;
        long startOffset;
        long declaredLength;
        try (AssetFileDescriptor fileDescriptor = context.getAssets().openFd("dense_model.tflite")) {
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            fileChannel = inputStream.getChannel();
            startOffset = fileDescriptor.getStartOffset();
            declaredLength = fileDescriptor.getDeclaredLength();
        }
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // 添加用于输入处理、模型推断和输出处理的方法
    // 设置训练集的均值和标准差
    private double[] mean = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    private double[] std = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};

    public double[][] runInference(double[][] input) {
        double[][] output = new double[1][3];
        //标准化输入数据
        for (int i = 0; i < input.length; i++) {
            input[0][i] = (input[0][i] - mean[i]) / std[i];
        }
        tflite.run(input, output);
        return output;
    }

}
