/*
 * Copyright (C) 2017 Aromajoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cookie.actionsheet;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

/**
 * Created by Quang Nguyen on 3/16/17.
 * <p>
 * Creates an ActionSheet view which is similar to UIAlertConroller(.actionsheet style) of iOS,
 * especially, shown in iPad.
 * It has an arrow which points to a anchor view.
 */

public class ActionSheet {

    /**
     * Defines the styles that ActionSheet supports
     * They are similar with iOS ActionSheet style
     */
    public enum Style {
        /**
         * For user's normal actions in which the button uses blue text color
         */
        DEFAULT,
        /**
         * For the actions in which users remove or modify app content and data.
         */
        DESTRUCTIVE
    }

    private PopupWindow popupWindow;

    private ActionSheetContent contentView;

    private Context context;

    private View rootView;

    private ActionSheetTouchListener touchListener;

    public ActionSheet(Context context) {
        init(context,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void init(Context context, int width, int height) {
        this.context = context;
        if (popupWindow == null) {
            contentView = new ActionSheetContent(this);
            popupWindow = new PopupWindow(contentView,
                    width,
                    height,
                    true);

            // Closes the popup window when touch outside
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);
            // Removes default background
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setTouchInterceptor(new View.OnTouchListener() {


                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int x = (int) event.getX();
                    final int y = (int) event.getY();


                    if ((event.getAction() == MotionEvent.ACTION_DOWN)
                            && ((x < 0) || (x >= v.getWidth()) || (y < 0) || (y >= v
                            .getHeight()))) {
                        popupWindow.dismiss();
                        if(touchListener!=null){
                            touchListener.onDismiss();
                        }
                        return false;
                    } else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                        popupWindow.dismiss();
                        if(touchListener!=null){
                            touchListener.onDismiss();
                        }
                        return false;
                    } else {
                        return v.onTouchEvent(event);
                    }
                }
            });
        }
    }

    Context getContext() {
        return context;
    }

    /**
     * Sets title for ActionSheet
     *
     * @param title the name of the ActionSheet view
     */
    public void setTitle(String title) {
        if (contentView != null) {
            contentView.setTitle(title);
        }
    }

    public String getTitle() {
        return contentView.getTitle();
    }

    /**
     * Shows ActionSheet view after initialization.
     */
    public void show(RectF viewRect) {
        // Add arrow to actionsheet
        //contentView.addArrow(Arrow.DOWN);

        if (popupWindow == null) return;
        if(rootView == null) return;

        if (viewRect == null) return;

        // Points SheetView to the center of its anchor view
        contentView.measure(contentView.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
        int xPoint = (int)(viewRect.left + (viewRect.right-viewRect.left) / 2f - contentView.getMeasuredWidth()/2); // left
        int yPoint = (int)(viewRect.top + (viewRect.bottom-viewRect.top) / 2f- contentView.getMeasuredHeight()/2); // top

        // Makes sure that action sheet is always shown inside screen.
        if (yPoint <= ViewUtils.getStatusBarHeight(context)) {
            yPoint = ViewUtils.getStatusBarHeight(context);
        }

        if (popupWindow.isShowing()) {
            popupWindow.update(xPoint,
                    yPoint,
                    -1,
                    -1);
        } else {
            popupWindow.showAtLocation(rootView,
                    Gravity.NO_GRAVITY,
                    xPoint,
                    yPoint);
        }
    }

    /**
     * Hides ActionSheet view when completion.
     */
    public void dismiss() {
        popupWindow.dismiss();
    }

    /**
     * Allows user to add action during initialization process
     *
     * @param title    title of action
     * @param style    style of action
     * @param listener call back when users click action
     */
    public void addAction(String title, Style style, OnActionListener listener) {
        if (contentView == null) return;
        if (listener == null) return;
        contentView.addActionView(title, style, listener);
    }

    public View getRootView() {
        return rootView;
    }

    public void setRootView(View rootView) {
        this.rootView = rootView;
    }

    public void setTouchListener(ActionSheetTouchListener touchListener) {
        this.touchListener = touchListener;
    }

    public interface ActionSheetTouchListener{
        void onDismiss();
    }
}
