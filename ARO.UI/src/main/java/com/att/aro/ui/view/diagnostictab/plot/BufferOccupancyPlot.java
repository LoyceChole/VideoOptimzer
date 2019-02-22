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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.att.aro.core.packetanalysis.pojo.BufferOccupancyBPResult;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.core.videoanalysis.PlotHelperAbstract;
import com.att.aro.core.videoanalysis.impl.BufferOccupancyCalculatorImpl;
import com.att.aro.core.videoanalysis.pojo.VideoEvent;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.utils.ResourceBundleHelper;


public class BufferOccupancyPlot implements IPlot {

	private static final String BUFFEROCCUPANCY_TOOLTIP = ResourceBundleHelper.getMessageString("bufferoccupancy.tooltip");
	private Shape shape  = new Ellipse2D.Double(0,0,10,10);

	private List<Double> bufferSizeList = new ArrayList<>();
	
	XYSeriesCollection bufferFillDataCollection = new XYSeriesCollection();
	XYSeries seriesBufferFill;
	Map<Integer,String> seriesDataSets; 
	BufferOccupancyCalculatorImpl bufferOccupancyCalculatorImpl= (BufferOccupancyCalculatorImpl) ContextAware.getAROConfigContext().getBean("bufferOccupancyCalculatorImpl",PlotHelperAbstract.class);
	Map<VideoEvent,Double> chunkPlayTimeList;
	
	public void setChunkPlayTimeList(Map<VideoEvent,Double> chunkPlayTime){
		this.chunkPlayTimeList = chunkPlayTime;
	}
	
	public void clearPlot(XYPlot plot){
		plot.setDataset(null);	
	}
	

	@Override
	public void populate(XYPlot plot, AROTraceData analysis) {

		if (analysis != null) {
			
			bufferFillDataCollection.removeAllSeries();
			seriesBufferFill = new XYSeries("Buffer Fill");
			seriesDataSets = new TreeMap<>();
			
			seriesDataSets = bufferOccupancyCalculatorImpl.populateBufferOccupancyDataSet(analysis.getAnalyzerResult().getVideoUsage(),chunkPlayTimeList);
			bufferSizeList.clear();
			
			double xCoordinate,yCoordinate;
			String ptCoordinate[] = new String[2]; // to hold x & y values
			if(!seriesDataSets.isEmpty()){

				for(int key :seriesDataSets.keySet()){
					ptCoordinate = seriesDataSets.get(key).trim().split(",");
					xCoordinate = Double.parseDouble(ptCoordinate[0]);
					yCoordinate = Double.parseDouble(ptCoordinate[1]);
					yCoordinate = yCoordinate/1024; //Converting Buffer size measurement unit to KB
					bufferSizeList.add(yCoordinate);
					seriesBufferFill.add(xCoordinate,yCoordinate);
				}			
			}
			Collections.sort(bufferSizeList);
			BufferOccupancyBPResult bufferOccupancyResult = bufferOccupancyCalculatorImpl.setMaxBuffer(bufferSizeList.get(bufferSizeList.size()-1));
			bufferOccupancyResult.setBufferByteDataSet(bufferSizeList);
			analysis.getAnalyzerResult().setBufferOccupancyResult(bufferOccupancyResult);
			// populate collection
			bufferFillDataCollection.addSeries(seriesBufferFill);

			XYItemRenderer renderer = new StandardXYItemRenderer();
			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {

				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {

					// Tooltip value
					Number timestamp = dataset.getX(series, item);
					Number bufferSize = dataset.getY(series, item);
					StringBuffer tooltipValue = new StringBuffer();
					tooltipValue.append(String.format("%.2f", (double) bufferSize / 1024) + "," + String.format("%.2f", timestamp));
 
				    String[] value = tooltipValue.toString().split(",");
					return (MessageFormat.format(BUFFEROCCUPANCY_TOOLTIP,value[0],value[1]));
				}

			});
			renderer.setSeriesStroke(0, new BasicStroke(2.0f));
			renderer.setSeriesPaint(0, Color.blue);
			renderer.setSeriesShape(0, shape);
			
			plot.setRenderer(renderer);

		}
		plot.setDataset(bufferFillDataCollection);
	}


}
