/*
 * Copyright 2002-2010 the original author or authors.
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
package net.hasor.db.mapping.resolve;
import net.hasor.db.mapping.TableMapping;
import net.hasor.db.metadata.MetaDataService;
import net.hasor.db.types.TypeHandlerRegistry;

import java.sql.SQLException;

/**
 * TableMapping 解析器
 * @version : 2021-06-21
 * @author 赵永春 (zyc@hasor.net)
 */
public interface ResolveTableMapping<T> {
    public TableMapping resolveTableMapping(T refData, ClassLoader classLoader, TypeHandlerRegistry typeRegistry, MetaDataService metaDataService, MappingOptions options) throws SQLException, ClassNotFoundException;
}
