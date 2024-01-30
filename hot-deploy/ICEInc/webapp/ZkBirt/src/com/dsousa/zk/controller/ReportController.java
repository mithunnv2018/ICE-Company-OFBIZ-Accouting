package com.dsousa.zk.controller;

import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Window;

/**
 * created by: dsousa
 */

public class ReportController extends Window implements AfterCompose
{

	private static final long serialVersionUID = 1L;
	private Iframe iframe;
	

	public void afterCompose()
	{
		Components.wireVariables(this, this);
		Components.addForwards(this, this);
	}

	public void onClick$btReport()
	{
		String url = "/run?__report=test.rptdesign";
		iframe.setSrc(url);
	}

}
