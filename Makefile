test:
	./add_collections.sh
	curl -XPUT -H "Content-Type: application/json" localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf -d '{ "videos": [ "-4lB5EKS5Uk", "-jlLkTtgWUk", "1FEFpk-uIYo" ] }'
	wrk -t1 -c16 -d30s --latency http://localhost:8080/collections/99757a95-f758-499f-a170-bea93b2d8bcf/stats

youtube_test:
	wrk -t2 -c256 -d30s --latency http://localhost:8081/youtube/v3/videos
