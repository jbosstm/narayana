/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.stomp;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents all the data in a STOMP frame.
  *
 * @author <a href="http://hiramchirino.com">chirino</a>
 */
public class StompFrame {
    private static final byte[] NO_DATA = new byte[] {};
    private String action;
    private Map<String, Object> headers;
    private byte[] content = StompFrame.NO_DATA;

    public StompFrame() {
        this.headers = new HashMap<String, Object>();
    }

    public StompFrame(String command, Map<String, Object> headers, byte[] data) {
        this.action = command;
        this.headers = headers;
        this.content = data;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String command) {
        this.action = command;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] data) {
        this.content = data;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }
}
