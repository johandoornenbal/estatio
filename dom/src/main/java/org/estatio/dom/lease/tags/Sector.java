/*
 *
 *  Copyright 2012-2013 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.dom.lease.tags;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.annotations.DiscriminatorStrategy;

import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.Title;

import org.estatio.dom.EstatioRefDataObject;
import org.estatio.dom.WithNameComparable;
import org.estatio.dom.WithNameUnique;

@javax.jdo.annotations.PersistenceCapable
@javax.jdo.annotations.Discriminator(strategy = DiscriminatorStrategy.CLASS_NAME)
@javax.jdo.annotations.Queries({
    @javax.jdo.annotations.Query(
            name = "findByName", language = "JDOQL", 
            value = "SELECT "
                    + "FROM org.estatio.dom.lease.tags.Sector "
                    + "WHERE name == :name"),
    @javax.jdo.annotations.Query(
            name = "findUniqueNames", language = "JDOQL", 
            value = "SELECT name "
                    + "FROM org.estatio.dom.lease.tags.Sector") 
})
@javax.jdo.annotations.Uniques({
    @javax.jdo.annotations.Unique(name = "SECTOR_NAME_UNIQUE_IDX", members="name")
})
@Immutable
public class Sector extends EstatioRefDataObject<Sector> implements WithNameUnique, WithNameComparable<Sector> {

    public Sector() {
        super("name");
    }

    // //////////////////////////////////////

    private String name;

    @javax.jdo.annotations.Column(allowsNull="false")
    @Title
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
    
    // //////////////////////////////////////


    @javax.jdo.annotations.Persistent(mappedBy = "sector")
    private SortedSet<Activity> activities = new TreeSet<Activity>();

    public SortedSet<Activity> getActivities() {
        return activities;
    }

    public void setActivities(final SortedSet<Activity> activities) {
        this.activities = activities;
    }

}