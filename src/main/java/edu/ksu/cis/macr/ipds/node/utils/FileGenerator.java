/**
 * Copyright 2012 Kansas State University MACR Laboratory
 * http://macr.cis.ksu.edu/ Department of Computing & Information Sciences
 *
 * See License.txt file for the license agreement.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package edu.ksu.cis.macr.ipds.node.utils;

import edu.ksu.cis.macr.ipds.node.utils.generate.JavaGenerator;

import javax.swing.*;
import java.util.List;

/**
 The {@code FileGenerator}
 */
public class FileGenerator {

  public static final JavaGenerator javaGen = new JavaGenerator();
  public static final XmlReader xmlRead = new XmlReader();

  public static void main(String[] args) {
    List<String> goalList = xmlRead
            .ReadGoalModels(xmlRead.FindGoalModels());

    for (String goal : goalList) {
      JOptionPane.showMessageDialog(null, "Generating files for goal: "
              + goal);
      javaGen.GeneratePlanFile(goal);
      javaGen.GenerateStateFiles(goal);
    }
    JOptionPane.showMessageDialog(null, "Done!");
  }
}
