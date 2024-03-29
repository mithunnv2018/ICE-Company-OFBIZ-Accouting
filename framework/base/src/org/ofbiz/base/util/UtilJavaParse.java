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
package org.ofbiz.base.util;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import javolution.util.FastSet;

import org.ofbiz.base.component.ComponentConfig;


/**
 * Java Source Parsing Utilities
 *
 * NOTE: the approach here is not the best and it may be better to use a parser, line one based on antlr, or using a Java Bytecode parser to look at .class files.
 *
 */
public class UtilJavaParse {

    public static final String module = UtilJavaParse.class.getName();

    public static String findRealPathAndFileForClass(String fullyQualifiedClassName) {
        // search through the component directories, in the src directory for each, using the class path as the path within it

        String sourceSubPath = fullyQualifiedClassName.substring(0, fullyQualifiedClassName.lastIndexOf(".")).replace('.', File.separatorChar);
        String classFileName = fullyQualifiedClassName.substring(fullyQualifiedClassName.lastIndexOf(".")+1) + ".java";

        Collection<ComponentConfig> allComponentConfigs = ComponentConfig.getAllComponents();
        for (ComponentConfig cc: allComponentConfigs) {
            String rootDirectory = cc.getRootLocation();
            if (!rootDirectory.endsWith(File.separatorChar + "")) rootDirectory += File.separatorChar;
            rootDirectory += "src" + File.separatorChar;

            File rootDirFile = new File(rootDirectory);
            if (!rootDirFile.exists()) {
                // no src directory, move along
                continue;
            }

            String classDir = rootDirectory + sourceSubPath;
            File classDirFile = new File(classDir);
            if (!classDirFile.exists()) {
                // no src class sub-directory, move along
                continue;
            }

            String fullPathAndFile = classDir + File.separatorChar + classFileName;
            File classFile = new File(fullPathAndFile);
            if (classFile.exists()) {
                if (Debug.verboseOn()) Debug.logVerbose("In findRealPathAndFileForClass for [" + fullyQualifiedClassName + "]: [" + fullPathAndFile + "]", module);
                return fullPathAndFile;
            }
        }

        return null;
    }

    public static int findServiceMethodBlockStart(String methodName, String javaFile) {
        if (Debug.verboseOn()) Debug.logVerbose("In findServiceMethodBlockStart for " + methodName, module);

        // starts with something like this: public static Map exportServiceEoModelBundle(DispatchContext dctx, Map context) {

        // start with the main pattern
        int methodNameIndex = javaFile.indexOf("public static Map " + methodName + "(DispatchContext dctx, Map context) {");
        // try a little less... and some nice messy variations...
        if (methodNameIndex < 0) methodNameIndex = javaFile.indexOf(" Map " + methodName + "(DispatchContext ");
        if (methodNameIndex < 0) methodNameIndex = javaFile.indexOf(" Map  " + methodName + "(DispatchContext ");
        if (methodNameIndex < 0) methodNameIndex = javaFile.indexOf(" Map " + methodName + " (DispatchContext ");
        if (methodNameIndex < 0) methodNameIndex = javaFile.indexOf(" Map " + methodName + "( DispatchContext ");
        if (methodNameIndex < 0) methodNameIndex = javaFile.indexOf(" Map " + methodName + " ( DispatchContext ");

        // not found!
        if (methodNameIndex < 0) return -1;

        // find the open brace and return its position
        return javaFile.indexOf("{", methodNameIndex);
    }

    public static int findEndOfBlock(int blockStart, String javaFile) {
        //Debug.logInfo("In findEndOfBlock for blockStart " + blockStart, module);

        int nextOpen = javaFile.indexOf("{", blockStart+1);
        int nextClose = javaFile.indexOf("}", blockStart+1);
        if (nextOpen > 0 && nextClose > 0 && nextClose > nextOpen) {
            String javaFragment = javaFile.substring(nextOpen, nextClose);
        }
        // if no close, end with couldn't find
        if (nextClose < 0) return -1;
        // while nextOpen is found and is before the next close, then recurse (list
        while (nextOpen > -1 && nextOpen < nextClose) {
            int endOfSubBlock = findEndOfBlock(nextOpen, javaFile);
            if (endOfSubBlock < 0) return -1;
            nextOpen = javaFile.indexOf("{", endOfSubBlock+1);
            nextClose = javaFile.indexOf("}", endOfSubBlock+1);
            //Debug.logInfo("In loop in findEndOfBlock for nextOpen=" + nextOpen + ", nextClose=" + nextClose + ", endOfSubBlock=" + endOfSubBlock, module);
        }

        // at this point there should be no nextOpen or nextOpen is after the nextClose, meaning we're at the end of the block
        return nextClose;
    }

    public static Set<String> serviceMethodNames = FastSet.newInstance();
    static {
        serviceMethodNames.add("runSync");
        serviceMethodNames.add("runSyncIgnore");
        serviceMethodNames.add("runAsync");
        serviceMethodNames.add("runAsyncWait");
        serviceMethodNames.add("registerCallback");
        serviceMethodNames.add("schedule"); // NOTE: the service name may be the 1st, 2nd or 3rd param for variations on this
        serviceMethodNames.add("addRollbackService");
        serviceMethodNames.add("addCommitService");
    }
    public static Set<String> findServiceCallsInBlock(int blockStart, int blockEnd, String javaFile) {
        Set<String> serviceNameSet = FastSet.newInstance();

        int dispatcherIndex = javaFile.indexOf("dispatcher.", blockStart+1);
        while (dispatcherIndex > 0 && dispatcherIndex < blockEnd) {
            // verify it is a call we're looking for
            int openParenIndex = javaFile.indexOf("(", dispatcherIndex);
            String curMethodName = javaFile.substring(dispatcherIndex + 11, openParenIndex).trim();
            if (serviceMethodNames.contains(curMethodName)) {
                // find the service name
                int openQuoteIndex = javaFile.indexOf("\"", openParenIndex);
                int closeQuoteIndex = javaFile.indexOf("\"", openQuoteIndex+1);
                if (openQuoteIndex - openParenIndex <= 3 && openQuoteIndex >= 0 && closeQuoteIndex >= 0) {
                    //more than two spaces/chars between quote and open paren... consider it something other than what we are looking for
                    String serviceName = javaFile.substring(openQuoteIndex+1, closeQuoteIndex).trim();
                    //Debug.logInfo("In findServiceCallsInBlock found serviceName [" + serviceName + "]", module);
                    serviceNameSet.add(serviceName);
                }
            }

            dispatcherIndex = javaFile.indexOf("dispatcher.", openParenIndex);
        }

        return serviceNameSet;
    }

    public static Set<String> entityMethodNames = FastSet.newInstance();
    static {
        entityMethodNames.add("getModelEntity");
        entityMethodNames.add("getEntityGroupName");
        entityMethodNames.add("getModelEntityMapByGroup");
        entityMethodNames.add("getGroupHelperName");
        entityMethodNames.add("getEntityHelperName");
        entityMethodNames.add("getEntityHelper");

        entityMethodNames.add("makeValue");
        entityMethodNames.add("makeValueSingle");
        entityMethodNames.add("makeValidValue");
        entityMethodNames.add("makePK");
        entityMethodNames.add("makePKSingle");

        entityMethodNames.add("create");
        entityMethodNames.add("createSingle");
        entityMethodNames.add("removeByAnd");
        entityMethodNames.add("removeByCondition");

        entityMethodNames.add("create");
        entityMethodNames.add("createSingle");
        entityMethodNames.add("removeByAnd");
        entityMethodNames.add("removeByCondition");
        entityMethodNames.add("storeByCondition");
        entityMethodNames.add("removeAll");
        entityMethodNames.add("findOne");
        entityMethodNames.add("findByPrimaryKey");
        entityMethodNames.add("findByPrimaryKeySingle");
        entityMethodNames.add("findByPrimaryKeyCache");
        entityMethodNames.add("findByPrimaryKeyCacheSingle");
        entityMethodNames.add("findAll");
        entityMethodNames.add("findAllCache");
        entityMethodNames.add("findByAnd");
        entityMethodNames.add("findByOr");
        entityMethodNames.add("findByAndCache");
        entityMethodNames.add("findByLike");
        entityMethodNames.add("findByCondition");
        entityMethodNames.add("findByConditionCache");
        entityMethodNames.add("findListIteratorByCondition");
        entityMethodNames.add("find");
        entityMethodNames.add("findList");
        entityMethodNames.add("findCountByAnd");
        entityMethodNames.add("findCountByCondition");
    }
    public static Set<String> findEntityUseInBlock(int blockStart, int blockEnd, String javaFile) {
        Set<String> entityNameSet = FastSet.newInstance();

        int delegatorIndex = javaFile.indexOf("delegator.", blockStart+1);
        while (delegatorIndex > 0 && delegatorIndex < blockEnd) {
            // verify it is a call we're looking for
            int openParenIndex = javaFile.indexOf("(", delegatorIndex);
            String curMethodName = javaFile.substring(delegatorIndex + 10, openParenIndex).trim();
            if (entityMethodNames.contains(curMethodName)) {
                // find the entity name
                int openQuoteIndex = javaFile.indexOf("\"", openParenIndex);
                int closeQuoteIndex = javaFile.indexOf("\"", openQuoteIndex+1);
                if (openQuoteIndex - openParenIndex <= 3 && openQuoteIndex >= 0 && closeQuoteIndex >= 0) {
                    //more than two spaces/chars between quote and open paren... consider it something other than what we are looking for
                    String entityName = javaFile.substring(openQuoteIndex+1, closeQuoteIndex).trim();
                    //Debug.logInfo("In findServiceCallsInBlock found valid entityName [" + entityName + "]", module);
                    entityNameSet.add(entityName);
                }
            }

            delegatorIndex = javaFile.indexOf("delegator.", openParenIndex);
        }

        return entityNameSet;
    }

    public static String stripComments(String javaFile) {
        StringBuilder strippedFile = new StringBuilder();
        int commentOpen = javaFile.indexOf("/*");
        int commentClose = javaFile.indexOf("*/");
        if (commentOpen > -1) {
            if (commentOpen > 0) {
                strippedFile.append(javaFile.substring(0, commentOpen));
            }
            commentOpen = javaFile.indexOf("/*", commentClose);
            while (commentOpen > -1) {
                strippedFile.append(javaFile.substring(commentClose + 2, commentOpen));
                commentClose = javaFile.indexOf("*/", commentOpen);
                commentOpen = javaFile.indexOf("/*", commentClose);
            }
            strippedFile.append(javaFile.substring(commentClose + 2));
        } else {
            strippedFile.append(javaFile);
        }

        String lineSeparator = System.getProperty("line.separator");
        int lineComment = strippedFile.indexOf("//");
        while (lineComment > -1) {
            int endOfLine = strippedFile.indexOf(lineSeparator, lineComment);
            strippedFile.delete(lineComment, endOfLine);
            lineComment = strippedFile.indexOf("//", lineComment);
        }
        return strippedFile.toString();
    }
}
