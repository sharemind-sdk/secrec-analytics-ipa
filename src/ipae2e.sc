/*
 * This file is a part of the Sharemind framework.
 * Copyright (C) Cybernetica AS
 *
 * All rights are reserved. Reproduction in whole or part is prohibited
 * without the written consent of the copyright owner. The usage of this
 * code is subject to the appropriate license agreement.
 */

import shared3p;
import stdlib;
import table_database;
import analytics_common;
import analytics_sort_table;
import analytics_group_by;
import analytics_debug;
import option;
import profiling;

domain pd_shared3p shared3p;

void main() {
    pd_shared3p uint proxy;

    //string table_name = "rust-input";
    string table_name = "mp-spdz-input";
    tdbOpenConnection("DS1");

    // READING

    print("Read");
    uint dbSize = tdbGetRowCount("DS1", table_name);
    uint32 sectionType = newSectionType("reading");
    uint32 section = startSection(sectionType, dbSize);

    Result readResult = dataFrameRead("DS1", table_name, proxy);
    if (readResult.status.failed) {
        print("Reading failed:");
        print(readResult.status.message);
        return;
    }
    print("Sort");
    DataFrame df = readResult.data;
    endSection(section);

    // SORTING

    sectionType = newSectionType("sorting");
    section = startSection(sectionType, dbSize);
    uint sortParameters = tdbVmapNew();
    tdbVmapAddString(sortParameters, "keys", "match_key");
    // For sorting by timestamp:
    // tdbVmapAddString(sortParameters, "keys", "timestamp");

    tdbVmapAddString(sortParameters, "data", "match_key");
    tdbVmapAddString(sortParameters, "data", "is_trigger");
    tdbVmapAddString(sortParameters, "data", "value");
    tdbVmapAddString(sortParameters, "data", "breakdown_key");

    tdbVmapAddString(sortParameters, "direction", "ascending");
    // For sorting by timestamp:
    // tdbVmapAddString(sortParameters, "direction", "ascending");
    tdbVmapAddString(sortParameters, "nulls-order", "last");

    option<uint> resNames = none();

    Result sortResult = dataFrameSort(df, sortParameters, resNames, proxy);
    if (sortResult.status.failed) {
        print("Sorting failed:");
        print(sortResult.status.message);
        tdbVmapDelete(sortParameters);
        return;
    }
    tdbVmapDelete(sortParameters);

    endSection(section);
    df = sortResult.data;


    // ATTRIBUTION
    print("Attribution");
    sectionType = newSectionType("attribution");
    section = startSection(sectionType, dbSize);

    // uint32/uint64 depending on input schema
    pd_shared3p uint32[[1]] keys =
        dataFrameGetColumn(df, "match_key");
    pd_shared3p bool[[1]] helper_bit =
        keys == cat({keys[0] - 1}, keys[:size(keys) - 1]);

    pd_shared3p bool[[1]] stop(size(keys)) = true;

    pd_shared3p bool[[1]] trigger_bit = dataFrameGetColumn(df, "is_trigger");
    pd_shared3p uint32[[1]] value = dataFrameGetColumn(df, "value");

    pd_shared3p uint32[[1]] accum = value;

    pd_shared3p bool[[1]] helper_and_trigger = helper_bit & trigger_bit;

    uint skip = 1;
    while (true) {
        uint succStart = skip;
        uint currEnd = size(keys) - skip;

        pd_shared3p bool[[1]] flag = stop[:currEnd] & helper_and_trigger[succStart:];
        accum[:currEnd] = accum[:currEnd] + (uint32) flag * accum[succStart:];
        skip = skip << 1;  // Square current skip
        if (skip > size(keys))
            break;
        stop[:currEnd] = flag & stop[succStart:];
    }

    endSection(section);

    // CAPPING
    print("Capping");
    sectionType = newSectionType("capping");
    section = startSection(sectionType, dbSize);

    pd_shared3p uint32 cap = 1024;

    stop = false;
    stop[:size(keys)/2] = true;
    pd_shared3p uint32[[1]] curr = accum;

    uint step = 1;
    while (step < size(keys) / 2) {
        step = step << 1;
        uint newsize = size(keys) - step;
        pd_shared3p bool[[1]] flag = stop[:newsize] & helper_bit[step:];
        curr[:newsize] = (uint32) flag * curr[step:] + curr[:newsize];
        stop[:newsize] = flag & stop[step:];
    }

    pd_shared3p uint32[[1]] comp = (uint32)(curr <= cap);
    pd_shared3p uint32[[1]] temp =
        cap - (uint32)helper_bit[1:] * (cap + comp[1:] * (cap - curr[1:]));

    accum[1:] = temp + comp[1:] * (accum[1:] - temp);

    endSection(section);

    // AGGREGATION
    print("Aggregation");
    sectionType = newSectionType("aggregation");
    section = startSection(sectionType, dbSize);
    // put the results back into the dataframe for grouping
    df = dataFrameAddColumn(df, "accum", accum);

    uint groupByParameters = tdbVmapNew();
    tdbVmapAddString(groupByParameters, "keys", "breakdown_key");
    tdbVmapAddString(groupByParameters, "data", "accum");
    tdbVmapAddString(groupByParameters, "functions", "sum");
    Result groupByResult = groupBy(df, groupByParameters, resNames, proxy);
    if (groupByResult.status.failed) {
        print("GroupBy failed:");
        print(groupByResult.status.message);
        tdbVmapDelete(groupByParameters);
        return;
    }
    tdbVmapDelete(groupByParameters);

    endSection(section);
    flushProfileLog();

    print("Done!");
    dataFramePrint(groupByResult.data, proxy);
}
