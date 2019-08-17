package com.imooc.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MergeVideoMp3 {

	private String ffmpegEXE;
	
	public MergeVideoMp3(String ffmpegEXE) {
		super();
		this.ffmpegEXE = ffmpegEXE;
	}
	
	public void convertor(String videoInputPath, String mp3InputPath,
			double seconds, String videoOutputPath) throws Exception {
//		ffmpeg.exe -i ceshi.mp4 -i bgm.mp3 -t 15 -y -c:v copy -c:a aac -strict experimental -map 0:v:0 -map 1:a:0 new1.mp4
		List<String> command = new ArrayList<>();
		command.add(ffmpegEXE);
		command.add("-i");
		command.add(videoInputPath);
		command.add("-i");
		command.add(mp3InputPath);
		command.add("-t");
		command.add(String.valueOf(seconds));

		command.add("-y");
		command.add("-c:v");
		command.add("copy");
		command.add("-c:a");
		command.add("aac");
		command.add("-strict");
		command.add("experimental");
		command.add("-map");
		command.add("0:v:0");
		command.add("-map");
		command.add("1:a:0");
		command.add(videoOutputPath);


	for (String c : command) {
		System.out.print(c + " ");
	}
		
		ProcessBuilder builder = new ProcessBuilder(command);
		Process process = builder.start();
		
		InputStream errorStream = process.getErrorStream();
		InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
		BufferedReader br = new BufferedReader(inputStreamReader);
		
		String line = "";
		while ( (line = br.readLine()) != null ) {
		}
		
		if (br != null) {
			br.close();
		}
		if (inputStreamReader != null) {
			inputStreamReader.close();
		}
		if (errorStream != null) {
			errorStream.close();
		}
		
	}

	public static void main(String[] args) {
		MergeVideoMp3 ffmpeg = new MergeVideoMp3("E:\\FFmpeg\\ffmpeg\\bin\\ffmpeg.exe");
		try {
			ffmpeg.convertor("E:\\FFmpeg\\测试视频\\123.mp4", "E:\\FFmpeg\\测试视频\\bgm.mp3", 15, "E:\\FFmpeg\\测试视频\\new1.mp4");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
