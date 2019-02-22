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
package com.att.aro.ui.view.diagnostictab.plot;

import java.text.MessageFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceResultType;
import com.att.aro.core.peripheral.pojo.RadioInfo;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class RadioPlot implements IPlot {
	private static final Logger LOGGER = LogManager.getLogger(RadioPlot.class);	

	private static final int MIN_SIGNAL = -121;
	List<RadioInfo> radioInfos;

	@Override
	public void populate(XYPlot plot, AROTraceData analysis) {
		XYSeries series = new XYSeries(0);

		if (analysis == null) {
			LOGGER.info("no trace data here");
		} else {
			TraceResultType resultType = analysis.getAnalyzerResult()
					.getTraceresult().getTraceResultType();

			if (resultType.equals(TraceResultType.TRACE_FILE)) {
				LOGGER.info("no trace folder data here");
			} else {

				TraceDirectoryResult traceResult = (TraceDirectoryResult) analysis
						.getAnalyzerResult().getTraceresult();
				radioInfos = traceResult.getRadioInfos();

				if (radioInfos.size() > 0
						&& analysis.getAnalyzerResult().getFilter()
								.getTimeRange() != null) {
					RadioInfo first = radioInfos.get(0);
					series.add(
							analysis.getAnalyzerResult().getFilter()
									.getTimeRange().getBeginTime(),
							first.getSignalStrength() < 0 ? first
									.getSignalStrength() : MIN_SIGNAL);
				}
				for (RadioInfo ri : radioInfos) {
					series.add(ri.getTimeStamp(),
							ri.getSignalStrength() < 0 ? ri.getSignalStrength()
									: MIN_SIGNAL);
				}
				if (radioInfos.size() > 0) {
					RadioInfo last = radioInfos.get(radioInfos.size() - 1);
					if (analysis.getAnalyzerResult().getFilter().getTimeRange() != null) {
						series.add(
								analysis.getAnalyzerResult().getFilter()
										.getTimeRange().getEndTime(),
								last.getSignalStrength() < 0 ? last
										.getSignalStrength() : MIN_SIGNAL);
					} else {
						series.add(
								traceResult.getTraceDuration(),
								last.getSignalStrength() < 0 ? last
										.getSignalStrength() : MIN_SIGNAL);
					}
				}

				// Assign ToolTip to renderer
				XYItemRenderer renderer = plot.getRenderer();
				renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {

					@Override
					public String generateToolTip(XYDataset dataset,
							int series, int item) {

						RadioInfo ri = radioInfos.get(Math.min(item,
								radioInfos.size() - 1));
						if (ri.getSignalStrength() < 0) {
							if (ri.isLte()) {
								return MessageFormat.format(
										ResourceBundleHelper
												.getMessageString("radio.tooltip.lte"),
										ri.getLteRsrp(), ri.getLteRsrq());
							} else {
								return MessageFormat.format(
										ResourceBundleHelper
												.getMessageString("radio.tooltip"),
										ri.getSignalStrength());
							}
						} else {
							return ResourceBundleHelper
									.getMessageString("radio.noSignal");
						}
					}

				});

			}
		}
		plot.setDataset(new XYSeriesCollection(series));
//		return plot;
	}

}
