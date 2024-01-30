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

public class CB_ReceiptDetails extends GenericForwardComposer {

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

	String variablemodel = "SalesOrder";
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

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		comp.setVariable(variablemodel, this, true);

		setData2(getCustomerTable());
		ListModel sm = new SimpleListModel(getData2());
		sodatabaseamt= new double[100];
		sopaydetails = new String[100];
		soamount = new double[100];
		if (comp.getId().equals("ReceiptAddNew")) {

			// lstcustomerdetailmodify.setModel(new
			// SimpleListModel(getCustomerTable()));
			// DONE Bill Number and A/C Year
			SetControls();

		}
		// txtMainCategory.setConstraint(objempty);

	}

	public List<GenericValue> getCustomerTable() {
		return objCommonServices
				.getAllDataTable("VE_Select_tbl_Customer_Master");

	}
	public void onClick$btnRefreshCust(Event e){
		
		lstCustomer.setModel(objCommonServices.PopulateListBox(
				"tbl_Customer_Master", "Cust_Name", "", ""));
	}
	public CB_ReceiptDetails() {
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

	public void onSelect$lstCustomer(Event e) {
		/*
		 * try{ EntityExpr xxp = new EntityExpr("SumPayment",
		 * EntityOperator.GREATER_THAN,new BigDecimal(0.0)); EntityExpr xxp2 =
		 * new EntityExpr("Cust_Id",EntityOperator.EQUALS
		 * ,objCommonServices.GetIdofName("tbl_Customer_Master", "Cust_Name",
		 * bdCustomer.getText(), "Cust_Id")); List a= UtilMisc.toList(xxp,xxp2);
		 * alert("Hi Lstcustomer"); EntityConditionList condList = new
		 * EntityConditionList(a,EntityOperator.AND); //ListModel xm = new
		 * ListModelList
		 * (objCommonServices.ReturnRowGen("VE_Select_tbl_SalesOrder_Payment",
		 * "Cust_Id", objCommonServices.GetIdofName("tbl_Customer_Master",
		 * "Cust_Name", bdCustomer.getText(), "Cust_Id"))); //ListModel xm = new
		 * ListModelList(objCommonServices.ReturnMultipleCondition(
		 * "VE_Select_tbl_SalesOrder_Payment",condList)); ListModel xm = new
		 * ListModelList(objCommonServices.ReturnSingleCondition(
		 * "VE_Select_tbl_SalesOrder_Payment",xxp)); //Its working
		 * lstSalesOrder.setModel(xm); } catch(Exception eeexx) {
		 * alert("ListModel Customer =" +eeexx.getMessage());
		 * alert("Class Error" + objCommonServices.strError); }
		 */
		/*List<GenericValue> StrRow = objCommonServices.ReturnRowGen(
				"VE_Select_tbl_SalesOrder_Payment", "Cust_Id",
				objCommonServices.GetIdofName("tbl_Customer_Master",
						"Cust_Name", bdCustomer.getText(), "Cust_Id"));*/

		
		EntityExpr xxp = new EntityExpr("So_Status",
				 EntityOperator.EQUALS,"APPROVED");
		
		EntityExpr xxp2 =
			  new EntityExpr("Cust_Id",EntityOperator.EQUALS
			 ,objCommonServices.GetIdofName("tbl_Customer_Master", "Cust_Name",
			 bdCustomer.getText(), "Cust_Id"));
		 List a= UtilMisc.toList(xxp,xxp2);
		 EntityConditionList condList = new EntityConditionList(a,EntityOperator.AND);
		
		 
		 List<GenericValue> StrRow = objCommonServices.ReturnMultipleCondition("VE_Select_tbl_SalesOrder_Payment", condList,"-So_Date");
		 
		ListModelList mymodel = new ListModelList();
		for (int i = 0; i < StrRow.size(); i++) {
			
			GenericValue genericValue5 = StrRow.get(i);
			if(Double.valueOf(genericValue5.getString("SumPayment"))>0){
			HashMap mymap = new HashMap();
			mymap.put("So_Nos", genericValue5.getString("So_Nos"));
			mymap.put("SumPayment", genericValue5.getString("SumPayment"));
			mymap.put("So_Id", genericValue5.getString("So_Id"));

			mymodel.add(mymap);
			}
		}
		/*
		 * HashMap mymap = new HashMap(); mymap.put("items",
		 * genericValue5.getString("Cust_Name")); mymap.put("qty",
		 * genericValue5.getString("Cust_Name")); mymap.put("unit",
		 * genericValue5.getString("Cust_Name"));
		 * 
		 * mymodel.add(mymap);
		 */
		lstSalesOrder.setModel(mymodel);
	}

	public void onClick$btnAdjust(Event e) {
		// alert("hi adjust");
		// txtPaidamt.disableClientUpdate(false);
		String soid = "";
		
		if (!txtPaidamt.getText().trim().equals("")) {
			
			if (txttobeAdjust.getText().trim().equals("")) {
				//alert("Inside Paid first if first");
				txttobeAdjust.setText(txtPaidamt.getText());
				
				rowcount = 0;
				txtPaidamt.setDisabled(true);
				length = 1;
				Listitem a2 = lstSalesOrder.getSelectedItem();
				double amount = 0;
				
				List b3 = a2.getChildren();
				
				for (int i = 0; i < b3.size(); i++) {
					Listcell lc = (Listcell) b3.get(i);
					// alert(lc.getLabel());
					if (i == 1) {
						amount = Double.valueOf(lc.getLabel());

					}// get Id
					if (i == 2) {
						soid = lc.getLabel();

					}
					
					// if (i==1){txtMainCatModify.setText(lc.getLabel());}//Set
					// Main
					// Category Name
				}
				
				sopaydetails[rowcount] = soid;
				//alert("Amount"+amount);
				txtBillamt.setText(String.valueOf(amount));
				//alert("after Amount"+amount);
				sodatabaseamt[rowcount]=amount;
				//alert("Inside Paid first if first :after for: before if");
				// soamount[rowcount] =amount;
				double txtadjamt = Double.valueOf(txttobeAdjust.getText());
				//alert("Inside Paid first if first :after for: before if:after double converstion");
				if (txtadjamt > amount) {
					txtadjamt = txtadjamt - amount;
					txttobeAdjust.setText(String.valueOf(txtadjamt));
					soamount[rowcount] = amount;
					

				} else {
					txttobeAdjust.setText("0");
					soamount[rowcount] = txtadjamt;
					
				}
				//alert("Inside Paid first if first :after for: before if:after if:before delete");
				DeleteItem();
			} else if (Double.valueOf(txttobeAdjust.getText()) > 0) {
				length = length + 1;
				rowcount = rowcount + 1;
				// txtPaidamt.setDisabled(true);

				Listitem a2 = lstSalesOrder.getSelectedItem();
				double amount = 0;

				List b3 = a2.getChildren();
				//alert("Inside Paid first if else");
				for (int i = 0; i < b3.size(); i++) {
					Listcell lc = (Listcell) b3.get(i);
					// alert(lc.getLabel());
					if (i == 1) {
						amount = Double.valueOf(lc.getLabel());

					}// get Id
					if (i == 2) {
						soid = lc.getLabel();

					}
					// if (i==1){txtMainCatModify.setText(lc.getLabel());}//Set
					// Main
					// Category Name
				}
				sopaydetails[rowcount] = soid;
				sodatabaseamt[rowcount]=amount;
				// soamount[rowcount] =amount;
				double txtadjamt = Double.valueOf(txttobeAdjust.getText());
				if (txtadjamt > amount) {
					txtadjamt = txtadjamt - amount;
					txttobeAdjust.setText(String.valueOf(txtadjamt));
					soamount[rowcount] = amount;
				} else {
					txttobeAdjust.setText("0");
					soamount[rowcount] = txtadjamt;
				}
				txtBillamt.setText(String.valueOf(Double.valueOf(txtBillamt
						.getText())
						+ amount));
				DeleteItem();
			} else {
				alert("Adjustment done on paid amount");
			}
		} else {
			alert("Enter Paid Amount");
		}
	}

	public void DeleteItem() {
		
		List l2 = lstSalesOrder.getItems();
		ListModelList mymodel;
		mymodel = (ListModelList) lstSalesOrder.getModel();
		int i = 0;

		for (Object object : l2) {
			Listitem a2 = (Listitem) object;
			if (a2.isSelected()) {

				mymodel.remove(i);
				break;
			}
			i++;
		}

		lstSalesOrder.setModel(mymodel);

	}

	
	
	public void onClick$btnSave(Event e) {

		if (!validateControlsNew()) {
				
			return;
		}
		
		
		// ---Sequence Id for Receipt

		Map Chn = new HashMap();
		Chn = UtilMisc.toMap("Sequence_Id", String.valueOf(billseq));

		Map SeqId = new HashMap();
		SeqId = UtilMisc.toMap("Sq_Id", String.valueOf(seqId));
		objCommonServices.updateTable("tbl_Sequence_AcYear", Chn, SeqId);

		// --Receipt Details
		long maxSeqVal = objCommonServices.CustSequenceId(
				"VE_Max_tbl_Receipt_Details", "Receipt_Tag");
		// alert(""+maxSeqVal);
		// alert("Error"+objCommonServices.strError );
		Map row = new HashMap();

		row = UtilMisc.toMap("Receipt_Id", String.valueOf(maxSeqVal),
				"Receipt_Nos", txtReceiptNo.getText().trim(), "Cust_Id", String
						.valueOf(objCommonServices.GetIdofName(
								"tbl_Customer_Master", "Cust_Name", bdCustomer
										.getText(), "Cust_Id")),
				"Receipt_Date", dateReceipt.getText(), "Receipt_Amt",
				txtPaidamt.getText(), "Receipt_Paytype", bdPaymenttype
						.getText(),"Receipt_Status","VOID", "Receipt_Remarks", txtRemarks.getText(),
				"Receipt_Tag", String.valueOf(maxSeqVal), "ac_year", acYear);
		objCommonServices.createTable("tbl_Receipt_Details", row);

		// ---Sales order Payment details
		for (int i = 0; i < length; i++) {
			// alert(txtReceiptNo.getText());
			// alert(sopaydetails[i]);
			// alert(""+soamount[i]);
			// alert("sopay id"+String.valueOf(objCommonServices.CustSequenceId("VE_Max_tbl_SOPayment_Details",
			// "SoPay_Id")+1));
			// Map row = new HashMap();
			// row = UtilMisc.toMap("SoPay_Id",
			// String.valueOf(objCommonServices.CustSequenceId("VE_Max_tbl_SOPayment_Details",
			// "SoPay_Id")) ,"So_Id", sopaydetails[i],
			// "SoPay_TotalAmt",
			// String.valueOf(soamount[i]*-1),"Receipt_Id",txtReceiptNo.getText());

			Map erow = UtilMisc.toMap("SoPay_Id", String
					.valueOf(objCommonServices.CustSequenceId(
							"VE_Max_tbl_SOPayment_Details2", "SoPay_Id")),
					"So_Id", String.valueOf(sopaydetails[i]), "SoPay_TotalAmt",
					String.valueOf(soamount[i] * -1), "Receipt_Id", String
							.valueOf(maxSeqVal));

			objCommonServices.createTable("tbl_SOPayment_Details", erow);
			
			if(soamount[i]==sodatabaseamt[i])
			{
				Map StrValues = UtilMisc.toMap("So_Status","PAID");
				
				Map StrId = UtilMisc.toMap("So_Id", String.valueOf(sopaydetails[i]));
				
				objCommonServices.updateTable("tbl_SalesOrder_Details", StrValues, StrId);
			}
			
		}
		
		alert("Updated");
		clearControlsNew();

		/*
		 * maxSeqVal = objCommonServices.CustSequenceId(
		 * "VE_Max_tbl_SalesOrder_Details", "So_Tag"); // alert(""+maxSeqVal);
		 * // alert("Error"+objCommonServices.strError );
		 * 
		 * 
		 * //---Update tbl_Sequence_AcYear for bill no sequence Map Chn = new
		 * HashMap();
		 * 
		 * Chn = UtilMisc.toMap("Sequence_Id",String.valueOf(billseq));
		 * 
		 * Map SeqId = new HashMap(); SeqId =
		 * UtilMisc.toMap("Sq_Id",String.valueOf(seqId));
		 * objCommonServices.updateTable("tbl_Sequence_AcYear", Chn, SeqId);
		 * 
		 * //--------------- Map row = new HashMap(); row =
		 * UtilMisc.toMap("So_Id",
		 * String.valueOf(maxSeqVal),"So_Nos",txtsalesordernoaddnew.getText(),
		 * "So_Name", "SALESORDER"+txtsalesordernoaddnew.getText(),
		 * "Cust_Id",objCommonServices.GetIdofName("tbl_Customer_Master",
		 * "Cust_Name", bdCustNameAddnew.getText(), "Cust_Id"),
		 * "Sup_Id","1","So_Status","VOID", "So_Date",
		 * dateSalesDateAddnew.getText(),"So_Amt",txtAmountAddnew.getText(),
		 * "So_TaxPer"
		 * ,bdVatPerAddnew.getText(),"So_TaxAmt",txtTaxAmtAddnew.getText
		 * (),"So_AmtAdjust",txtDiscAddnew.getText(),
		 * "So_TotalAmt",txtTotalAmtAddnew
		 * .getText(),"So_Remarks",txtRemarksAddnew
		 * .getText(),"So_Tag",String.valueOf(maxSeqVal), "ac_year",acYear); //
		 * UtilMisc.tom if (validateControlsNew()) { if
		 * (!objCommonServices.CustCheckDuplicate("tbl_SalesOrder_Details",
		 * "So_Nos", txtsalesordernoaddnew.getText().trim())) {
		 * if(UpdateTable(row)==true) { alert("Updated"); clearControlsNew(); }
		 * else { alert("Error" + objCommonServices.strError); } } else {
		 * alert("Entry already exist"); } }
		 */

		try {
			Set<Listitem> a2 = lstSalesOrder.getSelectedItems();

			for (Listitem object : a2) {
				List<Listcell> b3 = object.getChildren();
				Listcell gotit = b3.get(2);
				//alert(gotit.getLabel());
			}

			// alert("Deleted");
		} catch (Exception e2) {
			// alert("fROM btndelete:"+e2.getMessage());

		}

	}

	public void onSelect$lstSalesOrder(Event e) {

	}

	public void SetControls() {
		acYear = objCommonServices.GetACYear();
		ArrayList locala1 = objCommonServices.GetReceiptSequence(acYear);
		seqId = (Long) locala1.get(0);
		billseq = (Long) locala1.get(1) + 1;

		dateReceipt.setValue(objCommonServices.GetFromDate(acYear));
		//dateReceipt.setText("");
		
		txtReceiptNo.setText("RCP" + acYear + "-" + billseq);

		// DONE POPULATE Customer Listbox
		lstCustomer.setModel(objCommonServices.PopulateListBox(
				"tbl_Customer_Master", "Cust_Name", "", ""));

		bdCustomer.setText("--Select--");

		String a[] = new String[] {"Cash", "Cheque" };

		ListModel xxx = new ListModelList(a);

		lstpaymenttype.setModel(xxx);

		bdPaymenttype.setText("");

	}

	public boolean validateControlsNew() {
		String Error = "";
		boolean val;
		// / Error+=
		// objCommonValidations.TextBoxValidate(txtcustomername.getText().trim())?"":"Customer Name cannot be empty";
		Error+=objCommonValidations.TextBoxValidate(dateReceipt.getText().trim())?"":"Select Date of Receipt\n";
		Error+=!(bdCustomer.getText()=="--Select--")?"":"Select Customer\n";
		Error+=objCommonValidations.TextBoxValidate(txtBillamt.getText().trim())?"":"Bill Amount Cannot be Empty\n";
		Error+=objCommonValidations.TextBoxValidate(txtPaidamt.getText().trim())?"":"Paid Amount Cannot be Empty\n";
		Error+=!(bdPaymenttype.getText()=="--Select--")?"":"Select Payment Type\n";
		Error+=(txttobeAdjust.getText().trim().equals("0"))?"":"Amount Paid cannot be more than bill amount\n";
		
		if(Error.equals("")){
		Error+=objCommonServices.verifydate(dateReceipt.getValue(), "RECEIPT")?"":"Receipt Date should be in current accounting year: " + acYear+"\n";
		}
		if (!Error.equals("")) {
			alert(Error);
			return false;
		}else {
			return true;
		}

	}

	public void onClick$btnDelete(Event e) {
		// Set s=((Selectable)lstSoItemsAddnew.getModel()).getSelection();
		// Set s=lstSoItemsAddnew.getSelectedItems();
		// List l2=lstSalesOrder.getItems();
		// ListModelList mymodel;
		// mymodel=(ListModelList) lstSalesOrder.getModel();
		// int i=0;

		Date a = dateReceipt.getValue();
		// a.get
		try {
			Set<Listitem> a2 = lstSalesOrder.getSelectedItems();

			for (Listitem object : a2) {
				List<Listcell> b3 = object.getChildren();
				Listcell gotit = b3.get(2);
				alert(gotit.getLabel());
			}

			// alert("Deleted");
		} catch (Exception e2) {
			// alert("fROM btndelete:"+e2.getMessage());

		}
	}

	public void onClick$btnCancel(Event e) {

		// UpdateSOItems();
		clearControlsNew();

	}

	public void onClick$btnrefresh(Event e) {
		// alert("btnrefresh working" + variableId);
		refreshGrid();

	}

	public void refreshGrid() {
		try {
			setData2(objCommonServices
					.getAllDataTable("VE_Select_tbl_Customer_Master"));
			ListModel sm = new SimpleListModel(getData2());
			lstcustomerdetailmodify.setModel(sm);
		} catch (Exception ex) {
			alert("You are @ maincatregory modify" + ex.toString());
		}
	}

	public void clearControlsNew() {
		// txtcustomername.setText("");
		bdCustomer.setText("--Select--");
		bdPaymenttype.setText("--Select--");
		dateReceipt.setText("");
		txtBillamt.setText("");
		txtPaidamt.setText("");
		txttobeAdjust.setText("");
		txtRemarks.setText("");
		txtPaidamt.setDisabled(false);
		ListModel a = null;
		lstSalesOrder.setModel(a);
		SetControls();
	}

	public void setData2(List<GenericValue> data2) {
		this.data2 = data2;
	}

	public List<GenericValue> getData2() {
		return data2;
	}
}
