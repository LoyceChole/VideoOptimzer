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
package com.att.aro.core.peripheral.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.peripheral.IAlarmDumpsysTimestampReader;
import com.att.aro.core.peripheral.pojo.AlarmDumpsysTimestamp;
import com.att.aro.core.util.Util;

/**
 * Method to set a reference time Use ALARM_END_FILE elapsed realtime as dumpsys
 * batteryinfo time reference.
 * 
 * set: dumpsysEpochTimestamp dumpsysElapsedTimestamp
 *
 * Date: October 6, 2014
 */
public class AlarmDumpsysTimestampReaderImpl extends PeripheralBase implements IAlarmDumpsysTimestampReader {

	private static final Logger LOGGER = LogManager.getLogger(AlarmDumpsysTimestampReaderImpl.class.getName());

	@Override
	public AlarmDumpsysTimestamp readData(String directory
										, Date traceDateTime
										, double traceDuration
										, String osVersion
										, double eventTime0) {
		
		String filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.ALARM_END_FILE;
		boolean useStartFile = false;
		double dumpsysEpochTimestamp = 0.0;
		double dumpsysElapsedTimestamp = 0.0;
		if (!filereader.fileExist(filepath)) {

			// alarm_info_end does not exist, try alarm_info_start
			useStartFile = true;
			filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.ALARM_START_FILE;
			if (!filereader.fileExist(filepath)) {
				if (traceDateTime == null) {
					LOGGER.debug("traceDateTime is null");
				} else {
					LOGGER.debug("traceDuration: " + traceDuration);
					/**
					 * Neither of the alarm dumpsys files exist fall back to use
					 * file "time" written when trace start
					 */
					dumpsysEpochTimestamp = traceDateTime.getTime() + traceDuration * 1000;
					dumpsysElapsedTimestamp = eventTime0 + traceDuration * 1000;
				}
				return null;
			}
		}
		dumpsysEpochTimestamp = 0;
		String[] lines = null;
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e1) {
			LOGGER.error("failed to open alarm dumpsys timestamp file: " + filepath);
			return null;
		}

		for (String strLineBuf : lines) {
			if (dumpsysEpochTimestamp <= 0 && strLineBuf.indexOf("Realtime wakeup") > 0) {
				String realTime[] = strLineBuf.split("=");
				if (osVersion != null && osVersion.compareTo("2.3") < 0) {
					dumpsysEpochTimestamp = Double.parseDouble(realTime[1].trim().substring(0, realTime[1].length() - 2));
				} else {
					DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						dumpsysEpochTimestamp = format.parse(realTime[1].trim().substring(0, realTime[1].length() - 2)).getTime();
					} catch (ParseException e) {
						LOGGER.warn("ParseException ", e);
					}
				}
			}
			if (strLineBuf.indexOf("Elapsed realtime wakeup") > 0) {
				String strFields[] = strLineBuf.split("=");

				// pre-gingerbread devices does not has a "+" sign before the elapsed timestamp(ms)
				if (osVersion != null && osVersion.compareTo("2.3") < 0) {
					dumpsysElapsedTimestamp = Double.parseDouble(strFields[1].trim().substring(0, strFields[1].length() - 2));
				} else {
					dumpsysElapsedTimestamp = Util.convertTime(strFields[1].trim().substring(1, strFields[1].length() - 2));
				}
				break;
			}
		}
		if (useStartFile) {
			dumpsysEpochTimestamp += traceDuration * 1000;
			dumpsysElapsedTimestamp += traceDuration * 1000;
		}

		if (osVersion != null) {
			LOGGER.info("Elapsed realtime : " + dumpsysElapsedTimestamp + "\n\tEPOCH: " + dumpsysEpochTimestamp + "\n\tOS: " + osVersion);
		}
		AlarmDumpsysTimestamp time = new AlarmDumpsysTimestamp();
		time.setDumpsysElapsedTimestamp(dumpsysElapsedTimestamp);
		time.setDumpsysEpochTimestamp(dumpsysEpochTimestamp);
		return time;
	}

}
