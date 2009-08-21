package com.clarkparsia.owlwg.presentation;

import static com.clarkparsia.owlwg.Constants.RESULTS_ONTOLOGY_PHYSICAL_URI;
import static com.clarkparsia.owlwg.Constants.TEST_ONTOLOGY_PHYSICAL_URI;
import static com.clarkparsia.owlwg.presentation.Utilities.match;
import static com.clarkparsia.owlwg.presentation.Utilities.possibleReasoningRunTypes;
import static com.clarkparsia.owlwg.presentation.Utilities.possibleSyntaxConstraintTests;
import static com.clarkparsia.owlwg.runner.ReadOnlyTestRunner.testRunner;
import static com.clarkparsia.owlwg.testcase.filter.ConjunctionFilter.and;
import static com.clarkparsia.owlwg.testcase.filter.DisjunctionFilter.or;
import static com.clarkparsia.owlwg.testcase.filter.NegationFilter.not;
import static com.clarkparsia.owlwg.testcase.filter.SatisfiedSyntaxConstraintFilter.DL;
import static com.clarkparsia.owlwg.testcase.filter.SatisfiedSyntaxConstraintFilter.EL;
import static com.clarkparsia.owlwg.testcase.filter.SatisfiedSyntaxConstraintFilter.QL;
import static com.clarkparsia.owlwg.testcase.filter.SatisfiedSyntaxConstraintFilter.RL;
import static com.clarkparsia.owlwg.testcase.filter.StatusFilter.APPROVED;
import static com.clarkparsia.owlwg.testcase.filter.StatusFilter.EXTRACREDIT;
import static com.clarkparsia.owlwg.testcase.filter.StatusFilter.PROPOSED;
import static com.clarkparsia.owlwg.testcase.filter.SemanticsFilter.DIRECT;
import static com.clarkparsia.owlwg.testcase.filter.SemanticsFilter.RDF;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.stringtemplate.CommonGroupLoader;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

import com.clarkparsia.owlwg.TestCollection;
import com.clarkparsia.owlwg.owlapi2.testcase.impl.OwlApi2TestCaseFactory;
import com.clarkparsia.owlwg.runner.TestRunner;
import com.clarkparsia.owlwg.testcase.SyntaxConstraint;
import com.clarkparsia.owlwg.testcase.TestCase;
import com.clarkparsia.owlwg.testcase.filter.StatusFilter;
import com.clarkparsia.owlwg.testrun.RunResultType;
import com.clarkparsia.owlwg.testrun.RunTestType;
import com.clarkparsia.owlwg.testrun.SyntaxConstraintRun;
import com.clarkparsia.owlwg.testrun.TestRunResult;
import com.clarkparsia.owlwg.testrun.TestRunResultParser;
import com.clarkparsia.owlwg.testrun.TestRunResultVisitor;

/**
 * <p>
 * Title: Aggregate Result Wiki Formatter
 * </p>
 * <p>
 * Description: Aggregate test results for presentation in the wg wiki.
 * </p>
 * <p>
 * Copyright: Copyright &copy; 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <a
 * href="http://clarkparsia.com/"/>http://clarkparsia.com/</a>
 * </p>
 * 
 * @author Mike Smith &lt;msmith@clarkparsia.com&gt;
 */
public class AggregateResultWikiFormatter {

	private final static List<TestRunner>	dlReasoners, elReasoners, qlReasoners, rlReasoners, rdfReasoners, syntaxCheckers, allReasoners;
	private final static Logger				log;
	private final static TestRunResult		missingResult;

	static {
		log = Logger.getLogger( AggregateResultWikiFormatter.class.getCanonicalName() );

		StringTemplateGroupLoader loader = new CommonGroupLoader(
				"com/clarkparsia/owlwg/presentation/templates", new StringTemplateErrorListener() {

					public void error(String msg, Throwable e) {
						log.log( Level.SEVERE, msg, e );
					}

					public void warning(String msg) {
						log.warning( msg );
					}

				} );
		StringTemplateGroup.registerGroupLoader( loader );

		dlReasoners = new ArrayList<TestRunner>();
		dlReasoners.add( testRunner( URI.create( "http://clarkparsia.com/pellet" ), "Pellet" ) );
		dlReasoners.add( testRunner( URI.create( "http://hermit-reasoner.com/" ), "HermiT" ) );
		dlReasoners.add( testRunner( URI.create( "http://owl.cs.manchester.ac.uk/fact++/" ), "FaCT++" ) );

		elReasoners = new ArrayList<TestRunner>( dlReasoners );
		elReasoners.add( testRunner( URI.create( "http://lat.inf.tu-dresden.de/systems/cel/" ),	"CEL" ) );
		elReasoners.add( testRunner( URI.create( "http://kt.abdn.ac.uk/wiki/Projects/REL" ),	"REL" ) );

		qlReasoners = new ArrayList<TestRunner>( dlReasoners );
		qlReasoners.add( testRunner( URI.create( "http://www.dis.uniroma1.it/~quonto/" ), "QuOnto" ) );
		qlReasoners.add( testRunner( URI.create( "http://kt.abdn.ac.uk/wiki/Quill" ), "Quill" ) );

		rlReasoners = new ArrayList<TestRunner>( dlReasoners );
		rlReasoners.add( testRunner( URI.create( "http://www.oracle.com/technology/tech/semantic_technologies/index.html#owlreasoner" ), "Oracle Database 11g OWL Reasoner" ) );
		rlReasoners.add( testRunner( URI.create( "http://jena.sourceforge.net/inference/OWL2RLExpt.html" ), "Jena" ) );

		rdfReasoners = new ArrayList<TestRunner>( );
		rdfReasoners.add( testRunner( URI.create( "http://www.oracle.com/technology/tech/semantic_technologies/index.html#owlreasoner" ), "Oracle Database 11g OWL Reasoner" ) );
		rdfReasoners.add( testRunner( URI.create( "http://www.ivan-herman.net/Misc/2008/owlrl/" ), "OWLRL" ) );
		rdfReasoners.add( testRunner( URI.create( "http://jena.sourceforge.net/inference/OWL2RLExpt.html" ), "Jena" ) );

		syntaxCheckers = new ArrayList<TestRunner>();
		syntaxCheckers.add( testRunner( URI.create( "http://owlapi.sourceforge.net/" ), "OWLAPIv2" ) );
		syntaxCheckers.add( testRunner( URI.create( "http://dipper.csd.abdn.ac.uk:8080/OWL2ProfileChecker/" ), "Aberdeen Profile Checker" ) );
		
		allReasoners = new ArrayList<TestRunner>(elReasoners);
		allReasoners.add( testRunner( URI.create( "http://www.dis.uniroma1.it/~quonto/" ), "QuOnto" ) );
		allReasoners.add( testRunner( URI.create( "http://kt.abdn.ac.uk/wiki/Quill" ), "Quill" ) );
		allReasoners.add( testRunner( URI.create( "http://www.oracle.com/technology/tech/semantic_technologies/index.html#owlreasoner" ), "Oracle Database 11g OWL Reasoner" ) );
		allReasoners.add( testRunner( URI.create( "http://jena.sourceforge.net/inference/OWL2RLExpt.html" ), "Jena" ) );
		allReasoners.add( testRunner( URI.create( "http://www.ivan-herman.net/Misc/2008/owlrl/" ), "OWLRL" ) );		

		Comparator<TestRunner> nameComparator = new Comparator<TestRunner>() {
			public int compare(TestRunner arg0, TestRunner arg1) {
				return arg0.getName().compareToIgnoreCase( arg1.getName() );
			}

		};

		Collections.sort( dlReasoners, nameComparator );
		Collections.sort( elReasoners, nameComparator );
		Collections.sort( qlReasoners, nameComparator );
		Collections.sort( rlReasoners, nameComparator );
		Collections.sort( rdfReasoners, nameComparator );
		Collections.sort( allReasoners, nameComparator );
		Collections.sort( syntaxCheckers, nameComparator );

		missingResult = new TestRunResult() {

			public RunTestType getTestType() {
				return null;
			}

			public TestRunner getTestRunner() {
				return null;
			}

			public TestCase getTestCase() {
				return null;
			}

			public RunResultType getResultType() {
				return null;
			}

			public String getDetails() {
				return null;
			}

			public void accept(TestRunResultVisitor visitor) {
			}
		};
	}

	private static TestRunResult find(Collection<TestRunResult> results, TestCase test,
			TestRunner runner, RunTestType type) {
		for( TestRunResult r : results ) {
			if( r.getTestCase().equals( test ) && r.getTestRunner().equals( runner )
					&& r.getTestType().equals( type ) )
				return r;
		}
		return null;
	}

	private static TestRunResult find(Collection<TestRunResult> results, TestCase test,
			TestRunner runner, SyntaxConstraint constraint) {
		for( TestRunResult r : results ) {
			if( r.getTestCase().equals( test ) && r.getTestRunner().equals( runner )
					&& r.getTestType().equals( RunTestType.SYNTAX_CONSTRAINT ) ) {
				final SyntaxConstraintRun scr = (SyntaxConstraintRun) r;
				if( scr.getConstraint().equals( constraint ) )
					return r;
			}
		}
		return null;
	}

	public static void main(String[] args) {

		if( args.length < 2 )
			throw new IllegalArgumentException();

		final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		final StringTemplateGroup stg = StringTemplateGroup.loadGroup( "result-summary-wiki" );
		final StringTemplate template = stg.getInstanceOf( "result-summary" );

		try {
			/*
			 * Load the test and results ontology from local files before
			 * reading the test cases, otherwise import of them is likely to
			 * fail.
			 */
			manager.loadOntologyFromPhysicalURI( TEST_ONTOLOGY_PHYSICAL_URI );
			manager.loadOntologyFromPhysicalURI( RESULTS_ONTOLOGY_PHYSICAL_URI );

			OWLOntology casesOntology = manager.loadOntologyFromPhysicalURI( URI.create( args[0] ) );
			List<OWLOntology> resultsOntologies = new ArrayList<OWLOntology>();
			for( int i = 1; i < args.length; i++ ) {
				resultsOntologies
						.add( manager.loadOntologyFromPhysicalURI( URI.create( args[i] ) ) );
			}

			TestCollection<OWLOntology> caseCol = new TestCollection<OWLOntology>(
					new OwlApi2TestCaseFactory(), casesOntology );
			Map<String, TestCase<OWLOntology>> caseMap = new HashMap<String, TestCase<OWLOntology>>();
			for( TestCase<OWLOntology> c : caseCol )
				caseMap.put( c.getIdentifier(), c );

			List<TestCase<OWLOntology>> cases = caseCol.asList();
			Collections.sort( cases, new Comparator<TestCase<OWLOntology>>() {
				public int compare(TestCase<OWLOntology> o1, TestCase<OWLOntology> o2) {
					return o1.getIdentifier().compareTo( o2.getIdentifier() );
				}
			} );

			TestRunResultParser parser = new TestRunResultParser();
			List<TestRunResult> results = new ArrayList<TestRunResult>();
			for( OWLOntology o : resultsOntologies )
				results.addAll( parser.getResults( o, caseMap ) );

			/* Results by test case */
			Map<TestCase<OWLOntology>, List<TestRunResult>> caseToResult = new HashMap<TestCase<OWLOntology>, List<TestRunResult>>();
			for( TestCase<OWLOntology> t : cases )
				caseToResult.put( t, new ArrayList<TestRunResult>() );
			for( TestRunResult r : results ) {
				if ( r != null )
					caseToResult.get( r.getTestCase() ).add( r );
			}

			/*
			 * General info about report
			 */
			{
				SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mmZ" );
				template.setAttribute( "timestamp", df.format( new Date() ) );
			}

			/*
			 * DL reasoners
			 */
			template.setAttribute( "dl_reasoners", dlReasoners );
			for( Object[] pair : new Object[][] {
					new Object[] { APPROVED, "approved" }, new Object[] { PROPOSED, "proposed" },
					new Object[] { EXTRACREDIT, "extracredit" }, } ) {
				final StatusFilter f = (StatusFilter) pair[0];
				List<Object> dlResults = new ArrayList<Object>();
				for( final TestCase c : match( and( DIRECT, and( DL, f ) ), cases ) ) {
					List<RunTestType> testTypes = new ArrayList<RunTestType>(
							possibleReasoningRunTypes( c ) );
					Collections.sort( testTypes );
					List<TestRunResult> caseRes = caseToResult.get( c );
					final List<Object> byTypeList = new ArrayList<Object>();
					for( final RunTestType t : testTypes ) {
						final List<TestRunResult> ctRes = new ArrayList<TestRunResult>();
						for( TestRunner runner : dlReasoners ) {
							TestRunResult trr = find( caseRes, c, runner, t );
							if( trr == null )
								trr = missingResult;
							ctRes.add( trr );
						}
						Object o = new Object() {
							public List<TestRunResult> getResults() {
								return ctRes;
							}

							public RunTestType getType() {
								return t;
							}
						};
						byTypeList.add( o );
					}

					dlResults.add( new Object() {
						public final List<Object>	byType		= byTypeList;
						public final TestCase		testCase	= c;
					} );
				}
				template.setAttribute( "dl_results_" + pair[1], dlResults );
			}

			/*
			 * EL Reasoners
			 */
			template.setAttribute( "el_reasoners", elReasoners );
			for( Object[] pair : new Object[][] {
					new Object[] { APPROVED, "approved" }, new Object[] { PROPOSED, "proposed" },
					new Object[] { EXTRACREDIT, "extracredit" }, } ) {
				final StatusFilter f = (StatusFilter) pair[0];
				List<Object> elResults = new ArrayList<Object>();
				for( final TestCase c : match( and( DIRECT, and( EL, f ) ), cases ) ) {
					List<RunTestType> testTypes = new ArrayList<RunTestType>(
							possibleReasoningRunTypes( c ) );
					Collections.sort( testTypes );
					List<TestRunResult> caseRes = caseToResult.get( c );
					final List<Object> byTypeList = new ArrayList<Object>();
					for( final RunTestType t : testTypes ) {
						final List<TestRunResult> ctRes = new ArrayList<TestRunResult>();
						for( TestRunner runner : elReasoners ) {
							TestRunResult trr = find( caseRes, c, runner, t );
							if( trr == null )
								trr = missingResult;
							ctRes.add( trr );
						}
						Object o = new Object() {
							public List<TestRunResult> getResults() {
								return ctRes;
							}

							public RunTestType getType() {
								return t;
							}
						};
						byTypeList.add( o );
					}

					elResults.add( new Object() {
						public final List<Object>	byType		= byTypeList;
						public final TestCase		testCase	= c;
					} );
				}
				template.setAttribute( "el_results_" + pair[1], elResults );
			}

			/*
			 * QL Reasoners
			 */
			template.setAttribute( "ql_reasoners", qlReasoners );
			for( Object[] pair : new Object[][] {
					new Object[] { APPROVED, "approved" }, new Object[] { PROPOSED, "proposed" },
					new Object[] { EXTRACREDIT, "extracredit" }, } ) {
				final StatusFilter f = (StatusFilter) pair[0];
				List<Object> qlResults = new ArrayList<Object>();
				for( final TestCase c : match( and( DIRECT, and( QL, f ) ), cases ) ) {
					List<RunTestType> testTypes = new ArrayList<RunTestType>(
							possibleReasoningRunTypes( c ) );
					Collections.sort( testTypes );
					List<TestRunResult> caseRes = caseToResult.get( c );
					final List<Object> byTypeList = new ArrayList<Object>();
					for( final RunTestType t : testTypes ) {
						final List<TestRunResult> ctRes = new ArrayList<TestRunResult>();
						for( TestRunner runner : qlReasoners ) {
							TestRunResult trr = find( caseRes, c, runner, t );
							if( trr == null )
								trr = missingResult;
							ctRes.add( trr );
						}
						Object o = new Object() {
							public List<TestRunResult> getResults() {
								return ctRes;
							}

							public RunTestType getType() {
								return t;
							}
						};
						byTypeList.add( o );
					}

					qlResults.add( new Object() {
						public final List<Object>	byType		= byTypeList;
						public final TestCase		testCase	= c;
					} );
				}
				template.setAttribute( "ql_results_" + pair[1], qlResults );
			}

			/*
			 * RL Reasoners
			 */
			template.setAttribute( "rl_reasoners", rlReasoners );
			for( Object[] pair : new Object[][] {
					new Object[] { APPROVED, "approved" }, new Object[] { PROPOSED, "proposed" },
					new Object[] { EXTRACREDIT, "extracredit" }, } ) {
				final StatusFilter f = (StatusFilter) pair[0];
				List<Object> rlResults = new ArrayList<Object>();
				for( final TestCase c : match( and( DIRECT, and( RL, f ) ), cases ) ) {
					List<RunTestType> testTypes = new ArrayList<RunTestType>(
							possibleReasoningRunTypes( c ) );
					Collections.sort( testTypes );
					List<TestRunResult> caseRes = caseToResult.get( c );
					final List<Object> byTypeList = new ArrayList<Object>();
					for( final RunTestType t : testTypes ) {
						final List<TestRunResult> ctRes = new ArrayList<TestRunResult>();
						for( TestRunner runner : rlReasoners ) {
							TestRunResult trr = find( caseRes, c, runner, t );
							if( trr == null )
								trr = missingResult;
							ctRes.add( trr );
						}
						Object o = new Object() {
							public List<TestRunResult> getResults() {
								return ctRes;
							}

							public RunTestType getType() {
								return t;
							}
						};
						byTypeList.add( o );
					}

					rlResults.add( new Object() {
						public final List<Object>	byType		= byTypeList;
						public final TestCase		testCase	= c;
					} );
				}
				template.setAttribute( "rl_results_" + pair[1], rlResults );
			}

			/*
			 * RDF-based Reasoners (OWL Full)
			 */
			template.setAttribute( "rdf_reasoners", rdfReasoners );
			for( Object[] pair : new Object[][] {
					new Object[] { APPROVED, "approved" }, new Object[] { PROPOSED, "proposed" },
					new Object[] { EXTRACREDIT, "extracredit" }, } ) {
				final StatusFilter f = (StatusFilter) pair[0];
				List<Object> rdfResults = new ArrayList<Object>();
				for( final TestCase c : match( and( or( not(DIRECT), not(DL) ), f ), cases ) ) {
				//for( final TestCase c : match( and( RDF, f ), cases ) ) {
					List<RunTestType> testTypes = new ArrayList<RunTestType>(
							possibleReasoningRunTypes( c ) );
					Collections.sort( testTypes );
					List<TestRunResult> caseRes = caseToResult.get( c );
					final List<Object> byTypeList = new ArrayList<Object>();
					for( final RunTestType t : testTypes ) {
						final List<TestRunResult> ctRes = new ArrayList<TestRunResult>();
						for( TestRunner runner : rdfReasoners ) {
							TestRunResult trr = find( caseRes, c, runner, t );
							if( trr == null )
								trr = missingResult;
							ctRes.add( trr );
						}
						Object o = new Object() {
							public List<TestRunResult> getResults() {
								return ctRes;
							}

							public RunTestType getType() {
								return t;
							}
						};
						byTypeList.add( o );
					}

					rdfResults.add( new Object() {
						public final List<Object>	byType		= byTypeList;
						public final TestCase		testCase	= c;
					} );
				}
				template.setAttribute( "rdf_results_" + pair[1], rdfResults );
			}

			/*
			 * All Reasoners (check proposed tests)
			 */
			template.setAttribute( "all_reasoners", allReasoners );
			for( Object[] pair : new Object[][] {
					new Object[] { PROPOSED, "proposed" } } ) {
				final StatusFilter f = (StatusFilter) pair[0];
				List<Object> allResults = new ArrayList<Object>();
				for( final TestCase c : match( f, cases ) ) {
					List<RunTestType> testTypes = new ArrayList<RunTestType>(
							possibleReasoningRunTypes( c ) );
					Collections.sort( testTypes );
					List<TestRunResult> caseRes = caseToResult.get( c );
					final List<Object> byTypeList = new ArrayList<Object>();
					for( final RunTestType t : testTypes ) {
						final List<TestRunResult> ctRes = new ArrayList<TestRunResult>();
						for( TestRunner runner : allReasoners ) {
							TestRunResult trr = find( caseRes, c, runner, t );
							if( trr == null )
								trr = missingResult;
							ctRes.add( trr );
						}
						Object o = new Object() {
							public List<TestRunResult> getResults() {
								return ctRes;
							}

							public RunTestType getType() {
								return t;
							}
						};
						byTypeList.add( o );
					}

					allResults.add( new Object() {
						public final List<Object>	byType		= byTypeList;
						public final TestCase		testCase	= c;
					} );
				}
				template.setAttribute( "all_results_" + pair[1], allResults );
			}

			/*
			 * Syntax checkers
			 */
			template.setAttribute( "syntax_checkers", syntaxCheckers );
			for( Object[] pair : new Object[][] {
					new Object[] { APPROVED, "approved" }, new Object[] { PROPOSED, "proposed" },
					new Object[] { EXTRACREDIT, "extracredit" }, } ) {
				final StatusFilter f = (StatusFilter) pair[0];
				List<Object> syntaxResults = new ArrayList<Object>();
				for( final TestCase c : match( f, cases ) ) {
					List<SyntaxConstraint> constraints = new ArrayList<SyntaxConstraint>(
							possibleSyntaxConstraintTests( c ) );
					Collections.sort( constraints );
					List<TestRunResult> caseRes = caseToResult.get( c );
					final List<Object> byConstraintList = new ArrayList<Object>();
					for( final SyntaxConstraint con : constraints ) {
						final List<TestRunResult> ctRes = new ArrayList<TestRunResult>();
						for( TestRunner runner : syntaxCheckers ) {
							ctRes.add( find( caseRes, c, runner, con ) );
						}
						Object o = new Object() {
							public final SyntaxConstraint		constraint	= con;
							public final List<TestRunResult>	results		= ctRes;
							public final boolean				satisfied	= c
																					.getSatisfiedConstraints()
																					.contains( con );
						};
						byConstraintList.add( o );
					}

					syntaxResults.add( new Object() {
						public final List<Object>	byConstraint	= byConstraintList;
						public final TestCase		testCase		= c;
					} );
				}
				template.setAttribute( "syntax_results_" + pair[1], syntaxResults );
			}

			System.out.println( template.toString() );

		} catch( OWLOntologyCreationException e ) {
			log.log( Level.SEVERE, "Ontology creation exception caught.", e );
		}
	}
}
