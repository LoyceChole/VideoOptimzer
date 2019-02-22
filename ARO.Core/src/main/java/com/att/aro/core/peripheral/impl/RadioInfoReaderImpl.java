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
import com.att.aro.core.peripheral.IRadioInfoReader;
import com.att.aro.core.peripheral.pojo.RadioInfo;
import com.att.aro.core.util.Util;

/**
 * Reads the Radio data from the file and stores it in the RadioInfo.
 * Date: October 2, 2014
 *
 */
public class RadioInfoReaderImpl extends PeripheralBase implements IRadioInfoReader {

	private static final Logger LOGGER = LogManager.getLogger(RadioInfoReaderImpl.class.getName());

	@Override
	public List<RadioInfo> readData(String directory, double startTime) {
		List<RadioInfo> radioInfos = new ArrayList<RadioInfo>();
		String filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.RADIO_EVENTS_FILE;
		String[] lines = null;
		if (!filereader.fileExist(filepath)) {
			return radioInfos;
		}
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e1) {
			LOGGER.error("failed to read Radio info file: "+filepath);
		}
		
		Double lastDbmValue = null;
		if(lines != null && lines.length > 0){
			for (String strLineBuf : lines) {
	
				String[] strFields = strLineBuf.split(" ");
				try {
					if (strFields.length == 2) {
						double timestampVal = Util.normalizeTime(Double.parseDouble(strFields[0]),startTime);
						double dbmValue = Double.parseDouble(strFields[1]);
	
						// Special handling for lost or regained signal
						if (lastDbmValue != null && timestampVal > 0.0
								&& (dbmValue >= 0.0 || lastDbmValue.doubleValue() >= 0.0)
								&& dbmValue != lastDbmValue.doubleValue()) {
							radioInfos.add(new RadioInfo(timestampVal, lastDbmValue.doubleValue()));
						}
	
						// Add radio event
						radioInfos.add(new RadioInfo(timestampVal, dbmValue));
						lastDbmValue = dbmValue;
					} else if (strFields.length == 6) {
	
						// LTE
						double timestampVal = Util.normalizeTime(Double.parseDouble(strFields[0]),startTime);
						RadioInfo radioInformation = new RadioInfo(timestampVal, Integer.parseInt(strFields[1]),
								Integer.parseInt(strFields[2]), Integer.parseInt(strFields[3]),
								Integer.parseInt(strFields[4]), Integer.parseInt(strFields[5]));
	
						// Special handling for lost or regained signal
						if (lastDbmValue != null
								&& timestampVal > 0.0
								&& (radioInformation.getSignalStrength() >= 0.0 || lastDbmValue.doubleValue() >= 0.0)
								&& radioInformation.getSignalStrength() != lastDbmValue.doubleValue()) {
							radioInfos.add(new RadioInfo(timestampVal, lastDbmValue.doubleValue()));
						}
	
						// Add radio event
						radioInfos.add(radioInformation);
						lastDbmValue = radioInformation.getSignalStrength();
	
					} else {
						LOGGER.warn("Invalid radio_events entry: " + strLineBuf);
					}
				} catch (Exception e) {
					LOGGER.warn("Unexpected error parsing radio event: " + strLineBuf, e);
				}
			}
		}
		return radioInfos;
	}

}
