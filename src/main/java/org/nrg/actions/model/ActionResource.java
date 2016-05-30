package org.nrg.actions.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class ActionResource {
    @JsonProperty("id") private Integer resourceId;
    @JsonProperty("name") private String resourceName;
    @JsonProperty("mount") private String mountName;
    private Boolean overwrite = false;

    public ActionResource() {}

    public ActionResource(final CommandMount commandMount) {
        this.resourceName = commandMount.getName();
        this.mountName = commandMount.getName();
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(final Integer resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(final String resourceName) {
        this.resourceName = resourceName;
    }

    public String getMountName() {
        return mountName;
    }

    public void setMountName(final String mountName) {
        this.mountName = mountName;
    }

    public Boolean getOverwrite() {
        return overwrite;
    }

    public void setOverwrite(final Boolean overwrite) {
        this.overwrite = overwrite;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ActionResource that = (ActionResource) o;
        return Objects.equals(this.resourceId, that.resourceId) &&
                Objects.equals(this.resourceName, that.resourceName) &&
                Objects.equals(this.mountName, that.mountName) &&
                Objects.equals(this.overwrite, that.overwrite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceId, resourceName, mountName, overwrite);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("resourceId", resourceId)
                .add("resourceName", resourceName)
                .add("mountName", mountName)
                .add("overwrite", overwrite)
                .toString();
    }
}