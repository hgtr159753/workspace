package com.smsf.recordtrancharacter.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smsf.recordtrancharacter.R;
import com.smsf.recordtrancharacter.Utils.FSScreen;


/**
 * Created by Administrator on 13-7-29. 弹出提示框
 */
public class MSDialog extends Dialog {
	public static final int WIDTH = 380 * 2 / 3;
	public static final int WRAP_CONTENT = WindowManager.LayoutParams.WRAP_CONTENT;

	private LinearLayout _contentLayout;
	private Button _positiveButton;
	private Button _negativeButton;
	private TextView title;
	/**
	 * 两个按钮中间的线
	 */
	private View mButtonLine;
	private View mBottomLine;
	private ImageView topIco;
	private LinearLayout _bottomLayout = null;

	private Context cxt = null;
	private LayoutInflater inflater = null;
	private DisplayMetrics dm = null;
	private Window window;
	private int width;

	private LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT);

	public MSDialog(Context context) {
		super(context, R.style.msDialogTheme);
		init(context, WRAP_CONTENT, WRAP_CONTENT);
	}

	public MSDialog(Context context, int width, int height) {
		super(context, R.style.msDialogTheme);
		init(context, width, height);
	}

	private void init(Context cxt, int width, int height) {
		window = getWindow();
		window.setFlags(WindowManager.LayoutParams.SCREEN_ORIENTATION_CHANGED,
				WindowManager.LayoutParams.SCREEN_ORIENTATION_CHANGED);
		window.setContentView(R.layout.dialog_default);
		this.cxt = cxt;
		dm = new DisplayMetrics();

		window.getWindowManager().getDefaultDisplay().getMetrics(dm);
		WindowManager.LayoutParams wlp = window.getAttributes();
		
		this.width = (int) (FSScreen.pixelWidth - 50 * 2 * dm.density);
		
		wlp.width = (int) FSScreen.pixelWidth;
		wlp.height = WRAP_CONTENT;
		window.setAttributes(wlp);

		inflater = LayoutInflater.from(cxt);

		_contentLayout = findViewById(R.id.dialog_content_main);
		_contentLayout.setLayoutParams(params);
		_positiveButton =  findViewById(R.id.dialog_positive_button);
		_negativeButton
				= findViewById(R.id.dialog_negative_button);
		mButtonLine = findViewById(R.id.dialog_button_line);
		mBottomLine = findViewById(R.id.dialog_bottom_line);
		_bottomLayout = findViewById(R.id.dialog_bottom);
		_bottomLayout.setLayoutParams(params);
		topIco =findViewById(R.id.dialog_top_img);

	}

	/**
	 * 在dialog的大小
	 * 
	 * @param view
	 */
	public MSDialog setView(View view) {
		LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(width,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		setView(view, lpp);
		return this;
	}

	/**
	 * 在dialog的大小
	 * 
	 * @param view
	 */
	public MSDialog setView(View view, LinearLayout.LayoutParams lp) {
		_contentLayout.setVisibility(View.VISIBLE);
		_contentLayout.removeAllViews();
		_contentLayout.addView(view, lp);
		return this;
	}

	/**
	 * @param width
	 * @param height
	 * @return
	 */
	public MSDialog setDialogSize(int width, int height) {
		WindowManager.LayoutParams lp = window.getAttributes();
		float density = dm.density;
		lp.width = (int) (width * density); // 宽度
		if (height != 0) {
			lp.height = (int) (height * density); // 高度
		}
		window.setAttributes(lp);
		return this;
	}

	public LinearLayout getContentLayout() {
		return _contentLayout;
	}

	/**
	 * 设置顶部图片
	 */
	public MSDialog setTopIco() {
		topIco.setVisibility(View.VISIBLE);
		return this;
	}

	/**
	 * 设置顶部图片
	 */
	public MSDialog setTopIco(int resId) {
		topIco.setVisibility(View.VISIBLE);
		topIco.setImageResource(resId);
		return this;
	}

	/**
	 * 设置顶部图片
	 */
	public MSDialog setTopIco(Bitmap bitmap) {
		topIco.setVisibility(View.VISIBLE);
		topIco.setImageBitmap(bitmap);
		return this;
	}

	/**
	 * 设置标题
	 * */
	public MSDialog setTopTitle(CharSequence str){
		TextView title= (TextView) findViewById(R.id.dialog_top_title);
		title.setText(str);
		title.setVisibility(View.VISIBLE);
		return  this;
	}


	/**
	 * @param str
	 * @return
	 */
	public MSDialog setMessage(CharSequence str) {
		TextView text = (TextView) inflater.inflate(R.layout.dialog_textview,
				null);
		text.setText(str);
		setView(text);
		return this;
	}



	/**
	 * @param str
	 * @return 字体加粗 style 字体大小
	 */
	public MSDialog setMessage(CharSequence str, boolean isStyle, int size) {
		TextView text = (TextView) inflater.inflate(R.layout.dialog_textview,
				null);
		text.setText(str);
		text.setTextSize(size);
		if (isStyle){
			TextPaint tp = text .getPaint();
			tp.setFakeBoldText(true);
		}
		setView(text);
		return this;
	}





	/**
	 * @param str
	 * @param red
	 * @return
	 */
	public MSDialog setMessage(CharSequence str, String red) {
		TextView text = (TextView) inflater.inflate(R.layout.dialog_textview,
				null);
		SpannableString mSpannableString = new SpannableString(str);
		mSpannableString.setSpan(new ForegroundColorSpan(getContext()
				.getResources().getColor(R.color.red)), 8, str.length() - 2,
				Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		text.setText(mSpannableString);
		setView(text);
		return this;
	}

	/**
	 * @param str
	 * @param red
	 * @return
	 */
	public MSDialog setMessage(CharSequence str, int red) {
		TextView text = (TextView) inflater.inflate(R.layout.dialog_textview,
				null);
		SpannableString mSpannableString = new SpannableString(str);
		mSpannableString.setSpan(new ForegroundColorSpan(getContext()
				.getResources().getColor(red)), 8, str.length() - 3,
				Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		text.setText(mSpannableString);
		setView(text);
		return this;
	}

	/**
	 * @param str
	 * @param textsize
	 * @param textcolor
	 * @return
	 */
	public MSDialog setMessage(CharSequence str, float textsize, int textcolor) {
		TextView text = (TextView) inflater.inflate(R.layout.dialog_textview,
				null);
		text.setText(str);
		if (textsize != 0)
			text.setTextSize(textsize);
		if (textcolor != 0)
			text.setTextColor(textcolor);
		setView(text);
		return this;
	}

	/**
	 * 设置左边按钮文本、文本颜色、背景及点击事件
	 * 
	 * @param message
	 * @param textColor
	 *            0:默认
	 * @param textSize
	 *            0:默认
	 * @param backgroundResource
	 *            0：默认
	 * @param listener
	 * @return
	 */
	public MSDialog setPositiveButton(CharSequence message, int textColor,
                                      float textSize, int backgroundResource,
                                      Button.OnClickListener listener) {
		_positiveButton.setText(message);
		if (0 != textColor) {
			_positiveButton.setTextColor(textColor);
		}
		if (0 != textSize) {
			_positiveButton.setTextSize(textSize);
		}
		if (0 != backgroundResource) {
			_positiveButton.setBackgroundResource(backgroundResource);
		}
		setPositiveButton(listener);
		return this;
	}

	/**
	 * 设置左边按钮文本、背景及点击事件
	 * 
	 * @param message
	 * @param backgroundResource
	 * @param listener
	 * @return
	 */
	public MSDialog setPositiveButton(CharSequence message,
                                      int backgroundResource, Button.OnClickListener listener) {
		_positiveButton.setText(message);
		_positiveButton.setBackgroundResource(backgroundResource);
		setPositiveButton(listener);
		return this;
	}

	/**
	 * 设置左边按钮文本及点击事件
	 * 
	 * @param message
	 * @param listener
	 * @return
	 */
	public MSDialog setPositiveButton(CharSequence message,
                                      Button.OnClickListener listener) {
		_positiveButton.setText(message);
		setPositiveButton(listener);
		return this;
	}

	/**
	 * 设置左边按钮文本及点击事件
	 * 
	 * @param resId
	 * @param listener
	 * @return
	 */
	public MSDialog setPositiveButton(int resId,
			Button.OnClickListener listener) {
		_positiveButton.setText(resId);
		setPositiveButton(listener);
		return this;
	}

	/**
	 * 设置左边按钮点击事件
	 * 
	 * @param listener
	 * @return
	 */
	public MSDialog setPositiveButton(Button.OnClickListener listener) {
		_positiveButton.setVisibility(View.VISIBLE);
		_bottomLayout.setVisibility(View.VISIBLE);
		mBottomLine.setVisibility(View.VISIBLE);
		if(_negativeButton.getVisibility() == View.VISIBLE) {
			mButtonLine.setVisibility(View.VISIBLE);
		}
		_positiveButton.setOnClickListener(listener);
		return this;
	}

	/**
	 * 设置右边按钮文本、文本颜色、背景及点击事件
	 * 
	 * @param message
	 * @param textColor
	 *            0:默认
	 * @param textSize
	 *            0:默认
	 * @param backgroundResource
	 *            0:默认
	 * @param listener
	 * @return
	 */
	public MSDialog setNegativeButton(CharSequence message, int textColor,
                                      float textSize, int backgroundResource,
                                      Button.OnClickListener listener) {
		_negativeButton.setText(message);
		if (0 != textColor) {
			_negativeButton.setTextColor(textColor);
		}
		if (0 != textSize) {
			_negativeButton.setTextSize(textColor);
		}
		if (0 != backgroundResource) {
			_negativeButton.setBackgroundResource(backgroundResource);
		}
		setNegativeButton(listener);
		return this;
	}

	/**
	 * 设置右边按钮文本、背景及点击事件
	 * 
	 * @param message
	 * @param backgroundResource
	 *            0:默认
	 * @param listener
	 * @return
	 */
	public MSDialog setNegativeButton(CharSequence message,
                                      int backgroundResource, Button.OnClickListener listener) {
		_negativeButton.setText(message);
		if (0 != backgroundResource) {
			_negativeButton.setBackgroundResource(backgroundResource);
		}
		setNegativeButton(listener);
		return this;
	}

	/**
	 * 设置右边按钮文本、背景及点击事件
	 * 
	 * @param message
	 * @param listener
	 * @return
	 */
	public MSDialog setNegativeButton(CharSequence message,
                                      Button.OnClickListener listener) {
		_negativeButton.setText(message);
		setNegativeButton(listener);
		return this;
	}

	/**
	 * 设置右边按钮文本、背景及点击事件带文字背景
	 *
	 * @param message
	 * @param listener
	 * @return
	 */
	public MSDialog setNegativeButton(CharSequence message,
                                      Button.OnClickListener listener, int color) {
		_negativeButton.setText(message);
		_negativeButton.setTextColor(color);
		setNegativeButton(listener);
		return this;
	}



	/**
	 * 设置右边按钮文本及点击事件
	 * 
	 * @param resId
	 * @param listener
	 * @return
	 */
	public MSDialog setNegativeButton(int resId,
			Button.OnClickListener listener) {
		_negativeButton.setText(resId);
		setNegativeButton(listener);
		return this;
	}

	/**
	 * 设置右边按钮点击事件
	 * 
	 * @param listener
	 * @return
	 */
	public MSDialog setNegativeButton(Button.OnClickListener listener) {
		_negativeButton.setVisibility(View.VISIBLE);
		_bottomLayout.setVisibility(View.VISIBLE);
		mBottomLine.setVisibility(View.VISIBLE);
		_negativeButton.setOnClickListener(listener);
		if(_positiveButton.getVisibility() == View.VISIBLE) {
			mButtonLine.setVisibility(View.VISIBLE);
		}
		return this;
	}

	/**
	 * 物理键返回调用接口
	 * 
	 * @param listener
	 * @return
	 */
	public MSDialog setOnCMCCDialogCancelListener(
			OnCancelListener listener) {
		setOnCancelListener(listener);
		return this;
	}

}
