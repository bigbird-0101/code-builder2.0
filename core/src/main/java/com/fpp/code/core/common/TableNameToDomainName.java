package com.fpp.code.core.common;

import com.fpp.code.spi.TypeBasedSPI;

/**
 * @author bigbird-0101
 * @version 1.0.0
 * @since 2022-09-12 14:55:13
 */
public interface TableNameToDomainName extends TypeBasedSPI{

    String DEFAULT="CAMEL_CASE";

    /**
     * 表名构建domainName名
     * @param tableName 表名
     * @return
     */
    String buildDomainName(String tableName);
}
