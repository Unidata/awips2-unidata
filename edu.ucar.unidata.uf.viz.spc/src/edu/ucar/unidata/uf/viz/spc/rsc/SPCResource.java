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

import org.apache.commons.lang.ArrayUtils;
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
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

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

public class SPCResource extends AbstractVizResource<SPCResourceData, MapDescriptor> {

	private static final transient IUFStatusHandler statusHandler = UFStatus
			.getHandler(SPCResource.class);

	GeometryFactory geomFact = new GeometryFactory();

	private Map<String, RGB> CONVECTIVE_MAPPING = getColorMapping(convectiveFillColor,SPCRecord.CONVECTIVE_OUTLOOK);
	
	private Map<String, RGB> TORNADO_MAPPING = getColorMapping(tornadoFillColor,SPCRecord.TORNADO_OUTLOOK);
	
	private Map<String, RGB> WINDHAIL_MAPPING = getColorMapping(windHailFillColor,SPCRecord.HAILWIND_OUTLOOK);

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

	List<IShadedShape> shapeList = new ArrayList<IShadedShape>();

	private DataTime displayedDataTime;

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
			RGB color = getReportTypeColor(record);
			IShadedShape shadedShape = prepareShadedShape(record, target, color);
			IWireframeShape wireFrame = prepareWireframeShape(record, target, color);
			target.drawWireframeShape(wireFrame, color, 6.0f, null, 1.0f);
			target.drawShadedShape(shadedShape, 0.5f, 1.0f);
		}
	}

	/**
	 * 
	 * @param reportType
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
	 * prepare geometry and category color for a record.
	 * 
	 * @param record
	 * @param target
	 * @return
	 * @throws VizException
	 */
	private IShadedShape prepareShadedShape(SPCRecord record, IGraphicsTarget target, RGB color)
			throws VizException {
		
		Coordinate[] coords = record.getGeometry().getCoordinates();
		int y=0;
		for (Coordinate co : coords) {
			y++;
			if((y%2)==0) {
				coords = (Coordinate[]) ArrayUtils.removeElement(coords, co);
			}
		}
		Geometry geom = record.getGeometry();
		return computeShape(target, descriptor, geom, color);
	}
	
	private IWireframeShape prepareWireframeShape(SPCRecord record, IGraphicsTarget target, RGB color)
			throws VizException {
		Coordinate[] coords = record.getGeometry().getCoordinates();
		int y=0;
		for (Coordinate co : coords) {
			y++;if((y%2)==0) {
				coords = (Coordinate[]) ArrayUtils.removeElement(coords, co);}}
		Geometry geom = record.getGeometry();
		return computeWireframe(target, descriptor, geom, color);
	}

	private IWireframeShape computeWireframe(IGraphicsTarget target, 
			IMapDescriptor descriptor, Geometry g, RGB color) {
		Polygon polygon = (Polygon) g;
		GeneralGridGeometry genGrid = descriptor.getGridGeometry();
		IWireframeShape wireframe = target.createWireframeShape(false, new GeneralGridGeometry(genGrid));
		wireframe.addLineSegment(polygon.getCoordinates());
		return wireframe;
	}
	
	/**
	 * create shaded shape and compile to JTS
	 * 
	 * @param target
	 * @param descriptor
	 * @param g
	 * @param color
	 * @return
	 */
	private IShadedShape computeShape(IGraphicsTarget target, 
			IMapDescriptor descriptor, Geometry g, RGB color) {
		Polygon polygon = (Polygon) g;
		GeneralGridGeometry genGrid = descriptor.getGridGeometry();
		IShadedShape newShadedShape = target.createShadedShape(false, new GeneralGridGeometry(genGrid));
		JTSCompiler shapeCompiler = new JTSCompiler(newShadedShape, null, descriptor);
		JTSGeometryData geomData = shapeCompiler.createGeometryData();
		geomData.setWorldWrapCorrect(true);
		try {
			geomData.setGeometryColor(color);
			shapeCompiler.handle(polygon, geomData);
		} catch (VizException e) {
			statusHandler.handle(Priority.PROBLEM, "Error computing shaded shape", e);
		}
		newShadedShape.compile();
		return newShadedShape;
	}

	/**
	 * Add a new record to this resource
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
	 * 
	 * @param fillColor
	 * @param records
	 * @return
	 */
	private static Map<String, RGB> getColorMapping(RGB[] fillColor, Map<String, String> records) {
		Map<String, RGB> mapping = new LinkedHashMap<>();
		int i=0;
		for (String name : records.values() ) {
			mapping.put(name, fillColor[i]);
			i++;
		}
		return mapping;
	}

	/**
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
