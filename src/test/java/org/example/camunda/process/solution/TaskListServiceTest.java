package org.example.camunda.process.solution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.camunda.common.auth.*;
import io.camunda.common.auth.identity.IdentityConfig;
import io.camunda.common.auth.identity.IdentityContainer;
import io.camunda.identity.sdk.Identity;
import io.camunda.identity.sdk.IdentityConfiguration;
import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.exception.OperateException;
import io.camunda.operate.model.ProcessDefinition;
import io.camunda.operate.search.SearchQuery;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.Topology;
import org.example.camunda.process.solution.service.TaskListService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TaskListServiceTest {

  @Autowired ZeebeClient zeebeClient;

  @Autowired TaskListService taskListService;

  @Test
  public void testZeebe() {
    Topology topology = zeebeClient.newTopologyRequest().send().join();
    assertEquals(1, topology.getBrokers().size());
  }

  String tenantId = "cbd46654-4f74-4332-a490-69f0f071ba9f";
  String clientId = "ba4089db-c72a-4049-8fde-955179365ccf";
  String clientSecret = "7tB8Q~DXNton_.lTJkIs5AtXGgZnzsKUkILRoc4y";
  String scope = clientId + "/.default";
  String issuer = "https://login.microsoftonline.com/" + tenantId + "/v2.0";
  String issuerUrl = "https://login.microsoftonline.com/" + tenantId + "/v2.0";
  String authUrl = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";
  String audience = clientId;
  String type = "MICROSOFT";

  String taskListUrl = "https://dave03.gke.c8sm.com/tasklist";
  String operateUrl = "https://dave03.gke.c8sm.com/operate";

  @Test
  public void connectOperate() throws OperateException {
    JwtConfig jwtConfig = new JwtConfig();
    jwtConfig.addProduct(
        Product.OPERATE, new JwtCredential(clientId, clientSecret, clientId, authUrl));

    IdentityConfiguration configuration =
        new IdentityConfiguration(issuer, issuerUrl, clientId, clientSecret, audience, type);
    IdentityConfig identityConfig = new IdentityConfig();
    identityConfig.addProduct(
        Product.OPERATE, new IdentityContainer(new Identity(configuration), configuration));

    Authentication auth =
        SelfManagedAuthentication.builder()
            .identityConfig(identityConfig)
            .jwtConfig(jwtConfig)
            .build();

    CamundaOperateClient client =
        CamundaOperateClient.builder().operateUrl(operateUrl).authentication(auth).setup().build();

    SearchQuery searchQuery = new SearchQuery();
    Long processKey = 2251799813685249L;
    ProcessDefinition def = client.getProcessDefinition(processKey);
    assertNotNull(def);
  }

  @Test
  public void connectTaskList() throws TaskListException {

    JwtConfig jwtConfig = new JwtConfig();
    jwtConfig.addProduct(
        Product.TASKLIST, new JwtCredential(clientId, clientSecret, clientId, authUrl));

    IdentityConfiguration configuration =
        new IdentityConfiguration(issuer, issuerUrl, clientId, clientSecret, audience, type);
    IdentityConfig identityConfig = new IdentityConfig();
    identityConfig.addProduct(
        Product.TASKLIST, new IdentityContainer(new Identity(configuration), configuration));

    Authentication auth =
        SelfManagedAuthentication.builder()
            .identityConfig(identityConfig)
            .jwtConfig(jwtConfig)
            .build();

    CamundaTaskListClient client =
        CamundaTaskListClient.builder().taskListUrl(taskListUrl).authentication(auth).build();

    TaskSearch taskSearch = new TaskSearch();
    taskSearch.setAssigned(true);
    TaskList taskList = client.getTasks(taskSearch);
    assertNotNull(taskList);
  }
}
