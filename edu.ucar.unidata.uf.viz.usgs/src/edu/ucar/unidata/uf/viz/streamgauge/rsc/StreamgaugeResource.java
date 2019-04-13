package edu.ucar.unidata.uf.viz.streamgauge.rsc;

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

import edu.ucar.unidata.common.dataplugin.usgs.StreamflowRecord;
import edu.ucar.unidata.uf.viz.streamgauge.StreamgaugeDataResource;
import edu.ucar.unidata.uf.viz.streamgauge.StreamgaugeRenderable;

/**
 * Streamgauge Resource for displaying {@link StreamflowRecord}s
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

public class StreamgaugeResource extends
	AbstractPluginDataObjectResource<StreamgaugeResourceData, IMapDescriptor>
		implements StreamgaugeDataResource, IPaintListener {

	private String resourceName = "River Gauge CFS";
	private double maxValue;
	
    private class StreamgaugeGroupRenderable implements IRenderable {

        private List<StreamgaugeRenderable> renderables = new ArrayList<StreamgaugeRenderable>();

        public StreamgaugeGroupRenderable(Collection<PluginDataObject> records) {
            for (PluginDataObject obj : records) {
                if (obj instanceof StreamflowRecord) {
                    addRecord((StreamflowRecord) obj);
                }
            }
        }

        public void addRecord(StreamflowRecord record) {
            StreamgaugeRenderable renderable = new StreamgaugeRenderable(getDescriptor());
            renderable.setRecord(record);
            renderables.add(renderable);
        }

        @Override
        public void paint(IGraphicsTarget target, PaintProperties paintProps)
                throws VizException {
            RGB color = getCapability(ColorableCapability.class).getColor();
            ColorMapParameters params = getCapability(ColorMapCapability.class)
                    .getColorMapParameters();

            for (StreamgaugeRenderable renderable : renderables) {
                renderable.setColor(color);
                renderable.setParameters(params);
                renderable.paint(target, paintProps);
            }
        }

        public Collection<StreamflowRecord> getRecords() {
            List<StreamflowRecord> records = new ArrayList<StreamflowRecord>(
                    renderables.size());
            for (StreamgaugeRenderable renderable : renderables) {
                records.add(renderable.getRecord());
            }
            return records;
        }
    }

    /**
     * @param resourceData
     * @param loadProperties
     */
    protected StreamgaugeResource(StreamgaugeResourceData resourceData,
            LoadProperties loadProperties, PluginDataObject[] pdos) {
        super(resourceData, loadProperties);
        addDataObject(pdos);
    }

    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {
        super.initInternal(target);

        ColorMapParameters params = new ColorMapParameters();

        try {
            params.setColorMap(ColorMapLoader.loadColorMap("Matplotlib/Blues"));
        } catch (ColorMapException e) {
            throw new VizException(e);
        }

        DataMappingPreferences preferences = new DataMappingPreferences();

        DataMappingEntry entry = new DataMappingEntry();
        entry.setDisplayValue(0.0);
        entry.setPixelValue(0.0);
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(500.0);
        entry.setPixelValue(1.0);
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(1000.0);
        entry.setPixelValue(2.0);
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(1500.0);
        entry.setPixelValue(3.0);
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(2000.0);
        entry.setPixelValue(4.0);
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(2500.0);
        entry.setPixelValue(5.0);
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(3000.0);
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
    	return new StreamgaugeGroupRenderable(records);
    }
    
    @Override
    protected boolean updateRenderable(IRenderable renderable,
    		PluginDataObject... pdos) {
    	StreamgaugeGroupRenderable groupRenderable = (StreamgaugeGroupRenderable) renderable;
    	for (PluginDataObject pdo : pdos) {
    		if (pdo instanceof StreamflowRecord) {
    			groupRenderable.addRecord((StreamflowRecord) pdo);
    		}
    	}
    	return true;
    }

    @Override
    public String inspect(ReferencedCoordinate coord) throws VizException {
        InterrogateMap dataMap = interrogate(coord,
                descriptor.getTimeForResource(this),
                StreamgaugeDataResource.CLOSEST_STREAMGAUGE_RECORD_INTERROGATE_KEY);
        StreamflowRecord record = dataMap
                .get(StreamgaugeDataResource.CLOSEST_STREAMGAUGE_RECORD_INTERROGATE_KEY);
        if (record != null) {
            return record.getStationid() + ": " +  record.getName() + "\n" + String.valueOf(record.getCfs()) + " cfs/ "+ 
            	String.valueOf(record.getHeight()) + " ft @ " + record.getDataTime();
        }
        return super.inspect(coord);
    }

    @Override
    public Set<InterrogationKey<?>> getInterrogationKeys() {
        return new HashSet<InterrogationKey<?>>(Arrays.asList(
                StreamgaugeDataResource.STREAMGAUGE_RECORDS_INTERROGATE_KEY,
                StreamgaugeDataResource.CLOSEST_STREAMGAUGE_RECORD_INTERROGATE_KEY));
    }

    @Override
    public InterrogateMap interrogate(ReferencedCoordinate coordinate,
            DataTime time, InterrogationKey<?>... keys) {
        InterrogateMap map = new InterrogateMap();
        if (time != null) {
            Collection<StreamflowRecord> records = null;
            try {
                // We have data for this frame, get the renderable
                StreamgaugeGroupRenderable renderable = (StreamgaugeGroupRenderable) getOrCreateRenderable(time);
                records = renderable.getRecords();
            } catch (VizException e) {
                statusHandler.error("Error interrogating Streamgauge Resource", e);
            }
            if (records != null) {
                for (InterrogationKey<?> key : keys) {
                    if (STREAMGAUGE_RECORDS_INTERROGATE_KEY.equals(key)) {
                        map.put(STREAMGAUGE_RECORDS_INTERROGATE_KEY,
                                records.toArray(new StreamflowRecord[0]));
                    } else if (CLOSEST_STREAMGAUGE_RECORD_INTERROGATE_KEY.equals(key)) {
                        try {
                            map.put(CLOSEST_STREAMGAUGE_RECORD_INTERROGATE_KEY,
                                    getClosestRecord(coordinate, records));
                        } catch (VizException e) {
                            statusHandler.error(
                                    "Error getting closest StreamflowRecord", e);
                        }
                    }
                }
            }
        }
        return map;

    }

    private StreamflowRecord getClosestRecord(ReferencedCoordinate coord,
            Collection<StreamflowRecord> records) throws VizException {
        try {
            Coordinate cell = coord.asGridCell(descriptor.getGridGeometry(),
                    PixelInCell.CELL_CENTER);
            double distance = Double.MAX_VALUE;
            StreamflowRecord closest = null;
            for (StreamflowRecord record : records) {
                Coordinate c = record.getLocation().getGeometry().getCoordinate();
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
            throw new VizException("Error getting closest Streamgauge record", e);
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
                    Coordinate location = record.getLocation().getGeometry().getCoordinate();
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
                double maxCfs = 0;
                for (StreamflowRecord record : visible) {
                	if (record.getCfs() > maxCfs) {
                        maxCfs = record.getCfs();
                    }
                }
                this.maxValue = maxCfs;
            }
            this.resourceName = "River Gauge CFS";
        }

	}

}
