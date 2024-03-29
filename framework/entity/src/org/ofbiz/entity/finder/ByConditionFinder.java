/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.entity.finder;

import java.util.Map;

import org.ofbiz.base.util.UtilXml;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityJoinOperator;
import org.ofbiz.entity.finder.EntityFinderUtil.Condition;
import org.ofbiz.entity.finder.EntityFinderUtil.ConditionExpr;
import org.ofbiz.entity.finder.EntityFinderUtil.ConditionList;
import org.ofbiz.entity.finder.EntityFinderUtil.ConditionObject;
import org.ofbiz.entity.model.ModelEntity;
import org.w3c.dom.Element;

/**
 * Uses the delegator to find entity values by a condition
 *
 */
public class ByConditionFinder extends ListFinder {
    public static final String module = ByConditionFinder.class.getName();

    protected Condition whereCondition;
    protected Condition havingCondition;

    public ByConditionFinder(Element element) {
        super(element, "condition");

        // NOTE: the whereCondition can be null, ie (condition-expr | condition-list) is optional; if left out, means find all, or with no condition in essense
        // process condition-expr | condition-list
        Element conditionExprElement = UtilXml.firstChildElement(element, "condition-expr");
        Element conditionListElement = UtilXml.firstChildElement(element, "condition-list");
        Element conditionObjectElement = UtilXml.firstChildElement(element, "condition-object");
        if (conditionExprElement != null) {
            this.whereCondition = new ConditionExpr(conditionExprElement);
        } else if (conditionListElement != null) {
            this.whereCondition = new ConditionList(conditionListElement);
        } else if (conditionObjectElement != null) {
            this.whereCondition = new ConditionObject(conditionObjectElement);
        }
    }

    protected EntityCondition getWhereEntityCondition(Map<String, Object> context, ModelEntity modelEntity, GenericDelegator delegator) {
        // create whereEntityCondition from whereCondition
        if (this.whereCondition != null) {
            return this.whereCondition.createCondition(context, modelEntity.getEntityName(), delegator);
        }
        return null;
    }

    protected EntityCondition getHavingEntityCondition(Map<String, Object> context, ModelEntity modelEntity, GenericDelegator delegator) {
        // create havingEntityCondition from havingCondition
        if (this.havingCondition != null) {
            return this.havingCondition.createCondition(context, modelEntity.getEntityName(), delegator);
        }
        return null;
    }
}

