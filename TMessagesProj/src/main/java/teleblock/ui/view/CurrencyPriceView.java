package teleblock.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.coingecko.domain.Coins.CoinFullData;
import com.coingecko.domain.Coins.MarketChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.ruffian.library.widget.RTextView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewCurrencyPriceBinding;

import java.util.ArrayList;
import java.util.List;

import teleblock.manager.CoinGeckoManager;
import teleblock.util.StringUtil;

/**
 * 创建日期：2022/6/9
 * 描述：
 */
public class CurrencyPriceView extends FrameLayout implements RadioGroup.OnCheckedChangeListener, OnChartValueSelectedListener {

    private final static int ONE_DAY = 1;
    private final static int ONE_WEEK = 7;
    private final static int ONE_MONTH = 30;
    private final static int THREE_MONTH = 90;
    private final static int ONE_YEAR = 365;
    private final static int MAX_DAY = 1000;

    private ViewCurrencyPriceBinding binding;
    private String id;
    private int day;
    private SparseArray<ArrayList<Entry>> valuesArray = new SparseArray<>();

    public CurrencyPriceView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
        initData();
    }

    private void initView() {
        binding = ViewCurrencyPriceBinding.inflate(LayoutInflater.from(getContext()), this, true);

        binding.rb1d.setText(LocaleController.getString("currency_price_1d", R.string.currency_price_1d));
        binding.rb1w.setText(LocaleController.getString("currency_price_1w", R.string.currency_price_1w));
        binding.rb1m.setText(LocaleController.getString("currency_price_1m", R.string.currency_price_1m));
        binding.rb3m.setText(LocaleController.getString("currency_price_3m", R.string.currency_price_3m));
        binding.rb1y.setText(LocaleController.getString("currency_price_1y", R.string.currency_price_1y));
        binding.rbMax.setText(LocaleController.getString("currency_price_max", R.string.currency_price_max));
    }

    private void initData() {
        binding.radioGroup.setOnCheckedChangeListener(this);
        initLineChart();
    }

    private void initLineChart() {
        /************* 图表样式 *************/
        // 设置图表为空时应显示的文本
        binding.lineChart.setNoDataText("暂无数据");
        // 设置将覆盖整个图表视图的背景颜色
        binding.lineChart.setBackgroundColor(Color.WHITE);
        //允许启用/禁用图表的所有可能的触摸交互
        binding.lineChart.setTouchEnabled(true);
        // 用于通过触摸突出显示值时的回调
        binding.lineChart.setOnChartValueSelectedListener(this);
        //如果启用，将绘制图表绘图区域后面的背景矩形
        binding.lineChart.setDrawGridBackground(false);
        //启用/禁用图表的拖动（平移）
        binding.lineChart.setDragEnabled(true);
        //启用/禁用两个轴上图表的缩放
        binding.lineChart.setScaleEnabled(true);
        //如果设置为 true，则启用捏合缩放。如果禁用，可以分别缩放 x 轴和 y 轴
        binding.lineChart.setPinchZoom(true);
        // 动画水平和垂直轴，导致左/右底部/顶部堆积
        binding.lineChart.animateXY(1500, 1500);
        // 图表的图例(只有当数据集存在时候才生效)
        Legend legend = binding.lineChart.getLegend();
        legend.setEnabled(false);
        // 指示是否启用y轴上的自动缩放的标志。如果启用，则每当视口更改时，y 轴都会自动调整为当前 x 轴范围的最小和最大 y 值。
        binding.lineChart.setAutoScaleMinMaxEnabled(true);
        // 禁用描述文本(显示在图表右下角的描述文本相关的所有信息)
        binding.lineChart.getDescription().setEnabled(false);
        // 禁用右y轴。在水平条形图中，这是底轴
        binding.lineChart.getAxisRight().setEnabled(false);
        // 选择值时创建要显示的标记框
        MyMarkerView myMarkerView = new MyMarkerView(getContext());
        myMarkerView.setChartView(binding.lineChart);
        binding.lineChart.setMarker(myMarkerView);

        /************* X轴样式 *************/
        XAxis xAxis = binding.lineChart.getXAxis();
        //允许以虚线模式绘制网格线，例如像“ - - - - - - ”
        //“lineLength”控制线段的长度，“spaceLength”控制线之间的间隔，“phase”控制起始点。
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        // 将此设置为 true 以启用为轴绘制网格线
        xAxis.setDrawGridLines(false);
        // 设置应绘制轴标签的位置
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                switch (day) {
                    case ONE_DAY:
                        return TimeUtils.millis2String((long) value, "HH:mm");
                    case ONE_WEEK:
                    case ONE_MONTH:
                    case THREE_MONTH:
                    case ONE_YEAR:
                        return TimeUtils.millis2String((long) value, "MM dd");
                    case MAX_DAY:
                        return TimeUtils.millis2String((long) value, "yyyy");
                }
                return super.getFormattedValue(value);
            }
        });

        /************* Y轴样式 *************/
        YAxis yAxis = binding.lineChart.getAxisLeft();
        //允许以虚线模式绘制网格线，例如像“ - - - - - - ”
        //“lineLength”控制线段的长度，“spaceLength”控制线之间的间隔，“phase”控制起始点。
        yAxis.enableGridDashedLine(10f, 10f, 0f);
        // 设置应绘制轴标签的位置
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        // 如果应绘制轴（轴线）旁边的线，请将其设置为 true
        yAxis.setDrawAxisLine(false);
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "$" + value;
            }
        });
    }

    public void getMarketChartData(String id, Integer day) {
        this.id = id;
        this.day = day;
        if (valuesArray.get(day) != null) {
            setLineChartData(valuesArray.get(day));
            return;
        }
        CoinGeckoManager.getInstance().getCoinMarketChartById(id, day, new CoinGeckoManager.Callback<MarketChart>() {
            @Override
            public void onSuccess(MarketChart data) {
                List<List<String>> prices = data.getPrices();
                ArrayList<Entry> values = new ArrayList<>();
                for (List<String> strings : prices) {
                    values.add(new Entry(Float.parseFloat(strings.get(0)), Float.parseFloat(strings.get(1))));
                }
                valuesArray.put(day, values);
                setLineChartData(values);
            }
        });
    }

    public void setLineChartData(List<Entry> values) {
        LineDataSet lineDataSet;
        if (binding.lineChart.getData() != null && binding.lineChart.getData().getDataSetCount() > 0) {
            lineDataSet = (LineDataSet) binding.lineChart.getData().getDataSetByIndex(0);
            lineDataSet.setValues(values);
            lineDataSet.notifyDataSetChanged();
            binding.lineChart.getData().notifyDataChanged();
            binding.lineChart.notifyDataSetChanged();
        } else {
            // 创建数据集并为其指定类型
            lineDataSet = new LineDataSet(values, "");
            // 如果为true，则在图表上绘制y图标
            lineDataSet.setDrawIcons(false);
            // 设置图表的线宽（最小值=0.2f，最大值=10f）；默认1f注意：线条越细==性能越好，线条越粗==性能越差
            lineDataSet.setLineWidth(1f);
            // 设置此数据集应使用的颜色
            lineDataSet.setColor(Color.parseColor("#39AFEA"));
            // 设置此数据集的所有圆形指示器应具有的颜色
            lineDataSet.setCircleColor(Color.WHITE);
            // 设置圆形值指示符的大小（半径），默认大小 = 4f
            lineDataSet.setCircleRadius(3.5f);
            // 将此设置为 true 以允许在此数据集的每个圆圈中绘制一个孔。如果设置为 false，将绘制填充的圆圈（无孔）
            lineDataSet.setDrawCircleHole(true);
            // 设置线圆（孔）的内圆的颜色
            lineDataSet.setCircleHoleColor(Color.parseColor("#39AFEA"));
            // 设置绘制圆的孔半径。默认半径=2f，最小值=0.5f
            lineDataSet.setCircleHoleRadius(2.5f);
            ///设置显示值的文字大小
            lineDataSet.setValueTextSize(9f);
            // 将此设置为 true 以允许通过触摸突出显示此特定DataSet
            lineDataSet.setHighlightEnabled(true);
            // 启用/禁用水平高亮指示线。如果禁用，则不绘制指标。
            lineDataSet.setDrawHorizontalHighlightIndicator(false);
            // 启用/禁用垂直高亮指示线。如果禁用，则不绘制指标
            lineDataSet.setDrawVerticalHighlightIndicator(true);
            // 设置用于绘制高亮指示线的颜色
            lineDataSet.setHighLightColor(Color.parseColor("#000000"));
            // 设置高亮显示线的宽度
            lineDataSet.setHighlightLineWidth(1f);
            // 设置范围背景填充
            lineDataSet.setDrawFilled(true);
            // 设置填充区域的颜色
            lineDataSet.setFillDrawable(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{Color.parseColor("#39AFEA"), Color.TRANSPARENT}));
            // 添加数据集
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(lineDataSet);
            // 使用数据集创建数据对象
            LineData data = new LineData(dataSets);
            // 设置数据
            binding.lineChart.setData(data);

        }
        binding.lineChart.invalidate();
    }

    public void setCoinData(CoinFullData data) {
        if (data.getMarketData() != null) {
            setPriceChange(binding.tv1hChange, data.getMarketData().getPriceChangePercentage1hInCurrency().get("usd"));
            setPriceChange(binding.tv24hChange, data.getMarketData().getPriceChangePercentage24h());
            setPriceChange(binding.tv7dChange, data.getMarketData().getPriceChangePercentage7d());
            setPriceChange(binding.tv14dChange, data.getMarketData().getPriceChangePercentage14d());
            setPriceChange(binding.tv30dChange, data.getMarketData().getPriceChangePercentage30d());
            setPriceChange(binding.tv1yChange, data.getMarketData().getPriceChangePercentage1y());
        }
    }

    private void setPriceChange(RTextView rTextView, double price) {
        String change = StringUtil.formatNumber(price, 2);
        rTextView.setText(change + "%");
        if (change.startsWith("-")) {
            rTextView.getHelper()
                    .setIconNormalLeft(ResourceUtils.getDrawable(R.mipmap.coin_change_decrease))
                    .setTextColorNormal(Color.parseColor("#FF2929"));
        } else {
            rTextView.getHelper()
                    .setIconNormalLeft(ResourceUtils.getDrawable(R.mipmap.coin_change_increase))
                    .setTextColorNormal(Color.parseColor("#38D103"));
        }

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_1d:
                getMarketChartData(id, ONE_DAY);
                break;
            case R.id.rb_1w:
                getMarketChartData(id, ONE_WEEK);
                break;
            case R.id.rb_1m:
                getMarketChartData(id, ONE_MONTH);
                break;
            case R.id.rb_3m:
                getMarketChartData(id, THREE_MONTH);
                break;
            case R.id.rb_1y:
                getMarketChartData(id, ONE_YEAR);
                break;
            case R.id.rb_max:
                getMarketChartData(id, MAX_DAY);
                break;
        }
    }

    /**
     * 在图表内选择了一个值时调用。
     *
     * @param e 选定的条目。
     * @param h 包含信息的对应高亮对象
     *          关于突出显示的位置
     */
    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    /**
     * 在未选择任何内容或进行“取消选择”时调用。
     */
    @Override
    public void onNothingSelected() {

    }

    private class MyMarkerView extends MarkerView {

        private TextView tvValue;
        private TextView tvDate;

        public MyMarkerView(Context context) {
            super(context, R.layout.layout_custom_marker);
            tvValue = findViewById(R.id.tv_value);
            tvDate = findViewById(R.id.tv_date);
        }

        /**
         * 此方法使指定的自定义 IMarker 可以在每次重绘 IMarker 时更新其内容。
         *
         * @param e         IMarker 所属的条目。这也可以是 Entry 的任何子类，例如 BarEntry 或
         *                  CandleEntry，只需在运行时进行转换。
         * @param highlight 高亮对象包含有关高亮值的信息，例如它的数据集索引、
         *                  选定范围或堆栈索引（仅堆积条形条目）。
         */
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            tvValue.setText("$" + e.getY());
            tvDate.setText(TimeUtils.millis2String((long) e.getX(), "MM dd，yyyy HH:mm:ss"));
            super.refreshContent(e, highlight);
        }

        /**
         * @return 您希望 IMarker 在 x 轴和 y 轴上具有的所需（一般）偏移量。
         * 通过返回 x: -(width / 2) 您将 IMarker 水平居中。
         * 通过返回 y: -(height / 2) 您将 IMarker 垂直居中。
         */
        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }
    }
}