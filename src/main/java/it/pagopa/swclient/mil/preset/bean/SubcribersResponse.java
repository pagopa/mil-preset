/**
 * 
 */
package it.pagopa.swclient.mil.preset.bean;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;

@RegisterForReflection
public class SubcribersResponse {
	/*
	 * 	List of subscribed terminals
	 */
	@NotNull
	private List<SubscriberResponse> subscribers = new ArrayList<>();

	/**
	 * @return the subscribers
	 */
	public List<SubscriberResponse> getSubscribers() {
		return subscribers;
	}

	/**
	 * @param subscriberResponses the subscribers to set
	 */
	public void setSubscribers(List<SubscriberResponse> subscriberResponses) {
		this.subscribers = subscriberResponses;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SubcribersResponse [subscribers=");
		builder.append(subscribers);
		builder.append("]");
		return builder.toString();
	}
}
