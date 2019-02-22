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
package com.att.aro.core.videoanalysis.pojo.mpdplayerady;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Period")

public class PeriodPR {

	List<AdaptationSetPR> adaptationSet;

	@Override
	public String toString() {
		StringBuilder strblr = new StringBuilder(83);
		strblr.append("\n\tPeriod:");
		strblr.append(adaptationSet);
//		for(AdaptationSetPR set:adaptationSet){
//			strblr.append("\n\t\tAdaptationSetPR :"); strblr.append( set);
//		}
		return strblr.toString();
	}

	@XmlElement(name = "AdaptationSet")
	public List<AdaptationSetPR> getAdaptationSet() {
		return adaptationSet;
	}

	public void setAdaptationSet(List<AdaptationSetPR> adaptationSet) {
		this.adaptationSet = adaptationSet;
	}

}