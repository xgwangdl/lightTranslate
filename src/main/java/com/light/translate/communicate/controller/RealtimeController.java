package com.light.translate.communicate.controller;

import com.alibaba.dashscope.assistants.Assistant;
import com.light.translate.communicate.ali.RealtimeAssistant;
import com.light.translate.communicate.ali.SpeakerAssistant;
import com.light.translate.communicate.dto.SpeakerResponse;
import com.light.translate.communicate.services.AliSpeechRecognizer;
import com.light.translate.communicate.services.OmniService;
import com.light.translate.communicate.translate.AudioConverter;
import com.light.translate.communicate.utils.OssUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.TextMessage;
import reactor.core.publisher.Flux;

import java.io.*;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/dict/realtime")
@RequiredArgsConstructor
public class RealtimeController {

    private final OmniService omniService;
    private final AliSpeechRecognizer aliSpeechRecognizer;
    private final RealtimeAssistant realtimeAssistant;

    @PostMapping("/start")
    public void startRealtimeTranslate(@RequestParam("openid") String openid,
                                                  @RequestParam("level") String level) throws IOException, InterruptedException {
        omniService.createConversationForSession(openid, level);
    }

    @PostMapping("/sendMessage")
    public SpeakerResponse sendMessage(@RequestParam("openid") String openid,
                                                  @RequestPart("audio") MultipartFile audioFile) throws IOException, InterruptedException {
        File tempFile = this.convertMp3ToPcmWithFfmpeg(audioFile.getBytes());
        SpeakerResponse response = omniService.sendMessage(openid, tempFile);
        tempFile.delete();
        return response;
    }

    @GetMapping("/stop")
    public void stopRealtimeTranslate(@RequestParam("openid") String openid) {
        omniService.stop(openid);
    }

    @PostMapping("/chat")
    public SpeakerResponse chat(@RequestParam("openid") String openid,
                                @RequestPart("audio") MultipartFile audioFile) throws IOException, InterruptedException {
        // 保存临时文件
        File tempFile = File.createTempFile("upload", ".wav");
        audioFile.transferTo(tempFile);
        File convertFile = AudioConverter.convertToWav(tempFile);

        String userMessageContent = aliSpeechRecognizer.recognizeSentences(convertFile);
        Flux<String> chat = realtimeAssistant.chat(openid, userMessageContent);
        convertFile.delete();
        tempFile.delete();

        SpeakerResponse response = omniService.getVoice(chat,"Cherry");


        return response;
    }

    /**
     * 使用系统 ffmpeg 将 MP3 二进制转为 PCM s16le 16k mono（raw）
     * 依赖：ffmpeg 可执行文件在 PATH 中（或直接写全路径）
     *
     * 命令示例：
     * ffmpeg -y -i in.mp3 -f s16le -acodec pcm_s16le -ac 1 -ar 16000 out.pcm
     */
    private File convertMp3ToPcmWithFfmpeg(byte[] mp3Bytes) throws IOException, InterruptedException {
        // 创建临时文件
        File tmpIn = Files.createTempFile("ws_recv_", ".mp3").toFile();
        File tmpOut = Files.createTempFile("ws_out_", ".pcm").toFile();
        try (FileOutputStream fos = new FileOutputStream(tmpIn)) {
            fos.write(mp3Bytes);
        }

        // ffmpeg 命令（如 ffmpeg 不在 PATH，请改为绝对路径）
        String ffmpegCmd = "ffmpeg";
        ProcessBuilder pb = new ProcessBuilder(
                ffmpegCmd,
                "-y",
                "-i", tmpIn.getAbsolutePath(),
                "-f", "s16le",
                "-acodec", "pcm_s16le",
                "-ac", "1",
                "-ar", "16000",
                tmpOut.getAbsolutePath()
        );

        pb.redirectErrorStream(true); // 将 stderr 合并到 stdout，便于调试
        Process proc = pb.start();

        // 读取 ffmpeg 输出日志（避免阻塞）
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 可选择记录日志
                // System.out.println("[ffmpeg] " + line);
            }
        }

        int exit = proc.waitFor();
        if (exit != 0) {
            System.err.println("❌ ffmpeg 进程退出码: " + exit);
            tmpIn.delete();
            tmpOut.delete();
            return null;
        }

        // 删除临时文件
        tmpIn.delete();

        return tmpOut;
    }

}
