<%--
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
--%>
<%@ page contentType="text/plain" %><%@ page import="java.util.*" %><%@ page import="org.ofbiz.entity.*" %><%@ page import="org.ofbiz.entity.model.*" %><jsp:useBean id="delegator" type="org.ofbiz.entity.GenericDelegator" scope="request" /><jsp:useBean id="security" type="org.ofbiz.security.Security" scope="request" /><%
if(security.hasPermission("ENTITY_MAINT", session)) {
  ModelReader reader = delegator.getModelReader();
  Collection ec = reader.getEntityNames();
  TreeSet entities = new TreeSet(ec);
  Iterator classNamesIterator = entities.iterator();
  while(classNamesIterator != null && classNamesIterator.hasNext()) { ModelEntity entity = reader.getModelEntity((String)classNamesIterator.next());%>
CREATE TABLE <%=entity.getPlainTableName()%> (<%for(int i=0;i<entity.fields.size();i++){ModelField field=(ModelField)entity.fields.get(i); ModelFieldType type = delegator.getEntityFieldType(entity, field.type);%><%if(field.isPk){%>
  <%=field.colName%> <%=type.sqlType%> NOT NULL,<%}else{%>
  <%=field.colName%> <%=type.sqlType%>,<%}%><%}%>
  CONSTRAINT PK_<%=entity.getPlainTableName()%> PRIMARY KEY (<%=entity.colNameString(entity.pks)%>));
<%}%>
<%
}
else {
  %>ERROR: You do not have permission to use this page (ENTITY_MAINT needed)<%
}
%>
