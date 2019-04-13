package edu.ucar.unidata.common.dataplugin.usgs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Type;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.raytheon.uf.common.geospatial.ISpatialObject;
import com.raytheon.uf.common.geospatial.adapter.GeometryAdapter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
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
public class StreamflowStation  implements ISpatialObject {

    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "stationid")
    @XmlAttribute
    @DynamicSerializeElement
    private String stationid;
    
    @Column(name = "stationname")
    @XmlAttribute
    @DynamicSerializeElement
    private String stationname;
    
    @Column(name = "source")
    @XmlAttribute
    @DynamicSerializeElement
    private String source;

	@Column(name = "lat")
    @XmlAttribute
    @DynamicSerializeElement
    private Float latitude;

    @Column(name = "lon")
    @XmlAttribute
    @DynamicSerializeElement
    private Float longitude;
    
    @Column(name = "elev")
    @XmlAttribute
    @DynamicSerializeElement
    private Float elevation;
    
    @Column(name = "the_geom")
    @Type(type = "org.hibernate.spatial.GeometryType")
    @XmlJavaTypeAdapter(value = GeometryAdapter.class)
    @DynamicSerializeElement
    private Point location;
    
    public StreamflowStation() {
    }
    
    public String getStationId() {
		return stationid;
    }

    public void setStationId(String stationid) {
    	this.stationid = stationid;
    }

    public String getStationName() {
		return stationname;
	}

    public void setStationName(String stationName) {
    	this.stationname = stationName;
    }

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float lat) {
        this.latitude = lat;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float lon) {
        this.longitude = lon;
    }


	public Float getElevation() {
		return elevation;
	}

	public void setElevation(Float elevation) {
		this.elevation = elevation;
	}
	
	@Override
    public Point getGeometry() {
        return location;
    }

    @Override
    public CoordinateReferenceSystem getCrs() {
        return null;
    }

    @Override
    public Integer getNx() {
        return null;
    }

    @Override
    public Integer getNy() {
        return null;
    }
    
}
