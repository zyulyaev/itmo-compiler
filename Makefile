rc: rc.jar
	cat stub.sh rc.jar > rc
	chmod +x rc

rc.jar:
	mvn clean package

core_test: rc
	$(MAKE) -C tests/core -f checkInterpreter
	$(MAKE) -C tests/core -f checkStackMachine

expressions_test: rc
	$(MAKE) -C tests/expressions -f checkInterpreter
	$(MAKE) -C tests/expressions -f checkMachine

deep_expressions_test: rc
	$(MAKE) -C tests/deep-expressions -f checkInterpreter
	$(MAKE) -C tests/deep-expressions -f checkMachine

test: core_test expressions_test deep_expressions_test

clean:
	rm rc rc.jar

.PHONY: clean core_test expressions_test deep_expressions_test test