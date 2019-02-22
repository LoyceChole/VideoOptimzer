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
package com.att.aro.core.peripheral.pojo;

import java.util.List;
import java.util.Map;

import com.att.aro.core.packetanalysis.pojo.ScheduledAlarmInfo;

public class AlarmAnalysisResult {
	Map<String, List<ScheduledAlarmInfo>> scheduledAlarms;
	List<AlarmAnalysisInfo> statistics;
	public Map<String, List<ScheduledAlarmInfo>> getScheduledAlarms() {
		return scheduledAlarms;
	}
	public void setScheduledAlarms(
			Map<String, List<ScheduledAlarmInfo>> scheduledAlarms) {
		this.scheduledAlarms = scheduledAlarms;
	}
	public List<AlarmAnalysisInfo> getStatistics() {
		return statistics;
	}
	public void setStatistics(List<AlarmAnalysisInfo> statistics) {
		this.statistics = statistics;
	}
	
}
