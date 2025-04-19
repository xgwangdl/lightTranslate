import sys
import edge_tts
import asyncio
from pydub import AudioSegment
from io import BytesIO
import logging

# 配置日志格式
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stderr)  # 确保日志输出到stderr
    ]
)

async def generate_audio(text, voice):
    try:
        logging.info(f"开始生成音频，文本: {text}, 语音: {voice}")

        # 记录参数检查
        logging.debug(f"参数检查 - rate: +5%, pitch: +10Hz")
        communicate = edge_tts.Communicate(
            text=text,
            voice=voice,
            rate="+5%",
            pitch="+10Hz"
        )

        audio_stream = BytesIO()
        chunk_count = 0

        logging.info("开始流式获取音频数据...")
        async for chunk in communicate.stream():
            if chunk["type"] == "audio":
                chunk_count += 1
                audio_stream.write(chunk["data"])
                if chunk_count % 10 == 0:
                    logging.debug(f"已接收 {chunk_count} 个音频块，当前块大小: {len(chunk['data'])} bytes")

        logging.info(f"音频接收完成，共 {chunk_count} 个块，总大小: {audio_stream.tell()} bytes")
        audio_stream.seek(0)

        # 音频处理
        logging.info("开始音频处理...")
        audio = AudioSegment.from_file(audio_stream, format="mp3")
        logging.debug(f"原始音频信息 - 时长: {len(audio)/1000}s, 采样率: {audio.frame_rate}Hz")

        audio = audio.set_frame_rate(16000)
        logging.debug(f"重采样后 - 采样率: {audio.frame_rate}Hz")

        output_stream = BytesIO()
        audio.export(output_stream, format="mp3")
        output_stream.seek(0)
        logging.info(f"音频处理完成，输出大小: {output_stream.getbuffer().nbytes} bytes")

        return output_stream

    except Exception as e:
        logging.error(f"生成音频时出错: {str(e)}", exc_info=True)
        raise

def main():
    try:
        # 记录启动信息
        logging.info("脚本启动，参数: " + str(sys.argv))

        if len(sys.argv) < 3:
            logging.error("参数不足，需要文本和语音参数")
            sys.exit(1)

        text = sys.argv[1]
        voice = sys.argv[2]

        logging.info(f"开始处理 - 文本: {text}, 语音: {voice}")
        output_stream = asyncio.run(generate_audio(text, voice))

        # 输出音频前记录
        logging.info("准备输出音频数据到stdout...")
        sys.stdout.buffer.write(output_stream.read())
        logging.info("音频输出完成")

    except Exception as e:
        logging.error(f"主函数出错: {str(e)}", exc_info=True)
        sys.exit(2)

if __name__ == '__main__':
    main()