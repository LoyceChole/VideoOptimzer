/*
 *  Copyright 2018 AT&T
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
package com.att.aro.datacollector.rootedandroidcollector.pojo;

import com.att.aro.core.pojo.ErrorCode;
import com.att.aro.core.util.GoogleAnalyticsUtil;
import com.att.aro.core.ApplicationConfig;

/**
 * error code for rooted Android Collector start from 200
 * 
 * Date: Feb 24, 2015
 */
public final class ErrorCodeRegistry {
	private ErrorCodeRegistry() {
	}

	public static ErrorCode getAndroidBridgeFailedToStart() {
		ErrorCode error = new ErrorCode();
		error.setCode(200);
		error.setName("Android Debug Bridge failed to start");
		error.setDescription(ApplicationConfig.getInstance().getAppName() + " Collector tried to start Android Debug Bridge service. The service was not started successfully.");
		sendGAErrorCode(error);
		return error;
	}

	public static ErrorCode getFailToInstallAPK() {
		ErrorCode err = new ErrorCode();
		err.setCode(201);
		err.setName("Failed to install Android App on device");
		err.setDescription(ApplicationConfig.getInstance().getAppName() + " tried to install Collector on device and failed. Try to manually install it, then try again.");
		sendGAErrorCode(err);
		return err;
	}

	public static ErrorCode getTraceDirExist() {
		ErrorCode err = new ErrorCode();
		err.setCode(202);
		err.setName("Found existing trace directory that is not empty");
		err.setDescription(ApplicationConfig.getInstance().getAppName() + " found an existing directory that contains files and did not want to override it. Some files may be hidden.");
		sendGAErrorCode(err);
		return err;
	}

	public static ErrorCode getNoDeviceConnected() {
		ErrorCode err = new ErrorCode();
		err.setCode(203);
		err.setName("No Android device found.");
		err.setDescription(ApplicationConfig.getInstance().getAppName() + " cannot find any Android deviced plugged into the machine.");
		sendGAErrorCode(err);
		return err;
	}

	/**
	 * no device id matched the device list
	 * 
	 * @return
	 */
	public static ErrorCode getDeviceIdNotFound() {
		ErrorCode err = new ErrorCode();
		err.setCode(204);
		err.setName("Android device Id or serial number not found.");
		err.setDescription(ApplicationConfig.getInstance().getAppName() + " cannot find any Android deviced plugged into the machine that matched the device Id or serial number you specified.");
		sendGAErrorCode(err);
		return err;
	}

	/**
	 * device does not have any space in sdcard to collect trace
	 * 
	 * @return
	 */
	public static ErrorCode getDeviceHasNoSpace() {
		ErrorCode err = new ErrorCode();
		err.setCode(205);
		err.setName("Device has no space");
		err.setDescription("Device does not have any space for saving trace");
		sendGAErrorCode(err);
		return err;
	}

	public static ErrorCode getCollectorAlreadyRunning() {
		ErrorCode err = new ErrorCode();
		err.setCode(206);
		err.setName(ApplicationConfig.getInstance().getAppName() + " Rooted Android collector already running");
		err.setDescription("There is already an " + ApplicationConfig.getInstance().getAppName() +" collector running on this device. Stop it first before running another one.");
		sendGAErrorCode(err);
		return err;
	}

	/**
	 * failed to create local directory in user's machine to save trace data to.
	 * 
	 * @return
	 */
	public static ErrorCode getFailedToCreateLocalTraceDirectory() {
		ErrorCode err = new ErrorCode();
		err.setCode(207);
		err.setName("Failed to create local trace directory");
		err.setDescription(ApplicationConfig.getInstance().getAppName() + " tried to create local directory for saving trace data, but failed.");
		sendGAErrorCode(err);
		return err;
	}

	public static ErrorCode getFailToExtractTcpdump() {
		ErrorCode err = new ErrorCode();
		err.setCode(208);
		err.setName("Failed to extract tcpdump");
		err.setDescription(ApplicationConfig.getInstance().getAppName() + " failed to extract tcpdump from resource bundle and save it to local machine.");
		sendGAErrorCode(err);
		return err;
	}

	public static ErrorCode getFailToInstallTcpdump() {
		ErrorCode err = new ErrorCode();
		err.setCode(209);
		err.setName("Failed to install tcpdump");
		err.setDescription(ApplicationConfig.getInstance().getAppName() + " failed to install tcpdump on Emulator. Tcpdump is required to capture packet");
		sendGAErrorCode(err);
		return err;
	}

	public static ErrorCode getFailToRunApk() {
		ErrorCode err = new ErrorCode();
		err.setCode(210);
		err.setName("Failed to run " + ApplicationConfig.getInstance().getAppName() + " Data Collector");
		err.setDescription(ApplicationConfig.getInstance().getAppName() + " Analyzer tried to run Data Collector on device and received error from device.");
		sendGAErrorCode(err);
		return err;
	}

	public static ErrorCode getFailSyncService() {
		ErrorCode err = new ErrorCode();
		err.setCode(211);
		err.setName("Failed to connect to device SyncService");
		err.setDescription(ApplicationConfig.getInstance().getAppName() + " failed to get SyncService() from IDevice which is used for data transfer");
		sendGAErrorCode(err);
		return err;
	}

	public static ErrorCode getTcpdumpPermissionIssue() {
		ErrorCode err = new ErrorCode();
		err.setCode(212);
		err.setName("Failed to set execute permission on Tcpdump on device");
		err.setDescription("Error occured while trying to set permission on tcpdump file on device. Execute permission is required to run it.");
		sendGAErrorCode(err);
		return err;
	}

	public static ErrorCode getRootedStatus() {
		ErrorCode err = new ErrorCode();
		err.setCode(213);
		err.setName("Device not rooted");
		err.setDescription(ApplicationConfig.getInstance().getAppName() + " detected that device is not rooted. A rooted device is required to run this collector");
		sendGAErrorCode(err);
		return err;
	}

	public static ErrorCode getCollectorTimeout() {
		ErrorCode err = new ErrorCode();
		err.setCode(214);
		err.setName("Collector Failed to start traffic capture");
		err.setDescription(ApplicationConfig.getInstance().getAppName() + " detected that the collector failed to start capture in time");
		sendGAErrorCode(err);
		return err;
	}

	public static ErrorCode getProblemAccessingDevice(String message) {
		ErrorCode err = new ErrorCode();
		err.setCode(215);
		err.setName("Problem accessing device");
		err.setDescription(ApplicationConfig.getInstance().getAppName() + " failed to access device :"+message);
		sendGAErrorCode(err);
		return err;

	}

	public static ErrorCode getDeviceNotOnline() {
		ErrorCode err = new ErrorCode();
		err.setCode(216);
		err.setName("Android device not online.");
		err.setDescription(ApplicationConfig.getInstance().getAppName() + " cannot command an Android device that is not online.");
		sendGAErrorCode(err);
		return err;
	}

	public static ErrorCode getNotSupported(String message) {
		ErrorCode err = new ErrorCode();
		err.setCode(415);
		err.setName("ABI X86 Not Supported");
		err.setDescription(message);
		sendGAErrorCode(err);
		return err;
	}

	private static void sendGAErrorCode(ErrorCode err){
		GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendErrorEvents(err.getName(),err.getDescription(), false);
	}

}
