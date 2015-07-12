#include <algorithm>
#include <cstdio>
#include <iostream>
#include <stdexcept>
#include <string>
#include <thread>
#include <unordered_map>
#include <unordered_set>


static constexpr unsigned int NTHREADS   = 4;
static constexpr unsigned int ITERATIONS = 8;
static double alpha = 0.1;
static std::unordered_map<std::string, unsigned int>  name_to_id;
static std::vector<std::pair<std::string, double> >   ranks;
static std::vector<std::unordered_set<unsigned int> > graph;


static unsigned int insert_node(const std::string& name)
{
    auto it = name_to_id.find(name);
    if (it != name_to_id.end())
        return it->second;

    auto id = graph.size();
    name_to_id.insert({name, id});
    ranks.push_back({name, 0.0});
    graph.push_back(std::unordered_set<unsigned int>());
    return id;
}


static void parse_links()
{
    for (;;)
    {
        std::string src;
        if (!std::getline(std::cin, src))
            break;

        std::string dst;
        if (!std::getline(std::cin, dst))
            throw std::runtime_error("odd number of elements in input");

        auto src_id = insert_node(src);
        auto dst_id = insert_node(dst);
        graph[src_id].insert(dst_id);
    }
}


static double matrix(unsigned int i, unsigned int j)
{
    auto& edges = graph[i];
    auto  o_i   = edges.size();

    if (o_i)
    {
        double p = alpha / graph.size();
        if (edges.find(j) != edges.end())
            p += (1.0 - alpha) / o_i;
        return p;
    }

    return 1.0 / graph.size();
}


static double calc(unsigned int j, double a)
{
    double result = 0.0;
    for (unsigned int i = 0; i < graph.size(); ++i)
        result += a * matrix(i, j);
    return result;
}


static void thread_func(unsigned int pos, unsigned int end)
{
    for(; pos < end; ++pos)
        for (unsigned int i = 0; i < ITERATIONS; ++i)
            ranks[pos].second = calc(pos, ranks[pos].second);
}


static void parallel_calc()
{
    std::vector<std::thread> threads;

    unsigned int slice = graph.size() / NTHREADS;
    for (unsigned int i = 0; i < NTHREADS; ++i)
    {
        unsigned int start  = i * slice;
        unsigned int length = i == NTHREADS - 1 ? graph.size() - start : slice;
        threads.push_back(std::thread(thread_func, start, start + length));
    }

    for (auto& t : threads)
        t.join();
}


static bool comp(const std::pair<std::string, double>& a,
                 const std::pair<std::string, double>& b)
{   return a.second < b.second; }


int main()
{
    parse_links();
    for (auto& r : ranks)
        r.second = 1.0 / graph.size();

    parallel_calc();

    std::sort(ranks.begin(), ranks.end(), comp);
    for (const auto& r : ranks)
        std::cout << r.first << '\t' << r.second << '\n';
    return 0;
}
