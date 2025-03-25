### Media types

R ?= /devel/github.com/linkeddata/rabel/rabel.js
OUT = ../../out

PHONY: all
all: #media-types.spray
	echo ${OUT}

# touch ,gather to trigger:
#   picking up of the XML file from IANA only if changed there since our copy.
media-types.xml: ,gather
	curl  --time-cond $@ --output $@ http://www.iana.org/assignments/media-types/media-types.xml

media-types.ttl : check-tools media-types.xml $R
	$R -format=application/xml -in=media-types.xml -format=text/turtle -out="$@"

# Spray data about anything in this director or a sister directory (people) or below
# into linked data files. THIS CREATES A LOT OF FILES!!
PHONY: media-types.spray
media-types.spray : media-types.ttl $R
	for i in application audio font haptics image message model multipart text video ../person; do mkdir -p ${OUT}/media-types/$$i; done
	cp media-types.ttl index.html ${OUT}/media-types/
	cd ${OUT}/media-types/; $R -in=media-types.ttl -spray=../ -help

@PHONY:check-tools
check-tools:
	@ [ -x "$R" ] || (echo "\nSet env variable R with path to rabel.js\n"; false)
