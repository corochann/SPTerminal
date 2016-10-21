package com.corochann.spterminal.data.model;

import java.util.Vector;

/**
 * Model class which shows the command
 * where the i-th character specified by indexVec is highlighted.
 */
public class HighlightableCommand {
    /** command string */
    public String command = "";
    /**
     * the index of command string which should be highlighted,
     * index should be stored in increasing order.
     */
    public Vector<Integer> indexVec = new Vector<>();

    public HighlightableCommand(String command) {
        this.command = command;
    }

    public void addIndex(Integer index) {
        indexVec.add(index);
    }
}
