package com.palantir.atlasdb.schema.stream.generated;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.palantir.atlasdb.cleaner.api.OnCleanupTask;
import com.palantir.atlasdb.keyvalue.api.Cell;
import com.palantir.atlasdb.protos.generated.StreamPersistence.Status;
import com.palantir.atlasdb.protos.generated.StreamPersistence.StreamMetadata;
import com.palantir.atlasdb.table.description.ValueType;
import com.palantir.atlasdb.transaction.api.Transaction;

public class StreamTest2MetadataCleanupTask implements OnCleanupTask {

    private final StreamTestTableFactory tables = StreamTestTableFactory.of();

    @Override
    public boolean cellsCleanedUp(Transaction t, Set<Cell> cells) {
        StreamTest2StreamMetadataTable metaTable = tables.getStreamTest2StreamMetadataTable(t);
        Collection<StreamTest2StreamMetadataTable.StreamTest2StreamMetadataRow> rows = Lists.newArrayListWithCapacity(cells.size());
        for (Cell cell : cells) {
            rows.add(StreamTest2StreamMetadataTable.StreamTest2StreamMetadataRow.of((Long) ValueType.VAR_LONG.convertToJava(cell.getRowName(), 0)));
        }
        Map<StreamTest2StreamMetadataTable.StreamTest2StreamMetadataRow, StreamMetadata> currentMetadata = metaTable.getMetadatas(rows);
        Set<Long> toDelete = Sets.newHashSet();
        for (Map.Entry<StreamTest2StreamMetadataTable.StreamTest2StreamMetadataRow, StreamMetadata> e : currentMetadata.entrySet()) {
            if (e.getValue().getStatus() != Status.STORED) {
                toDelete.add(e.getKey().getId());
            }
        }
        StreamTest2StreamStore.of(tables).deleteStreams(t, toDelete);
        return false;
    }
}