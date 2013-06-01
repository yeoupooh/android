/* ownCloud Android client application
 *   Copyright (C) 2012  Bartek Przybylski
 *   Copyright (C) 2012-2013 ownCloud Inc.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License version 2,
 *   as published by the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.owncloud.android.files;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.owncloud.android.Log_OC;
import com.owncloud.android.files.services.FileUploader;

public class InstantUploadBroadcastReceiver extends BroadcastReceiver {

    private static String TAG = InstantUploadBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
       InstantUploadUtil instantUpload = new InstantUploadUtil(context);
        if (intent.getAction().equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION) && !intent.hasExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY)) {
            Log_OC.d(TAG, "Received: " + intent.getAction());
            instantUpload.uploadAwaitingFiles();
        }
        if (intent.getAction().equals(FileUploader.UPLOAD_FINISH_MESSAGE) && intent.getBooleanExtra(FileUploader.EXTRA_UPLOAD_RESULT, false)) {
                Log_OC.d(TAG, "Received: " + intent.getAction());
                String localPath = intent.getStringExtra(FileUploader.EXTRA_OLD_FILE_PATH);
                instantUpload.handleUploadFinished(localPath);
        }
//        else {
//            Log_OC.e(TAG, "Incorrect intent sent: " + intent.getAction());
//        }
    }

    

    
        
}
