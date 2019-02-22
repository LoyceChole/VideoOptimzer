/*
 *  Copyright 2014 AT&T
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
package com.att.aro.core.peripheral.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.peripheral.ICameraInfoReader;
import com.att.aro.core.peripheral.pojo.CameraInfo;
import com.att.aro.core.peripheral.pojo.CameraInfo.CameraState;
import com.att.aro.core.util.Util;

/**
 * Method to read the Camera data from the trace file and store it in the
 * cameraInfos list. It also updates the active duration for Camera.
 * Date: October 1, 2014
 */
public class CameraInfoReaderImpl extends PeripheralBase implements ICameraInfoReader {
	private static final Logger LOGGER = LogManager.getLogger(CameraInfoReaderImpl.class.getName());
	
	private double activeDuration = 0;
	@Override
	public List<CameraInfo> readData(String directory, double startTime,
			double traceDuration) {
		this.activeDuration = 0;
		List<CameraInfo> cameraInfos = new ArrayList<CameraInfo>();
		String filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.CAMERA_FILE;
		
		if (!filereader.fileExist(filepath)) {
			return cameraInfos;
		}
		
		double beginTime = 0.0;
		double endTime;
		double dLastActiveTimeStamp = 0.0;
		double dActiveDuration = 0.0;
		CameraState prevCameraState = null;
		CameraState cameraState = null;
		String firstLine;
		String strLineBuf;
		String[] lines = null;
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e1) {
			LOGGER.error("failed to read Camera info file: "+filepath);
		}
		
		if (lines != null && lines.length > 0) {
			firstLine = lines[0];
			String strFieldsFirstLine[] = firstLine.split(" ");
			if (strFieldsFirstLine.length == 2) {
				try {
					beginTime = Util.normalizeTime(Double.parseDouble(strFieldsFirstLine[0]),startTime);
					if (TraceDataConst.CAMERA_ON.equals(strFieldsFirstLine[1])) {
						prevCameraState = CameraState.CAMERA_ON;
						if (0.0 == dLastActiveTimeStamp) {
							dLastActiveTimeStamp = beginTime;
						}
					} else if (TraceDataConst.CAMERA_OFF.equals(strFieldsFirstLine[1])) {
						prevCameraState = CameraState.CAMERA_OFF;
					} else {
						LOGGER.warn("Unknown camera state: " + firstLine);
						prevCameraState = CameraState.CAMERA_UNKNOWN;
					}

					if ((!TraceDataConst.CAMERA_ON.equals(strFieldsFirstLine[1]))
							&& dLastActiveTimeStamp > 0.0) {
						dActiveDuration += (beginTime - dLastActiveTimeStamp);
						dLastActiveTimeStamp = 0.0;
					}
				} catch (Exception e) {
					LOGGER.warn("Unexpected error in camera events: " + firstLine, e);
				}
			} else {
				LOGGER.warn("Unrecognized camera event: " + firstLine);
			}

			for (int i=1;i<lines.length;i++) {
				strLineBuf = lines[i];
				String strFields[] = strLineBuf.split(" ");
				if (strFields.length == 2) {
					try {
						endTime = Util.normalizeTime(Double.parseDouble(strFields[0]),startTime);
						if (TraceDataConst.CAMERA_ON.equals(strFields[1])) {
							cameraState = CameraState.CAMERA_ON;
							if (0.0 == dLastActiveTimeStamp) {
								dLastActiveTimeStamp = endTime;
							}
						} else if (TraceDataConst.CAMERA_OFF.equals(strFields[1])) {
							cameraState = CameraState.CAMERA_OFF;
						} else {
							LOGGER.warn("Unknown camera state: " + strLineBuf);
							cameraState = CameraState.CAMERA_UNKNOWN;
						}
						cameraInfos.add(new CameraInfo(beginTime, endTime, prevCameraState));

						if ((!TraceDataConst.CAMERA_ON.equals(strFields[1])) && dLastActiveTimeStamp > 0.0) {
							dActiveDuration += (endTime - dLastActiveTimeStamp);
							dLastActiveTimeStamp = 0.0;
						}
						prevCameraState = cameraState;
						beginTime = endTime;

					} catch (Exception e) {
						LOGGER.warn("Unexpected error in camera events: "+ strLineBuf, e);
					}
				} else {
					LOGGER.warn("Unrecognized camera event: " + strLineBuf);
				}
			}
			cameraInfos.add(new CameraInfo(beginTime, traceDuration, prevCameraState));

			// Duration calculation should probably be done in analysis
			if (cameraState == CameraState.CAMERA_ON) {
				dActiveDuration += Math.max(0, traceDuration - dLastActiveTimeStamp);
			}

			this.activeDuration = dActiveDuration;
		}
		
		return cameraInfos;
	}

	@Override
	public double getActiveDuration() {
		return this.activeDuration;
	}

}
