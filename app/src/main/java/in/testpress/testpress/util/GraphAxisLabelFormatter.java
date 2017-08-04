package in.testpress.testpress.util;

import com.github.testpress.mikephil.charting.components.AxisBase;
import com.github.testpress.mikephil.charting.formatter.AxisValueFormatter;

import java.util.ArrayList;

public class GraphAxisLabelFormatter implements AxisValueFormatter {

    private ArrayList<String> mValues;
    private int interval;

    public GraphAxisLabelFormatter(ArrayList<String> values, int interval) {
        this.mValues = values;
        this.interval = interval;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if (interval > 1) {
            return Strings.ellipsize(mValues.get((int) ((value + interval) / interval)), 16);
        } else {
            return Strings.ellipsize(mValues.get((int) value), 16);
        }
    }

    @Override
    public int getDecimalDigits() {
        return 0;
    }
}
