package org.ofbiz.ICE.ZkCodeBehind;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.fortuna.ical4j.util.Dates;

import org.ofbiz.ICE.ZkServices.ZkCommonServices;
import org.ofbiz.ICE.zkValidation.zkCommonValidations;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
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

public class CB_SalesOrderModify extends GenericForwardComposer{

	public Window SalesOrderModify;
	private ZkCommonServices objCommonServices;
	private zkCommonValidations objCommonValidations;

	public long seqId;
	public String acYear;
	public long billseq;

	public Tabpanel tp_mainCategoryAddnew;
	public Tabpanel tp_mainCategoryModify;

	public Listbox lstcustomerdetailmodify;

	String variablemodel = "SalesOrderModify";
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
	public Button btnRefreshVat;
	public Button btnRefreshCust;

	// Modify ID
	public String variableId = "0";

	// -------Add new components--------
	public Textbox txtsalesordernoaddnew;
	public Bandbox bdCustNameAddnew;
	public Bandbox bdStatus;
	public Listbox lstStatus;
	public Listbox lstCustNameAddnew;
	public Datebox dateSalesDateAddnew;
	public Textbox txtItemAddnew;
	public Textbox txtQuantityAddnew;
	public Textbox txtUnitAmtAddnew;
	public Listbox lstSoItemsAddnew;
	public Textbox txtAmountAddnew;
	public Bandbox bdVatPerAddnew;
	public Listbox lstVatPerAddnew;
	public Textbox txtTaxAmtAddnew;
	public Textbox txtDiscAddnew;
	public Textbox txtTotalAmtAddnew;
	public Textbox txtRemarksAddnew;
	public Textbox txtIncTaxAmt;
	public Listbox lstSalesOrderModify;
	
	public Textbox txtcustomeraddress;
	public Textbox txtcity;
	public Textbox txtpostalcode;
	public Textbox txtphone;
	public Textbox txtemail;

	// ---Modify Components
	public Textbox txtmodifycustomername;
	public Textbox txtmodifyaddress;
	public Textbox txtmodifycity;
	public Textbox txtmodifyphone;
	public Textbox txtmodifyemail;
	public Textbox txtmodifypostalCode;
	public Textbox txterror;
	public Button btnGridRefresh;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		
		comp.setVariable(variablemodel, this, true);
		
		//ListModel sm = new SimpleListModel(getData2());
		acYear = objCommonServices.GetACYear();
		if (comp.getId().equals("SalesOrderModify")) {

			// lstcustomerdetailmodify.setModel(new
			// SimpleListModel(getCustomerTable()));
			// DONE Bill Number and A/C Year
			//lstSalesOrderModify.setModel(sm);
			SetControls();
			try{
				EntityExpr xxp = new EntityExpr("veSo_Status",
						 EntityOperator.EQUALS,new String("VOID"));
				
				EntityExpr xxp2 = new EntityExpr("veAc_year",
						 EntityOperator.EQUALS,new String(acYear));
						
				 List a= UtilMisc.toList(xxp,xxp2);
				 
				 EntityConditionList condList = new EntityConditionList(a,EntityOperator.AND);
				 
				 
				// ListModelList xas = new ListModelList(objCommonServices.ReturnMultipleCondition("VE_Select_tbl_SalesOrder_Customer", condList));
				 ListModelList xas = new ListModelList( objCommonServices.getAllDataTable("VE_Select_tbl_SalesOrder_Customer","-veSo_Id"));
				 //objCommonServices.getAllDataTable(tablename, OrderBy)
				 setData2(objCommonServices.ReturnMultipleCondition("VE_Select_tbl_SalesOrder_Customer", condList,"-veSo_Tag"));
				 //txterror.setText("Size =" +objCommonServices.ReturnSingleCondition("VE_Select_tbl_SalesOrder_Customer", xxp).size());
				 //lstSalesOrderModify.setModel(xas);
				}
				catch(Exception exxx)
				{
					//alert(objCommonServices.strError);
					//txterror.setText(txterror.getText()+ exxx.getMessage());
				}
			 
		
		}
		// txtMainCategory.setConstraint(objempty);

	}

	
	public void onClick$btnGridRefresh(Event e)
	{
		
		
	
		EntityExpr xxp = new EntityExpr("veSo_Status",
				 EntityOperator.EQUALS,new String("VOID"));
		
		EntityExpr xxp2 = new EntityExpr("veAc_year",
				 EntityOperator.EQUALS,new String(acYear));
				
		 List a= UtilMisc.toList(xxp,xxp2);
		 
		 EntityConditionList condList = new EntityConditionList(a,EntityOperator.AND);
		 
		 
		// ListModelList xas = new ListModelList(objCommonServices.ReturnMultipleCondition("VE_Select_tbl_SalesOrder_Customer", condList));
		 ListModelList xas = new ListModelList( objCommonServices.ReturnMultipleCondition("VE_Select_tbl_SalesOrder_Customer", condList,"-veSo_Id"));
		 setData2(objCommonServices.ReturnMultipleCondition("VE_Select_tbl_SalesOrder_Customer", condList,"-veSo_Id"));
			 lstSalesOrderModify.setModel(xas);
			// txterror.setText("Size =" +objCommonServices.ReturnSingleCondition("VE_Select_tbl_SalesOrder_Customer", xxp).size());
			 //lstSalesOrderModify.setModel(xas);
			
	}
	public CB_SalesOrderModify() {
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
	public void onClick$btnRefreshCust(Event e)
	{
		lstCustNameAddnew.setModel(objCommonServices.PopulateListBox(
				"tbl_Customer_Master", "Cust_Name", "", ""));
		
		bdCustNameAddnew.setText("--Select--");		
	}
	public void onClick$btnRefreshVat(Event e)
	{
		lstVatPerAddnew.setModel(objCommonServices.PopulateListBox(
				"tbl_SalesTax_Details", "St_Percentage", "", ""));
		bdVatPerAddnew.setText("--Select--");
	}
	
	public void onClick$btnCalculate(Event e) {

		CalculateSOItems();
		CalculateSOItemsTwo();

	}
	public void onClick$btnCancelModify(Event e) {

		clearControlsModify();

	}
	public boolean UpdateBagsDetails(int SOBags_Nos)
	{
		
		Map erow = UtilMisc.toMap("SoBags_Id",String.valueOf(objCommonServices.CustSequenceId("VE_Max_tbl_SOBags_Details", "SoBags_Id")),  "So_Id", String.valueOf(maxSeqVal),"SOBags_Nos",String.valueOf(SOBags_Nos),"SoBags_Date",dateSalesDateAddnew.getText());
		
		if(objCommonServices.createTable("tbl_SOBags_Details", erow)){
			return true;
		}
		else{
			alert(objCommonServices.strError);
			return false;
		}
	}
	public boolean UpdatePaymentDetails(double SoPay_TotalAmt)
	{
		
		Map erow = UtilMisc.toMap("SoPay_Id",String.valueOf(objCommonServices.CustSequenceId("VE_Max_tbl_SOPayment_Details", "SoPay_Id")),  "So_Id", String.valueOf(maxSeqVal),"SoPay_TotalAmt",String.valueOf(SoPay_TotalAmt),"Receipt_Id","0");
		
		if(objCommonServices.createTable("tbl_SOPayment_Details", erow)){
			return true;
		}
		else{
			return false;
		}
	}
	
	public void CalculateSOItems()
	{
		List abc2 = lstSoItemsAddnew.getItems();
		double amount=0;
		double totalamt=0;
		for(int i=0;i<abc2.size();i++)
		{
			Listitem row= (Listitem)abc2.get(i);
			List local2=row.getChildren();
			
			double qty=0;
			
			
			for(int	a=0;a<local2.size();a++) {
				Listcell lc=(Listcell) local2.get(a);
				
				//if(a==0){items = lc.getLabel(); }//items column
				
				if(a==1){
					qty = Double.parseDouble(lc.getLabel());				
				}//qty column
				
				if(a==2){amount =  Double.parseDouble(lc.getLabel());
				
				
				}//unit amount column
				
			}	
			totalamt = totalamt + (amount*qty);
			
		}
		txtAmountAddnew.setText(String.valueOf(totalamt));
	}
	
	/* taxamount=(Amount*vatpercentage)/100
	 * inc tax amount= taxamount+Amount
	 * totalamount=Incof tax amnount - discount
	 */
	public void CalculateSOItemsTwo()
	{
		if(txtDiscAddnew.getText().equals(""))
		{
			txtDiscAddnew.setText("0");
			
		}
		if(bdVatPerAddnew.getText().equals("--Select--"))
		{
			alert("Select VAT Percentage");
			bdVatPerAddnew.setFocus(true);
			return;
		}
		
		double amount2=Double.parseDouble(txtAmountAddnew.getText());
		double vatpercentage2=Double.parseDouble(bdVatPerAddnew.getValue());
		double taxamount=(amount2*vatpercentage2);
		taxamount=taxamount/100;
		double incloftaxamount=taxamount+amount2;
		double discount2=Double.parseDouble(txtDiscAddnew.getText());
		double totalamount=incloftaxamount-discount2;
		txtTaxAmtAddnew.setText(String.valueOf(taxamount));
		
		txtIncTaxAmt.setText(String.valueOf(incloftaxamount));
		
		txtTotalAmtAddnew.setText(String.valueOf(totalamount));
		
	}
	public void DeletePreviousRecords()
	{
		
		
		objCommonServices.DeleteConditionalRecords("tbl_SOItems_Details","So_Id",String.valueOf(maxSeqVal));
		objCommonServices.DeleteConditionalRecords("tbl_SOBags_Details","So_Id",String.valueOf(maxSeqVal));
		objCommonServices.DeleteConditionalRecords("tbl_SOPayment_Details","So_Id",String.valueOf(maxSeqVal));
		
	}
	public boolean UpdateSOItems()
	{
		//Listitem a= lstcustomerdetailmodify.getSelectedItem();
		
		DeletePreviousRecords();
		List abc2 = lstSoItemsAddnew.getItems();		
		int totalqty=0;
		double totalamount=0;
		
		try{
		for(int i=0;i<abc2.size();i++)
		{
			
			Listitem row= (Listitem)abc2.get(i);
			List local2=row.getChildren();
			String items="";
			int qty=0;
			
			double amount=0;
			for(int	a=0;a<local2.size();a++) {
				Listcell lc=(Listcell) local2.get(a);
				
				if(a==0){items = lc.getLabel(); }//items column
				if(a==1){qty = Integer.parseInt(lc.getLabel()); 
				totalqty = totalqty + qty;
				}//qty column
				if(a==2){amount =  Double.valueOf(lc.getLabel());
				
				}//unit amount column
				
			}
			
			Map erow = UtilMisc.toMap("SoItems_Id",String.valueOf(objCommonServices.CustSequenceId("VE_Max_tbl_SOItems_Details", "SoItems_Id")),  "So_Id", String.valueOf(maxSeqVal),
					"StockIt_Id",String.valueOf(objCommonServices.GetIdofName("tbl_StockItem_Master", "StockIt_Name",String.valueOf(items), "StockIt_Id")),"SOItems_Qty",String.valueOf(qty),"SOItems_Amt",String.valueOf(amount) );
			
			objCommonServices.createTable("tbl_SOItems_Details", erow);
			
		}
		
		UpdateBagsDetails(totalqty);
		
		UpdatePaymentDetails(Double.parseDouble(txtTotalAmtAddnew.getText()));
		return true;
		}
		catch(Exception ex)
		{
			alert(ex.getMessage());
			return false;
		}
		
	}
	
	public void onClickbtnSoItemAdndmn(Event e) {
		// List<GenericValue> aa = UtilMisc.toMap("Items",
		// txtItemAddnew.getText(), "Quantity",txtQuantityAddnew.getText(),
		// "UnitAmount",txtUnitAmtAddnew.getText();
		txtAmountAddnew.setText("0");
		List abc2 = lstSoItemsAddnew.getItems();
	
		String val1 = txtItemAddnew.getText();
		String val2 = txtQuantityAddnew.getText();
		String val3 = txtUnitAmtAddnew.getText();

		//
		ArrayList al2 = new ArrayList();
		if (abc2 != null && abc2.size() > 0)

		{
			// HashMap<String,String> map=new HashMap<String,String>();

			/*
			 * for(int i=0;i<abc2.size();i++) { Listitem row=
			 * (Listitem)abc2.get(i); List local2=row.getChildren(); for(int
			 * a=0;a<local2.size();i++) { HashMap<String,String> map=new
			 * HashMap<String,String>(); Listcell lc=(Listcell) local2.get(i);
			 * //alert(lc.getLabel()); if(i==0){map.put("items", lc.getLabel());
			 * }//get Id if(i==1){map.put("qty", lc.getLabel()); }
			 * if(i==2){map.put("unit", lc.getLabel()); } al2.add(map);
			 * 
			 * //if (i==1){txtMainCatModify.setText(lc.getLabel());}//Set Main
			 * Category Name }
			 * 
			 * }
			 */
			try {
				HashMap<String, String> map2 = new HashMap<String, String>();
				map2.put("items", val1);
				map2.put("qty", val2);
				map2.put("unit", val3);

				// Listitem xxx= new Listitem().setParent(new
				// Listcell("items",val1));
				// Listcell a1=
				// Listcell a2=
				// Listcell a3=
				abc2.add(map2);
				ListModel xyz = new SimpleListModel(abc2);
				lstSoItemsAddnew.setModel(xyz);
			} catch (Exception eee) {
				alert("Multiple" + eee.getMessage());
			}
			// al2.add(map2);
		} else {
			try {
				HashMap<String, String> map3 = new HashMap<String, String>();
				map3.put("items", val1);
				map3.put("qty", val2);
				map3.put("unit", val3);
				al2.add(map3);
				ListModel mod2 = new SimpleListModel(al2);
				lstSoItemsAddnew.setModel(mod2);
			} catch (Exception eee) {
				alert("Single" + eee.getMessage());
			}
		}

		// for multiple rows

		// List<GenericValue> = new ArrayList();

		// ListModel lstMpop = new SimpleListModel(UtilMisc.toMap("Items",
		// txtItemAddnew.getText(), "Quantity",txtQuantityAddnew.getText(),
		// "UnitAmount",txtUnitAmtAddnew.getText()));
	}

	//Add button functionality
	public void onClick$btnSoItemAdd(Event e) {

		if (validateControlsAdd() == false) {
			// alert("You have to enter number only");
			return;
		}
		
		
		String val1 = txtItemAddnew.getText();
		String val2 = txtQuantityAddnew.getText();
		String val3 = txtUnitAmtAddnew.getText();
		ListModelList mymodel;

		try {
			mymodel = (ListModelList) lstSoItemsAddnew.getModel();
			if (mymodel == null) {

				mymodel = new ListModelList();
			}
			HashMap mymap = new HashMap();
			mymap.put("veStockIt_Name", val1);
			mymap.put("veSOItems_Qty", val2);
			mymap.put("veSOItems_Amt", val3);

			mymodel.add(mymap);
			// mymodel.set(++curindex, mymap);
			lstSoItemsAddnew.setModel(mymodel);
			 

		} catch (Exception ez) {
			alert("testThis " + ez.getMessage());
		}

	}
	/*
	 * check if items qunatity and unitaqmount have a number.
	 */
	public boolean validateControlsAdd() {
		String Error = "";
		// boolean val;
		Error += objCommonValidations.validateIsNumber(txtQuantityAddnew
				.getText().trim()) ? "" : "Quantity should be numerical value";
		Error += objCommonValidations.validateIsNumber(txtUnitAmtAddnew
				.getText().trim()) ? ""
				: "Unit Amount should be numerical value";

		if (!Error.equals("")) {
			alert(Error);
			return false;
		} else {
			return true;
		}

	}
	// For Add New
	public void onClick$btnSave(Event e) {
		
		/*maxSeqVal = objCommonServices.CustSequenceId(
				"VE_Max_tbl_SalesOrder_Details", "So_Tag");
		*/
		
		// alert(""+maxSeqVal);
		// alert("Error"+objCommonServices.strError );
		
		if (!validateControlsNew()) {

			return;
		}
		//Re-Calculate the amount according to Vat % if user doent press calculate button
		CalculateSOItems();
		CalculateSOItemsTwo();
		
		
		/*Calendar c=Calendar.getInstance(Locale.UK);
		//c.set(dateSalesDateAddnew., 1, 17);
		c.setTime(dateSalesDateAddnew.getValue());
		Date a = new Date(dateSalesDateAddnew.getText());
		alert(" textbox date= "+dateSalesDateAddnew.getText());
		
		SimpleDateFormat sf=new SimpleDateFormat("yyyy:MM:dd");
		
		c=sf.getCalendar();
		c.setTime(a);*/
		
		/*alert("Month=" + a.getMonth());
		alert("year=" + c.get(Calendar.YEAR));
		alert("Day=" +  a.getDate());
		alert("Format ok"+c.getTime().toString());
		alert("OFBIZ FORMAT"+String.valueOf(UtilDateTime.toDate("" + (c.get(Calendar.MONTH)+1)+"/" + c.get(Calendar.DAY_OF_MONTH)+ "/" +  c.get(Calendar.YEAR)+ " " + "00:00:00")));*/
		//alert("OFBIZ FORMAT"+String.valueOf(UtilDateTime.toDate(""+c.get(Calendar.YEAR) + "-"  + (c.get(Calendar.MONTH)+1)+"-" + c.get(Calendar.DAY_OF_MONTH)+ "00:00:00")));
		
		//String.valueOf(c.get(Calendar.YEAR) + "-"  + (c.get(Calendar.MONTH)+1)+"-" + c.get(Calendar.DAY_OF_MONTH)+ "00:00:00")
//		int a6=1;
//		if(a6==1)return;
		//c.getTime();
		//---Update tbl_Sequence_AcYear for bill no sequence
		
		Map StrId = UtilMisc.toMap("So_Id",String.valueOf(maxSeqVal));
		Map StrValues =UtilMisc.toMap("Cust_Id",objCommonServices.GetIdofName("tbl_Customer_Master", "Cust_Name", bdCustNameAddnew.getText(), "Cust_Id"),
				 "So_Status",bdStatus.getText(), "So_Date", dateSalesDateAddnew.getText()  ,"So_Amt",txtAmountAddnew.getText(),
				 "So_TaxPer",bdVatPerAddnew.getText(),"So_TaxAmt",txtTaxAmtAddnew.getText(),"So_AmtAdjust",txtDiscAddnew.getText(),
				 "So_TotalAmt",txtTotalAmtAddnew.getText(),"So_Remarks",txtRemarksAddnew.getText(),
				 "ac_year",acYear);
		
		//---------------		
			
				if(UpdateTable(StrValues,StrId)==true) {
					//alert("Disc" +txtDiscAddnew.getText());
					//objCommonServices.updateTable("tbl_SalesOrder_Details", UtilMisc.toMap("So_AmtAdjust",txtDiscAddnew.getText()),StrId);
					alert("Updated");
					clearControlsNew();
				} else {
					alert("Error: " + objCommonServices.strError);
				}			
				
		 

	}

	public void SetControls()
	{
		/*acYear = objCommonServices.GetACYear();
		ArrayList locala1 = objCommonServices.GetBillSequence(acYear);
		seqId = (Long) locala1.get(0);
		billseq = (Long) locala1.get(1) + 1;

		txtsalesordernoaddnew.setText("BE" + acYear + "-" + billseq);*/

		// DONE POPULATE Customer Listbox
		lstCustNameAddnew.setModel(objCommonServices.PopulateListBox(
				"tbl_Customer_Master", "Cust_Name", "", ""));

		// DONE POPulate VAT Percentage in listbox
		lstVatPerAddnew.setModel(objCommonServices.PopulateListBox(
				"tbl_SalesTax_Details", "St_Percentage", "", ""));
		bdCustNameAddnew.setText("--Select--");
		bdVatPerAddnew.setText("--Select--");
		
		String a[] =new String[]{"VOID","APPROVED","PAID","CANCELED"};
		ListModel xyz = new ListModelList(a);
		
		lstStatus.setModel(xyz);

	}
	
	private boolean UpdateTable(Map StrValues,Map StrId)
	{
		
		if (objCommonServices.updateTable("tbl_SalesOrder_Details", StrValues,StrId) == true)
		{
			if(UpdateSOItems()){
			return true;
			}
			
		}
		return false;
	}
	// Vaidate Add New Components
	public boolean validateControlsNew() {
		String Error = "";
		boolean val;
		// / Error+=
		// objCommonValidations.TextBoxValidate(txtcustomername.getText().trim())?"":"Customer Name cannot be empty";

		if (lstSoItemsAddnew.getModel() != null) {
			Error += lstSoItemsAddnew.getModel().getSize() <= 0 ? "Add an item to items list \n"
					: "";
		} else {
			Error += "Add an item to items list \n";
		}
		// alert("model="+
		// lstSoItemsAddnew.getModel().getSize()+"rows "+lstSoItemsAddnew.getRows());
		// Error+=lstSoItemsAddnew.getRows()<=0?"Add an item to items list \n":"";
		Error += objCommonValidations.validateForSelect(bdCustNameAddnew
				.getText().trim()) ? "" : "Select a valid customer name \n";
		Error += objCommonValidations.TextBoxValidate(dateSalesDateAddnew
				.getText()) ? "" : "Select a valid Sales Date \n";
		Error += objCommonValidations.validateForSelect(bdVatPerAddnew
				.getText().trim()) ? "" : "Select a valid Percentage \n";
		/*txtDiscAddnew.setText(objCommonValidations
				.TextBoxValidate(txtDiscAddnew.getText().trim()) ? "0"
				: txtDiscAddnew.getText());
*/
		Error+=objCommonValidations.TextBoxValidate(txtAmountAddnew.getText().trim())?"":"Amount Cannot be empty, Please press caluclate button\n";
		//Error+=!(Integer.valueOf(txtAmountAddnew.getText().trim())==0)?"":"Amount cannot be zero\n";
		
		if(Error.equals("")){
			Error+=objCommonServices.verifydate(dateSalesDateAddnew.getValue(), "BILL")?"":"Sale Date should be in current accounting year: " + acYear+"\n";
		}
		//Error+=objCommonValidations.TextBoxValidate(txtTotalAmtAddnew.getText().trim())?"":"Total Amount Cannot be blank\n";
		//Error+=!(Integer.valueOf(txtTotalAmtAddnew.getText().trim())==0)?"":"Total Amount cannot be zero\n";
		
		
		if (!Error.equals("")) {
			alert(Error);
			return false;
		} else {
			return true;
		}

	}

	public void onClick$btnDelete(Event e)
	{
		//Set s=((Selectable)lstSoItemsAddnew.getModel()).getSelection();
		//Set s=lstSoItemsAddnew.getSelectedItems();
		List l2=lstSoItemsAddnew.getItems();
		ListModelList mymodel;
		mymodel=(ListModelList) lstSoItemsAddnew.getModel();
		int i=0;
		try
		{
		for (Object object : l2) {
			Listitem a2=(Listitem)object;
			
			if(a2.isSelected())
			{
				
				mymodel.remove(i);
				break;
			}
			i++;
		}
		
		lstSoItemsAddnew.setModel(mymodel);
 
		//alert("Deleted");
		}
		catch(Exception e2)
		{
			//alert("fROM btndelete:"+e2.getMessage());
		
		}
	}
	// For Modify
	public void onClick$btnSaveModify(Event e) {
		Map StrId = UtilMisc.toMap("Cust_Id", variableId);
		Map StrValues = UtilMisc.toMap("Cust_Name", txtmodifycustomername
				.getText(), "Cust_Address", txtmodifyaddress.getText(),
				"Cust_City", txtmodifycity.getText(), "Cust_PostalCode",
				txtmodifypostalCode.getText(), "Cust_PhNo", txtmodifyphone
						.getText(), "Cust_EmailId", txtmodifyemail.getText());

		if (validateControlsModify()) {
			if (objCommonServices.updateTable("tbl_Customer_Master", StrValues,
					StrId) == true) {
				alert("Updated");
				clearControlsModify();
				refreshGrid();
			} else {
				alert("Error" + objCommonServices.strError);
			}

		}
	}

	// Validate Modify Components
	public boolean validateControlsModify() {
		String Error = "";
		boolean val;

		Error += objCommonValidations.TextBoxValidate(txtmodifycustomername
				.getText().trim()) ? "" : "Enter Customer Name";

		if (!Error.equals("")) {
			alert(Error);
			return false;
		} else {
			return true;
		}

	}

	public void onClick$btnCancel(Event e) {

		//UpdateSOItems();
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

	/*
	 * public void onClick$tp_mainCategoryAddnew(Event e) { //
	 * alert("You are @ maincateogryaddnew");
	 * 
	 * }
	 */

	public void onClick$tp_mainCategoryModify(Event e) {

	}

	public void onSelect$lstcustomerdetailmodify(Event e) {
		// Set it= lbmaincategorymodify.getSelectedItems();
		// for (Object obj : it) {
		// Listitem d=(Listitem)obj;
		//			
		// //alert("object iterate ="+d.getLabel());
		// alert("my child ="+d.getFirstChild().toString());
		//			
		//			
		// }

		Listitem a = lstcustomerdetailmodify.getSelectedItem();
		List a2 = a.getChildren();
		for (int i = 0; i < a2.size(); i++) {
			Listcell lc = (Listcell) a2.get(i);
			// alert(lc.getLabel());
			if (i == 0) {
				variableId = lc.getLabel();
			}// get Id
			// if (i==1){txtMainCatModify.setText(lc.getLabel());}//Set Main
			// Category Name
		}

		List<GenericValue> StrRow = objCommonServices.ReturnRowGen(
				"tbl_Customer_Master", "Cust_Id", variableId);

		GenericValue genericValue5 = StrRow.get(0);

		txtmodifycustomername.setText(genericValue5.getString("Cust_Name"));
		txtmodifyaddress.setText(genericValue5.getString("Cust_Address"));
		txtmodifycity.setText(genericValue5.getString("Cust_City"));
		txtmodifyemail.setText(genericValue5.getString("Cust_EmailId"));
		txtmodifyphone.setText(genericValue5.getString("Cust_PhNo"));
		txtmodifypostalCode.setText(genericValue5.getString("Cust_PostalCode"));

		// alert(lbmaincategorymodify.getSelectedItem().getLabel());

		// alert(e.getData().toString());
	}

	public List<GenericValue> getSalesOrderCustomerTable() {
		
		EntityExpr xxp = new EntityExpr("veSo_Status",
				 EntityOperator.EQUALS,"VOID");
		
		EntityExpr xxp2 = new EntityExpr("veAc_year",
				 EntityOperator.EQUALS,acYear);
				
		 List a= UtilMisc.toList(xxp,xxp2);
		 
		 EntityConditionList condList = new EntityConditionList(a,EntityOperator.AND);
		 
		 return objCommonServices.ReturnSingleCondition("VE_Select_tbl_SalesOrder_Customer", xxp2);
		 
		/*return objCommonServices
				.getAllDataTable("VE_Select_tbl_SalesOrder_Customer");*/

	}

	public void clearControlsNew() {
		// txtcustomername.setText("");
		txtAmountAddnew.setText("");
		txtDiscAddnew.setText("");
		txtIncTaxAmt.setText("");
		//txtItemAddnew.setText("");
		txtQuantityAddnew.setText("");
		txtRemarksAddnew.setText("");
		txtsalesordernoaddnew.setText("");
		txtTaxAmtAddnew.setText("");
		txtTotalAmtAddnew.setText("");
		txtUnitAmtAddnew.setText("");	
		bdCustNameAddnew.setText("");
		bdVatPerAddnew.setText("");
		dateSalesDateAddnew.setText("");
		bdStatus.setText("--Select--");
		ListModelList mymodel=null;
		lstSoItemsAddnew.setModel(mymodel);	
		SetControls();
	}

	public void clearControlsModify() {
		// txtMainCatModify.setText("");
		try {
			txtmodifyaddress.setText("");
			txtmodifycity.setText("");
			txtmodifycustomername.setText("");
			txtmodifyemail.setText("");
			txtmodifyphone.setText("");
			txtmodifypostalCode.setText("");
		} catch (Exception sss) {
			alert(sss.getMessage());
		}
	}

	public void setData2(List<GenericValue> data2) {
		this.data2 = data2;
	}

	public List<GenericValue> getData2() {
		return data2;
	}
	public void onSelect$lstSalesOrderModify(Event e)
	{
		Listitem a= lstSalesOrderModify.getSelectedItem();
		List a2=a.getChildren();
		for(int i=0;i<a2.size();i++)
		{
			Listcell lc=(Listcell) a2.get(i);
			//alert(lc.getLabel());
			if(i==0){
				variableId=lc.getLabel(); 
				maxSeqVal=Long.valueOf(lc.getLabel());
			}
			//get Id
			//if (i==1){txtMainCatModify.setText(lc.getLabel());}//Set Main Category Name
		}
		
		List<GenericValue> StrRow = objCommonServices.ReturnRowGen("VE_Select_tbl_SalesOrder_Customer", "veSo_Id", variableId);
		
		GenericValue genericValue5 = StrRow.get(0);
		
		//txtmodifycustomername.setText(genericValue5.getString("Cust_Name"));
		//---------Populate Form--------------------
		
		txtsalesordernoaddnew.setText(genericValue5.getString("veSo_Nos"));
		bdCustNameAddnew.setText(genericValue5.getString("veCust_Name"));
		
		txtAmountAddnew.setText(genericValue5.getString("veSo_Amt"));
		bdVatPerAddnew.setText(genericValue5.getString("veSo_TaxPer"));
		txtTaxAmtAddnew.setText(genericValue5.getString("veSo_TaxAmt"));
		txtTotalAmtAddnew.setText(genericValue5.getString("veSo_TotalAmt"));
		txtDiscAddnew.setText(genericValue5.getString("veSo_AmtAdjust"));
		txtRemarksAddnew.setText(genericValue5.getString("veSo_Remarks"));
		txtIncTaxAmt.setText(String.valueOf(genericValue5.getDouble("veSo_Amt")+genericValue5.getDouble("veSo_TaxAmt")));
		bdStatus.setText(genericValue5.getString("veSo_Status"));
				
		//Have to convert date format----------
		//dateSalesDateAddnew.setText(genericValue5.getString("veSo_Date"));
		dateSalesDateAddnew.setValue(genericValue5.getDate("veSo_Date"));
		//------------------------------------------VE_Select_tbl_SalesOrder_Items
		
		ListModel xm = new ListModelList(objCommonServices.ReturnRowGen("VE_Select_tbl_Items_StockIt", "veSo_Id", variableId));
		lstSoItemsAddnew.setModel(xm);
	}
}
