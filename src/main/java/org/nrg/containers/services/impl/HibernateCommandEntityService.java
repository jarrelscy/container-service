package org.nrg.containers.services.impl;

import com.google.common.collect.Lists;
import org.hibernate.exception.ConstraintViolationException;
import org.nrg.containers.daos.CommandEntityRepository;
import org.nrg.containers.model.command.entity.CommandEntity;
import org.nrg.containers.model.command.entity.CommandWrapperEntity;
import org.nrg.containers.services.CommandEntityService;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.exceptions.NrgRuntimeException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class HibernateCommandEntityService extends AbstractHibernateEntityService<CommandEntity, CommandEntityRepository>
        implements CommandEntityService {
    private static final Logger log = LoggerFactory.getLogger(HibernateCommandEntityService.class);

    @Override
    @Nonnull
    public List<CommandEntity> getAll() {
        final List<CommandEntity> commandEntities = super.getAll();
        return commandEntities == null ? Lists.<CommandEntity>newArrayList() : commandEntities;
    }

    @Override
    @Nonnull
    public CommandEntity create(@Nonnull final CommandEntity commandEntity) throws NrgRuntimeException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Saving command " + commandEntity.getName());
            }
            if (commandEntity.getCommandWrapperEntities() != null) {
                for (final CommandWrapperEntity commandWrapperEntity : commandEntity.getCommandWrapperEntities()) {
                    commandWrapperEntity.setCommandEntity(commandEntity);
                }
            }
            return super.create(commandEntity);
        } catch (ConstraintViolationException e) {
            throw new NrgServiceRuntimeException("This command duplicates a command already in the database.", e);
        }
    }

    @Override
    @Nonnull
    public List<CommandEntity> findByProperties(@Nonnull final Map<String, Object> properties) {
        final List<CommandEntity> commandEntities = getDao().findByProperties(properties);
        return commandEntities == null ? Lists.<CommandEntity>newArrayList() : commandEntities;
    }

    @Override
    public CommandWrapperEntity addWrapper(final @Nonnull CommandEntity commandEntity, final @Nonnull CommandWrapperEntity wrapperToAdd) {
        getDao().addWrapper(commandEntity, wrapperToAdd);
        return wrapperToAdd;
    }

    @Override
    @Nullable
    public CommandWrapperEntity retrieveWrapper(final long wrapperId) {
        return getDao().retrieveWrapper(wrapperId);
    }

    @Override
    @Nullable
    public CommandWrapperEntity retrieveWrapper(final long commandId, final String wrapperName) {
        return getDao().retrieveWrapper(commandId, wrapperName);
    }

    @Override
    @Nonnull
    public CommandWrapperEntity getWrapper(final long wrapperId) throws NotFoundException {
        final CommandWrapperEntity commandWrapperEntity = retrieveWrapper(wrapperId);
        if (commandWrapperEntity == null) {
            throw new NotFoundException(String.format("No command wrapper for id %d", wrapperId));
        }
        return commandWrapperEntity;
    }

    @Override
    @Nonnull
    public CommandWrapperEntity getWrapper(final long commandId, final String wrapperName) throws NotFoundException {
        final CommandWrapperEntity commandWrapperEntity = retrieveWrapper(commandId, wrapperName);
        if (commandWrapperEntity == null) {
            throw new NotFoundException(String.format("No command wrapper for command id %d, wrapper name %s", commandId, wrapperName));
        }
        return commandWrapperEntity;
    }

    @Override
    public CommandWrapperEntity update(final CommandWrapperEntity updates) throws NotFoundException {
        getDao().update(updates);
        return updates;
    }

    @Override
    public void deleteWrapper(final long wrapperId) {
        final CommandWrapperEntity commandWrapperEntity = retrieveWrapper(wrapperId);
        if (commandWrapperEntity != null) {
            getDao().delete(commandWrapperEntity);
        }
    }

    @Override
    public long getWrapperId(final long commandId, final String wrapperName) throws NotFoundException {
        return getDao().getWrapperId(commandId, wrapperName);
    }

    @Override
    public CommandEntity getCommandByWrapperId(final long wrapperId) throws NotFoundException {
        return getDao().getCommandByWrapperId(wrapperId);
    }
}
