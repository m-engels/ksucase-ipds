/**
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
 */
package edu.ksu.cis.macr.ipds.node.utils.generate;

import java.io.*;

/**
 The {@code JavaGenerator}
 */
public class JavaGenerator {
  private static final String JAVA_EXT = ".java";
  private static final String IPDS_SIM_DIR = System.getProperty("user.dir");
  private static final String TEMPLATE_DIR = IPDS_SIM_DIR
          + "/src/main/java/edu/ksu/cis/macr/ipds/simulation/utils/generate/templates/";
  private static final String PLANS_TEMPLATE = TEMPLATE_DIR
          + "plans.template";
  private static final String INIT_TEMPLATE = TEMPLATE_DIR + "init.template";
  private static final String WORK_TEMPLATE = TEMPLATE_DIR + "work.template";
  private static final String STOP_TEMPLATE = TEMPLATE_DIR + "stop.template";
  private static final String IPDS_DIR = IPDS_SIM_DIR.replace("_simulator",
          "");
  private static final String PLANS_DIR = IPDS_DIR
          + "/src/main/java/edu/ksu/cis/macr/ipds/plans/";

  /*
   * Create a new file.
   */
  public synchronized void CreateFile(File file) throws IOException {
    file.createNewFile();
  }

  /*
   * Creates a new Folder if it doesn't exist, returns a boolean indicating if
   * it was successful.
   */
  public Boolean CreateFolder(File folder) {
    return folder.mkdirs();
  }

  /*
   * Check if a file already exists or not.
   */
  public Boolean FileExists(final File file) {
    return file.exists();
  }

  /*
   * Generates a java file after reading in the template file and replacing
   * the goal names in the appropriate places.
   */
  public synchronized void GeneratePlanFile(String goalName) {
    try {
      File planTemplate = new File(PLANS_TEMPLATE);
      String temp = ReadFile(planTemplate);
      temp = ReplaceName(temp, goalName);

      // Create Directory in plans folder
      CreateFolder(new File(PLANS_DIR + goalName.replace(" ", "")));
      File newFile = new File(PLANS_DIR + goalName.replace(" ", "") + "/"
              + goalName.replace(" ", "") + "Plan" + JAVA_EXT);

      if (!FileExists(newFile)) {
        CreateFile(newFile);
      }
      WriteFile(temp, newFile);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * Generates the three state files (Init, Work, & Stop.java)
   *
   * Called after the Plane File has been generated
   */
  public synchronized void GenerateStateFiles(String goalName) {

    try {
      File initTemplate = new File(INIT_TEMPLATE);
      String initTemp = ReadFile(initTemplate);
      initTemp = ReplaceName(initTemp, goalName);
      File workTemplate = new File(WORK_TEMPLATE);
      String workTemp = ReadFile(workTemplate);
      workTemp = ReplaceName(workTemp, goalName);
      File stopTemplate = new File(STOP_TEMPLATE);
      String stopTemp = ReadFile(stopTemplate);
      stopTemp = ReplaceName(stopTemp, goalName);

      File initFile = new File(PLANS_DIR + goalName.replace(" ", "")
              + "/Init" + JAVA_EXT);
      File workFile = new File(PLANS_DIR + goalName.replace(" ", "")
              + "/Work" + JAVA_EXT);
      File stopFile = new File(PLANS_DIR + goalName.replace(" ", "")
              + "/Stop" + JAVA_EXT);

      if (!FileExists(initFile)) {
        CreateFile(initFile);
      }
      if (!FileExists(workFile)) {
        CreateFile(workFile);
      }
      if (!FileExists(stopFile)) {
        CreateFile(stopFile);
      }

      WriteFile(initTemp, initFile);
      WriteFile(workTemp, workFile);
      WriteFile(stopTemp, stopFile);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * Reads in a file and returns it as a String.
   */
  public String ReadFile(File in) throws IOException {
    char[] buf = null;
    try (FileReader inReader = new FileReader(in); BufferedReader bR = new BufferedReader(
            inReader)) {
      buf = new char[(int) in.length()];
      int i = 0;
      int c = bR.read();
      while (c != -1) {
        buf[i++] = (char) c;
        c = bR.read();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return new String(buf);
  }

  /*
   * Replaces Goal names in the template string.
   *
   * [[[NAMEHERE]]] indicates a spot where the format GoalName would be used.
   * [[[NAME_HERE]]] indicates a spot where the format Goal_Name would be
   * used.
   *
   * [[NAME_HERE_]] indicates a spot where the format Goal_Name_ would be
   * used.
   */
  public String ReplaceName(String file, String name) {
    file = file.replace("[[[NAMEHERE]]]", name.replace(" ", ""));
    file = file.replace("[[[NAME_HERE]]]", name.replace(" ", "_"));
    return file.replace("[[NAME_HERE_]]", name.replace(" ", "_") + "_");
  }

  /*
   * Writes a String to a File.
   */
  public synchronized void WriteFile(String file, File in) {
    try {
      BufferedWriter bW = new BufferedWriter(new FileWriter(in));
      bW.write(file);
      bW.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
