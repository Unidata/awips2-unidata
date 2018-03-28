package edu.ucar.unidata.common.dataplugin.spc;

import javax.persistence.Access;
import javax.persistence.AccessType;
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
 * SPC Convective Outlook Record
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Mar 27, 2018             mjames@ucar  Initial creation
 * </pre>
 * 
 * @author mjames
 */

@Entity
@SequenceGenerator(initialValue = 1, name = PluginDataObject.ID_GEN, sequenceName = "spc_seq")
@Table(name = SPCRecord.PLUGIN_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = { "dataURI" }) })
/*
 * Both refTime and forecastTime are included in the refTimeIndex since
 * forecastTime is unlikely to be used.
 */
@org.hibernate.annotations.Table(appliesTo = SPCRecord.PLUGIN_NAME, indexes = { 
		@Index(name = "spc_refTimeIndex", columnNames = {
        "refTime", "forecastTime" }) })
@DynamicSerialize
public class SPCRecord extends PluginDataObject {

	private static final long serialVersionUID = 1L;

	public static final String PLUGIN_NAME = "spc";

	// report name
	@Column(length = 32)
	@DynamicSerializeElement
	@DataURI(position = 1)
	String reportName;

	// threat level
	@Column(length = 32)
    @DynamicSerializeElement
    @DataURI(position = 2)
    private String level;

	@Column(name = "location", columnDefinition = "geometry")
	@Type(type = "org.hibernate.spatial.GeometryType")
	@XmlJavaTypeAdapter(value = GeometryAdapter.class)
	@DynamicSerializeElement
	private Geometry geometry;

	/**
     * Default Constructor
     */
    public SPCRecord() {
        this.level = "";
        this.reportName = SPCRecord.PLUGIN_NAME;
        this.geometry = null;
    }
    
    /**
     * Convstructs an SPC record from a dataURI
     * 
     * @param uri
     *            The dataURI
     */
	public SPCRecord(String uri) {
		super(uri);
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
	
	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

    @Override
    @Column
    @Access(AccessType.PROPERTY)
    public String getDataURI() {
        return super.getDataURI();
    }
	
	@Override
	public String getPluginName() {
		return PLUGIN_NAME;
	}

}
