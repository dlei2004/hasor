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
import net.hasor.core.utils.BeanUtils;
import net.hasor.dataql.Options;
import net.hasor.dataql.result.ObjectModel;
import net.hasor.dataql.runtime.InsetProcess;
import net.hasor.dataql.runtime.InstSequence;
import net.hasor.dataql.runtime.ProcessContet;
import net.hasor.dataql.runtime.ProcessException;
import net.hasor.dataql.runtime.struts.LocalData;
import net.hasor.dataql.runtime.struts.MemStack;
import net.hasor.dataql.runtime.struts.ObjectResultStruts;

import java.util.Map;
/**
 * PUT，将栈顶的数据 set 到结果集中。
 * @author 赵永春(zyc@hasor.net)
 * @version : 2017-07-19
 */
class PUT implements InsetProcess {
    @Override
    public int getOpcode() {
        return PUT;
    }
    @Override
    public void doWork(InstSequence sequence, MemStack memStack, LocalData local, ProcessContet context) throws ProcessException {
        String filedName = sequence.currentInst().getString(0);
        Object data = memStack.pop();
        //
        Object ors = memStack.peek();
        if (ors instanceof ObjectResultStruts) {
            ((ObjectResultStruts) ors).addResultField(filedName, data);
            return;
        }
        if (ors instanceof ObjectModel) {
            ((ObjectModel) ors).addField(filedName);
            ((ObjectModel) ors).put(filedName, data);
            return;
        }
        if (ors instanceof Map) {
            ((Map) ors).put(filedName, data);
            return;
        }
        //
        Object optionValue = context.getOption(Options.SAFE_PUT);
        boolean safeput = Boolean.TRUE.equals(optionValue);
        if (!safeput && !BeanUtils.canWriteProperty(filedName, ors.getClass())) {
            throw new ProcessException("output data eror, target type must be 'ListResultStruts or ListModel or Collection.'");
        }
        //
        BeanUtils.writePropertyOrField(ors, filedName, data);
    }
}