// IResult.aidl
package com.shuaijun.plant;

// Declare any non-default types here with import statements

interface IResult {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void analysis(long id, String result);
}