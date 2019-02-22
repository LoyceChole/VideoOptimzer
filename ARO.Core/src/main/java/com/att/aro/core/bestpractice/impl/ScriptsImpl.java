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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.ApplicationConfig;
import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.ScriptsResult;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;

public class ScriptsImpl implements IBestPractice {
	private static final int MIN_NUM_OF_SCRIPTS_IN_HTML_DOC = 2;
	@Value("${3rd.party.scripts.title}")
	private String overviewTitle;

	@Value("${3rd.party.scripts.detailedTitle}")
	private String detailTitle;

	@Value("${3rd.party.scripts.desc}")
	private String aboutText;

	@Value("${3rd.party.scripts.url}")
	private String learnMoreUrl;

	@Value("${3rd.party.scripts.pass}")
	private String textResultPass;

	@Value("${3rd.party.scripts.results}")
	private String textResults;

	@Value("${exportall.csvNumberOfScriptFiles}")
	private String exportAllNumberOfScriptsFiles;

	private IHttpRequestResponseHelper reqhelper;

	@Autowired
	public void setHttpRequestResponseHelper(IHttpRequestResponseHelper reqhelper) {
		this.reqhelper = reqhelper;
	}

	private static final Logger LOG = LogManager.getLogger(ScriptsImpl.class.getName());

	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		ScriptsResult result = new ScriptsResult();

		for (Session session : tracedata.getSessionlist()) {
			for (HttpRequestResponseInfo req : session.getRequestResponseInfo()) {
				if (req.getDirection() == HttpDirection.RESPONSE && req.getContentLength() > 0 && req.getContentType() != null && reqhelper.isHtml(req.getContentType())) {
					result = analyzeHtml(req, session, result);
				}
			}
		}
		String text = "";
		if (result.getFirstFailedHtml() == null) {
			result.setResultType(BPResultType.PASS);
			text = MessageFormat.format(textResultPass, result.getNumberOfFailedFiles());
			result.setResultText(text);
		} else {
			result.setResultType(BPResultType.FAIL);
			text = MessageFormat.format(textResults, 
										ApplicationConfig.getInstance().getAppShortName(), 
										result.getNumberOfFailedFiles());
			result.setResultText(text);
		}
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAllNumberOfScriptsFiles(exportAllNumberOfScriptsFiles);
		return result;
	}

	private ScriptsResult analyzeHtml(HttpRequestResponseInfo hrri, Session session, ScriptsResult resdata) {
		ScriptsResult result = resdata;
		Document htmlDoc = null;
		String type = null;
		try {
			type = reqhelper.getContentString(hrri, session);
			if (type != null) {
				htmlDoc = Jsoup.parse(type);
				Elements allSrcElements = new Elements(htmlDoc.select("script"));
				if (allSrcElements.size() >= MIN_NUM_OF_SCRIPTS_IN_HTML_DOC) {
					result = is3rdPartyScript(hrri, allSrcElements, result);
				}
			}
		} catch (Exception e) {
			LOG.error("Failed to get content from HttpRequestResponseInfo", e);
			if (hrri != null && session != null) {
				LOG.error("jsoup error: contenttype: " + hrri.getContentType());
				LOG.error("jsoup error:  start time: " + session.getSessionStartTime());
			}
		}
		return result;
	}

	private ScriptsResult is3rdPartyScript(HttpRequestResponseInfo hrri, Elements elements, ScriptsResult resdata) {
		ScriptsResult result = resdata;
		String originDomain = getOriginDomain(hrri);

		List<String> domains = getScriptDomains(elements);

		if (isMultiple3rdPartyDomains(originDomain, domains)) {
			// store the 1st occurrence
			if (result.getFirstFailedHtml() == null) {
				result.setFirstFailedHtml(hrri);
			}
			result.incrementNumberOfFailedFiles();
		}

		return result;

	}

	private String getOriginDomain(HttpRequestResponseInfo hrri) {

		HttpRequestResponseInfo rsp = hrri.getAssocReqResp();
		return (rsp != null) ? rsp.getHostName() : "";
	}

	private List<String> getScriptDomains(Elements elements) {
		List<String> domains = new ArrayList<String>();
		String scriptDomain;
		for (Element element : elements) {
			if (element.hasAttr("src") && !element.attr("src").isEmpty()) {
				scriptDomain = getDomain(element);
				if (!scriptDomain.isEmpty()) {
					domains.add(scriptDomain);
				}
			}
		}
		return domains;
	}

	private String getDomain(Element element) {

		String url = element.attr("abs:src");
		if (!url.isEmpty()) {
			try {
				url = new URL(url).getHost();
			} catch (MalformedURLException e) {
				LOG.warn("getDomain() got malformedURLException: " + e.getMessage());
			}
		}
		return url;
	}

	private boolean isMultiple3rdPartyDomains(String originDomain, List<String> domains) {

		int counter = 0;
		for (String domain : domains) {
			if (!domain.equals(originDomain) && (++counter >= MIN_NUM_OF_SCRIPTS_IN_HTML_DOC)) {
				return true;
			}
		}

		return false;
	}
}// end class
