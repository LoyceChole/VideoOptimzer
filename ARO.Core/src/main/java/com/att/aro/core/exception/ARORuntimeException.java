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
package com.att.aro.core.exception;


public class ARORuntimeException extends RuntimeException {
	private static final long serialVersionUID = -8786852894857814735L;

	protected final ExceptionType exceptionType;

	public enum ExceptionType {
		unknown,
		invalidAttribute
	}

	protected ARORuntimeException(ExceptionType exceptionType) {
		super();
		this.exceptionType = exceptionType;
	}
	protected ARORuntimeException(ExceptionType exceptionType, String message) {
		super(message);
		this.exceptionType = exceptionType;
	}
	protected ARORuntimeException(ExceptionType exceptionType, Throwable cause) {
		super(cause);
		this.exceptionType = exceptionType;
	}
	protected ARORuntimeException(ExceptionType exceptionType, String message, Throwable cause) {
		super(message, cause);
		this.exceptionType = exceptionType;
	}

	public ARORuntimeException() {
		this(ExceptionType.unknown);
	}
	public ARORuntimeException(String message) {
		this(ExceptionType.unknown, message);
	}
	public ARORuntimeException(Throwable cause) {
		this(ExceptionType.unknown, cause);
	}
	public ARORuntimeException(String message, Throwable cause) {
		this(ExceptionType.unknown, message, cause);
	}

	public ExceptionType getExceptionType() {
		return exceptionType;
	}
}
