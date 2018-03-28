package edu.ucar.unidata.edex.plugin.spc;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.vividsolutions.jts.geom.GeometryFactory;

import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.TimeSpan;
import edu.ucar.unidata.common.dataplugin.spc.SPCRecord;

/**
 * 
 * Decoder for SPC convective outlook KML files.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 27, 2018            mjames@ucar Initial creation
 * 
 * </pre>
 *
 * @author mjames
 */

public class SPCDecoder {

	GeometryFactory geomFact = new GeometryFactory();

	private IUFStatusHandler logger = UFStatus.getHandler(SPCDecoder.class);    

	public PluginDataObject[] decode(byte[] data) throws Exception {

		ArrayList<SPCRecord> list = new ArrayList<SPCRecord>();

		// Modification needed for unmarshaling
		String input = new String(data).replace("xmlns=\"http://earth.google.com/kml/2.2\"", 
				"xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"" );

		ByteArrayInputStream stream = new ByteArrayInputStream( input.getBytes( "UTF-8" ) );
		
		try {
			Document document = (Document) Kml.unmarshal(stream).getFeature();
			List<Feature> folders = document.getFeature();
	
			for(Feature feature : folders) {
				String folderName = feature.getName();
				
				// SPC categorical outlook
				// TODO: probabalistic outlooks
				if (feature instanceof Folder && folderName.endsWith("_cat")) {
	
					Folder folder = (Folder) feature;
					List<Feature> placemarkList = folder.getFeature();
					int i = 0;
					for (Feature mark : placemarkList ) {
						i++;
						Placemark placemark = (Placemark) mark;
						String category = placemark.getName();
						Geometry geometry = placemark.getGeometry();
						if(geometry instanceof Polygon) {
							Polygon polygon = (Polygon) geometry;
							
							Boundary outerBoundaryIs = polygon.getOuterBoundaryIs();
							if(outerBoundaryIs != null) {
								LinearRing linearRing = outerBoundaryIs.getLinearRing();
								if(linearRing != null) {
									List<de.micromata.opengis.kml.v_2_2_0.Coordinate> coordinates = linearRing.getCoordinates();
									if(coordinates != null) {
										TimeSpan timePrimitive = (TimeSpan) placemark.getTimePrimitive();
										String dateString = timePrimitive.getBegin();
										Date date = new DateTime(dateString).toDate();
										DataTime dataTime = new DataTime(date);
										try {
											SPCRecord record = new SPCRecord();
											record.setReportName(category);
											com.vividsolutions.jts.geom.LinearRing outer = 
													modifyLinearRing(polygon.getOuterBoundaryIs().getLinearRing());
											List<Boundary> bound = polygon.getInnerBoundaryIs();
											int j = 0;
											com.vividsolutions.jts.geom.LinearRing[] inner = 
													new com.vividsolutions.jts.geom.LinearRing[bound.size()];
											for (Boundary b : bound) {
												inner[j] = modifyLinearRing(b.getLinearRing());
												j++;
											}
											
											record.setGeometry(geomFact.createPolygon(outer, inner));
											record.setDataTime(dataTime);
											record.setPart(i);
											list.add(record);
										} catch (Exception ex) {
											ex.printStackTrace();
										}
									}
								}
							}
						}
					}
					logger.info(placemarkList.size() + " polygons processed for KML feature " + folderName.toString() );
				}
			} 
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		PluginDataObject[] decodedData = list.toArray(new PluginDataObject[list.size()]);
		return (decodedData);
	}

	public com.vividsolutions.jts.geom.LinearRing modifyLinearRing(LinearRing l) {
		return geomFact.createLinearRing(this.convertCoords(l.getCoordinates()));
	}
	
	public com.vividsolutions.jts.geom.Coordinate[] convertCoords(List<Coordinate> geomList) {
		com.vividsolutions.jts.geom.Coordinate[] coordList = new com.vividsolutions.jts.geom.Coordinate[geomList.size()];
		int j = 0;
		for (Coordinate i : geomList) {
			com.vividsolutions.jts.geom.Coordinate b = new com.vividsolutions.jts.geom.Coordinate();
			b.x = i.getLongitude();
			b.y = i.getLatitude();
			coordList[j] = b;
			j = j + 1;
		}
		return coordList;
	}
}
