// ISnpeService.aidl
package com.shuaijun.plant;

import com.shuaijun.plant.IResult;
// Declare any non-default types here with import statements

interface ISnpeService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void observer(IResult listener);

    void putTask(long id, String pathName);
}