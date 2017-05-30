rc: rc.jar
	cat stub.sh rc.jar > rc
	chmod +x rc

rc.jar: runtime
	mvn clean package -DskipTests

runtime:
	$(MAKE) -C runtime

core_test: rc
	$(MAKE) -C tests/core

expressions_test: rc
	$(MAKE) -C tests/expressions

deep_expressions_test: rc
	$(MAKE) -C tests/deep-expressions

test: core_test expressions_test deep_expressions_test

clean:
	rm rc rc.jar

.PHONY: clean core_test expressions_test deep_expressions_test test