SOP ?= /home/pa/.cargo/bin/sop
# RML ?= /opt/BURP/target/burp.jar
RML ?= /opt/rml/rmlmapper.jar

# ⚠ the hash character ('#') MUST be escaped in the prefix declarations
RML_PREFIXES=rml:http://w3id.org/rml/
APP_PREFIXES=reg:https://www.w3.org/ns/assignments/reg\#,dct:http://purl.org/dc/terms/

.PHONY: dispatch
dispatch: out.nq .check-tools
	${SOP} parse out.nq ! dispatch https://www.w3.org/ns/iana/ -d ../../out --overwrite --relativize -p "${APP_PREFIXES}"
	cp ../../toolchain-rabel/media-types/index.html ../../out/media-types/index.html

.PHONY: inspect
inspect: out.nq .check-tools
	${SOP} parse out.nq ! relativize https://www.w3.org.ns/iana/media-types/ ! serialize -f trig -p "${APP_PREFIXES}"

out.nq: map.ttl media-types.xml .check-tools
	@if grep '<UNDEFINED:' map.ttl; then echo >&2 "typos detected in map.ttl"; false; fi
	java -jar ${RML} -m "$<" -o "$@"

.PHONY: clean
clean:
	rm -f map.jsonld map.ttl out.nq

.PHONY: clean-all
clean-all: clean
	rm -f media-types.xml .check-tools
	
media-types.xml:
	cp ../../toolchain-rabel/media-types/media-types.xml "$@"

%.ttl: %.yml rml_context.jsonld .check-tools
	${SOP} parse -u "$<" ! serialize -o "$@" -p "${RML_PREFIXES},${APP_PREFIXES}"

.check-tools:
	@ [ -x "${YQ}" ] || (echo "\nSet env variable YQ with path to yq\nhttps://github.com/mikefarah/yq\n"; false)
	@ [ -x "${SOP}" ] || (echo "\nSet env variable SOP with path to sop\nhttps://github.com/pchampin/sophia-cli\n"; false)
	@ [ -f "${RML}" ] || (echo "\nSet env variable RML with path to rmlmapper.jar\nhttps://github.com/RMLio/rmlmapper-java\n"; false)
	touch "$@"
