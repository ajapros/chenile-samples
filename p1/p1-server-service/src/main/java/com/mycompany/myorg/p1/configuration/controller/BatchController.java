package com.mycompany.myorg.p1.configuration.controller;

import com.mycompany.myorg.p1.api.BatchService;
import com.mycompany.myorg.p1.model.BatchIn;
import org.chenile.orchestrator.process.model.Process;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.base.response.GenericResponse;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@ChenileController(value="batch", serviceName="_batchService_", interfaceClass=BatchService.class,
    bluePrintName="chenile-process-management", registerInServiceRegistry=true)
public class BatchController extends ControllerSupport {
    @PostMapping("/batch")
    public ResponseEntity<GenericResponse<Process>> start(HttpServletRequest request, @RequestBody BatchIn input) {
        return process(request,input);
    }
}
