package com.odysseusinc.arachne.portal.model.security;

import com.odysseusinc.arachne.portal.model.RawDataSource;
import com.odysseusinc.arachne.portal.model.RawUser;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.solr.SolrValue;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "tenants")
public class Tenant implements SolrValue {

    @Id
    @SequenceGenerator(name = "tenants_pk_sequence", sequenceName = "tenants_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tenants_pk_sequence")
    private Long id;

    @Column
    private String name;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @ManyToMany(targetEntity = RawUser.class, fetch = FetchType.LAZY)
    @JoinTable(name = "tenant_dependent_users_view",
            joinColumns = @JoinColumn(name = "tenant_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private Set<RawUser> users;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @ManyToMany(targetEntity = RawDataSource.class, fetch = FetchType.LAZY)
    @JoinTable(name = "tenants_data_sources",
            joinColumns = @JoinColumn(name = "tenant_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "data_source_id", referencedColumnName = "id"))
    private Set<RawDataSource> dataSources;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(targetEntity = Study.class, mappedBy = "tenant")
    private Set<Study> studies;

    @Column
    private Boolean isDefault;

    @PrePersist
    public void prePersist() {

        if (getDefault() == null) {
            setDefault(false);
        }
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public Set<RawUser> getUsers() {

        return users;
    }

    public void setUsers(Set<RawUser> users) {

        this.users = users;
    }

    public Set<RawDataSource> getDataSources() {

        return dataSources;
    }

    public void setDataSources(Set<RawDataSource> dataSources) {

        this.dataSources = dataSources;
    }

    public Set<Study> getStudies() {

        return studies;
    }

    public void setStudies(Set<Study> studies) {

        this.studies = studies;
    }

    public Boolean getDefault() {

        return isDefault;
    }

    public void setDefault(Boolean aDefault) {

        isDefault = aDefault;
    }

    @Transient
    @Override
    public Object getSolrValue() {

        return id;
    }

    @Transient
    @Override
    public Object getSolrQueryValue() {

        return id;
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(this.getId());
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Tenant)) {
            return false;
        }
        final Tenant s = (Tenant) obj;
        return Objects.equals(getId(), s.getId());
    }
}
