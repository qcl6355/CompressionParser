import gnu.trove.TIntDoubleHashMap;

/**
 * @author Sheng Li
 *         Created on 14/12/8.
 */
public class FeatureVector {
    int index;
    double value;
    FeatureVector next;

    public FeatureVector(int i, double v, FeatureVector n){
        this.index = i;
        this.value = v;
        this.next = n;
    }

    public FeatureVector(int i, double v){
        this.index = i;
        this.value = v;
        this.next = null;
    }

    public FeatureVector(){
        this.index = -1;
        this.value = 0.0;
        this.next = null;
    }

    public static FeatureVector cat(FeatureVector fv1, FeatureVector fv2) {
        FeatureVector fv = new FeatureVector();
        for (FeatureVector curr = fv1; curr != null; curr = curr.next) {
            if (curr.index >= 0){
                fv = new FeatureVector(curr.index, curr.value, fv);
            }
        }

        for (FeatureVector curr = fv2; curr != null; curr = curr.next) {
            if (curr.index >= 0){
                fv = new FeatureVector(curr.index, curr.value, fv);
            }
        }
        return fv;
    }

    public static FeatureVector getDistFeatureVector(FeatureVector fv1, FeatureVector fv2) {
        FeatureVector fv = new FeatureVector();
        for (FeatureVector curr = fv1; curr != null; curr = curr.next) {
            if (curr.index >= 0){
                fv = new FeatureVector(curr.index, curr.value, fv);
            }
        }

        for (FeatureVector curr = fv2; curr != null; curr = curr.next) {
            if (curr.index >= 0){
                fv = new FeatureVector(curr.index, -curr.value, fv);
            }
        }
        return fv;
    }

    public static double dotProduct(FeatureVector fv1, FeatureVector fv2) {
        TIntDoubleHashMap hm1 = new TIntDoubleHashMap();
        TIntDoubleHashMap hm2 = new TIntDoubleHashMap();
        for (FeatureVector curr = fv1; curr != null; curr = curr.next){
            if (curr.index >= 0) {
                hm1.adjustOrPutValue(curr.index, curr.value, curr.value);
            }
        }
        for (FeatureVector curr = fv2; curr != null; curr = curr.next){
            if (curr.index >= 0) {
                hm2.adjustOrPutValue(curr.index, curr.value, curr.value);
            }
        }

        double score = 0.0;
        for (int key : hm1.keys()) {
            score += hm1.get(key) * hm2.get(key);
        }
        return score;
    }
}
