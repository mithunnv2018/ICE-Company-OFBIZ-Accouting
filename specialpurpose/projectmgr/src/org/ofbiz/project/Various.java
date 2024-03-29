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
package org.ofbiz.project;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.lang.*;
import java.util.TimeZone;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.*;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntity;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;

public class Various {

    public static final String module = Various.class.getName();


    public static void setDatesFollowingTasks(GenericValue task) {

        try {
            List assocs = task.getRelated("FromWorkEffortAssoc");
            if (UtilValidate.isNotEmpty(assocs)) {
                Iterator a = assocs.iterator();
                while (a.hasNext()) {
                    GenericValue assoc = (GenericValue) a.next();
                    GenericValue nextTask = assoc.getRelatedOne("ToWorkEffort");
                      Timestamp newStartDate = task.getTimestamp("estimatedCompletionDate"); // start of next task the next day
                      if (nextTask.get("estimatedStartDate") == null || nextTask.getTimestamp("estimatedStartDate").before(newStartDate) ) {
                            nextTask.put("estimatedStartDate", UtilDateTime.addDaysToTimestamp(task.getTimestamp("estimatedCompletionDate"), 1)); // start of next task the next day
                             nextTask.put("estimatedCompletionDate", calculateCompletionDate(nextTask, task.getTimestamp("estimatedCompletionDate")));
                             nextTask.store();
                      }
                    setDatesFollowingTasks(nextTask);
                }
            }

        } catch (GenericEntityException e) {
            Debug.logError("Could not updte task: " + e.getMessage(), module);
        }
    }

    public static Timestamp calculateCompletionDate(GenericValue task, Timestamp startDate) {
        Double plannedHours = 0.00;
        try {
            // get planned hours
            List standards = task.getRelated("WorkEffortSkillStandard");
            Iterator t = standards.iterator();
            while (t.hasNext()) {
                GenericValue standard = (GenericValue) t.next();
                if (standard.getDouble("estimatedNumPeople") == null) {
                    standard.put("estimatedNumPeople", new Double("1"));
                }
                if (standard.get("estimatedDuration") != null) {
                    plannedHours += standard.getDouble("estimatedDuration").doubleValue() / standard.getDouble("estimatedNumPeople").doubleValue();
                }
            }

        } catch (GenericEntityException e) {
            Debug.logError("Could not updte task: " + e.getMessage(), module);
        }
        if (plannedHours == 0.00) {
            plannedHours = new Double("24.00"); // default length of task is 3 days.
        }

        // only add days which are not saturday(7) or sunday(1)
        int days = plannedHours.intValue() / 8;
        while (days > 0) {
            int dayNumber = UtilDateTime.dayNumber(startDate);
            if (dayNumber != 1 && dayNumber != 7) {
                days--;
            }
            startDate = UtilDateTime.addDaysToTimestamp(startDate, 1);
        }
        return startDate;
    }

    public static double calculateActualHours(GenericDelegator delegator, String timesheetId) {
        List actuals = FastList.newInstance();
        double actualHours = 0.00;
        if (timesheetId != null) {
            try {
                actuals = delegator.findByAnd("TimeEntry", UtilMisc.toMap("timesheetId", timesheetId));
                if (actuals.size() > 0) {
                    Iterator ite = actuals.iterator();
                    while (ite.hasNext()) {
                        GenericValue actual =(GenericValue)ite.next();
                         Double hour = (Double) actual.get("hours");
                         double hours = hour.doubleValue();
                         actualHours = actualHours + hours;
                    }
                }
            } catch (GenericEntityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return actualHours;
    }
}
