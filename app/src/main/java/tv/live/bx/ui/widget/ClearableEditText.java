package tv.live.bx.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class ClearableEditText extends EditText {

	private OnFocusChangeListener outListener;
	private boolean performClear = false;

	public ClearableEditText(Context ctx) {
		super(ctx);
		init();
	}

	public ClearableEditText(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		init();
	}

	public ClearableEditText(Context ctx, AttributeSet attrs, int defaultSet) {
		super(ctx, attrs, defaultSet);

		init();
	}

	private void init() {
		super.setOnFocusChangeListener(onFocusChange);
		addTextChangedListener(onInnerWatcher);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			float x = event.getX();
			if (x >= getWidth() - getCompoundPaddingRight()
					- getCompoundDrawablePadding()
					&& x < getWidth() - getPaddingRight() && isFocused()) {
				performClear = true;
				showOrHideCloseBtn(true);
			}

			break;
		case MotionEvent.ACTION_MOVE:
			float moveX = event.getX();
			if (moveX > getWidth() - getPaddingRight() + 10
					|| moveX < getWidth() - getCompoundPaddingRight()
							- getCompoundDrawablePadding() - 10) {
				performClear = false;
				showOrHideCloseBtn(false);
			}
			break;
		case MotionEvent.ACTION_UP:
			if (performClear) {
				clearText();
				performClear = false;
			}
			performClick();
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	private void clearText() {
		setText("");
	}

	@Override
	public void setOnFocusChangeListener(OnFocusChangeListener l) {
		outListener = l;
	}

	private void showOrHideCloseBtn(boolean pressed) {

		Drawable[] drawables = getCompoundDrawables();
		int drawablePd = getCompoundDrawablePadding();

		if (isFocused() && getText().toString().length() > 0) {
//			if (pressed)
//				setCompoundDrawablesWithIntrinsicBounds(
//						drawables[0],
//						null,
//						getResources().getDrawable(
//								R.drawable.edittext_del_pressed), null);
//			else
//				setCompoundDrawablesWithIntrinsicBounds(drawables[0], null,
//						getResources().getDrawable(R.drawable.edittext_del),
//						null);
			setCompoundDrawablePadding(drawablePd != 0 ? drawablePd : 10);
		} else {
			setCompoundDrawablesWithIntrinsicBounds(drawables[0], null, null,
					null);
			setCompoundDrawablePadding(drawablePd);
		}
	}

	private OnFocusChangeListener onFocusChange = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (outListener != null)
				outListener.onFocusChange(v, hasFocus);

			showOrHideCloseBtn(false);
		}
	};

	private TextWatcher onInnerWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			showOrHideCloseBtn(false);
		}
	};
}
