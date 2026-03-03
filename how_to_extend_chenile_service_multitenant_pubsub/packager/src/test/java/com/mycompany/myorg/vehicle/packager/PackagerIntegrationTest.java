package com.mycompany.myorg.vehicle.packager;

import com.jayway.jsonpath.JsonPath;
import com.mycompany.myorg.vehicle.packager.pubsub.PubSubSharedData;
import org.chenile.core.context.HeaderUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PackagerSpringTestConfig.class)
@AutoConfigureMockMvc
public class PackagerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PubSubSharedData pubSubSharedData;

    @Test
    void clientAbcExtensionFlowWorksForTenant1AndPublishesEvent() throws Exception {
        runFlowAndAssert("client_abc_ext", "tenant1", "POL-ABC", "ABC-EVENT");
    }

    @Test
    void clientXyzExtensionFlowWorksForTenant1AndPublishesEvent() throws Exception {
        runFlowAndAssert("client_xyz_ext", "tenant1", "POL-XYZ", "XYZ-EVENT");
    }

    private void runFlowAndAssert(String extType, String tenant, String policy, String comment) throws Exception {
        pubSubSharedData.reset(1);

        String createPayload = buildCreatePayload(extType, policy);

        MvcResult createResult = mockMvc.perform(withTenant(post("/vehicle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload), tenant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.mutatedEntity.currentState.stateId").value("OPENED"))
                .andReturn();

        if ("client_xyz_ext".equals(extType)) {
            Assertions.assertEquals("BLR-01", JsonPath.read(createResult.getResponse().getContentAsString(), "$.payload.mutatedEntity.xyzBranchCode"));
            Assertions.assertEquals("CORP", JsonPath.read(createResult.getResponse().getContentAsString(), "$.payload.mutatedEntity.xyzSegment"));
            Assertions.assertEquals("P1", JsonPath.read(createResult.getResponse().getContentAsString(), "$.payload.mutatedEntity.xyzPriority"));
        }

        String id = JsonPath.read(createResult.getResponse().getContentAsString(), "$.payload.mutatedEntity.id");

        mockMvc.perform(withTenant(patch("/vehicle/{id}/assign", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignee\":\"PACKAGER\",\"comment\":\"PACKAGER-ASSIGN\"}"), tenant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.mutatedEntity.currentState.stateId").value("ASSIGNED"));

        mockMvc.perform(withTenant(patch("/vehicle/{id}/ext", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"" + comment + "\"}"), tenant))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.mutatedEntity.currentState.stateId").value("EXTENSION"))
                .andExpect(jsonPath("$.payload.mutatedEntity.newColumn").value(comment));

        assertTrue(pubSubSharedData.latch.await(5, TimeUnit.SECONDS));
        Assertions.assertTrue(pubSubSharedData.tenantsSeen.contains(tenant));
        Assertions.assertTrue(pubSubSharedData.messages.stream().anyMatch(m -> m.contains(comment)));
    }

    private String buildCreatePayload(String extType, String policy) {
        if ("client_xyz_ext".equals(extType)) {
            return """
                    {
                      "ext_type": "%s",
                      "openedBy": "PACKAGER-USER",
                      "description": "Packager integration %s",
                      "xyzBranchCode": "BLR-01",
                      "xyzSegment": "CORP",
                      "xyzPriority": "P1",
                      "insurancePolicyNumber": "%s",
                      "fitnessExpiry": "2032-01-01"
                    }
                    """.formatted(extType, extType, policy);
        }

        return """
                {
                  "ext_type": "%s",
                  "openedBy": "PACKAGER-USER",
                  "description": "Packager integration %s",
                  "insurancePolicyNumber": "%s",
                  "fitnessExpiry": "2032-01-01"
                }
                """.formatted(extType, extType, policy);
    }

    private MockHttpServletRequestBuilder withTenant(MockHttpServletRequestBuilder builder, String tenant) {
        return builder.header(HeaderUtils.TENANT_ID_KEY, tenant);
    }
}
