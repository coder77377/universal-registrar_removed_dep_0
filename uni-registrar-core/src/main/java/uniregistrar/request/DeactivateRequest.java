package uniregistrar.request;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uniregistrar.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class DeactivateRequest extends JsonObject implements Request {

	public static final String MIME_TYPE = "application/json";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@JsonProperty
	private String jobId;

	@JsonProperty
	private String did;

	@JsonProperty
	private Map<String, Object> options;

	@JsonProperty
	private Map<String, Object> secret;

	public DeactivateRequest() {

	}

	public DeactivateRequest(String jobId, String did, Map<String, Object> options, Map<String, Object> secret) {
		this.jobId = jobId;
		this.did = did;
		this.options = options;
		this.secret = secret;
	}

	/*
	 * Serialization
	 */

	public static DeactivateRequest fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
		return objectMapper.readValue(json, DeactivateRequest.class);
	}

	public static DeactivateRequest fromJson(Reader reader) throws JsonParseException, JsonMappingException, IOException {
		return objectMapper.readValue(reader, DeactivateRequest.class);
	}

	public static DeactivateRequest fromMap(Map<String, Object> map) {
		return objectMapper.convertValue(map, DeactivateRequest.class);
	}

	/*
	 * Getters and setters
	 */

	@JsonGetter
	public final String getJobId() {
		return this.jobId;
	}

	@JsonSetter
	public final void setJobId(String jobId) {
		this.jobId = jobId;
	}

	@JsonGetter
	public final String getDid() {
		return this.did;
	}

	@JsonSetter
	public final void setDid(String did) {
		this.did = did;
	}

	@JsonGetter
	public final Map<String, Object> getOptions() {
		return this.options;
	}

	@JsonSetter
	public final void setOptions(Map<String, Object> options) {
		this.options = options;
	}

	@JsonGetter
	public final Map<String, Object> getSecret() {
		return this.secret;
	}

	@JsonSetter
	public final void setSecret(Map<String, Object> secret) {
		this.secret = secret;
	}
}
