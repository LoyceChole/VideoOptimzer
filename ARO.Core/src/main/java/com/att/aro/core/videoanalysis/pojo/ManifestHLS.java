package com.att.aro.core.videoanalysis.pojo;

import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.util.StringParse;
import com.att.aro.core.videoanalysis.pojo.VideoEvent.VideoType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

/**
 * Handles HLS manifests. Encapsulates both parent and multiple child manifests.
 * 
 * Use caution when loading manifests from outside GET requests. The videoName may not sync between the manifest and REGEX identified ID's 
 *
 */
public class ManifestHLS extends AROManifest {

	protected static final Logger LOG = LogManager.getLogger(AROManifest.class.getName());	

	private static final String EXTEN_TS = ".ts";
	int segCount = 0;
	
	/**
	 * Map containing HLS "Child" manifests.
	 * Child manifests cover a particular quality (track) level for information about segments
	 */
	@Setter(AccessLevel.NONE)
	private SortedMap<String, ChildManifest> childMap = new TreeMap<>();
	
	@Override
	public int getSegIncremental() {
		return segCount++;
	}

	public ManifestHLS(HttpRequestResponseInfo resp, String videoPath) {
		super(VideoType.HLS, resp, videoPath);
	}
	
	public ManifestHLS(HttpRequestResponseInfo resp, byte[] data, String videoPath) {
		super(VideoType.HLS, resp, videoPath);

		content = data;
		exten = EXTEN_TS; // default for HLS
		parseManifestData(data);
	}

	public ManifestHLS(HttpRequestResponseInfo resp, VideoEventData ved, byte[] data, String videoPath) {
		super(VideoType.HLS, resp, videoPath);

		content = data;
		exten = EXTEN_TS; // default for HLS
		
		singletonSetVideoName(ved.getName());

		parseManifestData(data);
	}
	
	/**
	 * makeup an ManifestHLS only from the video request information
	 * 
	 * @param resp
	 * @param ved from video request
	 * @param videoPath
	 */
	public ManifestHLS(HttpRequestResponseInfo resp, VideoEventData ved, String videoPath) {
		super(VideoType.HLS, resp, ved, videoPath);

		exten = EXTEN_TS; // default for HLS

		singletonSetVideoName(ved.getName());
//		adhocSegment(ved);
	}

	/**
	 * Add Segment into segmentList,durationList
	 * 
	 * @param ved
	 */
	@Override
	public void adhocSegment(VideoEventData ved) {
		String key;
		if (ved.getSegmentReference() != null) {
			key = ved.getSegmentReference();
		} else if (ved.getDateTime() != null) {
			key  = String.format("%s.%s", ved.getDateTime(), ved.getExtension());
		} else {
			return;
		}
		if (key != null && !segmentList.containsKey(key)) {
			key = String.format("%s.%s", ved.getDateTime(), ved.getExtension());
		}
		int lineSeg = -1;
		Integer seg = segmentList.get(key);
		if (seg == null) {
			lineSeg = getSegIncremental();
			ved.setSegment(lineSeg);
		} else {
			lineSeg = seg;
			ved.setSegment(seg);
		}
		segmentList.put(key, lineSeg);
		durationList.put(lineSeg, String.valueOf(getDuration()));
	}
	
	public void parseManifestData(byte[] data) {
		if (data == null || data.length == 0) {
			return;
		}
		String strData = new String(data);
		String[] sData = (new String(data)).split("\r\n");
		if (sData == null || sData.length == 1) {
			sData = (new String(data)).split("[\n\r]");
		}

		LOG.info("import Manifest:\n"+strData);
		
		if (encryption.isEmpty()) {
			encryption = StringParse.findLabeledDataFromString("#EXT-X-KEY:METHOD=", ",", strData);
			if (!encryption.isEmpty() && uriStr.isEmpty()) {
				uriStr = StringParse.findLabeledDataFromString("URI=\"", "\"", strData);
			}
		}
		
		if (duration == 0) {
			Double dblDuration = StringParse.findLabeledDoubleFromString("#EXTINF:", strData);
			if (dblDuration != null) {
				setDuration(dblDuration);
				timeScale = 1D;
			}
		}

		/*
		 * Load bitrateMap(format, bandwidth)
		 * set videoName ( if not set already )
		 */
		if (bitrateMap.isEmpty()) {
			Double bandwidth;
			String resolution;
			for (int itr = 0; itr < sData.length; itr++) {
				String line = sData[itr];

				// video bandwidth
				bandwidth = StringParse.findLabeledDoubleFromString("BANDWIDTH=", line);
				resolution = StringParse.findLabeledDataFromString("RESOLUTION=", ",", line);

				ChildManifest childManifest = null;
				if (StringUtils.isNotEmpty(resolution)) {
					String[] resolutionNumbers = resolution.split("[Xx]");
					if (resolutionNumbers.length == 2) {
						childManifest = new ChildManifest(bandwidth, resolutionNumbers[0], resolutionNumbers[1]);
					}
				}
				if (bandwidth != null && ++itr < sData.length) {

					String nameLine = sData[itr];

					String format = "";

					String[] capGroup = null;

					if (capGroup == null) {
						capGroup = stringParse.parse(nameLine, "channel\\/([a-z0-9]*)\\/([a-z])\\.m3u8"); // espn channel/852f3e08953a485d90f8c924a101b668/d.m3u8
						if (capGroup != null) {
							singletonSetVideoName(capGroup[0]);
							format = capGroup[1];
						}
					}
					if (capGroup == null) {
						capGroup = stringParse.parse(nameLine, "(^.+)\\/([a-zA-Z0-9]*)()\\.(.+)"); // DTV_Live 05/playlist.m3u8
						if (capGroup != null) {
							singletonSetVideoName(capGroup[1]);
							format = capGroup[0];
						}
					}

					if (capGroup == null) {
						capGroup = stringParse.parse(nameLine, "[a-zA-Z]*(\\d+)\\/([a-zA-Z0-9]*)\\d\\_(\\d+)\\.(.+)"); // VOD HLS0/B001950818U0_0.m3u8
						if (capGroup != null) {
							singletonSetVideoName(capGroup[1]);
							format = capGroup[0];
						}
					}
					if (capGroup == null) {
						capGroup = stringParse.parse(nameLine, "([a-zA-Z0-9]*)\\_(\\d+)\\.(.+)"); // AmzHLS 2e08c253-9a90-4db2-9e67-8baa3a152a00_v4.m3u8
						if (capGroup != null) {
							singletonSetVideoName(capGroup[0]);
							format = capGroup[1];
						}
					}
					if (capGroup == null) {
						capGroup = stringParse.parse(nameLine, "([a-zA-Z0-9]*)\\.(.+)"); // DTVNOW 03.m3u8
						if (capGroup != null) {
							singletonSetVideoName(capGroup[0]);
							format = capGroup[0];
						}
					}

					bitrateMap.put(format.toUpperCase(), bandwidth);
					if (childManifest != null) {
						childMap.put(format.toUpperCase(), childManifest);
					}
				}
			}
		} else {
			String val = StringParse.findLabeledDataFromString("_", "\\.", sData[sData.length - 2]);
			int pos = val.indexOf('_');
			if (pos > -1) {
				try {
					double dVal = Double.valueOf(val.substring(pos + 1));
					setSegmentCount(dVal);
				} catch (NumberFormatException e) {
					setSegmentCount(0);
				}
			}
		}
		
		loadSegments(sData);
	}
	
	/**
	 * Returns screen height based on quality code
	 * 
	 * @param quality
	 * @return
	 */
	public int getHeight(String quality) {
		if (quality != null) {
			ChildManifest temp = childMap.get(quality);
			if (temp == null) {
				return 0;
			}
			return temp.getPixelHeight();
		}
		return 0;
	}

	/**
	 * Returns screen width based on quality code
	 * 
	 * @param quality
	 * @return
	 */
	public int getWidth(String quality) {
		ChildManifest temp = childMap.get(quality);
		if (temp == null) {
			return 0;
		}
		return temp.getPixelWidth();
	}

	public SortedMap<String, ChildManifest> getChildMap() {
		return childMap;
	}
	
	public ChildManifest getChildMap(String key) {
		return childMap.get(key);
	}
	
	/**
	 * Load transportStreams(.ts) into segment list
	 * from HLS child manifests
	 * 
	 * @param sData
	 */
	private void loadSegments(String[] sData) {
		String[] parsData;
		Integer lineSeg = 0;
		String segDuration = null;
		
		for (String line : sData) {
			if (line.startsWith("#EXTINF:")) {
				parsData = stringParse.parse(line, "#EXTINF:(.+),");
				if (parsData != null) {
					segDuration = parsData[0];
					continue;
				}
			}
			if (line.contains(EXTEN_TS) && !segmentList.containsKey(line)) {
				parsData = stringParse.parse(line, "([a-zA-Z0-9-]*)-(\\d+)-([a-zA-Z0-9]*\\..+)");
				int part = 1;
				if (parsData == null) {
					parsData = stringParse.parse(line, "([a-zA-Z0-9]*\\..+)");
					part = 0;
				}
				if (parsData != null && !segmentList.containsKey(parsData[part])) {
					lineSeg = getSegIncremental();
					segmentList.put(parsData[part], lineSeg);
					durationList.put(lineSeg, segDuration);
					if (getDuration() == 0) {
						try {
							setDuration(Double.parseDouble(segDuration));
						} catch (NumberFormatException e) {
							// Ignore error
						}
					}
				} else if (!segmentList.containsKey(line)) {
					segmentList.put(line, lineSeg);
					durationList.put(lineSeg, segDuration);
				}
			}
		}
	}

	/**<pre>
	 * Contains:
	 * 	 - width and height in pixels
	 *   - bandwidth
	 */
	@Data
	@AllArgsConstructor
	public class ChildManifest {
		double bandwidth;
		int pixelWidth;
		int pixelHeight;

		public ChildManifest(double bandwidth, String pixelWidth, String pixelHeight) {
			setBandwidth(bandwidth);
			try {
				setPixelWidth(Integer.valueOf(pixelWidth));
				setPixelHeight(Integer.valueOf(pixelHeight));
			} catch (NumberFormatException e) {
				LOG.error(String.format("failed to parse \"%s\" or \"%s\"", pixelWidth, pixelHeight));
			}
		}
	}
	
	@Override
	public Integer getSegment(String segName) {
		if (!segmentList.isEmpty()) {
			String key = segName.endsWith(EXTEN_TS) ? segName : segName + EXTEN_TS;
			Integer val = segmentList.get(key);
			if (val != null) {
				return val;
			}
		}
		return -1;
	}
	
	@Override
	public String toString() {
		StringBuilder strblr = new StringBuilder("ManifestHLS:");
		strblr.append(super.toString());
		if (MapUtils.isNotEmpty(childMap)) {
			strblr.append("\n\t, Quality:");
			for (String key : childMap.keySet()) {
				strblr.append("\n\t\t").append(key).append(':').append(childMap.get(key));
			}
		}
		return strblr.toString();
	}

	@Override
	public int parseSegment(String fullName, VideoEventData ved) {
		if (ved.getSegment() != null && ved.getSegment() >= 0) {
			return ved.getSegment();
		} else if (ved.getDateTime() != null) {
			Integer segment = getSegment(ved.getDateTime());
			ved.setSegment(segment);
			ved.setDuration(getDuration(segment));
			return ved.getSegment();
		}
		int segment = -1;
		int pos = fullName.lastIndexOf('_');
		int dot = fullName.lastIndexOf('.');
		if (pos > -1) {
			try {
				segment = Integer.valueOf(fullName.substring(pos + 1, dot));
			} catch (NumberFormatException e) {
				// don't care about error, report segment as -1
				segment = -1;
			}
		}
		ved.setSegment(segment);
		return segment;
	}
	
}