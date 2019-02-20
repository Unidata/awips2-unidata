package edu.ucar.unidata.common.dataplugin.usgs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.raytheon.uf.common.dataplugin.persist.PersistableDataObject;
import com.raytheon.uf.common.geospatial.ISpatialObject;
import com.raytheon.uf.common.geospatial.adapter.GeometryAdapter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * Represents a streamflow gauge station. This class maps to the streamflow_spatial table.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Feb 15, 2019             mjames      Created.
 * 
 * </pre>
 * 
 * @author mjames
 * @version 1
 */
@Entity
@Table(name = "streamflow_spatial")
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
public class StreamflowStation extends PersistableDataObject<Object> implements
        ISpatialObject {

    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "station_id")
    @XmlAttribute
    @DynamicSerializeElement
    private String station_id;
    
    @Column(name = "station_name")
    @XmlAttribute
    @DynamicSerializeElement
    private String stationName;
    
    @Column(name = "source")
    @XmlAttribute
    @DynamicSerializeElement
    private String source;

	@Column(name = "lat")
    @XmlAttribute
    @DynamicSerializeElement
    private Float lat;

    @Column(name = "lon")
    @XmlAttribute
    @DynamicSerializeElement
    private Float lon;

    @Column(name = "the_geom")
    @Type(type = "org.hibernate.spatial.GeometryType")
    @XmlJavaTypeAdapter(value = GeometryAdapter.class)
    @DynamicSerializeElement
    private Point station;

    public String getStationId() {
		return station_id;
    }

    public void setStationId(String stationId) {
    	this.station_id = stationId;
    }

    public String getStationName() {
		return stationName;
	}

    public void setStationName(String stationName) {
    	this.stationName = stationName;
    }

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

    public Float getLat() {
        return lat;
    }

    public void setLat(Float lat) {
        this.lat = lat;
    }

    public Float getLon() {
        return lon;
    }

    public void setLon(Float lon) {
        this.lon = lon;
    }

    @Override
    public Geometry getGeometry() {
        return station;
    }

    @Override
    public CoordinateReferenceSystem getCrs() {
        return null;
    }

    @Override
    public Integer getNx() {
        return 0;
    }

    @Override
    public Integer getNy() {
        return 0;
    }
}
