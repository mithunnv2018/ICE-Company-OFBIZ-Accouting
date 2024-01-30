package org.ofbiz.ICE.ZkCodeBehind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ofbiz.ICE.ZkServices.ZkCommonServices;
import org.ofbiz.ICE.zkValidation.zkCommonValidations;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityOperator;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.SimpleConstraint;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class CB_SalesTax extends GenericForwardComposer{

	private ZkCommonServices objCommonServices;
	private zkCommonValidations objCommonValidations;
	
	public Tabpanel tp_mainCategoryAddnew;
	public Tabpanel tp_mainCategoryModify;
	
	public Listbox lstsalestaxdetail;

	String variablemodel = "SalesTax";
	private List<GenericValue> data2 = null;

	//Buttons
	public Button btnSave;
	public Button btnCancel;
	public Button btnrefresh;
	public Button btnSaveModify;
	public Button btnCancelModify;
	
	//Modify ID 
	public String variableId="0";
	

	//-------Add new components--------
	public Textbox txttaxpercentage;

	
	//Date box
	public Datebox datesalestaxdate;
	
	
	//---Modify Components
	//No Modify Components in this form
	
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		 
		comp.setVariable(variablemodel, this, true);
		
		setData2(getSalesTaxTable());
		ListModel sm = new SimpleListModel(getData2());
		
		if(comp.getId().equals("ModifyCust"))
		{
			//lstcustomerdetailmodify.setModel(new SimpleListModel(getCustomerTable()));
		}
		// txtMainCategory.setConstraint(objempty);
		
	}

	public CB_SalesTax() {
		objCommonServices = new ZkCommonServices();
		objCommonValidations = new zkCommonValidations();
		// if (objempty == null) {
		// objCommonServices = new ZkCommonServices();
		// objempty = new SimpleConstraint(SimpleConstraint.NO_EMPTY,
		// "field cannot be empty!");
		// objnull = new SimpleConstraint("");
		//
		// txtMainCategory.setConstraint(objempty);
		// }
	}
	
	//For Add New
	public void onClick$btnSave(Event e) {
		long maxSeqVal = objCommonServices.CustSequenceId(
				"VE_Max_tbl_SalesTax_Details", "St_Id");
		// alert(""+maxSeqVal);
		// alert("Error"+objCommonServices.strError );
		Map row = new HashMap();

		row = UtilMisc.toMap("St_Id", String.valueOf(maxSeqVal),
				"St_Percentage", txttaxpercentage.getText().trim(), 
				 "st_Date", datesalestaxdate.getText());
		// UtilMisc.tom
		if (validateControlsNew())
		{			
				if (objCommonServices.createTable("tbl_SalesTax_Details", row) == true) {
					alert("Updated");
					clearControlsNew();
					refreshGrid();
				} else {
					alert("Error" + objCommonServices.strError);
				}			
		}		

	}
	//Vaidate Add New Components
	public boolean validateControlsNew()
	{
		 String Error ="";
		 boolean val;
		 Error+= objCommonValidations.TextBoxValidate(txttaxpercentage.getText().trim())?"":"Tax % cannot be empty";
		 Error+= objCommonValidations.TextBoxValidate(txttaxpercentage.getText().trim())?"":"Date cannot be empty";
		
		 if(!Error.equals(""))
		 {
			 alert(Error);
			 return false;
		 }
		 else
		 {
			 return true;
		 }	 

	}
	
	
	public void onClick$btnCancel(Event e) {
	
//		try {
//			Executions.getCurrent().sendRedirect("salestaxaddnew.zul");
////			Executions.getCurrent().forward("salestaxaddnew.zul");
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		clearControlsNew();

	}
	public void onClick$btnrefresh(Event e)
	{
		//alert("btnrefresh working" + variableId);
		refreshGrid();
		
	}
	public void refreshGrid()
	{
		try {
			setData2(objCommonServices
					.getAllDataTable("tbl_SalesTax_Details"));
			ListModel sm = new SimpleListModel(getData2());
			lstsalestaxdetail.setModel(sm);
		}
		catch (Exception ex) {
			alert("You are @ maincatregory modify"+ex.toString());
		}
	}

	/*public void onClick$tp_mainCategoryAddnew(Event e) {
//		alert("You are @ maincateogryaddnew");

	}*/

	

	public List<GenericValue> getSalesTaxTable() {
		return objCommonServices.getAllDataTable("tbl_SalesTax_Details");

	}

	public void clearControlsNew() {		
			txttaxpercentage.setText("");
			datesalestaxdate.setText("");
	}
	
	public void setData2(List<GenericValue> data2) {
		this.data2 = data2;
	}

	public List<GenericValue> getData2() {
		return data2;
	}
}
