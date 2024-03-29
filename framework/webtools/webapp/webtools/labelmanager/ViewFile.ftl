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
    <table class="basic-table" cellspacing="3">
        <tr>
            <td colspan="2"><b>${uiLabelMap.WebtoolsLabelManagerKey}</b> ${parameters.sourceKey?if_exists}</td>
        </tr>
        <tr>
            <td colspan="2"><b>${uiLabelMap.WebtoolsLabelManagerFileName}</b> ${parameters.fileName?if_exists}</td>
        </tr>
        <tr>
            <td colspan="2">&nbsp;</td>
        </tr>
        <#list rows as row>
        <tr>
            <td colspan="2">${row?if_exists}</td>
        </tr>
        </#list>
    </table>
</div>
