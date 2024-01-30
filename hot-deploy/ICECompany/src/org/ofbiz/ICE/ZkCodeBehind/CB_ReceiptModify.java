package org.ofbiz.ICE.ZkCodeBehind;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ofbiz.ICE.ZkServices.ZkCommonServices;
import org.ofbiz.ICE.zkValidation.zkCommonValidations;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.finder.EntityFinderUtil.ConditionList;
import org.python.antlr.base.expr;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.ext.client.Selectable;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.AbstractListModel;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.SimpleConstraint;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.api.Decimalbox;

public class CB_ReceiptModify extends GenericForwardComposer{

	
	private ZkCommonServices objCommonServices;
	private zkCommonValidations objCommonValidations;

	public long seqId;
	public String acYear;
	public long billseq;
	public String sopaydetails[];
	public double soamount[];
	public double sodatabaseamt[];
	public int rowcount;

	public Tabpanel tp_mainCategoryAddnew;
	public Tabpanel tp_mainCategoryModify;

	public Listbox lstcustomerdetailmodify;

	String variablemodel = "ReceiptModify";
	private List<GenericValue> data2 = null;
	private long maxSeqVal;

	// Buttons
	public Button btnSave;
	public Button btnCancel;
	public Button btnrefresh;
	public Button btnSaveModify;
	public Button btnCancelModify;
	public Button btnDelete;
	public Button btnSoItemAdd;
	public Button btnCalculate;
	public Button btnRefreshCust;
	public Button btnAdjust;
	public Button btnReset;

	// Modify ID
	public String variableId = "0";
	public int length;

	// -------Add new components--------

	public Textbox txtBillamt;
	public Textbox txttobeAdjust;
	public Bandbox bdCustomer;
	public Listbox lstCustomer;
	public Datebox dateSalesDateAddnew;
	public Textbox txtReceiptNo;
	public Datebox dateReceipt;
	public Listbox lstSalesOrder;
	public Textbox txtPaidamt;
	public Bandbox bdPaymenttype;
	public Listbox lstpaymenttype;
	public Textbox txtRemarks;
	public Textbox txtpaymenttype;
	public Textbox txtCustomer;
	public Listbox lstReceiptmodify;
	public Bandbox bdStatus;
	public Listbox lstStatus;
	public Button btnGridRefresh;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		comp.setVariable(variablemodel, this, true);

		
		//ListModel sm = new SimpleListModel(getData2());

		if (comp.getId().equals("ReceiptModify")) {

			// lstcustomerdetailmodify.setModel(new
			// SimpleListModel(getCustomerTable()));
			// DONE Bill Number and A/C Year
			

		}
		SetControls();
		setData2(getCustomerTable());
		// txtMainCategory.setConstraint(objempty);

	}

	public void onClick$btnGridRefresh(Event e)
	{
		EntityExpr xxp = new EntityExpr("ac_year",
				 EntityOperator.EQUALS,acYear);
		
		EntityExpr xxp2 = new EntityExpr("Receipt_Status",
				 EntityOperator.EQUALS,"VOID");
				
		 List a= UtilMisc.toList(xxp,xxp2);
		 
		 EntityConditionList condList = new EntityConditionList(a,EntityOperator.AND);
		
		 ListModelList x= new ListModelList(objCommonServices.ReturnMultipleCondition("VE_Select_tbl_SalesReceipt_Customer", condList,"-Receipt_Id"));
		 
		 lstReceiptmodify.setModel(x); 
	}
	
	public List<GenericValue> getCustomerTable() {
		
		EntityExpr xxp = new EntityExpr("ac_year",
				 EntityOperator.EQUALS,acYear);
		
		EntityExpr xxp2 = new EntityExpr("Receipt_Status",
				 EntityOperator.EQUALS,"VOID");
				
		 List a= UtilMisc.toList(xxp,xxp2);
		 
		 EntityConditionList condList = new EntityConditionList(a,EntityOperator.AND);
		
		return objCommonServices.ReturnMultipleCondition("VE_Select_tbl_SalesReceipt_Customer", condList,"-Receipt_Tag"); 
				//.getAllDataTable("VE_Select_tbl_SalesReceipt_Customer");

	}

	public CB_ReceiptModify() {
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
	public void onClick$btnSave(Event e) {
		Map StrId = UtilMisc.toMap("Receipt_Id", variableId);
		Map StrValues = UtilMisc.toMap("Receipt_Paytype", bdPaymenttype.getText()
				, "Receipt_Remarks", txtRemarks.getText(),
				"Receipt_Status", bdStatus.getText(),"Receipt_Date",dateReceipt.getText());

		if (validateControls()) {
			if (objCommonServices.updateTable("tbl_Receipt_Details", StrValues,
					StrId) == true) {
				if(bdStatus.getText().equals("CANCELED"))
				{
					List<GenericValue> StrRow = objCommonServices.ReturnRowGen(
							"tbl_SOPayment_Details", "Receipt_Id", variableId);
					
					for(int i=0;i<StrRow.size();i++)
					{
						GenericValue row = StrRow.get(i);
						
						Map SOValues = UtilMisc.toMap("So_Status","APPROVED");
						Map SOID = UtilMisc.toMap("So_Id", row.getString("So_Id"));
						objCommonServices.updateTable("tbl_SalesOrder_Details", SOValues, SOID);
						
					}
					
					objCommonServices.DeleteConditionalRecords("tbl_SOPayment_Details", "Receipt_Id", variableId);
				}
				alert("Updated");
				clearControls();
				refreshGrid();
			} else {
				alert("Error" + objCommonServices.strError);
			}

		}
	}
	public void refreshGrid() {
		try {
			EntityExpr xxp = new EntityExpr("ac_year",
					 EntityOperator.EQUALS,acYear);
			
			EntityExpr xxp2 = new EntityExpr("Receipt_Status",
					 EntityOperator.EQUALS,"VOID");
					
			 List a= UtilMisc.toList(xxp,xxp2);
			 
			 EntityConditionList condList = new EntityConditionList(a,EntityOperator.AND);
			
			 
			ListModelList xxxx  = new ListModelList(objCommonServices.ReturnMultipleCondition("VE_Select_tbl_SalesReceipt_Customer", condList,"-Receipt_Id"));
			lstReceiptmodify.setModel(xxxx);
		} catch (Exception ex) {
			alert("" + ex.toString());
		}
	}
	public void clearControls() {
		// txtMainCatModify.setText("");
		try {
			bdPaymenttype.setText("--Select--");
			bdStatus.setText("--Select--");
			txtReceiptNo.setText("");
			dateReceipt.setText("");
			txtCustomer.setText("");
			txtPaidamt.setText("");
			txtRemarks.setText("");
			ListModelList x=null;
			lstSalesOrder.setModel(x);
		} catch (Exception sss) {
			alert(sss.getMessage());
		}
	}
	public void onClick$btnCancel(Event e)
	{
		clearControls();
	}
	public boolean validateControls() {
		String Error = "";
		boolean val;

		Error += bdPaymenttype.getText().equals("--Select--") ? "Select Payment Type\n" : "";
		Error += bdStatus.getText().equals("--Select--") ? "Select Status" : "";
		Error += dateReceipt.getText().equals("")?"Select Receipt Date":"";
		
		//To be uncommented 
		
		if(Error.equals("")){
		Error += objCommonServices.verifydate(dateReceipt.getValue(), "RECEIPT")?"":"Receipt Date should be in current accounting year: " + acYear+"\n";
		}
		if (!Error.equals("")) {
			alert(Error);
			return false;
		} else {
			return true;
		}

	}	
	
	public void SetControls() {
		acYear = objCommonServices.GetACYear();
		ArrayList locala1 = objCommonServices.GetReceiptSequence(acYear);
		seqId = (Long) locala1.get(0);
		billseq = (Long) locala1.get(1) + 1;

		
		// DONE POPULATE Customer Listbox
		//lstCustomer.setModel(objCommonServices.PopulateListBox(
		//		"tbl_Customer_Master", "Cust_Name", "", ""));

		//bdCustomer.setText("--Select--");
		String a[] =new String[]{"--Select--","VOID","CANCELED"};
		ListModel xyz = new ListModelList(a);
		lstStatus.setModel(xyz);
		bdStatus.setText("--Select--");
		
		String ab[] = new String[] {"--Select--","Cash", "Cheque"};

		ListModel xxx = new ListModelList(ab);
		lstpaymenttype.setModel(xxx);
		//bdPaymenttype.setText("--Select--");
		//lstpaymenttype.setModel(xxx);

		//bdPaymenttype.setText("");

	}
	public void onSelect$lstReceiptmodify(Event e)
	{
		Listitem a= lstReceiptmodify.getSelectedItem();
		List a2=a.getChildren();
		for(int i=0;i<a2.size();i++)
		{
			Listcell lc=(Listcell) a2.get(i);
			//alert(lc.getLabel());
			if(i==0){
				variableId=lc.getLabel(); 
				//maxSeqVal=Long.valueOf(lc.getLabel());
			}
			//get Id
			//if (i==1){txtMainCatModify.setText(lc.getLabel());}//Set Main Category Name
		}
		List<GenericValue> StrRow = objCommonServices.ReturnRowGen("VE_Select_tbl_SalesReceipt_Customer", "Receipt_Id", variableId);
		
		GenericValue genericValue5 = StrRow.get(0);
		
		//txtmodifycustomername.setText(genericValue5.getString("Cust_Name"));
		//---------Populate Form--------------------
		
		 txtReceiptNo.setText(genericValue5.getString("Receipt_Nos"));
		 dateReceipt.setValue(genericValue5.getDate("Receipt_Date"));
		 txtCustomer.setText(genericValue5.getString("Cust_Name"));
		 txtPaidamt.setText(genericValue5.getString("Receipt_Amt"));
		 bdPaymenttype.setText(genericValue5.getString("Receipt_Paytype"));
		 txtRemarks.setText(genericValue5.getString("Receipt_Remarks"));
		 bdStatus.setText(genericValue5.getString("Receipt_Status"));
		
		 EntityExpr xxp = new EntityExpr("Receipt_Id",
				 EntityOperator.EQUALS,variableId);
		 ListModelList popgrid = new ListModelList(objCommonServices.ReturnSingleCondition("VE_Select_tbl_Receipt_SoPayment", xxp));
		 
		 lstSalesOrder.setModel(popgrid);
	}
	public void setData2(List<GenericValue> data2) {
		this.data2 = data2;
	}

	public List<GenericValue> getData2() {
		return data2;
	}
	
}
