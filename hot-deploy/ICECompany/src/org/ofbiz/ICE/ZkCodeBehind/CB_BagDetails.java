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

public class CB_BagDetails extends GenericForwardComposer {

	private ZkCommonServices objCommonServices;
	private zkCommonValidations objCommonValidations;

	public long seqId;
	public String acYear;
	public long billseq;
	public ListModelList mymodel;

	public Tabpanel tp_mainCategoryAddnew;
	public Tabpanel tp_mainCategoryModify;

	public Listbox lstcustomerdetailmodify;

	String variablemodel = "BagDetails";
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

	// Modify ID
	public String variableId = "0";

	// -------Add new components--------
	public Bandbox bdCustomer;
	public Listbox lstCustomer;
	public Listbox lstSalesBagDetails;
	public Textbox txtreturnedbags;
	public Datebox datereturned;
	
	
	public Textbox txtsalesordernoaddnew;
	public Bandbox bdCustNameAddnew;
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

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		comp.setVariable(variablemodel, this, true);

		setData2(getCustomerTable());
		ListModel sm = new SimpleListModel(getData2());

		lstCustomer.setModel(objCommonServices.PopulateListBox("tbl_Customer_Master", "Cust_Name", "", ""));
	}

	public CB_BagDetails() {
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

	public void onSelect$lstCustomer(Event e)
	{
		//alert(objCommonServices.GetIdofName("tbl_Customer_Master", "Cust_Name", bdCustomer.getText(), "Cust_Id"));
		//ListModel xxx = new SimpleListModel(objCommonServices.ReturnRowGen("VE_Select_tbl_SalesOrder_Bags", "Cust_Id", objCommonServices.GetIdofName("tbl_Customer_Master", "Cust_Name", bdCustomer.getText(), "Cust_Id")));
		//lstSalesBagDetails.setModel(xxx);
		//ListModel xm = new ListModelList(objCommonServices.ReturnRowGen("VE_Select_tbl_Items_StockIt", "veSo_Id", variableId));
		//lstSoItemsAddnew.setModel(xm);
		
		 List<GenericValue> StrRow = objCommonServices.ReturnRowGen("VE_Select_tbl_SalesOrder_Bags", "Cust_Id", objCommonServices.GetIdofName("tbl_Customer_Master", "Cust_Name", bdCustomer.getText(), "Cust_Id"),"-So_Tag");
		 
			ListModelList mymodel = new ListModelList();
			for (int i = 0; i < StrRow.size(); i++) {
				
				GenericValue genericValue5 = StrRow.get(i);
				if(Double.valueOf(genericValue5.getString("MaxBags"))>0){
				HashMap mymap = new HashMap();
				mymap.put("So_Id", genericValue5.getString("So_Id"));
				mymap.put("So_Nos", genericValue5.getString("So_Nos"));
				mymap.put("MaxBags", genericValue5.getString("MaxBags"));

				mymodel.add(mymap);
				}
			}
			
			lstSalesBagDetails.setModel(mymodel);
		
	}
	public void onClick$btnSave(Event e) {
		//-------------------
		
		if(!validateControlsNew())
		{
			return;
		}
		
		List abc2 = lstSalesBagDetails.getItems();		
		int totalqty=0;
		long totalamount=0;
		int returnedqty = Integer.parseInt(txtreturnedbags.getText());
		
		for(int i=0;i<abc2.size();i++)
		{
			Listitem row= (Listitem)abc2.get(i);
			List local2=row.getChildren();
			String SoId="";
			Map erow;
			int qty=0;
			
			double amount=0;
			for(int	a=0;a<local2.size();a++) {
				Listcell lc=(Listcell) local2.get(a);
				
				if(a==0){SoId = lc.getLabel(); }//SOID column			
				if(a==2){qty = Integer.parseInt(lc.getLabel()); }
				
				
			}
			
			if(returnedqty>0)
			{
			if(returnedqty>qty)
			{
				erow = UtilMisc.toMap("SoBags_Id",String.valueOf(objCommonServices.CustSequenceId("VE_Max_tbl_SOBags_Details", "SoBags_Id")),  "So_Id", String.valueOf(SoId),"SOBags_Nos",String.valueOf(qty*-1),"SoBags_Date",datereturned.getText() );
				objCommonServices.createTable("tbl_SOBags_Details", erow);
				
				returnedqty=returnedqty-qty;
			
				qty=0;
				
			}
			else
			{	
		
				
				erow = UtilMisc.toMap("SoBags_Id",String.valueOf(objCommonServices.CustSequenceId("VE_Max_tbl_SOBags_Details", "SoBags_Id")),  "So_Id", String.valueOf(SoId),"SOBags_Nos",String.valueOf(returnedqty*-1),"SoBags_Date",datereturned.getText() );
				objCommonServices.createTable("tbl_SOBags_Details", erow);
				
				returnedqty=0;		
				
			}
			}
			
			
		}
		clearControlsNew();
		alert("Updated");
				
		//------------------		

	}
public boolean UpdateTable(Map a)
{
	return true;
}
	public boolean validateControlsNew() {
		String Error = "";
			
		
		
		if (lstSalesBagDetails.getModel() != null) {
			Error += lstSalesBagDetails.getModel().getSize() <= 0 ? "No Bags to be returned\n"
					: "";
		} 
		
		
		
		
		boolean val;
		// / Error+=
		Error+=objCommonValidations.TextBoxValidate(txtreturnedbags.getText().trim())?"":"Returned bags cannot be empty\n";
		Error+=objCommonValidations.TextBoxValidate(datereturned.getText())?"":"Date cannot be empty";
		
		if (!Error.equals("")) {
			alert(Error);
			return false;
		} else {
			return true;
		}

	}

	public void onClick$btnCancel(Event e) {

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

	public List<GenericValue> getCustomerTable() {
		return objCommonServices
				.getAllDataTable("VE_Select_tbl_Customer_Master");

	}

	public void clearControlsNew() {
		// txtcustomername.setText("");
		txtreturnedbags.setText("");
		datereturned.setText("");
		bdCustomer.setText("--Select--");
		
		ListModelList a= null; 
		lstSalesBagDetails.setModel(a);
	}

	public void setData2(List<GenericValue> data2) {
		this.data2 = data2;
	}

	public List<GenericValue> getData2() {
		return data2;
	}

}
