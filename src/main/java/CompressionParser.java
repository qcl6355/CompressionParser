import java.io.*;
import java.util.List;

/**
 * @author Sheng Li
 *         Created on 14/12/8.
 */
public class CompressionParser {
    public CompressionPipe pipe;
    public CompressionDecoder decoder;
    public Parameters para;
    public int numIters;
    public int k_best;
    public String trainForest;
    private String modelName;
    private String predictedFile;
    public double rate;
    public boolean useRateDecoder;

    public CompressionParser(Options op, CompressionPipe p) {
        pipe = p;
        decoder = new CompressionDecoder();
        para = new Parameters(pipe.dataAlphabet.size());
        numIters = op.numIters;
        k_best = op.k_best;
        trainForest = op.trainForest;
        modelName = op.modelFile;
        predictedFile = op.predictedFile;
        rate = op.compression_rate;
        useRateDecoder = op.useRateDecoder;
    }

    public void train(List<CompressionInstance> instanceList) throws IOException {
        System.out.println("About Train");
        System.out.println("Num of Features:" + pipe.dataAlphabet.size());

        for (int i = 0; i < numIters; i++){
            System.out.println("=========================");
            System.out.println("========Iteration:\t" + i);

            long start = System.currentTimeMillis();
            trainIter(instanceList, trainForest, i+1);
            long end = System.currentTimeMillis();
            System.out.printf("Training one Iteration took:%.4fs\n", (end - start) / 1000.0);
        }
        para.averageParameters(numIters * instanceList.size());
    }

    private void trainIter(List<CompressionInstance> instanceList, String trainForest, int iter) throws IOException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(trainForest));
        for (int i = 0; i < instanceList.size(); i++) {
            if ((i+1) % 100 == 0) System.out.printf("Processed %d Instances\n", i+1);

            CompressionInstance instance = instanceList.get(i);
            int size = instance.size();
            FeatureVector[][] feat_edge = new FeatureVector[size][size];
            double[][] score_edge = new double[size][size];
            pipe.getFeatureVector(in, instance, feat_edge, score_edge, para);
            double upd = instanceList.size() * numIters - (instanceList.size()* (iter - 1) + (i+1)) + 1;
            Object[][] d;
            if (useRateDecoder) {
                d = decoder.dynamic_search(instance, feat_edge, score_edge, k_best, rate);
            } else {
                d = decoder.dynamic_search(instance, feat_edge, score_edge, k_best);
            }
            para.updateParametersMIRA(instance, d, upd);
        }
        in.close();
        System.out.printf("All %d Instances\n", instanceList.size());
    }

    public void parse(List<CompressionInstance> instanceList, String outputFile)
            throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
        for (int i = 0; i < instanceList.size(); i++) {
            System.err.println(i);
            CompressionInstance instance = instanceList.get(i);

            int size = instance.size();
            FeatureVector[][] feat_edge = new FeatureVector[size][size];
            double[][] score_edge = new double[size][size];
            pipe.getFeatureVector(instance, feat_edge, score_edge, para);

            Object[][] d;
            if (useRateDecoder) {
                d = decoder.dynamic_search(instance, feat_edge, score_edge, k_best, rate);
            } else {
                d = decoder.dynamic_search(instance, feat_edge, score_edge, k_best);
            }

            bw.write(getOutputString(instance, (List<Integer>) d[0][1]));
            bw.newLine();
        }
        bw.close();
    }

    private String getOutputString(CompressionInstance instance, List<Integer> parse){
        //System.out.println(parse);
        String res = "";

        for (int i = 1; i < parse.size() - 1; i++) {
            res += instance.words[parse.get(i)] + " ";
        }
        return res;
    }

    public void saveModel(String modelName) throws IOException{
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(modelName));
        out.writeObject(para.parameters);
        out.writeObject(pipe.dataAlphabet);
        out.close();
    }

    public void loadModel(String modelName) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(modelName));
        para.parameters = (double[])in.readObject();
        pipe.dataAlphabet = (Alphabet)in.readObject();
        in.close();
        pipe.closeAlphabets();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Options op = new Options();
        op.processArguments(args);
        op.printOptions();

        if (op.train){
            CompressionPipe pipe = new CompressionPipe();
            List<CompressionInstance> instanceList = pipe.createInstances(op.trainFile, op.trainParsedFile, op.trainForest);
            CompressionParser parser = new CompressionParser(op, pipe);

            pipe.closeAlphabets();

            System.out.println("=============Start Training============");
            parser.train(instanceList);

            System.out.println("==============Save Model===============");
            parser.saveModel(parser.modelName);
            System.out.println("==============Done!====================");
        } else if (op.test){
            System.out.println("=============Loading Model=============");
            CompressionPipe pipe = new CompressionPipe();
            CompressionParser parser = new CompressionParser(op, pipe);
            parser.loadModel(parser.modelName);
            List<CompressionInstance> instanceList = CompressionReader.read(op.testFile, true, false);
            //DependencyReader.read(op.testParsedFile, instanceList);
            parser.parse(instanceList, parser.predictedFile);
            System.out.println("============Parsed Done!===============");
        }
    }
}
