/*
 *  Copyright 2019 AT&T
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
package com.att.aro.core.videoanalysis.impl;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.att.aro.core.AROConfig;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.util.IStringParse;
import com.att.aro.core.util.StringParse;
import com.att.aro.core.util.Util;
import com.att.aro.core.videoanalysis.pojo.ChildManifest;
import com.att.aro.core.videoanalysis.pojo.Manifest;
import com.att.aro.core.videoanalysis.pojo.Manifest.ContentType;
import com.att.aro.core.videoanalysis.pojo.Manifest.ManifestType;
import com.att.aro.core.videoanalysis.pojo.VideoEvent.VideoType;
import com.att.aro.core.videoanalysis.pojo.VideoFormat;

//@Data
public class ManifestBuilderHLS extends ManifestBuilder {

	protected static final Logger LOG = LogManager.getLogger(ManifestBuilderHLS.class.getName());
	protected static final Pattern pattern = Pattern.compile("^(#[A-Z0-9\\-]*)");

	enum HlsType {
		Parent, VodChild, LiveChild
	}

	ApplicationContext context = new AnnotationConfigApplicationContext(AROConfig.class);
	IStringParse stringParse = context.getBean(IStringParse.class);
	private String childUriName;
	private boolean adjustDurationNeeded = false;

	public ManifestBuilderHLS() {

	}
	
	@Override
	public String buildSegmentName(HttpRequestResponseInfo request, String extension) {
		String name = request.getObjNameWithoutParams();
		int dot = name.lastIndexOf('.');
		if (StringUtils.isNotEmpty(extension) && name.endsWith(extension)) {
			name = name.substring(0, name.length() - extension.length());
		}
		if (dot > -1) {
			int sep = name.substring(0, dot).lastIndexOf('/');
			if (sep > -1) {
				name = name.substring(sep + 1);
			}
		}
		name = StringUtils.replace(name, "/", "-");
		name = StringUtils.replace(name, ".", "-");
		return name + extension;
	}

	public String buildSegmentKey(String segmentFile) {
		String key = segmentFile;
		if (manifestCollection.getCommonBaseLength() > 0) {
			key = segmentFile.substring(0, manifestCollection.getCommonBaseLength());
		} else {
			int pos = segmentFile.lastIndexOf(".");
			if (pos > -1) {
				key = segmentFile.substring(0, pos);
			}
		}
		return key;
	}
	
	public void parseManifestData(Manifest newManifest, byte[] data) {

		if (data == null || data.length == 0) {
			LOG.error("Manifest file has no content");
			return;
		}
		String strData = new String(data);
		String[] sData = (new String(data)).split("\r\n");
		if (sData == null || sData.length == 1) {
			sData = (new String(data)).split("[\n\r]");
		}

		int scanLength = strData.length() > 500 ? 20 : strData.length();
		if (strData.substring(0, scanLength).contains("#EXTM3U")) {
			newManifest.setVideoType(VideoType.HLS);
			newManifest.setVideoFormat(VideoFormat.TS);
		} else if (sData != null) {
			LOG.error("Unrecognized Manifest:" + strData);
			return;
		} else {
			LOG.error("Unrecognized Manifest:" + strData);
			return;
		}
		adjustDurationNeeded = false;
		
		for (int itr = 0; itr < sData.length; itr++) {
			if (!sData[itr].startsWith("#")) {
				continue;
			}
			
			String[] flag = stringParse.parse(sData[itr], pattern);
			if (flag == null) {
				continue;
			}
			ContentType contentType;
			switch (flag[0]) {
			
			// Master & Child -------------------------------
			case "#EXTM3U": // FirstLine of a manifest
				String key = newManifest.getUri() != null ? newManifest.getUri().getRawPath() : "null";
				key = newManifest.getVideoName() != null ? newManifest.getVideoName() : "null";
				if (strData.contains("#EXT-X-STREAM-INF:")) {
					switchManifestCollection(newManifest, key, manifest.getRequestTime());
				} else if (strData.contains("#EXTINF:")) {
					if (manifestCollection == null) { 
						// special handling of childmanifest when there is no parent manifest
						switchManifestCollection(newManifest, key, manifest.getRequestTime());
					}
					newManifest.setManifestType(ManifestType.CHILD);
					newManifest.setMasterManifest(masterManifest);
					childManifest = locateChildManifest(newManifest);
					if (childManifest.getManifest() != null && childManifest.getManifest().getChecksumCRC32() == newManifest.getChecksumCRC32()) {
						LOG.info("Identical VOD child manifest found, skipping..." + newManifest.getVideoName());
						return;
					}
					childManifest.setManifest(newManifest);
					childManifest.getManifest().setVideoName(removeParamsFromStringURI(childManifest.getUriName()));
					if (childManifest.isVideo()) {
						if (childManifest.getCodecs().contains(",")) {
							childManifest.setContentType(ContentType.MUXED);
						} else {
							childManifest.setContentType(ContentType.VIDEO);
						}
					}
				}
				break;
				
			case "#EXT-X-VERSION": // Indicates the compatibility version of the playlist file
				break;

			// Master only -------------------------------
			case "#EXT-X-MEDIA": // Alternate MediaPlaylist
				// #EXT-X-MEDIA:TYPE=AUDIO,GROUP-ID="audio",NAME="eng",DEFAULT=YES,AUTOSELECT=YES,LANGUAGE="eng",URI="06.m3u8"
				// #EXT-X-MEDIA:TYPE=SUBTITLES,GROUP-ID="subs",NAME="caption_1",DEFAULT=NO,AUTOSELECT=YES,LANGUAGE="ENG",URI="114.m3u8"

				childUriName = StringParse.findLabeledDataFromString("URI=", "\"", sData[itr]);
				if (StringUtils.isNotEmpty(childUriName)) {
					LOG.info("MEDIA childUriName :" + childUriName);
					childManifest = createChildManifest(null, "", childUriName);
					childManifest.setVideo(false);
					switch (StringParse.findLabeledDataFromString("TYPE=", ",", sData[itr])) {
					case "AUDIO":
						contentType = ContentType.AUDIO;
						break;
					case "CLOSED-CAPTIONS":
						contentType = ContentType.SUBTITLES;
						break;
					default:
						contentType = ContentType.UNKNOWN;
						break;
					}
					childManifest.setContentType(contentType);
					manifest.setContentType(contentType);
				}				
				break;

			case "#EXT-X-STREAM-INF": // ChildManifest Map itr:metadata-Info, ++itr:childManifestName
				String childParameters = sData[itr];
				childUriName = sData[++itr];
				childManifest = manifestCollection.getChildManifest(childUriName);

				if (!newManifest.isVideoNameValidated()) {
					validateManifestVideoName(newManifest, childUriName);
				}
				
				if (childManifest == null) {
					childManifest = createChildManifest(null, childParameters, childUriName); // stored in childMap
				}
				break;

			case "#EXT-X-I-FRAME-STREAM-INF": // "keyframes" I_FrameInfo for fast-forward
				break;

			// child manifest only -------------------------------
			case "#EXT-X-PLAYLIST-TYPE":
				if ("VOD".equals(StringParse.findLabeledDataFromString("#EXT-X-PLAYLIST-TYPE", ":", sData[itr]))) {
					newManifest.setPlayListType(Manifest.StreamType.VOD);
				}
				break;

			case "#EXT-X-INDEPENDENT-SEGMENTS":
				break;
				
			case "#EXTINF": // Segment duration + media-file
				if (childManifest == null) {
					LOG.error("failed to locate child manifest " + sData[++itr]);
				} else {
					String parameters = sData[itr];
					String segmentUriName;
					if (sData[itr + 1].startsWith("#EXT-X-BYTERANGE:")) {
						segmentUriName = sData[++itr];
						++itr; // skip next line
					} else {
						segmentUriName = sData[++itr];
					}
					
					SegmentInfo segmentInfo = createSegmentInfo(childManifest, parameters, segmentUriName);
					
					String val = StringUtils.substringBefore(segmentUriName, ".");
					if (val.matches("^[0-9]*T[0-9]*$")) {
						adjustDurationNeeded = true;
						double nVal = (double) Util.parseForUTC(val);
						segmentInfo.setStartTime((nVal) / 1000);
					}
					if (childManifest.addSegment(segmentUriName, segmentInfo)) {
						manifestCollection.addToSegmentTrie(segmentUriName, segmentInfo);
						manifestCollection.addToTimestampChildManifestMap(manifest.getRequest().getTimeStamp(), childManifest);
						if (!manifestCollection.getSegmentChildManifestTrie().containsKey(segmentUriName)) {
							manifestCollection.addToSegmentChildManifestTrie(segmentUriName, childManifest);
							addToSegmentManifestCollectionMap(segmentUriName);
						}
					}
				}
				break;
			case "#EXT-X-ENDLIST": // Segment ENDLIST for VOD (STATIC)
				// processExtXendlist();
				break;

			case "#EXT-X-KEY": // Segment is encrypted using [METHOD=____]
				break;

			case "#EXT-X-MEDIA-SEQUENCE": // Indicates the sequence number of the first URL that appears in a playlist file
				double segmentStart = StringParse.findLabeledDoubleFromString("#EXT-X-MEDIA-SEQUENCE", ":", sData[itr]);
				childManifest.setSegmentCount((int)segmentStart);
				break;

			case "#EXT-X-TARGETDURATION": // Specifies the maximum media-file duration.
				// processExtXtargetDuration(line);
				break;

			case "#EXT-X-PROGRAM-DATE-TIME": // YYYY-MM-DDTHH:MM:sss
				String dateString = StringParse.findLabeledDataFromString("#EXT-X-PROGRAM-DATE-TIME:", "\\$", sData[itr]);
				dateString = dateString.replaceAll("\\+00\\:00", "000Z");
				long programDateTime = Util.parseForUTC(dateString);
				masterManifest.updateStreamProgramDateTime(programDateTime);
				childManifest.setStreamProgramDateTime(programDateTime);
				break;

			case "#USP-X-MEDIA": // #USP-X-MEDIA:TYPE=VIDEO,GROUP-ID="video-avc1-157",LANGUAGE="en",NAME="English",AUTOSELECT=YES,BANDWIDTH=209000,CODECS="avc1.4D401E",RESOLUTION=768x432
				break;

			case "#USP-X-TIMESTAMP-MAP": // #USP-X-TIMESTAMP-MAP:MPEGTS=900000,LOCAL=1970-01-01T00:00:00Z
				break;

			default:
				break;
			}
		}
		
		if (adjustDurationNeeded ) {
			childManifest.adjustDurations();
		}
		assignQuality(manifestCollection);
		
	}

	@Override
	protected ChildManifest createChildManifest(Manifest manifest, String parameters, String childUriName) {
		ChildManifest childManifest = new ChildManifest();
		childManifest.setManifest(manifest);
		childManifest.setUriName(childUriName);

		if (StringUtils.isNotEmpty(parameters)) {
			Double bandwidth = StringParse.findLabeledDoubleFromString("BANDWIDTH=", parameters);
			if (bandwidth != null) {
				manifestCollection.addToBandwidthMap(bandwidth, childManifest);
				childManifest.setBandwidth(bandwidth);
			}

			String resolution = StringParse.findLabeledDataFromString("RESOLUTION=", ",", parameters);
			if (StringUtils.isNotEmpty(resolution)) {
				String[] resolutionNumbers = resolution.split("[Xx]");
				if (resolutionNumbers.length == 2) {
					try {
						childManifest.setPixelWidth(Integer.valueOf(resolutionNumbers[0]));
						childManifest.setPixelHeight(Integer.valueOf(resolutionNumbers[1]));
					} catch (NumberFormatException e) {
						LOG.error(String.format("failed to parse \"%s\" or \"%s\"", resolutionNumbers[0], resolutionNumbers[1]));
						childManifest.setPixelWidth(0);
						childManifest.setPixelHeight(0);
					}
					childManifest.setVideo(true);
					childManifest.setContentType(ContentType.VIDEO);
				}
			}
			
			String codecs = StringParse.findLabeledDataFromString("CODECS=\"", "\"", parameters);
			childManifest.setCodecs(codecs);
			
			if (codecs.contains(",")) {
				childManifest.setVideo(true);
				childManifest.setContentType(ContentType.MUXED);
			} else if (codecs.contains("v")) {
				childManifest.setVideo(true);
				childManifest.setContentType(ContentType.VIDEO);
			} else if (codecs.contains("a")) {
				childManifest.setVideo(false);
				childManifest.setContentType(ContentType.AUDIO);
			} else {
				childManifest.setVideo(false);
				childManifest.setContentType(ContentType.UNKNOWN);
			}
		}
		childUriName = childUriName.replaceAll("%2f", "/");
		manifestCollection.addToUriNameChildMap(StringUtils.countMatches(childUriName, "/"), childUriName, childManifest);
		return childManifest;
	}

	private String removeParamsFromStringURI(String stringURI) {
		if (StringUtils.isEmpty(stringURI)) {
			return "";
		}
		String[] split = (stringURI).split("\\?");
		return split.length > 1 ? split[0] : stringURI;
	}
	
	@Override
	public String toString() {
		StringBuilder strblr = new StringBuilder("\nManifestBuilderHLS :\n");
		strblr.append(super.toString());
		return strblr.toString();
	}

}
