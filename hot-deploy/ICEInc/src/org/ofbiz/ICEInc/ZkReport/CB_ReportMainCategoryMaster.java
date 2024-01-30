/**
 * 
 */
package org.ofbiz.ICEInc.ZkReport;

import java.util.List;

import org.ofbiz.ICEInc.ZkServices.ZkCommonServices;
import org.ofbiz.entity.GenericValue;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.SimpleListModel;

/**
 * @author mithun
 *
 */
public class CB_ReportMainCategoryMaster extends GenericForwardComposer {

	final String mainvariable="CB_ReportMainCatSession";
	ZkCommonServices objCommonServicesReport;
	private List<GenericValue> reportmodel=null;
	
	public Listbox lstReport1;
	public Label message4;

	public Button mytest;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		comp.setVariable(mainvariable, this, true);
		
		//setReportmodel(objCommonServicesReport.getAllDataTable("VE_All_tbl_MainCategory_Master"));
		//ListModel sm=new SimpleListModel(getReportmodel());
	
//		message4.setValue(objCommonServicesReport.getAllDataTable("VE_All_tbl_MainCategory_Master").toString());
		//ListModel pp=new SimpleListModel(objCommonServicesReport.getAllDataTable("VE_All_tbl_MainCategory_Master"));
		//lstReport1.setModel(pp);
	}

	public void onClick$mytest(Event e)
	{
		//alert("HI");
		ListModel pp=new SimpleListModel(objCommonServicesReport.getAllDataTable("VE_All_tbl_MainCategory_Master"));
		lstReport1.setModel(pp);

	}
	
	/**
	 * 
	 */
	public CB_ReportMainCategoryMaster() {
		objCommonServicesReport=new ZkCommonServices();
		List <GenericValue> il=objCommonServicesReport.getAllDataTable("VE_All_tbl_MainCategory_Master");
		reportmodel=il;
//		ListModel pp=new SimpleListModel();
	}

	/**
	 * @param separator
	 */
	public CB_ReportMainCategoryMaster(char separator) {
		super(separator);
		// TODO Auto-generated constructor stub
	}


	public void setReportmodel(List<GenericValue> reportmodel) {
		this.reportmodel = reportmodel;
	}


	public List<GenericValue> getReportmodel() {
		return reportmodel;
	}

}
