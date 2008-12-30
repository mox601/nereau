#!/bin/sh
if [ $# -ne 2 ]; then
	java -classpath .:lib/postgresql-8.3dev-600.jdbc4.jar:lib/htmlparser.jar:lib/yahoo_search-2.0.1.jar temp.ProvaEspansioni
else
	java -classpath .:lib/postgresql-8.3dev-600.jdbc4.jar:lib/htmlparser.jar:lib/yahoo_search-2.0.1.jar temp.ProvaEspansioni "$1" "$2"
fi

