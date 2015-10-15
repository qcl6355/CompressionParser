import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sheng Li
 *         Created on 14/12/8.
 */
public class CompressionReader {
    public static final  String FILE_ENCODE = "UTF-8";

    public static List<CompressionInstance> read(String inputFile, boolean preprocess, boolean train) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),
                FILE_ENCODE));
        List<CompressionInstance> instanceList = new ArrayList<CompressionInstance>();

        String[][] lines = getLines(br, train);
        while (lines != null) {
            if (preprocess) {
                lines = preProcess(lines);
            }
            String[] words = lines[0];
            String[] pos = lines[1];
            String[] ls = lines[2];
            int[] labs = null;
            if (ls != null) {
                labs = new int[ls.length];
                for (int i = 0; i < ls.length; i++) {
                    labs[i] = Integer.parseInt(ls[i]);
                }
            }
            instanceList.add(new CompressionInstance(words, pos, labs));
            lines = getLines(br, train);
        }

        br.close();
        return instanceList;
    }

    private static String[][] getLines(BufferedReader in, boolean train) throws IOException {
        String line = in.readLine();
        String posLine = in.readLine();
        String labsLine = null;
        if (train) labsLine = in.readLine();
        in.readLine(); //read blank line

        if (line == null) return null;

        String[] words = line.trim().split("\\s+");
        String[] pos = posLine.trim().split("\\s+");
        String[] labs = null;
        if (train) {
            labs = labsLine.trim().split("\\s+");
        }
        String[][] res = new String[3][];
        res[0] = words; res[1] = pos; res[2] = labs;
        return res;
    }

    private static String[][] preProcess(String[][] original) {
        String[] newWords = new String[original[0].length + 2];
        String[] newPos = new String[original[1].length + 2];
        String[] newLabs = null;
        for (int i = 0; i < newWords.length; i++) {
            if (i == 0){
                newWords[i] = "*START*";
            } else if (i == newWords.length - 1) {
                newWords[i] = "*STOP*";
            } else {
                newWords[i] = original[0][i - 1];
            }
        }

        for (int i = 0; i < newPos.length; i++) {
            if (i == 0){
                newPos[i] = "*DUMMY*";
            } else if (i == newPos.length - 1) {
                newPos[i] = "*DUMMY*";
            } else {
                newPos[i] = original[1][i - 1];
            }
        }

        if (original[2] != null) {
            newLabs = new String[original[2].length + 2];
            for (int i = 0; i < newLabs.length; i++){
                if (i == 0){
                    newLabs[i] = "0";
                } else if (i == newLabs.length - 1){
                    newLabs[i] = "" + (newWords.length - 1);
                } else {
                    newLabs[i] = original[2][i-1];
                }
            }
        }

        return new String[][]{newWords, newPos, newLabs};
    }

    public static void main(String[] args) throws IOException {
        Options op = new Options();
        for (CompressionInstance instance : read(op.trainFile, true, true)) {
            System.out.println(instance);
        }
    }
}
