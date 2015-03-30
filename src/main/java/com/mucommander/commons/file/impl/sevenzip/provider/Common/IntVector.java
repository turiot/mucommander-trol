package com.mucommander.commons.file.impl.sevenzip.provider.Common;

// TODO remove it!
public class IntVector {
    protected int[] data = new int[10];
    int capacityIncr = 10;
    int elt = 0;
    
    public IntVector() {
    }
    
    public int size() {
        return elt;
    }
    
    private void ensureCapacity(int minCapacity) {
        int oldCapacity = data.length;
        if (minCapacity > oldCapacity) {
            int [] oldData = data;
            int newCapacity = oldCapacity + capacityIncr;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            data = new int[newCapacity];
            System.arraycopy(oldData, 0, data, 0, elt);
        }
    }
    
    public int get(int index) {
        if (index >= elt)
            throw new ArrayIndexOutOfBoundsException(index);
        
        return data[index];
    }
    
    public void Reserve(int s) {
        ensureCapacity(s);
    }
    
    public void add(int b) {
        ensureCapacity(elt + 1);
        data[elt++] = b;
    }
    
    public void clear() {
        elt = 0;
    }
    
    public boolean isEmpty() {
        return elt == 0;
    }

    public int remove(int index) {
        if (index >= elt)
            throw new ArrayIndexOutOfBoundsException(index);
        int oldValue = data[index];
        
        int numMoved = elt - index - 1;
        if (numMoved > 0)
            System.arraycopy(elt, index+1, elt, index,numMoved);
        // TODO WTF ?!
        
        // data[--elt] = null; // Let gc do its work
        
        return oldValue;
    }
    
}
