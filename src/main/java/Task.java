/**
 * @author Yex
 */

import java.io.Serializable;

class Task implements Serializable {
    private static final long serialVersionUID = 1;
    private final String data;
    private AbstractAlgorithm algorithm;

    Task(String data, AbstractAlgorithm algorithm) {
        this.data = data;
        this.algorithm = algorithm;
    }

    String execute() {
        return algorithm.execute(data);
    }
}
