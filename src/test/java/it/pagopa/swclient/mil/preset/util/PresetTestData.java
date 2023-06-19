package it.pagopa.swclient.mil.preset.util;

import it.pagopa.swclient.mil.preset.OperationType;
import it.pagopa.swclient.mil.preset.PresetStatus;
import it.pagopa.swclient.mil.preset.bean.CreatePresetRequest;
import it.pagopa.swclient.mil.preset.bean.Notice;
import it.pagopa.swclient.mil.preset.bean.PaymentTransaction;
import it.pagopa.swclient.mil.preset.bean.PaymentTransactionStatus;
import it.pagopa.swclient.mil.preset.bean.Preset;
import it.pagopa.swclient.mil.preset.bean.PresetOperation;
import it.pagopa.swclient.mil.preset.dao.PresetEntity;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PresetTestData {

    public static Map<String, String> getPosHeaders(boolean isPos, boolean isKnownAcquirer) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("RequestId", UUID.randomUUID().toString());
        headerMap.put("Version", "1.0.0");
        headerMap.put("AcquirerId", isKnownAcquirer ? PresetTestData.ACQUIRER_ID_KNOWN : PresetTestData.ACQUIRER_ID_NOT_KNOWN);
        headerMap.put("Channel", isPos ? "POS" : "ATM");
        headerMap.put("TerminalId", "0aB9wXyZ");
        if (isPos) headerMap.put("MerchantId", "28405fHfk73x88D");
        headerMap.put("SessionId", UUID.randomUUID().toString());
        return headerMap;
    }

    public static Map<String, String> getInstitutionPortalHeaders() {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("RequestId", UUID.randomUUID().toString());
        headerMap.put("Version", "1.0.0");
        return headerMap;
    }

    public static Notice getNotice(String paymentToken) {
        Notice notice = new Notice();
        notice.setPaymentToken(paymentToken);
        notice.setPaTaxCode(PA_TAX_CODE);
        notice.setNoticeNumber(NOTICE_NUMBER);
        notice.setAmount(AMOUNT);
        notice.setDescription("Test payment notice");
        notice.setCompany("Test company");
        notice.setOffice("Test office");
        return notice;
    }

    public static Preset getPreset(String presetId, String subscriberId) {
        Preset preset = new Preset();
        preset.setSubscriberId(subscriberId);
        preset.setPresetId(presetId);
        preset.setPaTaxCode(PA_TAX_CODE);
        return preset;
    }

    public static PresetEntity getPresetEntity(String presetId, String subscriberId) {

        String timestamp = LocalDateTime.ofInstant(Instant.now().minus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC).toString();

        PresetOperation presetOperation = new PresetOperation();
        presetOperation.setPresetId(presetId);
        presetOperation.setCreationTimestamp(timestamp);
        presetOperation.setNoticeNumber("485564829563528563");
        presetOperation.setNoticeTaxCode(PA_TAX_CODE);
        presetOperation.setOperationType(OperationType.PAYMENT_NOTICE.name());
        presetOperation.setPaTaxCode(PA_TAX_CODE);
        presetOperation.setStatus(PresetStatus.TO_EXECUTE.name());
        presetOperation.setStatusTimestamp(timestamp);
        presetOperation.setSubscriberId(subscriberId);

        PresetEntity presetEntity = new PresetEntity();
        presetEntity.id = presetId;
        presetEntity.presetOperation = presetOperation;

        return presetEntity;
    }

    public static PaymentTransaction getPaymentTransaction(PaymentTransactionStatus status,
                                                           Map<String, String> headers,
                                                           Preset preset,
                                                           int tokens) {

        String transactionId = RandomStringUtils.random(32, true, true);

        if (status == PaymentTransactionStatus.ABORTED) throw new IllegalArgumentException();

        String timestamp = LocalDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC).toString();

        var paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionId(transactionId);
        paymentTransaction.setAcquirerId(headers.get("AcquirerId"));
        paymentTransaction.setChannel(headers.get("Channel"));
        paymentTransaction.setMerchantId(headers.get("MerchantId"));
        paymentTransaction.setTerminalId(headers.get("TerminalId"));
        paymentTransaction.setInsertTimestamp(timestamp);

        List<Notice> notices = new ArrayList<>();
        for (int i = 0; i < tokens; i++) {
            notices.add(getNotice(RandomStringUtils.random(32, true, true)));
        }

        paymentTransaction.setNotices(notices);
        paymentTransaction.setTotalAmount(notices.stream().map(Notice::getAmount).reduce(Long::sum).orElse(0L));

        paymentTransaction.setFee(100L);
        paymentTransaction.setStatus(status.name());

        switch (status) {
            case PRE_CLOSE -> {}
            case PENDING,ERROR_ON_PAYMENT, ERROR_ON_CLOSE -> {
                paymentTransaction.setPaymentMethod("PAGOBANCOMAT");
                paymentTransaction.setPaymentTimestamp(timestamp);
                paymentTransaction.setCloseTimestamp(timestamp);
            }
            case CLOSED, ERROR_ON_RESULT -> {
                paymentTransaction.setPaymentMethod("PAGOBANCOMAT");
                paymentTransaction.setPaymentTimestamp(timestamp);
                paymentTransaction.setCloseTimestamp(timestamp);
                paymentTransaction.setPaymentDate(timestamp);
                paymentTransaction.setCallbackTimestamp(timestamp);
                notices.forEach(n -> {
                    n.setDebtor("Mario Rossi");
                    n.setCreditorReferenceId("abcde");
                });
            }
        }

        paymentTransaction.setPreset(preset);

        return paymentTransaction;

    }
    
    public static CreatePresetRequest getCreatePresetRequest() {
    	CreatePresetRequest request = new CreatePresetRequest();
        request.setNoticeNumber("485564829563528563");
        request.setNoticeTaxCode("15376371009");
        request.setOperationType("PAYMENT_NOTICE");
        request.setPaTaxCode("15376371009");
        request.setSubscriberId("x46tr3");
        return request;
    }

    public static final String PA_TAX_CODE = "15376371009";
    public static final String NOTICE_NUMBER = "000000000000000000";
    public static final long AMOUNT = 9999;

    // ACQUIRER ID
    public static final String ACQUIRER_ID_KNOWN = "4585625";
    public static final String ACQUIRER_ID_NOT_KNOWN = "4585626";

    private PresetTestData() {
    }


}
