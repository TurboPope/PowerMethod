run: pagerank
	@perl -e 'die "Please provide the normalized LINKS on stdin\n" if -t'
	./pagerank

pagerank: pagerank.cpp
	g++ -std=c++11 -Wall -Wextra -Wshadow -pedantic -O3 -pthread -o $@ $<

.PHONY: run
