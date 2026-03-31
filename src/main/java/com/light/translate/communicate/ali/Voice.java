package com.light.translate.communicate.ali;

import com.alibaba.dashscope.aigc.multimodalconversation.AudioParameters;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.audio.qwen_tts_realtime.*;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.Constants;
import com.alibaba.dashscope.utils.JsonUtils;
import com.google.gson.JsonObject;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Voice {
    public static void main(String[] args) throws InterruptedException, LineUnavailableException, IOException {
        Scanner scanner = new Scanner(System.in);
        ByteArrayOutputStream audioOut = new ByteArrayOutputStream();
        QwenTtsRealtimeParam param = QwenTtsRealtimeParam.builder()
                .model("qwen3-tts-flash-realtime")
                // 以下为北京地域url，若使用新加坡地域的模型，需将url替换为：wss://dashscope-intl.aliyuncs.com/api-ws/v1/realtime
                .url("wss://dashscope.aliyuncs.com/api-ws/v1/realtime")
                // 新加坡和北京地域的API Key不同。获取API Key：https://help.aliyun.com/zh/model-studio/get-api-key
                .apikey("sk-2915d83282b14fa9bc492c6fc32a8c18")
                .build();

        AtomicReference<CountDownLatch> completeLatch = new AtomicReference<>(new CountDownLatch(1));

        final AtomicReference<QwenTtsRealtime> qwenTtsRef = new AtomicReference<>(null);
        QwenTtsRealtime qwenTtsRealtime = new QwenTtsRealtime(param, new QwenTtsRealtimeCallback() {
            //            File file = new File("result_24k.pcm");
//            FileOutputStream fos = new FileOutputStream(file);
            @Override
            public void onOpen() {
                System.out.println("connection opened");
                System.out.println("输入文本并按Enter发送，输入'quit'退出程序");
            }
            @Override
            public void onEvent(JsonObject message) {
                String type = message.get("type").getAsString();
                switch(type) {
                    case "session.created":
                        System.out.println("start session: " + message.get("session").getAsJsonObject().get("id").getAsString());
                        break;
                    case "response.audio.delta":
                        String recvAudioB64 = message.get("delta").getAsString();
                        byte[] rawAudio = Base64.getDecoder().decode(recvAudioB64);
                        //                            fos.write(rawAudio);
                        // 实时播放音频
                        try {
                            audioOut.write(rawAudio);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "response.done":
                        System.out.println("response done");
                        // 为下一次输入做准备
                        completeLatch.get().countDown();
                        break;
                    case "session.finished":
                        System.out.println("session finished");
                        if (qwenTtsRef.get() != null) {
                            System.out.println("[Metric] response: " + qwenTtsRef.get().getResponseId() +
                                    ", first audio delay: " + qwenTtsRef.get().getFirstAudioDelay() + " ms");
                        }
                        completeLatch.get().countDown();
                    default:
                        break;
                }
            }
            @Override
            public void onClose(int code, String reason) {
                System.out.println("connection closed code: " + code + ", reason: " + reason);
            }
        });
        qwenTtsRef.set(qwenTtsRealtime);
        try {
            qwenTtsRealtime.connect();
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        }
        QwenTtsRealtimeConfig config = QwenTtsRealtimeConfig.builder()
                .voice("Roy")
                .languageType("English")
                .responseFormat(QwenTtsRealtimeAudioFormat.PCM_24000HZ_MONO_16BIT)
                .mode("commit")
                .volume(100)
                .build();
        qwenTtsRealtime.updateSession(config);

        // 循环读取用户输入
        while (true) {
            System.out.print("请输入要合成的文本: ");
            String text = scanner.nextLine();

            // 如果用户输入quit，则退出程序
            if ("quit".equalsIgnoreCase(text.trim())) {
                System.out.println("正在关闭连接...");
                qwenTtsRealtime.finish();
                completeLatch.get().await();
                break;
            }

            // 如果用户输入为空，跳过
            if (text.trim().isEmpty()) {
                continue;
            }

            // 重新初始化倒计时锁存器
            completeLatch.set(new CountDownLatch(1));

            // 发送文本
            qwenTtsRealtime.appendText(text);
            qwenTtsRealtime.commit();

            // 等待本次合成完成
            completeLatch.get().await();

            byte[] pcmBytes = audioOut.toByteArray();
            byte[] mp3Bytes = convertPcmToMp3(pcmBytes);


            String filePath = "C:/Users/xgwan/Downloads/mp3/audio.mp3";
            Path path = Paths.get(filePath);
            Files.write(path, mp3Bytes);
        }

        scanner.close();
        System.exit(0);
    }

    private static byte[] convertPcmToMp3(byte[] pcmData) throws IOException, InterruptedException {
        // 1. 写入临时 PCM 文件
        File tmpPcm = File.createTempFile("audio", ".pcm");
        try (FileOutputStream fos = new FileOutputStream(tmpPcm)) {
            fos.write(pcmData);
        }

        // 2. 输出文件
        File tmpMp3 = File.createTempFile("audio", ".mp3");

        // 3. 使用 FFmpeg 转换
        // 注意：ffmpeg 需要知道输入参数 -f s16le -ar 24000 -ac 1
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-f", "s16le",
                "-ar", "24000",
                "-ac", "1",
                "-i", tmpPcm.getAbsolutePath(),
                "-codec:a", "libmp3lame",
                "-b:a", "64k",
                tmpMp3.getAbsolutePath()
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();

        // 打印 ffmpeg 输出，方便调试
        String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = p.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg 转换失败，退出码: " + exitCode);
        }

        // 4. 读取 mp3 文件字节
        byte[] mp3Data = Files.readAllBytes(tmpMp3.toPath());

        // 5. 清理临时文件
        tmpPcm.delete();
        tmpMp3.delete();

        return mp3Data;
    }
}
