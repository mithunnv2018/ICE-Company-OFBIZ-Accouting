<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<h1>${uiLabelMap.WebtoolsImportToDataSource}</h1>
<br />
<p>${uiLabelMap.WebtoolsXMLImportInfo}</p>
<hr/>
  <h2>${uiLabelMap.WebtoolsImport}:</h2>

  <form method="post" action="<@ofbizUrl>entityImportReaders</@ofbizUrl>">
    Enter Readers (comma separated, no spaces; from entityengine.xml and ofbiz-component.xml files; common ones include seed,ext,demo):</div>
    <input type="text" size="60" name="readers" value="${readers?default("seed")}"/><br />
    <input type="checkbox" name="mostlyInserts" <#if mostlyInserts?exists>"checked"</#if> value="true"/>${uiLabelMap.WebtoolsMostlyInserts}<br />
    <input type="checkbox" name="maintainTimeStamps" <#if keepStamps?exists>"checked"</#if> value="true"/>${uiLabelMap.WebtoolsMaintainTimestamps}<br />
    <input type="checkbox" name="createDummyFks" <#if createDummyFks?exists>"checked"</#if> value="true"/>${uiLabelMap.WebtoolsCreateDummyFks}<br />
    <input type="checkbox" name="checkDataOnly" <#if checkDataOnly?exists>"checked"</#if> value="true"/>${uiLabelMap.WebtoolsCheckDataOnly}<br />
    ${uiLabelMap.WebtoolsTimeoutSeconds}:<input type="text" size="6" value="${txTimeoutStr?default("7200")}" name="txTimeout"/><br />
    <div class="button-bar"><input type="submit" value="${uiLabelMap.WebtoolsImport}"/></div>
  </form>
  <#if messages?exists>
      <hr/>
      <h3>${uiLabelMap.WebtoolsResults}:</h3>
      <#list messages as message>
          <p>${message}</p>
      </#list>
  </#if>
