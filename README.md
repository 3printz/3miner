# minerz

Blockchain miner for 3d print app. When starting the service it will creates cassandra database,
table and elastic indexes with elassandra api

# index 

## transactions

```
{
  "settings":{
    "keyspace": "zchain"
  },
  "mappings": {
    "transactions" : {
      "discover" : ".*"
    }
  }
}
```

## blocks

```
{
  "settings":{
    "keyspace": "zchain"
  },
  "mappings": {
    "blocks" : {
      "discover" : ".*"
    }
  }
}
```

## cluster/index state

```
http://localhost:9200/_cluster/state?pretty=true
http://localhost:9200/blocks?pretty=true
http://localhost:9200/transactions?pretty=true
```
