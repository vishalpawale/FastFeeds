package com.sudosaints.rssfeeds.utils;

/**
 * Convenience Data Object to hold return status of a method call
 * @author Vishal
 *
 */
public class ResultStatus {

	boolean success;
	String statusMessage;
	Object resultObject;
	String dialogTitle;

	
	public static final ResultStatus OK	= new ResultStatus(true, null);
	public static final ResultStatus FAIL		= new ResultStatus(false, "Generic Error");
	public static final ResultStatus NO_SITES = new ResultStatus(false, "No web sites are available for this channel", "No Sites");
	
	public ResultStatus(boolean success) {
		this.success = success;
		if (!success) {
			statusMessage = "Generic Error";
		}
		else {
			statusMessage = "OK";
		}
		
	}
	public ResultStatus(boolean success, String message, String dialogTitle) {
		super();
		this.success = success;
		this.statusMessage = message;
		this.dialogTitle=dialogTitle;
	}
	
	public ResultStatus(boolean success, String message) {
		super();
		this.success = success;
		this.statusMessage = message;
	}
	
	public Object getResultObject() {
		return resultObject;
	}

	public void setResultObject(Object resultObject) {
		this.resultObject = resultObject;
	}

	

	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getStatusMessage() {
		return statusMessage;
	}
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	public String getDialogTitle() {
		return dialogTitle;
	}
	public void setDialogTitle(String dialogTitle) {
		this.dialogTitle = dialogTitle;
	}
	public static ResultStatus errorResult(String error) {
		return new ResultStatus(false, error);
	}
	
	public static ResultStatus errorResult(String error,String errorTitle) {
		return new ResultStatus(false, error, errorTitle);
	}
	
		
	
}
