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
package com.att.aro.core.peripheral;

import java.util.Date;

import com.att.aro.core.peripheral.pojo.AlarmDumpsysTimestamp;

/** 
 * Method to set a reference time
 * Use ALARM_END_FILE elapsed realtime as dumpsys batteryinfo time reference.
 * 
 * set: dumpsysEpochTimestamp
 * 	dumpsysElapsedTimestamp
 *
 * Date: October 6, 2014
 */
public interface IAlarmDumpsysTimestampReader {
	AlarmDumpsysTimestamp readData(String directory, Date traceDateTime, 
			double traceDuration, String osVersion, double eventTime0);
}
