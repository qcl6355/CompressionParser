import java.util.List;

/**
 * @author Sheng Li
 *         Created on 14/12/8.
 */
public class Parameters {
    public double[] parameters;
    public double[] total;

    public Parameters(int size){
        this.parameters = new double[size + 1];
        this.total = new double[size + 1];
        for (int i = 0; i < size + 1; i++){
            this.parameters[i] = this.total[i] = 0.0;
        }
    }

    public void averageParameters(long divide) {
        for (int i = 0; i < total.length; i++) {
            total[i] /= divide;
        }
        this.parameters = total;
    }

    public double getScore(FeatureVector fv) {
        double score = 0.0;
        for (FeatureVector curr = fv; curr != null; curr = curr.next) {
            if (curr.index >= 0){
                score += this.parameters[curr.index] *  curr.value;
            }
        }
        return score;
    }

    public void updateParametersMIRA(CompressionInstance instance, Object[][] d, double upd) {
        int K = 0;
        for (int i = 0; i < d.length && d[i][0] != null; i++){
            K++;
        }

        double[] b = new double[K];
        FeatureVector[] dist = new FeatureVector[K];

        for (int i = 0; i < K; i++){
            double score = getScore(instance.fv) - getScore((FeatureVector)d[i][0]);
            score = numErrors((List<Integer>)d[i][1], instance.labs) - score;
            b[i] = score;
            dist[i] = FeatureVector.getDistFeatureVector(instance.fv, (FeatureVector)d[i][0]);
        }

        double[] alpha = hildreth(dist, b);

        for (int i = 0; i < K; i++) {
            for (FeatureVector curr = dist[i]; curr != null; curr = curr.next) {
                if (curr.index >= 0){
                    this.parameters[curr.index] += alpha[i] * curr.value;
                    this.total[curr.index] += upd * alpha[i] * curr.value;
                }
            }
        }
    }

    public void updateParametersStand(CompressionInstance instance, Object[][] d, double upd){
        int K = 0;
        for (int i = 0; i < d.length && d[i][0] != null; i++){
            K++;
        }

        double[] b = new double[K];
        FeatureVector[] dist = new FeatureVector[K];

        for (int i = 0; i < K; i++){
            dist[i] = FeatureVector.getDistFeatureVector(instance.fv, (FeatureVector)d[i][0]);
        }

        for (int i = 0; i < K; i++) {
            for (FeatureVector curr = dist[i]; curr != null; curr = curr.next) {
                if (curr.index >= 0){
                    this.parameters[curr.index] += curr.value;
                    this.total[curr.index] += upd * curr.value;
                }
            }
        }
    }

    public int numErrors(List<Integer> parse, int[] gold) {
        int common = 0;
        for (int aParse : parse) {
            for (int aGold : gold) {
                if (aParse == aGold){
                    common++;
                    break;
                }
            }
        }
        return (parse.size() + gold.length - 2 * common);
    }

    private double[] hildreth(FeatureVector[] a, double[] b){

        int i;
        int max_iter = 10000;
        double eps = 0.00000001;
        double zero = 0.000000000001;

        double[] alpha = new double[b.length];

        double[] F = new double[b.length];
        double[] kkt = new double[b.length];
        double max_kkt = Double.NEGATIVE_INFINITY;

        int K = a.length;

        double[][] A = new double[K][K];
        boolean[] is_computed = new boolean[K];
        for(i = 0; i < K; i++) {
            A[i][i] = FeatureVector.dotProduct(a[i],a[i]);
            is_computed[i] = false;
        }

        int max_kkt_i = -1;


        for(i = 0; i < F.length; i++) {
            F[i] = b[i];
            kkt[i] = F[i];
            if(kkt[i] > max_kkt) { max_kkt = kkt[i]; max_kkt_i = i; }
        }

        int iter = 0;
        double diff_alpha;
        double try_alpha;
        double add_alpha;

        while(max_kkt >= eps && iter < max_iter) {

            diff_alpha = A[max_kkt_i][max_kkt_i] <= zero ? 0.0 : F[max_kkt_i]/A[max_kkt_i][max_kkt_i];
            try_alpha = alpha[max_kkt_i] + diff_alpha;
            add_alpha = 0.0;

            if(try_alpha < 0.0)
                add_alpha = -1.0 * alpha[max_kkt_i];
            else
                add_alpha = diff_alpha;

            alpha[max_kkt_i] = alpha[max_kkt_i] + add_alpha;

            if (!is_computed[max_kkt_i]) {
                for(i = 0; i < K; i++) {
                    A[i][max_kkt_i] = FeatureVector.dotProduct(a[i],a[max_kkt_i]);
                    is_computed[max_kkt_i] = true;
                }
            }

            for(i = 0; i < F.length; i++) {
                F[i] -= add_alpha * A[i][max_kkt_i];
                kkt[i] = F[i];
                if(alpha[i] > zero)
                    kkt[i] = Math.abs(F[i]);
            }

            max_kkt = Double.NEGATIVE_INFINITY;
            max_kkt_i = -1;
            for(i = 0; i < F.length; i++)
                if(kkt[i] > max_kkt) { max_kkt = kkt[i]; max_kkt_i = i; }

            iter++;
        }

        return alpha;
    }
}
