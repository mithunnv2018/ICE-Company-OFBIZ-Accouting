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
package org.ofbiz.service.test;

import java.util.List;
import java.util.Map;

import javolution.util.FastList;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericResultWaiter;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

public class ServiceEngineTestServices {

    public static final String module = ServiceEngineTestServices.class.getName();

    public static Map<String, Object> testServiceDeadLockRetry(DispatchContext dctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        try {
            // NOTE using persist=false so that the lock retry will have to fix the problem instead of the job poller picking it up again
            GenericResultWaiter threadAWaiter = dispatcher.runAsyncWait("testServiceDeadLockRetryThreadA", null, false);
            GenericResultWaiter threadBWaiter = dispatcher.runAsyncWait("testServiceDeadLockRetryThreadB", null, false);
            // make sure to wait for these to both finish to make sure results aren't checked until they are done
            Map<String, Object> threadAResult = threadAWaiter.waitForResult();
            Map<String, Object> threadBResult = threadBWaiter.waitForResult();
            List<Object> errorList = FastList.newInstance();
            if (ServiceUtil.isError(threadAResult)) {
                errorList.add("Error running testServiceDeadLockRetryThreadA: " + ServiceUtil.getErrorMessage(threadAResult));
            }
            if (ServiceUtil.isError(threadBResult)) {
                errorList.add("Error running testServiceDeadLockRetryThreadB: " + ServiceUtil.getErrorMessage(threadBResult));
            }
            if (errorList.size() > 0) {
                return ServiceUtil.returnError("Error(s) running sub-services in testServiceDeadLockRetry", errorList, null, null);
            }
        } catch (Exception e) {
            String errMsg = "Error running deadlock test services: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }

        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> testServiceDeadLockRetryThreadA(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();

        try {
            // grab entity SVCLRT_A by changing, then wait, then find and change SVCLRT_B
            GenericValue testingTypeA = delegator.findOne("TestingType", false, "testingTypeId", "SVCLRT_A");
            testingTypeA.set("description", "New description for SVCLRT_A");
            testingTypeA.store();

            // wait at least long enough for the other method to have locked resource B
            Debug.logInfo("In testServiceDeadLockRetryThreadA just updated SVCLRT_A, beginning wait", module);
            UtilMisc.staticWait(100);

            Debug.logInfo("In testServiceDeadLockRetryThreadA done with wait, updating SVCLRT_B", module);
            GenericValue testingTypeB = delegator.findOne("TestingType", false, "testingTypeId", "SVCLRT_B");
            testingTypeB.set("description", "New description for SVCLRT_B");
            testingTypeB.store();

            Debug.logInfo("In testServiceDeadLockRetryThreadA done with updating SVCLRT_B, updating SVCLRT_AONLY", module);
            GenericValue testingTypeAOnly = delegator.findOne("TestingType", false, "testingTypeId", "SVCLRT_AONLY");
            testingTypeAOnly.set("description", "New description for SVCLRT_AONLY; this is only changed by thread A so if it doesn't match something happened to thread A!");
            testingTypeAOnly.store();
        } catch (GenericEntityException e) {
            String errMsg = "Entity Engine Exception running dead lock test thread A: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        } catch (InterruptedException e) {
            String errMsg = "Wait Interrupted Exception running dead lock test thread A: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }

        return ServiceUtil.returnSuccess();
    }
    public static Map<String, Object> testServiceDeadLockRetryThreadB(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();

        try {
            // grab entity SVCLRT_B by changing, then wait, then change SVCLRT_A
            GenericValue testingTypeB = delegator.findOne("TestingType", false, "testingTypeId", "SVCLRT_B");
            testingTypeB.set("description", "New description for SVCLRT_B");
            testingTypeB.store();

            // wait at least long enough for the other method to have locked resource B
            Debug.logInfo("In testServiceDeadLockRetryThreadB just updated SVCLRT_B, beginning wait", module);
            UtilMisc.staticWait(100);

            Debug.logInfo("In testServiceDeadLockRetryThreadB done with wait, updating SVCLRT_A", module);
            GenericValue testingTypeA = delegator.findOne("TestingType", false, "testingTypeId", "SVCLRT_A");
            testingTypeA.set("description", "New description for SVCLRT_A");
            testingTypeA.store();

            Debug.logInfo("In testServiceDeadLockRetryThreadA done with updating SVCLRT_A, updating SVCLRT_BONLY", module);
            GenericValue testingTypeAOnly = delegator.findOne("TestingType", false, "testingTypeId", "SVCLRT_BONLY");
            testingTypeAOnly.set("description", "New description for SVCLRT_BONLY; this is only changed by thread B so if it doesn't match something happened to thread B!");
            testingTypeAOnly.store();
        } catch (GenericEntityException e) {
            String errMsg = "Entity Engine Exception running dead lock test thread B: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        } catch (InterruptedException e) {
            String errMsg = "Wait Interrupted Exception running dead lock test thread B: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }

        return ServiceUtil.returnSuccess();
    }

    // ==================================================

    public static Map<String, Object> testServiceLockWaitTimeoutRetry(DispatchContext dctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        try {
            // NOTE using persist=false so that the lock retry will have to fix the problem instead of the job poller picking it up again
            GenericResultWaiter grabberWaiter = dispatcher.runAsyncWait("testServiceLockWaitTimeoutRetryGrabber", null, false);
            GenericResultWaiter waiterWaiter = dispatcher.runAsyncWait("testServiceLockWaitTimeoutRetryWaiter", null, false);
            // make sure to wait for these to both finish to make sure results aren't checked until they are done
            Map<String, Object> grabberResult = grabberWaiter.waitForResult();
            Map<String, Object> waiterResult = waiterWaiter.waitForResult();
            List<Object> errorList = FastList.newInstance();
            if (ServiceUtil.isError(grabberResult)) {
                errorList.add("Error running testServiceLockWaitTimeoutRetryGrabber: " + ServiceUtil.getErrorMessage(grabberResult));
            }
            if (ServiceUtil.isError(waiterResult)) {
                errorList.add("Error running testServiceLockWaitTimeoutRetryWaiter: " + ServiceUtil.getErrorMessage(waiterResult));
            }
            if (errorList.size() > 0) {
                return ServiceUtil.returnError("Error(s) running sub-services in testServiceLockWaitTimeoutRetry", errorList, null, null);
            }
        } catch (Exception e) {
            String errMsg = "Error running deadlock test services: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }

        return ServiceUtil.returnSuccess();
    }
    public static Map<String, Object> testServiceLockWaitTimeoutRetryGrabber(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();

        try {
            // grab entity SVCLWTRT by changing, then wait a LONG time, ie more than the wait timeout
            GenericValue testingType = delegator.findOne("TestingType", false, "testingTypeId", "SVCLWTRT");
            testingType.set("description", "New description for SVCLWTRT from the GRABBER service, this should be replaced by Waiter service in the service engine auto-retry");
            testingType.store();

            Debug.logInfo("In testServiceLockWaitTimeoutRetryGrabber just updated SVCLWTRT, beginning wait", module);

            // wait at least long enough for the other method to have locked resource wait time out
            // (tx timeout 6s on this the Grabber and 2s on the Waiter): wait 4 seconds because timeout on this
            UtilMisc.staticWait(4 * 1000);
        } catch (GenericEntityException e) {
            String errMsg = "Entity Engine Exception running lock wait timeout test Grabber thread: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        } catch (InterruptedException e) {
            String errMsg = "Wait Interrupted Exception running lock wait timeout test Grabber thread: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }

        return ServiceUtil.returnSuccess();
    }
    public static Map<String, Object> testServiceLockWaitTimeoutRetryWaiter(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();

        try {
            // wait for a small amount of time to make sure the grabber does it's thing first
            UtilMisc.staticWait(100);

            Debug.logInfo("In testServiceLockWaitTimeoutRetryWaiter about to update SVCLWTRT, wait starts here", module);

            // TRY grab entity SVCLWTRT by looking up and changing, should get a lock wait timeout exception because of the Grabber thread
            GenericValue testingType = delegator.findOne("TestingType", false, "testingTypeId", "SVCLWTRT");
            testingType.set("description", "New description for SVCLWTRT from Waiter service, this is the value that should be there.");
            testingType.store();

            Debug.logInfo("In testServiceLockWaitTimeoutRetryWaiter successfully updated SVCLWTRT", module);
        } catch (GenericEntityException e) {
            String errMsg = "Entity Engine Exception running lock wait timeout test Waiter thread: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        } catch (InterruptedException e) {
            String errMsg = "Wait Interrupted Exception running lock wait timeout test Waiter thread: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }

        return ServiceUtil.returnSuccess();
    }

    // ==================================================

    /**
     * NOTE that this is a funny case where the auto-retry in the service engine for the call to
     * testServiceLockWaitTimeoutRetryCantRecoverWaiter would NOT be able to recover because it would try again
     * given the new transaction and all, but the lock for the waiting thread would still be there... so it will fail
     * repeatedly.
     *
     * TODO: there's got to be some way to do this, but how?!? :(
     *
     * NOTE: maybe this will work: create a list that the service engine maintains of services it will run after the
     * current service run is complete, and AFTER it has committed or rolled back its transaction; if a service finds
     * it has a lock wait timeout, add itself to the list for its parent service (somehow...) and off we go!
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> testServiceLockWaitTimeoutRetryCantRecover(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        try {
            // grab entity SVCLWTRTCR by changing, then wait a LONG time, ie more than the wait timeout
            GenericValue testingType = delegator.findOne("TestingType", false, "testingTypeId", "SVCLWTRTCR");
            testingType.set("description", "New description for SVCLWTRTCR from Lock Wait Timeout Lock GRABBER, this should be replaced by the one in the Waiter service.");
            testingType.store();

            Debug.logInfo("In testServiceLockWaitTimeoutRetryCantRecover (grabber) just updated SVCLWTRTCR, running sub-service in own transaction", module);
            // timeout is 5 seconds so it is longer than the tx timeout for this service, so will fail quickly; with this transaction keeping a lock on the record and that one trying to get it, bam we cause the error
            Map<String, Object> waiterResult = dispatcher.runSync("testServiceLockWaitTimeoutRetryCantRecoverWaiter", null, 5, true);
            if (ServiceUtil.isError(waiterResult)) {
                return ServiceUtil.returnError("Error running testServiceLockWaitTimeoutRetryCantRecoverWaiter", null, null, waiterResult);
            }

            Debug.logInfo("In testServiceLockWaitTimeoutRetryCantRecover (grabber) successfully finished running sub-service in own transaction", module);
        } catch (GenericServiceException e) {
            String errMsg = "Error running deadlock test services: " + e.toString();
            Debug.logError(e, errMsg, module);
        } catch (GenericEntityException e) {
            String errMsg = "Entity Engine Exception running lock wait timeout test main/Grabber thread: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }

        return ServiceUtil.returnSuccess();
    }
    public static Map<String, Object> testServiceLockWaitTimeoutRetryCantRecoverWaiter(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();

        try {
            Debug.logInfo("In testServiceLockWaitTimeoutRetryCantRecoverWaiter updating SVCLWTRTCR", module);

            // TRY grab entity SVCLWTRTCR by looking up and changing, should get a lock wait timeout exception because of the Grabber thread
            GenericValue testingType = delegator.findOne("TestingType", false, "testingTypeId", "SVCLWTRTCR");
            testingType.set("description", "New description for SVCLWTRTCR from Lock Wait Timeout Lock Waiter, this is the value that should be there.");
            testingType.store();

            Debug.logInfo("In testServiceLockWaitTimeoutRetryCantRecoverWaiter successfully updated SVCLWTRTCR", module);
        } catch (GenericEntityException e) {
            String errMsg = "Entity Engine Exception running lock wait timeout test Waiter thread: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }

        return ServiceUtil.returnSuccess();
    }

    // ==================================================

    public static Map<String, Object> testServiceOwnTxSubServiceAfterSetRollbackOnlyInParentErrorCatchWrapper(DispatchContext dctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        try {
            Map<String, Object> resultMap = dispatcher.runSync("testServiceOwnTxSubServiceAfterSetRollbackOnlyInParent", null, 60, true);
            if (ServiceUtil.isError(resultMap)) {
                return ServiceUtil.returnError("Error running main test service in testServiceOwnTxSubServiceAfterSetRollbackOnlyInParentErrorCatchWrapper", null, null, resultMap);
            }
        } catch (GenericServiceException e) {
            String errMsg = "This is the expected error running sub-service with own tx after the parent has set rollback only, logging and ignoring: " + e.toString();
            Debug.logError(e, errMsg, module);
        }

        return ServiceUtil.returnSuccess();
    }
    public static Map<String, Object> testServiceOwnTxSubServiceAfterSetRollbackOnlyInParent(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        try {
            // change the SVC_SRBO value first to test that the rollback really does revert/reset
            GenericValue testingType = delegator.findOne("TestingType", false, "testingTypeId", "SVC_SRBO");
            testingType.set("description", "New description for SVC_SRBO; this should be reset on the rollback, if this is in the db then the test failed");
            testingType.store();

            TransactionUtil.setRollbackOnly("Intentionally setting rollback only for testing purposes", null);

            Map<String, Object> resultMap = dispatcher.runSync("testServiceOwnTxSubServiceAfterSetRollbackOnlyInParentSubService", null, 60, true);
            if (ServiceUtil.isError(resultMap)) {
                return ServiceUtil.returnError("Error running sub-service in testServiceOwnTxSubServiceAfterSetRollbackOnlyInParent", null, null, resultMap);
            }
        } catch (Exception e) {
            String errMsg = "Error running sub-service with own tx: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }

        return ServiceUtil.returnSuccess();
    }
    public static Map<String, Object> testServiceOwnTxSubServiceAfterSetRollbackOnlyInParentSubService(DispatchContext dctx, Map<String, ? extends Object> context) {
        // this service doesn't actually have to do anything, the problem was in just pausing and resuming the transaciton with setRollbackOnly
        return ServiceUtil.returnSuccess();
    }


    // ==================================================

    public static Map<String, Object> testServiceEcaGlobalEventExec(DispatchContext dctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        try {
            // this will return an error, but we'll ignore the result
            dispatcher.runSync("testServiceEcaGlobalEventExecToRollback", null, 60, true);
        } catch (GenericServiceException e) {
            String errMsg = "Error calling sub-service, it should return an error but not throw an exception, so something went wrong: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }

        // this service doesn't actually have to do anything, just a placeholder for ECA rules, this one should commit
        return ServiceUtil.returnSuccess();
    }
    public static Map<String, Object> testServiceEcaGlobalEventExecOnCommit(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();

        try {
            GenericValue testingType = delegator.findOne("TestingType", false, "testingTypeId", "SVC_SECAGC");
            testingType.set("description", "New description for SVC_SECAGC, what it should be after the global-commit test");
            testingType.store();
        } catch (GenericEntityException e) {
            String errMsg = "Entity Engine Exception: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }

        return ServiceUtil.returnSuccess();
    }
    public static Map<String, Object> testServiceEcaGlobalEventExecToRollback(DispatchContext dctx, Map<String, ? extends Object> context) {
        // this service doesn't actually have to do anything, just a placeholder for ECA rules, this one should rollback
        return ServiceUtil.returnError("Intentional rollback to test global-rollback");
    }
    public static Map<String, Object> testServiceEcaGlobalEventExecOnRollback(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();

        try {
            GenericValue testingType = delegator.findOne("TestingType", false, "testingTypeId", "SVC_SECAGR");
            testingType.set("description", "New description for SVC_SECAGR, what it should be after the global-rollback test");
            testingType.store();
        } catch (GenericEntityException e) {
            String errMsg = "Entity Engine Exception: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }

        return ServiceUtil.returnSuccess();
    }
}
