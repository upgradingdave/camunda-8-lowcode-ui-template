package org.example.camunda.process.solution.service;

import org.example.camunda.process.solution.exception.UnauthorizedException;
import org.example.camunda.process.solution.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

  @Autowired private OrganizationService organizationService;

  public User getByUsernameAndPwd(String username, String pwd) {
    User user = organizationService.getUserByUsername(username);
    if (user == null) {
      throw new UnauthorizedException("User doesn't exist");
    }
    return user;
    /*if (user!=null && user.getPassword() != null) {

    	if (!SecurityUtils.matches(pwd, user.getPassword())) {
    		throw new UnauthorizedException("Invalid credentials");
    	}
        return user;
    }
    return null;*/
  }
}
