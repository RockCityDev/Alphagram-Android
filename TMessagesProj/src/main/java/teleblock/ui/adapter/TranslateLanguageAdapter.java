package teleblock.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.util.ArrayList;
import java.util.List;

import teleblock.model.TranslateEntity;


public class TranslateLanguageAdapter extends BaseAdapter {

    private Context mContext;
    private List<TranslateEntity> mList;

    public TranslateLanguageAdapter(Context context, boolean sourceLan) {
        this.mContext = context;
        this.mList = loadData(sourceLan);
    }

    private List<TranslateEntity> loadData(boolean sourceLan) {
        List<TranslateEntity> translateEntityList = new ArrayList<>();
        if (sourceLan) {
            translateEntityList.add(new TranslateEntity("auto", LocaleController.getString("translate_language_auto", R.string.translate_language_auto)));
        }
        translateEntityList.add(new TranslateEntity("zh-TW", LocaleController.getString("translate_language_zh_tw", R.string.translate_language_zh_tw)));
        translateEntityList.add(new TranslateEntity("zh-CN", LocaleController.getString("translate_language_zh_cn", R.string.translate_language_zh_cn)));
        translateEntityList.add(new TranslateEntity("en", LocaleController.getString("translate_language_en", R.string.translate_language_en)));
        translateEntityList.add(new TranslateEntity("af", LocaleController.getString("translate_language_af", R.string.translate_language_af)));
        //translateEntityList.add(new TranslateEntity("sq", LocaleController.getString("translate_language_sq",R.string.translate_language_sq)));
        translateEntityList.add(new TranslateEntity("ar", LocaleController.getString("translate_language_ar", R.string.translate_language_ar)));
        translateEntityList.add(new TranslateEntity("hy", LocaleController.getString("translate_language_hy", R.string.translate_language_hy)));
        translateEntityList.add(new TranslateEntity("az", LocaleController.getString("translate_language_az", R.string.translate_language_az)));
        //translateEntityList.add(new TranslateEntity("eu", LocaleController.getString("translate_language_eu",R.string.translate_language_eu)));
        translateEntityList.add(new TranslateEntity("be", LocaleController.getString("translate_language_be", R.string.translate_language_be)));
        translateEntityList.add(new TranslateEntity("bn", LocaleController.getString("translate_language_bn", R.string.translate_language_bn)));
        //translateEntityList.add(new TranslateEntity("bs", LocaleController.getString("translate_language_bs",R.string.translate_language_bs)));
        translateEntityList.add(new TranslateEntity("bg", LocaleController.getString("translate_language_bg", R.string.translate_language_bg)));
        translateEntityList.add(new TranslateEntity("ca", LocaleController.getString("translate_language_ca", R.string.translate_language_ca)));
        //translateEntityList.add(new TranslateEntity("ceb", LocaleController.getString("translate_language_ceb",R.string.translate_language_ceb)));
        //translateEntityList.add(new TranslateEntity("ny", LocaleController.getString("translate_language_ny",R.string.translate_language_ny)));
        //translateEntityList.add(new TranslateEntity("co", LocaleController.getString("translate_language_co",R.string.translate_language_co)));
        translateEntityList.add(new TranslateEntity("hr", LocaleController.getString("translate_language_hr", R.string.translate_language_hr)));
        translateEntityList.add(new TranslateEntity("cs", LocaleController.getString("translate_language_cs", R.string.translate_language_cs)));
        translateEntityList.add(new TranslateEntity("da", LocaleController.getString("translate_language_da", R.string.translate_language_da)));
        translateEntityList.add(new TranslateEntity("nl", LocaleController.getString("translate_language_nl", R.string.translate_language_nl)));
        translateEntityList.add(new TranslateEntity("eo", LocaleController.getString("translate_language_eo", R.string.translate_language_eo)));
        translateEntityList.add(new TranslateEntity("et", LocaleController.getString("translate_language_et", R.string.translate_language_et)));
        translateEntityList.add(new TranslateEntity("tl", LocaleController.getString("translate_language_tl", R.string.translate_language_tl)));
        translateEntityList.add(new TranslateEntity("fi", LocaleController.getString("translate_language_fi", R.string.translate_language_fi)));
        translateEntityList.add(new TranslateEntity("fr", LocaleController.getString("translate_language_fr", R.string.translate_language_fr)));
        translateEntityList.add(new TranslateEntity("fy", LocaleController.getString("translate_language_fy", R.string.translate_language_fy)));
        translateEntityList.add(new TranslateEntity("gl", LocaleController.getString("translate_language_gl", R.string.translate_language_gl)));
        translateEntityList.add(new TranslateEntity("ka", LocaleController.getString("translate_language_ka", R.string.translate_language_ka)));
        translateEntityList.add(new TranslateEntity("de", LocaleController.getString("translate_language_de", R.string.translate_language_de)));
        translateEntityList.add(new TranslateEntity("el", LocaleController.getString("translate_language_el", R.string.translate_language_el)));
        translateEntityList.add(new TranslateEntity("gu", LocaleController.getString("translate_language_gu", R.string.translate_language_gu)));
        translateEntityList.add(new TranslateEntity("ht", LocaleController.getString("translate_language_ht", R.string.translate_language_ht)));
        translateEntityList.add(new TranslateEntity("ha", LocaleController.getString("translate_language_ha", R.string.translate_language_ha)));
        //translateEntityList.add(new TranslateEntity("haw", LocaleController.getString("translate_language_haw",R.string.translate_language_haw)));
        translateEntityList.add(new TranslateEntity("iw", LocaleController.getString("translate_language_iw", R.string.translate_language_iw)));
        translateEntityList.add(new TranslateEntity("hi", LocaleController.getString("translate_language_hi", R.string.translate_language_hi)));
        //translateEntityList.add(new TranslateEntity("hmn", LocaleController.getString("translate_language_hmn",R.string.translate_language_hmn)));
        translateEntityList.add(new TranslateEntity("hu", LocaleController.getString("translate_language_hu", R.string.translate_language_hu)));
        translateEntityList.add(new TranslateEntity("is", LocaleController.getString("translate_language_is", R.string.translate_language_is)));
        translateEntityList.add(new TranslateEntity("ig", LocaleController.getString("translate_language_ig", R.string.translate_language_ig)));
        translateEntityList.add(new TranslateEntity("id", LocaleController.getString("translate_language_id", R.string.translate_language_id)));
        translateEntityList.add(new TranslateEntity("ga", LocaleController.getString("translate_language_ga", R.string.translate_language_ga)));
        translateEntityList.add(new TranslateEntity("it", LocaleController.getString("translate_language_it", R.string.translate_language_it)));
        translateEntityList.add(new TranslateEntity("ja", LocaleController.getString("translate_language_ja", R.string.translate_language_ja)));
        translateEntityList.add(new TranslateEntity("jw", LocaleController.getString("translate_language_jw", R.string.translate_language_jw)));
        translateEntityList.add(new TranslateEntity("kn", LocaleController.getString("translate_language_kn", R.string.translate_language_kn)));
        translateEntityList.add(new TranslateEntity("kk", LocaleController.getString("translate_language_kk", R.string.translate_language_kk)));
        translateEntityList.add(new TranslateEntity("km", LocaleController.getString("translate_language_km", R.string.translate_language_km)));
        translateEntityList.add(new TranslateEntity("ko", LocaleController.getString("translate_language_ko", R.string.translate_language_ko)));
        translateEntityList.add(new TranslateEntity("ku", LocaleController.getString("translate_language_ku", R.string.translate_language_ku)));
        translateEntityList.add(new TranslateEntity("ky", LocaleController.getString("translate_language_ky", R.string.translate_language_ky)));
        translateEntityList.add(new TranslateEntity("lo", LocaleController.getString("translate_language_lo", R.string.translate_language_lo)));
        translateEntityList.add(new TranslateEntity("la", LocaleController.getString("translate_language_la", R.string.translate_language_la)));
        translateEntityList.add(new TranslateEntity("lv", LocaleController.getString("translate_language_lv", R.string.translate_language_lv)));
        translateEntityList.add(new TranslateEntity("lt", LocaleController.getString("translate_language_lt", R.string.translate_language_lt)));
        translateEntityList.add(new TranslateEntity("lb", LocaleController.getString("translate_language_lb", R.string.translate_language_lb)));
        translateEntityList.add(new TranslateEntity("mk", LocaleController.getString("translate_language_mk", R.string.translate_language_mk)));
        translateEntityList.add(new TranslateEntity("mg", LocaleController.getString("translate_language_mg", R.string.translate_language_mg)));
        translateEntityList.add(new TranslateEntity("ms", LocaleController.getString("translate_language_ms", R.string.translate_language_ms)));
        translateEntityList.add(new TranslateEntity("ml", LocaleController.getString("translate_language_ml", R.string.translate_language_ml)));
        translateEntityList.add(new TranslateEntity("mt", LocaleController.getString("translate_language_mt", R.string.translate_language_mt)));
        translateEntityList.add(new TranslateEntity("mi", LocaleController.getString("translate_language_mi", R.string.translate_language_mi)));
        translateEntityList.add(new TranslateEntity("mr", LocaleController.getString("translate_language_mr", R.string.translate_language_mr)));
        translateEntityList.add(new TranslateEntity("mn", LocaleController.getString("translate_language_mn", R.string.translate_language_mn)));
        translateEntityList.add(new TranslateEntity("my", LocaleController.getString("translate_language_my", R.string.translate_language_my)));
        translateEntityList.add(new TranslateEntity("ne", LocaleController.getString("translate_language_ne", R.string.translate_language_ne)));
        translateEntityList.add(new TranslateEntity("no", LocaleController.getString("translate_language_no", R.string.translate_language_no)));
        translateEntityList.add(new TranslateEntity("ps", LocaleController.getString("translate_language_ps", R.string.translate_language_ps)));
        translateEntityList.add(new TranslateEntity("fa", LocaleController.getString("translate_language_fa", R.string.translate_language_fa)));
        translateEntityList.add(new TranslateEntity("pl", LocaleController.getString("translate_language_pl", R.string.translate_language_pl)));
        translateEntityList.add(new TranslateEntity("pt", LocaleController.getString("translate_language_pt", R.string.translate_language_pt)));
        translateEntityList.add(new TranslateEntity("ma", LocaleController.getString("translate_language_ma", R.string.translate_language_ma)));
        translateEntityList.add(new TranslateEntity("ro", LocaleController.getString("translate_language_ro", R.string.translate_language_ro)));
        translateEntityList.add(new TranslateEntity("ru", LocaleController.getString("translate_language_ru", R.string.translate_language_ru)));
        translateEntityList.add(new TranslateEntity("gd", LocaleController.getString("translate_language_gd", R.string.translate_language_gd)));
        translateEntityList.add(new TranslateEntity("sr", LocaleController.getString("translate_language_sr", R.string.translate_language_sr)));
        translateEntityList.add(new TranslateEntity("st", LocaleController.getString("translate_language_st", R.string.translate_language_st)));
        translateEntityList.add(new TranslateEntity("sn", LocaleController.getString("translate_language_sn", R.string.translate_language_sn)));
        translateEntityList.add(new TranslateEntity("sd", LocaleController.getString("translate_language_sd", R.string.translate_language_sd)));
        translateEntityList.add(new TranslateEntity("si", LocaleController.getString("translate_language_si", R.string.translate_language_si)));
        translateEntityList.add(new TranslateEntity("sk", LocaleController.getString("translate_language_sk", R.string.translate_language_sk)));
        translateEntityList.add(new TranslateEntity("sl", LocaleController.getString("translate_language_sl", R.string.translate_language_sl)));
        translateEntityList.add(new TranslateEntity("so", LocaleController.getString("translate_language_so", R.string.translate_language_so)));
        translateEntityList.add(new TranslateEntity("es", LocaleController.getString("translate_language_es", R.string.translate_language_es)));
        translateEntityList.add(new TranslateEntity("su", LocaleController.getString("translate_language_su", R.string.translate_language_su)));
        translateEntityList.add(new TranslateEntity("sw", LocaleController.getString("translate_language_sw", R.string.translate_language_sw)));
        translateEntityList.add(new TranslateEntity("sv", LocaleController.getString("translate_language_sv", R.string.translate_language_sv)));
        translateEntityList.add(new TranslateEntity("tg", LocaleController.getString("translate_language_tg", R.string.translate_language_tg)));
        translateEntityList.add(new TranslateEntity("ta", LocaleController.getString("translate_language_ta", R.string.translate_language_ta)));
        translateEntityList.add(new TranslateEntity("te", LocaleController.getString("translate_language_te", R.string.translate_language_te)));
        translateEntityList.add(new TranslateEntity("th", LocaleController.getString("translate_language_th", R.string.translate_language_th)));
        translateEntityList.add(new TranslateEntity("tr", LocaleController.getString("translate_language_tr", R.string.translate_language_tr)));
        translateEntityList.add(new TranslateEntity("uk", LocaleController.getString("translate_language_uk", R.string.translate_language_uk)));
        translateEntityList.add(new TranslateEntity("uz", LocaleController.getString("translate_language_uz", R.string.translate_language_uz)));
        translateEntityList.add(new TranslateEntity("vi", LocaleController.getString("translate_language_vi", R.string.translate_language_vi)));
        translateEntityList.add(new TranslateEntity("cy", LocaleController.getString("translate_language_cy", R.string.translate_language_cy)));
        translateEntityList.add(new TranslateEntity("xh", LocaleController.getString("translate_language_xh", R.string.translate_language_xh)));
        translateEntityList.add(new TranslateEntity("yi", LocaleController.getString("translate_language_yi", R.string.translate_language_yi)));
        translateEntityList.add(new TranslateEntity("yo", LocaleController.getString("translate_language_yo", R.string.translate_language_yo)));
        translateEntityList.add(new TranslateEntity("zu", LocaleController.getString("translate_language_zu", R.string.translate_language_zu)));
        return translateEntityList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = new TextView(mContext);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(16);
        textView.setTextColor(Color.parseColor("#66A9E0"));
        textView.setText(mList.get(position).getLanguage());
        return textView;

    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_translate_language, null);
            convertView.setTag(holder);

            holder.tv_language = (TextView) convertView.findViewById(R.id.tv_language);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv_language.setText(mList.get(position).getLanguage());
        return convertView;
    }

    static class ViewHolder {
        private TextView tv_language;
    }
}