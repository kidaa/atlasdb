/**
 * Copyright 2015 Palantir Technologies
 *
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.atlasdb.schema;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.palantir.atlasdb.keyvalue.api.Cell;
import com.palantir.atlasdb.keyvalue.api.RowResult;
import com.palantir.atlasdb.transaction.api.TransactionManager;
import com.palantir.common.collect.Maps2;

/**
 * Builder for a {@link TransactionRangeMigrator}.
 *
 * Required arguments are srcTable, txManager and checkpointer.
 *
 * If destTable is not given, it defaults to the srcTable. This makes sense for kvs migrations or
 * value changes that move columns.
 *
 * If readTxManager is not given, the read transaction will be the same as the write transaction.
 * This will not work for kvs migrations.
 */
public class TransactionRangeMigratorBuilder {
    private String srcTable;
    private String destTable;
    private int readBatchSize;
    private TransactionManager readTxManager;
    private TransactionManager txManager;
    private AbstractTaskCheckpointer checkpointer;
    private Function<RowResult<byte[]>, Map<Cell, byte[]>> rowTransform;

    public TransactionRangeMigratorBuilder() {
        srcTable = null;
        destTable = null;
        readBatchSize = 1000;
        readTxManager = null;
        txManager = null;
        checkpointer = null;
        rowTransform = getIdentityTransform();
    }

    private static Function<RowResult<byte[]>, Map<Cell, byte[]>> getIdentityTransform() {
        return new Function<RowResult<byte[]>, Map<Cell,byte[]>>() {
            @Override
            public Map<Cell, byte[]> apply(RowResult<byte[]> input) {
                return Maps2.fromEntries(input.getCells());
            }
        };
    }

    public TransactionRangeMigratorBuilder srcTable(String table) {
        Preconditions.checkNotNull(table);
        this.srcTable = table;
        return this;
    }

    public TransactionRangeMigratorBuilder destTable(String table) {
        Preconditions.checkNotNull(table);
        this.destTable = table;
        return this;
    }

    public TransactionRangeMigratorBuilder readBatchSize(int batchSize) {
        Preconditions.checkArgument(readBatchSize > 0);
        this.readBatchSize = batchSize;
        return this;
    }

    public TransactionRangeMigratorBuilder readTxManager(TransactionManager txm) {
        Preconditions.checkNotNull(txm);
        this.readTxManager = txm;
        return this;
    }

    public TransactionRangeMigratorBuilder txManager(TransactionManager txMgr) {
        Preconditions.checkNotNull(txMgr);
        this.txManager = txMgr;
        return this;
    }

    public TransactionRangeMigratorBuilder checkpointer(AbstractTaskCheckpointer c) {
        Preconditions.checkNotNull(c);
        this.checkpointer = c;
        return this;
    }

    public TransactionRangeMigratorBuilder rowTransformer(Function<RowResult<byte[]>, Map<Cell, byte[]>> f) {
        Preconditions.checkNotNull(f);
        this.rowTransform = f;
        return this;
    }

    public TransactionRangeMigrator build() {
        if (destTable == null) {
            destTable = srcTable;
        }
        if (readTxManager == null) {
            readTxManager = txManager;
        }

        Preconditions.checkNotNull(srcTable);
        Preconditions.checkNotNull(txManager);
        Preconditions.checkNotNull(checkpointer);

        return new TransactionRangeMigrator(
                srcTable,
                destTable,
                readBatchSize,
                readTxManager,
                txManager,
                checkpointer,
                rowTransform);
    }
}
