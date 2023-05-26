package it.pagopa.swclient.mil.preset.resource;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import it.pagopa.swclient.mil.preset.PresetStatus;
import it.pagopa.swclient.mil.preset.bean.PaymentTransaction;
import it.pagopa.swclient.mil.preset.bean.PaymentTransactionStatus;
import it.pagopa.swclient.mil.preset.dao.PresetEntity;
import it.pagopa.swclient.mil.preset.dao.PresetRepository;
import it.pagopa.swclient.mil.preset.utils.DateUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.time.Duration;

@ApplicationScoped
public class PresetTopicResource {
	
	@Inject
	PresetRepository presetRepository;

	/**
	 * Update the status and details of a preset operation with type: payment notice
	 *
	 * @param paymentTransaction {@link PaymentTransaction} the data of the payment transaction
	 */
	@Incoming("presets")
	public void consume(PaymentTransaction paymentTransaction) {
		Log.debugf("Consume Message %s", paymentTransaction);
		findPresetsOperation(paymentTransaction)
				.chain(entity -> updatePreset(paymentTransaction, entity))
				.subscribe()
				.with(
						item -> {
							Log.debugf("Item updated %s", item);
						},
						error -> {
							Log.debugf("Error while updating item", error);
						}
				);
	}

	/**
	 * Finds a preset operation by paTaxCode SubscriberId and PresetId
	 *
	 * @param paymentTransaction {@link PaymentTransaction} the data of the payment transaction
	 * @return an {@link Uni} emitting the found {@link PresetEntity}
	 */
	private Uni<PresetEntity> findPresetsOperation(PaymentTransaction paymentTransaction) {

		final String paTaxCode = paymentTransaction.getPreset().getPaTaxCode();
		final String presetId = paymentTransaction.getPreset().getPresetId();
		final String subscriberId = paymentTransaction.getPreset().getSubscriberId();

		Log.debugf("Find preset operation By paTaxCode %s, subscriberId %s, presetId %s", paTaxCode, subscriberId, presetId);

		return presetRepository.list("presetOperation.paTaxCode = :paTaxCode and presetOperation.subscriberId = :subscriberId and presetOperation.presetId = :presetId",
						Parameters.with("paTaxCode", paTaxCode)
								.and("subscriberId", subscriberId)
								.and("presetId", presetId)
								.map())
				.onItem().transform(Unchecked.function(e -> {
					if (e.isEmpty()) {
						Log.errorf("No preset found with id %s", presetId);
						throw new NotFoundException();
					}
					else return e.get(0);
				}));
	}

	/**
	 * Performs the update on the preset operation
	 *
	 * @param inputPaymentTransaction {@link PaymentTransaction} the data of the payment transaction
	 * @param presetEntity            {@link PresetEntity} the preset operation to update
	 * @return
	 */
	private Uni<PresetEntity> updatePreset(PaymentTransaction inputPaymentTransaction, PresetEntity presetEntity) {
		Log.debugf("Updating Preset");

		if (!PaymentTransactionStatus.PRE_CLOSE.name().equals(inputPaymentTransaction.getStatus())) {
			presetEntity.presetOperation.setStatus(PresetStatus.EXECUTED.name());
		}
		presetEntity.presetOperation.setStatusTimestamp(DateUtils.getCurrentTimestamp());
		presetEntity.presetOperation.setStatusDetails(inputPaymentTransaction);

		return presetRepository.update(presetEntity)
				.onFailure().retry().withBackOff(Duration.ofSeconds(30), Duration.ofSeconds(30)).atMost(2);
	}

}
