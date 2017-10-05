DEPENDENCIES=dependencies/jedis-2.9.0.jar:dependencies/junit-4.12.jar:dependencies/hamcrest-core-1.3.jar:.

test:
	-rm src/*.class
	javac -cp $(DEPENDENCIES) src/*.java
	java -cp $(DEPENDENCIES) org.junit.runner.JUnitCore src.RedisProxyTest