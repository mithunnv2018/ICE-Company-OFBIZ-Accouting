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

<#if !mechMap.facilityContactMech?exists && mechMap.contactMech?exists>
  <p><h3>${uiLabelMap.PartyContactInfoNotBelongToYou}.</h3></p>
  &nbsp;<a href="<@ofbizUrl>authview/${donePage}?facilityId=${facilityId}</@ofbizUrl>" class="buttontext">${uiLabelMapCommon.Back}</a>
<#else>
  <#if !mechMap.contactMech?exists>
    <#-- When creating a new contact mech, first select the type, then actually create -->
    <#if !preContactMechTypeId?has_content>
    <h1>${title}</h1>
    <div class="button-bar">
      <a href='<@ofbizUrl>authview/${donePage}?facilityId=${facilityId}</@ofbizUrl>' class='buttontext'>${uiLabelMap.CommonGoBack}</a>
    </div>
    <form method="post" action='<@ofbizUrl>EditContactMech</@ofbizUrl>' name="createcontactmechform">
      <input type='hidden' name='facilityId' value='${facilityId}'>
      <input type='hidden' name='DONE_PAGE' value='${donePage?if_exists}'>
      <table width="50%" class="basic-table" cellspacing="0">
        <tr>
          <td class="label">${uiLabelMap.PartySelectContactType}</td>
          <td>
            <select name="preContactMechTypeId" >
              <#list mechMap.contactMechTypes as contactMechType>
                <option value='${contactMechType.contactMechTypeId}'>${contactMechType.get("description",locale)}</option>
              </#list>
            </select>&nbsp;<a href="javascript:document.createcontactmechform.submit()" class="buttontext">${uiLabelMap.CommonCreate}</a>
          </td>
        </tr>
      </table>
    </form>
    </#if>
  </#if>

  <#if mechMap.contactMechTypeId?has_content>
    <#if !mechMap.contactMech?has_content>
      <h1>${title}</h1>
      <div class="button-bar">
        <a href='<@ofbizUrl>authview/${donePage}?facilityId=${facilityId}</@ofbizUrl>' class='buttontext'>${uiLabelMap.CommonGoBack}</a>
      </div>
      <#if contactMechPurposeType?exists>
        <div><span class="label">(${uiLabelMap.PartyMsgContactHavePurpose}</span>"${contactMechPurposeType.get("description",locale)?if_exists}")</div>
      </#if>
      <table width="90%" class="basic-table" cellspacing="0">
        <form method="post" action='<@ofbizUrl>${mechMap.requestName}</@ofbizUrl>' name="editcontactmechform">
        <input type='hidden' name='DONE_PAGE' value='${donePage}'>
        <input type='hidden' name='contactMechTypeId' value='${mechMap.contactMechTypeId}'>
        <input type='hidden' name='facilityId' value='${facilityId}'>
        <#if preContactMechTypeId?exists><input type='hidden' name='preContactMechTypeId' value='${preContactMechTypeId}'></#if>
        <#if contactMechPurposeTypeId?exists><input type='hidden' name='contactMechPurposeTypeId' value='${contactMechPurposeTypeId?if_exists}'></#if>

        <#if paymentMethodId?exists><input type='hidden' name='paymentMethodId' value='${paymentMethodId}'></#if>
    <#else>
      <h1>${title}</h1>
      <div class="button-bar">
        <a href='<@ofbizUrl>authview/${donePage}?facilityId=${facilityId}</@ofbizUrl>' class='buttontext'>${uiLabelMap.CommonGoBack}</a>
        <a href="<@ofbizUrl>EditContactMech?facilityId=${facilityId}</@ofbizUrl>" class="buttontext">${uiLabelMap.ProductNewContactMech}</a>
      </div>
      <table class="basic-table" cellspacing="0">
        <#if mechMap.purposeTypes?has_content>
        <tr>
          <td valign="top" class="label">${uiLabelMap.PartyContactPurposes}</td>
          <td>
            <table class="basic-table" cellspacing="0">
            <#if mechMap.facilityContactMechPurposes?has_content>
              <#assign alt_row = false>
              <#list mechMap.facilityContactMechPurposes as facilityContactMechPurpose>
                <#assign contactMechPurposeType = facilityContactMechPurpose.getRelatedOneCache("ContactMechPurposeType")>
                <tr valign="middle"<#if alt_row> class="alternate-row"</#if>>
                  <td>
                      <#if contactMechPurposeType?has_content>
                        <b>${contactMechPurposeType.get("description",locale)}</b>
                      <#else>
                        <b>${uiLabelMap.PartyMechPurposeTypeNotFound}: "${facilityContactMechPurpose.contactMechPurposeTypeId}"</b>
                      </#if>
                      (${uiLabelMap.CommonSince}: ${facilityContactMechPurpose.fromDate})
                      <#if facilityContactMechPurpose.thruDate?has_content>(${uiLabelMap.CommonExpires}: ${facilityContactMechPurpose.thruDate.toString()}</#if>
                      <a href="javascript:$('deleteFacilityContactMechPurpose_${facilityContactMechPurpose_index}').submit();" class="buttontext">${uiLabelMap.CommonDelete}</a>
                  </td>
                </tr>
                <#-- toggle the row color -->
                <#assign alt_row = !alt_row>
                <form id="deleteFacilityContactMechPurpose_${facilityContactMechPurpose_index}" method="post" action="<@ofbizUrl>deleteFacilityContactMechPurpose</@ofbizUrl>">
                  <input type="hidden" name="facilityId" value="${facilityId?if_exists}" />
                  <input type="hidden" name="contactMechId" value="${contactMechId?if_exists}" />
                  <input type="hidden" name="contactMechPurposeTypeId" value="${(facilityContactMechPurpose.contactMechPurposeTypeId)?if_exists}" />
                  <input type="hidden" name="fromDate" value="${(facilityContactMechPurpose.fromDate)?if_exists}" />
                  <input type="hidden" name="DONE_PAGE" value="${donePage?if_exists}" />
                  <input type="hidden" name="useValues" value="true" />
                </form>
              </#list>
              </#if>
              <tr>
                <td>
                  <form method="post" action='<@ofbizUrl>createFacilityContactMechPurpose?DONE_PAGE=${donePage}&useValues=true</@ofbizUrl>' name='newpurposeform'>
                  <input type="hidden" name='facilityId' value='${facilityId}'>
                  <input type="hidden" name='contactMechId' value='${contactMechId?if_exists}'>
                    <select name='contactMechPurposeTypeId'>
                      <option></option>
                      <#list mechMap.purposeTypes as contactMechPurposeType>
                        <option value='${contactMechPurposeType.contactMechPurposeTypeId}'>${contactMechPurposeType.get("description",locale)}</option>
                      </#list>
                    </select>
                    &nbsp;<a href='javascript:document.newpurposeform.submit()' class='buttontext'>${uiLabelMap.PartyAddPurpose}</a>
                  </form>
                </td>
              </tr>
            </table>
          </td>
        </tr>
        </#if>
        <form method="post" action='<@ofbizUrl>${mechMap.requestName}</@ofbizUrl>' name="editcontactmechform">
        <input type="hidden" name="contactMechId" value='${contactMechId}'>
        <input type="hidden" name="contactMechTypeId" value='${mechMap.contactMechTypeId}'>
        <input type="hidden" name='facilityId' value='${facilityId}'>
    </#if>

  <#if "POSTAL_ADDRESS" = mechMap.contactMechTypeId?if_exists>
    <tr>
      <td class="label">${uiLabelMap.PartyToName}</td>
      <td>
        <input type="text" size="30" maxlength="60" name="toName" value="${(mechMap.postalAddress.toName)?default(request.getParameter('toName')?if_exists)}">
      </td>
    </tr>
    <tr>
      <td class="label">${uiLabelMap.PartyAttentionName}</td>
      <td>
        <input type="text" size="30" maxlength="60" name="attnName" value="${(mechMap.postalAddress.attnName)?default(request.getParameter('attnName')?if_exists)}">
      </td>
    </tr>
    <tr>
      <td class="label">${uiLabelMap.PartyAddressLine1}</td>
      <td>
        <input type="text" class="required" size="30" maxlength="30" name="address1" value="${(mechMap.postalAddress.address1)?default(request.getParameter('address1')?if_exists)}">
      *</td>
    </tr>
    <tr>
      <td class="label">${uiLabelMap.PartyAddressLine2}</td>
      <td>
          <input type="text" size="30" maxlength="30" name="address2" value="${(mechMap.postalAddress.address2)?default(request.getParameter('address2')?if_exists)}">
      </td>
    </tr>
    <tr>
      <td class="label">${uiLabelMap.PartyCity}</td>
      <td>
          <input type="text" class="required" size="30" maxlength="30" name="city" value="${(mechMap.postalAddress.city)?default(request.getParameter('city')?if_exists)}">
      *</td>
    </tr>
    <tr>
      <td class="label">${uiLabelMap.PartyState}</td>
      <td>
        <select name="stateProvinceGeoId" class="required">
          <option>${(mechMap.postalAddress.stateProvinceGeoId)?if_exists}</option>
          <option></option>
           ${screens.render("component://common/widget/CommonScreens.xml#states")}
        </select>
      *</td>
    </tr>
    <tr>
      <td class="label">${uiLabelMap.PartyZipCode}</td>
      <td>
        <input type="text" class="required" size="12" maxlength="10" name="postalCode" value="${(mechMap.postalAddress.postalCode)?default(request.getParameter('postalCode')?if_exists)}">
      *</td>
    </tr>
    <tr>
      <td class="label">${uiLabelMap.PartyCountry}</td>
      <td>
        <select name="countryGeoId" class="required">
          <option>${(mechMap.postalAddress.countryGeoId)?if_exists}</option>
          <option></option>
          ${screens.render("component://common/widget/CommonScreens.xml#countries")}
        </select>
      *</td>
    </tr>
  <#elseif "TELECOM_NUMBER" = mechMap.contactMechTypeId?if_exists>
    <tr>
      <td class="label">${uiLabelMap.PartyPhoneNumber}</td>
      <td>
        <input type="text" size="4" maxlength="10" name="countryCode" value="${(mechMap.telecomNumber.countryCode)?default(request.getParameter('countryCode')?if_exists)}">
        -&nbsp;<input type="text" size="4" maxlength="10" name="areaCode" value="${(mechMap.telecomNumber.areaCode)?default(request.getParameter('areaCode')?if_exists)}">
        -&nbsp;<input type="text" size="15" maxlength="15" name="contactNumber" value="${(mechMap.telecomNumber.contactNumber)?default(request.getParameter('contactNumber')?if_exists)}">
        &nbsp;ext&nbsp;<input type="text" size="6" maxlength="10" name="extension" value="${(mechMap.facilityContactMech.extension)?default(request.getParameter('extension')?if_exists)}">
      </td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td>[${uiLabelMap.PartyCountryCode}] [${uiLabelMap.PartyAreaCode}] [${uiLabelMap.PartyContactNumber}] [${uiLabelMap.PartyExtension}]</td>
    </tr>
  <#elseif "EMAIL_ADDRESS" = mechMap.contactMechTypeId?if_exists>
    <tr>
      <td class="label">${uiLabelMap.PartyEmailAddress}</td>
      <td>
          <input type="text" class="required" size="60" maxlength="255" name="emailAddress" value="${(mechMap.contactMech.infoString)?default(request.getParameter('emailAddress')?if_exists)}">
      *</td>
    </tr>
  <#else>
    <tr>
      <td class="label">${mechMap.contactMechType.get("description",locale)}</td>
      <td>
          <input type="text" class="required" size="60" maxlength="255" name="infoString" value="${(mechMap.contactMech.infoString)?if_exists}">
      *</td>
    </tr>
  </#if>
    <tr>
      <td>&nbsp;</td>
      <td>
        <a href="javascript:document.editcontactmechform.submit()" class="buttontext">${uiLabelMap.CommonSave}</a>
      </td>
    </tr>
  </form>
  </table>
  </#if>
</#if>
