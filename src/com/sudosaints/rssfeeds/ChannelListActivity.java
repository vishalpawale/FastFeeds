package com.sudosaints.rssfeeds;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sudosaints.rssfeeds.R;
import com.sudosaints.rssfeeds.db.DbUtils;
import com.sudosaints.rssfeeds.model.Channel;
import com.sudosaints.rssfeeds.utils.IntentExtras;
import com.sudosaints.rssfeeds.utils.Logger;

public class ChannelListActivity extends ListActivity {

	private static final int MENU_ADD_CHANNEL = 1001;
	private static final int MENU_REMOVE = 1002;
	
	List<Channel> channels;
	DbUtils dbUtils;
	boolean isDestroyed = false;
	Button addIcon;
	boolean isNoChannel = false;
	Logger logger;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_list_layout);
        
        channels = new ArrayList<Channel>();
        dbUtils = new DbUtils(this);
        channels = dbUtils.getDbHelper().getAllChannels();
        logger = new Logger(this);
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
        if(channels.isEmpty()) {
        	addIcon = (Button) findViewById(R.id.addChannel);
        	addIcon.setVisibility(View.VISIBLE);
        	addIcon.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					isNoChannel = true;
					addNewChannel();
					addIcon.setVisibility(View.GONE);
				}
			});
        } else {
	        ArrayAdapter<Channel> adapter = new ChannelsAdapter(this, R.layout.channel_list_item_layout, channels);
	        setListAdapter(adapter);
        }
    }

    private class ChannelsAdapter extends ArrayAdapter<Channel> {

		public ChannelsAdapter(Context context, int textViewResourceId, List<Channel> objects) {
			super(context, textViewResourceId, objects);
		}
    	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			
			if(row == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.channel_list_item_layout, null, false);
			}
			
			TextView listItem = (TextView) row.findViewById(R.id.listItem);
			listItem.setText(channels.get(position).getName());
			return row;
		}
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	Channel channel = channels.get(position);
    	Intent intent = new Intent(ChannelListActivity.this, FeedsListActivity.class);
    	intent.putExtra(IntentExtras.CHANNEL_ID_EXTRA, channel.getId());
    	startActivity(intent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ADD_CHANNEL, 0, "Add Channel").setIcon(R.drawable.ic_menu_btn_add);
        menu.add(0, MENU_REMOVE, 0, "Remove Channels").setIcon(R.drawable.ic_menu_close_clear_cancel);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case MENU_ADD_CHANNEL:
			addNewChannel();
			break;
			
		case MENU_REMOVE:
			removeChannel();
			break;
		}
    	return true;
    }
    
    private void addNewChannel(){
    	LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.edittext_dialog, null);
		final EditText editText = (EditText) view.findViewById(R.id.editText);
		new AlertDialog.Builder(ChannelListActivity.this)
						.setView(view)
						.setPositiveButton("Add", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if(editText.length() > 0) {
									dbUtils.getDbHelper().addChannel(editText.getText().toString());
									Toast.makeText(ChannelListActivity.this, "Successfully added channel to Database", Toast.LENGTH_LONG).show();
									refreshListView();
								}
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if(isNoChannel) {
									addIcon.setVisibility(View.VISIBLE);
								}
							}
						})
						.setTitle("Add new channel")
						.setCancelable(false)
						.show();
    }
    
    private void removeChannel() {
    	
    	final Set<Channel> channelsToRemove = new HashSet<Channel>();
    	
    	ListAdapter listAdapter = new ArrayAdapter<Channel>(ChannelListActivity.this, R.layout.complete_list_row_layout, channels){
			
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
				final Channel channel = channels.get(position);
				name.setText(channel.getName());
				
				listItemCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						
						if(isChecked){
							channelsToRemove.add(channel);
						} else {
							channelsToRemove.remove(channel);
						}
					}
				});
				
				if(channelsToRemove.contains(channel)) {
					listItemCheckBox.setChecked(true);
				} else {
					listItemCheckBox.setChecked(false);
				}
				return row;
			}
		};
    	
		new AlertDialog.Builder(ChannelListActivity.this)
						.setAdapter(listAdapter, new DialogInterface.OnClickListener() {
				
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
				
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if(!channelsToRemove.isEmpty()) {
									//Remove channels one by one
									for (Channel channel : channelsToRemove) {
										dbUtils.getDbHelper().deleteChannel(channel.getId());
									}
									refreshListView();
								} else {
									Toast.makeText(ChannelListActivity.this, "Please select at-least one channel to remove", Toast.LENGTH_LONG).show();
								}
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.setTitle("Available Channels")
						.setCancelable(false)
						.show();
    }
    
    private void refreshListView(){
    	
    	channels = new ArrayList<Channel>();
    	channels = dbUtils.getDbHelper().getAllChannels();
    	if(channels.isEmpty()) {
    		addIcon = (Button) findViewById(R.id.addChannel);
        	addIcon.setVisibility(View.VISIBLE);
        	addIcon.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					isNoChannel = true;
					addNewChannel();
					addIcon.setVisibility(View.GONE);
				}
			});
        }
    	ArrayAdapter<Channel> adapter = new ChannelsAdapter(ChannelListActivity.this, R.layout.channel_list_item_layout, channels);
		setListAdapter(adapter);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	dbUtils.getDbHelper().close();
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    }
}
