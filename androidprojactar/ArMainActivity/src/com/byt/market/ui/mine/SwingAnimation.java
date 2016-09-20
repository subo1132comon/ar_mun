/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain mPivotOffset copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.byt.market.ui.mine;

import java.util.Random;

import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * An animation that controls the rotation of an object. This rotation takes
 * place int the X-Y plane. You can specify the point to use for the center of
 * the rotation, where (0,0) is the top left point. If not specified, (0,0) is
 * the default rotation point.
 * 
 */
public class SwingAnimation extends Animation {
	final int[] mPivotOffset = { -20, 25, -5, 20, -15, 5, 35 };
	int[] mRandom = new int[40];
	int mIdx = 0;
	Random random = new Random();
	private float mFromDegrees;
	private float mToDegrees;
	private float mPivotX;
	private float mPivotY;

	public SwingAnimation(float fromDegrees, float toDegrees) {
		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;
		
		for (int i = 0; i < 40; i++)
			mRandom[i] = random.nextInt(7);
	}
	
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		int offset = mPivotOffset[mRandom[mIdx]];
		float degrees = mFromDegrees + interpolatedTime * (mToDegrees - mFromDegrees);
		
		t.getMatrix().setRotate(degrees, mPivotX + offset, mPivotY + offset);
		
		mRandom[mIdx] = ((int) (0.1F + mRandom[mIdx]) % 7);
		mIdx = ((1 + mIdx) % 40);
	}

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mPivotX = resolveSize(RELATIVE_TO_SELF, 0.5F, width, parentWidth);
        mPivotY = resolveSize(RELATIVE_TO_SELF, 0.5F, height, parentHeight);
    }

}
