package uniregistrar.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.driver.util.HttpBindingServerUtil;
import uniregistrar.local.LocalUniRegistrar;
import uniregistrar.local.extensions.Extension;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.State;
import uniregistrar.web.WebUniRegistrar;

import java.io.IOException;
import java.util.Map;

public class UpdateServlet extends WebUniRegistrar {

	protected static Logger log = LoggerFactory.getLogger(UpdateServlet.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		String method = request.getParameter("method");

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		Map<String, Object> requestMap;

		try {
			requestMap = objectMapper.readValue(request.getReader(), Map.class);
		} catch (Exception ex) {
			if (log.isWarnEnabled()) log.warn("Cannot parse UPDATE request (JSON): " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse UPDATE request (JSON): " + ex.getMessage());
			return;
		}

		// [before read]

		if (this.getUniRegistrar() instanceof LocalUniRegistrar) {
			LocalUniRegistrar localUniRegistrar = ((LocalUniRegistrar) this.getUniRegistrar());
			for (Extension extension : localUniRegistrar.getExtensions()) {
				if (! (extension instanceof Extension.BeforeReadUpdateExtension)) continue;
				if (log.isDebugEnabled()) log.debug("Executing extension (beforeReadUpdate) " + extension.getClass().getSimpleName() + " with request map " + requestMap);
				try {
					((Extension.BeforeReadUpdateExtension) extension).beforeReadUpdate(method, requestMap, localUniRegistrar);
				} catch (Exception ex) {
					if (log.isWarnEnabled()) log.warn("Cannot parse UPDATE request (extension): " + ex.getMessage(), ex);
					ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse UPDATE request (extension): " + ex.getMessage());
					return;
				}
			}
		}

		// parse request

		UpdateRequest updateRequest;

		try {
			updateRequest = UpdateRequest.fromMap(requestMap);
		} catch (Exception ex) {
			if (log.isWarnEnabled()) log.warn("Cannot parse UPDATE request (object): " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse UPDATE request (object): " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Incoming UPDATE request for method " + method + ": " + updateRequest);

		if (updateRequest == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "No UPDATE request found.");
			return;
		}

		// execute the request

		State state;
		Map<String, Object> stateMap;

		try {

			state = this.update(method, updateRequest);
			if (state == null) throw new RegistrationException("No state.");
			stateMap = state.toMap();
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("UPDATE problem for " + updateRequest + ": " + ex.getMessage(), ex);

			if (! (ex instanceof RegistrationException)) ex = new RegistrationException("UPDATE problem for " + updateRequest + ": " + ex.getMessage());
			state = ((RegistrationException) ex).toFailedState();
			stateMap = state.toMap();
		}

		if (log.isInfoEnabled()) log.info("State for " + updateRequest + ": " + state);

		// [before write]

		if (this.getUniRegistrar() instanceof LocalUniRegistrar) {
			LocalUniRegistrar localUniRegistrar = ((LocalUniRegistrar) this.getUniRegistrar());
			for (Extension extension : localUniRegistrar.getExtensions()) {
				if (! (extension instanceof Extension.BeforeWriteUpdateExtension)) continue;
				if (log.isDebugEnabled()) log.debug("Executing extension (() " + extension.getClass().getSimpleName() + " with state map " + stateMap);
				try {
					((Extension.BeforeWriteUpdateExtension) extension).beforeWriteUpdate(method, stateMap, localUniRegistrar);
				} catch (Exception ex) {
					if (log.isWarnEnabled()) log.warn("Cannot write UPDATE state (extension): " + ex.getMessage(), ex);
					ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot write UPDATE state (extension): " + ex.getMessage());
					return;
				}
			}
		}

		// write state

		ServletUtil.sendResponse(
				response,
				HttpBindingServerUtil.httpStatusCodeForState(state),
				State.MEDIA_TYPE,
				HttpBindingServerUtil.toHttpBodyStreamState(stateMap));
	}
}