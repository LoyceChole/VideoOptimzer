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
package com.att.arocollector.privatedata;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

class MACAddressCollector extends AbstractDeviceDataCollector {

	private WifiManager wifiManager;
	private static final String TAG = MACAddressCollector.class.getSimpleName();
	
	MACAddressCollector(Context context, String dataFilePath) {
		
		super(dataFilePath);
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	
	@Override
	List<NameValuePair> getData() {
		
		String macAddress = wifiManager.getConnectionInfo().getMacAddress();
		Log.d(TAG, "MAC Address: " + macAddress);
		
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new NameValuePair(PrivateDataCollectionConst.MAC_ADDRESS, macAddress));
		return data;
	}
}
