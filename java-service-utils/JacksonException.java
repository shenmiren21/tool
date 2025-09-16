/*
 * Copyright 2022-2024 shenmiren21(2772734342@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.haedu.sso.authserver.utils;

public class JacksonException extends RuntimeException {
	
	private static final long serialVersionUID = -6243633891270839926L;

	public JacksonException() {
        super();
    }
    
    public JacksonException(String errMsg) {
        super(errMsg);
    }
    
    public JacksonException(Throwable throwable) {
        super(throwable);
    }
    
    public JacksonException(String errMsg, Throwable throwable) {
        super(errMsg, throwable);
    }
}