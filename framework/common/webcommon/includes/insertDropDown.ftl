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

<form method="post"  action="<@ofbizUrl>${topLine.action}</@ofbizUrl>"  onSubmit="javascript:submitFormDisableSubmits(this)" name="${topLine.action}" id="${topLine.action}">
${StringUtil.wrapString(topLine.textBegin?if_exists)}
<#assign listSize = topLine.dropDownList.size()>
  <#if topLine.dropDownList.size() gt 1>
  <#if topLine.hiddenFieldList?exists>
    <#list topLine.hiddenFieldList as field>
	  <input type="hidden" name="${field.name}" value="${field.value}"/>
    </#list>
  </#if>
  <select name="${topLine.selectionName?if_exists}" onChange="javascript:document.${topLine.action}.submit();">
    <#list topLine.dropDownList as option>
	  <option <#if option.key == topLine.selectedKey >selected="selected"</#if> value="${option.key?if_exists}">${option.value?if_exists}</option>
    </#list>
  </select>
<#else>
  ${topLine.dropDownList[0].value?if_exists}
</#if>
${StringUtil.wrapString(topLine.textEnd?if_exists)}
</form>
