package org.nrg.containers.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.nrg.containers.exceptions.*;
import org.nrg.containers.model.command.auto.Command;
import org.nrg.containers.model.command.auto.CommandSummaryForContext;
import org.nrg.containers.model.configuration.CommandConfiguration;
import org.nrg.containers.model.xnat.*;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.actions.MultiActionProvider;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommandActionProvider extends MultiActionProvider {
    private final String DISPLAY_NAME = "Container Service Action Provider";
    private final String DESCRIPTION = "This Action Provider facilitates linking Event Service events to Container Service commands.";

    private static final Logger log = LoggerFactory.getLogger(CommandActionProvider.class);

    private final ContainerService containerService;
    private final CommandService commandService;
    private final ContainerConfigService containerConfigService;
    private final ObjectMapper mapper;

    @Autowired
    public CommandActionProvider(final ContainerService containerService,
                                 final CommandService commandService,
                                 final ContainerConfigService containerConfigService,
                                 final ObjectMapper mapper) {
        this.containerService = containerService;
        this.commandService = commandService;
        this.containerConfigService = containerConfigService;
        this.mapper = mapper;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public void processEvent(EventServiceEvent event, SubscriptionEntity subscription, final UserI user) {
        final Object eventObject = event.getObject();
        String objectClass = event.getObjectClass();
        final long wrapperId;
        try {
            wrapperId = Long.parseLong(actionKeyToActionId(subscription.getActionKey()));
        }catch(Exception e){
            log.error("Could not extract WrapperId from actionKey:" + subscription.getActionKey());
            log.error("Aborting subscription: " + subscription.getName());
            return;
        }
        String projectId = null;
        final Map inputValues = subscription.getAttributes() != null ? subscription.getAttributes() : Maps.newHashMap();
        try {

            // Setup XNAT Object for Container
            XnatModelObject modelObject = null;
            String objectLabel = "";
            if(eventObject instanceof XnatProjectdata){
                modelObject = new Project(((XnatProjectdata) eventObject));
                objectLabel = "project";
            } else if(eventObject instanceof XnatSubjectdataI){
                modelObject = new Subject((XnatSubjectdataI) eventObject);
                objectLabel = "subject";
            } else if(eventObject instanceof XnatImagesessiondataI){
                modelObject = new Session((XnatImagesessiondataI) eventObject);
                objectLabel = "session";
            } else if(eventObject instanceof XnatImagescandataI){
                Session session = new Session(((XnatImagescandataI)eventObject).getImageSessionId(), user);
                String sessionUri = session.getUri();
                modelObject = new Scan((XnatImagescandataI) eventObject, sessionUri, null);
                objectLabel = "scan";
            } else if(eventObject instanceof XnatImageassessordataI){
                modelObject = new Assessor((XnatImageassessordataI) eventObject);
                objectLabel = "assessor";
            } else if(eventObject instanceof XnatResourcecatalog){
                modelObject = new Resource((XnatResourcecatalog) eventObject);
                objectLabel = "resource";
            } else {
                log.error(String.format("Container Service does not support %s Event Object.", objectClass));
            }
            String objectString = modelObject.getUri();
            try {
                objectString = mapper.writeValueAsString(modelObject);
            } catch (JsonProcessingException e) {
                log.error(String.format("Could not serialize ModelObject %s to json.", objectLabel), e);
            }
            inputValues.put(objectLabel, objectString);
            containerService.resolveCommandAndLaunchContainer(wrapperId, inputValues, user);

        } catch (NotFoundException | CommandResolutionException | NoDockerServerException | DockerServerException | ContainerException | UnauthorizedException e) {
            log.error("Error launching command wrapper " + wrapperId, e);
        }

    }


    @Override
    public List<Action> getAllActions() {
        List<Action> actions = new ArrayList<>();
        List<Command> commands = commandService.getAll();
        for(Command command : commands){
            for(Command.CommandWrapper wrapper : command.xnatCommandWrappers()) {
                actions.add(Action.builder()
                                  .id(String.valueOf(wrapper.id()))
                                  .displayName(wrapper.name())
                                  .description(wrapper.description())
                                  .provider(this)
                                  .actionKey(actionIdToActionKey(Long.toString(wrapper.id())))
                                  .build());
                }
            }
        return actions;
    }



    @Override
    public List<Action> getActions(UserI user) {
        return getActions(null, user);
    }

    @Override
    public List<Action> getActions(String xsiType, UserI user) {
        return getActions(null, xsiType, user);
    }

    @Override
    public List<Action> getActions(String projectId, String xsiType, UserI user) {
        List<Action> actions = new ArrayList<>();
        try {
            List<CommandSummaryForContext> available;
            if(projectId != null) {
                // Project configured Commands
                available = commandService.available(projectId, xsiType, user);
            } else {
                // Site configured Commands
                available = commandService.available(xsiType, user);
            }

            for(CommandSummaryForContext command : available){
                if(!command.enabled()) continue;
                Map<String, String> attributes = new HashMap<>();
                try {
                    ImmutableMap<String, CommandConfiguration.CommandInputConfiguration> inputs = commandService.getSiteConfiguration(command.wrapperId()).inputs();
                    for (String key : inputs.keySet()) {
                        CommandConfiguration.CommandInputConfiguration input = inputs.get(key);
                        if ((input.userSettable() == null || input.userSettable())
                                && (input.type().equalsIgnoreCase("string") || input.type().equalsIgnoreCase("boolean"))) {
                            attributes.put(
                                    key + " (" + input.type() + ")",
                                    input.defaultValue() == null ? "" : input.defaultValue());
                        }
                    }
                } catch (Exception e) {
                    log.error("Exception getting Command Configuration for command: " + command.commandName() + "\n" + e.getMessage());
                    e.printStackTrace();
                }

                actions.add(Action.builder()
                                  .id(String.valueOf(command.wrapperId()))
                                  .displayName(command.wrapperName())
                                  .description(command.wrapperDescription())
                                  .provider(this)
                                  .actionKey(actionIdToActionKey(Long.toString(command.wrapperId())))
                                  .attributes(attributes.isEmpty() ? null : attributes)
                                  .build());
            }
        } catch (ElementNotFoundException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return actions;
    }

    @Override
    public Boolean isActionAvailable(String actionKey, String projectId, String xnatType, UserI user) {
        for (Command command : commandService.getAll()) {
            for(Command.CommandWrapper wrapper : command.xnatCommandWrappers()){
                if(Long.toString(wrapper.id()).contentEquals(actionKeyToActionId(actionKey))){
                    if(Strings.isNullOrEmpty(xnatType) || wrapper.contexts().contains(xnatType)){
                        if( (Strings.isNullOrEmpty(projectId) && containerConfigService.isEnabledForSite(wrapper.id())) ||
                                (!Strings.isNullOrEmpty(projectId) && containerConfigService.isEnabledForProject(projectId, wrapper.id())) ){
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
