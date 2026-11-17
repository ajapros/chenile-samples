package org.chenile.samples.scheduler.configuration.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.annotation.EventsSubscribedTo;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.samples.scheduler.model.ScheduledReportRequest;
import org.chenile.samples.scheduler.model.ScheduledReportResult;
import org.chenile.samples.scheduler.service.ScheduledReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ChenileController(value = "scheduledReportService",
		serviceName = "_scheduledReportService_",
		interfaceClass = ScheduledReportService.class)
public class ScheduledReportController extends ControllerSupport {
	/** Runs on POST, and whenever the scheduledReport event fires (here, from the daily-report schedule). */
	@PostMapping("/scheduled-report/generate")
	@EventsSubscribedTo("scheduledReport")
	public ResponseEntity<GenericResponse<ScheduledReportResult>> generate(HttpServletRequest request,
			@RequestBody ScheduledReportRequest body) {
		return process("generate", request, body);
	}
}
