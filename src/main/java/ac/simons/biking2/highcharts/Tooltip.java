/*
 * Copyright 2014 Michael J. Simons.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ac.simons.biking2.highcharts;

import ac.simons.biking2.support.Sink;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Michael J. Simons, 2014-02-11
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tooltip {

    public static class Builder<PB> {

	private final Sink<PB, Tooltip> sink;

	private String footerFormat;

	private String headerFormat;

	private String pointFormat;

	private Boolean shared;

	private Boolean useHTML;
	
	private Boolean crosshairs;
	
	private String valueSuffix;

	public Builder(Sink<PB, Tooltip> sink) {
	    this.sink = sink;
	}
	
	public Builder<PB> withFooterFormat(final String footerFormat) {
	    this.footerFormat = footerFormat;
	    return this;
	}
	
	public Builder<PB> withHeaderFormat(final String headerFormat) {
	    this.headerFormat = headerFormat;
	    return this;
	}
	
	public Builder<PB> withPointFormat(final String pointFormat) {
	    this.pointFormat = pointFormat;
	    return this;
	}
	
	public Builder<PB> withValueSuffix(final String valueSuffix) {
	    this.valueSuffix = valueSuffix;
	    return this;
	}
	
	public Builder<PB> share() {
	    this.shared = true;
	    return this;
	}
			
	public Builder<PB> useHTML() {
	    this.useHTML = true;
	    return this;
	}
	
	public Builder<PB> enableCrosshairs() {
	    this.crosshairs = true;
	    return this;
	}
		
	public PB build() {
	    return this.sink.setObject(
		    new Tooltip(footerFormat, headerFormat, pointFormat, shared, useHTML, crosshairs, valueSuffix)
	    );
	}
    }

    /**
     * A string to append to the tooltip format. Defaults to false.
     */
    private final String footerFormat;

    /**
     * The HTML of the tooltip header line. Variables are enclosed by curly
     * brackets. Available variables	are point.key, series.name, series.color
     * and other members from the point and series objects. The point.key
     * variable contains the category name, x value or datetime string depending
     * on the type of axis. For datetime axes, the point.key date format can be
     * set using tooltip.xDateFormat.
     */
    private final String headerFormat;

    /**
     * The HTML of the point's line in the tooltip. Variables are enclosed by
     * curly brackets. Available variables are point.x, point.y, series.name and
     * series.color and other properties on the same form. Furthermore, point.y
     * can be extended by the tooltip.yPrefix and tooltip.ySuffix variables.
     * This can also be overridden for each series, which makes it a good hook
     * for displaying units.
     */
    private final String pointFormat;

    /**
     * When the tooltip is shared, the entire plot area will capture mouse
     * movement. Tooltip texts for series types with ordered data (not pie,
     * scatter, flags etc) will be shown in a single bubble. This is recommended
     * for single series charts and for tablet/mobile optimized charts. Defaults
     * to false.
     */
    private final Boolean shared;

    /**
     * Use HTML to render the contents of the tooltip instead of SVG. Using HTML
     * allows advanced formatting like tables and images in the tooltip. It is
     * also recommended for rtl languages as it works around rtl bugs in early
     * Firefox. Defaults to false.
     */
    private final Boolean useHTML;
    
    private final Boolean crosshairs;
    
    private final String valueSuffix;

    @JsonCreator
    Tooltip(
	    @JsonProperty("footerFormat") String footerFormat, 
	    @JsonProperty("headerFormat") String headerFormat, 
	    @JsonProperty("pointFormat") String pointFormat, 
	    @JsonProperty("shared") Boolean shared, 
	    @JsonProperty("useHTML") Boolean useHTML,
	    @JsonProperty("crosshairs") Boolean crosshairs,
	    @JsonProperty("valueSuffix") String valueSuffix 
    ) {
	this.footerFormat = footerFormat;
	this.headerFormat = headerFormat;
	this.pointFormat = pointFormat;
	this.shared = shared;
	this.useHTML = useHTML;
	this.crosshairs = crosshairs;		
	this.valueSuffix = valueSuffix;
    }

    public String getFooterFormat() {
	return footerFormat;
    }

    public String getHeaderFormat() {
	return headerFormat;
    }

    public String getPointFormat() {
	return pointFormat;
    }

    public Boolean isShared() {
	return shared;
    }

    public Boolean isUseHTML() {
	return useHTML;
    }

    public Boolean isCrosshairs() {
	return crosshairs;
    }

    public String getValueSuffix() {
	return valueSuffix;
    }
}
