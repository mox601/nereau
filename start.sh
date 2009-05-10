#!/bin/sh

java -classpath .:lib/postgresql-8.3dev-600.jdbc4.jar:lib/htmlparser.jar:lib/yahoo_search-2.0.1.jar server.MainServer 9994
