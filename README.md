# w3.org/ns/iana

This repository contains code that populates https://www.w3.org/ns/iana/,
from data extracted from IANA registries.

At the moment, the files are not automatically reflected on the W3C website.

## Usage

To (re-)generate the files, cd into a subdirectory of `toolchain-rebal`
(`media-types` or `link-relations`) and type `make`.

`git status` or `git diff` can then be used to inspect the changes into the `out` directory (see below).

### Comparing Turtle files with GIT

Sometimes, Turtle files can be superficially changed, without any impact on the produced triples.
However, in our case (especially for the `out` directory),
we are more interested in the triples than in the surface syntax.

This project contains a [`.gitattributes`](https://git-scm.com/docs/gitattributes) file,
which sets the "diff type" of Turtle files to `rdf`.

This has no effect unless you customize your git confiuration file.
My own `~/.gitconfig` contains the following directive:
```
[diff "rdf"]
		textconv = rdf-canon
```
where [`rdf-canon`] is a script that takes an RDF file as parameter,
and writes its [canonical form](https://github.com/w3c/rdf-canon) on the standard output.
Git will then compare the canonical forms of the Turtle files rather than the surface syntax,
with an empty diff if the triples are not modified, even if the surface syntax is.

NB: a limitation of this method is that sometimes, a tiny change in the file may cause a big diff,
because it may cause a large number of blank nodes to be relabeled.
This never occurs, though, when only triples with no blank nodes are modified.

NB: it is possible to revert to Git's default behaviour (i.e. comparing the surface syntax)
by passing the `--no-textconv` option to `git diff`.

[`rdf-canon`]: https://gist.githubusercontent.com/pchampin/ad883b8d6a35ce4d52e5efe50645b353/raw/caca8a74e21040ccdcc425970015fea5ecdcba70/gistfile1.txt
