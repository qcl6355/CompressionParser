/**
 * @author Sheng Li
 *         Created on 14/12/8.
 */
public class Options {
    public String trainFile = "./data/train.txt";
    public String trainParsedFile = "./data/train.parse.txt";
    public String testFile = "./data/test.txt";
    public String testParsedFile = "./data/test.parse.txt";
    public String modelFile = "./data/compress.model";
    public String predictedFile = "./data/predict.txt";
    public String trainForest = "./data/train-forest.txt";
    public int numIters = 10;
    public int k_best = 10;
    public boolean train = false;
    public boolean test = true;
    public boolean useRateDecoder = false;
    public double compression_rate = 0.75;

    public void processArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-train")){
                train = true;
            } else if (arg.equals("-test")){
                test = true;
            } else if (arg.equals("-t")) {
                numIters = Integer.parseInt(args[i + 1]);
            } else if (arg.equals("-k")) {
                k_best = Integer.parseInt(args[i + 1]);
            } else if (arg.equals("-trainFile")) {
                trainFile = args[i + 1];
            } else if (arg.equals("-modelFile")) {
                modelFile = args[i + 1];
            } else if (arg.equals("-testFile")) {
                testFile = args[i + 1];
            } else if (arg.equals("-predictFile")) {
                predictedFile = args[i + 1];
            } else if (arg.equals("-trainForest")) {
                trainForest = args[i + 1];
            } else if (arg.equals("-trainParse")) {
                trainParsedFile = args[i + 1];
            } else if (arg.equals("-testParse")) {
                testParsedFile = args[i + 1];
            } else if (arg.equals("-decodeWithRate")){
                useRateDecoder = true;
            } else if (arg.equals("-rate")) {
                compression_rate = Double.parseDouble(args[i + 1]);
            } else if (arg.equals("-h")){
                help();
                System.exit(1);
            }
        }
    }

    public void printOptions() {
        System.out.println("==========Parser Flags==============");
        if (train){
            System.out.println("under train mode");
            System.out.println("train file:\t" + trainFile);
            System.out.println("model file:\t" + modelFile);
            System.out.println("syntactic and dependency file for train:\t" + trainParsedFile);
            System.out.println("train forest file:\t" + trainForest);
            System.out.println("max iteration number:\t" + numIters);
            System.out.println("beam search size:\t" + k_best);
        } else if (test){
            System.out.println("under test mode");
            System.out.println("test file:\t" + testFile);
            System.out.println("model file:\t" + modelFile);
            System.out.println("syntactic and dependency file for test:\t" + testParsedFile);
            System.out.println("test result file:\t" + predictedFile);
            System.out.println("beam search size:\t" + k_best);
        }
        System.out.println("use decoder with rate:\t" + useRateDecoder);
        System.out.println("====================================");
    }

    public void help(){
        System.err.println("=================Usage==============");
        System.err.println("-train\t training mode");
        System.err.println("-test\t testing mode");
        System.err.println("-t\t maximum iteration");
        System.err.println("-k\t beam size");
        System.err.println("-decodeWithRate\t use decoder with fixed rate algorithm");
        System.err.println("-rate\t specify a concrete compression rate, e.g. 0.75");
        System.err.println("-trainFile\t specify train data path");
        System.err.println("-testFile\t specify test data path");
        System.err.println("-modelFile\t specify model saved path");
        System.err.println("-trainForest\t specify training feature tmp file");
        System.err.println("-trainParse\t specify training syntactic and dependency file path");
        System.err.println("-testParse\t specify testing syntactic and dependency file path");
        System.err.println("-predictFile\t specify test output file path");
        System.err.println("-h\t print usage information");
        System.err.println("");
        System.err.println("Example: java -cp xxx.jar -train -t 10 -k 10 -trainFile ../data/train.txt" +
                " -trainForest ./data/train.forest -trainParse ../data/train.parse.txt " +
                "-modelFile ../data/compression.model");
        System.err.println("================Enjoy it===============");
    }
}
