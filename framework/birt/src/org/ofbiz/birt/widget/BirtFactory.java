/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.widget.birt;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.ofbiz.base.location.FlexibleLocation;
import org.ofbiz.base.util.Debug;
import org.ofbiz.widget.screen.ScreenFactory;
import org.xml.sax.SAXException;

public class BirtFactory {
    
    public static final String module = BirtFactory.class.getName();
    
    /**
     * get report inport stream from location
     * @param resourceName
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static InputStream getReportInputStreamFromLocation(String resourceName)
        throws IOException, SAXException, ParserConfigurationException{
        
        InputStream reportInputStream = null;
        synchronized (BirtFactory.class) {
            long startTime = System.currentTimeMillis();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null) {
                loader = ScreenFactory.class.getClassLoader();
            }
            URL reportFileUrl = null;
            reportFileUrl = FlexibleLocation.resolveLocation(resourceName, loader);
            if (reportFileUrl == null) {
                throw new IllegalArgumentException("Could not resolve location to URL: " + resourceName);
            }
            reportInputStream = reportFileUrl.openStream();
            double totalSeconds = (System.currentTimeMillis() - startTime)/1000.0;
            Debug.logInfo("Got report in " + totalSeconds + "s from: " + reportFileUrl.toExternalForm(), module);
        }
        
        if (reportInputStream == null) {
            throw new IllegalArgumentException("Could not find report file with location [" + resourceName + "]");
        }
        return reportInputStream;
    }
}
