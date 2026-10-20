package com.mycompany.myorg.p1.configuration.controller;

import com.mycompany.myorg.p1.api.ItemService;
import com.mycompany.myorg.p1.model.ItemIn;
import org.chenile.orchestrator.process.model.Process;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.base.response.GenericResponse;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@ChenileController(value="item", serviceName="_itemService_", interfaceClass=ItemService.class,
    bluePrintName="chenile-process-management", registerInServiceRegistry=true)
public class ItemController extends ControllerSupport {
    @PostMapping("/item")
    public ResponseEntity<GenericResponse<Process>> start(HttpServletRequest request, @RequestBody ItemIn input) {
        return process(request,input);
    }
}
