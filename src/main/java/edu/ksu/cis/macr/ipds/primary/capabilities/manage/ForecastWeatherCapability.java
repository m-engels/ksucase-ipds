/*
 * Copyright 2012 
 * Kansas State University MACR Laboratory http://macr.cis.ksu.edu/
 * Department of Computing & Information Sciences
 * 
 * See License.txt file for the license agreement. 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * this file uses code from 
 * usmanali112.blogspot.com/2012/07/java-weather-underground-api.html
 *
 */
package edu.ksu.cis.macr.ipds.primary.capabilities.manage;

import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.GregorianCalendar;

/**
 The {@code ForecastWeatherCapability} will provide information about ambient temperature in degree F (affects load) and
 cloud cover (affects PV generation) as a percent of opaque clouds based on location and time of year/day.
 */
public class ForecastWeatherCapability extends AbstractOrganizationCapability {

  private static final Logger LOG = LoggerFactory.getLogger(ForecastWeatherCapability.class);
  private final int SUN_UP = 6;
  private final int SUN_DOWN = 16;  // 4 p.m. on 24-hour clock
  private String strLocation;

  /**
   Construct a new {@code ForecastWeatherCapability} instance without the special attributes defined in agent.xml.

   @param owner - the agent possessing this capability.
   @param organization - the immediate organization in which this agent operates.
   */
  public ForecastWeatherCapability(final IPersona owner,
                                   final IOrganization organization) {
    super(ForecastWeatherCapability.class, owner, organization);
  }


  /**
   Construct a new {@code ForecastWeatherCapability} instance with the special attributes defined in agent.xml.

   @param owner - the agent possessing this capability.
   @param organization - the immediate organization in which this agent operates.
   @param strLocation - the general location string for weather forecasting.
   */
  public ForecastWeatherCapability(final IPersona owner,
                                   final IOrganization organization, final String strLocation, final GregorianCalendar now) {  // TODO: be more precise with weather location
    super(ForecastWeatherCapability.class, owner, organization);

    // set capability special attributes (passed in above and declared in agent.xml)
    setLocation(strLocation);
  }

  @Override
  public double getFailure() {
    return 0;
  }

  public synchronized void getWeatherForecast() {
    try {
      URL googleWeatherXml = new URL("http://api.wunderground.com/api/b4d25ec018c3972b/forecast/q/KS/Manhattan.xml");

      //If you are using proxy only then use this code
      //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.1.11.11", 8080)); // or whatever your proxy is
      //HttpURLConnection uc = (HttpURLConnection)googleWeatherXml.openConnection(proxy);

      URLConnection uc = googleWeatherXml.openConnection();
      uc.connect();

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(uc.getInputStream());
      doc.getDocumentElement().normalize();


      // To print whole xml file
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      //initialize StreamResult with File object to save to file
      StreamResult result = new StreamResult(new StringWriter());
      DOMSource source = new DOMSource(doc);
      try {
        transformer.transform(source, result);
      } catch (TransformerException ex) {
        LOG.error(ex.getMessage());
      }

      String xmlString = result.getWriter().toString();
      LOG.debug(xmlString);

      NodeList forecast = doc.getElementsByTagName("forecast");
      for (int a = 0; a < forecast.getLength(); a++) {
        Node forecastNode = forecast.item(a);

        if (forecastNode.getNodeType() == Node.ELEMENT_NODE) {
          Element forecastElement = (Element) forecastNode;

          NodeList simpleforecast = forecastElement.getElementsByTagName("simpleforecast");

          for (int b = 0; b < simpleforecast.getLength(); b++) {
            Node simpleforecastNode = simpleforecast.item(b);

            if (simpleforecastNode.getNodeType() == Node.ELEMENT_NODE) {
              Element simpleforecastElement = (Element) simpleforecastNode;

              NodeList forecastdays = simpleforecastElement.getElementsByTagName("forecastdays");

              for (int c = 0; c < forecastdays.getLength(); c++) {
                Node forecastdaysNode = forecastdays.item(c);

                if (forecastdaysNode.getNodeType() == Node.ELEMENT_NODE) {
                  Element forecastdaysElement = (Element) forecastdaysNode;

                  NodeList forecastday = forecastdaysElement.getElementsByTagName("forecastday");

                  for (int d = 0; d < forecastday.getLength(); d++) {
                    Node forecastdayNode = forecastday.item(d);

                    if (forecastdayNode.getNodeType() == Node.ELEMENT_NODE) {
                      Element forecastdayElement = (Element) forecastdayNode;

                      NodeList date = forecastdayElement.getElementsByTagName("date");

                      for (int e = 0; e < date.getLength(); e++) {
                        Node dateNode = date.item(e);

                        if (dateNode.getNodeType() == Node.ELEMENT_NODE) {
                          Element dateElement = (Element) dateNode;

                          NodeList weekday = dateElement.getElementsByTagName("weekday");
                          Element day = (Element) weekday.item(0);
                          LOG.debug("Day of weekday: {}", day.getFirstChild().getNodeValue());
                        }
                      }

                      NodeList high = forecastdayElement.getElementsByTagName("high");

                      for (int f = 0; f < high.getLength(); f++) {
                        Node highNode = high.item(f);

                        if (highNode.getNodeType() == Node.ELEMENT_NODE) {
                          Element highElement = (Element) highNode;
                          NodeList celsius = highElement.getElementsByTagName("celsius");
                          Element cel = (Element) celsius.item(0);
                          LOG.debug("High cel: {}", cel.getFirstChild().getNodeValue());
                        }
                      }

                      NodeList low = forecastdayElement.getElementsByTagName("low");

                      for (int g = 0; g < low.getLength(); g++) {
                        Node lowNode = low.item(g);

                        if (lowNode.getNodeType() == Node.ELEMENT_NODE) {
                          Element lowElement = (Element) lowNode;
                          NodeList celsius = lowElement.getElementsByTagName("celsius");
                          Element cel = (Element) celsius.item(0);
                          LOG.debug("Low cel: {}", cel.getFirstChild().getNodeValue());
                        }
                      }

                      NodeList avewind = forecastdayElement.getElementsByTagName("avewind");

                      for (int h = 0; h < avewind.getLength(); h++) {
                        Node avewindNode = avewind.item(h);

                        if (avewindNode.getNodeType() == Node.ELEMENT_NODE) {
                          Element avewindElement = (Element) avewindNode;
                          NodeList mph = avewindElement.getElementsByTagName("mph");
                          Element mp = (Element) mph.item(0);
                          LOG.debug("mph: {}", mp.getFirstChild().getNodeValue());

                          NodeList kph = avewindElement.getElementsByTagName("kph");
                          Element kp = (Element) kph.item(0);
                          LOG.debug("kph: {}", kp.getFirstChild().getNodeValue());

                          NodeList dir = avewindElement.getElementsByTagName("dir");
                          Element dr = (Element) dir.item(0);
                          LOG.debug("Dir: {}", dr.getFirstChild().getNodeValue());

                          NodeList degrees = avewindElement.getElementsByTagName("degrees");
                          Element deg = (Element) degrees.item(0);
                          LOG.debug("Degree: {}", deg.getFirstChild().getNodeValue());
                        }
                      }

                      NodeList conditions = forecastdayElement.getElementsByTagName("conditions");
                      Element con = (Element) conditions.item(0);
                      LOG.debug("conditions: {}", con.getFirstChild().getNodeValue());

                      NodeList avehumidity = forecastdayElement.getElementsByTagName("avehumidity");
                      Element ave = (Element) avehumidity.item(0);
                      LOG.debug("avehumidity: {}", ave.getFirstChild().getNodeValue());
                    }
                  }
                }
              }
            }
          }
        }
      }
    } catch (Exception ex) {
      LOG.debug(ex.getMessage());
    }


  }


  @Override
  public synchronized void reset() {

  }

  /**
   @param value - the string name of the location
   */
  public synchronized void setLocation(final String value) {
    this.strLocation = value;

  }

  @Override
  public Element toElement(final Document document) {
    final Element capability = super.toElement(document);
    Element parameter = (Element) capability.appendChild(document
            .createElement(ELEMENT_PARAMETER));
    parameter.setAttribute(ATTRIBUTE_TYPE,
            int.class.getSimpleName());
    parameter.appendChild(document.createTextNode(this.strLocation));

    return capability;
  }

  @Override
  public String toString() {
    return "ForecastWeatherCapability [strLocation="
            + this.strLocation
            + "]";
  }

  /**
   @return the number of projected minutes to consider when planning
   */
  public double getPlanningHorizon_minutes() {
//    try {
//      planningHorizon_minutes = Integer.getInteger(RunManager.getValue("planningHorizon_minutes")).doubleValue();
//    } catch (Exception e) {
//      LOG.error("ERROR: Could not parse integer planning horizon in minutes from {}. Please check run" +
//              ".properties.", planningHorizon_minutes);
//      planningHorizon_minutes = 15;  // use 0 minute planning horizon if it can't be read
//    }
//    return planningHorizon_minutes;
    return 15;
  }

  public enum cloudCover {FULL_SUN, FULL_CLOUD, INTERMITTENT}
}
