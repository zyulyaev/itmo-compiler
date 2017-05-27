rc: rc.jar
	echo "#!java -jar" > rc
	cat rc.jar >> rc
	chmod +x rc

rc.jar:
	mvn clean package

test: rc
	$(MAKE) -C tests/core -f checkInterpreter

clean:
	rm rc rc.jar

.PHONY: clean test