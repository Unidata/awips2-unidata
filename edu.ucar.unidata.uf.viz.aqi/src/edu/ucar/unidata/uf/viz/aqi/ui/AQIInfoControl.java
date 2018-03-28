package edu.ucar.unidata.uf.viz.aqi.ui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.geospatial.ReferencedCoordinate;
import com.raytheon.uf.viz.core.IExtent;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.IRenderableDisplay;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IPaintListener;
import com.raytheon.uf.viz.core.rsc.interrogation.Interrogatable;
import com.raytheon.uf.viz.core.rsc.interrogation.InterrogateMap;
import com.vividsolutions.jts.geom.Coordinate;

import edu.ucar.unidata.common.dataplugin.aqi.AQIRecord;
import edu.ucar.unidata.uf.viz.aqi.IAQIDataResource;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 18, 2014            mschenke     Initial creation
 * 
 * </pre>
 * 
 * @author mschenke
 * @version 1.0
 */

public class AQIInfoControl implements IPaintListener {
	
    private DecimalFormat format = new DecimalFormat("0.00");

    private Text mean;

    private double meanValue = Double.NaN;

    private Text median;

    private double medianValue = Double.NaN;

    private Text mode;

    private double modeValue = Double.NaN;

    private final AbstractVizResource<?, ?> resource;

    public AQIInfoControl(AbstractVizResource<?, ?> resource) {
        this.resource = resource;
    }

    public void initializeControl(Composite parent) {
        parent.setLayout(new GridLayout(2, false));

        GridData labelData = new GridData(SWT.RIGHT, SWT.CENTER, false, true);
        labelData.widthHint = 60;
        GridData textData = new GridData(SWT.FILL, SWT.FILL, true, true);
        textData.widthHint = 100;

        Label label = new Label(parent, SWT.RIGHT);
        label.setLayoutData(labelData);
        label.setText("Mean:");

        mean = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
        mean.setLayoutData(textData);
        mean.setText(valueToText(meanValue));

        label = new Label(parent, SWT.RIGHT);
        label.setLayoutData(labelData);
        label.setText("Median:");

        median = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
        median.setLayoutData(textData);
        median.setText(valueToText(medianValue));

        label = new Label(parent, SWT.RIGHT);
        label.setLayoutData(labelData);
        label.setText("Mode:");

        mode = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
        mode.setLayoutData(textData);
        mode.setText(valueToText(modeValue));

        resource.registerListener(this);
    }

    public void dispose() {
        resource.unregisterListener(this);
    }

    private String valueToText(double value) {
        if (Double.isNaN(value)) {
            return "?.??";
        }
        return format.format(value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.core.rsc.IPaintListener#painted(com.raytheon.uf.viz
     * .core.rsc.AbstractVizResource)
     */
    @Override
    public void painted(AbstractVizResource<?, ?> resource) {
        Interrogatable interrogatable = (Interrogatable) resource;
        InterrogateMap dataMap = interrogatable.interrogate(
                new ReferencedCoordinate(new Coordinate()), resource
                        .getDescriptor().getTimeForResource(resource),
                IAQIDataResource.AQI_RECORDS_INTERROGATE_KEY);
        AQIRecord[] records = dataMap
                .get(IAQIDataResource.AQI_RECORDS_INTERROGATE_KEY);
        if (records != null) {
            IDescriptor descriptor = resource.getDescriptor();
            IRenderableDisplay display = descriptor.getRenderableDisplay();
            IExtent filter = null;
            if (display != null) {
                filter = display.getExtent();
            }
            List<AQIRecord> visible = new ArrayList<AQIRecord>();
            for (Object obj : records) {
                AQIRecord record = (AQIRecord) obj;
                boolean add = true;
                if (filter != null) {
                    Coordinate location = record.getGeometry().getCoordinate();
                    double[] pixel = descriptor.worldToPixel(new double[] {
                            location.x, location.y });
                    add = filter.contains(pixel);
                }
                if (add == true) {
                    visible.add(record);
                }
            }

            if (visible.isEmpty()) {
                this.meanValue = this.medianValue = this.modeValue = Double.NaN;
            } else {
                Collections.sort(visible, new Comparator<AQIRecord>() {
                    @Override
                    public int compare(AQIRecord o1, AQIRecord o2) {
                        return Integer.compare(o1.getAqi(), o2.getAqi());
                    }
                });

                double modeAqi = Double.NaN;
                int modeCount = 0;
                Integer currentAqi = null;
                int currentCount = 0;
                double meanTotal = 0;

                int medianStartIndex = visible.size() / 2;
                int medianEndIndex = medianStartIndex;
                if (visible.size() % 2 == 0) {
                    medianStartIndex -= 1;
                }

                int i = 0;
                double medianTotal = 0;

                for (AQIRecord record : visible) {
                    int aqi = record.getAqi();

                    // For mean
                    meanTotal += aqi;

                    // For median
                    if (i >= medianStartIndex && i <= medianEndIndex) {
                        medianTotal += aqi;
                    }

                    // For mode
                    if (currentAqi == null) {
                        currentAqi = aqi;
                        modeAqi = aqi;
                    }

                    if (aqi != currentAqi) {
                        // Aqi changed, update modeAqi
                        if (modeCount < currentCount) {
                            modeAqi = currentAqi;
                            modeCount = currentCount;
                        }

                        currentCount = 0;
                        currentAqi = aqi;
                    }
                    currentCount += 1;

                    i += 1;
                }
                this.meanValue = meanTotal / visible.size();
                this.medianValue = medianTotal
                        / (medianEndIndex - medianStartIndex + 1);
                this.modeValue = modeAqi;
            }

            this.mean.setText(valueToText(meanValue));
            this.median.setText(valueToText(medianValue));
            this.mode.setText(valueToText(modeValue));
        }
    }

}
