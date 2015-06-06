build/class-blueprint.dot: src/net/yura/domination/engine/core/RiskGame.java
	blueprint --debug --graphviz $^ > $@

build/class-blueprint.png: build/class-blueprint.dot
	dot -Tpng $< > $@

metrics: build/class-blueprint.png
