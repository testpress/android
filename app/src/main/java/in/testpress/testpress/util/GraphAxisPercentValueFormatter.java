package in.testpress.testpress.util;

import com.github.testpress.mikephil.charting.components.AxisBase;
import com.github.testpress.mikephil.charting.data.Entry;
import com.github.testpress.mikephil.charting.formatter.PercentFormatter;
import com.github.testpress.mikephil.charting.utils.ViewPortHandler;

public class GraphAxisPercentValueFormatter extends PercentFormatter {

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                                    ViewPortHandler viewPortHandler) {
        if (value > 15) {
            return super.getFormattedValue(value, entry, dataSetIndex, viewPortHandler);
        } else {
            return "";
        }
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if (value > 15) {
            return super.getFormattedValue(value, axis);
        } else {
            return "";
        }
    }
}
