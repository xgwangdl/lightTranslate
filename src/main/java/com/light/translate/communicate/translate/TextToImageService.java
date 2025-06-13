package com.light.translate.communicate.translate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class TextToImageService {
    @Value("${python.script.image.path}")
    private String pythonImagePath;

    public byte[] makeImage(String jsonPath) {
        byte[] bytes = null;
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String python;

            if (os.contains("win")) {
                python = "python";
            } else if (os.contains("linux") || os.contains("mac")) {
                python = "/usr/bin/python";
            } else {
                throw new UnsupportedOperationException("Unsupported operating system: " + os);
            }
            ProcessBuilder processBuilder = new ProcessBuilder(python, pythonImagePath, jsonPath);
            processBuilder.redirectErrorStream(true);

            // 启动 Python 进程
            Process process = processBuilder.start();

            InputStream errorStream = process.getErrorStream();
            String errorOutput = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
            System.err.println("错误信息: " + errorOutput);

            // 获取 Python 输出流
            InputStream inputStream = process.getInputStream();
            bytes = inputStream.readAllBytes();

            // 等待进程执行完毕
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("图片文件已保存为: byte[]");
            } else {
                System.err.println("Python 脚本执行失败，退出码: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return bytes;
    }
}
