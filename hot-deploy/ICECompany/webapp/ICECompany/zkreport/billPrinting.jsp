<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="org.ofbiz.ICE.ZkServices.*"%>
<%@page import="org.ofbiz.entity.GenericValue"%>
<%@page import="java.util.List"%>
<%@page import="org.ofbiz.entity.condition.EntityConditionList"%>
<%@page import="org.ofbiz.entity.condition.EntityExpr"%>
<%@page import="org.ofbiz.entity.condition.EntityOperator"%>
<%@page import="org.ofbiz.base.util.UtilMisc"%>
<%@page import="java.text.DecimalFormat"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
 <style type="text/css">
        .style1
        {
            height: 68px;
        }
        .style2
        {
            height: 75px;
            text-align: center;
        }
        .style3
        {
            height: 68px;
            width: 363px;
        }
        .style4
        {
            width: 363px;
        }
        .style5
        {
            height: 68px;
            width: 254px;
        }
        .style6
        {
            text-align: left;
        }
        .style7
        {
            width: 148px;
        }
        .style8
        {
            width: 110px;
        }
        .style9
        {
            width: 298px;
        }
        .style10
        {
            width: 151px;
        }
        .style11
        {
            width: 168px;
            text-align: right;
        }
        .style12
        {
            width: 168px;
            text-align: center;
        }
    </style>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body style="font-size: 12pt">
<%String args = (String)session.getAttribute("SalesOrderNo"); %>

<% %>
<br></br>
<%ZkCommonServices objCommonServices = new ZkCommonServices(); %>
<%!List<GenericValue> mygen; %>
<% //mygen = objCommonServices.ReturnRowGen("VE_Select_tbl_SalesOrder_Customer","veSo_Nos", args);  %>
<%mygen = objCommonServices.getAllDataTable("VE_Select_tbl_SalesOrder_Customer"); %>



<%for(int j=0;j<mygen.size();j++){ %>
<%GenericValue mygenval= mygen.get(j);%>
<table style="width:100%; margin-bottom: 290px;">
    <tr>
        <td class="style2" colspan="3" style="height: 25px">
            <strong><span style="font-family: Verdana">
                BABIBAI ENTERPRISE</span></strong></td>
        <td class="style2" colspan="1" style="height: 25px">
        </td>
    </tr>
        <tr>
            <td class="style2" colspan="3" style="height: 25px">
                <span style="font-size: 10pt; font-family: Verdana"><strong>
                Raghukul Bldg, Gala No.1 , Ground Floor, Thane(E)-400603</strong></span></td>
            <td class="style2" colspan="1" style="height: 25px">
            </td>
        </tr>
    <tr>
        <td class="style2" colspan="3" style="height: 25px; text-align: right">
            <strong><span style="font-size: 10pt; font-family: Verdana">
                Ph.No: 25982223 </span></strong>
        </td>
        <td class="style2" colspan="1" style="height: 25px">
        </td>
    </tr>
    <tr>
        <td class="style2" colspan="3" style="height: 25px; text-align: center">
            <hr />
                <span style="font-family: Verdana; text-decoration: underline"><strong>TAX INVOICE</strong></span></td>
        <td class="style2" colspan="1" style="height: 25px">
        </td>
    </tr>
        <tr>
            <td class="style5" style="width: 167px; text-align: right;">
            </td>
            <td class="style3" style="width: 332px; text-align: right;">
            </td>
            <td class="style1" style="text-align: right; width: 283px;">
                <table style="width: 100%; margin-left: 0px;">
                    <tr>
                        <td class="style7" style="width: 144px">
                            <span style="font-size: 10pt; font-family: Verdana"><strong>Bill No.</strong></span></td>
                        <td><B><%=mygenval.getString("veSo_Nos")%>
                            &nbsp;</B></td>
                    </tr>
                    <tr>
                        <td class="style7" style="width: 144px">
                            <span style="font-size: 10pt; font-family: Verdana">
                            Bill Date: </span>
                        </td>
                        <td><%=mygenval.getString("veSo_Date") %>
                            &nbsp;</td>
                    </tr>
                    <tr>
                        <td class="style7" style="width: 144px">
                            <span style="font-size: 10pt; font-family: Verdana">
                            Tin No.</span></td>
                        <td>
                            <span style="font-size: 10pt; font-family: Verdana">2796071551V</span>&nbsp;</td>
                    </tr>
                </table>
            </td>
            <td class="style1" style="text-align: right">
            </td>
        </tr>
        <tr>
            <td class="style6" colspan="3">
                <table style="width: 50%;">
                    <tr>
                        <td style="height: 21px">
                            <span style="font-size: 10pt; font-family: Verdana">
                            To,</span></td>
                        <td style="height: 21px">
                            &nbsp;</td>
                        <td style="height: 21px">
                            &nbsp;</td>
                    </tr>
                    <tr>
                        <td colspan="3">
                            &nbsp;<span style="font-family: Verdana"><span style="font-size: 10pt"><b><%=mygenval.getString("veCust_Name") %></b></span></span></td>
                    </tr>
                    <tr>
                        <td colspan="3"><b>
                        
                        <%if(mygenval.getString("veCust_Address")!=null){ %>
                        <%=mygenval.getString("veCust_Address") %></b><%} %>
                            &nbsp;</td>
                    </tr>
                </table>
            </td>
            <td class="style6" colspan="1">
            </td>
        </tr>
        <%List<GenericValue> data3=objCommonServices.ReturnRowGen("VE_Select_tbl_Items_StockIt", "veSo_Id", mygenval.getString("veSo_Id"));
		 %>
        <tr>
            <td class="style6" colspan="3">
                <table style="width:100%;" border=1>
                    <tr>
                        <td class="style8" style="width: 74px; height: 13px;">
                            <span style="font-size: 10pt; font-family: Verdana"><strong>
                            Sr.No.</strong></span></td>
                        <td class="style9" style="width: 460px; height: 13px;">
                            <span style="font-size: 10pt; font-family: Verdana"><strong>
                            Particulars</strong></span></td>
                        <td class="style10" style="width: 82px; text-align: center; height: 13px;">
                            <span style="font-size: 10pt; font-family: Verdana"><strong>
                            Qty</strong></span></td>
                        <td class="style12" style="width: 142px; height: 13px;">
                            <span style="font-size: 10pt; font-family: Verdana"><strong>
                            Unit Amt</strong></span></td>
                        <td style="text-align: center; height: 13px;">
                            <span style="font-size: 10pt; font-family: Verdana"><strong>
                            Amount</strong></span></td>
                    </tr>
                    <%!double multiamt; %>
                    <%!double qty; %>
                    <%double amt;%>
                     <%!double totalamt=0; %>
                    <% for(int i=0;i<data3.size();i++){ %>
                    <%GenericValue myforval = data3.get(i); %>
                    <tr>
                        <td class="style8" style="width: 74px; height: 23px; text-align: center">
                        
                            <span style="font-size: 9pt; font-family: Verdana">1&nbsp;</span></td>
                        <td class="style9" style="width: 460px; height: 23px; text-align: left">
                          <span style="font-size: 9pt; font-family: Verdana">  <%=myforval.getString("veStockIt_Name") %>&nbsp;</span></td>
                        <td class="style10" style="width: 82px; height: 23px; text-align: center">
                         <span style="font-size: 9pt; font-family: Verdana">    <%=myforval.getString("veSOItems_Qty") %>&nbsp;</span></td>
                        <td class="style11" style="width: 142px; height: 23px; text-align: center"><span style="font-size: 9pt; font-family: Verdana">
                             <%=myforval.getString("veSOItems_Amt") %>&nbsp;</span></td>
                             <%qty=Double.valueOf(myforval.getString("veSOItems_Qty")); %>
                             <%amt=Double.valueOf(myforval.getString("veSOItems_Amt")); %>
                             <%multiamt=amt*qty; %>
                            <%totalamt = totalamt+ multiamt;%>
                        <td style="height: 23px"> <span style="font-size: 9pt; font-family: Verdana"><%=multiamt%></span>
                            &nbsp;</td>
                    </tr>
                    <%} %>
                    <tr>
                        <td class="style8" style="width: 74px">
                            &nbsp;</td>
                        <td class="style9" style="width: 460px">
                            &nbsp;</td>
                        <td class="style10" style="width: 82px">
                            &nbsp;</td>
                        <td class="style11" style="width: 142px">
                            <span style="font-size: 9pt; font-family: Verdana">
                            Total:</span></td>
                        <td><span style="font-size: 10pt; font-family: Verdana"><%=multiamt %></span>
                            &nbsp;</td>
                    </tr>
                </table>
            </td>
            <td class="style6" colspan="1">
            </td>
        </tr>
        <tr>
            <td class="style4" colspan="2" style="text-align: left">
                &nbsp;<table width="100%">
                    <tr>
                        <td>
                        </td>
                        <td>
                        </td>
                        <td style="width: 97px">
                        </td>
                    </tr>
                    <tr>
                        <td>
                        </td>
                        <td>
                        </td>
                        <td style="width: 97px">
                        </td>
                    </tr>
                    <tr>
                        <td>
                        </td>
                        <td>
                        </td>
                        <td style="width: 97px">
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <span style="font-size: 7pt; font-family: Verdana"><strong>
                            Note:</strong></span></td>
                        <td>
                        </td>
                        <td style="width: 97px">
                        </td>
                    </tr>
                    <tr>
                        <td colspan="3">
                            <span style="font-size: 7pt; font-family: Verdana">
                            * Prices inclusive of all taxes.</span></td>
                    </tr>
                    <tr>
                        <td colspan="3">
                            <span style="font-size: 7pt; font-family: Verdana">
                            * Stock once sold will not be taken back.</span></td>
                    </tr>
                    <tr>
                        <td colspan="3">
                            <span style="font-size: 7pt; font-family: Verdana">
                            * Check your stock before leaving the premises.</span></td>
                    </tr>
                </table>
                &nbsp;</td>
            <td style="text-align: right; width: 283px;"><table style="width: 100%; margin-left: 0px;">
                <tr>
                    <td class="style7" style="width: 160px; text-align: right;">
                        <span style="font-size: 10pt; font-family: Verdana"><span style="font-size: 9pt">Tax
                            %</span>:</span></td>
                    <td>
                        <%=mygenval.getString("veSo_TaxPer") %>
                        &nbsp;</td>
                </tr>
                <tr>
                    <td class="style7" style="width: 160px; text-align: right;">
                        <span style="font-size: 10pt; font-family: Verdana"><span style="font-size: 9pt">Tax
                            Amount:</span> </span>
                    </td>
                    <td>
                        <%=mygenval.getString("veSo_TaxAmt") %>
                        &nbsp;</td>
                </tr>
                <tr>
                    <td class="style7" style="width: 160px; text-align: right;">
                        <span style="font-size: 9pt; font-family: Verdana">Inclusive of Tax:</span></td>
                    <td><%=multiamt+mygenval.getDouble("veSo_TaxAmt") %>
                        &nbsp;</td>
                </tr>
                <tr>
                    <td class="style7" style="width: 160px; text-align: right">
                        <span style="font-size: 9pt; font-family: Verdana">Adj/Discount:</span></td>
                    <td><%=mygenval.getString("veSo_AmtAdjust") %>
                    </td>
                </tr>
                <tr>
                    <td class="style7" style="width: 160px; height: 21px; text-align: right">
                        <span style="font-size: 9pt; font-family: Verdana">Total Amount:</span></td>
                    <td style="height: 21px"><%=totalamt+mygenval.getDouble("veSo_TaxAmt")-mygenval.getDouble("veSo_AmtAdjust")%>
                    </td>
                </tr>
               <%String err="";
               double otherOS=0;
               try{
              	EntityExpr expr = new EntityExpr("Cust_Id", EntityOperator.EQUALS, objCommonServices.GetIdofName("tbl_Customer_Master", "Cust_Name", mygenval.getString("veCust_Name"), "Cust_Id"));
       		
       			EntityExpr expr2 = new EntityExpr("So_Status", EntityOperator.EQUALS, "APPROVED");
       		
       		
       			List a= UtilMisc.toList(expr,expr2);
       		 
       			EntityConditionList condList = new EntityConditionList(a,EntityOperator.AND);
               	List<GenericValue> strRow= objCommonServices.ReturnMultipleCondition("VE_Sum_SOPayment_ByCustomer", condList); 
               	DecimalFormat twoDForm = new DecimalFormat("#.##");
       			GenericValue row = strRow.get(0);	
       		
       		//lblTotalOSetValue(String.valueOf(twoDForm.format(row.getDouble("SoPay_TotalAmt"))));
       	       otherOS =  Double.valueOf(twoDForm.format( row.getDouble("SoPay_TotalAmt")-mygenval.getDouble("veSo_TotalAmt")));
               }catch(Exception ex){
            	    err=ex.getMessage();
               }
              %>
                <tr>
                    <td class="style7" style="width: 160px; height: 21px; text-align: right">
                        <span style="font-size: 9pt; font-family: Verdana">Other O/S:</span></td>
                    <td style="height: 21px"><%=err%><%=otherOS %>
                    </td>
                </tr>
                <tr>
                    <td class="style7" style="width: 160px; height: 21px; text-align: right">
                        <span style="font-size: 9pt; font-family: Verdana">Total O/S:</span></td>
                    <td style="height: 21px"><%=otherOS + multiamt+mygenval.getDouble("veSo_TaxAmt")-mygenval.getDouble("veSo_AmtAdjust")%>
                    </td>
                </tr>
            </table>
            </td>
            <td style="text-align: right">
            </td>
        </tr>
    <tr>
        <td class="style6" style="width: 167px">
        </td>
        <td class="style4" style="width: 332px">
        </td>
        <td style="width: 283px">
        </td>
        <td>
        </td>
    </tr>
    </table>

<%} %>

</body>

</html>


