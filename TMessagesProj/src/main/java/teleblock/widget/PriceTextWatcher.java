package teleblock.widget;

/**
 * Created by 建荣 on 2017/11/28.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * 利用正则表达式限制EditText小数点前后位数和格式。
 */

public class PriceTextWatcher implements TextWatcher {

	// 输入前内容
	private String mBeforeText;
	// 输入后内容
	private String mAfterText;

	// 输入框
	private EditText mEditText;
	// 匹配器
	private Pattern mPattern;
	private static List<TextWatcher> mTextListWaiter = new ArrayList<TextWatcher>();

	public static void addTextChangedListener(TextWatcher watcher) {
		mTextListWaiter.add(watcher);
	}

	public PriceTextWatcher(EditText editText) {
		this.mEditText = editText;
		/**
		 * 正则表达式匹配
		 * 条件一:如果以0开始,那么小数点前最多只有1位
		 * 条件二:小数点后面最多只有18位
		 * 条件三:如果不以0开始,小数点前面最多只有10位
		 */
		mPattern = Pattern.compile("(([0]|(0[.]\\d{0,17}))|([1-9]\\d{0,9}(([.]\\d{0,17})?)))");
	}

	@Override
	public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		// Log.i(TAG, "beforeTextChanged:" + charSequence);
		mBeforeText = charSequence.toString();
	}

	@Override
	public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		// Log.i(TAG, "onTextChanged:" + charSequence);
		for (TextWatcher textWatcher : mTextListWaiter) {
			textWatcher.onTextChanged(mEditText.getText(), i, i1, i2);
		}

		if (TextUtils.isEmpty(charSequence))
			return;
		mAfterText = charSequence.toString();
		Matcher matcher = mPattern.matcher(mAfterText);
		if (matcher.matches()) {
			// Log.i(TAG, "匹配");
		} else {
			// Log.i(TAG, "不匹配");
			mEditText.setText(mBeforeText);
			// 游标移动到最后一位
			mEditText.setSelection(mEditText.length());
		}

	}

	@Override
	public void afterTextChanged(Editable editable) {

	}
}
