package org.broken.arrow.database.library.core.databases;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.broken.arrow.database.library.builders.ConnectionSettings;
import org.broken.arrow.database.library.builders.DataWrapper;
import org.broken.arrow.database.library.builders.LoadDataWrapper;
import org.broken.arrow.database.library.builders.tables.SqlQueryTable;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.construct.query.utlity.QueryDefinition;
import org.broken.arrow.database.library.core.Database;
import org.broken.arrow.database.library.utility.DatabaseCommandConfig;
import org.broken.arrow.database.library.utility.StatementContext;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;
import org.broken.arrow.library.logging.Logging;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

public class MongoDB extends Database {

    private final Logging log = new Logging(MongoDB.class);
    private final String startSQLUrl;
    private final String driver;
    private final ConnectionSettings preferences;
    private MongoClient mongoClient;
    private boolean isClosed;

    public MongoDB(final ConnectionSettings preferences) {
        super(preferences);
        this.preferences = preferences;
        this.startSQLUrl = "mongodb://";
        this.driver = "com.mongodb.Driver";
        connect();
    }

    @Override
    public void saveAll(@Nonnull final String tableName, @Nonnull final List<DataWrapper> dataWrapperList) {
        this.saveAll(tableName, dataWrapperList, false);
    }

    @Override
    public void saveAll(@Nonnull final String tableName, @Nonnull final List<DataWrapper> dataWrapperList, final boolean shallUpdate, final String... columns) {
        final  SqlQueryTable sqlQueryTable = this.getTableFromName(tableName);
        if (sqlQueryTable == null) {
            this.printFailFindTable(tableName);
            return;
        }
        if (!openMongo()) {
            errorCouldConnect();
            return;
        }

        MongoDatabase database = mongoClient.getDatabase(preferences.getDatabaseName());
        MongoCollection<Document> collection = database.getCollection(tableName);
        for (DataWrapper dataWrapper : dataWrapperList) {
            saveData(dataWrapper, sqlQueryTable, collection);
        }

        // Close the MongoDB connection
        this.closeConnection();
    }

    @Override
    public void save(@Nonnull final String tableName, @Nonnull final DataWrapper dataWrapper) {
        this.save(tableName, dataWrapper, false);
    }

    @Override
    public void save(@Nonnull final String tableName, @Nonnull final DataWrapper dataWrapper, final boolean shallUpdate, String... columns) {
        SqlQueryTable tableWrapper = this.getTableFromName(tableName);
        if (tableWrapper == null) {
            this.printFailFindTable(tableName);
            return;
        }
        if (isClosed()) return;
        MongoDatabase database = mongoClient.getDatabase(preferences.getDatabaseName());
        MongoCollection<Document> collection = database.getCollection(tableName);

        saveData(dataWrapper, tableWrapper, collection);

        // Close the MongoDB connection
        this.closeConnection();
    }

    @Nullable
    @Override
    public <T extends ConfigurationSerializable> List<LoadDataWrapper<T>> loadAll(@Nonnull final String tableName, @Nonnull final Class<T> clazz) {
        SqlQueryTable sqlQueryTable = this.getTableFromName(tableName);
        if (sqlQueryTable == null) {
            this.printFailFindTable(tableName);
            return null;
        }
        if (!openMongo()) {
            errorCouldConnect();
            return null;
        }

        final List<LoadDataWrapper<T>> loadDataWrappers = new ArrayList<>();
        MongoDatabase database = mongoClient.getDatabase(preferences.getDatabaseName());
        // Retrieve data from MongoDB
        MongoCollection<Document> collection = database.getCollection(tableName);

        Document query = new Document(); // Replace with your specific query if needed
        FindIterable<Document> documents = collection.find(query);
        // Check if documents has results
        if (documents.iterator().hasNext()) {
            // Iterate over the documents and process each document
            for (Document document : documents) {
                Object id = document.get("_id");

                Map<String, Object> resultMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : document.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    resultMap.put(key, value);

                }
                T deserialize = this.deSerialize(clazz,  resultMap);
                Map<String,Object> map = new HashMap<>();
                map.put("id", id);
                loadDataWrappers.add(new LoadDataWrapper<>(map, deserialize));
            }
        } else {
            log.log(() -> "Could not find any row within this table " + tableName);
        }
        this.closeConnection();
        return loadDataWrappers;
    }

    @Nullable
    @Override
    public <T extends ConfigurationSerializable> LoadDataWrapper<T> load(@Nonnull final String tableName, @Nonnull final Class<T> clazz, @Nonnull final String columnValue) {
        SqlQueryTable tableWrapper = this.getTableFromName(tableName);
        if (tableWrapper == null) {
            this.printFailFindTable(tableName);
            return null;
        }
        if (!openMongo()) {
            errorCouldConnect();
            return null;
        }

        LoadDataWrapper<T> loadDataWrapper = null;
        MongoDatabase database = mongoClient.getDatabase(preferences.getDatabaseName());
        MongoCollection<Document> collection = database.getCollection(tableName);

        Document query = new Document();
        query.append("_id", columnValue);
        FindIterable<Document> documents = collection.find(query);
        if (documents.iterator().hasNext()) {
            for (Document document : documents) {
                Object id = document.get("_id");
                if (!columnValue.equals(id)) continue;

                Map<String, Object> resultMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : document.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    resultMap.put(key, value);
                }
                T deserialize = this.deSerialize(clazz,  resultMap);
                Map<String,Object> map = new HashMap<>();
                map.put("id", id);
                loadDataWrapper = new LoadDataWrapper<>(map , deserialize);
            }
        } else {
            log.log(() -> "Could not find any row with this value " + columnValue);
        }
        // Close the MongoDB connection
        this.closeConnection();
        return loadDataWrapper;
    }


    /**
     * Executes a query against a MongoDB collection and passes the result to the given function.
     * This method retrieves a {@link MongoCollection} of type {@link Document} based on the provided
     * {@link QueryDefinition}, and supplies it to the consumer wrapped in a {@link StatementContext}.
     * <p>&nbsp;</p>
     * <p>You can use methods such as {@link MongoCollection#find(Bson)} to filter results, or retrieve all documents
     * if no filter is applied.</p>
     * <p>&nbsp;</p>
     * <p>Refer to the <a href="https://www.mongodb.com/docs/manual/">MongoDB Manual</a> for more information.</p>
     *
     * @param queryBuilder The query wrapper containing the name of the collection to access (typically treated as a "table name" in this library).
     * @param function     The function that will be applied to the command.
     * @param <T>          The type you want the method to return.
     * @return the value you set as the lambda should return or null if something did go wrong.
     */


    @Nullable
    public <T> T executeQuery(@Nonnull final QueryDefinition queryBuilder, Function<StatementContext<MongoCollection<Document>>, T> function) {
        if (!openMongo()) {
            errorCouldConnect();
            return null;
        }
        MongoDatabase database = mongoClient.getDatabase(preferences.getDatabaseName());
        MongoCollection<Document> collection = database.getCollection(queryBuilder.getQuery());
        try {
            return function.apply( new StatementContext<>(collection));
        } finally {
            closeConnection();
        }
    }

    /**
     * Executes a query against a MongoDB collection and passes the result to the given consumer.
     * This method retrieves a {@link MongoCollection} of type {@link Document} based on the provided
     * {@link QueryDefinition}, and supplies it to the consumer wrapped in a {@link StatementContext}.
     * <p>&nbsp;</p>
     * <p>You can use methods such as {@link MongoCollection#find(Bson)} to filter results, or retrieve all documents
     * if no filter is applied.</p>
     * <p>&nbsp;</p>
     * <p>Refer to the <a href="https://www.mongodb.com/docs/manual/">MongoDB Manual</a> for more information.</p>
     *
     * @param queryBuilder The query wrapper containing the name of the collection to access (typically treated as a "table name" in this library).
     * @param consumer     A consumer that operates on a {@link StatementContext} wrapping the collection.
     */
    public void executeQuery(@Nonnull final QueryDefinition queryBuilder, Consumer<StatementContext<MongoCollection<Document>>> consumer) {
        if (!openMongo()) {
            errorCouldConnect();
            return;
        }
        MongoDatabase database = mongoClient.getDatabase(preferences.getDatabaseName());
        MongoCollection<Document> collection = database.getCollection(queryBuilder.getQuery());
        try {
            consumer.accept(new StatementContext<>(collection));
        } finally {
            closeConnection();
        }
    }

    @Override
    public Connection connect() {
        String databaseName = preferences.getDatabaseName();
        String hostAddress = preferences.getHostAddress();
        String port = preferences.getPort();
        String user = preferences.getUser();
        String password = preferences.getPassword();
        String extra = preferences.getQuery();
        String credentials = "";
        if (user != null && password != null && !user.isEmpty() && !password.isEmpty())
            credentials = user + ":" + password + "@";

        ConnectionString connection = new ConnectionString(startSQLUrl + credentials + hostAddress + ":" + port + "/" + databaseName + extra);

        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connection)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1).build()).build();

        this.mongoClient = MongoClients.create(settings);
        return null;
    }

    @Override
    public void createTables() {
        if (!openMongo()) return;
        MongoDatabase database = mongoClient.getDatabase(preferences.getDatabaseName());
        for (final Entry<String, SqlQueryTable> entityTables : this.getTables().entrySet()) {
            boolean collectionExists = database.listCollectionNames().into(new ArrayList<>()).contains(entityTables.getKey());
            if (!collectionExists) {
                database.createCollection(entityTables.getKey());
            }
        }
        this.closeConnection();
    }


    @Nonnull
    @Override
    public DatabaseCommandConfig databaseConfig() {
        throw new UnsupportedOperationException("This function is not implemented for this database type yet." + this);
    }


    private void saveData(final DataWrapper dataWrapper, final SqlQueryTable tableWrapper, final MongoCollection<Document> collection) {
        Document document = new Document("_id", dataWrapper.getPrimaryValue());
        Bson filter = Filters.eq("_id", dataWrapper.getPrimaryValue());

        for (Entry<String, Object> entry : dataWrapper.getConfigurationSerialize().serialize().entrySet()) {
            Column column = getColumn(tableWrapper,entry.getKey());

            if (column == null) continue;
            Bson updatedRow = Updates.set(entry.getKey(), entry.getValue());
            UpdateResult updateResult = collection.updateOne(filter, updatedRow);
            if (updateResult.getMatchedCount() == 0)
                document.append(entry.getKey(), entry.getValue());
        }
        if (document.size() > 1)
            collection.insertOne(document);
    }

    private static Column getColumn(SqlQueryTable tableWrapper,String columnName) {
        for(Column colum :tableWrapper.getTable().getColumns()){
            if(colum.getColumnName().equals(columnName))
                return colum;
        }
        return null;
    }

    public boolean openMongo() {
        if (isClosed())
            connect();
        this.isClosed = false;
        return this.mongoClient != null;
    }

    /**
     * This method close the connection to the mongo db.
     * <p>
     * You need to use {@link #connect()} or {@link #openMongo()} for open the connection again.
     */
    public void closeConnection() {
        if (mongoClient == null) return;
        mongoClient.close();
        this.isClosed = true;
    }

    private void errorCouldConnect() {
        log.log(Level.WARNING, () -> "Could not open connection to the database.");
    }


    /**
     * This method check if connection is null or close.
     *
     * @return true if connection is closed or null.
     */

    public boolean isClosed() {
        return mongoClient == null || isClosed;
    }


    @Override
    public boolean usingHikari() {
        return false;
    }

    @Override
    public boolean isHasCastException() {
        return false;
    }
}
