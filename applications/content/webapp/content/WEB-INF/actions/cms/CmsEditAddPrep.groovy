/*
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
 */

import org.ofbiz.base.util.*
import org.ofbiz.minilang.SimpleMapProcessor
import org.ofbiz.content.ContentManagementWorker
import org.ofbiz.content.content.ContentWorker
import org.ofbiz.content.data.DataResourceWorker

userLogin = session.getAttribute("userLogin");
contentAssocDataResourceViewFrom = delegator.makeValue("ContentAssocDataResourceViewFrom");

contentId = context.contentId;

contentAssocPK = delegator.makeValue("ContentAssoc");
contentAssocPK.setAllFields(context, false, "ca", new Boolean(true));
Debug.logInfo("in cmseditaddprep, contentAssocPK:" + contentAssocPK,"");

contentAssoc = null;
if (contentAssocPK.isPrimaryKey()) {
    contentAssoc = delegator.findOne("ContentAssoc", contentAssocPK, false);
}

if (contentAssoc) {
    SimpleMapProcessor.runSimpleMapProcessor("org/ofbiz/content/ContentManagementMapProcessors.xml", "contentAssocOut", contentAssoc, contentAssocDataResourceViewFrom, new ArrayList(), Locale.getDefault());
} else {
    contentAssocPK.setAllFields(context, false, "ca", null); //set all field, pk and non
    SimpleMapProcessor.runSimpleMapProcessor("org/ofbiz/content/ContentManagementMapProcessors.xml", "contentAssocOut", contentAssocPK, contentAssocDataResourceViewFrom, new ArrayList(), Locale.getDefault());
}
Debug.logInfo("in cmseditaddprep, contentAssocDataResourceViewFrom:" + contentAssocDataResourceViewFrom,"");

dataResourceId = "";
textData = "";
content = null;
if (contentId) {
    content = delegator.findOne("Content", [contentId : contentId], true);
    if (content) {
        contentAssocDataResourceViewFrom.setAllFields(content, false, null, null);
    }
} else {
    contentAssocDataResourceViewFrom.set("contentTypeId", "DOCUMENT");
}

if (content) {
    dataResourceId = content.dataResourceId;
}
if (!dataResourceId) {
    dataResourceId = context.drDataResourceId;
    if (!dataResourceId) {
        dataResourceId = context.dataResourceId;
    }
}
if (dataResourceId) {
    dataResource = delegator.findOne("DataResource", [dataResourceId : dataResourceId], true);
    SimpleMapProcessor.runSimpleMapProcessor("org/ofbiz/content/ContentManagementMapProcessors.xml", "dataResourceOut", dataResource, contentAssocDataResourceViewFrom, new ArrayList(), Locale.getDefault());
    templateRoot = [:];
    FreeMarkerViewHandler.prepOfbizRoot(templateRoot, request, response);
    txt = DataResourceWorker.getDataResourceTextCache(dataResource, "text/html", Locale.getDefault(), templateRoot, delegator);

    if (txt) {
        textData = UtilFormatOut.encodeXmlValue(txt);
    }
}
Debug.logInfo("in cmseditaddprep, textData:" + textData,"");

currentValue = new HashMap(contentAssocDataResourceViewFrom);
currentValue.textData = textData;
currentValue.nowTimestamp = UtilDateTime.nowTimestamp();
context.currentValue = currentValue;

request.setAttribute("previousParams", currentValue);

persistAction = context.persistAction;
if (!persistAction) {
    persistAction = "persistContent";
}
context.persistAction = persistAction;