# Influencer Stats

This application gathers and aggregates stats for your influencer social media campaigns.

## Setup
### YouTube mock server
You need YouTube mock server to be able to test performance without going over YouTube API limits. To build the image, run `docker build -t miciek/influencer-stats-youtube youtube`. To run it, execute `docker run -d --rm --name youtube -p 8081:80 miciek/influencer-stats-youtube`.

### Running the application
After executing `sbt run`, you need to configure the first `collection`:

```
curl -XPUT -H "Content-Type: application/json" localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf -d '{ "videos": [ "-4lB5EKS5Uk", "-jlLkTtgWUk", "1FEFpk-uIYo" ] }'
```

Then, you will be able to fetch the stats for videos in this `collection`:

```
curl localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats
```

## Performance tests
To run performance tests, you will need [wrk](https://github.com/wg/wrk). To analyse what's going on inside the application, please install [async-profiler](https://github.com/jvm-profiling-tools/async-profiler).

Before starting, let's first establish the performance of our YouTube mock server:

```
> wrk -t2 -c256 -d30s --latency http://localhost:8081/youtube/v3/videos
  Running 30s test @ http://localhost:8081/youtube/v3/videos
    2 threads and 256 connections
    Thread Stats   Avg      Stdev     Max   +/- Stdev
      Latency    11.86ms    2.10ms  44.67ms   95.00%
      Req/Sec    10.87k   614.63    12.14k    79.17%
    Latency Distribution
       50%   11.72ms
       75%   11.98ms
       90%   12.36ms
       99%   23.59ms
    649268 requests in 30.01s, 486.65MB read
    Socket errors: connect 0, read 124, write 0, timeout 0
  Requests/sec:  21634.72
  Transfer/sec:     16.22MB
```

Additionally, let's see what is the performance of collections with no videos (no additional requests to YouTube server are made):

```
wrk -t1 -c1 -d30s --latency http://localhost:8080/collections/39757a95-e758-499f-a170-bea93b2d8bca/stats
Running 30s test @ http://localhost:8080/collections/39757a95-e758-499f-a170-bea93b2d8bca/stats
  1 threads and 1 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   682.73us    3.35ms  47.60ms   97.16%
    Req/Sec     5.83k   776.99     6.53k    83.67%
  Latency Distribution
     50%  155.00us
     75%  164.00us
     90%  196.00us
     99%   21.81ms
  174201 requests in 30.01s, 29.41MB read
Requests/sec:   5804.37
Transfer/sec:      0.98MB
```

Remember that each test should be run several times to warm up JVM.

### Flamegraph generation
To compare different versions, we will use [flamegraphs](http://www.brendangregg.com/flamegraphs.html). The command below generates flamegraph for the load-tested application (should be started after around 15s of `wrk`):

```
jps # to get the <PID> of the application
cd async-profiler
./profiler.sh -d 15 -f /tmp/flamegraph.svg <PID>
```

Generated flamegraphs are stored in [flamegraphs](./flamegraphs) directory.

### Version 1 (log-all/list-state/akka-http)
```
> wrk -t1 -c16 -d30s --timeout 10s --latency http://localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats
  Running 30s test @ http://localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats
    1 threads and 16 connections
    Thread Stats   Avg      Stdev     Max   +/- Stdev
      Latency     2.46s   219.22ms   3.39s    76.32%
      Req/Sec    14.35     12.33    59.00     71.58%
    Latency Distribution
       50%    2.40s
       75%    2.55s
       90%    2.85s
       99%    3.35s
    190 requests in 30.06s, 35.25KB read
  Requests/sec:      6.32
  Transfer/sec:      1.17KB
```

### Version 2 (logs-max-1kps-list/list-state/akka-http)
```
> wrk -t1 -c16 -d30s --timeout 10s --latency http://localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats
  Running 30s test @ http://localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats
    1 threads and 16 connections
    Thread Stats   Avg      Stdev     Max   +/- Stdev
      Latency   261.43ms   97.92ms 760.70ms   70.47%
      Req/Sec    61.60     26.42   150.00     68.60%
    Latency Distribution
       50%  256.15ms
       75%  317.90ms
       90%  389.43ms
       99%  551.64ms
    1832 requests in 30.01s, 339.92KB read
  Requests/sec:     61.05
  Transfer/sec:     11.33KB
```

### Version 3 (logs-max-1kps-array/list-state/akka-http)
```
> wrk -t1 -c16 -d30s --timeout 10s --latency http://localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats
  Running 30s test @ http://localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats
    1 threads and 16 connections
    Thread Stats   Avg      Stdev     Max   +/- Stdev
      Latency    51.43ms   17.43ms 239.30ms   77.16%
      Req/Sec   314.13     54.35   424.00     71.91%
    Latency Distribution
       50%   47.92ms
       75%   58.89ms
       90%   72.63ms
       99%  111.66ms
    9406 requests in 30.08s, 1.70MB read
  Requests/sec:    312.72
  Transfer/sec:     58.02KB
```

### Version 4 (logs-max-1kps-array/triemap-state/akka-http)
```
> wrk -t1 -c16 -d30s --timeout 10s --latency http://localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats
  Running 30s test @ http://localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats
    1 threads and 16 connections
    Thread Stats   Avg      Stdev     Max   +/- Stdev
      Latency    13.98ms   13.24ms 152.48ms   92.64%
      Req/Sec     1.35k   324.33     1.85k    75.25%
    Latency Distribution
       50%   10.44ms
       75%   12.79ms
       90%   19.87ms
       99%   86.41ms
    40329 requests in 30.02s, 7.31MB read
  Requests/sec:   1343.54
  Transfer/sec:    249.29KB
```

### Version 5 (logs-max-1kps-array/triemap-state/http4s)
```
> wrk -t1 -c16 -d30s --timeout 10s --latency http://localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats
  Running 30s test @ http://localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats
    1 threads and 16 connections
    Thread Stats   Avg      Stdev     Max   +/- Stdev
      Latency    30.36ms  103.88ms   1.01s    96.19%
      Req/Sec     1.39k   242.95     1.71k    87.41%
    Latency Distribution
       50%   11.01ms
       75%   12.66ms
       90%   16.42ms
       99%  696.71ms
    39701 requests in 30.05s, 6.21MB read
  Requests/sec:   1320.96
  Transfer/sec:    211.56KB
```

### Version 6 (logs-max-1kps-array/triemap-state/http4s/hammock)
```
> wrk -t1 -c16 -d30s --latency http://localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats
  Running 30s test @ http://localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats
    1 threads and 16 connections
    Thread Stats   Avg      Stdev     Max   +/- Stdev
      Latency     4.66ms    1.45ms  26.27ms   75.16%
      Req/Sec     3.45k   276.16     3.91k    64.33%
    Latency Distribution
       50%    4.49ms
       75%    5.36ms
       90%    6.33ms
       99%    9.19ms
    103117 requests in 30.01s, 16.13MB read
  Requests/sec:   3436.23
  Transfer/sec:    550.33KB
```

### Version 7 (logs-max-1kps-array/triemap-state/http4s/hammock/caching)
```
> wrk -t1 -c16 -d30s --latency http://localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats
  Running 30s test @ http://localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats
    1 threads and 16 connections
    Thread Stats   Avg      Stdev     Max   +/- Stdev
      Latency   453.00us    0.94ms  15.36ms   97.61%
      Req/Sec    38.25k     3.57k   50.75k    83.67%
    Latency Distribution
       50%  319.00us
       75%  398.00us
       90%  485.00us
       99%    5.77ms
    1142285 requests in 30.01s, 178.66MB read
  Requests/sec:  38062.91
  Transfer/sec:      5.95MB
```