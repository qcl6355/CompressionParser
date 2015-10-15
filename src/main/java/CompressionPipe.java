import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Sheng Li
 *         Created on 14/12/8.
 */
public class CompressionPipe {
    public Alphabet dataAlphabet;
    public static final String Verb_String = "VA VC VE VV";
    public static final String Open_Brackets = "([{<（【《";
    public static final String Close_Brackets = ")]}>）】》";

    public Set<String> VERB_SET;

    public CompressionPipe() {
        this.dataAlphabet = new Alphabet();
        VERB_SET = new HashSet<String>();
        Collections.addAll(VERB_SET, Verb_String.split(" "));
    }

    public List<CompressionInstance> createInstances(String trainFile, String parseFile, String trainForest) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(trainForest));
        List<CompressionInstance> res = CompressionReader.read(trainFile, true, true);
        //DependencyReader.read(parseFile, res);
        for (CompressionInstance instance : res) {
            // build gold stand feature vectors
            FeatureVector fv = new FeatureVector(-1, 0.0, null);
            for (int i = 1; i < instance.labs.length; i++) {
                fv = createFeatureVector(instance, instance.labs[i - 1], instance.labs[i], fv);
            }
            instance.fv = fv;
        }
        for (CompressionInstance instance : res){
            possibleFeatures(instance, out); // add unsupported features
        }
        out.close();
        return res;
    }

    public FeatureVector createFeatureVector(CompressionInstance instance, int prev, int curr, FeatureVector fv) {
        String[] pos = instance.pos;
        String[] tokens = instance.words;
        DepTree[] deps = instance.deps;

        //word bigram
//        String pre_word = "unigram:" + tokens[prev];
//        fv = add(pre_word, 1.0, fv);
//        String curr_word = "unigram:" + tokens[curr];
//        fv = add(curr_word, 1.0, fv);
//        String comp_bigram = "bigram:" + tokens[prev] + tokens[curr];
//        fv = add(comp_bigram, 1.0, fv);

        // pos bigram
        String pos_bigram = "pos-bigram:" + pos[prev] + "-" + pos[curr];
        String prev_pos = "prev-pos:" + pos[prev];
        String curr_pos = "curr-pos:" + pos[curr];

        fv = add(pos_bigram, 1.0, fv);
        fv = add(prev_pos, 1.0, fv);
        fv = add(curr_pos, 1.0, fv);

        //pos context of current word (bigram + trigram)
        String cont_bigram = "context-bi:" + (curr > 1 ? pos[curr - 2] : "*DUMMY*") +
                (curr > 0 ? pos[curr - 1] : "*DUMMY*");
        fv = add(cont_bigram, 1.0, fv);

        String cont_trigram = "context-tri:" + (curr > 2 ? pos[curr - 3] : "*DUMMY*") +
                (curr > 1 ? pos[curr - 2] : "*DUMMY*") + (curr > 0 ? pos[curr - 1] : "*DUMMY*");
        fv = add(cont_trigram, 1.0, fv);

        //whether prev and curr word are adjacent is original sentence
        String adjacent = prev + 1 == curr ? "Adjacent" : "NotAdjacent";
        fv = add(adjacent, 1.0, fv);

        //combination
        fv = add(pos_bigram + adjacent, 1.0, fv);
        fv = add(prev_pos + adjacent, 1.0, fv);
        fv = add(curr_pos + adjacent, 1.0, fv);
        fv = add(cont_bigram + adjacent, 1.0, fv);
        fv = add(cont_trigram + adjacent, 1.0, fv);

        // dropped word
        int[] perfectMatch = new int[Open_Brackets.length()];
        for (int i = 0 ; i < perfectMatch.length; i++) perfectMatch[i] = 0;

        for (int i = prev + 1; i < curr; i++) {
            int oid = Open_Brackets.indexOf(tokens[i]);
            if (oid != -1) perfectMatch[oid] += 1;
            int cid = Close_Brackets.indexOf(tokens[i]);
            if (cid != -1) perfectMatch[cid] -= 1;

            String dpos = pos[i];
            fv = add("d-pos:" + dpos, 1.0, fv);
            fv = add("d-pos-conj:" + dpos + pos[prev] + pos[curr], 1.0, fv);
            if (this.VERB_SET.contains(dpos)) {
                fv = add("verb", 1.0, fv);
            }
            cont_bigram = "d-context-bi:" + (i > 1 ? pos[i - 2] : "*DUMMY*") +
                    (i > 0 ? pos[i - 1] : "*DUMMY*");
            fv = add(cont_bigram, 1.0, fv);

            cont_trigram = "d-context-tri:" + (i > 2 ? pos[i - 3] : "*DUMMY*") +
                    (i > 1 ? pos[i - 2] : "*DUMMY*") + (i > 0 ? pos[i - 1] : "*DUMMY*");
            fv = add(cont_trigram, 1.0, fv);
        }

        boolean match = true;
        for (int aPerfectMatch : perfectMatch) {
            if (aPerfectMatch != 0) {
                match = false;
                //System.out.println("miss match");
            }
        }
        fv = add("BracketsMatch:" + match, 1.0, fv);

        // dependency features
        /*for (int i = prev + 1; i < curr; i++) {
            if (i <= 0 || i >= instance.size() - 1) continue;
            DepTree dep = deps[i];
            String headPos;
            if (dep.head == 0) {
                headPos = "root";
            } else if (dep.head == -1) {
                headPos = "null";
            } else {
                headPos = pos[dep.head];
            }
            fv = add("DDdepHeadPos:" + headPos, 1.0, fv);
            fv = add("DDdepHeadConj:" + headPos + pos[i], 1.0, fv);
            fv = add("DDIsDepLeaf:" + dep.isLeaf(), 1.0, fv);
        }

        int[] ccs = new int[]{prev, curr};
        for (int i : ccs) {
            if (i <= 0 || i >= instance.size() - 1) continue;
            DepTree ccDep = deps[i];
            String ccHeadPos;
            if (ccDep.head == 0) {
                ccHeadPos = "root";
            } else if (ccDep.head == -1) {
                ccHeadPos = "null";
            } else {
                ccHeadPos = pos[ccDep.head];
            }
            fv = add("CCdepHeadPos:" + ccHeadPos, 1.0, fv);
            fv = add("CCdepHeadConj:" + ccHeadPos + pos[i], 1.0, fv);
            fv = add("CCIsDepLeaf:" + ccDep.isLeaf(), 1.0, fv);
        }*/

        return fv;
    }

    public void possibleFeatures(CompressionInstance instance, ObjectOutputStream out) throws IOException {
        for (int i = 1; i < instance.size(); i++) {
            for (int j = i - 1; j >= 0; j--) {
                FeatureVector fv = createFeatureVector(instance, j, i, new FeatureVector(-1, 0.0, null));
                while (fv.next != null) {
                    if (fv.index >= 0) {
                        out.writeInt(fv.index);
                    }
                    fv = fv.next;
                }
                out.writeInt(-2);
            }
        }
        out.writeInt(-3);
    }

    private FeatureVector add(String feat, double val, FeatureVector fv) {
        int index = dataAlphabet.lookupIndex(feat);
        if (index >= 0) {
            return new FeatureVector(index, val, fv);
        }
        return fv;
    }

    public void getFeatureVector(ObjectInputStream in, CompressionInstance instance,
                                 FeatureVector[][] feat_edge,
                                 double[][] score_edge,
                                 Parameters para) throws IOException {

        for (int i = 1; i < instance.size(); i++) {
            for (int j = i - 1; j >= 0; j--) {
                FeatureVector fv = new FeatureVector(-1, 0.0, null);
                int idx = in.readInt();
                while (idx != -2) {
                    fv = new FeatureVector(idx, 1.0, fv);
                    idx = in.readInt();
                }
                double score = para.getScore(fv);
                feat_edge[j][i] = fv;
                score_edge[j][i] = score;
            }
        }
        int last = in.readInt();
        if (last != -3){
            System.err.println("Error Reading File!");
            System.exit(0);
        }
    }

    public void getFeatureVector(CompressionInstance instance,
                                          FeatureVector[][] feat_edge,
                                          double[][] score_edge,
                                          Parameters para) {
        for (int i = 1; i < instance.size(); i++) {
            for (int j = i - 1; j >= 0; j--) {
                FeatureVector fv = new FeatureVector();
                fv = createFeatureVector(instance, j, i, fv);
                feat_edge[j][i] = fv;
                score_edge[j][i] = para.getScore(fv);
            }
        }
    }

    public void closeAlphabets(){
        this.dataAlphabet.stopGrowth();
    }
}
