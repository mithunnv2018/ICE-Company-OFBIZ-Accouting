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

<#-- ==================== Party Selection dialog box ========================= -->
<div class="screenlet">
    <div class="screenlet-body">
<table border="0" width="100%" cellspacing="0" cellpadding="0">
  <form method="post" action="<@ofbizUrl>finalizeOrder</@ofbizUrl>" name="checkoutsetupform">
    <input type="hidden" name="finalizeReqAdditionalParty" value="false"/>
    <input type="hidden" name="finalizeMode" value="addpty"/>
  </form>
  <form method="post" action="<@ofbizUrl>setAdditionalParty</@ofbizUrl>" name="quickAddPartyForm">

  <tr>
    <td><div>1) ${uiLabelMap.OrderSelectPartyToOrder} :</div></td>
  </tr>
  <tr>
    <td width="100%">
      <table border="0" cellspacing="0" cellpadding="0" class="boxbottom">
        <tr>
          <td align="right">
            <input type="radio" name="additionalPartyType" value="Person" onclick="<#if additionalPartyType?exists>javascript:document.quickAddPartyForm.additionalPartyId.value='';</#if>document.quickAddPartyForm.submit()"<#if (additionalPartyType?exists && additionalPartyType == "Person")> checked="checked"</#if>>
	  </td>
          <td>
            <div>${uiLabelMap.CommonPerson}</div>
          </td>
        </tr>
        <tr>
          <td align="right">
            <input type="radio" name="additionalPartyType" value="Group" onclick="<#if additionalPartyType?exists>javascript:document.quickAddPartyForm.additionalPartyId.value='';</#if>document.quickAddPartyForm.submit()"<#if additionalPartyType?exists && additionalPartyType == "Group"> checked="checked"</#if>>
          </td>
          <td>
            <div>${uiLabelMap.CommonGroup}</div>
          </td>
        </tr>
        <tr>
          <td align="right">
            <input type="radio" name="additionalPartyType" value="None" onclick="<#if additionalPartyType?exists>javascript:document.quickAddPartyForm.additionalPartyId.value='';</#if>document.quickAddPartyForm.submit()"<#if !additionalPartyType?exists> checked="checked"</#if>>
          </td>
          <td>
            <div>${uiLabelMap.OrderPartyDontWish}</div>
          </td>
        </tr>
      </table>
    </td>
  </tr>

  <tr>
    <td>&nbsp;</td>
  </tr>

  <#if additionalPartyType?exists && additionalPartyType != "" && additionalPartyType != "None">
    <#if additionalPartyType == "Person">
      <#assign lookupPartyView="LookupPerson">
    <#else>
      <#assign lookupPartyView="LookupPartyGroup">
    </#if>
  <tr>
    <td><div>2) ${uiLabelMap.PartyFindParty} :</div></td>
  </tr>

  <tr>
    <td width="100%">
      <table border="0" cellspacing="0" cellpadding="0" class="boxbottom">
        <tr>
          <td>
            <div>${uiLabelMap.CommonIdentifier} :</div>
          </td>
          <td>
            <input type="text" name="additionalPartyId" value="${additionalPartyId?if_exists}" onchange="javascript:document.quickAddPartyForm.submit()">
          </td>
          <td>
            <a href="javascript:document.quickAddPartyForm.additionalPartyId.focus();call_fieldlookup2(document.quickAddPartyForm.additionalPartyId, '${lookupPartyView}');"><img src="<@ofbizContentUrl>/images/fieldlookup.gif</@ofbizContentUrl>" width="16" height="16" border="0" alt="${uiLabelMap.CommonClickHereForFieldLookup}"></a>
          </td>
          <td>
            &nbsp;<a href="javascript:document.quickAddPartyForm.submit()" class="buttontext">${uiLabelMap.CommonApply}</a>
          </td>
        </tr>
      </table>
    </td>
  </tr>

  <tr>
    <td>&nbsp;</td>
  </tr>

  </form>

  <#if roles?has_content>
  <tr>
    <td><div>3) ${uiLabelMap.OrderPartySelectRoleForParty} :</div></td>
  </tr>

  <tr>
    <td width="100%">
      <table border="0" cellspacing="0" cellpadding="0" class="boxbottom">
        <form method="post" action="<@ofbizUrl>addAdditionalParty</@ofbizUrl>">
        <tr>
          <td>&nbsp;</td>
          <td>
            <select name="additionalRoleTypeId" size="5" multiple="multiple">
              <#list roles as role>
              <option value="${role.roleTypeId}">${role.get("description",locale)}</option>
              </#list>
            </select>
          </td>
          <td>&nbsp;</td>
          <td>
            <input type="hidden" name="additionalPartyId" value="${additionalPartyId}">
            <input type="submit" class="smallSubmit" value="${uiLabelMap.CommonAdd}">
          </td>
        </tr>
        </form>
      </table>
    </td>
  </tr>
  </#if> <#-- roles?has_content -->
  <#else>
  </form>
  </#if> <#-- additionalPartyType?has_content -->
</table>
    </div>
</div>
