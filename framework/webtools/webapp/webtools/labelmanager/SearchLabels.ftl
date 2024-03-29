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
<div class="screenlet-body">
  <form action="<@ofbizUrl>SearchLabels</@ofbizUrl>" method="post">
    <table class="basic-table">
      <tr>
        <td align="right" class="label">
          ${uiLabelMap.WebtoolsLabelManagerKey}
        </td>
        <td align="left">
          <input type="text" name="labelKey" size="30" maxlength="70" value="${parameters.labelKey?if_exists}">
        </td>
        <td align="right" class="label">
          ${uiLabelMap.WebtoolsLabelManagerComponentName}
        </td>
        <td align="left">
          <select name="labelComponentName">
            <option value="">${uiLabelMap.WebtoolsLabelManagerAllComponents}</option>
            <#list componentNamesFound as componentNameFound>
              <option <#if parameters.labelComponentName?exists && parameters.labelComponentName == componentNameFound>selected="selected"</#if> value="${componentNameFound}">${componentNameFound}</option>
            </#list>
          </select>
        </td>
      </tr>
      <tr>
        <td align="right" class="label">
          ${uiLabelMap.WebtoolsLabelManagerFileName}
        </td>
        <td align="left">
          <select name="labelFileName">
            <option value="">${uiLabelMap.WebtoolsLabelManagerAllFiles}</option>
            <#assign fileNames = fileNamesFound.keySet()>
            <#list fileNames as fileName>
              <option <#if parameters.labelFileName?exists && parameters.labelFileName == fileName>selected="selected"</#if> value="${fileName}">${fileName}</option>
            </#list>
          </select>
        </td>
        <td align="right" class="label">
          ${uiLabelMap.WebtoolsLabelManagerLocale}
        </td>
        <td align="left">
          <select name="labelLocaleName">
            <option value="">${uiLabelMap.WebtoolsLabelManagerAllLocales}</option>
            <#list localesFound as localeFound>
              <#assign locale = Static["org.ofbiz.base.util.UtilMisc"].parseLocale(localeFound)?if_exists/>
              <#assign langAttr = localeFound.toString()?replace("_", "-")>
              <#assign langDir = "ltr">
              <#if "ar.iw"?contains(langAttr?substring(0, 2))>
                <#assign langDir = "rtl">
              </#if>
              <option <#if parameters.labelLocaleName?exists && parameters.labelLocaleName == localeFound>selected="selected"</#if> value="${localeFound}" lang="${langAttr}" dir="${langDir}"><#if locale?exists && locale?has_content>${locale.getDisplayName(locale)}<#else>${localeFound}</#if></option>
            </#list>
          </select>
        </td>
      </tr>
      <tr>
        <td align="right" class="label">
          ${uiLabelMap.WebtoolsLabelManagerOnlyNotUsedLabels}
        </td>
        <td align="left">
          <input type="checkbox" name="onlyNotUsedLabels" value="Y" <#if parameters.onlyNotUsedLabels?exists && parameters.onlyNotUsedLabels == "Y">checked</#if>>
        </td>
        <td align="right" class="label">
          ${uiLabelMap.WebtoolsLabelManagerOnlyMissingTranslations}
        </td>
        <td align="left">
          <input type="checkbox" name="onlyMissingTranslations" value="Y" <#if parameters.onlyMissingTranslations?exists && parameters.onlyMissingTranslations == "Y">checked</#if>>
        </td>
      </tr>
      <tr>
        <td colspan="4" align="center">
          <#if (duplicatedLocalesLabels > 0)>
            <br/>
            <b>${uiLabelMap.WebtoolsLabelManagerWarningMessage} (${duplicatedLocalesLabels})</b>
            <br/>
            <#list duplicatedLocalesLabelsList as duplicatedLocalesLabel>
                <br>${duplicatedLocalesLabel.labelKey}
            </#list>
            <br/><br/>${uiLabelMap.WebtoolsLabelManagerClearCacheAfterFixingDuplicateLabels}
          <#else>
            <input type="submit" name="searchLabels" value="${uiLabelMap.CommonFind}"/>
          </#if>
        </td>
      </tr>
    </table>
  </form>
</div>
