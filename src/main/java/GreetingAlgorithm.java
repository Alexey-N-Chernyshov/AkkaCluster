/**
 * @author Yex
 */

class GreetingAlgorithm extends AbstractAlgorithm {
    @Override
    public String execute(String data) {
        String res =
                "===================================\n" +
                "worker: " + data + "\n" +
                "===================================\n";
        System.out.print(res);
        return res;
    }
}
