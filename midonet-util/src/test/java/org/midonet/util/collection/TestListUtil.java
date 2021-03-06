/*
 * Copyright 2014 Midokura SARL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.midonet.util.collection;

import java.util.List;
import java.util.Arrays;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junitparams.JUnitParamsRunner.$;

@RunWith(JUnitParamsRunner.class)
public class TestListUtil {

    @Test
    @Parameters(source = TestListUtil.class, method="intListForToStringTest")
    public void testToString(List<Integer> input, String expected) {

        Assert.assertEquals(ListUtil.toString(input), expected);
    }

    public static Object[] intListForToStringTest() {
        return $(
                $(null, "null"),
                $(Arrays.asList(), "[]"),
                $(Arrays.asList(0), "[0]"),
                $(Arrays.asList(0, null), "[0, null]"),
                $(Arrays.asList(0, 1), "[0, 1]")
        );
    }

}
