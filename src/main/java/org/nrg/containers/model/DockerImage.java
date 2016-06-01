package org.nrg.containers.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Audited
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
@ApiModel(description = "Properties that define an image.")
public class DockerImage extends AbstractHibernateEntity {

    private String name;
    @JsonProperty("image-id") private String imageId;
    @JsonProperty("repo-tags") private List<String> repoTags = Lists.newArrayList();
    private Map<String, String> labels = Maps.newHashMap();

    public DockerImage() {}

    public DockerImage(final String name,
                       final String imageId,
                       final List<String> repoTags,
                       final Map<String, String> labels) {
        this.name = name;
        this.imageId = imageId;
        setRepoTags(repoTags);
        setLabels(labels);
    }

    /**
     * The image's XNAT name.
     **/
    @ApiModelProperty(value = "The image's name.")
    public String getName() {
        return name;
    }

    public void setName(final String name) { this.name = name; }

    /**
     * The image's docker id.
     **/
    @ApiModelProperty(value = "The image's id.")
    public String getImageId() { return imageId; }

    public void setImageId(final String imageId) { this.imageId = imageId; }

    /**
     * The image's repo tags.
     **/
    @ElementCollection(fetch = FetchType.EAGER)
    @ApiModelProperty(value = "The image's repo tags.")
    public List<String> getRepoTags() { return repoTags; }

    public void setRepoTags(final List<String> repoTags) {
        this.repoTags = repoTags == null ?
                Lists.<String>newArrayList() :
                repoTags;
    }

    /**
     * Image labels
     **/
    @ApiModelProperty(value = "Image labels")
    @ElementCollection(fetch = FetchType.EAGER)
    public Map<String, String> getLabels() { return labels; }

    public void setLabels(final Map<String, String> labels) {
        this.labels = labels == null ?
                Maps.<String, String>newHashMap() :
                labels;
    }

    public String toString() {
        return addParentPropertiesToString(MoreObjects.toStringHelper(this))
                .add("imageId", imageId)
                .add("name", name)
                .add("tags", repoTags)
                .add("labels", labels)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !super.equals(o) || getClass() != o.getClass()) {
            return false;
        }

        DockerImage that = (DockerImage) o;

        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.imageId, that.imageId) &&
                Objects.equals(this.repoTags, that.repoTags) &&
                Objects.equals(this.labels, that.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, imageId, repoTags, labels);
    }
}
