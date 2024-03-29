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

<script language="JavaScript" type="text/javascript">
    function quicklookup(func, locationelement, facilityelement, productelement) {
        
        var productId = productelement.value;
        var facilityId = facilityelement.value;
        var request = "LookupProductInventoryLocation?productId=" + productId + "&facilityId=" + facilityId;
        window[func](locationelement, request);
    }
</script>

<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">${uiLabelMap.ProductStockMovesNeeded}</li>
            <li><a href="<@ofbizUrl>PickMoveStockSimple?facilityId=${facilityId?if_exists}</@ofbizUrl>">${uiLabelMap.CommonPrint}</a></li>
        </ul>
        <br class="clear"/>
    </div>
    <div class="screenlet-body">
          <form method="post" action="<@ofbizUrl>processPhysicalStockMove</@ofbizUrl>" name='selectAllForm' style='margin: 0;'>
              <#-- general request fields -->
              <input type="hidden" name="facilityId" value="${facilityId?if_exists}">
              <input type="hidden" name="_useRowSubmit" value="Y">
              <#assign rowCount = 0>
              <table cellspacing="0" class="basic-table hover-bar">
                <tr class="header-row">
                    <td>${uiLabelMap.ProductProductId}</td>
                    <td>${uiLabelMap.ProductProduct}</td>
                    <td>${uiLabelMap.ProductFromLocation}</td>
                    <td>${uiLabelMap.ProductQoh}</td>
                    <td>${uiLabelMap.ProductAtp}</td>
                    <td>${uiLabelMap.ProductToLocation}</td>
                    <td>${uiLabelMap.ProductQoh}</td>
                    <td>${uiLabelMap.ProductAtp}</td>
                    <td>${uiLabelMap.ProductMinimumStock}</td>
                    <td>${uiLabelMap.ProductMoveQuantity}</td>
                    <td>${uiLabelMap.CommonConfirm}</td>
                    <td align="right">
                        ${uiLabelMap.ProductSelectAll}&nbsp;
                        <input type="checkbox" name="selectAll" value="Y" onclick="javascript:toggleAll(this, 'selectAllForm');highlightAllRows(this, 'moveInfoId_tableRow_', 'selectAllForm');">
                    </td>
                </tr>
                <#if moveByOisgirInfoList?has_content || moveByPflInfoList?has_content>
                    <#assign alt_row = false>
                    <#list moveByOisgirInfoList?if_exists as moveByOisgirInfo>
                        <#assign product = moveByOisgirInfo.product>
                        <#assign facilityLocationFrom = moveByOisgirInfo.facilityLocationFrom>
                        <#assign facilityLocationTypeEnumFrom = (facilityLocationFrom.getRelatedOneCache("TypeEnumeration"))?if_exists>
                        <#assign facilityLocationTo = moveByOisgirInfo.facilityLocationTo>
                        <#assign targetProductFacilityLocation = moveByOisgirInfo.targetProductFacilityLocation>
                        <#assign facilityLocationTypeEnumTo = (facilityLocationTo.getRelatedOneCache("TypeEnumeration"))?if_exists>
                        <#assign totalQuantity = moveByOisgirInfo.totalQuantity>
                        <tr id="moveInfoId_tableRow_${rowCount}" valign="middle"<#if alt_row> class="alternate-row"</#if>>
                            <td>${product.productId}</td>
                            <td>${product.internalName?if_exists}</td>
                            <td>${facilityLocationFrom.areaId?if_exists}:${facilityLocationFrom.aisleId?if_exists}:${facilityLocationFrom.sectionId?if_exists}:${facilityLocationFrom.levelId?if_exists}:${facilityLocationFrom.positionId?if_exists}<#if facilityLocationTypeEnumFrom?has_content>(${facilityLocationTypeEnumFrom.description})</#if>[${facilityLocationFrom.locationSeqId}]</td>
                            <td>${moveByOisgirInfo.quantityOnHandTotalFrom?if_exists}</td>
                            <td>${moveByOisgirInfo.availableToPromiseTotalFrom?if_exists}</td>
                            <td>${facilityLocationTo.areaId?if_exists}:${facilityLocationTo.aisleId?if_exists}:${facilityLocationTo.sectionId?if_exists}:${facilityLocationTo.levelId?if_exists}:${facilityLocationTo.positionId?if_exists}<#if facilityLocationTypeEnumTo?has_content>(${facilityLocationTypeEnumTo.description})</#if>[${facilityLocationTo.locationSeqId}]</td>
                            <td>${moveByOisgirInfo.quantityOnHandTotalTo?if_exists}</td>
                            <td>${moveByOisgirInfo.availableToPromiseTotalTo?if_exists}</td>
                            <td>${targetProductFacilityLocation.minimumStock?if_exists}</td>
                            <td>${targetProductFacilityLocation.moveQuantity?if_exists}</td>
                            <td align="right">
                                <input type="hidden" name="productId_o_${rowCount}" value="${product.productId?if_exists}">
                                <input type="hidden" name="facilityId_o_${rowCount}" value="${facilityId?if_exists}">
                                <input type="hidden" name="locationSeqId_o_${rowCount}" value="${facilityLocationFrom.locationSeqId?if_exists}">
                                <input type="hidden" name="targetLocationSeqId_o_${rowCount}" value="${facilityLocationTo.locationSeqId?if_exists}">
                                <input type="text" name="quantityMoved_o_${rowCount}" size="6" value="${totalQuantity?string.number}">
                            </td>
                            <td align="right">
                                <input type="checkbox" name="_rowSubmit_o_${rowCount}" value="Y" onclick="javascript:checkToggle(this, 'selectAllForm');highlightRow(this,'moveInfoId_tableRow_${rowCount}');">
                            </td>
                        </tr>
                        <#assign rowCount = rowCount + 1>
                        <#-- toggle the row color -->
                        <#assign alt_row = !alt_row>
                    </#list>
                    <#list moveByPflInfoList?if_exists as moveByPflInfo>
                        <#assign product = moveByPflInfo.product>
                        <#assign facilityLocationFrom = moveByPflInfo.facilityLocationFrom>
                        <#assign facilityLocationTypeEnumFrom = (facilityLocationFrom.getRelatedOneCache("TypeEnumeration"))?if_exists>
                        <#assign facilityLocationTo = moveByPflInfo.facilityLocationTo>
                        <#assign targetProductFacilityLocation = moveByPflInfo.targetProductFacilityLocation>
                        <#assign facilityLocationTypeEnumTo = (facilityLocationTo.getRelatedOneCache("TypeEnumeration"))?if_exists>
                        <#assign totalQuantity = moveByPflInfo.totalQuantity>
                        <tr id="moveInfoId_tableRow_${rowCount}" valign="middle"<#if alt_row> class="alternate-row"</#if>>
                            <td>${product.productId}</td>
                            <td>${product.internalName?if_exists}</td>
                            <td>${facilityLocationFrom.areaId?if_exists}:${facilityLocationFrom.aisleId?if_exists}:${facilityLocationFrom.sectionId?if_exists}:${facilityLocationFrom.levelId?if_exists}:${facilityLocationFrom.positionId?if_exists}<#if facilityLocationTypeEnumFrom?has_content>(${facilityLocationTypeEnumFrom.description})</#if>[${facilityLocationFrom.locationSeqId}]</td>
                            <td>${moveByPflInfo.quantityOnHandTotalFrom?if_exists}</td>
                            <td>${moveByPflInfo.availableToPromiseTotalFrom?if_exists}</td>
                            <td>${facilityLocationTo.areaId?if_exists}:${facilityLocationTo.aisleId?if_exists}:${facilityLocationTo.sectionId?if_exists}:${facilityLocationTo.levelId?if_exists}:${facilityLocationTo.positionId?if_exists}<#if facilityLocationTypeEnumTo?has_content>(${facilityLocationTypeEnumTo.description})</#if>[${facilityLocationTo.locationSeqId}]</td>
                            <td>${moveByPflInfo.quantityOnHandTotalTo?if_exists}</td>
                            <td>${moveByPflInfo.availableToPromiseTotalTo?if_exists}</td>
                            <td>${targetProductFacilityLocation.minimumStock?if_exists}</td>
                            <td>${targetProductFacilityLocation.moveQuantity?if_exists}</td>
                            <td align="right">
                                <input type="hidden" name="productId_o_${rowCount}" value="${product.productId?if_exists}">
                                <input type="hidden" name="facilityId_o_${rowCount}" value="${facilityId?if_exists}">
                                <input type="hidden" name="locationSeqId_o_${rowCount}" value="${facilityLocationFrom.locationSeqId?if_exists}">
                                <input type="hidden" name="targetLocationSeqId_o_${rowCount}" value="${facilityLocationTo.locationSeqId?if_exists}">
                                <input type="text" name="quantityMoved_o_${rowCount}" size="6" value="${totalQuantity?string.number}">
                            </td>
                            <td align="right">
                                <input type="checkbox" name="_rowSubmit_o_${rowCount}" value="Y" onclick="javascript:checkToggle(this, 'selectAllForm');highlightRow(this,'moveInfoId_tableRow_${rowCount}');">
                            </td>
                        </tr>
                        <#assign rowCount = rowCount + 1>
                    </#list>
                    <tr>
                        <td colspan="13" align="right">
                            <a href="javascript:document.selectAllForm.submit();" class="buttontext">${uiLabelMap.ProductConfirmSelectedMoves}</a>
                        </td>
                    </tr>
                <#else>
                    <tr><td colspan="13"><h3>${uiLabelMap.ProductNoStockMovesNeeded}.</h3></td></tr>
                </#if>
                <#assign messageCount = 0>
                <#list pflWarningMessageList?if_exists as pflWarningMessage>
                    <#assign messageCount = messageCount + 1>
                    <tr><td colspan="13"><h3>${messageCount}:${pflWarningMessage}.</h3></td></tr>
                </#list>
            </table>
            <input type="hidden" name="_rowCount" value="${rowCount}">
        </form>
    </div>
    <div class="screenlet-title-bar">
        <ul>
            <li class="h3">${uiLabelMap.ProductQuickStockMove}</li>
        </ul>
        <br class="clear"/>
    </div>
    <div class="screenlet-body">
        <form method="post" action="<@ofbizUrl>processQuickStockMove</@ofbizUrl>" name='quickStockMove'>
            <input type="hidden" name="facilityId" value="${facilityId?if_exists}">
            <table cellspacing="0" class="basic-table hover-bar">
                <tr class="header-row">
                    <td>${uiLabelMap.ProductProduct}</td>
                    <td>${uiLabelMap.ProductFromLocation}</td>
                    <td>${uiLabelMap.ProductToLocation}</td>
                    <td>${uiLabelMap.ProductMoveQuantity}</td>
                    <td>&nbsp</td>
                </tr>
                    <td>
                        <input type="text" size="20" name="productId" maxlength="20">
                        <a href="javascript:call_fieldlookup2(document.quickStockMove.productId,'LookupProduct');">
                            <img src="<@ofbizContentUrl>/images/fieldlookup.gif</@ofbizContentUrl>" width="15" height="14" border="0" alt="${uiLabelMap.CommonClickHereForFieldLookup}"/>
                        </a>
                    </td>
                    <td>
                        <input type="text" size="20" name="locationSeqId" maxlength="20">
                        <a href="javascript:quicklookup('call_fieldlookup2', document.quickStockMove.locationSeqId, document.quickStockMove.facilityId, document.quickStockMove.productId)">
                            <img src="<@ofbizContentUrl>/images/fieldlookup.gif</@ofbizContentUrl>" width="15" height="14" border="0" alt="${uiLabelMap.CommonClickHereForFieldLookup}"/>
                        </a>    
                    <td>
                        <input type="text" size="20" name="targetLocationSeqId" maxlength="20">
                        <a href="javascript:call_fieldlookup2(document.quickStockMove.targetLocationSeqId,'LookupFacilityLocation?facilityId=${facilityId}&locationTypeEnumId=FLT_PICKLOC');">
                            <img src="<@ofbizContentUrl>/images/fieldlookup.gif</@ofbizContentUrl>" width="15" height="14" border="0" alt="${uiLabelMap.CommonClickHereForFieldLookup}"/>
                        </a>
                    </td> 
                    <td><input type="text" name="quantityMoved" size="6"></td>
                <tr>
                    <td colspan="13" align="right">
                        <a href="javascript:document.quickStockMove.submit();" class="buttontext">${uiLabelMap.ProductQuickStockMove}</a>
                    </td>
                </tr>
            </table>
        </form>
    </div>
</div>