package org.ofbiz.ICE.zkValidation;

import java.lang.annotation.Documented;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.*;
//import org.apache.jasper.tagplugins.jstl.core.ForEach;
import org.codehaus.groovy.ast.expr.RegexExpression;
import org.joni.Regex;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;

public class zkCommonValidations {

	public boolean TextBoxValidate(String strtxt) {

		if (strtxt == null || strtxt.length()==0) {

			return false;
		} else {
			return true;
		}
	}

	/*
	 * DONE on 24-feb2011
	 */
	public boolean validateToDateFromDate(Date todate, Date fromdate,
			Date givendate) {
		
		
		if(givendate.after(fromdate) && givendate.before(todate))
		{
			return true;
		}

		return false;
	}
	
	public boolean validateIsNumber(String num) {
		
		try
		{
			Double.parseDouble(num);
			return true;
		}
		catch(NumberFormatException exx)
		{
			return false;
		}
		
	}
	
	
	/* checks if text is empty or has "--Select--"
	 * 
	 */
	public boolean validateForSelect(String param1)
	{
		if(TextBoxValidate(param1)==false)
		{
			return false;
		}
		
		if(param1.equalsIgnoreCase("--Select--"))
		{
			
			return false;
		}
		return true;
	}
	
}
