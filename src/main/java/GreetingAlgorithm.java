/**
 * @author Yex
 */

class GreetingAlgorithm extends AbstractAlgorithm {
    @Override
    public String execute(String data) {
        String res = "worker hello, " + data;
        System.out.print(res);
        return res;
    }
}
