package cilab.utdallas.edu.ccimobile;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.scichart.charting.model.dataSeries.XyDataSeries;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.axes.IAxis;
import com.scichart.charting.visuals.renderableSeries.FastColumnRenderableSeries;
import com.scichart.core.framework.UpdateSuspender;
import com.scichart.core.model.DoubleValues;
import com.scichart.drawing.utility.ColorUtil;
import com.scichart.extensions.builders.SciChartBuilder;

import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class TestingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testing);

        SciChartSurface surface = findViewById(R.id.chartView);

        // Licensing SciChartSurface
        try {
            surface.setRuntimeLicenseKeyFromResource(this, "app\\src\\main\\res\\raw\\license.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize the SciChartBuilder
        SciChartBuilder.init(this);

        // Obtain the SciChartBuilder instance
        final SciChartBuilder sciChartBuilder = SciChartBuilder.instance();

        // Create a numeric X axis
        final IAxis xAxis = sciChartBuilder.newNumericAxis()
                .withAxisTitle("Electrode")
                .withVisibleRange(1, 22)
                .build();

        // Create a numeric Y axis
        final IAxis yAxis = sciChartBuilder.newNumericAxis()
                .withAxisTitle("Current").withVisibleRange(0, 1).build();

        // Add the Y axis to the YAxes collection of the surface
        Collections.addAll(surface.getYAxes(), yAxis);

        // Add the X axis to the XAxes collection of the surface
        Collections.addAll(surface.getXAxes(), xAxis);

        final XyDataSeries lineData = sciChartBuilder.newXyDataSeries(Integer.class, Double.class).build();

        final int dataCount = 22;
        for (int i = 0; i < dataCount; i++)
        {
            lineData.append(i+1, Math.abs(Math.sin(i * 2 * Math.PI / dataCount)));
        }

        // Set up an update
        final DoubleValues lineDoubleData = new DoubleValues(dataCount);
        lineDoubleData.setSize(dataCount);

        TimerTask updateDataTask = new TimerTask() {
            private double _phaseShift = 0.0;
            @Override
            public void run() {
                UpdateSuspender.using(surface, () -> {
                    // Fill the DoubleValues collections
                    for (int i = 0; i < dataCount; i++)
                    {
                        lineDoubleData.set(i, Math.abs(Math.sin(i * 2 * Math.PI / dataCount + _phaseShift)));
                    }
                    // Update DataSeries using bunch update
                    lineData.updateRangeYAt(0, lineDoubleData);
                    surface.zoomExtents();
                });
                _phaseShift += 0.01;
            }
        };

        Timer timer = new Timer();
        long delay = 0;
        long interval = 50; // updates every 10 ms
        timer.schedule(updateDataTask, delay, interval);

        // Create and configure the Column Chart Series
        final FastColumnRenderableSeries columnSeries = sciChartBuilder.newColumnSeries()
                .withStrokeStyle(0xA99A8A)
                .withDataPointWidth(1)
                .withLinearGradientColors(ColorUtil.LightSteelBlue, ColorUtil.SteelBlue)
                .withDataSeries(lineData)
                .build();

        // Add the chart series to the RenderableSeriesCollection of the surface
        Collections.addAll(surface.getRenderableSeries(), columnSeries);

        // Should be called at the end of chart set up
        surface.zoomExtents();

    }

}

//        TimerTask updateDataTask = new TimerTask() {
//            //private double _phaseShift = 0.0;
//            @Override
//            public void run() {
//                UpdateSuspender.using(surface, () -> {
//                    // zero
//                    for (int i = 0; i < dataCount; i++)
//                    {
//                        lineIntegerData.set(i, 0);
//                    }
//
//                    // current values
//                    for (int i : leftStimuli.Electrodes) {
//                        lineIntegerData.set(i, leftStimuli.Amplitudes[i]);
//                    }
//
//
//                    // Update DataSeries using bunch update
//                    lineData.updateRangeYAt(0, lineIntegerData);
//                    surface.zoomExtents();
//                });
//                //_phaseShift += 0.01;
//            }
//        };