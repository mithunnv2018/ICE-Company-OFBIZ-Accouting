package org.atulmith.constructionmanagement;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;

import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.GenericServiceException;

public class SiteMasterEvents {
    
    public static final String module = SiteMasterEvents.class.getName();
     
    public static String createSiteMasterJavaEvent(HttpServletRequest request, HttpServletResponse response) {
//           LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
           GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
//           GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
           
//           String salutation = (String) request.getParameter("salutation");
//           String firstName = (String) request.getParameter("firstName");
//           String middleName=(String) request.getParameter("middleName");
//           String lastName=(String) request.getParameter("lastName");
//           String suffix=(String) request.getParameter("suffix");
           String SiteName=(String) request.getParameter("SiteName");
           String SiteAddress1=(String) request.getParameter("SiteAddress1");
           String SiteAddress2=(String) request.getParameter("SiteAddress2");
           String SiteTelno=(String) request.getParameter("SiteTelno");
           
           
//         Map createPersonContext = new HashMap();
//         createPersonContext.put("firstName", firstName);
//         createPersonContext.put("middleName", middleName);
//         createPersonContext.put("lastName", lastName);
//         createPersonContext.put("suffix", suffix);
                       
           
           Map createPersonCtx = UtilMisc.toMap("site_id", SiteName.toUpperCase(),"site_name", SiteName, "site_add1", SiteAddress1, "site_add2", SiteAddress2, "site_phoneno", SiteTelno);
           try {
               GenericValue row=delegator.makeValue("tbl_site_master", createPersonCtx);
               
               row.create();
               
           } catch (GenericEntityException e) {
               Debug.logError(e.toString(), module);
               return "error";
           }
           return "success";
    }           
            
}
