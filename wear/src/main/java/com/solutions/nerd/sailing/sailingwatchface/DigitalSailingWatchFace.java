/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.solutions.nerd.sailing.sailingwatchface;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.android.gms.wearable.Node;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class DigitalSailingWatchFace extends BaseSailingWatchFace {
    private static final String TAG=DigitalSailingWatchFace.class.getSimpleName();

    @Override
    public Engine onCreateEngine() {
        if (getResources().getBoolean(R.bool.is_debug)){
            Log.i(TAG,"Is in debug mode");
        }
        return new DigitalEngine();

    }



    @Override
    int getWatchType() {
        return Consts.DIGITAL;
    }

    private class DigitalEngine extends BaseWatchEngine {
        private final String TAG=DigitalEngine.class.getSimpleName();





    }

}
