package com.bdwater.meterinput.model;

import java.io.Serializable;

/**
 * Created by hegang on 16/6/13.
 */
public class Book implements Serializable {
    public String BookID;
    public String BookNo;
    public String Title;
    @Override
    public String toString() {
        return this.Title;
    }
}
