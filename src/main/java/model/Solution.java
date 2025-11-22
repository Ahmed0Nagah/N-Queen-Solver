package model;

import java.util.Arrays;

public class Solution {
    private final int[] cols;
    public Solution(int[] cols) { this.cols = cols; }
    public int[] getCols() { return cols; }
    @Override public String toString() { return Arrays.toString(cols); }
}