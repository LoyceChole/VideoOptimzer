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
package com.att.aro.core.packetanalysis.pojo;

import javax.annotation.Nonnull;

import com.att.aro.core.videoanalysis.pojo.VideoEvent;

import lombok.Data;

@Data
public class VideoStall {
	private double duration;
	private double stallStartTimestamp;
	private double stallEndTimestamp;

	private VideoEvent videoEvent;
	@Nonnull
	private String stallState = "";

	@Override
	public String toString() {
		StringBuilder strblr = new StringBuilder(83);
		strblr.append("Segment:")
			.append(videoEvent != null ? videoEvent.getSegmentID() : "")
			.append(", stallState:").append(stallState)
			.append(", duration:").append(String.format("%.3f", duration))
			.append(", start:").append(String.format("%.3f", stallStartTimestamp))
			.append(", end :").append(String.format("%.3f :", stallEndTimestamp));
		return strblr.toString();
	}

	public VideoStall(double stallStartTimestamp) {
		setStallStartTimestamp(stallStartTimestamp);
	}

	public VideoStall(VideoEvent videoEvent) {
		setVideoEvent(videoEvent);
		setStallStartTimestamp(videoEvent.getPlayTime() - videoEvent.getStallTime());
		setStallEndTimestamp(videoEvent.getPlayTime());
		setDuration(stallEndTimestamp - stallStartTimestamp);
	}

	public void setStallEndTimestamp(double stallEndTimestamp) {
		this.stallEndTimestamp = stallEndTimestamp;
		setDuration(this.stallEndTimestamp - this.stallStartTimestamp);
	}

	public void setSegmentTryingToPlay(VideoEvent videoEvent) {
		setVideoEvent(videoEvent);
	}

	public VideoEvent getSegmentTryingToPlay() {
		return getVideoEvent();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		VideoStall other = (VideoStall) obj;
		if (Double.doubleToLongBits(other.getDuration()) != Double.doubleToLongBits(duration)) {
			return false;
		}
		if (Double.doubleToLongBits(other.getStallEndTimestamp()) != Double.doubleToLongBits(stallEndTimestamp)
				|| Double.doubleToLongBits(other.getStallEndTimestamp()) != Double.doubleToLongBits(stallStartTimestamp)) {
			return false;
		}
		if (!other.getStallState().equals(stallState)) {
			return false;
		}
		if (!other.getSegmentTryingToPlay().equals(videoEvent)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(duration);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(stallEndTimestamp);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(stallStartTimestamp);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + stallState.hashCode();
		result = prime * result + videoEvent.hashCode();
		return result;
	}
}
