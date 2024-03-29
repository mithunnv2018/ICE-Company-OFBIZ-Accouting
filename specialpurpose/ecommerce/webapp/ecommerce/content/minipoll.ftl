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

<#assign groupName = page.randomSurveyGroup?if_exists>
<#if groupName?has_content>
  <#assign randomSurvey = Static["org.ofbiz.product.store.ProductStoreWorker"].getRandomSurveyWrapper(request, "testSurveyGroup")?if_exists>
</#if>

<#if randomSurvey?has_content>
<div id="minipoll" class="screenlet">
    <div class="screenlet-header">
        <div class="boxhead">${randomSurvey.getSurveyName()?if_exists}</div>
    </div>
    <div class="screenlet-body">
        <form method="post" action="<@ofbizUrl>minipoll<#if requestAttributes._CURRENT_VIEW_?exists>/${requestAttributes._CURRENT_VIEW_}</#if></@ofbizUrl>" style="margin: 0;">
          ${randomSurvey.render()}
        </form>
    </div>
</div>
</#if>
