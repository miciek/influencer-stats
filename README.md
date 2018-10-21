# Influencer Stats

This application gathers and aggregates stats for your influencer social media campaigns.

## Setup
### YouTube mock server
You need YouTube mock server to be able to test performance without going over YouTube API limits. To build the image, run `docker build -t miciek/influencer-stats-youtube youtube`. To run it, execute `docker run -d --rm -p 8081:80 miciek/influencer-stats-youtube`.

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
> wrk -t1 -c1 -d30s --latency http://localhost:8081/youtube/v3/videos
Running 30s test @ http://localhost:8081/youtube/v3/videos
  1 threads and 1 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   591.88us  689.01us  29.57ms   95.23%
    Req/Sec     1.87k   295.13     2.32k    79.33%
  Latency Distribution
     50%  471.00us
     75%  548.00us
     90%  756.00us
     99%    2.53ms
  55956 requests in 30.00s, 41.51MB read
Requests/sec:   1865.16
Transfer/sec:      1.38MB
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

### Version 1
```
> wrk -t1 -c1 -d30s --latency http://localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats
Running 30s test @ http://localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats
  1 threads and 1 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     3.42ms    9.64ms 127.42ms   93.03%
    Req/Sec     0.88k   192.91     1.29k    77.26%
  Latency Distribution
     50%    0.89ms
     75%    1.20ms
     90%    8.88ms
     99%   50.33ms
  26174 requests in 30.03s, 4.74MB read
Requests/sec:    871.65
Transfer/sec:    161.73KB
```
