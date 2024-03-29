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
package org.ofbiz.widget.screen;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.collections.MapStack;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntity;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.security.Security;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.webapp.control.LoginWorker;
import org.ofbiz.widget.cache.GenericWidgetOutput;
import org.ofbiz.widget.cache.ScreenCache;
import org.ofbiz.widget.cache.WidgetContextCacheKey;
import org.xml.sax.SAXException;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.jsp.TaglibFactory;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.ext.servlet.HttpSessionHashModel;

/**
 * Widget Library - Screen model class
 */
public class ScreenRenderer {

    public static final String module = ScreenRenderer.class.getName();

    protected Appendable writer;
    protected MapStack<String> context;
    protected ScreenStringRenderer screenStringRenderer;

    public ScreenRenderer(Appendable writer, MapStack<String> context, ScreenStringRenderer screenStringRenderer) {
        this.writer = writer;
        this.context = context;
        if (this.context == null) this.context = MapStack.create();
        this.screenStringRenderer = screenStringRenderer;
    }

    /**
     * Renders the named screen using the render environment configured when this ScreenRenderer was created.
     *
     * @param combinedName A combination of the resource name/location for the screen XML file and the name of the screen within that file, separated by a pound sign ("#"). This is the same format that is used in the view-map elements on the controller.xml file.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public String render(String combinedName) throws GeneralException, IOException, SAXException, ParserConfigurationException {
        String resourceName = ScreenFactory.getResourceNameFromCombined(combinedName);
        String screenName = ScreenFactory.getScreenNameFromCombined(combinedName);
        this.render(resourceName, screenName);
        return "";
    }

    /**
     * Renders the named screen using the render environment configured when this ScreenRenderer was created.
     *
     * @param resourceName The name/location of the resource to use, can use "component://[component-name]/" and "ofbiz://" and other special OFBiz style URLs
     * @param screenName The name of the screen within the XML file specified by the resourceName.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public String render(String resourceName, String screenName) throws GeneralException, IOException, SAXException, ParserConfigurationException {
        ModelScreen modelScreen = ScreenFactory.getScreenFromLocation(resourceName, screenName);
        if (modelScreen.useCache) {
            // if in the screen definition use-cache is set to true
            // then try to get an already built screen output from the cache:
            // 1) if we find it then we get it and attach it to the passed in writer
            // 2) if we can't find one, we create a new StringWriter,
            //    and pass it to the renderScreenString;
            //    then we wrap its content and put it in the cache;
            //    and we attach it to the passed in writer
            WidgetContextCacheKey wcck = new WidgetContextCacheKey(context);
            String screenCombinedName = resourceName + ":" + screenName;
            ScreenCache screenCache = new ScreenCache();
            GenericWidgetOutput gwo = screenCache.get(screenCombinedName, wcck);
            if (gwo == null) {
                Writer sw = new StringWriter();
                screenStringRenderer.renderScreenBegin(writer, context);
                modelScreen.renderScreenString(sw, context, screenStringRenderer);
                screenStringRenderer.renderScreenEnd(writer, context);
                gwo = new GenericWidgetOutput(sw.toString());
                screenCache.put(screenCombinedName, wcck, gwo);
                writer.append(gwo.toString());
            } else {
                writer.append(gwo.toString());
            }
        } else {
            screenStringRenderer.renderScreenBegin(writer, context);
            modelScreen.renderScreenString(writer, context, screenStringRenderer);
            screenStringRenderer.renderScreenEnd(writer, context);
        }
        return "";
    }

    public ScreenStringRenderer getScreenStringRenderer() {
        return this.screenStringRenderer;
    }

    public void populateBasicContext(Map<String, Object> parameters, GenericDelegator delegator, LocalDispatcher dispatcher, Security security, Locale locale, GenericValue userLogin) {
        populateBasicContext(context, this, parameters, delegator, dispatcher, security, locale, userLogin);
    }

    public static void populateBasicContext(MapStack<String> context, ScreenRenderer screens, Map<String, Object> parameters, GenericDelegator delegator, LocalDispatcher dispatcher, Security security, Locale locale, GenericValue userLogin) {
        // ========== setup values that should always be in a screen context
        // include an object to more easily render screens
        context.put("screens", screens);

        // make a reference for high level variables, a global context
        context.put("globalContext", context.standAloneStack());

        // make sure the "nullField" object is in there for entity ops; note this is nullField and not null because as null causes problems in FreeMarker and such...
        context.put("nullField", GenericEntity.NULL_FIELD);

        context.put("parameters", parameters);
        context.put("delegator", delegator);
        context.put("dispatcher", dispatcher);
        context.put("security", security);
        context.put("locale", locale);
        context.put("userLogin", userLogin);
        context.put("nowTimestamp", UtilDateTime.nowTimestamp());
    }

    /**
     * This method populates the context for this ScreenRenderer based on the HTTP Request and Response objects and the ServletContext.
     * It leverages various conventions used in other places, namely the ControlServlet and so on, of OFBiz to get the different resources needed.
     *
     * @param request
     * @param response
     * @param servletContext
     */
    public void populateContextForRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
        populateContextForRequest(context, this, request, response, servletContext);
    }

    public static void populateContextForRequest(MapStack<String> context, ScreenRenderer screens, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
        HttpSession session = request.getSession();

        // attribute names to skip for session and application attributes; these are all handled as special cases, duplicating results and causing undesired messages
        Set<String> attrNamesToSkip = UtilMisc.toSet("delegator", "dispatcher", "security", "webSiteId");
        Map<String, Object> parameterMap = UtilHttp.getCombinedMap(request, attrNamesToSkip);

        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");

        populateBasicContext(context, screens, parameterMap, (GenericDelegator) request.getAttribute("delegator"),
                (LocalDispatcher) request.getAttribute("dispatcher"), (Security) request.getAttribute("security"),
                UtilHttp.getLocale(request), userLogin);

        context.put("autoUserLogin", session.getAttribute("autoUserLogin"));
        context.put("person", session.getAttribute("person"));
        context.put("partyGroup", session.getAttribute("partyGroup"));

        // some things also seem to require this, so here it is:
        request.setAttribute("userLogin", userLogin);

        // set up the user's time zone
        context.put("timeZone", UtilHttp.getTimeZone(request));

        // ========== setup values that are specific to OFBiz webapps

        context.put("request", request);
        context.put("response", response);
        context.put("session", session);
        context.put("application", servletContext);
        if (servletContext != null) {
            String rootDir = (String) context.get("rootDir");
            String webSiteId = (String) context.get("webSiteId");
            String https = (String) context.get("https");
            if (UtilValidate.isEmpty(rootDir)) {
                rootDir = servletContext.getRealPath("/");
                context.put("rootDir", rootDir);
            }
            if (UtilValidate.isEmpty(webSiteId)) {
                webSiteId = (String) servletContext.getAttribute("webSiteId");
                context.put("webSiteId", webSiteId);
            }
            if (UtilValidate.isEmpty(https)) {
                https = (String) servletContext.getAttribute("https");
                context.put("https", https);
            }
        }
        context.put("javaScriptEnabled", Boolean.valueOf(UtilHttp.isJavaScriptEnabled(request)));

        // these ones are FreeMarker specific and will only work in FTL templates, mainly here for backward compatibility
        BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
        context.put("sessionAttributes", new HttpSessionHashModel(session, wrapper));
        context.put("requestAttributes", new HttpRequestHashModel(request, wrapper));
        TaglibFactory JspTaglibs = new TaglibFactory(servletContext);
        context.put("JspTaglibs", JspTaglibs);
        context.put("requestParameters",  UtilHttp.getParameterMap(request));

        // this is a dummy object to stand-in for the JPublish page object for backward compatibility
        context.put("page", FastMap.newInstance());

        // some information from/about the ControlServlet environment
        context.put("controlPath", request.getAttribute("_CONTROL_PATH_"));
        context.put("contextRoot", request.getAttribute("_CONTEXT_ROOT_"));
        context.put("serverRoot", request.getAttribute("_SERVER_ROOT_URL_"));
        context.put("checkLoginUrl", LoginWorker.makeLoginUrl(request));
        String externalLoginKey = LoginWorker.getExternalLoginKey(request);
        String externalKeyParam = externalLoginKey == null ? "" : "&amp;externalLoginKey=" + externalLoginKey;
        context.put("externalLoginKey", externalLoginKey);
        context.put("externalKeyParam", externalKeyParam);

        // setup message lists
        List<String> eventMessageList = UtilGenerics.toList(request.getAttribute("eventMessageList"));
        if (eventMessageList == null) eventMessageList = FastList.newInstance();
        List<String> errorMessageList = UtilGenerics.toList(request.getAttribute("errorMessageList"));
        if (errorMessageList == null) errorMessageList = FastList.newInstance();

        if (request.getAttribute("_EVENT_MESSAGE_") != null) {
            eventMessageList.add(UtilFormatOut.replaceString((String) request.getAttribute("_EVENT_MESSAGE_"), "\n", "<br/>"));
            request.removeAttribute("_EVENT_MESSAGE_");
        }
        List<String> msgList = UtilGenerics.toList(request.getAttribute("_EVENT_MESSAGE_LIST_"));
        if (msgList != null) {
            eventMessageList.addAll(msgList);
            request.removeAttribute("_EVENT_MESSAGE_LIST_");
        }
        if (request.getAttribute("_ERROR_MESSAGE_") != null) {
            errorMessageList.add(UtilFormatOut.replaceString((String) request.getAttribute("_ERROR_MESSAGE_"), "\n", "<br/>"));
            request.removeAttribute("_ERROR_MESSAGE_");
        }
        if (session.getAttribute("_ERROR_MESSAGE_") != null) {
            errorMessageList.add(UtilFormatOut.replaceString((String) session.getAttribute("_ERROR_MESSAGE_"), "\n", "<br/>"));
            session.removeAttribute("_ERROR_MESSAGE_");
        }
        msgList = UtilGenerics.toList(request.getAttribute("_ERROR_MESSAGE_LIST_"));
        if (msgList != null) {
            errorMessageList.addAll(msgList);
            request.removeAttribute("_ERROR_MESSAGE_LIST_");
        }
        context.put("eventMessageList", eventMessageList);
        context.put("errorMessageList", errorMessageList);

        if (request.getAttribute("serviceValidationException") != null) {
            context.put("serviceValidationException", request.getAttribute("serviceValidationException"));
            request.removeAttribute("serviceValidationException");
        }

        // if there was an error message, this is an error
        context.put("isError", errorMessageList.size() > 0 ? Boolean.TRUE : Boolean.FALSE);
        // if a parameter was passed saying this is an error, it is an error
        if ("true".equals(parameterMap.get("isError"))) {
            context.put("isError", Boolean.TRUE);
        }

        // to preserve these values, push the MapStack
        context.push();
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void populateContextForService(DispatchContext dctx, Map<String, Object> serviceContext) {
        this.populateBasicContext(serviceContext, dctx.getDelegator(), dctx.getDispatcher(), dctx.getSecurity(),
                (Locale) serviceContext.get("locale"), (GenericValue) serviceContext.get("userLogin"));
    }
}
