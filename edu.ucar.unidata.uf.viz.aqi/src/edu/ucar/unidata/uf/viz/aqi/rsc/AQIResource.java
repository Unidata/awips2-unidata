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
package edu.ucar.unidata.uf.viz.aqi.rsc;

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

import edu.ucar.unidata.common.dataplugin.aqi.AQIRecord;
import edu.ucar.unidata.uf.viz.aqi.AQIRenderable;
import edu.ucar.unidata.uf.viz.aqi.IAQIDataResource;

/**
 * AQI Resource for displaying {@link AQIRecord}s
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar  7, 2014            mschenke    Initial creation
 * 
 * </pre>
 * 
 * @author mschenke
 * @version 1.0
 */

public class AQIResource extends
        AbstractPluginDataObjectResource<AQIResourceData, IMapDescriptor>
        implements IAQIDataResource, IPaintListener {

	private String resourceName = "Air Quality Index";
	private double meanValue;
    private DecimalFormat format = new DecimalFormat("0.00");
	
    private class AQIGroupRenderable implements IRenderable {

        private List<AQIRenderable> renderables = new ArrayList<AQIRenderable>();

        public AQIGroupRenderable(Collection<PluginDataObject> records) {
            for (PluginDataObject obj : records) {
                if (obj instanceof AQIRecord) {
                    addRecord((AQIRecord) obj);
                }
            }
        }

        public void addRecord(AQIRecord record) {
            AQIRenderable renderable = new AQIRenderable(getDescriptor());
            renderable.setRecord(record);
            renderables.add(renderable);
        }

        @Override
        public void paint(IGraphicsTarget target, PaintProperties paintProps)
                throws VizException {
            RGB color = getCapability(ColorableCapability.class).getColor();
            ColorMapParameters params = getCapability(ColorMapCapability.class)
                    .getColorMapParameters();

            for (AQIRenderable renderable : renderables) {
                renderable.setColor(color);
                renderable.setParameters(params);
                renderable.paint(target, paintProps);
            }
        }

        public Collection<AQIRecord> getRecords() {
            List<AQIRecord> records = new ArrayList<AQIRecord>(
                    renderables.size());
            for (AQIRenderable renderable : renderables) {
                records.add(renderable.getRecord());
            }
            return records;
        }
    }

    /**
     * @param resourceData
     * @param loadProperties
     */
    protected AQIResource(AQIResourceData resourceData,
            LoadProperties loadProperties, PluginDataObject[] pdos) {
        super(resourceData, loadProperties);
        addDataObject(pdos);
    }

    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {
        super.initInternal(target);
        ColorMapParameters params = new ColorMapParameters();

        try {
            params.setColorMap(ColorMapLoader.loadColorMap("AQI"
                    + IPathManager.SEPARATOR + "Default Colormap"));
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
        return new AQIGroupRenderable(records);
    }

    @Override
    protected boolean updateRenderable(IRenderable renderable,
            PluginDataObject... pdos) {
        AQIGroupRenderable groupRenderable = (AQIGroupRenderable) renderable;
        for (PluginDataObject pdo : pdos) {
            if (pdo instanceof AQIRecord) {
                groupRenderable.addRecord((AQIRecord) pdo);
            }
        }
        return true;
    }

    @Override
    public String inspect(ReferencedCoordinate coord) throws VizException {
        InterrogateMap dataMap = interrogate(coord,
                descriptor.getTimeForResource(this),
                IAQIDataResource.CLOSEST_AQI_RECORD_INTERROGATE_KEY);
        AQIRecord record = dataMap
                .get(IAQIDataResource.CLOSEST_AQI_RECORD_INTERROGATE_KEY);
        if (record != null) {
            ColorMapParameters params = getCapability(ColorMapCapability.class)
                    .getColorMapParameters();
            int pixelValue = (int) params.getDisplayToColorMapConverter()
                    .convert(record.getAqi());
            DataMappingPreferences prefs = params.getDataMapping();
            String aqi = String.valueOf(record.getAqi());
            for (DataMappingEntry entry : prefs.getEntries()) {
                if (entry.getPixelValue() == pixelValue) {
                    aqi = entry.getSample();
                    break;
                }
            }

            return record.getName() + ": " + aqi + " @ " + record.getDataTime();
        }
        return super.inspect(coord);
    }

    @Override
    public Set<InterrogationKey<?>> getInterrogationKeys() {
        return new HashSet<InterrogationKey<?>>(Arrays.asList(
                IAQIDataResource.AQI_RECORDS_INTERROGATE_KEY,
                IAQIDataResource.CLOSEST_AQI_RECORD_INTERROGATE_KEY));
    }

    @Override
    public InterrogateMap interrogate(ReferencedCoordinate coordinate,
            DataTime time, InterrogationKey<?>... keys) {
        InterrogateMap map = new InterrogateMap();
        if (time != null) {
            Collection<AQIRecord> records = null;
            try {
                // We have data for this frame, get the renderable
                AQIGroupRenderable renderable = (AQIGroupRenderable) getOrCreateRenderable(time);
                records = renderable.getRecords();
            } catch (VizException e) {
                statusHandler.error("Error interrogating AQI Resource", e);
            }
            if (records != null) {
                for (InterrogationKey<?> key : keys) {
                    if (AQI_RECORDS_INTERROGATE_KEY.equals(key)) {
                        map.put(AQI_RECORDS_INTERROGATE_KEY,
                                records.toArray(new AQIRecord[0]));
                    } else if (CLOSEST_AQI_RECORD_INTERROGATE_KEY.equals(key)) {
                        try {
                            map.put(CLOSEST_AQI_RECORD_INTERROGATE_KEY,
                                    getClosestRecord(coordinate, records));
                        } catch (VizException e) {
                            statusHandler.error(
                                    "Error getting closest AQIRecord", e);
                        }
                    }
                }
            }
        }
        return map;

    }

    private AQIRecord getClosestRecord(ReferencedCoordinate coord,
            Collection<AQIRecord> records) throws VizException {
        try {
            Coordinate cell = coord.asGridCell(descriptor.getGridGeometry(),
                    PixelInCell.CELL_CENTER);
            double distance = Double.MAX_VALUE;
            AQIRecord closest = null;
            for (AQIRecord record : records) {
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
            throw new VizException("Error getting closest AQI record", e);
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
                this.meanValue = Double.NaN;
            } else {
                Collections.sort(visible, new Comparator<AQIRecord>() {
                    @Override
                    public int compare(AQIRecord o1, AQIRecord o2) {
                        return Integer.compare(o1.getAqi(), o2.getAqi());
                    }
                });
                double meanTotal = 0;
                for (AQIRecord record : visible) {
                    meanTotal += record.getAqi();
                }
                this.meanValue = meanTotal / visible.size();
            }
            this.resourceName = "Air Quality Index (" + valueToText(meanValue) + " avg)";
        }

	}
}
