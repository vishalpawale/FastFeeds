package com.sudosaints.rssfeeds;

import com.sudosaints.rssfeeds.R;
import com.sudosaints.rssfeeds.utils.IntentExtras;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {

	ProgressDialog progressDialog;
	boolean loadingFinished = true;
	boolean redirect = false;
	
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		WebView webView = (WebView) findViewById(R.id.webpage);
		
		if(!getIntent().hasExtra(IntentExtras.URL_EXTRA)){
			finish();
		} else {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Please wait...");
			progressDialog.show();
			String url = getIntent().getStringExtra(IntentExtras.URL_EXTRA);
			webView.getSettings().setJavaScriptEnabled(true);
			webView.loadUrl(url);
			webView.addJavascriptInterface(this, "hostObject");
			webView.loadUrl("javascript:");
			webView.setWebViewClient(new DisPlayWebPageActivityClient());
		}
	}
	
	private class DisPlayWebPageActivityClient extends WebViewClient {
		
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	if (!loadingFinished) {
	    		redirect = true;
	    	}
	    	loadingFinished = false;
	    	view.loadUrl(url);
	    	return true;
	    }
	    
	    @Override
	    public void onPageFinished(WebView view, String url) {
	    	if(!redirect){
	    		loadingFinished = true;
	    	}

	    	if(loadingFinished && !redirect){
	    		//HIDE LOADING IT HAS FINISHED
	    		if(progressDialog != null && progressDialog.isShowing()) {
	    			progressDialog.dismiss();
	    		}
	    	} else{
	    		redirect = false; 
	    	}
	    }
	    
	    @Override
	    public void onPageStarted(WebView view, String url, Bitmap favicon) {
	        loadingFinished = false;
	    }
	}
}
