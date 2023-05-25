/**
 * 
 */
package it.pagopa.swclient.mil.preset.resource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.bean.Errors;
import it.pagopa.swclient.mil.preset.ErrorCode;
import it.pagopa.swclient.mil.preset.PresetStatus;
import it.pagopa.swclient.mil.preset.bean.Notice;
import it.pagopa.swclient.mil.preset.bean.PaymentTransaction;
import it.pagopa.swclient.mil.preset.bean.Preset;
import it.pagopa.swclient.mil.preset.dao.PresetRepository;
import it.pagopa.swclient.mil.preset.dao.PresetsEntity;
import it.pagopa.swclient.mil.preset.utils.DateUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@ApplicationScoped
@Path("/kafka") //TODO: remove only for test
public class PresetTopicResource {
	
	@Inject
	private PresetRepository presetRepository;
//	
//	@Inject
//    @Channel("presets")
//    Emitter<PaymentTransaction> testEmitter;
//
//    @POST
//    @Consumes(MediaType.TEXT_PLAIN)
//    public void addPrice() {
//    	PaymentTransaction transaction = new PaymentTransaction();
//    	transaction.setTransactionId("517a4216840E461fB011036A0fd134E1");
//    	transaction.setAcquirerId("4585625");
//    	transaction.setChannel("POS");
//    	transaction.setMerchantId("28405fHfk73x88D");
//    	transaction.setTerminalId("0aB9wXyZ");
//    	transaction.setInsertTimestamp("2023-04-11T16:20:34");
//    	
//    	Notice notice = new Notice();
//    	notice.setPaymentToken("648fhg36s95jfg7DS");
//    	notice.setPaTaxCode("15376371009");
//    	notice.setNoticeNumber("485564829563528563");
//    	notice.setAmount(Long.valueOf("12345"));
//    	notice.setDescription("Health ticket for chest x-ray");
//    	notice.setCompany("ASL Roma 2");
//    	notice.setOffice("Office RoMA");
//    	List<Notice> notices = new ArrayList<>();
//    	notices.add(notice);
//    	
//    	Preset preset = new Preset();
//    	preset.setPaTaxCode("15376371009");
//    	preset.setSubscriberId("csl0kq");
//    	preset.setPresetId("77457c64-0870-407a-b2cb-0f948b04fb9a");
//    	transaction.setPreset(preset);
//    	
//    	transaction.setNotices(notices);
//    	
//    	transaction.setTotalAmount(1000L);
//    	transaction.setFee(Long.valueOf("50"));
//    	transaction.setStatus("SSS");
//    	transaction.setPaymentMethod("CASH");
//    	transaction.setPaymentTimestamp("2023-05-21T09:29:34.526");
//    	transaction.setCloseTimestamp("2023-05-21T10:29:34.526");
//    	
//    	
//        CompletionStage<Void> ack = testEmitter.send(transaction);
//    }
	
    /**
     * Update the status and details of a preset operation with type: payment notice 
     * @param paymentTransaction {@link PaymentTransaction} the data of the payment transaction
     * @return
     */
    @Incoming("presets")
    public Uni<PresetsEntity> consume(PaymentTransaction paymentTransaction) {
//    	PaymentTransaction paymentTransaction = SerializationUtils.deserialize(data);
        Log.debugf("consume Message %s", paymentTransaction);
        return findPresetsOperationByPaTaxCodeSubscriberIdAndPresetId(paymentTransaction);
    }
    
    /**
     * Finds a preset operation by paTaxCode SubscriberId and PresetId
     * @param paymentTransaction {@link PaymentTransaction} the data of the payment transaction
     * @return
     */
    private Uni<PresetsEntity> findPresetsOperationByPaTaxCodeSubscriberIdAndPresetId(PaymentTransaction paymentTransaction) {
    	
   		final String paTaxCode		= paymentTransaction.getPreset().getPaTaxCode();
   		final String presetId 		= paymentTransaction.getPreset().getPresetId(); 
   		final String subscriberId 	= paymentTransaction.getPreset().getSubscriberId();
       	Log.debugf("Find Preset Operation By paTaxCode %s, subscriberId %s, presetId %s",paTaxCode,subscriberId,presetId);
       	return presetRepository.list("paTaxCode = :paTaxCode and subscriberId = :subscriberId and presetId = :presetId", 
										 Parameters.with("paTaxCode", paTaxCode)
										 				.and("subscriberId", subscriberId)
										 				.and("presetId", presetId).map()
										 )
							.onFailure().transform(err -> {
								Log.errorf(err, "[%s] Error while find subscriber",  ErrorCode.ERROR_COMMUNICATION_MONGO_DB);
								return new InternalServerErrorException(Response
										.status(Status.INTERNAL_SERVER_ERROR)
										.entity(new Errors(List.of(ErrorCode.ERROR_COMMUNICATION_MONGO_DB)))
										.build());
								}
							).chain(m -> {
								Log.debugf("Entity Found with paTaxCode %s", m.get(0).getPaTaxCode());
								return !m.isEmpty() ? updatePreset(paymentTransaction,m.get(0)) : null;
							});
//       	return Uni.createFrom().voidItem();
	}
    

    /**
     * Update the preset operation with the payment notices, set the status as EXECUTED and update the status timestamp with the current datetime
     * @param inputPaymentTransaction {@link PaymentTransaction} the data of the payment transaction
     * @param presetEntity {@link PresetsEntity} the preset operation to update 
     */
    private void mapPresetEntityInformation(PaymentTransaction inputPaymentTransaction, PresetsEntity presetEntity) {
    	PaymentTransaction paymentTransaction =new PaymentTransaction();
    	paymentTransaction.setTransactionId(inputPaymentTransaction.getTransactionId());
    	paymentTransaction.setAcquirerId(inputPaymentTransaction.getAcquirerId());
    	paymentTransaction.setChannel(inputPaymentTransaction.getChannel());
    	paymentTransaction.setMerchantId(inputPaymentTransaction.getMerchantId());
    	paymentTransaction.setTerminalId(inputPaymentTransaction.getTerminalId());
    	paymentTransaction.setInsertTimestamp(inputPaymentTransaction.getInsertTimestamp());
    	paymentTransaction.setNotices(inputPaymentTransaction.getNotices());
    	paymentTransaction.setTotalAmount(inputPaymentTransaction.getTotalAmount());
    	paymentTransaction.setFee(inputPaymentTransaction.getFee());
    	paymentTransaction.setStatus(inputPaymentTransaction.getStatus());
    	paymentTransaction.setPaymentMethod(inputPaymentTransaction.getPaymentMethod());
    	paymentTransaction.setPaymentTimestamp(inputPaymentTransaction.getPaymentTimestamp());
    	paymentTransaction.setCloseTimestamp(inputPaymentTransaction.getCloseTimestamp());


    	presetEntity.setStatusDetails(paymentTransaction);
    	presetEntity.setStatus(PresetStatus.EXECUTED.name());
    	presetEntity.setStatusTimestamp(DateUtils.getAndFormatCurrentDate());
    }
    
    /**
     * Performs the update on the preset operation
     * @param inputPaymentTransaction {@link PaymentTransaction} the data of the payment transaction
     * @param presetEntity {@link PresetsEntity} the preset operation to update 
     * @return
     */
    private Uni<PresetsEntity> updatePreset(PaymentTransaction inputPaymentTransaction, PresetsEntity presetEntity) {
		Log.debugf("Updating Preset");
		mapPresetEntityInformation(inputPaymentTransaction, presetEntity);
		return presetRepository.update(presetEntity)
		.onFailure().retry().withBackOff(Duration.ofSeconds(30), Duration.ofSeconds(30)).atMost(2)
        .map(m ->  {
        	Log.debugf("Preset Operation By paTaxCode %s, SubscriberId %s, presetId %s",presetEntity.getPaTaxCode(),presetEntity.getSubscriberId(),presetEntity.getPresetId());
//        	testEmitter.send("ok");
        	return m;
        });
	}
}
