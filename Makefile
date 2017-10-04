APPNAME=segment-redis-proxy

# test:
# 	# Currently needs "sudo docker daemon" running in another terminal
# 	-sudo docker ps | grep $(APPNAME) | awk '{ print $$1 }' | xargs sudo docker stop
# 	-sudo docker ps -a | grep $(APPNAME) | awk '{ print $$1 }' | xargs sudo docker rm
# 	-sudo docker run -d --name $(APPNAME) -p 6379:6379 redis
# 	mvn package

DEPENDENCIES=dependencies/jedis-2.9.0.jar:dependencies/junit-4.12.jar:dependencies/hamcrest-core-1.3.jar:.

test:
	-rm src/*.class
	javac -cp $(DEPENDENCIES) src/*.java
	java -cp $(DEPENDENCIES) org.junit.runner.JUnitCore src.RedisProxyTest