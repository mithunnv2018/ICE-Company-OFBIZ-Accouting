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
package org.ofbiz.webapp.taglib;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

public class IterateNextTEI extends TagExtraInfo {

    public IterateNextTEI() {
        super();
    }

    public VariableInfo[] getVariableInfo(TagData data) {
        String name = null;
        String className = null;

        name = data.getAttributeString("name");
        if (name == null)
            name = "next";

        className = data.getAttributeString("type");
        if (className == null)
            className = "org.ofbiz.entity.GenericValue";

        VariableInfo info =
            new VariableInfo(name, className, true, VariableInfo.NESTED);
        VariableInfo[] result = {info};

        return result;
    }

    public boolean isValid(TagData data) {
        return true;
    }
}

