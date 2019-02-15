package edu.ucar.unidata.uf.viz.usgs.rsc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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

import edu.ucar.unidata.common.dataplugin.usgs.USGSRecord;
import edu.ucar.unidata.uf.viz.usgs.USGSRenderable;
import edu.ucar.unidata.uf.viz.usgs.IUSGSDataResource;

/**
 * USGS Resource for displaying {@link USGSRecord}s
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

public class USGSResource extends
        AbstractPluginDataObjectResource<USGSResourceData, IMapDescriptor>
        implements IUSGSDataResource, IPaintListener {

	private String resourceName = "Air Quality Index";
	private double meanValue;
    private DecimalFormat format = new DecimalFormat("0.00");
	
    private class USGSGroupRenderable implements IRenderable {

        private List<USGSRenderable> renderables = new ArrayList<USGSRenderable>();

        public USGSGroupRenderable(Collection<PluginDataObject> records) {
            for (PluginDataObject obj : records) {
                if (obj instanceof USGSRecord) {
                    addRecord((USGSRecord) obj);
                }
            }
        }

        public void addRecord(USGSRecord record) {
            USGSRenderable renderable = new USGSRenderable(getDescriptor());
            renderable.setRecord(record);
            renderables.add(renderable);
        }

        @Override
        public void paint(IGraphicsTarget target, PaintProperties paintProps)
                throws VizException {
            RGB color = getCapability(ColorableCapability.class).getColor();
            ColorMapParameters params = getCapability(ColorMapCapability.class)
                    .getColorMapParameters();

            for (USGSRenderable renderable : renderables) {
                renderable.setColor(color);
                renderable.setParameters(params);
                renderable.paint(target, paintProps);
            }
        }

        public Collection<USGSRecord> getRecords() {
            List<USGSRecord> records = new ArrayList<USGSRecord>(
                    renderables.size());
            for (USGSRenderable renderable : renderables) {
                records.add(renderable.getRecord());
            }
            return records;
        }
    }

    /**
     * @param resourceData
     * @param loadProperties
     */
    protected USGSResource(USGSResourceData resourceData,
            LoadProperties loadProperties, PluginDataObject[] pdos) {
        super(resourceData, loadProperties);
        addDataObject(pdos);
    }

    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {
        super.initInternal(target);
        ColorMapParameters params = new ColorMapParameters();

        try {
            params.setColorMap(ColorMapLoader.loadColorMap("Air Quality Index"));
        } catch (ColorMapException e) {
            throw new VizException(e);
        }

        DataMappingPreferences preferences = new DataMappingPreferences();

        DataMappingEntry entry = new DataMappingEntry();
        entry.setDisplayValue(0.0);
        entry.setPixelValue(0.0);
        entry.setSample("Good");
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(50.0);
        entry.setPixelValue(1.0);
        entry.setSample("Moderate");
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(100.0);
        entry.setPixelValue(2.0);
        entry.setSample("Unhealthy (SG)");
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(150.0);
        entry.setPixelValue(3.0);
        entry.setSample("Unhealthy");
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(200.0);
        entry.setPixelValue(4.0);
        entry.setSample("Very Unhealthy");
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(300.0);
        entry.setPixelValue(5.0);
        entry.setSample("Hazardous");
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
        return new USGSGroupRenderable(records);
    }

    @Override
    protected boolean updateRenderable(IRenderable renderable,
            PluginDataObject... pdos) {
        USGSGroupRenderable groupRenderable = (USGSGroupRenderable) renderable;
        for (PluginDataObject pdo : pdos) {
            if (pdo instanceof USGSRecord) {
                groupRenderable.addRecord((USGSRecord) pdo);
            }
        }
        return true;
    }

    @Override
    public String inspect(ReferencedCoordinate coord) throws VizException {
        InterrogateMap dataMap = interrogate(coord,
                descriptor.getTimeForResource(this),
                IUSGSDataResource.CLOSEST_USGS_RECORD_INTERROGATE_KEY);
        USGSRecord record = dataMap
                .get(IUSGSDataResource.CLOSEST_USGS_RECORD_INTERROGATE_KEY);
        if (record != null) {
            ColorMapParameters params = getCapability(ColorMapCapability.class)
                    .getColorMapParameters();
            int pixelValue = (int) params.getDisplayToColorMapConverter()
                    .convert(record.getCfs());
            DataMappingPreferences prefs = params.getDataMapping();
            String usgs = String.valueOf(record.getCfs());
            for (DataMappingEntry entry : prefs.getEntries()) {
                if (entry.getPixelValue() == pixelValue) {
                    usgs = entry.getSample();
                    break;
                }
            }

            return record.getName() + ": " + usgs + " @ " + record.getDataTime();
        }
        return super.inspect(coord);
    }

    @Override
    public Set<InterrogationKey<?>> getInterrogationKeys() {
        return new HashSet<InterrogationKey<?>>(Arrays.asList(
                IUSGSDataResource.USGS_RECORDS_INTERROGATE_KEY,
                IUSGSDataResource.CLOSEST_USGS_RECORD_INTERROGATE_KEY));
    }

    @Override
    public InterrogateMap interrogate(ReferencedCoordinate coordinate,
            DataTime time, InterrogationKey<?>... keys) {
        InterrogateMap map = new InterrogateMap();
        if (time != null) {
            Collection<USGSRecord> records = null;
            try {
                // We have data for this frame, get the renderable
                USGSGroupRenderable renderable = (USGSGroupRenderable) getOrCreateRenderable(time);
                records = renderable.getRecords();
            } catch (VizException e) {
                statusHandler.error("Error interrogating USGS Resource", e);
            }
            if (records != null) {
                for (InterrogationKey<?> key : keys) {
                    if (USGS_RECORDS_INTERROGATE_KEY.equals(key)) {
                        map.put(USGS_RECORDS_INTERROGATE_KEY,
                                records.toArray(new USGSRecord[0]));
                    } else if (CLOSEST_USGS_RECORD_INTERROGATE_KEY.equals(key)) {
                        try {
                            map.put(CLOSEST_USGS_RECORD_INTERROGATE_KEY,
                                    getClosestRecord(coordinate, records));
                        } catch (VizException e) {
                            statusHandler.error(
                                    "Error getting closest USGSRecord", e);
                        }
                    }
                }
            }
        }
        return map;

    }

    private USGSRecord getClosestRecord(ReferencedCoordinate coord,
            Collection<USGSRecord> records) throws VizException {
        try {
            Coordinate cell = coord.asGridCell(descriptor.getGridGeometry(),
                    PixelInCell.CELL_CENTER);
            double distance = Double.MAX_VALUE;
            USGSRecord closest = null;
            for (USGSRecord record : records) {
                Coordinate c = record.getGeometry().getCoordinate();
                double[] pixel = descriptor.worldToPixel(new double[] { c.x,
                        c.y });
                Coordinate recordLoc = new Coordinate(pixel[0], pixel[1]);
                double recordDist = recordLoc.distance(cell);
                if (recordDist < distance) {
                    distance = recordDist;
                    closest = record;
                }
            }
            return closest;
        } catch (FactoryException | TransformException e) {
            throw new VizException("Error getting closest USGS record", e);
        }
    }

    @Override
    protected void capabilityChanged(IRenderable renderable,
            AbstractCapability capability) {
    }

    @Override
    public String getName() {
        return this.resourceName;
    }

    private String valueToText(double value) {
        if (Double.isNaN(value)) {
            return "?.??";
        }
		return format.format(value);
    }
    
	@Override
	public void painted(AbstractVizResource<?, ?> resource) {
		Interrogatable interrogatable = (Interrogatable) resource;
        InterrogateMap dataMap = interrogatable.interrogate(
                new ReferencedCoordinate(new Coordinate()), resource
                        .getDescriptor().getTimeForResource(resource),
                IUSGSDataResource.USGS_RECORDS_INTERROGATE_KEY);
        USGSRecord[] records = dataMap
                .get(IUSGSDataResource.USGS_RECORDS_INTERROGATE_KEY);
        if (records != null) {
            IDescriptor descriptor = resource.getDescriptor();
            IRenderableDisplay display = descriptor.getRenderableDisplay();
            IExtent filter = null;
            if (display != null) {
                filter = display.getExtent();
            }
            List<USGSRecord> visible = new ArrayList<USGSRecord>();
            for (Object obj : records) {
                USGSRecord record = (USGSRecord) obj;
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
                this.meanValue = Double.NaN;
            } else {
                Collections.sort(visible, new Comparator<USGSRecord>() {
                    @Override
                    public int compare(USGSRecord o1, USGSRecord o2) {
                        return Double.compare(o1.getCfs(), o2.getCfs());
                    }
                });
                double meanTotal = 0;
                for (USGSRecord record : visible) {
                    meanTotal += record.getCfs();
                }
                this.meanValue = meanTotal / visible.size();
            }
            this.resourceName = "Air Quality Index (" + valueToText(meanValue) + " avg)";
        }

	}
}
