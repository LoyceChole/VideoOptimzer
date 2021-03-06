package com.att.aro.core.videoanalysis.parsers;

import java.io.ByteArrayInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.att.aro.core.videoanalysis.parsers.encodedsegment.MPDEncodedSegment;
import com.att.aro.core.videoanalysis.parsers.segmenttimeline.MPDSegmentTimeline;
import com.att.aro.core.videoanalysis.parsers.smoothstreaming.SSM;

import lombok.Data;

@Data
public class XmlManifestHelper {

	private static final Logger LOG = LogManager.getLogger(XmlManifestHelper.class.getName());

	public enum ManifestFormat{
		SmoothStreamingMedia
		, MPD_EncodedSegmentList
		, MPD_SegmentTimeline
		, MPD
	}

	private ManifestFormat manifestType;
	private MpdBase manifest;

	@Override
	public String toString() {
		StringBuilder strblr = new StringBuilder(83);
		strblr.append("XmlManifestHelper:"); 
		strblr.append(" manifestType :"); strblr.append( manifestType);
		strblr.append("\n :"); strblr.append( manifest);
		return strblr.toString();
	}

	public XmlManifestHelper(byte[] data) {
		if (data == null || data.length == 0) {
			return;
		}
		String sData = new String(data);
		if (sData.contains("MPD") && sData.contains("EncodedSegmentList")) {
			manifestType = ManifestFormat.MPD_EncodedSegmentList;
			manifest = xml2EncodedSegmentList(new ByteArrayInputStream(data));

		} else if (sData.contains("MPD") && sData.contains("<SegmentTimeline>")) {
			manifestType = ManifestFormat.MPD_SegmentTimeline;
			manifest = xml2SegmentTimeline(new MPDSegmentTimeline(), new ByteArrayInputStream(data));

		} else if (sData.contains("SmoothStreamingMedia")) {
			manifestType = ManifestFormat.SmoothStreamingMedia;
			manifest = xml2SSM(new ByteArrayInputStream(data));

		} else if (sData.contains("MPD")) {
			manifestType = ManifestFormat.MPD;
			manifest = xml2EncodedSegmentList(new ByteArrayInputStream(data));
		}
	}

	String getMajorVersion(){
		return manifest.getMajorVersion();
	}

	/**
	 * Load Amazon manifest into an MPDAmz object
	 * 
	 * @param xmlByte
	 * @return an MPDAmz object
	 */
	private MPDEncodedSegment xml2EncodedSegmentList(ByteArrayInputStream xmlByte) {
		JAXBContext context;
		Unmarshaller unMarshaller;
		MPDEncodedSegment mpdOutput = new MPDEncodedSegment();

		try {
			context = JAXBContext.newInstance(MPDEncodedSegment.class);
			
			unMarshaller = context.createUnmarshaller();
			mpdOutput = (MPDEncodedSegment) unMarshaller.unmarshal(xmlByte);
			if (mpdOutput.getPeriod().isEmpty()) {
				LOG.error("MPD NULL");
			}
		} catch (JAXBException e) {
			LOG.error("JAXBException",e);
		} catch (Exception ex) {
			LOG.error("JaxB parse Exception", ex);
		}
		return mpdOutput;
	}
	
	private MpdBase xml2SegmentTimeline(MpdBase mpdOutput, ByteArrayInputStream xmlByte) {
		Class<? extends MpdBase> outClass = mpdOutput.getClass();
		JAXBContext context;
		Unmarshaller unMarshaller;

		try {
			context = JAXBContext.newInstance(outClass);

			unMarshaller = context.createUnmarshaller();
			mpdOutput = (MpdBase) unMarshaller.unmarshal(xmlByte);
			if (mpdOutput.getSize() == 0) {
				LOG.error("MPD NULL");
			}
		} catch (JAXBException e) {
			e.printStackTrace();
			LOG.error("JAXBException",e);
		} catch (Exception ex) {
			LOG.error("JaxB parse Exception", ex);
		}
		return mpdOutput;
	}

	private SSM xml2SSM(ByteArrayInputStream xmlByte) {
		JAXBContext context;
		Unmarshaller unMarshaller;
		SSM manifest = new SSM();

		try {
			context = JAXBContext.newInstance(SSM.class);
			
			unMarshaller = context.createUnmarshaller();
			manifest = (SSM) unMarshaller.unmarshal(xmlByte);
			if (manifest.getStreamIndex().isEmpty()) {
				LOG.error("SSM NULL");
			}
		} catch (JAXBException e) {
			LOG.error("JAXBException" + e.getMessage());
		} catch (Exception ex) {
			LOG.error("JaxB parse Exception" + ex.getMessage());
		}
		return manifest;
	}

}
