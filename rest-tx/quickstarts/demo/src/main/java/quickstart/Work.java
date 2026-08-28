package quickstart;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

class Work implements java.io.Serializable {
    static Map<String, Work> work = new HashMap<String, Work>();

    String enlistUrl;
    int wid;
    int index;
    int counter;

    public Work(String enlistUrl, int wid, int index, int counter) {
	System.out.println("Creating counter with: " + wid + " " + index + " " + counter);
        this.enlistUrl = enlistUrl;
        this.wid = wid;
        this.index = index;
        this.counter = counter;
    }
}
