package org.nrg.containers.services;

import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.containers.model.configuration.CommandConfigurationInternal;

public interface ContainerConfigService {
    String TOOL_ID = "container-service";

    long getDefaultDockerHubId();
    void setDefaultDockerHubId(long hubId, String username, String reason);

    void configureForSite(CommandConfigurationInternal commandConfiguration, long commandId, String wrapperName, String username, String reason) throws CommandConfigurationException;
    void configureForProject(CommandConfigurationInternal commandConfiguration, String project, long commandId, String wrapperName, String username, String reason) throws CommandConfigurationException;

    CommandConfigurationInternal getSiteConfiguration(long commandId, String wrapperName);
    CommandConfigurationInternal getProjectConfiguration(String project, long commandId, String wrapperName);

    void deleteSiteConfiguration(long commandId, String wrapperName, final String username) throws CommandConfigurationException;
    void deleteProjectConfiguration(String project, long commandId, String wrapperName, final String username) throws CommandConfigurationException;
    void deleteAllConfiguration(long commandId, String wrapperName);
    void deleteAllConfiguration(long commandId);

    void enableForSite(long commandId, String wrapperName, final String username, final String reason) throws CommandConfigurationException;

    void disableForSite(long commandId, String wrapperName, final String username, final String reason) throws CommandConfigurationException;
    Boolean isEnabledForSite(long commandId, String wrapperName);
    void enableForProject(String project, long commandId, String wrapperName, final String username, final String reason) throws CommandConfigurationException;
    void disableForProject(String project, long commandId, String wrapperName, final String username, final String reason) throws CommandConfigurationException;
    Boolean isEnabledForProject(String project, long commandId, String wrapperName);

    Boolean getAllEnabled();
    void enableAll(String username, String reason) throws ConfigServiceException;
    void disableAll(String username, String reason) throws ConfigServiceException;
    void deleteAllEnabledSetting(String username, String reason) throws ConfigServiceException;
    Boolean getAllEnabled(String project);
    void enableAll(String project, String username, String reason) throws ConfigServiceException;
    void disableAll(String project, String username, String reason) throws ConfigServiceException;
    void deleteAllEnabledSetting(String project, String username, String reason) throws ConfigServiceException;

    class CommandConfigurationException extends Exception {
        public CommandConfigurationException(final String message, final Throwable e) {
            super(message, e);
        }
    }
}
