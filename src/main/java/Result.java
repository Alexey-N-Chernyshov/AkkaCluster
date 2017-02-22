/**
 * @author Yex
 */

import java.io.Serializable;

class Result implements Serializable {
    private static final long serialVersionUID = 1;
    String data;

    Result(String data) {
        this.data = data;
    }
}
