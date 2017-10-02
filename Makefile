APPNAME=segment-redis-proxy

start:
	-sudo docker ps | grep $(APPNAME) | awk '{ print $$1 }' | xargs sudo docker stop
	-sudo docker ps -a | grep $(APPNAME) | awk '{ print $$1 }' | xargs sudo docker rm
	sudo docker run -d --name $(APPNAME) -p 6379:6379 redis

test:
	mvn package