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
package cn.com.chaochuang.pdf_operation.ui.actionsheet;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import cn.com.chaochuang.pdf_operation.R;


class ActionSheetContent extends LinearLayout {
  private TextView titleView;
  private LinearLayout actionContainer;
  private ActionSheet actionSheet;

  // Custom styleable properties
  private float width;
  private int titleColor;
  private int defaultActionColor;
  private int destructiveActionColor;
  private float titleTextSize;
  private float actionTextSize;

  public ActionSheetContent(ActionSheet actionSheet) {
    super(actionSheet.getContext());
    this.actionSheet = actionSheet;

    init(actionSheet.getContext());
  }

  private void init(Context context) {
    getAttrs(context);

    setOrientation(VERTICAL);
    setGravity(Gravity.CENTER);
    addTitle(context);
    addActionContainer(context);
  }

  private void getAttrs(Context context) {
    TypedArray typedArray = context.getTheme().obtainStyledAttributes(R.styleable.ActionSheetContent);

    width = typedArray.getDimension(R.styleable.ActionSheetContent_asWidth,
        getResources().getDimension(R.dimen.pdf_actionsheet_width));
    titleColor = typedArray.getColor(R.styleable.ActionSheetContent_asTitleColor,
        ContextCompat.getColor(getContext(), R.color.pdf_black));
    defaultActionColor = typedArray.getColor(R.styleable.ActionSheetContent_asDefaultColor,
        ContextCompat.getColor(getContext(), R.color.pdf_blue));
    destructiveActionColor = typedArray.getColor(R.styleable.ActionSheetContent_asDestructiveColor,
        ContextCompat.getColor(getContext(), R.color.pdf_red));
    titleTextSize = typedArray.getDimension(R.styleable.ActionSheetContent_asTitleTextSize,
        getResources().getDimension(R.dimen.pdf_title_text_size));
    actionTextSize = typedArray.getDimension(R.styleable.ActionSheetContent_asActionTextSize,
        getResources().getDimension(R.dimen.pdf_action_text_size));
  }

  private void addTitle(Context context) {
    titleView = new TextView(context);
    FrameLayout.LayoutParams titleLp =
        new FrameLayout.LayoutParams((int) width, FrameLayout.LayoutParams.WRAP_CONTENT);
    titleView.setGravity(Gravity.LEFT);
    titleView.setClickable(false);
    int paddingVertical = (int) (getContext().getResources().getDimension(R.dimen.pdf_title_padding_vertical));
    titleView.setPadding(paddingVertical, paddingVertical, paddingVertical, paddingVertical);
    titleView.setBackgroundResource(R.drawable.bg_top_rounded);
    titleView.setTextColor(titleColor);
    titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
    addView(titleView, titleLp);
  }

  private void addActionContainer(Context context) {
    actionContainer = new LinearLayout(context);
    LayoutParams actionContainerLp = new LayoutParams((int) width,
        LayoutParams.WRAP_CONTENT);
    actionContainer.setBackgroundResource(R.drawable.bg_bottom_rounded);
    actionContainer.setOrientation(VERTICAL);
    actionContainer.setPadding(0, 0, 0, 0);
    addView(actionContainer, actionContainerLp);
  }

  public void setTitle(String title) {
    if (titleView != null) {
      titleView.setText(title);
    }
  }

  public String getTitle() throws NullPointerException {
    if (titleView != null) {
      return titleView.getText().toString();
    } else {
      throw new NullPointerException();
    }
  }

  public void addActionView(final String title, ActionSheet.Style style,
                            final OnActionListener listener) {
    // Adds its divider above
    addDividerView();

    Button actionButton = new Button(
        new android.view.ContextThemeWrapper(getContext(), R.style.DefaultTheme_Action), null,
        R.style.DefaultTheme_Action);

    FrameLayout.LayoutParams buttonLp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.WRAP_CONTENT);
    actionContainer.addView(actionButton, buttonLp);

    actionButton.setText(title);
    actionButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, actionTextSize);

    // Sets text color based on action style
    if (style == ActionSheet.Style.DEFAULT) {
      actionButton.setTextColor(defaultActionColor);
    } else if (style == ActionSheet.Style.DESTRUCTIVE) {
      actionButton.setTextColor(destructiveActionColor);
    }

    //Sets button callback to handle to user's click
    actionButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        listener.onSelected(actionSheet, title);
      }
    });
  }

  /**
   * Draws a line between action buttons
   */
  private void addDividerView() {
    int dividerHeight = (int) (getContext().getResources().getDimension(R.dimen.pdf_divider_height));

    View divider = new View(getContext());
    LayoutParams dividerBarLp = new LayoutParams(LayoutParams.MATCH_PARENT, dividerHeight);

    actionContainer.addView(divider, dividerBarLp);
    divider.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.pdf_light_gray));
  }

  private ImageView arrowView;

  public void addArrow(Arrow style) {
    if (arrowView != null) {
      return;
    }
    arrowView = new ImageView(getContext());
    switch (style) {
      case UP:
        break;
      case DOWN:
        arrowView.setBackgroundResource(R.drawable.ic_down);
        break;
      case RIGHT:
        break;
      case LEFT:
        break;
    }
    int size = (int) (getContext().getResources().getDimension(R.dimen.pdf_arrow_size));
    LayoutParams arrowLp =
        new LayoutParams(size, size);
    addView(arrowView, arrowLp);
  }
}
