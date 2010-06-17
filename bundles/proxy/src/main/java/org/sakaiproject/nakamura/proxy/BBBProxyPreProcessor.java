package org.sakaiproject.nakamura.proxy;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.ComponentContext;
import org.sakaiproject.nakamura.api.proxy.ProxyPreProcessor;
import org.sakaiproject.nakamura.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Dictionary;
import java.util.Map;
import java.util.Map.Entry;

@Service(value = ProxyPreProcessor.class)
@Component(name = "BBBProxyPreProcessor", label = "ProxyPreProcessor for BBB", description = "Pre processor for BBB requests.", immediate = true, metatype = true)
@Properties(value = {
    @Property(name = "service.vendor", value = "The Sakai foundation"),
    @Property(name = "service.description", value = "Pre processor who removes all headers from the request.") })
public class BBBProxyPreProcessor implements ProxyPreProcessor {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(BBBProxyPreProcessor.class);

  @Property(value = "5af2bf4adbb742573154c206c0af6ad6")
  static final String bbbSalt = "bbb.salt";

  private static String SALT;

  @Property(value = "10.0.2.15")
  static final String bbbHost = "bbb.host";

  private static String HOST;

  public String getName() {
    return "bbb";
  }

  public void preProcessRequest(SlingHttpServletRequest request,
      Map<String, String> headers, Map<String, Object> templateParams) {
    @SuppressWarnings("unchecked")
    Map<String, String[]> params = request.getParameterMap();
    // construct the string to be summed, should look like
    // apiMethod + queryString
    String apiMethod = params.get("apiMethod")[0];
    String queryString = "";
    for (Entry<String, String[]> e : params.entrySet()) {
      if (!e.getKey().equals("apiMethod")) {
        queryString += e.getKey() + "=" + e.getValue()[0] + "&";
      }
    }
    // trim trailing '&'
    queryString = queryString.substring(0, queryString.length() - 1);
    // compute and append the checksum
    String checksum = computeChecksum(apiMethod + queryString);
    queryString += "&checksum=" + checksum;
    templateParams.put("hostname", HOST);
    templateParams.put("apiMethod", apiMethod);
    templateParams.put("queryString", queryString);
    LOGGER.info(queryString);
  }

  private String computeChecksum(String toSum) {
    String result = toSum + SALT;
    try {
      result = StringUtils.sha1Hash(result);
    } catch (UnsupportedEncodingException e) {
      LOGGER.error("Error computing checksum: ", e);
    } catch (NoSuchAlgorithmException e) {
      LOGGER.error("Error computing checksum: ", e);
    }
    return result;
  }

  public void activate(ComponentContext context) {
    @SuppressWarnings("unchecked")
    Dictionary props = context.getProperties();

    String _salt = (String) props.get(bbbSalt);
    if (_salt != null) {
      if (diff(SALT, _salt)) {
        SALT = _salt;
      }
    } else {
      LOGGER.error("Salt not set.");
    }

    String _host = (String) props.get(bbbHost);
    if (_host != null) {
      if (diff(HOST, _host)) {
        HOST = _host;
      }
    } else {
      LOGGER.error("Host not set.");
    }
  }

  /**
   * Determine if there is a difference between two objects.
   * 
   * @param obj1
   * @param obj2
   * @return true if the objects are different (only one is null or !obj1.equals(obj2)).
   *         false otherwise.
   */
  private boolean diff(Object obj1, Object obj2) {
    boolean diff = true;

    boolean bothNull = obj1 == null && obj2 == null;
    boolean neitherNull = obj1 != null && obj2 != null;

    if (bothNull || (neitherNull && obj1.equals(obj2))) {
      diff = false;
    }
    return diff;
  }
}
