package edu.ucar.unidata.uf.viz.spc.rsc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.graphics.RGB;
import org.geotools.coverage.grid.GeneralGridGeometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.raytheon.uf.common.dataplugin.PluginDataObject;

import edu.ucar.unidata.common.dataplugin.spc.SPCRecord;
import com.raytheon.uf.common.geospatial.ReferencedCoordinate;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.IShadedShape;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.IMapDescriptor;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorableCapability;
import com.raytheon.viz.core.rsc.jts.JTSCompiler;
import com.raytheon.viz.core.rsc.jts.JTSCompiler.JTSGeometryData;
import com.raytheon.viz.core.rsc.jts.JTSCompiler.PointStyle;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Resource for SPC data
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 28, 2018            mjames@ucar Initial creation
 * 
 * 
 * @author mjames@ucar
 */

public class SPCResource extends
        AbstractVizResource<SPCResourceData, MapDescriptor> {
	
    private static final transient IUFStatusHandler statusHandler = UFStatus
    .getHandler(SPCResource.class);
    
    private static final String[] category= { 
    		"TSTM", 
    		"MRGL", 
    		"SLGT", 
    		"ENH", 
    		"MDT", 
    		"HIGH"
    	};

    private static final String[] categoryName = { 
    		"General Thunder",
    		"Marginal Risk",
    		"Slight Risk",
    		"Enhanced Risk",
    		"Moderate Risk",
    		"High Risk"
    };

    private static final RGB[] categoryFillColor = { 
    		new RGB(192, 232, 192), 
    		new RGB(127, 197, 127), 
    		new RGB(246, 246, 127), 
    		new RGB(230, 194, 127), 
    		new RGB(230, 127, 127), 
    		new RGB(255, 127, 255), 
    	};
    
    private static final RGB[] categoryLineColor = { 
    		new RGB(255, 255, 255), 
    		new RGB(60, 120, 60), 
    		new RGB(255, 150, 0), 
    		new RGB(215, 150, 60), 
    		new RGB(150, 25, 0), 
    		new RGB(200, 60, 150), 
    	};
    
    public static String[] getCategory() {
		return category;
	}

	public static String[] getCategoryname() {
		return categoryName;
	}

	public static RGB[] getCategoryfillcolor() {
		return categoryFillColor;
	}

	public static RGB[] getCategorylinecolor() {
		return categoryLineColor;
	}
    
    private static Map<String, RGB> getColorMapping() {
        Map<String, RGB> mapping = new LinkedHashMap<>();
        int i=0;
    		for (String name : categoryName ) {
    			mapping.put(name, categoryFillColor[i]);
    			i++;
    		}
        return mapping;
    }

    private Map<String, RGB> MAPPING = getColorMapping();

    private Map<DataTime, Collection<SPCRecord>> unprocessedRecords = new HashMap<DataTime, Collection<SPCRecord>>();

    List<IShadedShape> shapeList = new ArrayList<IShadedShape>();
    
    private DataTime displayedDataTime;
    
    private IShadedShape createShapeFromRecord(SPCRecord record, IGraphicsTarget target)
            throws VizException {
        Geometry geom = record.getGeometry();
        RGB color = getCapability(ColorableCapability.class).getColor();
        for (Entry<String, RGB> entry : MAPPING.entrySet()) {
        		if (entry.getKey().equals(record.getReportName())) {
        			color = entry.getValue();
        			break;
        		}
        }
        return computeShape(target, descriptor, geom, color);
    }

    protected SPCResource(SPCResourceData resourceData,
            LoadProperties loadProperties, PluginDataObject[] pdos) {
        super(resourceData, loadProperties);
        resourceData.addChangeListener(new IResourceDataChanged() {
            @Override
            public void resourceChanged(ChangeType type, Object object) {
                if (type == ChangeType.DATA_UPDATE) {
                    for (PluginDataObject p : pdos) {
                        if (p instanceof SPCRecord) {
                            addRecord((SPCRecord) p);
                        }
                    }
                }
                issueRefresh();
            } 
        });
        this.dataTimes = new ArrayList<DataTime>();
    }

	@Override
    protected void disposeInternal() {
    }

    @Override
    public DataTime[] getDataTimes() {
        if (this.dataTimes == null) {
            return new DataTime[0];
        }
        return this.dataTimes.toArray(new DataTime[this.dataTimes.size()]);
    }

    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {
    }

    @Override
    public String inspect(ReferencedCoordinate coord) throws VizException {
		return null;
    }

    /**
     * process all records for the displayedDataTime
     * 
     * @param target
     * @param paintProps
     * @throws VizException
     */
    @SuppressWarnings("null")
	private void updateRecords(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {

        Collection<SPCRecord> newRecords = null;
        synchronized (unprocessedRecords) {
            newRecords = unprocessedRecords.get(this.displayedDataTime);
        }
        for (SPCRecord record : newRecords) {
        		target.drawShadedShape(createShapeFromRecord(record, target), 0.5f, 1.0f);
        }
    }

    @Override
    protected void paintInternal(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {
        this.displayedDataTime = paintProps.getDataTime();

        Collection<SPCRecord> unprocessed = null;
        synchronized (unprocessedRecords) {
            unprocessed = unprocessedRecords.get(this.displayedDataTime);
        }
        if (unprocessed != null && unprocessed.size() > 0) {
            updateRecords(target, paintProps);
        }

        // Draw the text
        //for (SPCRecord record : frame.records) {
        //	if (isPaintingText(record)) {
        //		paintText(record, target);
        //	}
        //}
    }

    private IShadedShape computeShape(IGraphicsTarget target,
            IMapDescriptor descriptor, Geometry g, RGB color) {
        IShadedShape newShadedShape = target.createShadedShape(false,
                new GeneralGridGeometry(descriptor.getGridGeometry()));
        JTSCompiler shapeCompiler = new JTSCompiler(newShadedShape, null,
                descriptor);
        JTSGeometryData geomData = shapeCompiler.createGeometryData();
        geomData.setWorldWrapCorrect(true);
        try {
            geomData.setGeometryColor(color);
            shapeCompiler.handle(g, geomData);
        } catch (VizException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Error computing shaded shape", e);
        }
        newShadedShape.compile();
        return newShadedShape;
    }

    /**
     * Adds a new record to this resource
     * 
     * @param obj
     */
    protected void addRecord(SPCRecord obj) {
        DataTime dataTime = obj.getDataTime();
        Collection<SPCRecord> records = null;
        boolean brandNew = false;
        synchronized (unprocessedRecords) {
            records = unprocessedRecords.get(dataTime);
            if (records == null) {
                records = new LinkedHashSet<SPCRecord>();
                unprocessedRecords.put(dataTime, records);
                brandNew = true;
            }
        }
        if (brandNew) {
            this.dataTimes.add(dataTime);
            Collections.sort(this.dataTimes);
        }
        records.add(obj);
    }

    @Override
    public String getName() {
        return "SPC Convective Outlook";
    }

    @Override
    public void project(CoordinateReferenceSystem crs) throws VizException {
    }

    @Override
    public void remove(DataTime time) {
    }

}
