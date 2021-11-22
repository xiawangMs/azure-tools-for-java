/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.database;

import com.microsoft.azure.toolkit.lib.database.JdbcUrl;
import com.microsoft.azuretools.ActionConstants;
import com.microsoft.azuretools.telemetrywrapper.EventType;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.sql.*;
import java.util.Collections;

public class DatabaseConnectionUtils {

    private static final String SQL_SERVER_URL_PREFIX = "jdbc:sqlserver:";
    private static final String POSTGRE_URL_PREFIX = "jdbc:postgresql:";
    private static final String CONNECTION_ISSUE_MESSAGE = "%s Please follow https://docs.microsoft.com/en-us/azure/mysql/howto-manage-firewall-using-portal "
            + "to create a firewall rule to unblock your local access.";
    private static final int CONNECTION_ERROR_CODE = 9000;
    public static final int ACCESS_DENIED_ERROR_CODE = 1045;
    private static final int CLASS_NOT_FOUND_ERROR_CODE = -1000;
    private static final int UNKNOWN_EXCEPTION_ERROR_CODE = -1;

    public static boolean connect(JdbcUrl url, String username, String password) {
        try {
            Class.forName(getDriverClassName(url));
            DriverManager.getConnection(url.toString(), username, password);
            return true;
        } catch (final ClassNotFoundException | SQLException ignored) {
        }
        return false;
    }

    public static ConnectResult connectWithPing(JdbcUrl url, String username, String password) {
        int errorCode = 0;
        boolean connected = false;
        String errorMessage = null;
        Long pingCost = null;
        String serverVersion = null;
        // refresh property
        try {
            Class.forName(getDriverClassName(url));
            final long start = System.currentTimeMillis();
            final Connection connection = DriverManager.getConnection(url.toString(), username, password);
            connected = true;
            final Statement statement = connection.createStatement();
            final ResultSet resultSet = statement.executeQuery("select 'hi'");
            if (resultSet.next()) {
                final String result = resultSet.getString(1);
                connected = "hi".equals(result);
            }
            pingCost = System.currentTimeMillis() - start;
            if (StringUtils.startsWith(url.toString(), SQL_SERVER_URL_PREFIX)) {
                try {
                    serverVersion = (String) FieldUtils.readField(connection, "sqlServerVersion", true);
                } catch (final IllegalAccessException e) {
                    serverVersion = "unknown";
                }
            } else {
                final DatabaseMetaData meta = connection.getMetaData();
                serverVersion = meta == null ? "unknown" : String.format("%d.%d", meta.getDatabaseMajorVersion(), meta.getDatabaseMinorVersion());
            }
        } catch (final SQLException exception) {
            errorCode = exception.getErrorCode();
            errorMessage = exception.getErrorCode() == CONNECTION_ERROR_CODE ?
                String.format(CONNECTION_ISSUE_MESSAGE, exception.getMessage()) : exception.getMessage();
        } catch (final ClassNotFoundException | RuntimeException exception) {
            errorCode = exception instanceof ClassNotFoundException ? CLASS_NOT_FOUND_ERROR_CODE : UNKNOWN_EXCEPTION_ERROR_CODE;
            errorMessage = exception.getMessage();
        }
        EventUtil.logEvent(EventType.info, ActionConstants.parse(ActionConstants.MySQL.TEST_CONNECTION).getServiceName(),
                ActionConstants.parse(ActionConstants.MySQL.TEST_CONNECTION).getOperationName(),
                Collections.singletonMap("result", String.valueOf(connected)));
        return new ConnectResult(connected, errorMessage, pingCost, serverVersion, errorCode);
    }

    private static String getDriverClassName(JdbcUrl url) {
        final String jdbcUrl = url.toString();
        if (StringUtils.startsWith(jdbcUrl, SQL_SERVER_URL_PREFIX)) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } else if (StringUtils.startsWith(jdbcUrl, POSTGRE_URL_PREFIX)) {
            return "org.postgresql.Driver";
        } else {
            return "com.mysql.cj.jdbc.Driver";
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ConnectResult {
        private final boolean connected;
        private final String message;
        private final Long pingCost;
        private final String serverVersion;
        private final int errorCode;
    }
}
