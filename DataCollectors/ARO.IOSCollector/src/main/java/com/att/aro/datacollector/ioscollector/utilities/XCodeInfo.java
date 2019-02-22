/*
 *  Copyright 2012 AT&T
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
package com.att.aro.datacollector.ioscollector.utilities;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import com.att.aro.datacollector.ioscollector.reader.ExternalProcessRunner;

public class XCodeInfo {

	ExternalProcessRunner runner = null;
    private boolean xcodeCLTError = false;
	private static final Logger LOG = LogManager.getLogger(XCodeInfo.class);

	public XCodeInfo() {
		runner = new ExternalProcessRunner();
	}

	public XCodeInfo(ExternalProcessRunner runner) {
		this.runner = runner;
	}

	/**
	 * Find out if component rvictl is available. This component come with XCode
	 * 4.2 and above.
	 * 
	 * @return true or false
	 */
	public boolean isRVIAvailable() {
		boolean yes = false;
		String[] cmd = new String[] { "bash", "-c", "which rvictl" };
		String result = "";
		try {
			result = runner.runCmd(cmd);

		} catch (IOException e) {
			LOG.debug("IOException:", e);
		}
		if (result.length() > 1) {
			yes = true;
		}
		return yes;
	}

	/**
	 * Find whether xcode is installed or not.
	 * 
	 * @return
	 */
	public boolean isXcodeAvailable() {
		boolean flag = false;
		String[] cmd = new String[] { "bash", "-c", "which xcodebuild" };
		String xCode = "";
		try {
			xCode = runner.runCmd(cmd);
			LOG.info("xCode Installation Dir : " + xCode);
		} catch (IOException ioE) {
			LOG.debug("IOException:", ioE);
		}
		if (xCode.length() > 1) {
			flag = true;
		}
		return flag;
	}

	/**
	 * ARO is supporting version 5 and above. This is method will check for
	 * supported version.
	 * 
	 * @return
	 */
	public boolean isXcodeSupportedVersionInstalled() {
		boolean supportedVersionFlag = false;
		String[] cmd = new String[] { "bash", "-c", "xcodebuild -version" };
		String xCodeVersion = "";
		try {
			xCodeVersion = runner.runCmd(cmd);
			LOG.info("xCode Version : " + xCodeVersion);
		} catch (IOException ex) {
			LOG.debug("IOException:", ex);
		}
		if (!xCodeVersion.isEmpty()) {
			String[] version = xCodeVersion.split("\\r?\\n");
			String xCode = version[0];
			String versionOfxCode = xCode.substring(xCode.indexOf(" "));
			LOG.info(" Version Code : " + versionOfxCode);
			int versionNumber = 0;
			try {
				versionNumber = Integer.parseInt(versionOfxCode.substring(0, versionOfxCode.indexOf(".")).trim());
				LOG.info(" Version Number : " + versionNumber);
			} catch (NumberFormatException e) {
				LOG.debug("NumberFormatException:", e);
			}
			if (versionNumber >= 7) { //only 8% of iphone users has xcode version 6 and below --- November 2016 statistics
				supportedVersionFlag = true;
			}

		}else{
			xcodeCLTError = true;
		}
		return supportedVersionFlag;
	}

	public boolean isXcodeCLTError() {
		return xcodeCLTError;
	}
	
	
}//end class

