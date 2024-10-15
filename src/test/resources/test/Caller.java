/*
 * Copyright (C) 2013 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test;

import java.util.HashMap;
import java.util.Map;

public class Caller {

    public void call() {
        Callee callee = new Callee();
        int a = 1;
        if (a == 1) {
            int b = 2;
        }

//        Map map = new HashMap();
//        map.put("a", a);
//        callee.beforeExecute();
        callee.execute();
    }

//    public void call2() {
//        Callee callee = new Callee();
//        int a = 1;
//        if (a == 1) {
//            int b = 2;
//        };
//        java.util.Map map = new java.util.HashMap();
//        map.put("callee", callee);;
//        map.put("a", a);;
//        callee.beforeExecute();;
//        callee.execute();;
//    }
}
