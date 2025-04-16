import sys
import edge_tts
import asyncio
from pydub import AudioSegment
from io import BytesIO

async def generate_audio(text, voice):
    # 使用 Edge TTS 生成音频并保存到内存
    communicate = edge_tts.Communicate(text=text, voice=voice, rate="+5%", volume="soft")
    audio_stream = BytesIO()

    # 使用流式获取音频数据并保存到内存,
    async for chunk in communicate.stream():
        if chunk["type"] == "audio":
            audio_stream.write(chunk["data"])

    # 重置音频流指针
    audio_stream.seek(0)

    # 读取音频为 AudioSegment 对象
    audio = AudioSegment.from_file(audio_stream, format="mp3")

    # 重新采样到 16000 Hz 并转换为 WAV 格式
    audio = audio.set_frame_rate(16000)

    # 保存音频到内存中的 BytesIO 对象
    output_stream = BytesIO()
    audio.export(output_stream, format="mp3")
    output_stream.seek(0)

    return output_stream

def main():
    text = sys.argv[1]  # 获取传入的文本
    voice = sys.argv[2]  # 获取传入的声音

    # 使用 asyncio 运行异步操作
    output_stream = asyncio.run(generate_audio(text, voice))

    # 返回音频数据到标准输出
    sys.stdout.buffer.write(output_stream.read())  # 将音频数据写入标准输出

if __name__ == '__main__':
    main()
