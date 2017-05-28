rc: rc.jar
	cat stub.sh rc.jar > rc
	chmod +x rc

rc.jar:
	mvn clean package

test: rc
	$(MAKE) -C tests/core -f checkInterpreter
	$(MAKE) -C tests/core -f checkStackMachine

clean:
	rm rc rc.jar

.PHONY: clean test