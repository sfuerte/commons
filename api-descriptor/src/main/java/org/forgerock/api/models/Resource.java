/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2016 ForgeRock AS.
 */

package org.forgerock.api.models;

import static org.forgerock.api.util.ValidationUtil.isEmpty;

import org.forgerock.api.ApiValidationException;
import org.forgerock.api.annotations.Actions;
import org.forgerock.api.annotations.Queries;
import org.forgerock.api.annotations.RequestHandler;

import org.forgerock.util.Reject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Class that represents the Resource type in API descriptor.
 */
public final class Resource {
    private static final Logger LOGGER = LoggerFactory.getLogger(Resource.class);

    @JsonProperty("$ref")
    private final Reference reference;
    private final Schema resourceSchema;
    private final String description;
    private final Create create;
    private final Read read;
    private final Update update;
    private final Delete delete;
    private final Patch patch;
    private final Action[] actions;
    private final Query[] queries;
    private final SubResources subresources;
    private final Resource items;
    private final Boolean mvccSupported;

    private Resource(Builder builder) {
        this.reference = builder.reference;
        this.resourceSchema = builder.resourceSchema;
        this.description = builder.description;
        this.create = builder.create;
        this.read = builder.read;
        this.update = builder.update;
        this.delete = builder.delete;
        this.patch = builder.patch;
        this.subresources = builder.subresources;
        this.actions = builder.actions.toArray(new Action[builder.actions.size()]);
        this.queries = builder.queries.toArray(new Query[builder.queries.size()]);
        this.items = builder.items;
        this.mvccSupported = builder.mvccSupported;

        if ((create != null || read != null || update != null || delete != null || patch != null
                || !isEmpty(actions) || !isEmpty(queries)) && reference != null) {
            throw new ApiValidationException("Cannot have a reference as well as operations");
        }
        if (mvccSupported == null) {
            throw new ApiValidationException("mvccSupported required");
        }
    }

    /**
     * Getter of resoruce schema.
     *
     * @return Resource schema
     */
    public Schema getResourceSchema() {
        return resourceSchema;
    }

    /**
     * Getter of description.
     *
     * @return Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter of Create.
     *
     * @return Create
     */
    public Create getCreate() {
        return create;
    }

    /**
     * Getter of Read.
     *
     * @return Read
     */
    public Read getRead() {
        return read;
    }

    /**
     * Getter of Update.
     *
     * @return Update
     */
    public Update getUpdate() {
        return update;
    }

    /**
     * Getter of Delete.
     *
     * @return Delete
     */
    public Delete getDelete() {
        return delete;
    }

    /**
     * Getter of Patch.
     *
     * @return Patch
     */
    public Patch getPatch() {
        return patch;
    }

    /**
     * Getter of actions.
     *
     * @return Actions
     */
    public Action[] getActions() {
        return actions;
    }

    /**
     * Getter of queries.
     *
     * @return Queries
     */
    public Query[] getQueries() {
        return queries;
    }

    /**
     * Getter of sub-resources.
     *
     * @return Sub-resources
     */
    public SubResources getSubresources() {
        return subresources;
    }

    /**
     * Informs if MVCC is supported.
     *
     * @return {@code true} if MVCC is supported and {@code false} otherwise
     */
    public boolean isMvccSupported() {
        return mvccSupported;
    }

    /**
     * Create a new Builder for Resoruce.
     *
     * @return Builder
     */
    public static Builder resource() {
        return new Builder();
    }

    /**
     * Build a {@code Resource} from an annotated request handler.
     * @param type The annotated type.
     * @param variant The annotated type variant.
     * @param descriptor The root descriptor to add definitions to.
     * @return The built {@code Resource} object.
     */
    public static Resource fromAnnotatedType(Class<?> type, AnnotatedTypeVariant variant, ApiDescription descriptor) {
        Builder builder = resource();
        RequestHandler requestHandler = type.getAnnotation(RequestHandler.class);
        if (requestHandler == null) {
            LOGGER.warn("Asked for Resource for annotated type, but type does not have required RequestHandler"
                    + " annotation. Returning null for " + type);
            return null;
        }
        boolean foundCrudpq = false;
        for (Method m : type.getDeclaredMethods()) {
            boolean instanceMethod = Arrays.asList(m.getParameterTypes()).indexOf(String.class) > -1;
            org.forgerock.api.annotations.Action action = m.getAnnotation(org.forgerock.api.annotations.Action.class);
            if (action != null && instanceMethod == variant.actionRequiresId) {
                builder.actions.add(Action.fromAnnotation(action, m, descriptor, type));
            }
            Actions actions = m.getAnnotation(Actions.class);
            if (actions != null && instanceMethod == variant.actionRequiresId) {
                for (org.forgerock.api.annotations.Action a : actions.value()) {
                    builder.actions.add(Action.fromAnnotation(a, null, descriptor, type));
                }
            }
            org.forgerock.api.annotations.Create create = m.getAnnotation(org.forgerock.api.annotations.Create.class);
            if (create != null) {
                builder.create = Create.fromAnnotation(create, variant.instanceCreate, descriptor, type);
                foundCrudpq = true;
            }
            if (variant.rudpOperations) {
                org.forgerock.api.annotations.Read read = m.getAnnotation(org.forgerock.api.annotations.Read.class);
                if (read != null) {
                    builder.read = Read.fromAnnotation(read, descriptor, type);
                    foundCrudpq = true;
                }
                org.forgerock.api.annotations.Update update =
                        m.getAnnotation(org.forgerock.api.annotations.Update.class);
                if (update != null) {
                    builder.update = Update.fromAnnotation(update, descriptor, type);
                    foundCrudpq = true;
                }
                org.forgerock.api.annotations.Delete delete =
                        m.getAnnotation(org.forgerock.api.annotations.Delete.class);
                if (delete != null) {
                    builder.delete = Delete.fromAnnotation(delete, descriptor, type);
                    foundCrudpq = true;
                }
                org.forgerock.api.annotations.Patch patch = m.getAnnotation(org.forgerock.api.annotations.Patch.class);
                if (patch != null) {
                    builder.patch = Patch.fromAnnotation(patch, descriptor, type);
                    foundCrudpq = true;
                }
            }
            if (variant.queryOperations) {
                org.forgerock.api.annotations.Query query = m.getAnnotation(org.forgerock.api.annotations.Query.class);
                if (query != null) {
                    builder.queries.add(Query.fromAnnotation(query, m, descriptor, type));
                    foundCrudpq = true;
                }
                Queries queries = m.getAnnotation(Queries.class);
                if (queries != null) {
                    for (org.forgerock.api.annotations.Query q : queries.value()) {
                        builder.queries.add(Query.fromAnnotation(q, null, descriptor, type));
                        foundCrudpq = true;
                    }
                }
            }
        }
        Schema resourceSchema = Schema.fromAnnotation(requestHandler.resourceSchema(), descriptor, type);
        if (foundCrudpq && resourceSchema == null) {
            throw new IllegalArgumentException("CRUDPQ operation(s) defined, but no resource schema declared");
        }
        builder.resourceSchema(resourceSchema);
        builder.mvccSupported(requestHandler.mvccSupported());
        return builder.build();
    }

    /**
     * Gets the reference.
     * @return The reference.
     */
    public Reference getReference() {
        return reference;
    }

    /**
     * The varient of the annotated type. Allows the annotation processing to make assumptions about what type of
     * operations are expected from this context of the type.
     */
    public enum AnnotatedTypeVariant {
        /** A singleton resource handler. (expect RUDPA operations). */
        SINGLETON_RESOURCE(true, true, false, false),
        /** A collection resource handler, collection endpoint (expect CAQ opererations). */
        COLLECTION_RESOURCE_COLLECTION(false, false, false, true),
        /** A collection resource handler, instance endpoint (expect CRUDPA operations). */
        COLLECTION_RESOURCE_INSTANCE(true, true, true, false),
        /** A plain request handler (expects all operations). */
        REQUEST_HANDLER(false, true, false, true);

        private final boolean instanceCreate;
        private final boolean rudpOperations;
        private final boolean actionRequiresId;
        private final boolean queryOperations;

        AnnotatedTypeVariant(boolean instanceCreate, boolean rudpOperations, boolean actionRequiresId,
                boolean queryOperations) {
            this.instanceCreate = instanceCreate;
            this.rudpOperations = rudpOperations;
            this.actionRequiresId = actionRequiresId;
            this.queryOperations = queryOperations;
        }
    }

    /**
     * Builder to help construct the Resource.
     */
    public final static class Builder {
        private Schema resourceSchema;
        private String description;
        private Create create;
        private Read read;
        private Update update;
        private Delete delete;
        private Patch patch;
        private SubResources subresources;
        private final Set<Action> actions;
        private final Set<Query> queries;
        private Resource items;
        private Boolean mvccSupported;
        public Reference reference;

        /**
         * Private default constructor.
         */
        protected Builder() {
            actions = new TreeSet<>();
            queries = new TreeSet<>();
        }

        /**
         * Set a reference.
         * @param reference The reference.
         * @return This builder.
         */
        public Builder reference(Reference reference) {
            this.reference = reference;
            return this;
        }

        /**
         * Set the resource schema.
         *
         * @param resourceSchema The schema of the resource for this path.
         * Required when any of create, read, update, delete, patch are supported
         * @return Builder
         */
        public Builder resourceSchema(Schema resourceSchema) {
            this.resourceSchema = resourceSchema;
            return this;
        }

        /**
         * Set the description.
         *
         * @param description A description of the endpoint
         * @return Builder
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Set create.
         *
         * @param create The create operation description, if supported
         * @return Builder
         */
        public Builder create(Create create) {
            this.create = create;
            return this;
        }

        /**
         * Set Read.
         *
         * @param read The read operation description, if supported
         * @return Builder
         */
        public Builder read(Read read) {
            this.read = read;
            return this;
        }

        /**
         * Set Update.
         *
         * @param update The update operation description, if supported
         * @return Builder
         */
        public Builder update(Update update) {
            this.update = update;
            return this;
        }

        /**
         * Set Delete.
         *
         * @param delete The delete operation description, if supported
         * @return Builder
         */
        public Builder delete(Delete delete) {
            this.delete = delete;
            return this;
        }

        /**
         * Set Patch.
         *
         * @param patch The patch operation description, if supported
         * @return Builder
         */
        public Builder patch(Patch patch) {
            this.patch = patch;
            return this;
        }

        /**
         * Set Actions.
         *
         * @param actions The list of action operation descriptions, if supported
         * @return Builder
         */
        public Builder actions(List<Action> actions) {
            this.actions.addAll(actions);
            return this;
        }

        /**
         * Adds one Action to the list of Actions.
         *
         * @param action Action operation description to be added to the list
         * @return Builder
         */
        public Builder action(Action action) {
            this.actions.add(action);
            return this;
        }

        /**
         * Set Queries.
         *
         * @param queries The list or query operation descriptions, if supported
         * @return Builder
         */
        public Builder queries(List<Query> queries) {
            this.queries.addAll(queries);
            return this;
        }

        /**
         * Adds one Query to the list of queries.
         *
         * @param query Query operation description to be added to the list
         * @return Builder
         */
        public Builder query(Query query) {
            this.queries.add(query);
            return this;
        }

        /**
         * Sets the sub-resources for this resource.
         *
         * @param subresources The sub-reosurces definition.
         * @return Builder
         */
        public Builder subresources(SubResources subresources) {
            this.subresources = subresources;
            return this;
        }

        /**
         * Allocates the operations given in the parameter by their type.
         *
         * @param operations One or more Operations
         * @return Builder
         */
        public Builder operations(Operation... operations) {
            Reject.ifNull(operations);
            for (Operation operation : operations) {
                operation.allocateToResource(this);
            }
            return this;
        }

        /**
         * Setter for MVCC-supported flag.
         *
         * @param mvccSupported Whether this resource supports MVCC
         * @return Builder
         */
        public Builder mvccSupported(boolean mvccSupported) {
            this.mvccSupported = mvccSupported;
            return this;
        }

        /**
         * Adds items Resource.
         *
         * @param items The Resource definition of the collection items
         * @return Builder
         */
        public Builder items(Resource items) {
            this.items = items;
            return this;
        }

        /**
         * Construct a new instance of Resource.
         *
         * @return Resource instance
         */
        public Resource build() {
            if (create == null && read == null && update == null && delete == null && patch == null
                    && actions.isEmpty() && queries.isEmpty() && reference == null) {
                return null;
            }

            return new Resource(this);
        }

    }
}
