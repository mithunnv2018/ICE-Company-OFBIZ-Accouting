/**
 * 
 */
package org.ofbiz.ICEInc.ZkCodeBehind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ofbiz.ICEInc.ZkServices.ZkCommonServices;
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

/**
 * @author Mithun
 * 
 */
public class CB_MainCategory extends GenericForwardComposer {

	public Textbox txtMainCategory;
	public Button btnSave;
	public Button btnCancel;
	private ZkCommonServices objCommonServices;
	public Tabpanel tp_mainCategoryAddnew;
	public Tabpanel tp_mainCategoryModify;
	
	public Listbox lbmaincategorymodify;

	String variablemodel = "mysession2";
	private List<GenericValue> data2 = null;

	public Button btnrefresh;
	public Button btnSaveModify;
	
	public String variableId="0";
	SimpleConstraint objempty = null;

	public Textbox txtMainCatModify;
	
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		 
		comp.setVariable(variablemodel, this, true);
		objempty = new SimpleConstraint(SimpleConstraint.NO_EMPTY,
				"field cannot be empty!");
		setData2(getMainCategoryTable());

		// txtMainCategory.setConstraint(objempty);

	}

	
	public CB_MainCategory() {
		objCommonServices = new ZkCommonServices();

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
				"VE_Max_tbl_MainCategory_Master", "MainCat_tag");
		// alert(""+maxSeqVal);
		// alert("Error"+objCommonServices.strError );
		Map row = new HashMap();

		row = UtilMisc.toMap("MainCat_Id", String.valueOf(maxSeqVal),
				"MainCat_Name", txtMainCategory.getText().trim(), "PrimGrp_Id",
				"1", "Is_Deleted", "0", "MainCat_tag", String
						.valueOf(maxSeqVal));
		// UtilMisc.tom
		if (!objCommonServices.CustCheckDuplicate("tbl_MainCategory_Master",
				"MainCat_Name", txtMainCategory.getText().trim())) {
			if (objCommonServices.createTable("tbl_MainCategory_Master", row) == true) {
				alert("Updated");
				clearControls();
			} else {
				alert("Error" + objCommonServices.strError);
			}
		} else {
			alert("Entry already exist");
		}

	}
	//For Modify
	public void onClick$btnSaveModify(Event e)
	{
		Map StrId = UtilMisc.toMap("MainCat_Id",variableId);
		Map StrValues =UtilMisc.toMap("MainCat_Name",txtMainCatModify.getText());
		
		
		if (!objCommonServices.CustCheckDuplicate("tbl_MainCategory_Master",
				"MainCat_Name", txtMainCatModify.getText().trim())) {
			if (objCommonServices.updateTable("tbl_MainCategory_Master", StrValues, StrId) == true) 
			{
				alert("Updated");
				clearControlsModify();
				refreshGrid();
			} else {
				alert("Error" + objCommonServices.strError);
			}
		} else {
			alert("Entry already exist");
		}
	}
	public void onClick$btnCancel(Event e) {

		clearControls();

	}
	public void onClick$btnrefresh(Event e)
	{
		alert("btnrefresh working" + variableId);
		refreshGrid();
		
	}
	public void refreshGrid()
	{
		try {
			setData2(objCommonServices
					.getAllDataTable("tbl_MainCategory_Master"));
			ListModel sm = new SimpleListModel(getData2());
			lbmaincategorymodify.setModel(sm);
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
	
	public void onSelect$lbmaincategorymodify(Event e)
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
		
		Listitem a= lbmaincategorymodify.getSelectedItem();
		List a2=a.getChildren();
		for(int i=0;i<a2.size();i++)
		{
			Listcell lc=(Listcell) a2.get(i);
			//alert(lc.getLabel());
			if(i==0){variableId=lc.getLabel(); }//get Id
			if (i==1){txtMainCatModify.setText(lc.getLabel());}//Set Main Category Name
		}
		
		//alert(lbmaincategorymodify.getSelectedItem().getLabel());
		
		//alert(e.getData().toString());
	}

	public List<GenericValue> getMainCategoryTable() {
		return objCommonServices.getAllDataTable("tbl_MainCategory_Master");

	}

	public void clearControls() {

		
		txtMainCategory.setText("");
		
		

	}
	public void clearControlsModify(){
		txtMainCatModify.setText("");
	}

	public void setData2(List<GenericValue> data2) {
		this.data2 = data2;
	}

	public List<GenericValue> getData2() {
		return data2;
	}
}
