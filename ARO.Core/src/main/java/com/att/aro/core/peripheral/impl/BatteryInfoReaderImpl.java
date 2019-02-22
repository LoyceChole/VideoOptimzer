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
import com.att.aro.core.peripheral.IBatteryInfoReader;
import com.att.aro.core.peripheral.pojo.BatteryInfo;
import com.att.aro.core.util.Util;

/**
 * Method to read the Battery data from the trace file and store it in the
 * batteryInfos list.
 * 
 * Date: October 7, 2014
 */
public class BatteryInfoReaderImpl extends PeripheralBase implements IBatteryInfoReader {
	
	private static final Logger LOGGER = LogManager.getLogger(BatteryInfoReaderImpl.class.getName());

	@Override
	public List<BatteryInfo> readData(String directory, double startTime) {
		List<BatteryInfo> batteryInfos = new ArrayList<BatteryInfo>();
		int previousLevel = 0;
		int previousTemp = 0;
		boolean previousState = false;
		String filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.BATTERY_FILE;

		if (!filereader.fileExist(filepath)) {
			return batteryInfos;
		}
		String[] lines = null;
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e1) {
			LOGGER.error("failed to open file for battery info: " + filepath);
		}
		if (lines != null) {
			for (String strLineBuf : lines) {
				String strFields[] = strLineBuf.split(" ");
				if (strFields.length == 4) {
					try {
						double bTimeStamp = Util.normalizeTime(Double.parseDouble(strFields[0]), startTime);
						int bLevel = Integer.parseInt(strFields[1]);
						int bTemp = Integer.parseInt(strFields[2]);
						boolean bState = Boolean.valueOf(strFields[3]);

						// Checks to make sure that the new line is not the same
						// as the previous line so duplicate points arn't
						// plotted
						if ((bLevel != previousLevel) || (bTemp != previousTemp) || (bState != previousState)) {
							batteryInfos.add(new BatteryInfo(bTimeStamp, bState, bLevel, bTemp));
						}

						previousLevel = Integer.parseInt(strFields[1]);
						previousTemp = Integer.parseInt(strFields[2]);
						previousState = Boolean.valueOf(strFields[3]);

					} catch (Exception e) {
						LOGGER.warn("Unexpected error parsing battery event: " + strLineBuf, e);
					}
				} else {
					LOGGER.warn("Invalid battery_events entry: " + strLineBuf);
				}
			}
		}
		return batteryInfos;
	}

}
