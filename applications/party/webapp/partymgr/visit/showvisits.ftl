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
  <#if partyId?exists>
    <#assign title = uiLabelMap.PartyParty>
  <#else>
    <#assign title = uiLabelMap.PartyActive>
  </#if>
  <div class="screenlet-title-bar">
    <ul>
      <li class="h3">${title}&nbsp;${uiLabelMap.PartyVisitListing}</li>
      <#if !partyId?exists && showAll?lower_case == "true">
        <li><a href="<@ofbizUrl>showvisits?showAll=false</@ofbizUrl>">${uiLabelMap.PartyShowActive}</a></li>
      <#elseif !partyId?exists>
        <li><a href="<@ofbizUrl>showvisits?showAll=true</@ofbizUrl>">${uiLabelMap.PartyShowAll}</a></li>
      </#if>
    </ul>
    <br class="clear"/>
  </div>
  <div class="screenlet-body">
      <br/>
        <div class="align-float">
            <span class="label">
            <#if (visitSize > 0)>
                <#if (viewIndex > 1)>
                  <a href="<@ofbizUrl>showvisits?VIEW_SIZE=${viewSize}&VIEW_INDEX=${viewIndex-1}<#if sort?has_content>&sort=${sort}</#if><#if partyId?has_content>&partyId=${partyId}</#if>&showAll=${showAll}</@ofbizUrl>" class="smallSubmit">${uiLabelMap.CommonPrevious}</a> |
                </#if>
                ${lowIndex} - ${highIndex} ${uiLabelMap.CommonOf} ${visitSize}
                <#if highIndex < visitSize>
                  | <a href="<@ofbizUrl>showvisits?VIEW_SIZE=${viewSize}&VIEW_INDEX=${viewIndex+1}<#if sort?has_content>&sort=${sort}</#if><#if partyId?has_content>&partyId=${partyId}</#if>&showAll=${showAll}</@ofbizUrl>" class="smallSubmit">${uiLabelMap.CommonNext}</a>
                </#if>
            </#if>
            </span>
        </div>
        <br class="clear"/>
      <br/>
      <table class="basic-table hover-bar" cellspacing="0">
        <tr class="header-row">
          <td><a href="<@ofbizUrl>showvisits?sort=visitId&showAll=${showAll}<#if partyId?has_content>&partyId=${partyId}</#if></@ofbizUrl>">${uiLabelMap.PartyVisitId}</a></td>
          <td><a href="<@ofbizUrl>showvisits?sort=visitorId&showAll=${showAll}<#if visitorId?has_content>&visitorId=${visitorId}</#if></@ofbizUrl>">${uiLabelMap.PartyVisitorId}</a></td>
          <td><a href="<@ofbizUrl>showvisits?sort=partyId&showAll=${showAll}<#if partyId?has_content>&partyId=${partyId}</#if></@ofbizUrl>">${uiLabelMap.PartyPartyId}</a></td>
          <td><a href="<@ofbizUrl>showvisits?sort=userLoginId&showAll=${showAll}<#if partyId?has_content>&partyId=${partyId}</#if></@ofbizUrl>">${uiLabelMap.PartyUserLoginId}</a></td>
          <td><a href="<@ofbizUrl>showvisits?sort=-userCreated&showAll=${showAll}<#if partyId?has_content>&partyId=${partyId}</#if></@ofbizUrl>">${uiLabelMap.PartyNewUser}</a></td>
          <td><a href="<@ofbizUrl>showvisits?sort=webappName&showAll=${showAll}<#if partyId?has_content>&partyId=${partyId}</#if></@ofbizUrl>">${uiLabelMap.PartyWebApp}</a></td>
          <td><a href="<@ofbizUrl>showvisits?sort=clientIpAddress&showAll=${showAll}<#if partyId?has_content>&partyId=${partyId}</#if></@ofbizUrl>">${uiLabelMap.PartyClientIP}</a></td>
          <td><a href="<@ofbizUrl>showvisits?sort=fromDate&showAll=${showAll}<#if partyId?has_content>&partyId=${partyId}</#if></@ofbizUrl>">${uiLabelMap.CommonFromDate}</a></td>
          <td><a href="<@ofbizUrl>showvisits?sort=thruDate&showAll=${showAll}<#if partyId?has_content>&partyId=${partyId}</#if></@ofbizUrl>">${uiLabelMap.CommonThruDate}</a></td>
        </tr>
        <#assign alt_row = false>
        <#list visitList as visitObj>
          <tr<#if alt_row> class="alternate-row"</#if>>
            <td class="button-col"><a href="<@ofbizUrl>visitdetail?visitId=${visitObj.visitId}</@ofbizUrl>">${visitObj.visitId}</a></td>
            <td>${visitObj.visitorId?if_exists}</td>
            <td class="button-col"><a href="<@ofbizUrl>viewprofile?partyId=${visitObj.partyId?if_exists}</@ofbizUrl>">${visitObj.partyId?if_exists}</a></td>
            <td>${visitObj.userLoginId?if_exists}</td>
            <td>${visitObj.userCreated?if_exists}</td>
            <td>${visitObj.webappName?if_exists}</td>
            <td>${visitObj.clientIpAddress?if_exists}</td>
            <td>${(visitObj.fromDate?string)?if_exists}</td>
            <td>${(visitObj.thruDate?string)?if_exists}</td>
          </tr>
          <#assign alt_row = !alt_row>
        </#list>
      </table>
      <br/>
      <div class="align-float">
          <span class="label">
          <#if (visitSize > 0)>
              <#if (viewIndex > 1)>
                <a href="<@ofbizUrl>showvisits?VIEW_SIZE=${viewSize}&VIEW_INDEX=${viewIndex-1}<#if sort?has_content>&sort=${sort}</#if><#if partyId?has_content>&partyId=${partyId}</#if>&showAll=${showAll}</@ofbizUrl>" class="smallSubmit">${uiLabelMap.CommonPrevious}</a> |
              </#if>
              ${lowIndex} - ${highIndex} ${uiLabelMap.CommonOf} ${visitSize}
              <#if highIndex < visitSize>
                | <a href="<@ofbizUrl>showvisits?VIEW_SIZE=${viewSize}&VIEW_INDEX=${viewIndex+1}<#if sort?has_content>&sort=${sort}</#if><#if partyId?has_content>&partyId=${partyId}</#if>&showAll=${showAll}</@ofbizUrl>" class="smallSubmit">${uiLabelMap.CommonNext}</a>
              </#if>
           </#if>
           </span>
      </div>
      <br class="clear"/>
  </div>
</div>