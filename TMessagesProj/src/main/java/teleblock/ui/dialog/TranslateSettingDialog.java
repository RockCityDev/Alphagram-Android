package teleblock.ui.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;

import com.blankj.utilcode.util.LanguageUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.LayoutHelper;

import teleblock.model.TranslateEntity;
import teleblock.ui.adapter.TranslateLanguageAdapter;
import teleblock.util.MMKVUtil;

/**
 * 翻译设置
 */
public class TranslateSettingDialog extends BaseBottomSheetDialog implements View.OnClickListener {

    private ChatActivity chatActivity;
    private TextView tvTranslateTitle;
    private AppCompatSpinner mSpinnerTranslateSource;
    private AppCompatSpinner mSpinnerTranslateTarget;
    private TextView mTvTranslateClose;
    private TextView mTvTranslateEngine;
    private FrameLayout mflTranslateSwitch;
    private TextCheckCell textCheckCell;

    private String sourceLan = "";//自动

    public TranslateSettingDialog(@NonNull ChatActivity chatActivity) {
        super(chatActivity.getParentActivity());
        this.chatActivity = chatActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_translate_setting);

        initView();
        initData();
    }

    private void initView() {
        tvTranslateTitle = findViewById(R.id.tv_translate_title);
        mSpinnerTranslateSource = findViewById(R.id.spinner_translate_source);
        mSpinnerTranslateTarget = findViewById(R.id.spinner_translate_target);
        mflTranslateSwitch = findViewById(R.id.fl_translate_switch);
        mTvTranslateClose = findViewById(R.id.tv_translate_close);
        mTvTranslateEngine = findViewById(R.id.tv_translate_engine);

        textCheckCell = new TextCheckCell(getContext());
        mflTranslateSwitch.addView(textCheckCell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        mflTranslateSwitch.setOnClickListener(this);
        mTvTranslateClose.setOnClickListener(this);
        mTvTranslateEngine.setOnClickListener(this);
    }

    private void initData() {
        tvTranslateTitle.setText(LocaleController.getString("translate_setting_title", R.string.translate_setting_title));
        mTvTranslateClose.setText(LocaleController.getString("translate_dialog_close", R.string.translate_dialog_close));
        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("translate_switch_title", R.string.translate_switch_title), LocaleController.getString("translate_switch_info", R.string.translate_switch_info), MMKVUtil.chatTranslationSwitch(), false, false);

        mSpinnerTranslateSource.setAdapter(new TranslateLanguageAdapter(getContext(), true));
        mSpinnerTranslateSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TranslateEntity translateEntity = (TranslateEntity) mSpinnerTranslateSource.getItemAtPosition(position);
                sourceLan = translateEntity.getCode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpinnerTranslateTarget.setAdapter(new TranslateLanguageAdapter(getContext(), false));
        mSpinnerTranslateTarget.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TranslateEntity translateEntity = (TranslateEntity) mSpinnerTranslateTarget.getItemAtPosition(position);
                if (translateEntity.getCode().equals(MMKVUtil.getTranslateCode())) return;
                MMKVUtil.setTranslateCode(translateEntity.getCode());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void show() {
        super.show();
        setSpinnerItemSelectedByValue(mSpinnerTranslateTarget);
    }

    private void setSpinnerItemSelectedByValue(Spinner spinner) {
        String code = MMKVUtil.getTranslateCode();
        // 首次使用默认选中本地语言
        if (TextUtils.isEmpty(code)) {
            code = LanguageUtils.getAppContextLanguage().getLanguage();
            if ("zh".equals(code)) {
                code += "-" + LanguageUtils.getSystemLanguage().getCountry();
            }
        }
        SpinnerAdapter apsAdapter = spinner.getAdapter();
        for (int i = 0; i < apsAdapter.getCount(); i++) {
            TranslateEntity translateEntity = (TranslateEntity) apsAdapter.getItem(i);
            if (code.equalsIgnoreCase(translateEntity.getCode())) {
                // 默认选中上次使用的
                spinner.setSelection(i, true);
                MMKVUtil.setTranslateCode(translateEntity.getCode());
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mflTranslateSwitch)) {
            MMKVUtil.chatTranslationSwitch(!textCheckCell.isChecked());
            textCheckCell.setChecked(MMKVUtil.chatTranslationSwitch());
            chatActivity.chatAdapter.notifyDataSetChanged(true);
        } else if (v.equals(mTvTranslateClose)) {
            dismiss();
        } else if (v.equals(mTvTranslateEngine)) {

        }
    }
}