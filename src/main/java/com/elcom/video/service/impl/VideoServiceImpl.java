/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.video.service.impl;

import com.elcom.video.controller.VideoController;
import com.elcom.video.service.VideoService;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author Admin
 */
@Service
public class VideoServiceImpl implements VideoService {

    private final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(VideoController.class);

    @Override
    public void convertVideoHls(String path) {
        try {
//            FFmpeg ffmpeg = new FFmpeg("C:/install/ffmpeg/bin/ffmpeg");
//            FFprobe ffprobe = new FFprobe("C:/install/ffmpeg/bin/ffprobe");
            FFmpeg ffmpeg = new FFmpeg();
            FFprobe ffprobe = new FFprobe();
            File file = new File(path);
            String filePathOutPut = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator));
            LOGGER.info("filePathOutPut: " + filePathOutPut);
            String outputName = file.getName().substring(0, file.getName().length() - Files.getFileExtension(path).length() - 1);

            //tao thu muc chua file HLS - ex : file name : input.mp4 -> create folder : input
            File dir = new File(filePathOutPut + "/" + outputName);
            LOGGER.info("Pathname: " + dir.getAbsolutePath());
            dir.mkdirs();

            //video 360P
            FFmpegBuilder builder360 = new FFmpegBuilder()
                    .setInput(path)
                    .overrideOutputFiles(true)
                    .addOutput(dir.getAbsolutePath() + "/" + outputName + "_360P" + ".m3u8")
                    .setFormat("wav")
                    .setAudioBitRate(96000) // at 96 kbit/s
                    .setAudioChannels(1)
                    .setAudioCodec("aac") // using the aac codec
                    .setAudioSampleRate(48_000)
                    .setAudioBitRate(32768) // at 32 kbit/s
                    .setVideoFrameRate(24, 1) // at 24 frames per second
                    .setVideoResolution(640, 360) // at 640x360 resolution
                    .setVideoBitRate(700000)
                    .setStrict(FFmpegBuilder.Strict.STRICT)
                    .setFormat("hls")
                    .addExtraArgs("-start_number", "0", "-hls_time", "5", "-hls_list_size", "0")
                    .done();

            //video 480P
            FFmpegBuilder builder480 = new FFmpegBuilder()
                    .setInput(path)
                    .overrideOutputFiles(true)
                    .addOutput(dir.getAbsolutePath() + "/" + outputName + "_480P" + ".m3u8")
                    .setFormat("wav")
                    .setAudioBitRate(96000) // at 96 kbit/s
                    .setAudioChannels(1)
                    .setAudioCodec("aac") // using the aac codec
                    .setAudioSampleRate(48_000)
                    .setAudioBitRate(128000) // at 32 kbit/s
                    .setVideoFrameRate(24, 1) // at 24 frames per second
                    .setVideoResolution(854, 480) // at 640x360 resolution
                    .setVideoBitRate(1250000)
                    .setStrict(FFmpegBuilder.Strict.STRICT)
                    .setFormat("hls")
                    .addExtraArgs("-start_number", "0", "-hls_time", "5", "-hls_list_size", "0")
                    .done();

            //video 720P
            FFmpegBuilder builder720 = new FFmpegBuilder()
                    .setInput(path)
                    .overrideOutputFiles(true)
                    .addOutput(dir.getAbsolutePath() + "/" + outputName + "_720P" + ".m3u8")
                    .setFormat("wav")
                    .setAudioBitRate(96000) // at 96 kbit/s
                    .setAudioChannels(1)
                    .setAudioCodec("aac") // using the aac codec
                    .setAudioSampleRate(48_000)
                    .setAudioBitRate(128000) // at 32 kbit/s
                    .setVideoFrameRate(24, 1) // at 24 frames per second
                    .setVideoResolution(1280, 720) // at 640x360 resolution
                    .setVideoBitRate(2500000)
                    .setStrict(FFmpegBuilder.Strict.STRICT)
                    .setFormat("hls")
                    .addExtraArgs("-start_number", "0", "-hls_time", "5", "-hls_list_size", "0")
                    .done();

            FFmpegBuilder builder1080 = new FFmpegBuilder()
                    .setInput(path)
                    .overrideOutputFiles(true)
                    .addOutput(dir.getAbsolutePath() + "/" + outputName + "_1080P" + ".m3u8")
                    .setFormat("wav")
                    .setAudioBitRate(96000) // at 96 kbit/s
                    .setAudioChannels(1)
                    .setAudioCodec("aac") // using the aac codec
                    .setAudioSampleRate(48_000)
                    .setAudioBitRate(192000) // at 192 kbit/s
                    .setVideoFrameRate(24, 1) // at 24 frames per second
                    .setVideoResolution(1920, 1080) // at 640x360 resolution
                    .setVideoBitRate(4500000)
                    .setStrict(FFmpegBuilder.Strict.STRICT)
                    .setFormat("hls")
                    .addExtraArgs("-start_number", "0", "-hls_time", "5", "-hls_list_size", "0")
                    .done();

            LOGGER.info("Converting .....");

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder360).run();
            executor.createJob(builder480).run();
            executor.createJob(builder720).run();
            executor.createJob(builder1080).run();

            File myObj = new File(filePathOutPut + "/" + outputName + "/playlist.m3u8");
            if (myObj.createNewFile()) {
                LOGGER.info("File created: " + myObj.getName());
            }
            try (FileWriter myWriter = new FileWriter(myObj)) {
                myWriter.write("#EXTM3U\n");
                myWriter.write("#EXT-X-VERSION:3\n");
                myWriter.write("#EXT-X-STREAM-INF:BANDWIDTH=700000,RESOLUTION=640x360\n");
                myWriter.write(outputName + "_360P.m3u8\n");
                myWriter.write("#EXT-X-STREAM-INF:BANDWIDTH=1250000,RESOLUTION=842x480\n");
                myWriter.write(outputName + "_480P.m3u8\n");
                myWriter.write("#EXT-X-STREAM-INF:BANDWIDTH=2500000,RESOLUTION=1280x720\n");
                myWriter.write(outputName + "_720P.m3u8\n");
                myWriter.write("#EXT-X-STREAM-INF:BANDWIDTH=5000000,RESOLUTION=1920x1080\n");
                myWriter.write(outputName + "_1080P.m3u8");
            }
            
            LOGGER.info("Done .....");

        } catch (IOException ex) {
            Logger.getLogger(VideoServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void compressVideo(String path) {
        try {
//            FFmpeg ffmpeg = new FFmpeg("C:/install/ffmpeg/bin/ffmpeg");
//            FFprobe ffprobe = new FFprobe("C:/install/ffmpeg/bin/ffprobe");
            FFmpeg ffmpeg = new FFmpeg();
            FFprobe ffprobe = new FFprobe();
            File file = new File(path);
            String filePathOutPut = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator));
            String outputName = file.getName().substring(0, file.getName().length() - Files.getFileExtension(path).length() - 1);

            FFmpegBuilder builder360 = new FFmpegBuilder()
                    .setInput(path) // Filename, or a FFmpegProbeResult
                    .overrideOutputFiles(true) // Override the output if it exists
                    .addOutput(filePathOutPut + "/" + outputName + "_360P." + Files.getFileExtension(path))
                    .setFormat(Files.getFileExtension(path).substring(0))
                    .disableSubtitle() // No subtiles
                    .setAudioChannels(1) // Mono audio
                    .setAudioCodec("aac") // using the aac codec
                    .setAudioSampleRate(48_000) // at 48KHz
                    .setAudioBitRate(96000) // at 32 kbit/s
                    .setVideoCodec("libx264") // Video using x264
                    .setVideoFrameRate(24, 1) // at 24 frames per second
                    .setVideoResolution(640, 360) // at 640x360 resolution
                    .setVideoBitRate(700000)
                    .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
                    .done();

            FFmpegBuilder builder480 = new FFmpegBuilder()
                    .setInput(path) // Filename, or a FFmpegProbeResult
                    .overrideOutputFiles(true) // Override the output if it exists
                    .addOutput(filePathOutPut + "/" + outputName + "_480P." + Files.getFileExtension(path))
                    .setFormat(Files.getFileExtension(path).substring(0))
                    .disableSubtitle() // No subtiles
                    .setAudioChannels(1) // Mono audio
                    .setAudioCodec("aac") // using the aac codec
                    .setAudioSampleRate(48_000) // at 48KHz
                    .setAudioBitRate(128000) // at 32 kbit/s
                    .setVideoCodec("libx264") // Video using x264
                    .setVideoFrameRate(24, 1) // at 24 frames per second
                    .setVideoResolution(854, 480) // at 640x480 resolution
                    .setVideoBitRate(1250000)
                    .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
                    .done();

            FFmpegBuilder builder720 = new FFmpegBuilder()
                    .setInput(path) // Filename, or a FFmpegProbeResult
                    .overrideOutputFiles(true) // Override the output if it exists
                    .addOutput(filePathOutPut + "/" + outputName + "_720P." + Files.getFileExtension(path))
                    .setFormat(Files.getFileExtension(path).substring(0))
                    .disableSubtitle() // No subtiles
                    .setAudioChannels(1) // Mono audio
                    .setAudioCodec("aac") // using the aac codec
                    .setAudioSampleRate(48_000) // at 48KHz
                    .setAudioBitRate(128000) // at 32 kbit/s
                    .setVideoCodec("libx264") // Video using x264
                    .setVideoFrameRate(24, 1) // at 24 frames per second
                    .setVideoResolution(1280, 720) // at 1280x720 resolution
                    .setVideoBitRate(2500000)
                    .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
                    .done();

            LOGGER.info("Compressing .....");
            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder360).run();
            executor.createJob(builder480).run();
            executor.createJob(builder720).run();
            LOGGER.info("Done .....");
        } catch (IOException ex) {
            Logger.getLogger(VideoServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
