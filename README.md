# w3.org/ns/iana

This repository contains code that populates https://www.w3.org/ns/iana/,
from data extracted from IANA registries.

At the moment, the files are not automatically reflected on the W3C website.

## Usage

To (re-)generate the files, cd into a subdirectory of `toolchain-rebal`
(`media-types` or `link-relations`) and type `make`.

`git status` or `git diff` can then be used to inspect the changes into the `out` directory.
