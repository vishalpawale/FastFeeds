package com.sudosaints.rssfeeds;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.helper.StringUtil;

import twitter4j.Twitter;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sudosaints.rssfeeds.R;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.sudosaints.rssfeeds.db.DbUtils;
import com.sudosaints.rssfeeds.model.RSSFeed;
import com.sudosaints.rssfeeds.model.RSSItem;
import com.sudosaints.rssfeeds.model.WebSite;
import com.sudosaints.rssfeeds.utils.DateHelper;
import com.sudosaints.rssfeeds.utils.IntentExtras;
import com.sudosaints.rssfeeds.utils.Logger;
import com.sudosaints.rssfeeds.utils.RSSParser;
import com.sudosaints.rssfeeds.utils.ResultStatus;

public class FeedsListActivity extends ListActivity{

	private static final int MENU_ADD_SITE = 1001;
	private static final int MENU_VIEW_SITES = 1002;
	private static final int MENU_REMOVE_SITES = 1003;
	private static final int MENU_FEEDS_REFRESH = 1004;
	private static final String[] PERMISSIONS = new String[] {"publish_actions"};
	
	List<RSSItem> feeds;
	DbUtils dbUtils;
	Logger logger;
	long channelId;
	ProgressDialog pDialog;
	AlertDialog alertDialog;
	String userInputToShare;
	RSSItem feedToShare;
	boolean isDestroyed= false;
	boolean isNoSites = false; 
	
	Twitter twitter;
	Session.StatusCallback statusCallback = new StatusCallBack();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feeds_list_layout);

		feeds = new ArrayList<RSSItem>();
		logger = new Logger(this);
		dbUtils = new DbUtils(this);
		
		if(getIntent().hasExtra(IntentExtras.CHANNEL_ID_EXTRA)) {
			channelId = getIntent().getLongExtra(IntentExtras.CHANNEL_ID_EXTRA, 1);
			logger.debug("Channel Id is - " + channelId);
			new DownloadFeeds(channelId).execute();
		} else {
			finish();
		}
		/*try {
        	logger.debug("Checking signs");
            PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                logger.debug(Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (NameNotFoundException e) {
        	e.printStackTrace();
        	logger.debug(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
        	e.printStackTrace();
        	logger.debug(e.getMessage());
        }*/
		getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view, final int position, long id) {
				
				final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				final List<String> socialChoice = new ArrayList<String>();
				socialChoice.add(0, "Share on facebook");
				socialChoice.add(1, "Share on Twitter");
				final List<Drawable> socialLogos = new ArrayList<Drawable>();
				socialLogos.add(0, getResources().getDrawable(R.drawable.facebook_logo));
				socialLogos.add(1, getResources().getDrawable(R.drawable.twitter));
				
				ListAdapter listAdapter = new ArrayAdapter<String>(FeedsListActivity.this, R.layout.social_share_layout, socialChoice) {
					
					public View getView(int position, View convertView, ViewGroup parent) {
						
						View row = convertView;
						if(row == null) {
							row = inflater.inflate(R.layout.social_share_layout, null);
						}
						ImageView socialLogo = (ImageView) row.findViewById(R.id.socialLogo);
						socialLogo.setImageDrawable(socialLogos.get(position));
						TextView socialText = (TextView) row.findViewById(R.id.socialText);
						socialText.setText(socialChoice.get(position));
						return row;
					};
				};
				
				alertDialog = new AlertDialog.Builder(FeedsListActivity.this)
								.setAdapter(listAdapter, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										alertDialog.dismiss();
										if(which == 0) {
											getUserInputAndPublish(feeds.get(position));
										} else {
											//Toast.makeText(FeedsListActivity.this, "This feature is under progress", Toast.LENGTH_LONG).show();
											//new ShareTweet().execute();
											Intent intent = new Intent(FeedsListActivity.this, TwitterOAuthActivity.class);
											intent.putExtra(IntentExtras.URL_EXTRA, feeds.get(position).getLink());
											startActivity(intent);
										}
									}
								})
								.setCancelable(false)
								.show();
				return true;
			}
		});
		
		
		Session session = Session.getActiveSession();
        if(session == null) {
        	if(savedInstanceState != null) {
        		session = Session.restoreSession(FeedsListActivity.this, null, statusCallback, savedInstanceState);
        	}
        	if(session == null){
        		session = new Session(this);
        	}
        	session.addCallback(statusCallback);
        	Session.setActiveSession(session);
        }
		logger.debug("Session State - " + session.getState());
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String url = feeds.get(position).getLink();
		Intent intent = new Intent(FeedsListActivity.this, WebViewActivity.class);
		intent.putExtra(IntentExtras.URL_EXTRA, url);
		startActivity(intent);
	}
	
	private class FeedsAdapter extends ArrayAdapter<RSSItem> {

		public FeedsAdapter(Context context, int textViewResourceId, List<RSSItem> objects) {
			super(context, textViewResourceId, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			
			if(row == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.feed_list_item_layout, null, false);
			}
			
			TextView title = (TextView) row.findViewById(R.id.feedListItemTitle);
			TextView date = (TextView) row.findViewById(R.id.feedListItemDate);
			ImageView facebookIcon = (ImageView) row.findViewById(R.id.facebook);
			ImageView twitterIcon = (ImageView) row.findViewById(R.id.twitter);
			
			final RSSItem rssItem = feeds.get(position);
			facebookIcon.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					feedToShare = rssItem;
					getUserInputAndPublish(rssItem);
				}
			});
			
			twitterIcon.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(FeedsListActivity.this, TwitterOAuthActivity.class);
					intent.putExtra(IntentExtras.URL_EXTRA, rssItem.getGuid());
					startActivity(intent);
				}
			});
			
			title.setText(rssItem.getTitle());
			date.setText(rssItem.getPubdate());
			return row;
		}
		
	}
	
	private class DownloadFeeds extends AsyncTask<Void, Void, ResultStatus> {

		ProgressDialog dialog;
		long channelId;
		
		public DownloadFeeds(long channelId) {
			super();
			this.channelId = channelId;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(FeedsListActivity.this);
			dialog.setMessage("Please wait...");
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
		}
		
		@Override
		protected ResultStatus doInBackground(Void... params) {
			ResultStatus resultStatus = new ResultStatus(true);
			List<WebSite> webSites = new ArrayList<WebSite>();
			RSSParser rssParser = new RSSParser();
			feeds = new ArrayList<RSSItem>();
			webSites = dbUtils.getDbHelper().getSitesByChannel(channelId);
			
			try {
				if(!webSites.isEmpty()) {
					for (WebSite webSite : webSites) {
						if(StringUtil.isBlank(webSite.getRSSLink())) {
							logger.debug("Downloading source for - " + webSite.getLink());
							String rssUrl = rssParser.getRSSLinkFromURL(webSite.getLink());
							RSSFeed rssFeed = rssParser.getRSSFeed(rssUrl);
							feeds.addAll(rssFeed.getItems());
							webSite.setRSSLink(rssUrl);
							dbUtils.getDbHelper().updateSite(webSite);
						} else {
							logger.debug("Already downloaded source for - " + webSite.getLink());
							feeds.addAll(rssParser.getRSSFeedItems(webSite.getRSSLink()));
						}
					}
				} else {
					isNoSites = true;
					return ResultStatus.NO_SITES;
				}
				Collections.sort(feeds, new Comparator<RSSItem>() {
					
					@Override
					public int compare(RSSItem lhs, RSSItem rhs) {
						// TODO Compare pub dates
						Date lhsDate = DateHelper.parseDayDateTimeLocale(lhs.getPubdate());
						Date rhsDate = DateHelper.parseDayDateTimeLocale(rhs.getPubdate());
						return rhsDate.compareTo(lhsDate);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				resultStatus.setSuccess(false);
				resultStatus.setStatusMessage("Unable to communicate with server.\nPlease try again later");
			}
			return resultStatus;
		}
		
		@Override
		protected void onPostExecute(ResultStatus result) {
			super.onPostExecute(result);
			if(!isDestroyed) {
				dialog.dismiss();
				if(result.isSuccess()) {
					ArrayAdapter<RSSItem> feedsAdapter = new FeedsAdapter(FeedsListActivity.this, R.layout.feed_list_item_layout, feeds);
					setListAdapter(feedsAdapter);
				} else {
					/*Toast.makeText(FeedsListActivity.this, "Unable to reach server. Please try again later.", Toast.LENGTH_LONG).show();
					finish();*/
					if(result.equals(ResultStatus.NO_SITES)) {
						new AlertDialog.Builder(FeedsListActivity.this)
						.setTitle(result.getDialogTitle())
						.setMessage(result.getStatusMessage())
						.setPositiveButton("Add New WebSite", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								//finish();
								addSite();
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								finish();
							}
						})
						.setCancelable(false)
						.show();
					} else {
						new AlertDialog.Builder(FeedsListActivity.this)
										.setTitle("Communication Error")
										.setMessage(result.getStatusMessage())
										.setPositiveButton("OK", new DialogInterface.OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												// TODO Auto-generated method stub
												//finish();
											}
										})
										.setCancelable(false)
										.show();
					}
				}
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ADD_SITE, 0, "Add Site").setIcon(R.drawable.ic_menu_btn_add);
		menu.add(0, MENU_VIEW_SITES, 0, "View Sites").setIcon(R.drawable.ic_menu_find_holo_dark);
		menu.add(0, MENU_REMOVE_SITES, 0, "Remove Sites").setIcon(R.drawable.ic_menu_close_clear_cancel);
		menu.add(0, MENU_FEEDS_REFRESH, 0, "Refresh").setIcon(R.drawable.ic_menu_refresh);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD_SITE:
			addSite();
			break;

		case MENU_VIEW_SITES:
			viewSites();
			break;
		
		case MENU_REMOVE_SITES:
			removeSites();
			break;
			
		case MENU_FEEDS_REFRESH:
			refreshFeeds();
			break;
		}
		return true;
	}

	private void addSite() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.addsite_dialog, null);
		final EditText siteTitle = (EditText) view.findViewById(R.id.siteTitle);
		final EditText siteUrl = (EditText) view.findViewById(R.id.siteUrl);
		new AlertDialog.Builder(FeedsListActivity.this)
						.setView(view)
						.setPositiveButton("Add", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								String title = "Default";
								if(siteTitle.getText().length() > 0) {
									title = siteTitle.getText().toString();
								}
								if(siteUrl.length() > 0) {
									String url = siteUrl.getText().toString();
									if(!url.startsWith("http://")) {
										url = "http://" + url;
									}
									dbUtils.getDbHelper().addSite(url, "", title, channelId);
									Toast.makeText(FeedsListActivity.this, "Successfully added site to Database", Toast.LENGTH_LONG).show();
									refreshFeeds();
									return;
								}
								Toast.makeText(FeedsListActivity.this, "Please enter valid data", Toast.LENGTH_LONG).show();
								addSite();
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if(isNoSites && ! isDestroyed) {
									finish();
								}
							}
						})
						.setTitle("Add new website")
						.setCancelable(false)
						.show();
	}
	
	private void viewSites() {
		
		final List<WebSite> webSites = dbUtils.getDbHelper().getSitesByChannel(channelId);
		ListAdapter listAdapter = new ArrayAdapter<WebSite>(FeedsListActivity.this, R.layout.complete_list_row_layout, webSites){
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				
				View row = convertView;
				if(row == null) {
					LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					row = layoutInflater.inflate(R.layout.complete_list_row_layout, null);
				}
				
				TextView name = (TextView) row.findViewById(R.id.channelName);
				((CheckBox) row.findViewById(R.id.listItemCheckBox)).setVisibility(View.GONE);
				final WebSite website = webSites.get(position);
				name.setText(website.getTitle());
				return row;
			}
		};
    	
		new AlertDialog.Builder(FeedsListActivity.this)
						.setAdapter(listAdapter, new DialogInterface.OnClickListener() {
				
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
				
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.setTitle("Available WebSites")
						.setCancelable(false)
						.show();
	}
	
	private void removeSites() {
		
		final List<WebSite> webSites = dbUtils.getDbHelper().getSitesByChannel(channelId);
		final Set<WebSite> sitesToRemove = new HashSet<WebSite>();
    	
    	ListAdapter listAdapter = new ArrayAdapter<WebSite>(FeedsListActivity.this, R.layout.complete_list_row_layout, webSites){
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				
				View row = convertView;
				if(row == null) {
					LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					row = layoutInflater.inflate(R.layout.complete_list_row_layout, null);
				}
				
				TextView name = (TextView) row.findViewById(R.id.channelName);
				//name.setTypeface(robotoTypeface);
				CheckBox listItemCheckBox = (CheckBox) row.findViewById(R.id.listItemCheckBox);
				final WebSite website = webSites.get(position);
				name.setText(website.getTitle());
				
				listItemCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						
						if(isChecked){
							sitesToRemove.add(website);
						} else {
							sitesToRemove.remove(website);
						}
					}
				});
				
				if(sitesToRemove.contains(website)) {
					listItemCheckBox.setChecked(true);
				} else {
					listItemCheckBox.setChecked(false);
				}
				return row;
			}
		};
    	
		new AlertDialog.Builder(FeedsListActivity.this)
						.setAdapter(listAdapter, new DialogInterface.OnClickListener() {
				
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
				
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if(!sitesToRemove.isEmpty()) {
									//Remove websites one by one
									for (WebSite website : sitesToRemove) {
										dbUtils.getDbHelper().deleteSite(website.getId());
									}
								} else {
									Toast.makeText(FeedsListActivity.this, "Please select at-least one website to remove", Toast.LENGTH_LONG).show();
								}
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.setTitle("Available Websites")
						.setCancelable(false)
						.show();
	}
	
	private void refreshFeeds() {
		new DownloadFeeds(channelId).execute();
	}
	
	/**
	 * Facebook Methods
	 */
	
	private class StatusCallBack implements Session.StatusCallback{

		@Override
		public void call(Session session, SessionState state, Exception exception) {
			//Process change in session states
			if(session != null) {
				logger.debug("Session Info after status change - " + session);
			}
			if(session != null && state.isOpened()) {
				logger.debug("Session is opened");
				if(session.getPermissions().contains("publish_actions")) {
					logger.debug("Starting share");
					postURL(session, userInputToShare, feedToShare);
				} else {
					logger.debug("Session dont have permissions");
					publishStory(userInputToShare, feedToShare);
				}
			} else {
				logger.debug("Invalid fb Session");
			}
		}
    }
	
	@Override
    protected void onStart() {
    	super.onStart();
    	Session.getActiveSession().addCallback(statusCallback);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	Session.getActiveSession().removeCallback(statusCallback);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	logger.debug("Result Code is - " + resultCode +"");
		//Session.getActiveSession().addCallback(statusCallback);
    	Session.getActiveSession().onActivityResult(FeedsListActivity.this, requestCode, resultCode, data);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	Session session = Session.getActiveSession();
    	Session.saveSession(session, outState);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	dbUtils.getDbHelper().close();
    }
    
    private void getUserInputAndPublish(final RSSItem rssItem) {
    	
    	if (alertDialog != null && alertDialog.isShowing()) {
			logger.debug("Showing alert dialog");
			alertDialog.dismiss();
		}
    	LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    	View view = inflater.inflate(R.layout.userinput_fb_dialog, null, false);
    	final TextView userText = (TextView) view.findViewById(R.id.socialText);
    	
    	new AlertDialog.Builder(FeedsListActivity.this)
    					.setView(view)
    					.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if(userText.getText().length() > 0) {
									userInputToShare = userText.getText().toString();
									publishStory(userText.getText().toString(), rssItem);
								}  else {
									userInputToShare = "";
									publishStory("", rssItem);
								}
							}
						})
						.setCancelable(false)
						.show();
    }
    
    private void publishStory(final String userText, final RSSItem rssItem){
    	
    	Session session = Session.getActiveSession();
    	if(session != null && session.isOpened()) {
    		logger.debug("Session is Opened");
    		checkSessionAndPost(userText, rssItem);
    	}else {
    		logger.debug("Session is null");
			session = new Session(FeedsListActivity.this);
			Session.setActiveSession(session);
			session.addCallback(statusCallback);
			
			logger.debug("Session info - " + session);
			try {
				logger.debug("Opening session for read");
				session.openForRead(new Session.OpenRequest(FeedsListActivity.this));
			} catch(UnsupportedOperationException exception) {
				exception.printStackTrace();
				logger.debug("Exception Caught");
				Toast.makeText(FeedsListActivity.this, "Unable to post your status on facebook", Toast.LENGTH_LONG).show();
			}
    	}
    }
    
    private void checkSessionAndPost (String userText, RSSItem rssItem){

		Session session = Session.getActiveSession();
		session.addCallback(statusCallback);
		logger.debug("Session Permissions Are - " + session.getPermissions());
			
			if(session.getPermissions().contains("publish_actions")) {
				postURL(session, userText, rssItem);
			} else {
				session.requestNewPublishPermissions(new Session.NewPermissionsRequest(FeedsListActivity.this, Arrays.asList(PERMISSIONS)));
				
		} 
	}
    
    private void postURL(Session session, String userText, RSSItem rssItem) {
    	
    	Bundle postParams = new Bundle();
    	postParams.putString("name", "FastFeeds");
		postParams.putString("caption", rssItem.getTitle());
		postParams.putString("description", rssItem.getGuid());
		postParams.putString("link", rssItem.getLink());
		//postParams.putString("picture", "http://sudosaints.com/wp-content/themes/sudosaints/images/logo.png");
		postParams.putString("message", userText);
		pDialog = new ProgressDialog(FeedsListActivity.this);
		pDialog.setMessage("Please wait...");
		
		Request.Callback callback = new Request.Callback() {
			
			@Override
			public void onCompleted(Response response) {
				
				if(pDialog.isShowing()) {					
					pDialog.dismiss();
				}
				FacebookRequestError error = response.getError();
				if(!isDestroyed) {
					if(error != null) {
						Toast.makeText(FeedsListActivity.this, error.getErrorMessage(), Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(FeedsListActivity.this, "Successfully shared your Link of Facebook", Toast.LENGTH_LONG).show();
						userInputToShare = "";
						feedToShare = new RSSItem();
					}
				}
			}
		};
		
		Request request = new Request(session, "me/feed", postParams, HttpMethod.POST, callback);
		RequestAsyncTask asyncTask = new RequestAsyncTask(request);
		asyncTask.execute();
    }
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		isDestroyed = true;
		Session session = Session.getActiveSession();
		Session.saveSession(session, new Bundle());
	}

}
