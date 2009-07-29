Source code in this package is provided under the terms of the GNU
Affero General Public License (AGPL) version 3 (see LICENSE.txt).

This software aggregates the results produced by the owlwg-test software
at http://github.com/msmithcp/owlwg-test/ and produces the OWL WG test
suite status page.

It consists of two separate programs


+ com.clarkparsia.owlwg.presentation.AggregateTestWikiFormatter

  Produces a summary of the test suite and does not read any test
  results.

  It requires a single argument which should be a URI to the complete
  test collection (e.g., http://wiki.webont.org/exports/all.rdf)


+ com.clarkparsia.owlwg.presentation.AggregateResultWikiFormatter

  Produces a report of the test results.

  Accepts multiple arguments.  The first argument is the same as the
  test summary program.  Subsequent arguments (as many as required)
  should be URIs to ontologies containing test run results.

Comments to Michael Smith <msmith@clarkparsia.com>
