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
package com.att.aro.core.packetanalysis;

import java.util.List;

import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.packetanalysis.pojo.EnergyModel;
import com.att.aro.core.peripheral.pojo.BluetoothInfo;
import com.att.aro.core.peripheral.pojo.CameraInfo;
import com.att.aro.core.peripheral.pojo.GpsInfo;
import com.att.aro.core.peripheral.pojo.ScreenStateInfo;

/**
 * method to generate EnergyModel
 * Date: November 3, 2014
 */
public interface IEnergyModelFactory {
	EnergyModel create(Profile profile, double totalRrcEnergy, List<GpsInfo> gpsinfos, List<CameraInfo> camerainfos, 
			List<BluetoothInfo> bluetoothinfos, List<ScreenStateInfo> screenstateinfos);
}
