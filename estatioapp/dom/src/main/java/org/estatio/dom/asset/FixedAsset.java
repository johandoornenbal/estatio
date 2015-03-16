/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
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
package org.estatio.dom.asset;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.VersionStrategy;

import com.google.common.collect.Sets;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.RenderType;
import org.apache.isis.applib.annotation.Title;

import org.estatio.dom.EstatioDomainObject;
import org.estatio.dom.JdoColumnLength;
import org.estatio.dom.RegexValidation;
import org.estatio.dom.WithNameComparable;
import org.estatio.dom.WithReferenceUnique;
import org.estatio.dom.communicationchannel.CommunicationChannelOwner;
import org.estatio.dom.party.Party;

@javax.jdo.annotations.PersistenceCapable(identityType = IdentityType.DATASTORE)
@javax.jdo.annotations.DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE,
        column = "id")
@javax.jdo.annotations.Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@javax.jdo.annotations.Discriminator(
        strategy = DiscriminatorStrategy.CLASS_NAME,
        column = "discriminator")
@javax.jdo.annotations.Uniques({
        @javax.jdo.annotations.Unique(
                name = "FixedAsset_reference_UNQ", members = { "reference" })
})
@javax.jdo.annotations.Indices({
        // to cover the 'findAssetsByReferenceOrName' query
        // both in this superclass and the subclasses
        @javax.jdo.annotations.Index(
                name = "FixedAsset_reference_name_IDX", members = { "reference", "name" })
})
@javax.jdo.annotations.Queries({
        @javax.jdo.annotations.Query(
                name = "matchByReferenceOrName", language = "JDOQL",
                value = "SELECT FROM org.estatio.dom.asset.FixedAsset "
                        + "WHERE reference.matches(:regex) "
                        + "|| name.matches(:regex) ")
})
@DomainObject(editing = Editing.DISABLED, autoCompleteRepository = FixedAssets.class, autoCompleteAction = "autoComplete")
@DomainObjectLayout(bookmarking = BookmarkPolicy.AS_ROOT)
public abstract class FixedAsset
        extends EstatioDomainObject<FixedAsset>
        implements WithNameComparable<FixedAsset>, WithReferenceUnique, CommunicationChannelOwner {

    public FixedAsset() {
        super("name");
    }

    // //////////////////////////////////////

    private String reference;

    @javax.jdo.annotations.Column(allowsNull = "false", length = JdoColumnLength.REFERENCE)
    @Title(sequence = "1", prepend = "[", append = "] ")
    @Property(regexPattern = RegexValidation.REFERENCE)
    @PropertyLayout(describedAs = "Unique reference code for this asset")
    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    // //////////////////////////////////////

    // /**
    // * Although both {@link Property} and {@link Unit} (the two subclasses)
    // have
    // * a name, they are mapped separately because they have different
    // uniqueness
    // * constraints.
    // *
    // * <p>
    // * For {@link Property}, the {@link Property#getName() name} by itself is
    // unique.
    // *
    // * <p>
    // * For {@link Unit}, the combination of ({@link Unit#getProperty()
    // property}, {@link Unit#getName() name})
    // * is unique.
    // */
    // public abstract String getName();

    private String name;

    @javax.jdo.annotations.Column(allowsNull = "false", length = JdoColumnLength.NAME)
    @Title(sequence = "2")
    @PropertyLayout(describedAs = "Unique name for this property")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    // //////////////////////////////////////

    private String externalReference;

    @javax.jdo.annotations.Column(allowsNull = "true", length = JdoColumnLength.REFERENCE)
    @Property(optionality = Optionality.OPTIONAL)
    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(final String externalReference) {
        this.externalReference = externalReference;
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Persistent(mappedBy = "asset")
    private SortedSet<FixedAssetRole> roles = new TreeSet<FixedAssetRole>();

    @CollectionLayout(render = RenderType.EAGERLY)
    public SortedSet<FixedAssetRole> getRoles() {
        return roles;
    }

    public void setRoles(final SortedSet<FixedAssetRole> roles) {
        this.roles = roles;
    }

    public FixedAsset newRole(
            final @ParameterLayout(named = "Type") FixedAssetRoleType type,
            final Party party,
            final @Parameter(optionality = Optionality.OPTIONAL) @ParameterLayout(named = "Start date") LocalDate startDate,
            final @Parameter(optionality = Optionality.OPTIONAL) @ParameterLayout(named = "End date") LocalDate endDate) {
        createRole(type, party, startDate, endDate);
        return this;
    }

    public String validateNewRole(
            final FixedAssetRoleType type,
            final Party party,
            final LocalDate startDate,
            final LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return "End date cannot be earlier than start date";
        }
        if (!Sets.filter(getRoles(), type.matchingRole()).isEmpty()) {
            return "Add a successor/predecessor from existing role";
        }
        return null;
    }

    @Programmatic
    public FixedAssetRole createRole(
            final FixedAssetRoleType type, final Party party, final LocalDate startDate, final LocalDate endDate) {
        final FixedAssetRole role = newTransientInstance(FixedAssetRole.class);
        role.setStartDate(startDate);
        role.setEndDate(endDate);
        role.setType(type); // must do before associate with agreement, since
                            // part of AgreementRole#compareTo impl.

        // JDO will manage the relationship for us
        // see http://markmail.org/thread/b6lpzktr6hzysisp, Dan's email
        // 2013-7-17
        role.setParty(party);
        role.setAsset(this);

        persistIfNotAlready(role);

        return role;
    }

}
