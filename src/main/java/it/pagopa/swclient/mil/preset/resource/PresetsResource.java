package it.pagopa.swclient.mil.preset.resource;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.bean.Errors;
import it.pagopa.swclient.mil.preset.ErrorCode;
import it.pagopa.swclient.mil.preset.OperationType;
import it.pagopa.swclient.mil.preset.PresetStatus;
import it.pagopa.swclient.mil.preset.bean.CreatePresetRequest;
import it.pagopa.swclient.mil.preset.bean.GetPresetsResponse;
import it.pagopa.swclient.mil.preset.bean.InstitutionPortalHeaders;
import it.pagopa.swclient.mil.preset.bean.PresetOperation;
import it.pagopa.swclient.mil.preset.bean.Subscriber;
import it.pagopa.swclient.mil.preset.bean.SubscriberPathParams;
import it.pagopa.swclient.mil.preset.dao.PresetEntity;
import it.pagopa.swclient.mil.preset.dao.PresetRepository;
import it.pagopa.swclient.mil.preset.dao.SubscriberEntity;
import it.pagopa.swclient.mil.preset.dao.SubscriberRepository;
import it.pagopa.swclient.mil.preset.utils.DateUtils;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/presets")
public class PresetsResource {

	public static final String SUBSCRIBER_FILTER = "subscriber.paTaxCode = :paTaxCode and subscriber.subscriberId = :subscriberId";
	public static final String PRESET_FILTER = "presetOperation.paTaxCode = :paTaxCode and presetOperation.subscriberId = :subscriberId";

    public static final String LAST_PRESET_FILTER = PRESET_FILTER + " and presetOperation.status = 'TO_EXECUTE'";

    /**
     * The base URL for the location header returned by the createPreset API (i.e. the API management base URL)
     */
    @ConfigProperty(name="preset.location.base-url")
    String presetLocationBaseURL;


	@Inject
    PresetRepository presetRepository;

    @Inject
    SubscriberRepository subscriberRepository;

    /**
     * Creates a preset operation
     *
     * @param portalHeaders a set of mandatory headers sent by the institution portal
     * @param createPresetRequest {@link CreatePresetRequest} containing the request parameters
     * @return an {@link Uni} emitting an empty 201 Created response with the Location header populated
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> createPreset(@Valid @BeanParam InstitutionPortalHeaders portalHeaders,

                                      @Valid
                                      @NotNull(message = "[" + ErrorCode.CREATE_PRESET_REQUEST_MUST_NOT_BE_EMPTY + "] request must not be empty")
                                      CreatePresetRequest createPresetRequest) {

        Log.debugf("createPreset - Input parameters: %s,: %s", portalHeaders, createPresetRequest);

        return findSubscriber(createPresetRequest.getPaTaxCode(), createPresetRequest.getSubscriberId())
                .chain(subscriberEntity -> {
                    if (subscriberEntity == null) {
                        Log.errorf("[%s] No subscriber found on DB", ErrorCode.SUBSCRIBER_NOT_FOUND);
                        return Uni.createFrom().item(
                                Response.status(Status.BAD_REQUEST)
                                        .entity(new Errors(List.of(ErrorCode.SUBSCRIBER_NOT_FOUND)))
                                        .build());
                    } else {
                        // update last usage timestamp
                        subscriberEntity.subscriber.setLastUsageTimestamp(DateUtils.getCurrentTimestamp());
                        return updateSubscriber(subscriberEntity)
                                // save preset operation on DB
                                .chain(se -> persistPreset(se.subscriber, createPresetRequest))
                                .map(pe -> {
                                    final URI locationURI = buildLocationPath(subscriberEntity.subscriber.getPaTaxCode(),
                                            subscriberEntity.subscriber.getSubscriberId(), pe.presetOperation.getPresetId());
                                    Log.debugf("createPreset - Response presetId %s ", pe.presetOperation.getPresetId());
                                    return Response.status(Status.CREATED).location(locationURI).build();
                                });
                    }
                });
    }

    /**
     * Returns the list of preset operations configured for a subscriber
     *
     * @param headers a set of mandatory headers sent by the institution portal
     * @param pathParams {@link SubscriberPathParams} contains the path parameters paTaxCode and subscriberId
     * @return an {@link Uni} emitting the list of {@link PresetOperation} configured for the subscriber
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{paTaxCode}/{subscriberId}")
    public Uni<Response> getPresets(@Valid @BeanParam InstitutionPortalHeaders headers, @Valid @BeanParam SubscriberPathParams pathParams) {

        Log.debugf("getPresets - Input parameters: %s, %s", headers, pathParams);

        return findPresetOperations(pathParams.getPaTaxCode(), pathParams.getSubscriberId())
                .map(presets -> {
                    GetPresetsResponse response = new GetPresetsResponse();
                    response.setPresets(presets);
                    Log.debugf("getPresets - Response %s", response);
                    return Response.status(Status.OK).entity(response).build();
                });
    }

    /**
     * Returns the last preset operation to be executed by a subscriber
     *
     * @param headers a set of mandatory headers
     * @param pathParams pathParams {@link SubscriberPathParams} contains the path parameters: paTaxCode and subscriberId
     * @return an {@link Uni} emitting the latest {@link PresetOperation} configured for the subscriber
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{paTaxCode}/{subscriberId}/last_to_execute")
    public Uni<Response> getLastPresetsOperation(@Valid @BeanParam CommonHeader headers, @Valid @BeanParam SubscriberPathParams pathParams) {

        Log.debugf("getLastPresetsOperation - Input parameters: %s, %s", headers, pathParams);

        return findLatestPresetOperation(pathParams.getPaTaxCode(), pathParams.getSubscriberId())
				.chain(m -> {
                    if (m == null) {
                        Log.debugf("getLastPresetsOperation - Response %s", Status.NOT_FOUND);
                        return Uni.createFrom().item(Response.status(Status.NOT_FOUND).build());
                    }
					else {
                        Log.debugf("getLastPresetsOperation - Response %s", m);
                        return Uni.createFrom().item(Response.status(Status.OK).entity(m).build());
                    }
                });
    }

    /**
     * Retrieves the subscriber by tax code and its id
     *
     * @param paTaxCode    Tax code of the creditor company
     * @param subscriberId the id of the subscriber
     * @return the subscriber entity
     */
    private Uni<SubscriberEntity> findSubscriber(String paTaxCode, String subscriberId) {

        return subscriberRepository.list(SUBSCRIBER_FILTER, Parameters.with("paTaxCode", paTaxCode).and("subscriberId", subscriberId).map())
                .onFailure().transform(err -> {
                    Log.errorf(err, "[%s] Error while retrieving data from DB", ErrorCode.ERROR_READING_DATA_FROM_DB);
                    return new InternalServerErrorException(
							Response.status(Status.INTERNAL_SERVER_ERROR)
									.entity(new Errors(List.of(ErrorCode.ERROR_READING_DATA_FROM_DB)))
									.build());
                })
                .map(entityList -> entityList.isEmpty() ? null : entityList.get(0));
    }

    /**
     * Retrieves all the preset operations configured for a subscriber
     *
     * @param paTaxCode    tax code of the creditor company
     * @param subscriberId ID assigned to subscribed terminal
     * @return the list of the preset operations
     */
    private Uni<List<PresetOperation>> findPresetOperations(String paTaxCode, String subscriberId) {

        return presetRepository.list(PRESET_FILTER, Parameters.with("paTaxCode", paTaxCode).and("subscriberId", subscriberId).map())
                .onFailure().transform(err -> {
                    Log.errorf(err, "[%s] Error while retrieving data from DB", ErrorCode.ERROR_READING_DATA_FROM_DB);
                    return new InternalServerErrorException(
							Response.status(Status.INTERNAL_SERVER_ERROR)
									.entity(new Errors(List.of(ErrorCode.ERROR_READING_DATA_FROM_DB)))
									.build());
                })
                .map(entities -> entities.stream().map(entity -> entity.presetOperation).toList());
    }

    /**
     * Retrieves the latest preset operation configured for a subscriber
     *
     * @param paTaxCode    Tax code of the creditor company
     * @param subscriberId ID assigned to subscribed terminal
     * @return the last preset operation
     */
    private Uni<PresetOperation> findLatestPresetOperation(String paTaxCode, String subscriberId) {

        return presetRepository.find(LAST_PRESET_FILTER,
                        Sort.by("presetOperation.creationTimestamp").descending(),
                        Parameters
                                .with("paTaxCode", paTaxCode)
                                .and("subscriberId", subscriberId)
                                .map()
						)
				.firstResult()
                .onFailure().transform(err -> {
                            Log.errorf(err, "[%s] Error while retrieving data from DB", ErrorCode.ERROR_READING_DATA_FROM_DB);
                            return new InternalServerErrorException(
									Response.status(Status.INTERNAL_SERVER_ERROR)
											.entity(new Errors(List.of(ErrorCode.ERROR_READING_DATA_FROM_DB)))
											.build());
                        }
                )
				.map(presetEntity -> presetEntity != null ? presetEntity.presetOperation : null);
    }


    /**
     * Update the subscriber info
     *
     * @param entity the subscriber entity to update
     * @return the updated entity
     */
    private Uni<SubscriberEntity> updateSubscriber(SubscriberEntity entity) {
        Log.debugf("Updating Subscriber");
        return subscriberRepository.update(entity)
                // ignoring update error
                .onFailure().recoverWithItem(entity);
    }

    /**
     * Save a preset operation on the database
     *
     * @param subscriber bean containing the subscriber data
     * @param createPresetRequest request of the create preset operation
     * @return an {@link Uni} emitting the persisted {@link PresetEntity}
     */
    private Uni<PresetEntity> persistPreset(Subscriber subscriber,
                                            CreatePresetRequest createPresetRequest) {

        final String presetId = UUID.randomUUID().toString();
        PresetEntity entity = buildPresetEntity(subscriber, createPresetRequest, presetId);

        return presetRepository.persist(entity)
                .onFailure().transform(f -> {
                    Log.errorf(f, "[%s] Error while storing data in the DB", ErrorCode.ERROR_WRITING_DATA_IN_DB);
                    return new InternalServerErrorException(
                            Response.status(Status.INTERNAL_SERVER_ERROR)
                                    .entity(new Errors(List.of(ErrorCode.ERROR_WRITING_DATA_IN_DB)))
                                    .build());
                });

    }

    /**
     * Build the preset entity object to be persisted the {@link #persistPreset(Subscriber, CreatePresetRequest)} operation
     *
     * @param subscriber bean containing the subscriber data
     * @param createPresetRequest request of the create preset operation
     * @param presetId the generated preset identifier
     * @return the {@link PresetEntity} instance
     */
    private PresetEntity buildPresetEntity(Subscriber subscriber,
                                           CreatePresetRequest createPresetRequest,
                                           String presetId) {

        final String timestamp = DateUtils.getCurrentTimestamp();
        PresetOperation presetOperation = new PresetOperation();
        presetOperation.setPresetId(presetId);
        presetOperation.setCreationTimestamp(timestamp);
        presetOperation.setNoticeNumber(createPresetRequest.getNoticeNumber());
        presetOperation.setNoticeTaxCode(createPresetRequest.getNoticeTaxCode());
        presetOperation.setOperationType(OperationType.PAYMENT_NOTICE.name());
        presetOperation.setPaTaxCode(subscriber.getPaTaxCode());
        presetOperation.setStatus(PresetStatus.TO_EXECUTE.name());
        presetOperation.setStatusTimestamp(timestamp);
        presetOperation.setSubscriberId(subscriber.getSubscriberId());

		PresetEntity presetEntity = new PresetEntity();
		presetEntity.id = presetId;
		presetEntity.presetOperation = presetOperation;

        return presetEntity;
    }

    /**
     * Utility method used to build the location header path to be returned in the
     * {@link #getLastPresetsOperation(CommonHeader, SubscriberPathParams)}
     *
     * @param paTaxCode tax code of the creditor company
     * @param subscriberId the subscriber id
     * @param presetId the generated preset id
     * @return an {@link URI} representing the location header
     */
    private URI buildLocationPath(String paTaxCode, String subscriberId, String presetId) {
        final StringBuilder location = new StringBuilder();
        location.append("/presets/");
        location.append(paTaxCode);
        location.append("/");
        location.append(subscriberId);
        location.append("/");
        location.append(presetId);
        return URI.create(presetLocationBaseURL + location.toString());
    }
}
