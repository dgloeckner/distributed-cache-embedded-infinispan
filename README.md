# Distributed Cache with Spring Boot and Infinispan

Uses the [DNS_PING protocol](http://www.jgroups.org/manual5/#_dns_ping) to
discover the Infinispan nodes in a K8S cluster.

## Build and try it out

* `mvn compile jib:build`
* `helm install distr-cache ./helm`
* Port-forward the service using e.g. kubectl
* `curl -X POST localhost:8080/entries`
* `curl localhost:8080/entries`


## Install Postgres
Postgres is used to persist the contents of the Infinispan cache.

It can be installed by using the following command
```sh
helm install -f helm/postgres-values.yaml postgres oci://registry-1.docker.io/bitnamicharts/postgresql #
```

## Subscribing to cache updates via server-sent events

### Receive all events until the client closes the connection
```sh
curl -N "localhost:8080/entries-stream"
```

### Receive events for a certain job
```sh
curl -N "localhost:8080/entries-stream?jobId=one
```

### Receive events for a certain job and close the stream when the Job succeeded or failed
```sh
curl -N "localhost:8080/entries-stream?jobId=one&completeAfterFinalJobStatus=true"
```