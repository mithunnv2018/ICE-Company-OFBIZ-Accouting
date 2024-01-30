package org.ofbiz.ICE.ZkCodeBehind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ofbiz.ICE.ZkServices.ZkCommonServices;
import org.ofbiz.ICE.zkValidation.zkCommonValidations;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.SimpleConstraint;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class CB_SubCategory extends GenericForwardComposer {

	
	public Button btnSave;
	public Button btnCancel;
	
	private ZkCommonServices objCommonServices;
	private zkCommonValidations objCommonValidations;
	
	public Listbox lstMainCategory;
	public Listbox lstSubcategorymodify;
	public Listbox lstMainCategoryModify;

	String variablemodel = "SubCat";
	private List<GenericValue> data2 = null;
	private ListModel model;

	public Button btnrefresh;
	public Button btnSaveModify;
	public Button btnCancelModify;
	
	public String variableId="0";
	public Textbox txtSubCategory;
	public Textbox txtSubCatModify;
	public Label message3;
	private String MainCatId="";
	
	public Bandbox bdModify;
	public Bandbox bdNew;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		/*String aa=comp.getId();
		message3.setValue(message3.getValue() + "id= "+ aa);*/
		comp.setVariable(variablemodel, this, true);
		
		setData2(getSubCategoryTable());
		ListModel sm = new SimpleListModel(getData2());
		
		// txtMainCategory.setConstraint(objempty);
		//Embeded this for testing purpose
		/*List<GenericValue> abc = LocalPop("tbl_MainCategory_Master", "MainCat_Name", "", "");
		//ListModel model=null;
		String temp3[]=new String[abc.size()];
		
		for(int i=0;i<abc.size();i++)
		{
			GenericValue temp2=(GenericValue) abc.get(i);
			temp3[i]=temp2.getString("MainCat_Name");				
		}
		//setModel(new SimpleListModel(temp3));
		//alert(lstMainCategoryModify.toString()+"");
		//message2.setValue(lstMainCategoryModify.toString()+"");
		
		ListModel model3=new SimpleListModel(temp3);*/
		if(comp.getId().equals("subcat2"))
		{
			//lstSubcategorymodify.setModel(sm);
			
			bdModify.setText("--Select--");
			lstMainCategoryModify.setModel(objCommonServices.PopulateListBox("tbl_MainCategory_Master", "MainCat_Name", "", ""));
		}
		if(comp.getId().equals("subcat1"))
		{
			bdNew.setText("--Select--");
			lstMainCategory.setModel(objCommonServices.PopulateListBox("tbl_MainCategory_Master", "MainCat_Name", "", ""));
		}
		
	}
	private List<GenericValue> LocalPop(String tblname,String fldname, String Confldname,String Confldvalue)
	{
		List<GenericValue> abc;
		try 
		{
			GenericDelegator delegator = GenericDelegator.getGenericDelegator("default");
						
			if(Confldname.equals("")||Confldvalue.equals(""))
			{
				abc = delegator.findAll(tblname);
				
			}
			else
			{
				EntityExpr expr = new EntityExpr(Confldname, EntityOperator.EQUALS,Confldvalue);
				abc = delegator.findByCondition(tblname,expr, null, null);				
			}
			
			return abc;
		}
		catch (Exception e) 
		{
			
			return null;
		}
	}
	public CB_SubCategory() {
		objCommonServices = new ZkCommonServices();
		objCommonValidations = new zkCommonValidations();
		//Removed this for testing purpose
		//lstMainCategory.setModel(objCommonServices.PopulateListBox("tbl_MainCategory_Master", "MainCat_Name", "", ""));
		
		
		//lstMainCategoryModify.setModel(getModel());
		
		//lstMainCategoryModify.setModel(objCommonServices.PopulateListBox("tbl_MainCategory_Master", "MainCat_Name", "", "") );
		//lstMainCategory.setModel(model);
		
		//----------------------------------
		//
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
				"VE_Max_tbl_SubCategory_Master", "SubCat_tag");
		// alert(""+maxSeqVal);
		// alert("Error"+objCommonServices.strError );
		Map row = new HashMap();
		
		
		// UtilMisc.tom
		if (validateControlsNew())
		{
			row = UtilMisc.toMap("SubCat_Id",String.valueOf(maxSeqVal),"SubCat_Name",txtSubCategory.getText(), "MainCat_Id",
					objCommonServices.GetIdofName("tbl_MainCategory_Master", "MainCat_Name", lstMainCategory.getSelectedItem().getLabel(), "MainCat_Id"),						
					"Is_Deleted","1","SubCat_tag", String.valueOf(maxSeqVal));
			if (!objCommonServices.CustCheckDuplicate("tbl_SubCategory_Master",
					"SubCat_Name", txtSubCategory.getText().trim())) {
				if (objCommonServices.createTable("tbl_SubCategory_Master", row) == true) {
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
		 Error+= objCommonValidations.TextBoxValidate(txtSubCategory.getText().trim())?"":"Sub Category cannot be empty";
		 Error+= objCommonValidations.TextBoxValidate(bdNew.getText())?"":"Select Main Category";
		 
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
		
		MainCatId = objCommonServices.GetIdofName("tbl_MainCategory_Master", "MainCat_Name",bdModify.getText(),"MainCat_Id");
		
		//alert(MainCatId+"MAin cat Id");
		if(validateControlsModify()){
			Map StrId = UtilMisc.toMap("SubCat_Id",variableId);
			Map StrValues =UtilMisc.toMap("SubCat_Name",txtSubCatModify.getText(),"MainCat_Id",MainCatId );
			
			//can save the same name without changes so can be duplicate
		//if (!objCommonServices.CustCheckDuplicate("tbl_SubCategory_Master","SubCat_Name", txtSubCatModify.getText().trim())) {
			if (objCommonServices.updateTable("tbl_SubCategory_Master", StrValues, StrId) == true) 
			{
				alert("Updated");
				clearControlsModify();
				refreshGrid();
			} else {
				alert("Error" + objCommonServices.strError);
			}
		/*} else {
			alert("Entry already exist");
		}*/
	  }
	}
	//Validate Modify Components
	public boolean validateControlsModify()
	{
		 String Error ="";
		 boolean val;
		 
		 Error+= objCommonValidations.TextBoxValidate(txtSubCatModify.getText().trim())?"":"Select Main Category, ";
		 Error+= objCommonValidations.TextBoxValidate(bdModify.getText())?"":"Select Main Category";
				 
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
		refreshGrid();
		
	}
	public void refreshGrid()
	{
		try {
			setData2(objCommonServices
					.getAllDataTable("VE_Select_tbl_SubCategory_Master"));
			ListModel sm = new SimpleListModel(getData2());
			lstSubcategorymodify.setModel(sm);
		}
		catch (Exception ex) {
			alert("You are @ maincatregory modify"+ex.toString());
		}
	}

	public void onClick$tp_mainCategoryAddnew(Event e) {
//		alert("You are @ maincateogryaddnew");

	}

	public void onClick$tp_mainCategoryModify(Event e) {
		 
	}
	
	public void onSelect$lstSubcategorymodify(Event e)
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
		
		Listitem a= lstSubcategorymodify.getSelectedItem();
		List a2=a.getChildren();
		for(int i=0;i<a2.size();i++)
		{
			Listcell lc=(Listcell) a2.get(i);
			//alert(lc.getLabel());
			if(i==0){variableId=lc.getLabel(); }//get Id
			if (i==1){txtSubCatModify.setText(lc.getLabel());}//Set Main Category Name
			if (i==2){MainCatId=lc.getLabel();}
			if (i==3){
				//lstMainCategoryModify.selectItem((Listitem) lc.getParent())
				int indexofa=lstMainCategoryModify.getItems().indexOf(lc.getLabel());
				
				lstMainCategoryModify.setSelectedIndex(indexofa);
				bdModify.setText(lc.getLabel());
				
				}
		}
		
		//alert(lbmaincategorymodify.getSelectedItem().getLabel());
		
		//alert(e.getData().toString());
	}
	
	public List<GenericValue> getSubCategoryTable() {
		
		return objCommonServices.getAllDataTable("VE_Select_tbl_SubCategory_Master");

	}

	public void clearControlsNew() {		
		txtSubCategory.setText("");
		bdNew.setText("--Select--");
		//lstMainCategory.setModel(objCommonServices.PopulateListBox("tbl_MainCategory_Master", "MainCat_Name", "", ""));
	}
	
	public void clearControlsModify(){
		txtSubCatModify.setText("");
		bdModify.setText("--Select--");
		//lstMainCategoryModify.setModel(objCommonServices.PopulateListBox("tbl_MainCategory_Master", "MainCat_Name", "", ""));
	}

	public void setData2(List<GenericValue> data2) {
		this.data2 = data2;
	}

	public List<GenericValue> getData2() {
		return data2;
	}
	public void setModel(ListModel model) {
		this.model = model;
	}
	public ListModel getModel() {
		return model;
	}
	
}
