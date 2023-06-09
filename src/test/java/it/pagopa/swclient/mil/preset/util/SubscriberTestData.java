/**
 * 
 */
package it.pagopa.swclient.mil.preset.util;

import it.pagopa.swclient.mil.preset.bean.Subscriber;
import it.pagopa.swclient.mil.preset.dao.SubscriberEntity;

public class SubscriberTestData {
	public static final String SUBCRIBER_FOUND		= "x46tr0";
	public static final String SUBCRIBER_NOT_FOUND	= "aaaaaa";
	public static final String UNSUBCRIBE			= "bbbbbb";
	
	public static SubscriberEntity getSubscribers(String subscriberId) {
		SubscriberEntity subscriberEntity 	= new SubscriberEntity();
		Subscriber subscriber 				= new Subscriber();
		subscriber.setAcquirerId("4585625");
		subscriber.setChannel("POS");
		subscriber.setLabel("test label");
		subscriber.setLastUsageTimestamp("2023-05-17T14:50:05.446");
		subscriber.setMerchantId("23533");
		subscriber.setPaTaxCode("15376371009");
		subscriber.setSubscriberId(subscriberId);
		subscriber.setSubscriptionTimestamp("2023-05-17T14:50:05.446");
		subscriber.setTerminalId("0aB9wXyZ");
		
		subscriberEntity.subscriber = subscriber;
		subscriberEntity.id = subscriberId;
		return subscriberEntity;
	}
}
