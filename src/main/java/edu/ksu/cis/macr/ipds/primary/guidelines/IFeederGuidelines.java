/**
 * Copyright 2014
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
 */
package edu.ksu.cis.macr.ipds.primary.guidelines;


/**
 The {@code IFeederGuidelines} provide information about the guidelines for power quality for this participant in the
 organization.
 */
public interface IFeederGuidelines {

  double getMaxKW();

  void setMaxKW(double kw);

  /**
   Get the maximum voltage multiplier (e.g. 1.05) allowed for all participants in a local organization.

   @return double
   */
  double getMaxVoltageMultiplier();

  /**
   Set the maximum voltage multiplier (e.g. 1.05) allowed for all participants in a local organization.

   @param maxVoltageMultiplier - the maximum voltage multiplier
   */
  void setMaxVoltageMultiplier(double maxVoltageMultiplier);

  double getMinKW();

  void setMinKW(double kw);

  /**
   Get the minimum voltage multiplier (e.g. 0.95) allowed for all participants in a local organization.

   @return double - the minimum voltage multiplier
   */
  double getMinVoltageMultiplier();

  /**
   Set the minimum voltage multiplier (e.g. 0.95) allowed for all participants in a local organization.

   @param minVoltageMultiplier - the minimum voltage multiplier (e.g. 0.95)
   */
  void setMinVoltageMultiplier(double minVoltageMultiplier);

  /**
   Get the net delta P for the community request.  A positive net delta P is a request for additional smart inverter
   reactive power.  A negative net delta P is a request to reduce reactive power.

   @return - the net delta P (the net change in real power generation) in kW.
   */
  double getNetDeltaP();

  /**
   Set the net delta P for the community request. A positive net delta P is a request for additional smart inverter
   reactive power.  A negative net delta P is a request to reduce reactive power.

   @param netDeltaP - the net delta P (the net change in real power generation) in kW.
   */
  void setNetDeltaP(double netDeltaP);

  /**
   @return the specificationFilePath
   */
  String getSpecificationFilePath();

  /**
   @param specificationFilePath the specificationFilePath to set
   */
  void setSpecificationFilePath(String specificationFilePath);
}
