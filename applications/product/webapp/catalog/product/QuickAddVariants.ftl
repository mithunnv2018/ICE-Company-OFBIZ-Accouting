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
<script>
function setProductVariantId(e, value, fieldname) {
    var cform = document.selectAllForm;
    var len = cform.elements.length;
    for (var i = 0; i < len; i++) {
        var element = cform.elements[i];
        if (element.name == fieldname) {
            if (e.checked) {
                if (element.value == null || element.value == "") {
                    element.value = value;
                }
            } else {
                element.value = "";
            }
            return;
        }
    }
}
function clickAll(e) {
    var cform = document.selectAllForm;
    var len = cform.elements.length;
    for (var i = 0; i < len; i++) {
        var element = cform.elements[i];
        if (element.name.substring(0, 10) == "_rowSubmit" && element.checked != e.checked) {
            element.click();
        }
    }
}
</script>
<#if (product.isVirtual)?if_exists != "Y">
    <h2>${uiLabelMap.ProductWarningProductNotVirtual}</h2>
</#if>
<#if featureTypes?has_content && (featureTypes.size() > 0)>
    <table cellspacing="0" class="basic-table">
        <#assign rowCount = 0>
        <form method="post" action="<@ofbizUrl>QuickAddChosenVariants</@ofbizUrl>" name="selectAllForm">
            <input type="hidden" name="productId" value="${productId}">
            <input type="hidden" name="_useRowSubmit" value="Y">
            <input type="hidden" name="_checkGlobalScope" value="Y">
        <tr class="header-row">
            <#list featureTypes as featureType>
                <td><b>${featureType}</b></td>
            </#list>
            <td><b>${uiLabelMap.ProductNewProductCreate} !</b></td>
            <td><b>${uiLabelMap.ProductSequenceNum}</b></td>
            <td><b>${uiLabelMap.ProductExistingVariant} :</b></td>
            <td align="right"><b>${uiLabelMap.CommonAll}<input type="checkbox" name="selectAll" value="${uiLabelMap.CommonY}" onclick="javascript:clickAll(this);"></b></td>
        </tr>

        <#assign defaultSequenceNum = 10>
        <#assign rowClass = "2">
        <#list featureCombinationInfos as featureCombinationInfo>
            <#assign curProductFeatureAndAppls = featureCombinationInfo.curProductFeatureAndAppls>
            <#assign existingVariantProductIds = featureCombinationInfo.existingVariantProductIds>
            <#assign defaultVariantProductId = featureCombinationInfo.defaultVariantProductId>
            <tr valign="middle"<#if rowClass == "1"> class="alternate-row"</#if>>
                <#assign productFeatureIds = "">
                <#list curProductFeatureAndAppls as productFeatureAndAppl>
                <td>
                    ${productFeatureAndAppl.description?if_exists}
                    <#assign productFeatureIds = productFeatureIds + "|" + productFeatureAndAppl.productFeatureId>
                </td>
                </#list>
                <input type="hidden" name="productFeatureIds_o_${rowCount}" value="${productFeatureIds}"/>
                <td>
                    <input type="text" size="20" maxlength="20" name="productVariantId_o_${rowCount}" value=""/>
                </td>
                <td>
                    <input type"text" size="5" maxlength="10" name="sequenceNum_o_${rowCount}" value="${defaultSequenceNum}"/>
                </td>
                <td>
                    <div>
                    <#list existingVariantProductIds as existingVariantProductId>
                        <a href="<@ofbizUrl>EditProduct?productId=${existingVariantProductId}</@ofbizUrl>" class="buttontext">${existingVariantProductId}</a>
                    </#list>
                    </div>
                </td>
                <td align="right">
                  <input type="checkbox" name="_rowSubmit_o_${rowCount}" value="Y" onclick="javascript:setProductVariantId(this, '${defaultVariantProductId}', 'productVariantId_o_${rowCount}');">
                </td>
            </tr>
            <#assign defaultSequenceNum = defaultSequenceNum + 10>
            <#assign rowCount = rowCount + 1>
            <#-- toggle the row color -->
            <#if rowClass == "2">
                <#assign rowClass = "1">
            <#else>
                <#assign rowClass = "2">
            </#if>
        </#list>
        <tr>
            <#assign columns = featureTypes.size() + 4>
            <td colspan="${columns}" align="center">
                <input type="hidden" name="_rowCount" value="${rowCount}">
                <input type="submit" class="smallSubmit" value="${uiLabelMap.CommonCreate}"/>
            </td>
        </tr>
        </form>
    </table>
<#else>
    <b>${uiLabelMap.ProductNoSelectableFeaturesFound}</b>
</#if>
<table cellspacing="0" class="basic-table">
    <form action="<@ofbizUrl>addVariantsToVirtual</@ofbizUrl>" method="post" name="addVariantsToVirtual">
        <tr class="header-row">
            <td><b>${uiLabelMap.ProductVariantAdd}:</b></td>
        </tr>
        <tr>
            <td>
                <br/>
                <input type="hidden" name="productId" value="${productId}"/>
                <span class="label">${uiLabelMap.ProductVariantProductIds}:</span>
                <textarea name="variantProductIdsBag" rows="6" cols="20"></textarea>
                <input type="submit" class="smallSubmit" value="${uiLabelMap.ProductVariantAdd}"/>
            </td>
        </tr>
    </form>
</table>