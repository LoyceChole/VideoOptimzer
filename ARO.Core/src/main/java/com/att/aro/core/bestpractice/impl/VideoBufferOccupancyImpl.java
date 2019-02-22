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

package com.att.aro.core.bestpractice.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nonnull;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.BufferOccupancyResult;
import com.att.aro.core.bestpractice.pojo.VideoUsage;
import com.att.aro.core.packetanalysis.pojo.BufferOccupancyBPResult;
import com.att.aro.core.packetanalysis.pojo.BufferTimeBPResult;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.videoanalysis.IVideoUsagePrefsManager;
import com.att.aro.core.videoanalysis.pojo.AROManifest;

/**
 * <pre>
 * VBP #3 Video Buffer Occupancy
 * 
 * Criteria: ARO will measure frequency and duration of video stream buffers
 * 
 * About: Buffer occupancy is the amount of video stored in RAM to help prevent interruption due to transmission delays, known as "buffering".
 * 
 * Result: Your video had X size buffer occupancy By managing buffer occupancy you can make sure it is not too long or too short.
 * 
 * Link: goes to a view of buffers.
 * 
 */

public class VideoBufferOccupancyImpl implements IBestPractice {
	@Value("${bufferOccupancy.title}")
	private String overviewTitle;

	@Value("${bufferOccupancy.detailedTitle}")
	private String detailTitle;

	@Value("${bufferOccupancy.desc}")
	private String aboutText;

	@Value("${bufferOccupancy.url}")
	private String learnMoreUrl;

	@Value("${bufferOccupancy.pass}")
	private String textResultPass;

	@Value("${bufferOccupancy.results}")
	private String textResults;

	@Value("${startUpDelay.init}")
	private String startUpDelayNotSet;

	@Value("${videoSegment.empty}")
	private String novalidManifestsFound;

	@Value("${video.noData}")
	private String noData;

	@Autowired
	private IVideoUsagePrefsManager videoUsagePrefs;
	
	@Value("${videoManifest.multipleManifestsSelected}")
	private String multipleManifestsSelected;
	
	@Value("${videoManifest.noManifestsSelected}")
	private String noManifestsSelected;
	
	@Value("${videoManifest.noManifestsSelectedMixed}")
	private String noManifestsSelectedMixed;
	
	@Value("${videoManifest.invalid}")
	private String invalidManifestsFound;

	@Nonnull
	private SortedMap<Double, AROManifest> manifestCollection = new TreeMap<>();
	
	@Nonnull
	VideoUsage videoUsage;

	private BufferOccupancyResult result;

	private int selectedManifestCount;
	private boolean hasSelectedManifest;

	@Nonnull
	private BPResultType bpResultType;

	private double maxBufferSet;
	private double maxBufferReached;

	private int invalidCount;

	public void updateVideoPrefMaxBuffer() {
		if (videoUsagePrefs.getVideoUsagePreference() != null) {
			maxBufferSet = videoUsagePrefs.getVideoUsagePreference().getMaxBuffer();
		}
	}

	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {

		result = new BufferOccupancyResult();

		init(result);

		videoUsage = tracedata.getVideoUsage();

		if (videoUsage != null) {
			manifestCollection = videoUsage.getAroManifestMap();
		}

		if (MapUtils.isNotEmpty(manifestCollection)) {
			bpResultType = BPResultType.CONFIG_REQUIRED;

			selectedManifestCount = videoUsage.getSelectedManifestCount();
			hasSelectedManifest = (selectedManifestCount > 0);
			invalidCount = videoUsage.getInvalidManifestCount();

			if (selectedManifestCount == 0) {
				if (invalidCount == manifestCollection.size()) {
					result.setResultText(invalidManifestsFound);
				} else if (invalidCount > 0) {
					result.setResultText(noManifestsSelectedMixed);
				} else {
					result.setResultText(noManifestsSelected);
				}
			} else if (selectedManifestCount > 1) {
				result.setResultText(multipleManifestsSelected);
			} else if (hasSelectedManifest) {

				BufferOccupancyBPResult bufferBPResult = tracedata.getBufferOccupancyResult();
				BufferTimeBPResult bufferTimeBPResult = tracedata.getBufferTimeResult();
				if (bufferBPResult != null && bufferBPResult.getBufferByteDataSet().size() > 0) {
					maxBufferReached = bufferBPResult.getMaxBuffer();// maxBufferReached is in KB (1024)
					maxBufferReached = maxBufferReached / 1024; // change to MB (2^20)
					List<Double> bufferDataSet = bufferBPResult.getBufferByteDataSet();
					result.setMinBufferByte(bufferDataSet.get(0) / 1024);
					double bufferSum = bufferDataSet.stream().reduce((a, b) -> a + b).get();
					result.setAvgBufferByte((bufferSum / bufferDataSet.size()) / 1024);
				} else {
					maxBufferReached = 0;
				}
				if (bufferTimeBPResult != null && bufferTimeBPResult.getBufferTimeDataSet().size() > 0) {
					List<Double> bufferTimeDataSet = bufferTimeBPResult.getBufferTimeDataSet();
					result.setMinBufferTime(bufferTimeDataSet.get(0));
					result.setMaxBufferTime(bufferTimeDataSet.get(bufferTimeDataSet.size() - 1));
					double sum = bufferTimeDataSet.stream().reduce((a, b) -> a + b).get();
					result.setAvgBufferTime(sum / bufferTimeDataSet.size());
				}

				result.setSelfTest(true);
				result.setMaxBuffer(maxBufferReached);

				updateVideoPrefMaxBuffer();
//				double startupDelay = videoUsagePrefs.getVideoUsagePreference().getStartupDelay();
				double percentage = 0;
				if (maxBufferSet != 0) {
					percentage = (maxBufferReached / maxBufferSet) * 100;
				}

				if (MapUtils.isEmpty(videoUsage.getChunkPlayTimeList())) {
					result.setResultText(MessageFormat.format(startUpDelayNotSet, String.format("%.2f", percentage), String.format("%.2f", maxBufferReached),
							String.format("%.2f", maxBufferSet)));
					bpResultType = BPResultType.CONFIG_REQUIRED;
				} else {
					if (percentage > 100) {
						bpResultType = BPResultType.WARNING;
					}
					bpResultType = BPResultType.PASS;
					result.setResultText(MessageFormat.format(this.textResults, String.format("%.2f", percentage), String.format("%.2f", maxBufferReached),
							String.format("%.2f", maxBufferSet)));
				}
			}
		} else {
			result.setSelfTest(false);
			result.setResultText(noData);
			bpResultType = BPResultType.NO_DATA;
		}
		result.setResultType(bpResultType);
		return result;
	}

	private void init(BufferOccupancyResult result) {
		bpResultType = BPResultType.SELF_TEST;
		selectedManifestCount = 0;
		hasSelectedManifest = false;

		maxBufferSet = 0;
		maxBufferReached = 0;

		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setOverviewTitle(overviewTitle);
		result.setLearnMoreUrl(learnMoreUrl);
	}

}// end class