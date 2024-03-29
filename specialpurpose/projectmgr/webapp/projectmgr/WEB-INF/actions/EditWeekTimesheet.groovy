/*
/*
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
 */

import java.util.*;
import java.lang.*;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.*;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.*;
import org.ofbiz.entity.condition.*;
import java.sql.Timestamp;

partyId = parameters.partyId;
if (!partyId) {
    partyId = parameters.userLogin.partyId;
}

// show the requested timesheet, otherwise the current , if not exist create
timesheet = null;
timesheetId = parameters.timesheetId;
if (timesheetId) {
    timesheet = delegator.findByPrimaryKey("Timesheet", ["timesheetId" : timesheetId]);
    partyId = timesheet.partyId; // use the party from this timesheet
} else {
    start = UtilDateTime.getWeekStart(UtilDateTime.nowTimestamp());
    timesheets = delegator.findByAnd("Timesheet", ["partyId" : partyId, "fromDate" : start]);
    if (timesheets) {
        timesheet = timesheets[0];
    } else {
    	result = dispatcher.runSync("createProjectTimesheet", ["userLogin" : parameters.userLogin, "partyId" : partyId]);
        if (result && result.timesheetId) {
            timesheet = delegator.findByPrimaryKey("Timesheet", ["timesheetId" : result.timesheetId]);
        }
    }
}
if (!timesheet) return;
context.timesheet = timesheet;
context.weekNumber = UtilDateTime.weekNumber(timesheet.fromDate);

// get the user names
context.partyNameView = delegator.findByPrimaryKey("PartyNameView",["partyId" : partyId]);
// get the default rate for this person
rateTypes = EntityUtil.filterByDate(delegator.findByAnd("PartyRate", ["partyId" : partyId, "defaultRate" : "Y"]));
if (rateTypes) {
    context.defaultRateTypeId = rateTypes[0].rateTypeId;
}

entries = [];
entry = ["timesheetId" : timesheet.timesheetId];
taskTotal = 0.00;
day0Total = 0.00; day1Total=0.00; day2Total=0.00; day3Total=0.00; day4Total=0.00; day5Total=0.00; day6Total=0.00;
pHours = 0.00;
timeEntry = null;
lastTimeEntry = null;

// retrieve work effort data when the workeffortId has changed.
void retrieveWorkEffortData() {
        // get the planned number of hours
        entryWorkEffort = lastTimeEntry.getRelatedOne("WorkEffort");
        if (entryWorkEffort) {
            plannedHours = entryWorkEffort.getRelated("WorkEffortSkillStandard");
            pHours = 0.00;
            plannedHours.each { plannedHour ->
                if (plannedHour.estimatedDuration) {
                    pHours += plannedHour.estimatedDuration;
                }
            }
            entry.plannedHours = pHours;
            actualHours = entryWorkEffort.getRelated("TimeEntry");
            aHours = 0.00;
            actualHours.each { actualHour ->
                if (actualHour.hours) {
                    aHours += actualHour.hours;
                }
            }
            entry.actualHours = aHours;
            // get party assignment data to be able to set the task to complete
            workEffortPartyAssigns = EntityUtil.filterByDate(entryWorkEffort.getRelatedByAnd("WorkEffortPartyAssignment", ["partyId" : partyId]));
            if (workEffortPartyAssigns) {
                workEffortPartyAssign = workEffortPartyAssigns[0];
                entry.fromDate = workEffortPartyAssign.getTimestamp("fromDate");
                entry.roleTypeId = workEffortPartyAssign.roleTypeId;
                if ("PAS_COMPLETED".equals(workEffortPartyAssign.statusId)) {
                    entry.checkComplete = "Y";
                }
            }

            // get project/phase information
            entry.workEffortId = entryWorkEffort.workEffortId;
            entry.workEffortName = entryWorkEffort.workEffortName;
            result = dispatcher.runSync("getProjectIdAndNameFromTask", ["userLogin" : parameters.userLogin,"taskId" : entryWorkEffort.workEffortId]);
                entry.phaseId = result.phaseId;
                entry.phaseName = result.phaseName;
                entry.projectId = result.projectId;
                entry.projectName = result.projectName;

        }
        entry.total = taskTotal;
        //Drop Down Lists
        entries.add(entry);
        // start new entry
        taskTotal = 0.00;
        entry = ["timesheetId" : timesheet.timesheetId];
}

timeEntries = timesheet.getRelated("TimeEntry", ["workEffortId", "rateTypeId", "fromDate"]);
te = timeEntries.iterator();
while (te.hasNext()) {
    // only fill lastTimeEntry when not the first time
    if (timeEntry!=void) {
        lastTimeEntry = timeEntry;
    }
    timeEntry = te.next();

    if (lastTimeEntry &&
            (!lastTimeEntry.workEffortId.equals(timeEntry.workEffortId) ||
            !lastTimeEntry.rateTypeId.equals(timeEntry.rateTypeId))) {
            retrieveWorkEffortData();
        }
    if (timeEntry.hours) {
        dayNumber = "d" + (timeEntry.fromDate.getTime() - timesheet.fromDate.getTime()) / (24*60*60*1000);
        hours = timeEntry.hours.doubleValue();
        entry.put(String.valueOf(dayNumber), hours);
        if (dayNumber.equals("d0")) day0Total += hours;
        if (dayNumber.equals("d1")) day1Total += hours;
        if (dayNumber.equals("d2")) day2Total += hours;
        if (dayNumber.equals("d3")) day3Total += hours;
        if (dayNumber.equals("d4")) day4Total += hours;
        if (dayNumber.equals("d5")) day5Total += hours;
        if (dayNumber.equals("d6")) day6Total += hours;
        taskTotal += hours;
    }
    entry.rateTypeId = timeEntry.rateTypeId;
}

if (timeEntry) {
    lastTimeEntry = timeEntry;
    retrieveWorkEffortData();
    }

// add empty lines if timesheet not completed
if (!timesheet.statusId.equals("TIMESHEET_COMPLETED")) {
    for (c=0; c < 3; c++) { // add empty lines
        entries.add(["timesheetId" : timesheet.timesheetId]);
    }
}

// add the totals line if at least one entry
if (timeEntry) {
    entry = ["timesheetId" : timesheet.timesheetId];
    entry.d0 = day0Total;
    entry.d1 = day1Total;
    entry.d2 = day2Total;
    entry.d3 = day3Total;
    entry.d4 = day4Total;
    entry.d5 = day5Total;
    entry.d6 = day6Total;
    entry.phaseName = "Totals";
    entry.workEffortId = "Totals";
    entry.total = day0Total + day1Total + day2Total + day3Total + day4Total + day5Total + day6Total;
    entries.add(entry);
}
context.timeEntries = entries;
// get all timesheets of this user, including the planned hours
timesheetsDb = delegator.findByAnd("Timesheet", ["partyId" : partyId], ["fromDate DESC"]);
timesheets = new LinkedList();
timesheetsDb.each { timesheetDb ->
    timesheet = [:];
    timesheet.putAll(timesheetDb);
    entries = timesheetDb.getRelated("TimeEntry");
    hours = 0.00;
    entries.each { timeEntry ->
        if (timeEntry.hours) {
            hours += timeEntry.hours.doubleValue();
        }
    }
    timesheet.weekNumber = UtilDateTime.weekNumber(timesheetDb.fromDate);
    timesheet.hours = hours;
    timesheets.add(timesheet);
}
context.timesheets = timesheets;
