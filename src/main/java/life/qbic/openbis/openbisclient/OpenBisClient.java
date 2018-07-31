package life.qbic.openbis.openbisclient;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class OpenBisClient {

  private final int TIMEOUT = 10000;
  private String userId, password, sessionToken;
  private IApplicationServerApi v3;


  public OpenBisClient(String userId, String password, String serverURL) {
    this.userId = userId;
    this.password = password;

    // get a reference to AS API
    v3 = HttpInvokerUtils
        .createServiceStub(IApplicationServerApi.class, serverURL, TIMEOUT);
    sessionToken = null;
  }

  /**
   * Checks if connection to AS API is established
   */
  public boolean connectedToAS() {
    if (v3 == null) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Checks if we are logged in
   */
  public boolean loggedin() {
    try {
      return v3.isSessionActive(sessionToken);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * logs out of the OpenBIS server
   */
  public void logout() {
    if (loggedin()) {
      v3.logout(sessionToken);
      sessionToken = null;
    } else {
    }

  }

  /**
   * logs in to the OpenBIS server with the system userid after calling this function, the user has
   * to provide the password
   */
  public void login() {
    if (loggedin()) {
      logout();
    }

    // login to obtain a session token
    sessionToken = v3.login(userId, password);
  }

  public String getUserId() {
    return userId;
  }

  public IApplicationServerApi getV3() {
    return v3;
  }

  public String getPassword() {
    return password;
  }

  public String getSessionToken() {
    return sessionToken;
  }
}
