package edu.ucar.unidata.common.dataplugin.spc;

import java.util.LinkedHashMap;
import java.util.Map;

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
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Mar 27, 2018             mjames@ucar  Initial creation
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
	
	private static final String[] tornadoCategory = {
			"2 %",
			"5 %",
			"10 %",
			"15 %",
			"30 %",
			"45 %",
			"60 %",
			"Sig"
	};
	
	private static final String[] windCategory = {
			"5 %",
			"15 %",
			"30 %",
			"45 %",
			"60 %",
			"Sig"
	};
	
	private static final String[] hailCategory = {
			"5 %",
			"15 %",
			"30 %",
			"45 %",
			"60 %",
			"Sig"
	};
	
	private static final String[] tstormCategory = {
			"10 %",
			"40 %",
			"70 %"
	};
	
	private static final String[] convectiveCategory= { 
			"TSTM", 
			"MRGL", 
			"SLGT", 
			"ENH", 
			"MDT", 
			"HIGH"
	};

	private static final String[] convectiveCategoryName = { 
			"General Thunder",
			"Marginal Risk",
			"Slight Risk",
			"Enhanced Risk",
			"Moderate Risk",
			"High Risk"
	};
	
	public static Map<String, String> CONVECTIVE_OUTLOOKS = 
			mapDefinitions(convectiveCategory, convectiveCategoryName);

	public static final String PLUGIN_NAME = "spc";

	// report type
	@Column(length = 32)
	@DynamicSerializeElement
	@DataURI(position = 1)
	String reportType;

	// report name
	@Column(length = 32)
	@DynamicSerializeElement
	@DataURI(position = 2)
	String typeCategory;
	
	// report part
	@Column
    @DynamicSerializeElement
    @DataURI(position = 3)
    Integer reportPart;

	@Column(name = "location", columnDefinition = "geometry")
	@Type(type = "org.hibernate.spatial.GeometryType")
	@XmlJavaTypeAdapter(value = GeometryAdapter.class)
	@DynamicSerializeElement
	private Geometry geometry;

	/**
     * Default Constructor
     */
    public SPCRecord() {
        this.reportType = SPCRecord.PLUGIN_NAME;
        this.typeCategory = null;
        this.geometry = null;
    }

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public void setReportPart(Integer part) {
		this.reportPart = part;
	}
	
	public Integer getReportPart() {
		return reportPart;
	}

	public String getTypeCategory() {
		return typeCategory;
	}

	public void setTypeCategory(String typeCategory) {
		this.typeCategory = typeCategory;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
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
	
	public static Map<String, String> mapDefinitions(String[] s1, String[] s2) {
		Map<String, String> mapping = new LinkedHashMap<>();
		int i=0;
		for (String name : s1 ) {
			mapping.put(name, s2[i]);
			i++;
		}
		return mapping;
	}

}
