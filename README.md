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