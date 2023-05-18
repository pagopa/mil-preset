/**
 * This module contains the REST endpoints exposed by the microservice.
 * 
 * @author Antonio Tarricone
 */
package it.pagopa.swclient.mil.preset.resource;

import java.security.SecureRandom;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.bean.Errors;
import it.pagopa.swclient.mil.preset.ErrorCode;
import it.pagopa.swclient.mil.preset.bean.PresetHeaders;
import it.pagopa.swclient.mil.preset.bean.SubcribersResponse;
import it.pagopa.swclient.mil.preset.bean.SubscriberRequest;
import it.pagopa.swclient.mil.preset.bean.SubscriberResponse;
import it.pagopa.swclient.mil.preset.bean.SubscribersPathParam;
import it.pagopa.swclient.mil.preset.bean.UnsubscriberHeaders;
import it.pagopa.swclient.mil.preset.bean.UnsubscriberPathParam;
import it.pagopa.swclient.mil.preset.dao.SubscriberEntity;
import it.pagopa.swclient.mil.preset.dao.SubscriberRepository;
import it.pagopa.swclient.mil.preset.utils.DateUtils;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;


@Path("/terminals")
public class SubscribeResource {
	
	public static final String LOCATION = "Location";
	
	@Inject
	private SubscriberRepository subscriberRepository;
	
	/**
	 * Returns the list of subscribed terminals
	 * @param headers a set of mandatory headers
	 * @param pathParams paTaxCode of the creditor company
	 * @return List of subscribed terminals
	 */
	@GET
	@Path("/{paTaxCode}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Uni<Response> getSubscribers(@Valid @BeanParam PresetHeaders headers,@Valid SubscribersPathParam pathParams) {
		Log.debugf("getSubscribers - Input parameters: %s, taxCode: %s", headers, pathParams.getPaTaxCode());
		
		return findSubscribersByPaTaxCode(pathParams.getPaTaxCode())
				.onFailure().transform(f -> f)
				.map(m -> {
					Log.debugf("getSubscribers Response %s",m);
					return Response.status(Status.OK).entity(m).build();
				});
		
	}
	
	/**
	 * Unsubscribes a terminal to handle preset operations
	 * @param headers a set of mandatory headers
	 * @param pathParams paTaxCode and subscriberId
	 * @return
	 */
	@DELETE
	@Path(value = "/{paTaxCode}/{subscriberId}")
	public Uni<Response> unsubscriber(@Valid @BeanParam UnsubscriberHeaders headers, UnsubscriberPathParam pathParams) {
		return deleteByPaTaxCodeAndSubscriberId(pathParams.getPaTaxCode(),pathParams.getSubscriberId())
				.map(numberOfEntityDeleted -> {
					Log.debugf("unbscriber Response - Deleted [%s] entity",numberOfEntityDeleted);
					if (numberOfEntityDeleted > 0) {
						return Response.status(Status.NO_CONTENT).build();
					} else {
						return Response.status(Status.NOT_FOUND).build();
					}
				});
	}
	
	/**
	 * Subscribes a terminal to handle preset operations
	 * @param commonHeader a set of mandatory headers
	 * @param subscriberRequest {@link SubscriberRequest} contains Tax code of the creditor company and a mnemonic terminal label 
	 * @return status of operation
	 */
	@POST
	public Uni<Response> subscribe(@Valid @BeanParam CommonHeader commonHeader, @Valid SubscriberRequest subscriberRequest) {
		Log.debugf("subscribe - Input parameters: %s, subscriberRequest: %s", commonHeader, subscriberRequest);
		return findSubscriber(commonHeader.getAcquirerId(),
								commonHeader.getChannel(),
								commonHeader.getMerchantId(),
								commonHeader.getTerminalId(),
								subscriberRequest.getPaTaxCode())
				.chain(subscriberId -> {
					if (subscriberId.equals("")) {
						Log.debugf("No SubscriberId found ");
						Log.debugf("Generating new SubscriberId ... ");
						final String subId = RandomStringUtils.random(6, 0, 0, true, true, null, new SecureRandom()).toLowerCase();
						Log.debugf("SubscriberId %s generated ", subId);
						return persist(subscriberRequest, commonHeader, subId);
					} else {
						final String location = buildLocationPath(subscriberRequest.getPaTaxCode(), subscriberId);
						Log.debugf("[%s] Error while storing terminal %s into db", ErrorCode.ERROR_CONFLICT_TERMINAL_IN_DB, subscriberId);

						return Uni.createFrom().item(
								Response.status(Status.CONFLICT)
								.entity(new Errors(List.of(ErrorCode.ERROR_CONFLICT_TERMINAL_IN_DB)))
								.header(LOCATION, location).build());
						}	
					});
	}
	
	/**
	 * Perform the persist operation on the database
	 * @param subscriberRequest {@link SubscriberRequest} contains Tax code of the creditor company and a mnemonic terminal label 
	 * @param commonHeader a set of mandatory headers
	 * @param subId subscriber Id
	 * @return the status of the persist
	 */
	private Uni<Response> persist( SubscriberRequest subscriberRequest,
								 CommonHeader commonHeader,
								 String subId ) {
		SubscriberEntity entity = buildEntity(subscriberRequest, commonHeader, subId);
		return  subscriberRepository.persist(entity)
				.onFailure().transform( f -> {
					 Log.errorf(f, "[%s] Error while storing terminal %s into db",
		                        ErrorCode.ERROR_STORING_TERMINAL_IN_DB, subId);
		                return 
		                		new InternalServerErrorException(
		                				Response.status(Status.INTERNAL_SERVER_ERROR)
		                        .entity(new Errors(List.of(ErrorCode.ERROR_STORING_TERMINAL_IN_DB)))
		                        .build());
				})
				.map(m -> {
					final String location = buildLocationPath(subscriberRequest.getPaTaxCode(), subId);
					Log.debugf("SubscriberId %s SAVED ", subId);
					return Response.status(Status.CREATED).header(LOCATION, location).build();
				});
	}
	
	/**
	 * Retrieves the list of subscribers by paTaxCode 
	 * @param taxCodeToken token returned by the PDV-Tokenizer service
	 * @param tcVersion T&C version
	 * @return  true if the current version is equals to older one. False otherwise.
	 */
	private Uni<SubcribersResponse> findSubscribersByPaTaxCode(String paTaxCode) {
		Log.debugf("findSubscribersByPaTaxCode - find Subscribers by paTaxCode: [%s] ", paTaxCode);
		
		return subscriberRepository.list("paTaxCode", paTaxCode)
				.onFailure().transform(err -> {
					 Log.errorf(err, "[%s] Error while find subscriber",  ErrorCode.ERROR_COMMUNICATION_MONGO_DB);
					return new InternalServerErrorException(Response
							.status(Status.INTERNAL_SERVER_ERROR)
							.entity(new Errors(List.of(ErrorCode.ERROR_COMMUNICATION_MONGO_DB)))
							.build());
					}
				).map(entity -> {
					SubcribersResponse subscribers = new SubcribersResponse();
					entity.forEach(element -> mapResponse(element, subscribers));
					return subscribers;
				});
	}
	

	/**
	 * Make a query to check if the terminal is already subscribed
	 * @param acquirerId Acquirer ID assigned by PagoPA
	 * @param channel Channel originating the request
	 * @param merchantId Merchant ID. Mandatory when Channel equals POS.
	 * @param terminalId ID of the terminal originating the transaction. It must be unique per acquirer and channel.
	 * @param paTaxCode Tax code of the creditor company
	 * @return 
	 */
	private Uni<String> findSubscriber(String acquirerId,
										String channel,
										String merchantId,
										String terminalId,
										String paTaxCode
			) {
		Log.debugf("findSubscriber - find Subscribers by acquirerId [%s] channel [%s] merchantId [%s] terminalid [%s] paTaxCode: [%s] ", acquirerId, channel, merchantId, terminalId, paTaxCode);
		
		return subscriberRepository.list("acquirerId = :acquirerId and channel = :channel and merchantId =:merchantId and terminalId =:terminalId and paTaxCode =:paTaxCode", 
									 Parameters.with("acquirerId", acquirerId)
									 		    .and("channel", channel)
									 		    .and("merchantId", merchantId)
									 		    .and("terminalId", terminalId)
									 		    .and("paTaxCode", paTaxCode)
									 		    .map())
				.onFailure().transform(err -> {
					Log.errorf(err, "[%s] Error while list subscribers",  ErrorCode.ERROR_COMMUNICATION_MONGO_DB);
					return new InternalServerErrorException(Response
							.status(Status.INTERNAL_SERVER_ERROR)
							.entity(new Errors(List.of(ErrorCode.ERROR_COMMUNICATION_MONGO_DB)))
							.build());
					}
				).map(entity -> 	
						 !entity.isEmpty() ? entity.get(0).getSubscriberId() : ""
					);
	}
	
	/**
	 * Delete the subsctiber by paTaxCode and Subscriber Id
	 * @param paTaxCode Tax code of the creditor company
	 * @param subscriberId subscriber Id
	 * @return
	 */
	private Uni<Long> deleteByPaTaxCodeAndSubscriberId(String paTaxCode, String subscriberId) {
		Log.debugf("Deleting entity with paTaxCode [%s] and subscriberId [%s]", paTaxCode, subscriberId);

		return subscriberRepository.delete("paTaxCode = :paTaxCode and subscriberId = :subscriberId", 
										Parameters.with("paTaxCode", paTaxCode).and("subscriberId", subscriberId).map()
									  )
				.onFailure().transform(err -> {
					Log.debugf("Internal server error deleting entity with paTaxCode [%s] and subscriberId [%s]", paTaxCode, subscriberId);
					return new InternalServerErrorException(Response
							.status(Status.INTERNAL_SERVER_ERROR)
							.entity(new Errors(List.of(ErrorCode.ERROR_COMMUNICATION_MONGO_DB)))
							.build());
				}).map(f -> f);
	}
	
	/**
	 * Maps the Entity to the  SubcribersResponse
	 * @param entity entity retrieved form database
	 * @param subscribersResponse response containing the subscriber info
	 */
	private void mapResponse(SubscriberEntity entity, SubcribersResponse subscribersResponse) {
		SubscriberResponse subscriberResponse = new SubscriberResponse();
		subscriberResponse.setAcquirerId(entity.getAcquirerId());
		subscriberResponse.setChannel(entity.getChannel());
		subscriberResponse.setLabel(entity.getLabel());
		subscriberResponse.setLastUsageTimestamp(entity.getLastUsageTimestamp());
		subscriberResponse.setMerchantId(entity.getMerchantId());
		subscriberResponse.setPaTaxCode(entity.getPaTaxCode());
		subscriberResponse.setSubscriberId(entity.getSubscriberId());
		subscriberResponse.setSubscriptionTimestamp(entity.getSubscriptionTimestamp());
		subscriberResponse.setTerminalId(entity.getTerminalId());
		subscribersResponse.getSubscribers().add(subscriberResponse);
	}
	
	/**
	 * Maps the input parameters to the {@link SubscriberEntity}, used to persiste the information into the database
	 * @param subscriberRequest {@link SubscriberRequest} contains Tax code of the creditor company and a mnemonic terminal label
	 * @param commonHeader a set of mandatory headers 
	 * @param subId  subscriber Id
	 * @return the entity to persist
	 */
	private SubscriberEntity buildEntity(SubscriberRequest subscriberRequest,
									 CommonHeader commonHeader,
									 String subId) {
		
		final String timestamp = DateUtils.getAndFormatCurrentDate();
		SubscriberEntity entity = new SubscriberEntity();
		//set id as subscriber Id
		entity.setId(subId);
		entity.setAcquirerId(commonHeader.getAcquirerId());
		entity.setChannel(commonHeader.getChannel());
		entity.setLabel(subscriberRequest.getLabel());
		entity.setLastUsageTimestamp(timestamp);
		entity.setMerchantId(commonHeader.getMerchantId());
		entity.setPaTaxCode(subscriberRequest.getPaTaxCode());
		entity.setSubscriberId(subId);
		entity.setSubscriptionTimestamp(timestamp);
		entity.setTerminalId(commonHeader.getTerminalId());
		
		return entity;
	}
	
	/**
	 * Utility method used to build the location path
	 * @param paTaxCode Tax code of the creditor company
	 * @param subscriber subscriber Id
	 * @return the location path as string
	 */
	private String buildLocationPath(String paTaxCode, String subscriberId) {
		final StringBuilder location = new StringBuilder();
		location.append("/terminals/");
		location.append(paTaxCode);
		location.append("/");
		location.append(subscriberId);
		return location.toString();
	}
	
}