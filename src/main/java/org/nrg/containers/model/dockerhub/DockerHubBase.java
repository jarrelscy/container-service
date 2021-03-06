package org.nrg.containers.model.dockerhub;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

public abstract class DockerHubBase {

    @JsonProperty("id") public abstract long id();
    @Nullable @JsonProperty("name") public abstract String name();
    @Nullable @JsonProperty("url") public abstract String url();
    @JsonProperty("default") public abstract Boolean isDefault();

    @AutoValue
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public abstract static class DockerHub extends DockerHubBase {
        public static final String DEFAULT_NAME = "Docker Hub";
        public static final String DEFAULT_URL = "https://index.docker.io/v1/";
        public static final DockerHub DEFAULT = DockerHub.create(0L, DEFAULT_NAME, DEFAULT_URL, false);

        @JsonCreator
        public static DockerHub create(@JsonProperty("id") final Long id,
                                       @JsonProperty("name") final String name,
                                       @JsonProperty("url") final String url,
                                       @JsonProperty("default") final Boolean isDefault) {
            return new AutoValue_DockerHubBase_DockerHub(id == null ? 0L : id, name, url, isDefault == null ? false : isDefault);
        }
    }

    @AutoValue
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public abstract static class DockerHubWithPing extends DockerHubBase {
        @Nullable @JsonProperty("ping") public abstract Boolean ping();

        @JsonCreator
        public static DockerHubWithPing create(@JsonProperty("id") final Long id,
                                               @JsonProperty("name") final String name,
                                               @JsonProperty("url") final String url,
                                               @JsonProperty("default") final Boolean isDefault,
                                               @JsonProperty("ping") final Boolean ping) {
            return new AutoValue_DockerHubBase_DockerHubWithPing(id == null ? 0L : id, name, url, isDefault == null ? false : isDefault, ping);
        }

        public static DockerHubWithPing create(final DockerHub dockerHub,
                                               final Boolean ping) {
            return create(
                    dockerHub.id(),
                    dockerHub.name(),
                    dockerHub.url(),
                    dockerHub.isDefault(),
                    ping
            );
        }
    }
}
