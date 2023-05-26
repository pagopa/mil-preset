package it.pagopa.swclient.mil.preset;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySource;
import it.pagopa.swclient.mil.preset.bean.PaymentTransaction;
import it.pagopa.swclient.mil.preset.bean.PaymentTransactionStatus;
import it.pagopa.swclient.mil.preset.bean.PresetOperation;
import it.pagopa.swclient.mil.preset.dao.PresetEntity;
import it.pagopa.swclient.mil.preset.dao.PresetRepository;
import it.pagopa.swclient.mil.preset.resource.KafkaTestResourceLifecycleManager;
import it.pagopa.swclient.mil.preset.util.PresetTestData;
import it.pagopa.swclient.mil.preset.util.TestUtils;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@QuarkusTest
@QuarkusTestResource(KafkaTestResourceLifecycleManager.class)
class PresetTopicResourceTest {

	static final Logger logger = LoggerFactory.getLogger(PresetTopicResourceTest.class);

	@Inject @Any
	InMemoryConnector connector;

	@InjectMock
	PresetRepository presetRepository;

	@Test
	void consume_close_ok() {

		InMemorySource<PaymentTransaction> paymentTransactionsIn = connector.source("presets");

		String presetId = UUID.randomUUID().toString();

		PaymentTransaction paymentTransaction = PresetTestData.getPaymentTransaction(
				PaymentTransactionStatus.PENDING,
				PresetTestData.getMilHeaders(true, true),
				PresetTestData.getPreset(presetId, "x46tr3"),
				1);

		PresetEntity presetEntity = PresetTestData.getPresetEntity(presetId, "x46tr3");

		Mockito
				.when(presetRepository.list(Mockito.any(String.class), Mockito.anyMap()))
				.thenReturn(Uni.createFrom().item(List.of(TestUtils.getClonedObject(presetEntity, PresetEntity.class))));

		Mockito
				.when(presetRepository.update(Mockito.any(PresetEntity.class)))
				.then(i -> Uni.createFrom().item(i.getArgument(0, PresetEntity.class)));

		paymentTransactionsIn.send(paymentTransaction);

		// check mongo panache repository integration
		ArgumentCaptor<String> captorFindQuery = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Map<String, Object>> captorFindArguments = ArgumentCaptor.forClass(Map.class);

		Mockito.verify(presetRepository, Mockito.timeout(10000)).list(captorFindQuery.capture(), captorFindArguments.capture());
		Assertions.assertEquals(3, captorFindArguments.getValue().size());
		Assertions.assertEquals(PresetTestData.PA_TAX_CODE, captorFindArguments.getValue().get("paTaxCode"));
		Assertions.assertEquals("x46tr3", captorFindArguments.getValue().get("subscriberId"));
		Assertions.assertEquals(presetId, captorFindArguments.getValue().get("presetId"));

		ArgumentCaptor<PresetEntity> captorUpdateQuery = ArgumentCaptor.forClass(PresetEntity.class);
		Mockito.verify(presetRepository, Mockito.timeout(10000)).update(captorUpdateQuery.capture());

		Assertions.assertNotNull(captorUpdateQuery.getValue());
		PresetOperation updatedPreset = captorUpdateQuery.getValue().presetOperation;
		logger.info("{}", updatedPreset);

		Assertions.assertEquals(presetEntity.presetOperation.getOperationType(), updatedPreset.getOperationType());
		Assertions.assertEquals(presetEntity.presetOperation.getPresetId(),updatedPreset.getPresetId());
		Assertions.assertEquals(presetEntity.presetOperation.getPaTaxCode(), updatedPreset.getPaTaxCode());
		Assertions.assertEquals(presetEntity.presetOperation.getSubscriberId(), updatedPreset.getSubscriberId());
		Assertions.assertEquals(presetEntity.presetOperation.getCreationTimestamp(),updatedPreset.getCreationTimestamp());
		Assertions.assertEquals(PresetStatus.EXECUTED.name(), updatedPreset.getStatus());
		Assertions.assertNotEquals(presetEntity.presetOperation.getStatusTimestamp(), updatedPreset.getStatusTimestamp());
		Assertions.assertEquals(presetEntity.presetOperation.getNoticeTaxCode(), updatedPreset.getNoticeTaxCode());
		Assertions.assertEquals(presetEntity.presetOperation.getNoticeNumber(), updatedPreset.getNoticeNumber());
		Assertions.assertNotNull(updatedPreset.getStatusDetails());

	}

}
