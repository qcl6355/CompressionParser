import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sheng Li
 *         Created on 14/12/15.
 */
public class DependencyReader {
    public static void read(String file, List<CompressionInstance> instanceList) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                "UTF-8"));
        String line;
        List<String> deps = new ArrayList<String>();
        List<String> parsed = new ArrayList<String>();
        int idx = 0;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("(")) {
                parsed.add(line);
            } else if (line.equals("")) {
                CompressionInstance instance = instanceList.get(idx);
                instance.deps = DepTree.parse(deps, instance.size());
                deps = new ArrayList<String>();
                idx++;
            } else {
                deps.add(line);
            }
        }
        br.close();
    }
}
