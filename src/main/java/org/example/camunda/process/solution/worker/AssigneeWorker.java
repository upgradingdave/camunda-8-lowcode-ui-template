package org.example.camunda.process.solution.worker;

import io.camunda.zeebe.spring.client.annotation.ZeebeVariablesAsType;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.example.camunda.process.solution.ProcessVariables;
import org.example.camunda.process.solution.model.User;
import org.example.camunda.process.solution.service.OrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AssigneeWorker {

  private static final Logger LOG = LoggerFactory.getLogger(AssigneeWorker.class);

  @Autowired private OrganizationService organizationService;

  @ZeebeWorker(type = "selectAssignee", autoComplete = true)
  public ProcessVariables selectAssignee(@ZeebeVariablesAsType ProcessVariables variables) {
    LOG.info("Invoking myService with variables: " + variables);

    Collection<User> users = organizationService.all();
    int idx = (int) Math.floor(Math.random() * users.size());
    List<User> usersList = new ArrayList<User>(users);
    return new ProcessVariables().setAssignee1(usersList.get(idx).getUsername());
  }
}
