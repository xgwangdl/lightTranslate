package com.light.translate.communicate.translate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Service
public class TextToSpeechService {
    @Value("${python.script.tts.path}")
    private String pythonTtsPath;

    /**
     * 根据文字转化对应的声音
     * @param text 文字
     * @param voice 声音
     * @return byte
     */
    public byte[] tts(String text,String voice) {
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
            ProcessBuilder processBuilder = new ProcessBuilder(python, pythonTtsPath, text, voice);
            processBuilder.redirectErrorStream(true);

            // 启动 Python 进程
            Process process = processBuilder.start();

            InputStream errorStream = process.getErrorStream();
            String errorOutput = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
            System.err.println("错误信息: " + errorOutput);

            // 获取 Python 输出流（音频数据）
            InputStream inputStream = process.getInputStream();
            bytes = inputStream.readAllBytes();

            // 等待进程执行完毕
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("音频文件已保存为: byte[]");
            } else {
                System.err.println("Python 脚本执行失败，退出码: " + exitCode);

            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * 音频流返回Byte
     * @param inputStream 音频流
     * @return 返回值
     * @throws IOException
     */
    private byte[] inputStreamToBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] tempBuffer = new byte[4096]; // Adjust buffer size for audio (e.g., 8192, 16384)
        int bytesRead;

        while ((bytesRead = inputStream.read(tempBuffer)) != -1) {
            buffer.write(tempBuffer, 0, bytesRead);
        }

        return buffer.toByteArray();
    }

}