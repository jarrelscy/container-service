package org.nrg.containers.model.xnat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.nrg.containers.model.command.entity.CommandWrapperInputType;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.helpers.uri.archive.AssessorURII;
import org.nrg.xnat.helpers.uri.archive.impl.ExptAssessorURI;
import org.nrg.xnat.helpers.uri.archive.impl.ExptURI;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)
public class Assessor extends XnatModelObject {
    @JsonIgnore private XnatImageassessordataI xnatImageassessordataI;
    @JsonIgnore private XnatImagesessiondata parent;
    private List<Resource> resources;
    @JsonProperty("project-id") private String projectId;
    @JsonProperty("session-id") private String sessionId;
    private String directory;

    public Assessor() {}

    public Assessor(final AssessorURII assessorURII, final boolean loadFiles,
                    final Map<String, Boolean> loadTypesMap) {
        this.xnatImageassessordataI = assessorURII.getAssessor();
        if (ExptAssessorURI.class.isAssignableFrom(assessorURII.getClass())) {
            parent = ((ExptAssessorURI) assessorURII).getSession();
        }
        this.uri = ((URIManager.DataURIA) assessorURII).getUri();
        populateProperties(null, loadFiles, loadTypesMap);
    }

    public Assessor(final XnatImageassessordataI xnatImageassessordataI, final boolean loadFiles,
                    final Map<String, Boolean> loadTypesMap) {
        this(xnatImageassessordataI, loadFiles, loadTypesMap, null, null);
    }

    public Assessor(final XnatImageassessordataI xnatImageassessordataI, final boolean loadFiles,
                    final Map<String, Boolean> loadTypesMap, final String parentUri, final String rootArchivePath) {
        this.xnatImageassessordataI = xnatImageassessordataI;

        if (parentUri == null) {
            final String parentId = xnatImageassessordataI.getImagesessionId();
            if (StringUtils.isNotBlank(parentId)) {
                this.uri = "/experiments/" + parentId + "/assessors/" + xnatImageassessordataI.getId();
            } else {
                this.uri = UriParserUtils.getArchiveUri(xnatImageassessordataI);
            }
        } else {
            this.uri = parentUri + "/assessors/" + xnatImageassessordataI.getId();
        }
        populateProperties(rootArchivePath, loadFiles, loadTypesMap);
    }

    private void populateProperties(final String rootArchivePath, final boolean loadFiles,
                                    @Nullable final Map<String, Boolean> loadTypesMap) {
        this.id = xnatImageassessordataI.getId();
        this.label = xnatImageassessordataI.getLabel();
        this.xsiType = xnatImageassessordataI.getXSIType();
        this.projectId = xnatImageassessordataI.getProject();
        this.sessionId = parent == null ? xnatImageassessordataI.getImagesessionId() : parent.getId();

        this.directory = null;
        final XnatImageassessordata assessor = ((XnatImageassessordata) xnatImageassessordataI);
        final File sessionDir = parent != null ? parent.getSessionDir() : null;
        if (sessionDir != null && sessionDir.isDirectory()) {
            final File assessorsDir = new File(sessionDir, "ASSESSORS");
            if (assessorsDir.isDirectory()) {
                final File assessorDir = new File(assessorsDir, assessor.getArchiveDirectoryName());
                if (assessorDir.isDirectory()) {
                    this.directory = assessorDir.getAbsolutePath();
                }
            }
        }

        this.resources = Lists.newArrayList();
        if (loadFiles || (loadTypesMap != null && loadTypesMap.get(CommandWrapperInputType.RESOURCE.getName()))) {
            for (final XnatAbstractresourceI xnatAbstractresourceI : xnatImageassessordataI.getResources_resource()) {
                if (xnatAbstractresourceI instanceof XnatResourcecatalog) {
                    resources.add(new Resource((XnatResourcecatalog) xnatAbstractresourceI, loadFiles, loadTypesMap,
                            this.uri, rootArchivePath));
                }
            }
        }
    }

    public static Function<URIManager.ArchiveItemURI, Assessor> uriToModelObject(final boolean loadFiles,
                                                                                 final Map<String, Boolean> loadTypesMap) {
        return new Function<URIManager.ArchiveItemURI, Assessor>() {
            @Nullable
            @Override
            public Assessor apply(@Nullable URIManager.ArchiveItemURI uri) {
                if (uri != null &&
                        AssessorURII.class.isAssignableFrom(uri.getClass())) {
                    final XnatImageassessordata assessor = ((AssessorURII) uri).getAssessor();
                    if (assessor != null &&
                            XnatImageassessordata.class.isAssignableFrom(assessor.getClass())) {
                        return new Assessor((AssessorURII) uri, loadFiles, loadTypesMap);
                    }
                } else if (uri != null &&
                        ExptURI.class.isAssignableFrom(uri.getClass())) {
                    final XnatExperimentdata expt = ((ExptURI) uri).getExperiment();
                    if (expt != null &&
                            XnatImageassessordata.class.isAssignableFrom(expt.getClass())) {
                        return new Assessor((XnatImageassessordata) expt, loadFiles, loadTypesMap);
                    }
                }

                return null;
            }
        };
    }

    public static Function<String, Assessor> idToModelObject(final UserI userI, final boolean loadFiles,
                                                             final Map<String, Boolean> loadTypesMap) {
        return new Function<String, Assessor>() {
            @Nullable
            @Override
            public Assessor apply(@Nullable String s) {
                if (StringUtils.isBlank(s)) {
                    return null;
                }
                final XnatImageassessordata xnatImageassessordata =
                        XnatImageassessordata.getXnatImageassessordatasById(s, userI, true);
                if (xnatImageassessordata != null) {
                    return new Assessor(xnatImageassessordata, loadFiles, loadTypesMap);
                }
                return null;
            }
        };
    }

    public Project getProject(final UserI userI, final boolean loadFiles,
                              final Map<String, Boolean> loadTypesMap) {
        loadXnatImageassessordataI(userI);
        return new Project(xnatImageassessordataI.getProject(), userI, loadFiles, loadTypesMap);
    }

    public Session getSession(final UserI userI, final boolean loadFiles,
                              final Map<String, Boolean> loadTypesMap) {
        loadXnatImageassessordataI(userI);
        return new Session(xnatImageassessordataI.getImagesessionId(), userI, loadFiles, loadTypesMap);
    }

    public void loadXnatImageassessordataI(final UserI userI) {
        if (xnatImageassessordataI == null) {
            xnatImageassessordataI = XnatImageassessordata.getXnatImageassessordatasById(id, userI, false);
        }
    }

    public XnatImageassessordataI getXnatImageassessordataI() {
        return xnatImageassessordataI;
    }

    public void setXnatImageassessordataI(final XnatImageassessordataI xnatImageassessordataI) {
        this.xnatImageassessordataI = xnatImageassessordataI;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(final List<Resource> resources) {
        this.resources = resources;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(final String directory) {
        this.directory = directory;
    }

    @Override
    public XFTItem getXftItem(final UserI userI) {
        loadXnatImageassessordataI(userI);
        return xnatImageassessordataI == null ? null : ((XnatImageassessordata) xnatImageassessordataI).getItem();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final Assessor that = (Assessor) o;
        return Objects.equals(this.resources, that.resources) &&
                Objects.equals(this.projectId, that.projectId) &&
                Objects.equals(this.sessionId, that.sessionId) &&
                Objects.equals(this.directory, that.directory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), resources, projectId, sessionId, directory);
    }

    @Override
    public String toString() {
        return addParentPropertiesToString(MoreObjects.toStringHelper(this))
                .add("resources", resources)
                .add("projectId", projectId)
                .add("sessionId", sessionId)
                .add("directory", directory)
                .toString();
    }
}
