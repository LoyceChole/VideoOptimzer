/*
 *  Copyright 2017 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.att.aro.datacollector.ioscollector.reader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

/**
 * To help execute external program and consume data returned back as string or
 * bytes.
 *
 */
public class ExternalProcessRunner {

	private static final Logger LOG = LogManager.getLogger(ExternalProcessRunner.class.getName());
	ProcessBuilder builder = null;

	public ExternalProcessRunner() {
		builder = new ProcessBuilder("");
	}

	public ExternalProcessRunner(ProcessBuilder builder) {
		this.builder = builder;
	}

	/**
	 * Execute command passed in as a string and return string back.
	 */
	public String runGetString(String command) throws IOException {
		ByteArrayOutputStream data = this.run(command);
		String out = "";
		if (data != null) {
			out = data.toString();
			data.close();
			return out;
		}
		return null;
	}

	/**
	 * Execute command passed in as a array of string and return string back.
	 */
	public String runCmd(String[] command) throws IOException {

		Process process = Runtime.getRuntime().exec(command);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream input = process.getInputStream();
		byte[] data = new byte[1024];
		int totalread = -1;

		while ((totalread = input.read(data, 0, data.length)) != -1) {
			out.write(data, 0, totalread);
		}

		if (input != null) {
			input.close();
		}
		if (process != null) {
			process.destroy();
		}
		String datastr = null;
		if (out != null) {
			datastr = out.toString();
			out.close();
		}

		return datastr;
	}
	
	/**
	 * Execute command passed in as a array of string and return string back.
	 */
	public String runCmd(String command) throws IOException {
		
		Process process = Runtime.getRuntime().exec(command);
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream input = process.getInputStream();
		byte[] data = new byte[1024];
		int totalread = -1;
		
		while ((totalread = input.read(data, 0, data.length)) != -1) {
			out.write(data, 0, totalread);
		}
		
		if (input != null) {
			input.close();
		}
		if (process != null) {
			process.destroy();
		}
		String datastr = null;
		if (out != null) {
			datastr = out.toString();
			out.close();
		}
		return datastr;
	}

	public String runCmdWithTimeout(String[] command, long timeout) throws IOException {
		ProcessBuilder processbuild = builder.command(command);
		Process process = processbuild.start();
		ProcessWorker worker = new ProcessWorker(process, 3000);
		worker.start();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream input = process.getInputStream();
		byte[] data = new byte[1024];
		int totalread = -1;

		while ((totalread = input.read(data, 0, data.length)) != -1) {
			out.write(data, 0, totalread);
			LOG.info(new String(data));
		}
		worker.setExit();
		worker.interrupt();
		worker = null;
		if (input != null) {
			input.close();
		}
		if (process != null) {
			process.destroy();
		}
		String datastr = null;
		if (out != null) {
			datastr = out.toString();
			out.close();
		}

		return datastr;
	}

	public String runCmdInDirectory(String[] command, String directoryPath) throws IOException {
		builder.command(command);
		builder.directory(new File(directoryPath));
		Process process = builder.start();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream input = process.getInputStream();
		byte[] data = new byte[1024];
		int totalread = -1;

		while ((totalread = input.read(data, 0, data.length)) != -1) {
			out.write(data, 0, totalread);
		}

		if (input != null) {
			input.close();
		}
		if (process != null) {
			process.destroy();
		}
		String datastr = null;
		if (out != null) {
			datastr = out.toString();
			out.close();
		}

		return datastr;
	}

	/**
	 * Execute command passed in as a string and return ByteArrayOutputStream
	 * object back.
	 */
	public ByteArrayOutputStream run(String command) throws IOException {

		builder.command(command);
		Process process = builder.start();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream input = process.getInputStream();
		byte[] data = new byte[1024];
		int totalread = -1;

		while ((totalread = input.read(data, 0, data.length)) != -1) {
			out.write(data, 0, totalread);
		}

		if (input != null) {
			input.close();
		}
		if (process != null) {
			process.destroy();
		}
		return out;
	}

}