package edu.ucar.unidata.uf.viz.spc.ui;

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

import edu.ucar.unidata.common.dataplugin.spc.SPCRecord;
import edu.ucar.unidata.uf.viz.spc.ISPCDataResource;

/**
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 28, 2018            mjames@ucar Initial creation
 * 
 * @author mjames
 */

public class SPCInfoControl implements IPaintListener {
	
    private DecimalFormat format = new DecimalFormat("0.00");

    private final AbstractVizResource<?, ?> resource;

    public SPCInfoControl(AbstractVizResource<?, ?> resource) {
        this.resource = resource;
    }

    public void initializeControl(Composite parent) {
        parent.setLayout(new GridLayout(2, false));
        GridData labelData = new GridData(SWT.RIGHT, SWT.CENTER, false, true);
        labelData.widthHint = 60;
        GridData textData = new GridData(SWT.FILL, SWT.FILL, true, true);
        textData.widthHint = 100;
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
                ISPCDataResource.SPC_RECORDS_INTERROGATE_KEY);
        SPCRecord[] records = dataMap
                .get(ISPCDataResource.SPC_RECORDS_INTERROGATE_KEY);
        if (records != null) {
            IDescriptor descriptor = resource.getDescriptor();
            IRenderableDisplay display = descriptor.getRenderableDisplay();
            IExtent filter = null;
            if (display != null) {
                filter = display.getExtent();
            }
            List<SPCRecord> visible = new ArrayList<SPCRecord>();
            for (Object obj : records) {
                SPCRecord record = (SPCRecord) obj;
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
        }
    }

}
