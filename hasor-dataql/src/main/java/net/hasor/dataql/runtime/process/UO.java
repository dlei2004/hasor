/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dataql.runtime.process;
import net.hasor.dataql.runtime.OperatorProcess;
import net.hasor.dataql.runtime.*;
import net.hasor.dataql.runtime.struts.LocalData;
import net.hasor.dataql.runtime.struts.MemStack;
/**
 * UO 指令是用于进行 一元运算。
 * 该指令会通过运算符和被计算的表达式来寻找 OperatorProcess 运算实现类进行运算。
 * 开发者可以通过实现 OperatorProcess 接口，覆盖某个运算符实现 运算符重载功能。
 * @author 赵永春(zyc@hasor.net)
 * @version : 2017-07-19
 */
class UO implements InsetProcess {
    @Override
    public int getOpcode() {
        return UO;
    }
    @Override
    public void doWork(InstSequence sequence, MemStack memStack, LocalData local, ProcessContet context) throws ProcessException {
        String dyadicSymbol = sequence.currentInst().getString(0);
        Object expData = memStack.pop();
        //
        Class<?> expType = (expData == null) ? Void.class : expData.getClass();
        OperatorProcess process = context.findOperator(Symbol.Unary, dyadicSymbol, expType, null);
        //
        if (process == null) {
            throw new ProcessException("UO -> " + dyadicSymbol + " OperatorProcess is Undefined");
        }
        //
        Object result = process.doProcess(dyadicSymbol, new Object[] { expData });
        memStack.push(result);
    }
}