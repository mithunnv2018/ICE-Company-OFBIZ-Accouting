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
package org.ofbiz.manufacturing.techdata;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

/**
 * TechDataServices - TechData related Services
 *
 */
public class TechDataServices {

    public static final String module = TechDataServices.class.getName();

    /**
     *
     * Used to retreive some RoutingTasks (WorkEffort) selected by Name or MachineGroup ordered by Name
     *
     * @param ctx
     * @param context: a map containing workEffortName (routingTaskName) and fixedAssetId (MachineGroup or ANY)
     * @return result: a map containing lookupResult (list of RoutingTask <=> workEffortId with currentStatusId = "ROU_ACTIVE" and workEffortTypeId = "ROU_TASK"
     */
    public static Map lookupRoutingTask(DispatchContext ctx, Map context) {
        GenericDelegator delegator = ctx.getDelegator();
        Map result = new HashMap();

        String workEffortName = (String) context.get("workEffortName");
        String fixedAssetId = (String) context.get("fixedAssetId");

        List listRoutingTask = null;
        List constraints = new LinkedList();

        if (workEffortName != null && workEffortName.length()>0)
            constraints.add(EntityCondition.makeCondition("workEffortName", EntityOperator.GREATER_THAN_EQUAL_TO, workEffortName));
        if (fixedAssetId != null && fixedAssetId.length()>0 && ! "ANY".equals(fixedAssetId))
            constraints.add(EntityCondition.makeCondition("fixedAssetId", EntityOperator.EQUALS, fixedAssetId));

        constraints.add(EntityCondition.makeCondition("currentStatusId", EntityOperator.EQUALS, "ROU_ACTIVE"));
        constraints.add(EntityCondition.makeCondition("workEffortTypeId", EntityOperator.EQUALS, "ROU_TASK"));

        EntityConditionList<EntityExpr> ecl = EntityCondition.makeCondition(constraints, EntityOperator.AND);
        try {
            listRoutingTask = delegator.findList("WorkEffort", ecl, null, UtilMisc.toList("workEffortName"), null, false);
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
            return ServiceUtil.returnError("Error finding desired WorkEffort records: " + e.toString());
        }
        if (listRoutingTask == null) listRoutingTask = new LinkedList();
        if (listRoutingTask.size() == 0) listRoutingTask.add(UtilMisc.toMap("label","no Match","value","NO_MATCH"));
        result.put("lookupResult", listRoutingTask);
        return result;
    }
    /**
     *
     * Used to check if there is not two routing task with the same SeqId valid at the same period
     *
     * @param ctx            The DispatchContext that this service is operating in.
     * @param context    a map containing workEffortIdFrom (routing) and SeqId, fromDate thruDate
     * @return result      a map containing sequenceNumNotOk which is equal to "Y" if it's not Ok
     */
    public static Map checkRoutingTaskAssoc(DispatchContext ctx, Map context) {
        GenericDelegator delegator = ctx.getDelegator();
        Map result = new HashMap();
        String sequenceNumNotOk = "N";

        String workEffortIdFrom = (String) context.get("workEffortIdFrom");
        String workEffortIdTo = (String) context.get("workEffortIdTo");
        String workEffortAssocTypeId = (String) context.get("workEffortAssocTypeId");
        Long sequenceNum =  (Long) context.get("sequenceNum");
        Timestamp  fromDate =  (Timestamp) context.get("fromDate");
        Timestamp  thruDate =  (Timestamp) context.get("thruDate");
        String create = (String) context.get("create");

        boolean createProcess = (create !=null && create.equals("Y")) ? true : false;
        List listRoutingTaskAssoc = null;

        try {
            listRoutingTaskAssoc = delegator.findByAnd("WorkEffortAssoc",UtilMisc.toMap("workEffortIdFrom", workEffortIdFrom,"sequenceNum",sequenceNum), UtilMisc.toList("fromDate"));
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
            return ServiceUtil.returnError("Error finding desired WorkEffortAssoc records: " + e.toString());
        }

        if (listRoutingTaskAssoc != null) {
            Iterator  i = listRoutingTaskAssoc.iterator();
            while (i.hasNext()) {
                GenericValue routingTaskAssoc = (GenericValue) i.next();
                if ( ! workEffortIdFrom.equals(routingTaskAssoc.getString("workEffortIdFrom")) ||
                ! workEffortIdTo.equals(routingTaskAssoc.getString("workEffortIdTo")) ||
                ! workEffortAssocTypeId.equals(routingTaskAssoc.getString("workEffortAssocTypeId")) ||
                ! sequenceNum.equals(routingTaskAssoc.getLong("sequenceNum"))
                ) {
                    if (routingTaskAssoc.getTimestamp("thruDate") == null && routingTaskAssoc.getTimestamp("fromDate") == null) sequenceNumNotOk = "Y";
                    else if (routingTaskAssoc.getTimestamp("thruDate") == null) {
                        if (thruDate == null) sequenceNumNotOk = "Y";
                        else if (thruDate.after(routingTaskAssoc.getTimestamp("fromDate"))) sequenceNumNotOk = "Y";
                    }
                    else  if (routingTaskAssoc.getTimestamp("fromDate") == null) {
                        if (fromDate == null) sequenceNumNotOk = "Y";
                        else if (fromDate.before(routingTaskAssoc.getTimestamp("thruDate"))) sequenceNumNotOk = "Y";
                    }
                    else if ( fromDate == null && thruDate == null) sequenceNumNotOk = "Y";
                    else if (thruDate == null) {
                        if (fromDate.before(routingTaskAssoc.getTimestamp("thruDate"))) sequenceNumNotOk = "Y";
                    }
                    else if (fromDate == null) {
                        if (thruDate.after(routingTaskAssoc.getTimestamp("fromDate"))) sequenceNumNotOk = "Y";
                    }
                    else if ( routingTaskAssoc.getTimestamp("fromDate").before(thruDate) && fromDate.before(routingTaskAssoc.getTimestamp("thruDate")) ) sequenceNumNotOk = "Y";
                } else if (createProcess) sequenceNumNotOk = "Y";
            }
        }
        result.put("sequenceNumNotOk", sequenceNumNotOk);
        return result;
    }

    /**
     * Used to get the techDataCalendar for a routingTask, if there is a entity exception
     * or routingTask associated with no MachineGroup the DEFAULT TechDataCalendar is return.
     *
     * @param routingTask    the routingTask for which we are looking for
     * @return the techDataCalendar associated
     */
    public static GenericValue getTechDataCalendar(GenericValue routingTask) {
        GenericValue machineGroup = null, techDataCalendar = null;
        try {
            machineGroup = routingTask.getRelatedOneCache("FixedAsset");
        } catch (GenericEntityException e) {
            Debug.logError("Pb reading FixedAsset associated with routingTask"+e.getMessage(), module);
        }
        if (machineGroup != null) {
            if (machineGroup.getString("calendarId") != null) {
                try {
                    techDataCalendar = machineGroup.getRelatedOneCache("TechDataCalendar");
                } catch (GenericEntityException e) {
                    Debug.logError("Pb reading TechDataCalendar associated with machineGroup"+e.getMessage(), module);
                }
            } else {
                try {
                    List  machines = machineGroup.getRelatedCache("ChildFixedAsset");
                    if (machines != null && machines.size()>0) {
                        GenericValue machine = EntityUtil.getFirst(machines);
                        techDataCalendar = machine.getRelatedOneCache("TechDataCalendar");
                    }
                } catch (GenericEntityException e) {
                    Debug.logError("Pb reading machine child from machineGroup"+e.getMessage(), module);
                }
            }
        }
        if (techDataCalendar == null) {
            try {
                GenericDelegator delegator = routingTask.getDelegator();
                techDataCalendar = delegator.findByPrimaryKey("TechDataCalendar",UtilMisc.toMap("calendarId","DEFAULT"));
            } catch (GenericEntityException e) {
                Debug.logError("Pb reading TechDataCalendar DEFAULT"+e.getMessage(), module);
            }
        }
        return techDataCalendar;
    }

    /** Used to find the fisrt day in the TechDataCalendarWeek where capacity != 0, beginning at dayStart, dayStart included.
     *
     * @param techDataCalendarWeek        The TechDataCalendarWeek cover
     * @param dayStart
     * @return a map with the  capacity (Double) available and moveDay (int): the number of day it's necessary to move to have capacity available
     */
    public static Map dayStartCapacityAvailable(GenericValue techDataCalendarWeek,  int  dayStart) {
        Map result = new HashMap();
        int moveDay = 0;
        Double capacity = null;
        Time startTime = null;
        while (capacity == null || capacity.doubleValue()==0) {
            switch ( dayStart) {
                case Calendar.MONDAY:
                    capacity =  techDataCalendarWeek.getDouble("mondayCapacity");
                    startTime =  techDataCalendarWeek.getTime("mondayStartTime");
                    break;
                case Calendar.TUESDAY:
                    capacity =  techDataCalendarWeek.getDouble("tuesdayCapacity");
                    startTime =  techDataCalendarWeek.getTime("tuesdayStartTime");
                    break;
                case Calendar.WEDNESDAY:
                    capacity =  techDataCalendarWeek.getDouble("wednesdayCapacity");
                    startTime =  techDataCalendarWeek.getTime("wednesdayStartTime");
                    break;
                case Calendar.THURSDAY:
                    capacity =  techDataCalendarWeek.getDouble("thursdayCapacity");
                    startTime =  techDataCalendarWeek.getTime("thursdayStartTime");
                    break;
                case Calendar.FRIDAY:
                    capacity =  techDataCalendarWeek.getDouble("fridayCapacity");
                    startTime =  techDataCalendarWeek.getTime("fridayStartTime");
                    break;
                case Calendar.SATURDAY:
                    capacity =  techDataCalendarWeek.getDouble("saturdayCapacity");
                    startTime =  techDataCalendarWeek.getTime("saturdayStartTime");
                    break;
                case Calendar.SUNDAY:
                    capacity =  techDataCalendarWeek.getDouble("sundayCapacity");
                    startTime =  techDataCalendarWeek.getTime("sundayStartTime");
                    break;
            }
            if (capacity == null || capacity.doubleValue() == 0) {
                moveDay +=1;
                dayStart = (dayStart==7) ? 1 : dayStart +1;
            }
            //                Debug.logInfo("capacity loop: " + capacity+ " moveDay=" +moveDay, module);
        }
        result.put("capacity",capacity);
        result.put("startTime",startTime);
        result.put("moveDay",new Integer(moveDay));
        return result;
    }
    /** Used to to request the remain capacity available for dateFrom in a TechDataCalenda,
     * If the dateFrom (param in) is not  in an available TechDataCalendar period, the return value is zero.
     *
     * @param techDataCalendar        The TechDataCalendar cover
     * @param dateFrom                        the date
     * @return  long capacityRemaining
     */
    public static long capacityRemaining(GenericValue techDataCalendar,  Timestamp  dateFrom) {
        GenericValue techDataCalendarWeek = null;
        // TODO read TechDataCalendarExcWeek to manage execption week (maybe it's needed to refactor the entity definition
        try {
            techDataCalendarWeek = techDataCalendar.getRelatedOneCache("TechDataCalendarWeek");
        } catch (GenericEntityException e) {
            Debug.logError("Pb reading Calendar Week associated with calendar"+e.getMessage(), module);
            return 0;
        }
        // TODO read TechDataCalendarExcDay to manage execption day
        Calendar cDateTrav =  Calendar.getInstance();
        cDateTrav.setTime(dateFrom);
        Map position = dayStartCapacityAvailable(techDataCalendarWeek, cDateTrav.get(Calendar.DAY_OF_WEEK));
        int moveDay = ((Integer) position.get("moveDay")).intValue();
        if (moveDay != 0) return 0;
        Time startTime = (Time) position.get("startTime");
        Double capacity = (Double) position.get("capacity");
        Timestamp startAvailablePeriod = new Timestamp(UtilDateTime.getDayStart(dateFrom).getTime() + startTime.getTime() + cDateTrav.get(Calendar.ZONE_OFFSET) + cDateTrav.get(Calendar.DST_OFFSET));
        if (dateFrom.before(startAvailablePeriod) ) return 0;
        Timestamp endAvailablePeriod = new Timestamp(startAvailablePeriod.getTime()+capacity.longValue());
        if (dateFrom.after(endAvailablePeriod)) return 0;
        return  endAvailablePeriod.getTime() - dateFrom.getTime();
    }
    /** Used to move in a TechDataCalenda, produce the Timestamp for the begining of the next day available and its associated capacity.
     * If the dateFrom (param in) is not  in an available TechDataCalendar period, the return value is the next day available
     *
     * @param techDataCalendar        The TechDataCalendar cover
     * @param dateFrom                        the date
     * @return a map with Timestamp dateTo, Double nextCapacity
     */
    public static Map startNextDay(GenericValue techDataCalendar,  Timestamp  dateFrom) {
        Map result = new HashMap();
        Timestamp dateTo = null;
        GenericValue techDataCalendarWeek = null;
        // TODO read TechDataCalendarExcWeek to manage execption week (maybe it's needed to refactor the entity definition
        try {
            techDataCalendarWeek = techDataCalendar.getRelatedOneCache("TechDataCalendarWeek");
        } catch (GenericEntityException e) {
            Debug.logError("Pb reading Calendar Week associated with calendar"+e.getMessage(), module);
            return ServiceUtil.returnError("Pb reading Calendar Week associated with calendar");
        }
        // TODO read TechDataCalendarExcDay to manage execption day
        Calendar cDateTrav =  Calendar.getInstance();
        cDateTrav.setTime(dateFrom);
        Map position = dayStartCapacityAvailable(techDataCalendarWeek, cDateTrav.get(Calendar.DAY_OF_WEEK));
        Time startTime = (Time) position.get("startTime");
        int moveDay = ((Integer) position.get("moveDay")).intValue();
        dateTo = (moveDay == 0) ? dateFrom : UtilDateTime.getDayStart(dateFrom,moveDay);
        Timestamp startAvailablePeriod = new Timestamp(UtilDateTime.getDayStart(dateTo).getTime() + startTime.getTime() + cDateTrav.get(Calendar.ZONE_OFFSET) + cDateTrav.get(Calendar.DST_OFFSET));
        if (dateTo.before(startAvailablePeriod) ) {
            dateTo = startAvailablePeriod;
        }
        else {
            dateTo = UtilDateTime.getNextDayStart(dateTo);
            cDateTrav.setTime(dateTo);
            position = dayStartCapacityAvailable(techDataCalendarWeek, cDateTrav.get(Calendar.DAY_OF_WEEK));
            startTime = (Time) position.get("startTime");
            moveDay = ((Integer) position.get("moveDay")).intValue();
            if (moveDay != 0) dateTo = UtilDateTime.getDayStart(dateTo,moveDay);
            dateTo.setTime(dateTo.getTime() + startTime.getTime() + cDateTrav.get(Calendar.ZONE_OFFSET) + cDateTrav.get(Calendar.DST_OFFSET));
        }
        result.put("dateTo",dateTo);
        result.put("nextCapacity",position.get("capacity"));
        return result;
    }
    /** Used to move forward in a TechDataCalenda, start from the dateFrom and move forward only on available period.
     * If the dateFrom (param in) is not  a available TechDataCalendar period, the startDate is the begining of the next  day available
     *
     * @param techDataCalendar        The TechDataCalendar cover
     * @param dateFrom                        the start date
     * @param amount                           the amount of millisecond to move forward
     * @return the dateTo
     */
    public static Timestamp addForward(GenericValue techDataCalendar,  Timestamp  dateFrom, long amount) {
        Timestamp dateTo = (Timestamp) dateFrom.clone();
        long nextCapacity = capacityRemaining(techDataCalendar, dateFrom);
        if (amount <= nextCapacity) {
            dateTo.setTime(dateTo.getTime()+amount);
            amount = 0;
        } else amount -= nextCapacity;

        Map result = new HashMap();
        while (amount > 0)  {
            result = startNextDay(techDataCalendar, dateTo);
            dateTo = (Timestamp) result.get("dateTo");
            nextCapacity = ((Double) result.get("nextCapacity")).longValue();
            if (amount <= nextCapacity) {
                dateTo.setTime(dateTo.getTime()+amount);
                amount = 0;
            } else amount -= nextCapacity;
        }
        return dateTo;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /** Used to find the last day in the TechDataCalendarWeek where capacity != 0, ending at dayEnd, dayEnd included.
     *
     * @param techDataCalendarWeek        The TechDataCalendarWeek cover
     * @param dayEnd
     * @return a map with the  capacity (Double) available, the startTime and  moveDay (int): the number of day it's necessary to move to have capacity available
     */
    public static Map dayEndCapacityAvailable(GenericValue techDataCalendarWeek,  int  dayEnd) {
        Map result = new HashMap();
        int moveDay = 0;
        Double capacity = null;
        Time startTime = null;
        while (capacity == null || capacity.doubleValue() == 0) {
            switch ( dayEnd) {
                case Calendar.MONDAY:
                    capacity =  techDataCalendarWeek.getDouble("mondayCapacity");
                    startTime =  techDataCalendarWeek.getTime("mondayStartTime");
                    break;
                case Calendar.TUESDAY:
                    capacity =  techDataCalendarWeek.getDouble("tuesdayCapacity");
                    startTime =  techDataCalendarWeek.getTime("tuesdayStartTime");
                    break;
                case Calendar.WEDNESDAY:
                    capacity =  techDataCalendarWeek.getDouble("wednesdayCapacity");
                    startTime =  techDataCalendarWeek.getTime("wednesdayStartTime");
                    break;
                case Calendar.THURSDAY:
                    capacity =  techDataCalendarWeek.getDouble("thursdayCapacity");
                    startTime =  techDataCalendarWeek.getTime("thursdayStartTime");
                    break;
                case Calendar.FRIDAY:
                    capacity =  techDataCalendarWeek.getDouble("fridayCapacity");
                    startTime =  techDataCalendarWeek.getTime("fridayStartTime");
                    break;
                case Calendar.SATURDAY:
                    capacity =  techDataCalendarWeek.getDouble("saturdayCapacity");
                    startTime =  techDataCalendarWeek.getTime("saturdayStartTime");
                    break;
                case Calendar.SUNDAY:
                    capacity =  techDataCalendarWeek.getDouble("sundayCapacity");
                    startTime =  techDataCalendarWeek.getTime("sundayStartTime");
                    break;
            }
            if (capacity == null || capacity.doubleValue() == 0) {
                moveDay -=1;
                dayEnd = (dayEnd==1) ? 7 : dayEnd - 1;
            }
        }
        result.put("capacity",capacity);
        result.put("startTime",startTime);
        result.put("moveDay",new Integer(moveDay));
        return result;
    }
    /** Used to request the remaining capacity available for dateFrom in a TechDataCalenda,
     * If the dateFrom (param in) is not  in an available TechDataCalendar period, the return value is zero.
     *
     * @param techDataCalendar        The TechDataCalendar cover
     * @param dateFrom                        the date
     * @return  long capacityRemaining
     */
    public static long capacityRemainingBackward(GenericValue techDataCalendar,  Timestamp  dateFrom) {
        GenericValue techDataCalendarWeek = null;
        // TODO read TechDataCalendarExcWeek to manage exception week (maybe it's needed to refactor the entity definition
        try {
            techDataCalendarWeek = techDataCalendar.getRelatedOneCache("TechDataCalendarWeek");
        } catch (GenericEntityException e) {
            Debug.logError("Pb reading Calendar Week associated with calendar"+e.getMessage(), module);
            return 0;
        }
        // TODO read TechDataCalendarExcDay to manage execption day
        Calendar cDateTrav =  Calendar.getInstance();
        cDateTrav.setTime(dateFrom);
        Map position = dayEndCapacityAvailable(techDataCalendarWeek, cDateTrav.get(Calendar.DAY_OF_WEEK));
        int moveDay = ((Integer) position.get("moveDay")).intValue();
        if (moveDay != 0) return 0;
        Time startTime = (Time) position.get("startTime");
        Double capacity = (Double) position.get("capacity");
        Timestamp startAvailablePeriod = new Timestamp(UtilDateTime.getDayStart(dateFrom).getTime() + startTime.getTime() + cDateTrav.get(Calendar.ZONE_OFFSET) + cDateTrav.get(Calendar.DST_OFFSET));
        if (dateFrom.before(startAvailablePeriod) ) return 0;
        Timestamp endAvailablePeriod = new Timestamp(startAvailablePeriod.getTime()+capacity.longValue());
        if (dateFrom.after(endAvailablePeriod)) return 0;
        return  dateFrom.getTime() - startAvailablePeriod.getTime();
    }
    /** Used to move in a TechDataCalenda, produce the Timestamp for the end of the previous day available and its associated capacity.
     * If the dateFrom (param in) is not  in an available TechDataCalendar period, the return value is the previous day available
     *
     * @param techDataCalendar        The TechDataCalendar cover
     * @param dateFrom                        the date
     * @return a map with Timestamp dateTo, Double previousCapacity
     */
    public static Map endPreviousDay(GenericValue techDataCalendar,  Timestamp  dateFrom) {
        Map result = new HashMap();
        Timestamp dateTo = null;
        GenericValue techDataCalendarWeek = null;
        // TODO read TechDataCalendarExcWeek to manage exception week (maybe it's needed to refactor the entity definition
        try {
            techDataCalendarWeek = techDataCalendar.getRelatedOneCache("TechDataCalendarWeek");
        } catch (GenericEntityException e) {
            Debug.logError("Pb reading Calendar Week associated with calendar"+e.getMessage(), module);
            return ServiceUtil.returnError("Pb reading Calendar Week associated with calendar");
        }
        // TODO read TechDataCalendarExcDay to manage execption day
        Calendar cDateTrav =  Calendar.getInstance();
        cDateTrav.setTime(dateFrom);
        Map position = dayEndCapacityAvailable(techDataCalendarWeek, cDateTrav.get(Calendar.DAY_OF_WEEK));
        Time startTime = (Time) position.get("startTime");
        int moveDay = ((Integer) position.get("moveDay")).intValue();
        Double capacity = (Double) position.get("capacity");
        dateTo = (moveDay == 0) ? dateFrom : UtilDateTime.getDayEnd(dateFrom, new Long(moveDay));
        Timestamp endAvailablePeriod = new Timestamp(UtilDateTime.getDayStart(dateTo).getTime() + startTime.getTime() + capacity.longValue() + cDateTrav.get(Calendar.ZONE_OFFSET) + cDateTrav.get(Calendar.DST_OFFSET));
        if (dateTo.after(endAvailablePeriod) ) {
            dateTo = endAvailablePeriod;
        }
        else {
            dateTo = UtilDateTime.getDayStart(dateTo, -1);
            cDateTrav.setTime(dateTo);
            position = dayEndCapacityAvailable(techDataCalendarWeek, cDateTrav.get(Calendar.DAY_OF_WEEK));
            startTime = (Time) position.get("startTime");
            moveDay = ((Integer) position.get("moveDay")).intValue();
            capacity = (Double) position.get("capacity");
            if (moveDay != 0) dateTo = UtilDateTime.getDayStart(dateTo,moveDay);
            dateTo.setTime(dateTo.getTime() + startTime.getTime() + capacity.longValue() + cDateTrav.get(Calendar.ZONE_OFFSET) + cDateTrav.get(Calendar.DST_OFFSET));
        }
        result.put("dateTo",dateTo);
        result.put("previousCapacity",position.get("capacity"));
        return result;
    }
    /** Used to move backward in a TechDataCalendar, start from the dateFrom and move backward only on available period.
     * If the dateFrom (param in) is not  a available TechDataCalendar period, the startDate is the end of the previous day available
     *
     * @param techDataCalendar        The TechDataCalendar cover
     * @param dateFrom                        the start date
     * @param amount                           the amount of millisecond to move backward
     * @return the dateTo
     */
    public static Timestamp addBackward(GenericValue techDataCalendar,  Timestamp  dateFrom, long amount) {
        Timestamp dateTo = (Timestamp) dateFrom.clone();
        long previousCapacity = capacityRemainingBackward(techDataCalendar, dateFrom);
        if (amount <= previousCapacity) {
            dateTo.setTime(dateTo.getTime()-amount);
            amount = 0;
        } else amount -= previousCapacity;

        Map result = new HashMap();
        while (amount > 0)  {
            result = endPreviousDay(techDataCalendar, dateTo);
            dateTo = (Timestamp) result.get("dateTo");
            previousCapacity = ((Double) result.get("previousCapacity")).longValue();
            if (amount <= previousCapacity) {
                dateTo.setTime(dateTo.getTime()-amount);
                amount = 0;
            } else amount -= previousCapacity;
        }
        return dateTo;
    }
}
