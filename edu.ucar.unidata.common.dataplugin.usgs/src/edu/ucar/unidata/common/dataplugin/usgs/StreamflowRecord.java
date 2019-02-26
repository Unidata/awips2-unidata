package edu.ucar.unidata.common.dataplugin.usgs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.annotations.DataURI;
import com.raytheon.uf.common.geospatial.adapter.GeometryAdapter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * USGS Streamflow Record
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Feb 19, 2019             mjames@ucar  Initial creation
 * 
 * @author mjamess
 */

@Entity
@SequenceGenerator(initialValue = 1, name = PluginDataObject.ID_GEN, sequenceName = "streamflowseq")
@Table(name = StreamflowRecord.PLUGIN_NAME, uniqueConstraints = { @UniqueConstraint(
		columnNames = { "stationid", "refTime" } ) })
@org.hibernate.annotations.Table(
		appliesTo = StreamflowRecord.PLUGIN_NAME, 
		indexes = { @Index(
				name = "sf_refTimeIndex", 
				columnNames = {"refTime", "forecastTime" }) }
		)
@DynamicSerialize
public class StreamflowRecord extends PluginDataObject {

    private static final long serialVersionUID = 1L;

    public static final String PLUGIN_NAME = "streamflow";
    
	@Column(name = "stationid")
    @DataURI(position = 1)
    @DynamicSerializeElement
    private String stationID;
    
	@Column(name = "stationname")
    @DynamicSerializeElement
	private String stationName;
	
	@Column(name = "elevation")
    @DynamicSerializeElement
	private Float elevation;
	
    @Column(name = "status")
    @DynamicSerializeElement
    private String status;
    
    @Column(name = "cfs")
    @DynamicSerializeElement
    private Float cfs;

    @Column(name = "height")
    @DynamicSerializeElement
    private Float height;
    
    @Column(name = "location", columnDefinition = "geometry")
    @Type(type = "org.hibernate.spatial.GeometryType")
    @XmlJavaTypeAdapter(value = GeometryAdapter.class)
    @DynamicSerializeElement
    private Geometry geometry;

    public StreamflowRecord() {
    }

    public String getStationID() {
		return stationID;
	}
    
	public void setStationID(String id) {
		this.stationID = id;
	}
	
	public String getStationName() {
		return stationName;
	}

	public void setStationName(String name) {
		this.stationName = name;
	}

	public Float getElevation() {
		return elevation;
	}

	public void setElevation(Float elev) {
		this.elevation = elev;
	}

	public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Float getCfs() {
        return cfs;
    }

    public void setCfs(Float cfs) {
        this.cfs = cfs;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }
    
	public Geometry getGeometry() {
		return geometry;
	}

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
    
	@Override
	public String getPluginName() {
		return PLUGIN_NAME;
	}

}
