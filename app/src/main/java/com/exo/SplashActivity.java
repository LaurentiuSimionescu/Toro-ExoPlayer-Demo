/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import im.ene.toro.exoplayer.exo.ExoPlayerApplication;
import im.ene.toro.exoplayer.exo.MediaCachingHelper;
import im.ene.toro.exoplayer.exo.VideoSource;

public class SplashActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        ArrayList<VideoSource> videoSourceList = PlayerListActivity.Companion.getVideoList();
        ArrayList<Uri> videoUriList = new ArrayList<Uri>();
        for (VideoSource videoSource : videoSourceList) {
            videoUriList.add(videoSource.uri());
        }

        findViewById(R.id.openListActivityButton).setOnClickListener(v -> startActivity(new Intent(this, PlayerListActivity.class)));

        findViewById(R.id.cacheButton).setOnClickListener(v -> {
            MediaCachingHelper mediaCachingHelper = new MediaCachingHelper(((ExoPlayerApplication) getApplication()).getDownloadTracker());
            mediaCachingHelper.cache(videoUriList);
        });

        findViewById(R.id.removeButton).setOnClickListener(v -> {
            MediaCachingHelper mediaCachingHelper = new MediaCachingHelper(((ExoPlayerApplication) getApplication()).getDownloadTracker());
            mediaCachingHelper.remove(videoUriList);
        });

    }

}