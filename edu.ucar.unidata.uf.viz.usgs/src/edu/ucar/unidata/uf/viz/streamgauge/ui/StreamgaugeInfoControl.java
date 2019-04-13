package edu.ucar.unidata.uf.viz.streamgauge.ui;

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
import edu.ucar.unidata.uf.viz.streamgauge.StreamgaugeDataResource;

/**
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
	
    private Text cfsText;
    
    private Double maxValue = Double.NaN;

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
        label.setText("Max:");

        cfsText = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
        cfsText.setLayoutData(textData);
        cfsText.setText(maxValue.toString());

        resource.registerListener(this);
    }

    public void dispose() {
        resource.unregisterListener(this);
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
                    Coordinate location =  record.getGeometry().getCoordinate();
                    double[] pixel = descriptor.worldToPixel(new double[] {
                            location.x, location.y });
                    add = filter.contains(pixel);
                }
                if (add == true) {
                    visible.add(record);
                }
            }

            if (visible.isEmpty()) {
                this.maxValue = Double.NaN;
            } else {
                Collections.sort(visible, new Comparator<StreamflowRecord>() {
                    @Override
                    public int compare(StreamflowRecord o1, StreamflowRecord o2) {
                        return Double.compare(o1.getCfs(),o2.getCfs());
                    }
                });
                
                Float maxCfs = new Float(0);
                for (StreamflowRecord record : visible) {
                    if (record.getCfs() > maxCfs) {
                        maxCfs = record.getCfs();
                    }
                }
                this.maxValue = (double) maxCfs;
            }
            this.cfsText.setText(this.maxValue.toString());
        }
    }
}
