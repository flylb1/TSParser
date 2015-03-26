/*******************************************************************************
 * Copyright 2015 Bin Liu (flylb1@gmail.com)
 *   
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *******************************************************************************/
package core;

public class Service {
    private int onId;
    private int tsId;
    private int svcId;
    private String serviceName;

    public Service(int onId, int tsId, int svcId) { // 
        super();
        this.onId = onId;
        this.tsId = tsId;
        this.svcId = svcId;
    }

    public int getOnId() {
        return onId;
    }

    public void setOnId(int onId) {
        this.onId = onId;
    }

    public int getTsId() {
        return tsId;
    }

    public void setTsId(int tsId) {
        this.tsId = tsId;
    }

    public int getSvcId() {
        return svcId;
    }

    public void setSvcId(int svcId) {
        this.svcId = svcId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + onId;
        result = prime * result + svcId;
        result = prime * result + tsId;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Service other = (Service) obj;
        if (onId != other.onId)
            return false;
        if (svcId != other.svcId)
            return false;
        if (tsId != other.tsId)
            return false;
        return true;
    }

    public String toString() {
        return "Service [onId=" + onId + ", tsId=" + tsId + ", svcId=" + svcId + ", serviceName=" + serviceName + "]";
    }

}
