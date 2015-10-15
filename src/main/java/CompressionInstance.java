/**
 * @author Sheng Li
 *         Created on 14/12/8.
 */
public class CompressionInstance {
    String[] words;
    String[] pos;
    int[] labs;
    DepTree[] deps;
    FeatureVector fv;

    public CompressionInstance(String[] w, String[] p, int[] ls) {
        this.words = w;
        this.pos = p;
        this.labs = ls;
        fv = null;
    }

    public int size(){
        return words.length;
    }

    @Override
    public String toString() {
        String res = "";
        for (String astr : words) {
            res += astr + " ";
        }
        return res;
    }
}
