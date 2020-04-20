// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;

import com.mopub.common.AdReport;
import com.mopub.common.test.support.SdkTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowGestureDetector;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;


@RunWith(SdkTestRunner.class)
public class ViewGestureDetectorTest {
    private Activity context;
    private ViewGestureDetector subject;
    private AdAlertGestureListener adAlertGestureListener;
    private View view;
    @Mock AdReport mockAdReport;

    @Before
    public void setUp() throws Exception {
        context = Robolectric.buildActivity(Activity.class).create().get();
        view = mock(View.class);
        when(view.getWidth()).thenReturn(320);
        when(view.getHeight()).thenReturn(50);

        adAlertGestureListener = mock(AdAlertGestureListener.class);

        subject = new ViewGestureDetector(context, view, mockAdReport);
        subject.setAdAlertGestureListener(adAlertGestureListener);
    }

    @Test
    public void constructor_shouldDisableLongPressAndSetGestureListener() throws Exception {
        subject = new ViewGestureDetector(context, view, mockAdReport);

        ShadowGestureDetector shadowGestureDetector = Shadows.shadowOf(subject);

        assertThat(subject.isLongpressEnabled()).isFalse();
        assertThat(shadowGestureDetector.getListener()).isNotNull();
        assertThat(shadowGestureDetector.getListener()).isInstanceOf(AdAlertGestureListener.class);
    }

    @Test
    public void onTouchEvent_whenActionDown_shouldForwardOnTouchEvent() throws Exception {
        MotionEvent expectedMotionEvent = createMotionEvent(MotionEvent.ACTION_DOWN);

        subject.onTouchEvent(expectedMotionEvent);

        MotionEvent actualMotionEvent = Shadows.shadowOf(subject).getOnTouchEventMotionEvent();

        assertThat(actualMotionEvent).isEqualTo(expectedMotionEvent);
    }

    @Test
    public void onTouchEvent_whenActionMoveWithinView_shouldForwardOnTouchEvent() throws Exception {
        MotionEvent downEvent = createMotionEvent(MotionEvent.ACTION_DOWN);
        subject.onTouchEvent(downEvent);

        MotionEvent expectedMotionEvent = createActionMove(160);
        subject.onTouchEvent(expectedMotionEvent);

        MotionEvent actualMotionEvent = Shadows.shadowOf(subject).getOnTouchEventMotionEvent();

        assertThat(actualMotionEvent).isEqualTo(expectedMotionEvent);
        verify(adAlertGestureListener, never()).reset();
    }

    @Test
    public void resetAdFlaggingGesture_shouldNotifyAdAlertGestureListenerOfReset() throws Exception {
        subject.resetAdFlaggingGesture();

        verify(adAlertGestureListener).reset();
    }

    private MotionEvent createActionMove(float x) {
        return MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, x, 0, 0);
    }

    private MotionEvent createMotionEvent(int action) {
        return MotionEvent.obtain(0, 0, action, 0, 0, 0);
    }
}
