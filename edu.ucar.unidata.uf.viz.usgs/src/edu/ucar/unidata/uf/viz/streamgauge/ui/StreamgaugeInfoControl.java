package edu.ucar.unidata.uf.viz.streamgauge.ui;

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

import edu.ucar.unidata.common.dataplugin.usgs.StreamflowRecord;
import edu.ucar.unidata.common.dataplugin.usgs.StreamflowStation;
import edu.ucar.unidata.uf.viz.streamgauge.StreamgaugeDataResource;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 15, 2019            mjames      Initial creation
 * 
 * </pre>
 * 
 * @author mjames
 * @version 1.0
 */

public class StreamgaugeInfoControl implements IPaintListener {
	
    private DecimalFormat format = new DecimalFormat("0.00");

    private Text mean;

    private double meanValue = Double.NaN;

    private Text median;

    private double medianValue = Double.NaN;

    private Text mode;

    private double modeValue = Double.NaN;

    private final AbstractVizResource<?, ?> resource;

    public StreamgaugeInfoControl(AbstractVizResource<?, ?> resource) {
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
                StreamgaugeDataResource.STREAMGAUGE_RECORDS_INTERROGATE_KEY);
        StreamflowRecord[] records = dataMap
                .get(StreamgaugeDataResource.STREAMGAUGE_RECORDS_INTERROGATE_KEY);
        if (records != null) {
            IDescriptor descriptor = resource.getDescriptor();
            IRenderableDisplay display = descriptor.getRenderableDisplay();
            IExtent filter = null;
            if (display != null) {
                filter = display.getExtent();
            }
            List<StreamflowRecord> visible = new ArrayList<StreamflowRecord>();
            for (Object obj : records) {
                StreamflowRecord record = (StreamflowRecord) obj;
                boolean add = true;
                if (filter != null) {
                	StreamflowStation station = new StreamflowStation();
                	
                    Coordinate location =  station.getGeometry().getCoordinate();
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
                Collections.sort(visible, new Comparator<StreamflowRecord>() {
                    @Override
                    public int compare(StreamflowRecord o1, StreamflowRecord o2) {
                        return Double.compare(o1.getCfs(), o2.getCfs());
                    }
                });

                float modeCfs = Float.NaN;
                int modeCount = 0;
                float currentCfs = 0;
                int currentCount = 0;
                double meanTotal = 0;

                int medianStartIndex = visible.size() / 2;
                int medianEndIndex = medianStartIndex;
                if (visible.size() % 2 == 0) {
                    medianStartIndex -= 1;
                }

                int i = 0;
                double medianTotal = 0;

                for (StreamflowRecord record : visible) {
                    Float usgs = record.getCfs();

                    // For mean
                    meanTotal += usgs;

                    // For median
                    if (i >= medianStartIndex && i <= medianEndIndex) {
                        medianTotal += usgs;
                    }

                    // For mode
                    if (currentCfs == 0) {
                        currentCfs = usgs;
                        modeCfs = usgs;
                    }

                    if (usgs != currentCfs) {
                        // Cfs changed, update modeCfs
                        if (modeCount < currentCount) {
                            modeCfs = currentCfs;
                            modeCount = currentCount;
                        }

                        currentCount = 0;
                        currentCfs = usgs;
                    }
                    currentCount += 1;

                    i += 1;
                }
                this.meanValue = meanTotal / visible.size();
                this.medianValue = medianTotal
                        / (medianEndIndex - medianStartIndex + 1);
                this.modeValue = modeCfs;
            }

            this.mean.setText(valueToText(meanValue));
            this.median.setText(valueToText(medianValue));
            this.mode.setText(valueToText(modeValue));
        }
    }

}
