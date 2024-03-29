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
<#assign states = Static["org.ofbiz.common.CommonWorkers"].getStateList(delegator)>
<#list states as state>
    <option value='${state.geoId}'>${state.geoName?default(state.geoId)}</option>
</#list>

<#-- Here is some alternate code to get states limited to a region
<#if requestParameters.CUSTOMER_COUNTRY?exists>
    <#assign stateAssocs = Static["org.ofbiz.common.CommonWorkers"].getAssociatedStateList(delegator,requestParameters.CUSTOMER_COUNTRY)>
<#else>
    <#assign stateAssocs = Static["org.ofbiz.common.CommonWorkers"].getAssociatedStateList(delegator,defaultCountryId)>
</#if>

<#list stateAssocs as stateAssoc>
    <#assign state = delegator.getRelatedOne("AssocGeo", stateAssoc )>
    <option value='${state.geoId}'>${state.geoName?default(state.geoId)}</option>
</#list>
-->
