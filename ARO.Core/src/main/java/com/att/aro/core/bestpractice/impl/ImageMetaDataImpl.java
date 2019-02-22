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
package com.att.aro.core.bestpractice.impl;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.GenericImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.bytesource.ByteSourceFile;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageParser;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.ApplicationConfig;
import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.ImageMdataEntry;
import com.att.aro.core.bestpractice.pojo.ImageMdtaResult;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.util.ImageHelper;
import com.att.aro.core.util.Util;

public class ImageMetaDataImpl implements IBestPractice {
	@Value("${imageMetadata.title}")
	private String overviewTitle;

	@Value("${imageMetadata.detailedTitle}")
	private String detailTitle;

	@Value("${imageMetadata.desc}")
	private String aboutText;

	@Value("${imageMetadata.url}")
	private String learnMoreUrl;

	@Value("${imageMetadata.pass}")
	private String textResultPass;

	@Value("${imageMetadata.results}")
	private String textResults;

	@Value("${exportall.csvNumberOfMdataImages}")
	private String exportNumberOfMdataImages;
	
	@Value("${bestPractices.noData}")
	private String noData;

	private static final Logger LOG = LogManager.getLogger(ImageMetaDataImpl.class.getName());

	private boolean isMetaDataPresent = false;

	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		ImageMdtaResult result = new ImageMdtaResult();
		List<String> imageList = new ArrayList<String>();
		List<ImageMdataEntry> entrylist = new ArrayList<ImageMdataEntry>();
		
		String imagePath = tracedata.getTraceresult().getTraceDirectory()
				+ System.getProperty("file.separator") + "Image" + System.getProperty("file.separator");
		
		if (Util.isFilesforAnalysisAvailable(new File(imagePath))) {
			for (Session session : tracedata.getSessionlist()) {

				for (HttpRequestResponseInfo req : session.getRequestResponseInfo()) {

					if (req.getDirection() == HttpDirection.RESPONSE && req.getContentType() != null
							&& req.getContentType().contains("image/")) {

						String extractedImageName = ImageHelper.extractFullNameFromRRInfo(req);
						int pos = extractedImageName.lastIndexOf('.') + 1;

						// List<String> imageList = new ArrayList<String>();
						File folder = new File(imagePath);
						File[] listOfFiles = folder.listFiles();
						if (listOfFiles != null) {
							runTestForFiles(imageList, entrylist, session, req, imagePath, extractedImageName,
									pos, listOfFiles);
						}
					}
				}
			}

			result.setResults(entrylist);
			String text = "";
			if (entrylist.isEmpty()) {
				result.setResultType(BPResultType.PASS);
				text = MessageFormat.format(textResultPass, entrylist.size());
				result.setResultText(text);
			} else {
				result.setResultType(BPResultType.FAIL);
				text = MessageFormat.format(textResults, ApplicationConfig.getInstance().getAppShortName(),
						entrylist.size());
				result.setResultText(text);
			}
		} else {
			result.setResultText(noData);
			result.setResultType(BPResultType.NO_DATA);
		}
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportNumberOfMdataImages(String.valueOf(entrylist.size()));
		return result;
	}

	private void runTestForFiles(List<String> imageList, List<ImageMdataEntry> entrylist, Session session,
			HttpRequestResponseInfo req, String imagePath,  String extractedImageName, int pos,
			File[] listOfFiles) {
		String imgFullName = "";
		String imgExtn = "";
		String imgFile = "";

		// check folder exists
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				imgFullName = listOfFiles[i].getName();
				if (extractedImageName.equalsIgnoreCase(imgFullName)) {
					imgExtn = imgFullName.substring(pos, imgFullName.length());
					imgFile = imagePath + imgFullName;
					if (Util.isJPG(new File(imgFile), imgExtn)) {
						extractMetadata(imgFile);
					}
					// isMetaDataPresent = true;
				} // clear
			}

			if (isMetaDataPresent) {
				File getImage = new File(imgFile);
				JpegImageParser imgP = new JpegImageParser();
				byte[] mdata = null;
				long mSize = 0;

				try {
					mdata = imgP.getExifRawData(new ByteSourceFile(getImage));
					mSize = mdata.length;
				} catch (ImageReadException | IOException e) {

				}
				imageList.add(imgFile);

				long iSize = getImage.length();
 
				double savings = (mSize * 100) / iSize;

				if (savings >= 15.00) {
					entrylist.add(new ImageMdataEntry(req, session.getDomainName(), imgFile, Util.doubleFileSize(iSize), Util.doubleFileSize(mSize),
							String.valueOf(new DecimalFormat("##.##").format(savings)) + "%"));
				}
				isMetaDataPresent = false;
			}
		}
	}

	private void extractMetadata(String fullpath) {
		ImageMetadata metadata;
		try {
			metadata = Imaging.getMetadata(new File(fullpath));
			if (metadata != null) {
				if (!metadata.getClass().equals(GenericImageMetadata.class)) {
					JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
					TiffImageMetadata exif = jpegMetadata.getExif();
					if (exif != null) {
						isMetaDataPresent = true;
					}
				} else {
					GenericImageMetadata genMetadata = (GenericImageMetadata) metadata;
					if (genMetadata.getItems() != null && genMetadata.getItems().size() > 5) {
						isMetaDataPresent = true;
					} else if (genMetadata.getItems() != null && genMetadata.getItems().isEmpty()) {
						isMetaDataPresent = false;
					}
				}
			}
		} catch (IOException | ImageReadException imgException) {
			LOG.error(imgException.toString());
		}

	}
}
