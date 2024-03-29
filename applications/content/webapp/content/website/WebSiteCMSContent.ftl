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

<script type="text/javascript">
    function cmsSave() {
        var simpleFormAction = '<@ofbizUrl>/updateContentCms</@ofbizUrl>';
        var editor = dojo.widget.byId("w_editor");
        if (editor) {
            var cmsdata = dojo.byId("cmsdata");
            cmsdata.value = editor.getEditorContent();
        }

        // get the cmsform
        var form = document.cmsform;

        // set the data resource name
        form.dataResourceName.value = form.contentName.value;

        // check to see if we need to change the form action
        var isUpload = form.elements['isUploadObject'];
        if (isUpload && isUpload.value == 'Y') {
            var uploadValue = form.elements['uploadedFile'].value;
            if (uploadValue == null || uploadValue == "") {
                form.action = simpleFormAction;
            }
        }

        // submit the form
        if (form != null) {
            form.submit();
        } else {
            alert("Cannot find the cmsform!");
        }

        return false;
    }

    function selectDataType(contentId) {
        var selectObject = document.forms['cmsdatatype'].elements['dataResourceTypeId'];
        var typeValue = selectObject.options[selectObject.selectedIndex].value;
        callEditor(true, contentId, '', typeValue);
    }
</script>

<#-- cms menu bar -->
<div id="cmsmenu" style="margin-bottom: 8px;">
    <#if (content?has_content)>
        <a href="javascript:void(0);" onclick="javascript:callEditor(true, '${content.contentId}', '', 'ELECTRONIC_TEXT');" class="tabButton">${uiLabelMap.ContentQuickSubContent}</a>
        <a href="javascript:void(0);" onclick="javascript:callPathAlias('${content.contentId}');" class="tabButton">${uiLabelMap.ContentPathAlias}</a>
        <a href="javascript:void(0);" onclick="javascript:callMetaInfo('${content.contentId}');" class="tabButton">${uiLabelMap.ContentMetaTags}</a>
    </#if>
</div>

<#-- content info -->
<#if (!content?has_content)>
    <div style="margin-bottom: 8px;">
        ${uiLabelMap.CommonNew} <span class="label">${contentAssocTypeId?default("SUBSITE")}</span> ${uiLabelMap.ContentWebSiteAttachedToContent} ${contentIdFrom?default(contentRoot)}
    </div>
</#if>

<#-- dataResourceTypeId -->
<#if (!dataResourceTypeId?has_content)>
    <#if (dataResource?has_content)>
        <#assign dataResourceTypeId = dataResource.dataResourceTypeId/>
    <#elseif (content?has_content)>
        <#assign dataResourceTypeId = "NONE"/>
    <#else>
        <form name="cmsdatatype">
            <table>
                <tr>
                    <td class="label">${uiLabelMap.ContentDataType}</td>
                    <td>
                        <select name="dataResourceTypeId">
                            <option value="NONE">${uiLabelMap.ContentResourceNone}</option>
                            <option value="SHORT_TEXT">${uiLabelMap.ContentResourceShortText}</option>
                            <option value="ELECTRONIC_TEXT">${uiLabelMap.ContentResourceLongText}</option>
                            <option value="URL_RESOURCE">${uiLabelMap.ContentResourceUrlResource}</option>
                            <option value="IMAGE_OBJECT">${uiLabelMap.ContentImage}</option>
                            <option value="VIDEO_OBJECT">${uiLabelMap.ContentResourceVideo}</option>
                            <option value="AUDIO_OBJECT">${uiLabelMap.ContentResourceAudio}</option>
                            <option value="OTHER_OBJECT">${uiLabelMap.ContentResourceOther}</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td align="center" colspan="2">
                        <a href="javascript:void(0);" onclick="javascript:selectDataType('${contentIdFrom?default(contentRoot)}');" class="buttontext">${uiLabelMap.CommonContinue}</a>
                    </td>
                </tr>
                <#list 0..15 as x>
                    <tr><td colspan="2">&nbsp;</td></tr>
                </#list>
            </table>
        </form>
    </#if>
</#if>

<#-- form action -->
<#if (dataResourceTypeId?has_content)>
    <#assign actionSuffix = "ContentCms"/>
    <#if (dataResourceTypeId == "NONE" || (content?has_content && !content.dataResourceId?has_content))>
        <#assign actionMiddle = ""/>
    <#else>
        <#if (dataResourceTypeId?ends_with("_OBJECT"))>
            <#assign actionMiddle = "Object"/>
        <#else>
            <#assign actionMiddle = "Text"/>
        </#if>
    </#if>

    <#if (!contentRoot?has_content)>
        <#assign contentRoot = parameters.contentRoot/>
    </#if>
    <#if (content?has_content)>
        <#assign actionPrefix = "/update"/>
    <#else>
        <#assign actionPrefix = "/create"/>
    </#if>
    <#assign formAction = actionPrefix + actionMiddle + actionSuffix/>
<#else>
    <#assign formAction = "javascript:void(0);"/>
</#if>

<#-- main content form -->
<#if (dataResourceTypeId?has_content)>
    <form name="cmsform" enctype="multipart/form-data" method="post" action="<@ofbizUrl>${formAction}</@ofbizUrl>" style="margin: 0;">
        <#if (content?has_content)>
            <input type="hidden" name="dataResourceId" value="${(dataResource.dataResourceId)?if_exists}"/>
            <input type="hidden" name="mimeTypeId" value="${content.mimeTypeId?default(mimeTypeId)}"/>
            <input type="hidden" name="contentId" value="${content.contentId}"/>

            <#list requestParameters.keySet() as paramName>
                <#if (paramName == 'contentIdFrom' || paramName == 'contentAssocTypeId' || paramName == 'fromDate')>
                    <input type="hidden" name="${paramName}" value="${requestParameters.get(paramName)}"/>
                </#if>
            </#list>
        <#else>
            <input type="hidden" name="contentAssocTypeId" value="${contentAssocTypeId?default('SUBSITE')}"/>
            <input type="hidden" name="ownerContentId" value="${contentIdFrom?default(contentRoot)}"/>
            <input type="hidden" name="contentIdFrom" value="${contentIdFrom?default(contentRoot)}"/>
            <input type="hidden" name="mimeTypeId" value="${mimeTypeId}"/>
        </#if>
        <#if (dataResourceTypeId != 'NONE')>
            <input type="hidden" name="dataResourceTypeId" value="${dataResourceTypeId}"/>
        </#if>
        <input type="hidden" name="webSiteId" value="${webSiteId}"/>
        <input type="hidden" name="dataResourceName" value="${(dataResource.dataResourceName)?if_exists}"/>

        <table>
          <#if (content?has_content)>
            <tr>
                <td class="label">${uiLabelMap.FormFieldTitle_contentId}</td>
                <td>${content.contentId}</td>
            </tr>
          </#if>
          <tr>
            <td class="label">${uiLabelMap.CommonName}</td>
            <td>
                <input type="text" name="contentName" value="${(content.contentName)?if_exists}" size="40"/>
            </td>
          </tr>
          <tr>
            <td class="label">${uiLabelMap.CommonDescription}</td>
            <td>
                <textarea name="description" cols="40" rows="6">${(content.description)?if_exists}</textarea>
            </td>
          </tr>
          <tr>
            <td class="label">${uiLabelMap.ContentMapKey}</td>
            <td>
                <input type="text" name="mapKey" value="${(assoc.mapKey)?if_exists}" size="40"/>
            </td>
          </tr>
          <tr>
            <td class="label">${uiLabelMap.CommonPurpose}</td>
            <td>
                <select name="contentPurposeTypeId">
                    <#if (currentPurposes?has_content)>
                        <#assign purpose = currentPurposes[0].getRelatedOne("ContentPurposeType")/>
                        <option value="${purpose.contentPurposeTypeId}">${purpose.description?default(purpose.contentPurposeTypeId)}</option>
                        <option value="${purpose.contentPurposeTypeId}">----</option>
                    <#else>
                        <option value="SECTION">Section</option>
                        <option value="SECTION">----</option>
                    </#if>
                    <#list purposeTypes as type>
                        <option value="${type.contentPurposeTypeId}">${type.description}</option>
                    </#list>
                </select>
            </td>
          </tr>
          <tr>
            <td class="label">${uiLabelMap.ContentDataType}</td>
            <td>
                <select name="dataTemplateTypeId">
                    <#if (dataResource?has_content)>
                        <#if (dataResource.dataTemplateTypeId?has_content)>
                            <#assign thisType = dataResource.getRelatedOne("DataTemplateType")?if_exists/>
                            <option value="${thisType.dataTemplateTypeId}">${thisType.description}</option>
                            <option value="${thisType.dataTemplateTypeId}">----</option>
                        </#if>
                    </#if>
                    <#list templateTypes as type>
                        <option value="${type.dataTemplateTypeId}">${type.description}</option>
                    </#list>
                </select>
            </td>
          </tr>
          <tr>
            <td class="label">${uiLabelMap.ContentDecorator}</td>
            <td>
                <select name="decoratorContentId">
                    <#if (content?has_content)>
                        <#if (content.decoratorContentId?has_content)>
                            <#assign thisDec = content.getRelatedOne("DecoratorContent")/>
                            <option value="${thisDec.contentId}">${thisDec.contentName}</option>
                            <option value="${thisDec.contentId}">----</option>
                        </#if>
                    </#if>
                    <option value="">${uiLabelMap.ContentResourceNone}</option>
                    <#list decorators as decorator>
                        <option value="${decorator.contentId}">${decorator.contentName}</option>
                    </#list>
                </select>
            </td>
          </tr>
          <tr>
            <td class="label">${uiLabelMap.ContentTemplate}</td>
            <td>
                <select name="templateDataResourceId">
                    <#if (content?has_content)>
                        <#if (content.templateDataResourceId?has_content && content.templateDataResourceId != "NONE")>
                            <#assign template = content.getRelatedOne("TemplateDataResource")/>
                            <option value="${template.dataResourceId}">${template.dataResourceName}</option>
                            <option value="${template.dataResourceId}">----</option>
                        </#if>
                    </#if>
                    <option value="">${uiLabelMap.ContentResourceNone}</option>
                    <#list templates as template>
                        <option value="${template.dataResourceId}">${template.dataResourceName}</option>
                    </#list>
                </select>
            </td>
          </tr>
          <tr>
            <td class="label">${uiLabelMap.CommonStatus}</td>
            <td>
                <select name="statusId">
                    <#if (content?has_content)>
                        <#if (content.statusId?has_content)>
                            <#assign statusItem = content.getRelatedOne("StatusItem")/>
                            <option value="${statusItem.statusId}">${statusItem.description}</option>
                            <option value="${statusItem.statusId}">----</option>
                        </#if>
                    </#if>
                    <#list statuses as status>
                        <option value="${status.statusId}">${status.description}</option>
                    </#list>
                </select>
            </td>
          </tr>
          <tr>
            <td class="label">${uiLabelMap.FormFieldTitle_isPublic}</td>
            <td>
                <select name="isPublic">
                    <#if (dataResource?has_content)>
                        <#if (dataResource.isPublic?has_content)>
                            <option>${dataResource.isPublic}</option>
                            <option value="${dataResource.isPublic}">----</option>
                        <#else>
                            <option></option>
                        </#if>
                    </#if>
                    <option>Y</option>
                    <option>N</option>
                </select>
            </td>
          </tr>
          <tr>
            <td colspan="2">
              <textarea id="cmsdata" name="textData" cols="40" rows="6" style="display: none;">
                <#if (dataText?has_content)>
                    ${dataText.textData}
                </#if>
              </textarea>
            </td>
          </tr>

          <#-- this all depends on the dataResourceTypeId which was selected -->
          <#if (dataResourceTypeId == 'IMAGE_OBJECT' || dataResourceTypeId == 'OTHER_OBJECT' ||
                dataResourceTypeId == 'VIDEO_OBJECT' || dataResourceTypeId == 'AUDIO_OBJECT')>
            <tr>
              <td colspan="2" align="right">
                <#if ((content.contentId)?has_content)>
                    <@renderContentAsText contentId="${content.contentId}" ignoreTemplate="true"/>
                </#if>
              </td>
            </tr>
            <tr>
              <td class="label">${uiLabelMap.CommonUpload}</td>
              <td>
                <input type="hidden" name="isUploadObject" value="Y"/>
                <input type="file" name="uploadedFile" size="30"/>
              </td>
            </tr>
          <#elseif (dataResourceTypeId == 'URL_RESOURCE')>
            <tr>
              <td class="label">${uiLabelMap.ContentUrl}</td>
              <td>
                <input type="text" name="objectInfo" size="40" maxsize="255" value="${(dataResource.objectInfo)?if_exists}"/>
              </td>
            </tr>
          <#elseif (dataResourceTypeId == 'SHORT_TEXT')>
            <tr>
              <td class="label">${uiLabelMap.ContentText}</td>
              <td>
                <input type="text" name="objectInfo" size="40" maxsize="255" value="${(dataResource.objectInfo)?if_exists}"/>
              </td>
            </tr>
          <#elseif (dataResourceTypeId == 'ELECTRONIC_TEXT')>
            <tr>
              <td colspan="2">
                <div id="editorcontainer" class="nocolumns">
                    <div id="cmseditor" style="margin: 0; width: 100%; border: 1px solid black;"></div>
                </div>
              </td>
            </tr>
          </#if>

          <tr>
            <td align="center" colspan="2">
                <a href="javascript:void(0);" onclick="javascript:cmsSave();" class="buttontext">${uiLabelMap.CommonSave}</a>
            </td>
          </tr>
        </table>
    </form>
</#if>