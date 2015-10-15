
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Sheng Li
 *         Created on 14/12/8.
 */
public class CompressionDecoder {
    /**
     *
     * @param instance
     * @param feat_edge
     * @param score_edge
     * @param k_best
     * @return
     */
    public Object[][] dynamic_search(CompressionInstance instance,
                                     FeatureVector[][] feat_edge,
                                     double[][] score_edge,
                                     int k_best) {
        int size = instance.size();
        Object[][][] dp_table = new Object[size][k_best][3];

        for (int i = 0; i < size; i++){
            for (int j = 0; j < k_best; j++){
                dp_table[i][j][0] = Double.NaN;
                dp_table[i][j][1] = -1;
                dp_table[i][j][2] = -1;
            }
        }

        // init table
        dp_table[0][0][0] = 0.0;

        // filling table
        for (int i = 1; i < size; i++){
            List<Node> candidate = new ArrayList<Node>();
            for (int j = i - 1; j >= 0; j--){
                for (int k = 0; k < k_best; k++) {
                    if (!Double.isNaN((Double)dp_table[j][k][0])) {
                        // if dp_table[j][k][0] is NaN, it indicates an unconnected path
                        double s = score_edge[j][i] + (Double)dp_table[j][k][0];
                        candidate.add(new Node(s, j, k));
                    }
                }
            }

            // select k-best candidate
            Collections.sort(candidate);
            for (int c = 0; c < candidate.size() && c < k_best; c++) {
                Node n = candidate.get(c);
                dp_table[i][c][0] = n.score;
                dp_table[i][c][1] = n.parent;
                dp_table[i][c][2] = n.which;
            }
        }

        //back trace result
        Object[][] res = new Object[k_best][2];
        int num = 0;
        for (int k = 0; k < k_best; k++){
            if (!Double.isNaN((Double)dp_table[size - 1][k][0])) {
                //System.out.println(dp_table[size - 1][k][0]);
                FeatureVector fv = new FeatureVector();
                int parent = size - 1;
                int which = k;
                List<Integer> parse = new ArrayList<Integer>();
                parse.add(parent);
                while (parent != -1){
                    int t_p = (Integer)dp_table[parent][which][1];
                    int t_k = (Integer)dp_table[parent][which][2];
                    if (t_p == -1) break;
                    fv = FeatureVector.cat(fv, feat_edge[t_p][parent]);
                    parse.add(t_p);
                    parent = t_p;
                    which = t_k;
                }
                res[num][0] = fv;
                Collections.reverse(parse);
                res[num][1] = parse;
                num++;
            }
        }
        return res;
    }

    /**
     *
     * @param instance
     * @param feat_edge
     * @param score_edge
     * @param k_best
     * @param rate specify a compression rate (e.g.,rate= 0.75)
     * @return
     */
    public Object[][] dynamic_search(CompressionInstance instance,
                                     FeatureVector[][] feat_edge,
                                     double[][] score_edge,
                                     int k_best,
                                     double rate){
        int size = instance.size();
        int maxLength = (int) (size * rate);

        Object[][][][] dp_table = new Object[size][maxLength][k_best][3];

        for (int i = 0; i < size; i++){
            for (int r = 0; r < maxLength; r++){
                for (int j = 0; j < k_best; j++) {
                    dp_table[i][r][j][0] = Double.NaN;
                    dp_table[i][r][j][1] = -1;
                    dp_table[i][r][j][2] = -1;
                }
            }
        }

        // init table
        dp_table[0][0][0][0] = 0.0;

        // filling table
        for (int i = 1; i < size; i++){
            for (int r = 1; r <= i && r < maxLength; r++) { // it can't exceed i
                List<Node> candidate = new ArrayList<Node>();
                for (int j = i - 1; j >= 0; j--){
                    for (int k = 0; k < k_best; k++) {
                        if (!Double.isNaN((Double) dp_table[j][r - 1][k][0])) {
                            // if dp_table[j][r][k][0] is NaN, it indicates an unconnected path
                            double s = score_edge[j][i] + (Double) dp_table[j][r - 1][k][0];
                            candidate.add(new Node(s, j, k));
                        }
                    }
                }

                // select k-best candidate
                Collections.sort(candidate);
                for (int c = 0; c < candidate.size() && c < k_best; c++) {
                    Node n = candidate.get(c);
                    dp_table[i][r][c][0] = n.score;
                    dp_table[i][r][c][1] = n.parent;
                    dp_table[i][r][c][2] = n.which;
                }
            }
        }

        //back trace result
        Object[][] res = new Object[k_best][2];
        List<Tuple> tmpCandidate = new ArrayList<Tuple>();
        for (int r = maxLength - 1; r >= 1; r--) {
            for (int k = 0; k < k_best; k++) {
                double score = (Double) dp_table[size - 1][r - 1][k][0];
                if (!Double.isNaN(score)) {
                    //System.out.println(dp_table[size - 1][k][0]);
                    FeatureVector fv = new FeatureVector();
                    int parent = size - 1;
                    int which = k;
                    int tr = r;
                    List<Integer> parse = new ArrayList<Integer>();
                    parse.add(parent);
                    while (parent != -1) {
                        int t_p = (Integer) dp_table[parent][tr][which][1];
                        int t_k = (Integer) dp_table[parent][tr][which][2];
                        if (t_p == -1) break;
                        fv = FeatureVector.cat(fv, feat_edge[t_p][parent]);
                        parse.add(t_p);
                        parent = t_p;
                        which = t_k;
                        tr -= 1;
                    }
                    Collections.reverse(parse);
                    tmpCandidate.add(new Tuple(score, fv, parse));
                }
            }
        }

        Collections.sort(tmpCandidate);
        for (int i = 0; i < tmpCandidate.size() && i < k_best; i++){
            res[i][0] = tmpCandidate.get(i).fv;
            res[i][1] = tmpCandidate.get(i).parse;
        }
        return res;

    }

    class Tuple implements Comparable{
        List<Integer> parse;
        FeatureVector fv;
        double score;

        public Tuple(double s, FeatureVector f, List<Integer> p) {
            score = s;
            fv = f;
            parse = p;
        }

        public int compareTo(Object o) {
            if (this.score < ((Tuple)o).score){
                return 1;
            } else if (this.score > ((Tuple)o).score) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    class Node implements Comparable{
        double score;
        int parent;
        int which;

        public Node(double s, int j, int k) {
            score = s;
            parent = j;
            which = k;
        }

        public int compareTo(Object o) {
            if (this.score < ((Node)o).score){
                return 1;
            } else if (this.score > ((Node)o).score) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
