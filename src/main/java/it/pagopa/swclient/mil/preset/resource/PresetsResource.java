/**
 * 
 */
package it.pagopa.swclient.mil.preset.resource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.bean.Errors;
import it.pagopa.swclient.mil.preset.ErrorCode;
import it.pagopa.swclient.mil.preset.OperationType;
import it.pagopa.swclient.mil.preset.PresetStatus;
import it.pagopa.swclient.mil.preset.bean.PresetHeaders;
import it.pagopa.swclient.mil.preset.bean.PresetRequest;
import it.pagopa.swclient.mil.preset.bean.PresetResponse;
import it.pagopa.swclient.mil.preset.bean.PresetsPathParam;
import it.pagopa.swclient.mil.preset.bean.PresetsResponse;
import it.pagopa.swclient.mil.preset.dao.PresetRepository;
import it.pagopa.swclient.mil.preset.dao.PresetsEntity;
import it.pagopa.swclient.mil.preset.dao.SubscriberEntity;
import it.pagopa.swclient.mil.preset.dao.SubscriberRepository;
import it.pagopa.swclient.mil.preset.utils.DateUtils;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/presets")
public class PresetsResource {
	
	@Inject
	private PresetRepository presetRepository;
	
	@Inject 
	private SubscriberRepository subscriberRepository;

	/**
	 * Creates Preset Operation
	 * @param headers a set of mandatory headers
	 * @param presetRequest {@link PresetRequest} containing the request parameters
	 * @return HttpStatus CREATED. Preset operation created successfully
	 */
	@POST
	public Uni<Response> createPreset(@Valid @BeanParam PresetHeaders headers, @Valid PresetRequest presetRequest) {
		Log.debugf("createPreset - Input parameters: %s, request: %s", headers, presetRequest);
		return findSubscriberByTaxCodeAndSubscriberId(presetRequest.getPaTaxCode(), presetRequest.getSubscriberId())
				.onFailure().transform(f -> f)
				.chain(entity -> {
					if (entity == null) {
						Log.errorf("[%s] No subscriber found",  ErrorCode.ERROR_SUBSCRIBER_NOT_FOUND);
						return Uni.createFrom().item(
								Response.status(Status.BAD_REQUEST)
								.entity(new Errors(List.of(ErrorCode.ERROR_SUBSCRIBER_NOT_FOUND)))
								.build());
					} else {
						Log.debugf("Subscriber found. Update the Last Usage Timestamp");
						entity.setLastUsageTimestamp(DateUtils.getAndFormatCurrentDate());
						return updateSubscriber(entity).chain(m -> {
							Log.debugf("Generating new UUID ... ");
							final String uuid = UUID.randomUUID().toString();
							Log.debugf("UUID %s generated ", uuid);
							return persist(entity, presetRequest, uuid);
						});
						
					}
				});
	}
	
	/**
	 * Returns preset Operations for a specific subscriber
	 * @param headers a set of mandatory headers
	 * @param pathParams {@link PresetsPathParam} contains the path parameters: paTaxCode and subscriberId
	 * @return the presets operation for a specific subscriber
	 */
	@GET
	@Path("/{paTaxCode}/{subscriberId}")
	public Uni<Response> getPresetsOperations(@Valid @BeanParam PresetHeaders headers, @Valid PresetsPathParam pathParams) {
		Log.debugf("getPresetsOperations - Input parameters: %s, request: %s", headers, pathParams);
		return findPresetsOperationByTaxCodeAndSubscriberId(pathParams.getPaTaxCode(), pathParams.getSubscriberId())
				.onFailure().transform(f -> f)
				.map(m -> {
					Log.debugf("getPresets Response %s",m);
					return Response.status(Status.OK).entity(m).build();
				});
	}
	
	/**
	 * Return last preset operation to execute
	 * @param headers a set of mandatory headers
	 * @param pathParams pathParams {@link PresetsPathParam} contains the path parameters: paTaxCode and subscriberId
	 * @return Return last preset operation to execute for a specific subscriber
	 */
	@GET
	@Path("/{paTaxCode}/{subscriberId}/last_to_execute")
	public Uni<Response> getLastPresetsOperation(@Valid @BeanParam CommonHeader headers, @Valid PresetsPathParam pathParams) {
		Log.debugf("getLastPresetsOperation - Input parameters: %s, request: %s", headers, pathParams);
		return findPresetsOperationByTaxCodeAndSubscriberIdSortByCreationDate(pathParams.getPaTaxCode(), pathParams.getSubscriberId())
				.onFailure().transform(f -> f)
				.chain(m -> {
					if (m == null) {
						Log.errorf("[%s] No subscriber found",  ErrorCode.ERROR_PRESET_OPERATION_NOT_FOUND);
						return Uni.createFrom().item(
								Response.status(Status.NOT_FOUND)
								.entity(new Errors(List.of(ErrorCode. ERROR_PRESET_OPERATION_NOT_FOUND)))
								.build());
					} else {
						Log.debugf("getPresets Response %s",m);
						return Uni.createFrom().item(Response.status(Status.OK).entity(m).build());
					}
				});
	}
	
	/**
	 * searches the subscriber by paTaxCode and Subscriber Id
	 * @param paTaxCode Tax code of the creditor company
	 * @param subscriberId subscriber ID
	 * @return
	 */
	private Uni<SubscriberEntity> findSubscriberByTaxCodeAndSubscriberId(String paTaxCode, String subscriberId) {
		Log.debugf("find By paTaxCode %s And SubscriberId %s",paTaxCode,subscriberId);
		 return subscriberRepository.list("paTaxCode = :paTaxCode and subscriberId = :subscriberId", 
											 Parameters.with("paTaxCode", paTaxCode).and("subscriberId", subscriberId).map())
					.onFailure().transform(err -> {
						Log.errorf(err, "[%s] Error while find subscriber",  ErrorCode.ERROR_COMMUNICATION_MONGO_DB);
						return new InternalServerErrorException(Response
								.status(Status.INTERNAL_SERVER_ERROR)
								.entity(new Errors(List.of(ErrorCode.ERROR_COMMUNICATION_MONGO_DB)))
								.build());
						}
					).map(entity ->  !entity.isEmpty() ? entity.get(0) : null);
	}
	
	/**
	 * searches the last preset operation by paTaxCode and subscriber ID
	 * @param paTaxCode Tax code of the creditor company
	 * @param subscriberId ID assigned to subscribed terminal
	 * @return the last preset operations
	 */
	private Uni<PresetResponse> findPresetsOperationByTaxCodeAndSubscriberIdSortByCreationDate(String paTaxCode, String subscriberId) {
		Log.debugf("find last preset Operation By paTaxCode %s And SubscriberId %s",paTaxCode,subscriberId);
		 return presetRepository.list("paTaxCode = :paTaxCode and subscriberId = :subscriberId", 
						 Sort.by("creationTimestamp").descending(),
										 Parameters.with("paTaxCode", paTaxCode).and("subscriberId", subscriberId).map()
										 )
					.onFailure().transform(err -> {
						Log.errorf(err, "[%s] Error while find subscriber",  ErrorCode.ERROR_COMMUNICATION_MONGO_DB);
						return new InternalServerErrorException(Response
								.status(Status.INTERNAL_SERVER_ERROR)
								.entity(new Errors(List.of(ErrorCode.ERROR_COMMUNICATION_MONGO_DB)))
								.build());
						}
					).map(m -> 
						!m.isEmpty() ? mapPresetOperation(m.get(0)) : null
					);
	}
	
	/**
	 * searches the last preset operation by paTaxCode and subscriber ID
	 * @param paTaxCode Tax code of the creditor company
	 * @param subscriberId ID assigned to subscribed terminal
	 * @return the last preset operations
	 */
	private Uni<PresetsResponse> findPresetsOperationByTaxCodeAndSubscriberId(String paTaxCode, String subscriberId) {
		Log.debugf("find presets Operation By paTaxCode %s And SubscriberId %s",paTaxCode,subscriberId);
		 return presetRepository.list("paTaxCode = :paTaxCode and subscriberId = :subscriberId", 
										 Parameters.with("paTaxCode", paTaxCode).and("subscriberId", subscriberId).map())
					.onFailure().transform(err -> {
						Log.errorf(err, "[%s] Error while find subscriber",  ErrorCode.ERROR_COMMUNICATION_MONGO_DB);
						return new InternalServerErrorException(Response
								.status(Status.INTERNAL_SERVER_ERROR)
								.entity(new Errors(List.of(ErrorCode.ERROR_COMMUNICATION_MONGO_DB)))
								.build());
						}
					).map(this::mapPresetsOperationResponse);
	}
	/**
	 * Maps the list of {@link PresetsEntity} to {@link PresetsResponse}
	 * @param listOfPresetsEntity
	 * @return {@link PresetsResponse}
	 */
	private PresetsResponse mapPresetsOperationResponse(List<PresetsEntity> listOfPresetsEntity) {
		PresetsResponse presetsResponse = new PresetsResponse();
		List<PresetResponse> presets	= new ArrayList<>();
		listOfPresetsEntity.forEach(element -> {
			presets.add(mapPresetOperation(element));
		});
		presetsResponse.setPresets(presets);
		return presetsResponse;
	}
	
	/**
	 * Maps the {@link PresetsEntity} to {@link PresetResponse}
	 * @param presetsEntity
	 * @return
	 */
	private PresetResponse mapPresetOperation(PresetsEntity presetsEntity) {
		
		PresetResponse response = new PresetResponse();
		response.setCreationTimestamp(presetsEntity.getCreationTimestamp());
		response.setNoticeNumber(presetsEntity.getNoticeNumber());
		response.setNoticeTaxCode(presetsEntity.getNoticeTaxCode());
		response.setOperationType(presetsEntity.getOperationType());
		response.setPaTaxCode(presetsEntity.getPaTaxCode());
		response.setPresetId(presetsEntity.getPresetId());
		response.setStatus(presetsEntity.getStatus());
		response.setStatusTimestamp(presetsEntity.getStatusTimestamp());
		response.setSubscriberId(presetsEntity.getSubscriberId());
		response.setStatusDetails(presetsEntity.getStatusDetails());
		return response;
	}
	
	/**
	 * Update the subscriber 
	 * @param entity
	 * @return
	 */
	private Uni<SubscriberEntity> updateSubscriber(SubscriberEntity entity) {
		Log.debugf("Updating Subscriber");
		return subscriberRepository.update(entity)
		.onFailure().retry().withBackOff(Duration.ofSeconds(30), Duration.ofSeconds(30)).atMost(2)
        .map(m -> m);
	}
	
	/**
	 * Save a preset operation
	 * @param subscriberEntity entity describing the subscriber
	 * @param presetRequest request of the preset operation
	 * @param presetId the generated preset ID
	 * @return
	 */
	private Uni<Response> persist(  SubscriberEntity subscriberEntity,
									PresetRequest presetRequest,
									String presetId ) {
		PresetsEntity entity = buildPresetEntity(subscriberEntity, presetRequest, presetId);
		Log.debugf("Peristing Preset");
		return  presetRepository.persist(entity)
			.onFailure().transform( f -> {
			Log.errorf(f, "[%s] Error while storing preset %s into db",
			           ErrorCode.ERROR_STORING_TERMINAL_IN_DB, presetId);
			   return 
			   		new InternalServerErrorException(
			   				Response.status(Status.INTERNAL_SERVER_ERROR)
			           .entity(new Errors(List.of(ErrorCode.ERROR_STORING_TERMINAL_IN_DB)))
			           .build());
			})
			.map(m -> {
				final String location = buildLocationPath(subscriberEntity.getPaTaxCode(), subscriberEntity.getSubscriberId(), presetId);
				Log.debugf("PresetId %s SAVED ", presetId);
				return Response.status(Status.CREATED).header(SubscribeResource.LOCATION, location).build();
			});
	}
	
	/**
	 * Build the presetEntity object used to persist the preset operation
	 * @param subscriberEntity entity describing the subscriber
	 * @param presetRequest request of the preset operation
	 * @param presetId the generated preset ID
	 * @return a preset entity
	 */
	private PresetsEntity buildPresetEntity(SubscriberEntity subscriberEntity,
										 	PresetRequest presetRequest,
										 	String presetId) {

		final String timestamp = DateUtils.getAndFormatCurrentDate();
		PresetsEntity presetEntity = new PresetsEntity();
		presetEntity.setId(presetId);
		presetEntity.setPresetId(presetId);
		presetEntity.setCreationTimestamp(timestamp);
		presetEntity.setNoticeNumber(presetRequest.getNoticeNumber());
		presetEntity.setNoticeTaxCode(presetRequest.getNoticeTaxCode());
		presetEntity.setOperationType(OperationType.PAYMENT_NOTICE.name());
		presetEntity.setPaTaxCode(subscriberEntity.getPaTaxCode());
		presetEntity.setStatus(PresetStatus.TO_EXECUTE.name());
		presetEntity.setStatusTimestamp(timestamp);
		presetEntity.setSubscriberId(subscriberEntity.getSubscriberId());
		return presetEntity;
	}
	
	/**
	 * Utility method used to build the location path
	 * @param paTaxCode  Tax code of the creditor company
	 * @param subscriberId Subscriber ID
	 * @param presetId the generated preset ID
	 * @return
	 */
	private String buildLocationPath(String paTaxCode, String subscriberId, String presetId) {
		final StringBuilder location = new StringBuilder();
		location.append("/presets/");
		location.append(paTaxCode);
		location.append("/");
		location.append(subscriberId);
		location.append("/");
		location.append(presetId);
		return location.toString();
	}
}
