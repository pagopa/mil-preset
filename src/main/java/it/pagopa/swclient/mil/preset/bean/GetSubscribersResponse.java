package it.pagopa.swclient.mil.preset.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@RegisterForReflection
public class GetSubscribersResponse {

	/*
	 * 	List of subscribed terminals
	 */
	@NotNull
	private List<Subscriber> subscribers = new ArrayList<>();

	/**
	 * @return the subscribers
	 */
	public List<Subscriber> getSubscribers() {
		return subscribers;
	}

	/**
	 * @param subscribers the subscribers to set
	 */
	public void setSubscribers(List<Subscriber> subscribers) {
		this.subscribers = subscribers;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetSubscribersResponse [subscribers=");
		builder.append(subscribers);
		builder.append("]");
		return builder.toString();
	}

}
