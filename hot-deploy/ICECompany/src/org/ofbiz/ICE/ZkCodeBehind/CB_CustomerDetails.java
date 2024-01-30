package org.ofbiz.ICE.ZkCodeBehind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ofbiz.ICE.ZkServices.ZkCommonServices;
import org.ofbiz.ICE.zkValidation.zkCommonValidations;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericValue;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Constraint;
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

public class CB_CustomerDetails extends GenericForwardComposer {

	
	
	
	private ZkCommonServices objCommonServices;
	private zkCommonValidations objCommonValidations;
	
	public Tabpanel tp_mainCategoryAddnew;
	public Tabpanel tp_mainCategoryModify;
	
	public Listbox lstcustomerdetailmodify;

	String variablemodel = "CustomerMaster";
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
	public Textbox txtcustomername;
	public Textbox txtcustomeraddress;
	public Textbox txtcity;
	public Textbox txtpostalcode;
	public Textbox txtphone;
	public Textbox txtemail;
	
	
	//---Modify Components
	public Textbox txtmodifycustomername;
	public Textbox txtmodifyaddress;
	public Textbox txtmodifycity;
	public Textbox txtmodifyphone;
	public Textbox txtmodifyemail;
	public Textbox txtmodifypostalCode;
	
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		 
		comp.setVariable(variablemodel, this, true);
		
		setData2(getCustomerTable());
		ListModel sm = new SimpleListModel(getData2());
		
		if(comp.getId().equals("ModifyCust"))
		{
			//lstcustomerdetailmodify.setModel(new SimpleListModel(getCustomerTable()));
		}
		// txtMainCategory.setConstraint(objempty);

	}

	public CB_CustomerDetails() {
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
	public void onClick$btnCancelModify(Event e)
	{
		
		clearControlsModify();
		
	}
	//For Add New
	public void onClick$btnSave(Event e) {
		long maxSeqVal = objCommonServices.CustSequenceId(
				"VE_Max_tbl_Customer_Master", "Cust_Tag");
		// alert(""+maxSeqVal);
		// alert("Error"+objCommonServices.strError );
		Map row = new HashMap();

		row = UtilMisc.toMap("Cust_Id", String.valueOf(maxSeqVal),
				"Cust_Name", txtcustomername.getText().trim(), "Cust_Address",txtcustomeraddress.getText(),
				 "Cust_City",txtcity.getText(),"Cust_PostalCode",txtpostalcode.getText(), "Cust_PhNo", txtphone.getText(),
				 "Cust_EmailId",txtemail.getText(),"Cust_Tag", String
						.valueOf(maxSeqVal));
		// UtilMisc.tom
		if (validateControlsNew())
		{
			if (!objCommonServices.CustCheckDuplicate("tbl_Customer_Master",
					"Cust_Name", txtcustomername.getText().trim())) {
				if (objCommonServices.createTable("tbl_Customer_Master", row) == true) {
					alert("Updated");
					clearControlsNew();
				} else {
					alert("Error" + objCommonServices.strError);
				}
			} else {
				alert("Entry already exist");
			}			
		}		

	}
	//Vaidate Add New Components
	public boolean validateControlsNew()
	{
		 String Error ="";
		 boolean val;
		 Error+= objCommonValidations.TextBoxValidate(txtcustomername.getText().trim())?"":"Customer Name cannot be empty";
		 
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
	//For Modify
	public void onClick$btnSaveModify(Event e)
	{
		Map StrId = UtilMisc.toMap("Cust_Id",variableId);
		Map StrValues =UtilMisc.toMap("Cust_Name",txtmodifycustomername.getText(),
				"Cust_Address",txtmodifyaddress.getText(),"Cust_City",txtmodifycity.getText(),
				"Cust_PostalCode",txtmodifypostalCode.getText(),"Cust_PhNo",txtmodifyphone.getText(),
				"Cust_EmailId",txtmodifyemail.getText());
		
		
		if(validateControlsModify()){		
			if (objCommonServices.updateTable("tbl_Customer_Master", StrValues, StrId) == true) 
			{
				alert("Updated");
				clearControlsModify();
				refreshGrid();
			} else {
				alert("Error" + objCommonServices.strError);
			}
		
	  }
	}
	//Validate Modify Components
	public boolean validateControlsModify()
	{
		 String Error ="";
		 boolean val;
		 
		 Error+= objCommonValidations.TextBoxValidate(txtmodifycustomername.getText().trim())?"":"Enter Customer Name";
		 				 
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
					.getAllDataTable("VE_Select_tbl_Customer_Master"));
			ListModel sm = new SimpleListModel(getData2());
			lstcustomerdetailmodify.setModel(sm);
		}
		catch (Exception ex) {
			alert("You are @ maincatregory modify"+ex.toString());
		}
	}

	/*public void onClick$tp_mainCategoryAddnew(Event e) {
//		alert("You are @ maincateogryaddnew");

	}*/

	public void onClick$tp_mainCategoryModify(Event e) {
		 
	}
	
	public void onSelect$lstcustomerdetailmodify(Event e)
	{
//		Set it= lbmaincategorymodify.getSelectedItems();
//		for (Object obj : it) {
//			Listitem d=(Listitem)obj;
//			
//			//alert("object iterate ="+d.getLabel());
//			alert("my child ="+d.getFirstChild().toString());
//			
//			
//		}
		
		Listitem a= lstcustomerdetailmodify.getSelectedItem();
		List a2=a.getChildren();
		for(int i=0;i<a2.size();i++)
		{
			Listcell lc=(Listcell) a2.get(i);
			//alert(lc.getLabel());
			if(i==0){variableId=lc.getLabel(); }//get Id
			//if (i==1){txtMainCatModify.setText(lc.getLabel());}//Set Main Category Name
		}
		
		List<GenericValue> StrRow = objCommonServices.ReturnRowGen("tbl_Customer_Master", "Cust_Id", variableId);
		
		GenericValue genericValue5 = StrRow.get(0);
		
		txtmodifycustomername.setText(genericValue5.getString("Cust_Name"));
		txtmodifyaddress.setText(genericValue5.getString("Cust_Address"));
		txtmodifycity.setText(genericValue5.getString("Cust_City"));
		txtmodifyemail.setText(genericValue5.getString("Cust_EmailId"));
		txtmodifyphone.setText(genericValue5.getString("Cust_PhNo"));
		txtmodifypostalCode.setText(genericValue5.getString("Cust_PostalCode"));
		
		
		//alert(lbmaincategorymodify.getSelectedItem().getLabel());
		
		//alert(e.getData().toString());
	}

	public List<GenericValue> getCustomerTable() {
		return objCommonServices.getAllDataTable("VE_Select_tbl_Customer_Master");

	}

	public void clearControlsNew() {		
		txtcustomername.setText("");
		txtcity.setText("");
		txtcustomeraddress.setText("");
		txtemail.setText("");
		txtphone.setText("");
		txtpostalcode.setText("");		
	}
	
	public void clearControlsModify(){
		//txtMainCatModify.setText("");
		try{
		txtmodifyaddress.setText("");
		txtmodifycity.setText("");
		txtmodifycustomername.setText("");
		txtmodifyemail.setText("");
		txtmodifyphone.setText("");
		txtmodifypostalCode.setText("");
		}
		catch(Exception sss)
		{
			alert(sss.getMessage());
		}
	}

	public void setData2(List<GenericValue> data2) {
		this.data2 = data2;
	}

	public List<GenericValue> getData2() {
		return data2;
	}
	
}
