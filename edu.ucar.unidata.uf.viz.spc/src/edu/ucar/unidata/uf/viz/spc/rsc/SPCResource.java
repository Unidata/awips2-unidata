/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package edu.ucar.unidata.uf.viz.spc.rsc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.measure.unit.Unit;

import org.eclipse.swt.graphics.RGB;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.TransformException;

import com.raytheon.uf.common.colormap.ColorMapException;
import com.raytheon.uf.common.colormap.ColorMapLoader;
import com.raytheon.uf.common.colormap.prefs.ColorMapParameters;
import com.raytheon.uf.common.colormap.prefs.DataMappingPreferences;
import com.raytheon.uf.common.colormap.prefs.DataMappingPreferences.DataMappingEntry;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.geospatial.ReferencedCoordinate;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.time.BinOffset;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.IExtent;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.IRenderable;
import com.raytheon.uf.viz.core.drawables.IRenderableDisplay;
import com.raytheon.uf.viz.core.drawables.IShadedShape;
import com.raytheon.uf.viz.core.drawables.IWireframeShape;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.IMapDescriptor;
import com.raytheon.uf.viz.core.rsc.AbstractPluginDataObjectResource;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IPaintListener;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.capabilities.AbstractCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorMapCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorableCapability;
import com.raytheon.uf.viz.core.rsc.interrogation.Interrogatable;
import com.raytheon.uf.viz.core.rsc.interrogation.InterrogateMap;
import com.raytheon.uf.viz.core.rsc.interrogation.InterrogationKey;
import com.vividsolutions.jts.geom.Coordinate;

import edu.ucar.unidata.common.dataplugin.spc.SPCRecord;
import edu.ucar.unidata.uf.viz.spc.SPCRenderable;
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

public class SPCResource extends
        AbstractPluginDataObjectResource<SPCResourceData, IMapDescriptor>
        implements ISPCDataResource, IPaintListener {

	private String resourceName = "SPC Convective Outlook";
	
	protected class ConvectiveOutlookEntry {

        protected SPCRecord record;

        protected IWireframeShape wireframeShape;

        protected IShadedShape shadedShape;

    }
	
	protected List<SPCRecord> recordsToLoad;
	
	protected Map<String, ConvectiveOutlookEntry> entryMap;
	
    private class SPCGroupRenderable implements IRenderable {

        private List<SPCRenderable> renderables = new ArrayList<SPCRenderable>();

        public SPCGroupRenderable(Collection<PluginDataObject> records) {
            for (PluginDataObject obj : records) {
                if (obj instanceof SPCRecord) {
                    addRecord((SPCRecord) obj);
                }
            }
        }

        public void addRecord(SPCRecord record) {
//        		ConvectiveOutlookEntry entry = entryMap.get(record.getDataURI());
//        		if (entry == null) {
//                entry = new ConvectiveOutlookEntry();
//                entry.record = record;
//                entryMap.put(record.getDataURI(), entry);
//            }
//            IWireframeShape wfs = entry.wireframeShape;
//            if (wfs != null) wfs.dispose();
//            
            SPCRenderable renderable = new SPCRenderable(getDescriptor());
            renderable.setRecord(record);
            renderables.add(renderable);
        }

        @Override
        public void paint(IGraphicsTarget target, PaintProperties paintProps)
                throws VizException {
            RGB color = getCapability(ColorableCapability.class).getColor();
            
            for (SPCRenderable renderable : renderables) {
                renderable.setColor(color);
                renderable.paint(target, paintProps);
            }
        }

        public Collection<SPCRecord> getRecords() {
            List<SPCRecord> records = new ArrayList<SPCRecord>(renderables.size());
            for (SPCRenderable renderable : renderables) {
                records.add(renderable.getRecord());
            }
            return records;
        }
    }

    /**
     * @param resourceData
     * @param loadProperties
     */
    protected SPCResource(SPCResourceData resourceData,
            LoadProperties loadProperties, PluginDataObject[] pdos) {
        super(resourceData, loadProperties);
        addDataObject(pdos);
    }

    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {
        super.initInternal(target);
        
        ColorMapParameters params = new ColorMapParameters();

        try {
            params.setColorMap(ColorMapLoader.loadColorMap("Grid/Gridded Data"));
        } catch (ColorMapException e) {
            throw new VizException(e);
        }

        DataMappingPreferences preferences = new DataMappingPreferences();

        DataMappingEntry entry = new DataMappingEntry();
        entry.setDisplayValue(0.0);
        entry.setPixelValue(0.0);
        entry.setSample("TSTM");
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(50.0);
        entry.setPixelValue(1.0);
        entry.setSample("MRGL");
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(100.0);
        entry.setPixelValue(2.0);
        entry.setSample("SLGT");
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(150.0);
        entry.setPixelValue(3.0);
        entry.setSample("ENH");
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(200.0);
        entry.setPixelValue(4.0);
        entry.setSample("MDT");
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(300.0);
        entry.setPixelValue(5.0);
        entry.setSample("HIGH");
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(400.0);
        entry.setPixelValue(6.0);
        preferences.addEntry(entry);

        params.setDisplayUnit(Unit.ONE);
        params.setDataMapping(preferences);
        params.setColorMapMin(0);
        params.setColorMapMax(6);

        getCapability(ColorMapCapability.class).setColorMapParameters(params);
        
        registerListener(this);

    }

    @Override
	protected void disposeResource() {
		unregisterListener(this);
		super.disposeResource();
	}

	@Override
    protected DataTime getDataObjectTime(PluginDataObject pdo) {
        BinOffset offset = resourceData.getBinOffset();
        if (offset != null) {
            return offset.getNormalizedTime(super.getDataObjectTime(pdo));
        }
        return super.getDataObjectTime(pdo);
    }

    @Override
    protected void disposeRenderable(IRenderable renderable) {

    }

    @Override
    protected boolean projectRenderable(IRenderable renderable,
            CoordinateReferenceSystem crs) throws VizException {
        return false;
    }

    @Override
    protected IRenderable constructRenderable(DataTime time,
            List<PluginDataObject> records) throws VizException {
        return new SPCGroupRenderable(records);
    }

    @Override
    protected boolean updateRenderable(IRenderable renderable,
            PluginDataObject... pdos) {
        SPCGroupRenderable groupRenderable = (SPCGroupRenderable) renderable;
        for (PluginDataObject pdo : pdos) {
            if (pdo instanceof SPCRecord) {
                groupRenderable.addRecord((SPCRecord) pdo);
            }
        }
        return true;
    }

    @Override
    public String inspect(ReferencedCoordinate coord) throws VizException {
        InterrogateMap dataMap = interrogate(coord,
                descriptor.getTimeForResource(this),
                ISPCDataResource.CLOSEST_SPC_RECORD_INTERROGATE_KEY);
        SPCRecord record = dataMap
                .get(ISPCDataResource.CLOSEST_SPC_RECORD_INTERROGATE_KEY);
        if (record != null) {
            ColorMapParameters params = getCapability(ColorMapCapability.class)
                    .getColorMapParameters();
            DataMappingPreferences prefs = params.getDataMapping();
            return record.getReportName() + " @ " + record.getDataTime();
        }
        return super.inspect(coord);
    }

    @Override
    public Set<InterrogationKey<?>> getInterrogationKeys() {
        return new HashSet<InterrogationKey<?>>(Arrays.asList(
                ISPCDataResource.SPC_RECORDS_INTERROGATE_KEY,
                ISPCDataResource.CLOSEST_SPC_RECORD_INTERROGATE_KEY));
    }

    @Override
    public InterrogateMap interrogate(ReferencedCoordinate coordinate,
            DataTime time, InterrogationKey<?>... keys) {
        InterrogateMap map = new InterrogateMap();
        if (time != null) {
            Collection<SPCRecord> records = null;
            try {
                // We have data for this frame, get the renderable
                SPCGroupRenderable renderable = (SPCGroupRenderable) getOrCreateRenderable(time);
                records = renderable.getRecords();
            } catch (VizException e) {
                statusHandler.error("Error interrogating SPC Resource", e);
            }
        }
        return map;

    }

    @Override
    protected void capabilityChanged(IRenderable renderable,
            AbstractCapability capability) {
    }

    @Override
    public String getName() {
        return this.resourceName;
    }
    
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

            //this.resourceName = "Air Quality Index (" + valueToText(meanValue) + " avg)";
        }

	}
}
