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
package ac.simons.biking2.misc;

import ac.simons.biking2.api.BuildProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.Duration;

/**
 * @author Michael J. Simons, 2014-03-06
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class About {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonPropertyOrder(alphabetic = true)
    public static class VMProperties {

	private final String version = System.getProperty("java.runtime.version");

	@JsonSerialize(using = ToStringSerializer.class)
	private Duration uptime;

	private Long usedMemory;

	private Long totalMemory;

	private Long maxMemory;

	public String getVersion() {
	    return version;
	}

	public Duration getUptime() {
	    return uptime;
	}

	public void setUptime(Duration uptime) {
	    this.uptime = uptime;
	}

	public Long getUsedMemory() {
	    return usedMemory;
	}

	public void setUsedMemory(Long usedMemory) {
	    this.usedMemory = usedMemory;
	}

	public Long getAvailableMemory() {
	    return totalMemory;
	}

	public void setAvailableMemory(Long availableMemory) {
	    this.totalMemory = availableMemory;
	}

	public Long getTotalMemory() {
	    return totalMemory;
	}

	public void setTotalMemory(Long totalMemory) {
	    this.totalMemory = totalMemory;
	}

	public Long getMaxMemory() {
	    return maxMemory;
	}

	public void setMaxMemory(Long maxMemory) {
	    this.maxMemory = maxMemory;
	}
    }

    private VMProperties vm;

    private BuildProperties build;

    public VMProperties getVm() {
	return vm;
    }

    public void setVm(VMProperties vm) {
	this.vm = vm;
    }

    public BuildProperties getBuild() {
	return build;
    }

    public void setBuild(BuildProperties build) {
	this.build = build;
    }
}
