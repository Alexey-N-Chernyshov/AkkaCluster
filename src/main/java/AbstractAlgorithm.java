/**
 * @author Yex
 */

import java.io.Serializable;

abstract class AbstractAlgorithm implements Serializable {
    private static final long serialVersionUID = 1;

    abstract public String execute(String data);
}
