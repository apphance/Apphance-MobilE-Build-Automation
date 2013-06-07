package com.apphance.flow.util

class SortedProperties extends Properties {

    @Override
    public synchronized Enumeration keys() {
        Enumeration keysEnum = super.keys();

        Vector keyList = new Vector();
        while (keysEnum.hasMoreElements()) {
            keyList.add(keysEnum.nextElement());
        }
        keyList.sort()

        return keyList.elements();
    }
}
