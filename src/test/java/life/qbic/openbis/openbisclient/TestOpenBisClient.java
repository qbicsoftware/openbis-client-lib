package life.qbic.openbis.openbisclient;

import static com.google.common.truth.Truth.assertThat;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestOpenBisClient {

  private static OpenBisClient openbisClient;
  private static String DATASOURCE_USER = "datasource.user";
  private static String DATASOURCE_PASS = "datasource.password";
  private static String DATASOURCE_URL = "datasource.url";
  private static Properties config;

  @BeforeClass
  public static void setUpBeforeClass() {
    config = new Properties();
    config.setProperty(DATASOURCE_URL,
        "https://qbis.qbic.uni-tuebingen.de/openbis/openbis" + IApplicationServerApi.SERVICE_URL);
    config.setProperty(DATASOURCE_USER, "zxmqw74");
    config.setProperty(DATASOURCE_PASS, "***REMOVED***");
  }

  @AfterClass
  public static void tearDownAfterClass() {
  }

  @Before
  public void setUp() {

    openbisClient =
        new OpenBisClient(config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    openbisClient.login();
  }

  @After
  public void tearDown() {
    openbisClient.logout();
    openbisClient = null;
  }

  @Test
  public void testConnectedToAS() {
    openbisClient =
        new OpenBisClient(config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    assertThat(openbisClient.connectedToAS());
    openbisClient =
        new OpenBisClient(config.getProperty("someuser"), config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    assertThat(openbisClient.connectedToAS());
  }

  @Test
  public void testLoggedin() {
    openbisClient =
        new OpenBisClient(config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    openbisClient.login();
    assertThat(openbisClient.loggedin());

    openbisClient =
        new OpenBisClient("someuser", config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    assertThat(!openbisClient.loggedin());
  }

  @Test
  public void testLogout() {
    openbisClient =
        new OpenBisClient(config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    openbisClient.login();
    assertThat(openbisClient.loggedin());
    openbisClient.logout();
    assertThat(!openbisClient.loggedin());
  }

  @Test
  public void testLogin() {
    openbisClient =
        new OpenBisClient(config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    openbisClient.login();
    assertThat(openbisClient.loggedin());
    openbisClient.logout();
    openbisClient.login();
    assertThat(openbisClient.loggedin());
  }

  @Test
  public void testGetSessionToken() {
    openbisClient =
        new OpenBisClient(config.getProperty(DATASOURCE_USER), config.getProperty(DATASOURCE_PASS),
            config.getProperty(DATASOURCE_URL));
    openbisClient.login();
    openbisClient.loggedin();
    assertThat(openbisClient.getSessionToken().startsWith(config.getProperty(DATASOURCE_USER)));
    openbisClient.logout();
    assertThat(openbisClient.getSessionToken() == null);
  }

}