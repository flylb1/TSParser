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

import java.util.Calendar;

public class Event implements Comparable<Event> {
    private TSSection section;
    private int eventId;
    private Calendar startTime;
    private Calendar endTime;

    private int duration;
    private Object value;
    private boolean pf;

    public Event() {
        super();
    }

    public Event(int eventId, Calendar startTime, int duration) { // 
        super();
        this.eventId = eventId;
        this.startTime = startTime;
        this.duration = duration;
    }

    public TSSection getSection() {
        return section;
    }

    public void setSection(TSSection section) {
        this.section = section;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isPf() {
        return pf;
    }

    public void setPf(boolean pf) {
        this.pf = pf;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + eventId;
        result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Event other = (Event) obj;
        if (eventId != other.eventId)
            return false;
        if (startTime == null) {
            if (other.startTime != null)
                return false;
        } else if (!startTime.equals(other.startTime))
            return false;
        return true;
    }

    public int compareTo(Event o) {
        if (this.startTime.before(o.startTime)) {
            return -1;
        } else if (this.startTime.after(o.startTime)) {
            return 1;
        } else {
            return 0;
        }
    }

}
