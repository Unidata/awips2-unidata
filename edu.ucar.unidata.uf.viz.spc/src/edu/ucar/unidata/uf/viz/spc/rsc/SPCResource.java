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
import com.raytheon.uf.viz.core.DrawableString;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.TextStyle;
import com.raytheon.uf.viz.core.IGraphicsTarget.VerticalAlignment;
import com.raytheon.uf.viz.core.drawables.IShadedShape;
import com.raytheon.uf.viz.core.drawables.IWireframeShape;
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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Resource for SPC data
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Engineer    Description
 * ------------ ----------- --------------------------
 * Mar 28, 2018 mjames      Initial creation
 * Apr 04, 2018 mjames      Type/category labeling and sampling
 * 
 * 
 * @author mjames@ucar
 */

public class SPCResource extends AbstractVizResource<SPCResourceData, MapDescriptor> {

	private static final transient IUFStatusHandler statusHandler = UFStatus
			.getHandler(SPCResource.class);

	GeometryFactory geomFact = new GeometryFactory();

	private Map<String, RGB> CONVECTIVE_MAPPING = 
			getColorMapping(convectiveFillColor,SPCRecord.CONVECTIVE_OUTLOOK);
	
	private Map<String, RGB> TORNADO_MAPPING = 
			getColorMapping(tornadoFillColor,SPCRecord.TORNADO_OUTLOOK);
	
	private Map<String, RGB> WINDHAIL_MAPPING = 
			getColorMapping(windHailFillColor,SPCRecord.HAILWIND_OUTLOOK);

	private static final RGB[] tornadoFillColor = { 
			new RGB(0, 139, 0), 
			new RGB(139, 71, 38), 
			new RGB(255, 200, 0), 
			new RGB(255, 0, 0), 
			new RGB(255, 0, 255), 
			new RGB(145, 44, 238), 
			new RGB(16, 78, 139), 
			new RGB(0, 0, 0)
	};
	
	private static final RGB[] windHailFillColor = { 
			new RGB(139, 71, 38), 
			new RGB(255, 200, 0), 
			new RGB(255, 0, 0), 
			new RGB(255, 0, 255), 
			new RGB(145, 44, 238), 
			new RGB(0, 0, 0)
	};

	private static final RGB[] convectiveFillColor = { 
			new RGB(192, 232, 192), 
			new RGB(127, 197, 127), 
			new RGB(246, 246, 127), 
			new RGB(230, 194, 127), 
			new RGB(230, 127, 127), 
			new RGB(255, 127, 255)
	};

	public static RGB[] getConvectiveFillColor() {
		return convectiveFillColor;
	}

	private Map<DataTime, Collection<SPCRecord>> unprocessedRecords = new HashMap<DataTime, Collection<SPCRecord>>();

	Collection<SPCRecord> records = null;
	
	List<IShadedShape> shapeList = new ArrayList<IShadedShape>();

	private DataTime displayedDataTime;

	protected SPCResource(SPCResourceData resourceData,
			LoadProperties loadProperties, PluginDataObject[] pdos) {
		super(resourceData, loadProperties, false);
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
        StringBuilder res = new StringBuilder();
        for (SPCRecord record : records) {
            try {
                Geometry geom = record.getGeometry();
                Coordinate latLon = coord.asLatLon();
                Point point = geom.getFactory().createPoint(latLon);
                if (geom.contains(point)) {
                    String data = record.getTypeCategory();
                    res.append(data);
                    res.append("\n");
                }
                
            } catch (Exception e) {
                throw new VizException("Error interogating SPC geometry", e);
            }
        }
        return res.toString();
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
	}
	
    /**
     * Paint the text onto the target
     * 
     * @param record
     * @param target
     * @return 
     * @throws VizException
     */
    private DrawableString paintText(SPCRecord record, IGraphicsTarget target)
            throws VizException {
    	
        RGB color = getReportTypeColor(record);
        String type = record.getTypeCategory();
        
        Point center = record.getGeometry().getEnvelope().getCentroid();
        double[] pt = descriptor.worldToPixel(
        		new double[] {center.getCoordinate().x, center.getCoordinate().y});
        
        DrawableString string = new DrawableString(type, color);
        string.setCoordinates(pt[0], pt[1], pt[2]);
        string.addTextStyle(TextStyle.BLANKED);
        string.verticalAlignment = VerticalAlignment.MIDDLE;
        
        return string;
    }

	/**
	 * Process all records for the displayedDataTime
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
			Polygon polygon = (Polygon) record.getGeometry();
			
			RGB color = getReportTypeColor(record);
			double zoom = 12.-(descriptor.getRenderableDisplay().getZoom() * 100.);
			if (zoom < 4) zoom = 4;
			IShadedShape shadedShape = computeShape(target, descriptor, polygon, color);
			IWireframeShape wireFrame = computeWireframe(target, descriptor, polygon, color);
			target.drawWireframeShape(wireFrame, color, (float) zoom, null, 0.7f);
			target.drawShadedShape(shadedShape, 0.3f, 1.0f);
			
		}
		for (SPCRecord record : newRecords) {
			Polygon polygon = (Polygon) record.getGeometry();
			if (polygon.getArea() >= 1.)	target.drawStrings(paintText(record, target));
		}
		
	}

	/**
	 * Create wireframe object from Polygon
	 * 
	 * @param target
	 * @param descriptor
	 * @param p
	 * @param color
	 * @return
	 */
	private IWireframeShape computeWireframe(IGraphicsTarget target, 
			IMapDescriptor descriptor, Polygon p, RGB color) {
		GeneralGridGeometry genGrid = descriptor.getGridGeometry();
		IWireframeShape wireframe = target.createWireframeShape(false, new GeneralGridGeometry(genGrid));
		Coordinate[] coords = sampleCoordinates(p.getBoundary().getCoordinates());
		wireframe.addLineSegment(coords);
		return wireframe;
	}
	
	/**
	 * Downsample polygon coordinate list
	 * 
	 * @param coordinates
	 * @return
	 */
	private Coordinate[] sampleCoordinates(Coordinate[] coordinates) {
		if (coordinates.length > 10) {
			double zoom = descriptor.getRenderableDisplay().getZoom() * 200.;
			int inc = (int) zoom;
			if (inc < 5) inc = 1;
			if (inc > 5) inc = 10;
			int limit = Math.round(coordinates.length/inc);
			Coordinate[] coordList = new Coordinate[limit];
			for (int i = 0; i < coordList.length; i++) {
				coordList[i] = coordinates[i*inc];
			}
			// Ensure last coordinates are equal for a valid closed LinearRing
			coordList[coordList.length-1] = coordList[0];
			return coordList;
		} else {
			return coordinates;
		}
	}

	/**
	 * Create shaded shape and compile to JTS
	 * 
	 * @param target
	 * @param descriptor
	 * @param p
	 * @param color
	 * @return
	 */
	private IShadedShape computeShape(IGraphicsTarget target, 
			IMapDescriptor descriptor, Polygon p, RGB color) {
		GeneralGridGeometry genGrid = descriptor.getGridGeometry();
		IShadedShape newShadedShape = target.createShadedShape(false, new GeneralGridGeometry(genGrid));
		JTSCompiler shapeCompiler = new JTSCompiler(newShadedShape, null, descriptor);
		JTSGeometryData geomData = shapeCompiler.createGeometryData();
		geomData.setWorldWrapCorrect(true);
		try {
			geomData.setGeometryColor(color);
			shapeCompiler.handle(p, geomData);
		} catch (VizException e) {
			statusHandler.handle(Priority.PROBLEM, "Error computing shaded shape", e);
		}
		newShadedShape.compile();
		return newShadedShape;
	}

	/**
	 * Get color for report type
	 * 
	 * @param record
	 * @return
	 */
	private RGB getReportTypeColor(SPCRecord record) {
		RGB color = getCapability(ColorableCapability.class).getColor();
		switch(record.getReportType()) {
			case("Convective Outlook"):
				for (Entry<String, RGB> entry : CONVECTIVE_MAPPING.entrySet()) {
					if (entry.getKey().equals(record.getTypeCategory())) {
						color = entry.getValue();
						break;}}
			case("Tornado Outlook"):
				for (Entry<String, RGB> entry : TORNADO_MAPPING.entrySet()) {
					if (entry.getKey().equals(record.getTypeCategory())) {
						color = entry.getValue();
						break;}}
			case("Wind Outlook"):
			case("Hail Outlook"):
				for (Entry<String, RGB> entry : WINDHAIL_MAPPING.entrySet()) {
					if (entry.getKey().equals(record.getTypeCategory())) {
						color = entry.getValue();
						break;}}
		}
		return color;
	}
	
	/**
	 * Add a new record to this resource
	 * 
	 * @param obj
	 */
	protected void addRecord(SPCRecord obj) {
		DataTime dataTime = obj.getDataTime();
		
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
		}
		records.add(obj);
	}

	@Override
	public String getName() {
		return "SPC " + this.resourceData.getMetadataMap()
		.get("reportType").getConstraintValue().toString().trim();
	}

	@Override
	public void project(CoordinateReferenceSystem crs) throws VizException {
	}

	@Override
	public void remove(DataTime time) {
	}
	
	/**
	 * Color mapping by report type
	 * 
	 * @param fillColor
	 * @param records
	 * @return
	 */
	private static Map<String, RGB> getColorMapping(RGB[] fillColor, Map<String, String> records) {
		Map<String, RGB> mapping = new LinkedHashMap<>();
		int i=0;
		for (String name : records.keySet() ) {
			mapping.put(name, fillColor[i]);
			i++;
		}
		return mapping;
	}

	/**
	 * Color mapping by report type
	 * 
	 * @param fillColor
	 * @param records
	 * @return
	 */
	private static Map<String, RGB> getColorMapping(RGB[] fillColor, String[] records) {
		Map<String, RGB> mapping = new LinkedHashMap<>();
		int i=0;
		for (String name : records ) {
			mapping.put(name, fillColor[i]);
			i++;
		}
		return mapping;
	}

}
