package com.owncloud.android.files;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;

import com.owncloud.android.AccountUtils;
import com.owncloud.android.Log_OC;
import com.owncloud.android.db.DbHandler;
import com.owncloud.android.files.services.FileUploader;
import com.owncloud.android.network.ConnectionUtil;
import com.owncloud.android.utils.FileStorageUtils;

public class InstantUploadUtil extends ContentObserver {

    private static String TAG = InstantUploadUtil.class.getSimpleName();
  
    private  String mediaType;
    private  ConnectionUtil connectionUtil;
    private  Context mContext;
      
    public InstantUploadUtil(Context context) {
        super(null);
        mContext = context;
    }
    
    public InstantUploadUtil(Context context,String mediaType) {
        super(null);
        this.mContext = context;
        this.mediaType= mediaType;
    }
    
    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
      
        handleNewMediaAction();
        Log_OC.d("INSTANT", "detected media");
    }
    
    public void uploadAwaitingFiles() {
        DbHandler db = new DbHandler(mContext);
        Cursor cursor = db.getAwaitingFiles();
        uploadInstantFiles(cursor);
    }
    
    public void handleNewMediaAction() {
        Cursor cursor = null;
        if (!connectionUtil.instantUploadEnabled()) {
           Log_OC.d(TAG, "Instant upload disabled, aborting uploading");
           return;
        }
        // check if it is a photo or a video for upload and get the cursor
        if (mediaType.equals("photo")) {
           cursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null,null, "date_added DESC");
        }
        else if (mediaType.equals("video")) {
           cursor = mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null,null, "date_added DESC");
        }
        if (cursor != null) {
           uploadInstantFiles(cursor);
        }
    }
    
    public  void handleUploadFinished(String localPath) {
        // remove successfully uploads, ignore rest for re-upload on reconnect
            DbHandler db = new DbHandler(mContext);
            if (!db.removeIUPendingFile(localPath)) {
                Log_OC.w(TAG, "Tried to remove non existing instant upload file " + mContext);
            }
            db.close();
    }
 
    private void uploadInstantFiles(Cursor cursor) {
        String file_path = null;
        String file_name = null;
        String mime_type = null;
        Account account = AccountUtils.getCurrentOwnCloudAccount(mContext);
        if (account == null) {
            Log_OC.w(TAG, "No owncloud account found for instant upload, aborting");
            return;
        }
          if (cursor != null) {
            if (cursor.moveToNext()) {
                int dataColumn = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
                int mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaColumns.MIME_TYPE);
                int displayNameColumn = cursor.getColumnIndexOrThrow(MediaColumns.DISPLAY_NAME);
                file_path = cursor.getString(dataColumn);
                mime_type = cursor.getString(mimeTypeColumn);
                file_name = cursor.getString(displayNameColumn);
            }
            cursor.close();
          }
          else {
              Log_OC.e(TAG, "No Media DATA found for instantUpload");
              return;
          }
        Log_OC.e(TAG, file_path + "");
        // same always temporally the picture to upload
        DbHandler db = new DbHandler(mContext);
        db.putFileForLater(file_path, account.name, null);
        db.close();
        if (!connectionUtil.isOnline() || (connectionUtil.instantUploadViaWiFiOnly() && !connectionUtil.isConnectedViaWiFi())) {
            return;
        }
        IntentFilter filter = new IntentFilter(FileUploader.UPLOAD_FINISH_MESSAGE);
        mContext.getApplicationContext().registerReceiver(new InstantUploadBroadcastReceiver(), filter);
        Intent i = new Intent(mContext, FileUploader.class);
        i.putExtra(FileUploader.KEY_ACCOUNT, account);
        i.putExtra(FileUploader.KEY_LOCAL_FILE, file_path);
        i.putExtra(FileUploader.KEY_REMOTE_FILE, FileStorageUtils.getInstantUploadFilePath(mContext, file_name));
        i.putExtra(FileUploader.KEY_UPLOAD_TYPE, FileUploader.UPLOAD_SINGLE_FILE);
        i.putExtra(FileUploader.KEY_MIME_TYPE, mime_type);
        i.putExtra(FileUploader.KEY_INSTANT_UPLOAD, true);
        mContext.startService(i);
    }
}
