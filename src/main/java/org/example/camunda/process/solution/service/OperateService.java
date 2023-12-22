package org.example.camunda.process.solution.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.auth.SaasAuthentication;
import io.camunda.operate.auth.SelfManagedAuthentication;
import io.camunda.operate.dto.ProcessDefinition;
import io.camunda.operate.dto.Variable;
import io.camunda.operate.exception.OperateException;
import io.camunda.operate.search.ProcessDefinitionFilter;
import io.camunda.operate.search.SearchQuery;
import io.camunda.operate.search.Sort;
import io.camunda.operate.search.SortOrder;
import io.camunda.operate.search.VariableFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.example.camunda.process.solution.utils.BpmnUtils;
import org.example.camunda.process.solution.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

@Service
@EnableCaching
public class OperateService {

  private static final Logger LOG = LoggerFactory.getLogger(OperateService.class);

  @Value("${zeebe.client.cloud.clientId:notProvided}")
  private String clientId;

  @Value("${zeebe.client.cloud.clientSecret:notProvided}")
  private String clientSecret;

  @Value("${zeebe.client.cloud.clusterId:notProvided}")
  private String clusterId;

  @Value("${zeebe.client.cloud.region:notProvided}")
  private String region;

  @Value("${identity.clientId:notProvided}")
  private String identityClientId;

  @Value("${identity.clientSecret:notProvided}")
  private String identityClientSecret;

  @Value("${operateUrl:notProvided}")
  private String operateUrl;

  @Value("${keycloakUrl:notProvided}")
  private String keycloakUrl;

  private CamundaOperateClient client;

  private CamundaOperateClient getCamundaOperateClient() throws OperateException {
    if (client == null) {
      if (!"notProvided".equals(clientId)) {
        SaasAuthentication sa = new SaasAuthentication(clientId, clientSecret);
        client =
            new CamundaOperateClient.Builder()
                .operateUrl("https://" + region + ".operate.camunda.io/" + clusterId)
                .authentication(sa)
                .build();
      } else {
        SelfManagedAuthentication la =
            new SelfManagedAuthentication()
                .clientId(identityClientId)
                .clientSecret(identityClientSecret)
                .keycloakUrl(keycloakUrl);
        client =
            new CamundaOperateClient.Builder().operateUrl(operateUrl).authentication(la).build();
      }
    }
    return client;
  }

  public List<ProcessDefinition> getProcessDefinitions() throws OperateException {
    ProcessDefinitionFilter processDefinitionFilter = new ProcessDefinitionFilter.Builder().build();
    SearchQuery procDefQuery =
        new SearchQuery.Builder()
            .filter(processDefinitionFilter)
            .size(1000)
            .sort(new Sort("version", SortOrder.DESC))
            .build();

    return getCamundaOperateClient().searchProcessDefinitions(procDefQuery);
  }

  public List<ProcessDefinition> getProcessDefinitionByKey(Long key) throws OperateException {
    ProcessDefinitionFilter processDefinitionFilter = new ProcessDefinitionFilter.Builder().build();
    SearchQuery procDefQuery =
        new SearchQuery.Builder()
            .filter(processDefinitionFilter)
            .size(1000)
            .sort(new Sort("version", SortOrder.DESC))
            .build();

    return getCamundaOperateClient().searchProcessDefinitions(procDefQuery);
  }

  @Cacheable("processXmls")
  public String getProcessDefinitionXmlByKey(Long key) throws OperateException {
    LOG.info("Entering getProcessDefinitionXmlByKey for key " + key);
    return getCamundaOperateClient().getProcessDefinitionXml(key);
  }

  public String getInitializationForm(String processDefinitionId)
      throws NumberFormatException, OperateException {
    String xml = getProcessDefinitionXmlByKey(Long.valueOf(processDefinitionId));
    return BpmnUtils.getStartingFormSchema(xml);
  }

  public Map<String, Set<JsonNode>> listVariables() throws OperateException, IOException {
    List<Variable> vars =
        getCamundaOperateClient()
            .searchVariables(
                new SearchQuery.Builder().filter(new VariableFilter()).size(1000).build());
    Map<String, Set<JsonNode>> result = new HashMap<>();
    for (Variable var : vars) {
      if (!result.containsKey(var.getName())) {
        result.put(var.getName(), new HashSet<>());
      }
      result.get(var.getName()).add(JsonUtils.toJsonNode(var.getValue()));
    }
    return result;
  }
}
