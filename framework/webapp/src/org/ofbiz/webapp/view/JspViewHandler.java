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
package org.ofbiz.webapp.view;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;

import org.ofbiz.base.util.Debug;
import org.ofbiz.webapp.control.ContextFilter;

/**
 * JspViewHandler - Java Server Pages View Handler
 */
public class JspViewHandler extends AbstractViewHandler {

    public static final String module = JspViewHandler.class.getName();

    protected ServletContext context;

    public void init(ServletContext context) throws ViewHandlerException {
        this.context = context;
    }

    public void render(String name, String page, String contentType, String encoding, String info, HttpServletRequest request, HttpServletResponse response) throws ViewHandlerException {
        // some containers call filters on EVERY request, even forwarded ones,
        // so let it know that it came from the control servlet

        if (request == null) {
            throw new ViewHandlerException("Null HttpServletRequest object");
        }
        if (page == null || page.length() == 0) {
            throw new ViewHandlerException("Null or empty source");
        }

        //Debug.log("Requested Page : " + page, module);
        //Debug.log("Physical Path  : " + context.getRealPath(page));

        // tell the ContextFilter we are forwarding
        request.setAttribute(ContextFilter.FORWARDED_FROM_SERVLET, Boolean.TRUE);
        RequestDispatcher rd = request.getRequestDispatcher(page);

        if (rd == null) {
            Debug.logInfo("HttpServletRequest.getRequestDispatcher() failed; trying ServletContext", module);
            rd = context.getRequestDispatcher(page);
            if (rd == null) {
                Debug.logInfo("ServletContext.getRequestDispatcher() failed; trying ServletContext.getNamedDispatcher(\"jsp\")", module);
                rd = context.getNamedDispatcher("jsp");
                if (rd == null) {
                    throw new ViewHandlerException("Source returned a null dispatcher (" + page + ")");
                }
            }
        }

        try {
            rd.include(request, response);
        } catch (IOException ie) {
            throw new ViewHandlerException("IO Error in view", ie);
        } catch (ServletException e) {
            Throwable throwable = e.getRootCause() != null ? e.getRootCause() : e;

            if (throwable instanceof JspException) {
                JspException jspe = (JspException) throwable;

                throwable = jspe.getCause() != null ? jspe.getCause() : jspe;
            }
            Debug.logError(throwable, "ServletException rendering JSP view", module);
            throw new ViewHandlerException(e.getMessage(), throwable);
        }
    }
}
