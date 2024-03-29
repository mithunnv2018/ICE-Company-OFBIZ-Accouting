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

<div class="screenlet">
  <div class="screenlet-title-bar">
    <h3>${uiLabelMap.WorkEffortCalendarUpComingEventsView}</h3>
  </div>
  <#if days?has_content>
    <table class="basic-table hover-bar" cellspacing="0">
      <tr class="header-row">
        <td>${uiLabelMap.CommonStartDateTime}</td>
        <td>${uiLabelMap.CommonEndDateTime}</td>
        <td>${uiLabelMap.CommonType}</td>
        <td>${uiLabelMap.WorkEffortName}</td>
      </tr>
      <#list days as day>
        <#assign workEfforts = day.calendarEntries>
        <#if workEfforts?has_content>
          <tr class="header-row"><th colspan="4"><hr/></th></tr>
          <#assign alt_row = false>
          <#list workEfforts as calendarEntry>
            <#assign workEffort = calendarEntry.workEffort>
            <tr<#if alt_row> class="alternate-row"</#if>>
              <td>${workEffort.estimatedStartDate}</td>
              <td>${workEffort.estimatedStartDate}</td>
              <td>${workEffort.getRelatedOne("WorkEffortType").get("description",locale)}</td>
              <td class="button-col"><a href="<@ofbizUrl>EditWorkEffort?workEffortId=${workEffort.workEffortId}${addlParam?if_exists}</@ofbizUrl>">${workEffort.workEffortName}</a></td>
            </tr>
            <#assign alt_row = !alt_row>
          </#list>
        </#if>
      </#list>
    </table>
  <#else>
    <div class="screenlet-body">${uiLabelMap.WorkEffortNoEventsFound}.</div>
  </#if>
</div>