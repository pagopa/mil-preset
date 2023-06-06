package it.pagopa.swclient.mil.preset.resource;

import java.net.URI;
import java.security.SecureRandom;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.bean.Errors;
import it.pagopa.swclient.mil.preset.ErrorCode;
import it.pagopa.swclient.mil.preset.bean.GetSubscribersResponse;
import it.pagopa.swclient.mil.preset.bean.InstitutionPortalHeaders;
import it.pagopa.swclient.mil.preset.bean.SubscribeRequest;
import it.pagopa.swclient.mil.preset.bean.Subscriber;
import it.pagopa.swclient.mil.preset.bean.SubscriberPathParams;
import it.pagopa.swclient.mil.preset.bean.UnsubscribeHeaders;
import it.pagopa.swclient.mil.preset.dao.SubscriberEntity;
import it.pagopa.swclient.mil.preset.dao.SubscriberRepository;
import it.pagopa.swclient.mil.preset.utils.DateUtils;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.SecurityContext;


@Path("/terminals")
public class TerminalsResource {

	@Inject
	SubscriberRepository subscriberRepository;

	/**
	 * The base URL for the location header returned by the subscribe API (i.e. the API management base URL)
	 */
	@ConfigProperty(name="preset.location.base-url")
	String presetLocationBaseURL;
	
	/**
	 * Returns the list of subscribed terminals for a given tax code
	 * @param portalHeaders a set of mandatory headers sent by the institution portal
	 * @param paTaxCode the tax code of the creditor company
	 * @return the list of subscribed terminals
	 */
	@GET
	@Path("/{paTaxCode}")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> getSubscribers(@Valid @BeanParam InstitutionPortalHeaders portalHeaders,

										@PathParam(value = "paTaxCode")
										@NotNull(message = "[" + ErrorCode.PA_TAX_CODE_MUST_NOT_BE_NULL + "] paTaxCode must not be null")
										@Pattern(regexp = "^[0-9]{11}$", message = "[" + ErrorCode.PA_TAX_CODE_MUST_MATCH_REGEXP + "] paTaxCode must match \"{regexp}\"")
										String paTaxCode,

										@Context SecurityContext ctx) {

		Log.debugf("getSubscribers - Input parameters: %s, taxCode: %s, %s", portalHeaders, paTaxCode, ctx);
		
		return subscriberRepository.list("subscriber.paTaxCode", paTaxCode)
				.onFailure().transform(err -> {
					Log.errorf(err, "[%s] Error while retrieving data from DB", ErrorCode.ERROR_READING_DATA_FROM_DB);
					return new InternalServerErrorException(Response
							.status(Status.INTERNAL_SERVER_ERROR)
							.entity(new Errors(List.of(ErrorCode.ERROR_READING_DATA_FROM_DB)))
							.build());
				})
				.map(subs -> {
					GetSubscribersResponse response = new GetSubscribersResponse();
					response.setSubscribers(subs.stream().map(entity -> entity.subscriber).toList());
					Log.debugf("getSubscribers - Response %s", response);
					return Response
							.status(Status.OK)
							.entity(response)
							.build();
				});
		
	}
	
	/**
	 * Unsubscribes a terminal to handle preset operations
	 * @param headers a set of mandatory headers sent by the terminal or by the institution portal
	 * @param pathParams paTaxCode and subscriberId
	 * @return no content, if the terminal was successfully unsubscribed, or 404 if no subscriber was found with the given id
	 */
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path(value = "/{paTaxCode}/{subscriberId}")
	public Uni<Response> unsubscribe(@Valid @BeanParam UnsubscribeHeaders headers, SubscriberPathParams pathParams) {

		Log.debugf("unsubscribe - Input parameters: %s, %s", headers, pathParams);

		return subscriberRepository.delete("subscriber.paTaxCode = :paTaxCode and subscriber.subscriberId = :subscriberId",
						Parameters
								.with("paTaxCode", pathParams.getPaTaxCode())
								.and("subscriberId", pathParams.getSubscriberId())
								.map()
				)
				.onFailure().transform(err -> {
					Log.debugf("[%s] Error while deleting data from DB", ErrorCode.ERROR_WRITING_DATA_IN_DB);
					return new InternalServerErrorException(Response
							.status(Status.INTERNAL_SERVER_ERROR)
							.entity(new Errors(List.of(ErrorCode.ERROR_WRITING_DATA_IN_DB)))
							.build());
				})
				.map(deleted -> {
					Status status;
					if (deleted > 0) {
						status = Status.NO_CONTENT;
					} else {
						Log.warnf("No entity found for paTaxCode %s and subscriberId %s",
								pathParams.getPaTaxCode(), pathParams.getSubscriberId());
						status = Status.NOT_FOUND;
					}
					Log.debugf("unsubscribe - Response %s", status);
					return Response.status(status).build();
				});
	}
	
	/**
	 * Subscribes a terminal to handle preset operations
	 *
	 * @param commonHeader a set of mandatory headers sent by the terminal
	 * @param subscribeRequest the request containing the pa tax code and a mnemonic label
	 * @return 201 if the terminal was subscribed
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> subscribe(@Valid @BeanParam CommonHeader commonHeader,

								   @Valid
								   @NotNull(message = "[" + ErrorCode.SUBSCRIBE_REQUEST_MUST_NOT_BE_EMPTY + "] request must not be empty")
								   SubscribeRequest subscribeRequest) {

		Log.debugf("subscribe - Input parameters: %s, %s", commonHeader, subscribeRequest);

		return findSubscriber(commonHeader, subscribeRequest.getPaTaxCode())
				.chain(subscriber -> {
					if (subscriber == null) {
						// no subscriber found, generating new subscriber id
						final String subscriberId = RandomStringUtils.random(6, 0, 0, true, true, null, new SecureRandom()).toLowerCase();
						Log.debugf("Generated subscriber id %s", subscriberId);

						SubscriberEntity entity = buildSubscriberEntity(subscribeRequest, commonHeader, subscriberId);
						return subscriberRepository.persist(entity)
								.onFailure().transform( f -> {
									Log.errorf(f, "[%s] Error while storing data in the DB", ErrorCode.ERROR_WRITING_DATA_IN_DB, subscriberId);
									return new InternalServerErrorException(Response.status(Status.INTERNAL_SERVER_ERROR)
											.entity(new Errors(List.of(ErrorCode.ERROR_WRITING_DATA_IN_DB)))
											.build());
								})
								.map(m -> {
									final URI locationURI = buildLocationPath(subscribeRequest.getPaTaxCode(), subscriberId);
									Log.debugf("SubscriberId %s SAVED ", subscriberId);
									return Response.status(Status.CREATED).location(locationURI).build();
								});
					}
					else {
						final URI locationURI = buildLocationPath(subscribeRequest.getPaTaxCode(), subscriber.getSubscriberId());
						Log.debugf("Terminal already subscribed", ErrorCode.SUBSCRIBER_ALREADY_EXISTS, subscriber.getSubscriberId());
						return Uni.createFrom().item(Response.status(Status.CONFLICT).location(locationURI).build());
						}	
					});
	}

	/**
	 * Searches for a subscribed terminal by its data and the tax code of the company
	 *
	 * @param commonHeader a set of mandatory headers sent by the terminal
	 * @param paTaxCode tax code of the creditor company
	 * @return the subscriber if found, null otherwise
	 */
	private Uni<Subscriber> findSubscriber(CommonHeader commonHeader, String paTaxCode) {

		return subscriberRepository.list(
						"""
								subscriber.acquirerId = :acquirerId and
								subscriber.channel = :channel and
								subscriber.merchantId =:merchantId and
								subscriber.terminalId =:terminalId and
								subscriber.paTaxCode =:paTaxCode
								""",
						Parameters.with("acquirerId", commonHeader.getAcquirerId())
								.and("channel", commonHeader.getChannel())
								.and("merchantId", commonHeader.getMerchantId())
								.and("terminalId", commonHeader.getTerminalId())
								.and("paTaxCode", paTaxCode)
								.map())
				.onFailure().transform(err -> {
							Log.errorf(err, "[%s] Error while retrieving data from DB", ErrorCode.ERROR_READING_DATA_FROM_DB);
							return new InternalServerErrorException(Response
									.status(Status.INTERNAL_SERVER_ERROR)
									.entity(new Errors(List.of(ErrorCode.ERROR_READING_DATA_FROM_DB)))
									.build());
						}
				)
				.map(entityList -> entityList.isEmpty() ? null : entityList.get(0).subscriber);
	}

	/**
	 * Maps the input parameters to the {@link SubscriberEntity}, used to persist the information into the database
	 *
	 * @param subscribeRequest {@link SubscribeRequest} contains Tax code of the creditor company and a mnemonic terminal label
	 * @param commonHeader a set of mandatory headers
	 * @param subscriberId the id of the subscriber
	 * @return the entity to persist
	 */
	private SubscriberEntity buildSubscriberEntity(SubscribeRequest subscribeRequest,
												   CommonHeader commonHeader,
												   String subscriberId) {

		Subscriber subscriber = new Subscriber();
		subscriber.setAcquirerId(commonHeader.getAcquirerId());
		subscriber.setChannel(commonHeader.getChannel());
		subscriber.setLabel(subscribeRequest.getLabel());
		subscriber.setLastUsageTimestamp(null);
		subscriber.setMerchantId(commonHeader.getMerchantId());
		subscriber.setPaTaxCode(subscribeRequest.getPaTaxCode());
		subscriber.setSubscriberId(subscriberId);
		subscriber.setSubscriptionTimestamp(DateUtils.getCurrentTimestamp());
		subscriber.setTerminalId(commonHeader.getTerminalId());

		SubscriberEntity subscriberEntity = new SubscriberEntity();
		subscriberEntity.id = subscriberId;
		subscriberEntity.subscriber = subscriber;

		return subscriberEntity;
	}
	
	/**
	 * Utility method used to build the location path
	 * @param paTaxCode Tax code of the creditor company
	 * @param subscriberId subscriber id
	 * @return the location path as string
	 */
	private URI buildLocationPath(String paTaxCode, String subscriberId) {
		final StringBuilder location = new StringBuilder();
		location.append("/terminals/");
		location.append(paTaxCode);
		location.append("/");
		location.append(subscriberId);
		return URI.create(presetLocationBaseURL + location.toString());
	}


}