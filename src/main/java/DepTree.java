import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sheng Li
 *         Created on 14/12/15.
 */
public class DepTree {
    public int head;
    public List<Integer> modifier;
    public static Pattern depPattern = Pattern.compile("(.+)\\(.+-(\\d+), .+-(\\d+)\\)");

    public DepTree(){
        head = -1;
        modifier = new ArrayList<Integer>();
    }

    public DepTree(int pid, List<Integer> chi) {
        head = pid;
        modifier = chi;
    }

    public static DepTree[] parse(List<String> deps, int size) {
        DepTree[] res = new DepTree[size];
        for (int i = 0; i < res.length; i++) {
            res[i] = new DepTree();
        }

        for (String line : deps) {
            _parse(line, res);
        }
        return res;
    }

    private static void _parse(String line, DepTree[] all_dep) {
        Matcher matcher = depPattern.matcher(line);
        if (matcher.find()){
            //String type = matcher.group(1);
            int head = Integer.parseInt(matcher.group(2));
            int modifier = Integer.parseInt(matcher.group(3));
            all_dep[head].modifier.add(modifier);
            all_dep[modifier].head = head;
        }
    }

    public boolean isLeaf() {
        return modifier.size() == 0;
    }

    public boolean isRoot() {
        return head == 0;
    }
    @Override
    public String toString() {
        return "Parent:" + head + ";Children:" + modifier.toString();
    }

    public static void main(String[] args) {
        String test = "advmod(有-9, 据悉-1)\n" +
                "det(内容-8, 这些-3)\n" +
                "amod(措施-5, 新-4)\n" +
                "assmod(内容-8, 措施-5)\n" +
                "case(措施-5, 的-6)\n" +
                "amod(内容-8, 主要-7)\n" +
                "dep(有-9, 内容-8)\n" +
                "xsubj(经营-13, 内容-8)\n" +
                "root(ROOT-0, 有-9)\n" +
                "case(规模-12, 以-10)\n" +
                "nn(规模-12, 发展-11)\n" +
                "prep(经营-13, 规模-12)\n" +
                "conj(经营-13, 组建-15)\n" +
                "nn(集团-17, 企业-16)\n" +
                "dobj(组建-15, 集团-17)\n" +
                "case(重点-19, 为-18)\n" +
                "prep(推进-21, 重点-19)\n" +
                "conj(经营-13, 推进-21)\n" +
                "amod(公司-24, 省属-22)\n" +
                "nn(公司-24, 外贸-23)\n" +
                "assmod(重组-27, 公司-24)\n" +
                "case(公司-24, 的-25)\n" +
                "amod(重组-27, 战略性-26)\n" +
                "dobj(推进-21, 重组-27)\n" +
                "conj(经营-13, 加大-29)\n" +
                "nn(出口-33, 支柱-30)\n" +
                "nn(出口-33, 产业-31)\n" +
                "nn(出口-33, 产品-32)\n" +
                "assmod(力度-35, 出口-33)\n" +
                "case(出口-33, 的-34)\n" +
                "dobj(加大-29, 力度-35)\n" +
                "conj(经营-13, 办好-37)\n" +
                "case(香港-39, 在-38)\n" +
                "prep(举办-40, 香港-39)\n" +
                "relcl(洽谈会-50, 举办-40)\n" +
                "mark(举办-40, 的-41)\n" +
                "nn(招商会-44, 外商-42)\n" +
                "nn(招商会-44, 投资-43)\n" +
                "conj(洽谈会-50, 招商会-44)\n" +
                "cc(洽谈会-50, 和-45)\n" +
                "nn(投资-48, 九八-46)\n" +
                "nn(投资-48, 中国-47)\n" +
                "nn(洽谈会-50, 投资-48)\n" +
                "nn(洽谈会-50, 贸易-49)\n" +
                "dobj(办好-37, 洽谈会-50)\n" +
                "etc(洽谈会-50, 等-51)";
        List<String> tmp = new ArrayList<String>();
        Collections.addAll(tmp, test.split("\\n"));
        DepTree[] result = parse(tmp, 54);
        for (DepTree t : result) {
            System.out.println(t);
        }
    }
}
