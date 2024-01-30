package org.ofbiz.widget.screen;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.StringWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;

import javax.xml.parsers.ParserConfigurationException;

import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilJ2eeCompat;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.template.FreeMarkerWorker;
import org.ofbiz.widget.html.HtmlScreenRenderer;
import org.ofbiz.widget.html.HtmlFormRenderer;


import org.ofbiz.webapp.view.ViewHandler;
import org.ofbiz.webapp.view.ViewHandlerException;
import org.xml.sax.SAXException;
import org.zkoss.mesg.Messages;
import org.zkoss.lang.D;
import org.zkoss.lang.Exceptions;
import org.zkoss.util.logging.Log;

import org.zkoss.web.servlet.Servlets;
import org.zkoss.web.servlet.http.Https;

import org.zkoss.zk.mesg.*;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.metainfo.*;
import org.zkoss.zk.ui.sys.*;
import org.zkoss.zk.ui.impl.*;
import org.zkoss.zk.ui.http.*;

import freemarker.template.*;


public class ZkViewHandler implements ViewHandler {

	private ServletContext _ctx;
	private String _name="ZKMIth";
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return _name;
	}

	@Override
	public void init(ServletContext context) throws ViewHandlerException {
	
		if(_ctx==null)
		{
			
			this._ctx=context;
		}
	}
	
	public ServletContext getServletContext() {
		return _ctx;
		
	}

	@Override
	public void render(String name, String page, String info,
			String contentType, String encoding, HttpServletRequest request,
			HttpServletResponse response) throws ViewHandlerException {
		
		Writer writer=null;
		try 
		{
			RequestDispatcher dispatcher =getServletContext().getRequestDispatcher(page);
			dispatcher.forward(request	, response);
			
		}
		catch (Exception e) 
		{
			System.out.println(e);
		}

	}

	
	@Override
	public void setName(String name) {
		this._name=name;

	}

}
