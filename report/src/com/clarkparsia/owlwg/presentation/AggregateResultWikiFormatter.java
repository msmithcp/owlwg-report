package com.clarkparsia.owlwg.presentation;

import static com.clarkparsia.owlwg.Constants.RESULTS_ONTOLOGY_PHYSICAL_URI;
import static com.clarkparsia.owlwg.Constants.TEST_ONTOLOGY_PHYSICAL_URI;
import static com.clarkparsia.owlwg.presentation.Utilities.collectReasoningRunners;
import static com.clarkparsia.owlwg.presentation.Utilities.collectSyntaxConstraintRunners;
import static com.clarkparsia.owlwg.presentation.Utilities.indexByStatus;
import static com.clarkparsia.owlwg.presentation.Utilities.possibleReasoningRunTypes;
import static com.clarkparsia.owlwg.presentation.Utilities.possibleSyntaxConstraintTests;
import static java.lang.String.format;

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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

import com.clarkparsia.owlwg.TestCollection;
import com.clarkparsia.owlwg.runner.TestRunner;
import com.clarkparsia.owlwg.testcase.Status;
import com.clarkparsia.owlwg.testcase.SyntaxConstraint;
import com.clarkparsia.owlwg.testcase.TestCase;
import com.clarkparsia.owlwg.testrun.RunTestType;
import com.clarkparsia.owlwg.testrun.SyntaxConstraintRun;
import com.clarkparsia.owlwg.testrun.TestRunResult;
import com.clarkparsia.owlwg.testrun.TestRunResultParser;

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

	private final static Logger	log;

	static {
		log = Logger.getLogger( AggregateResultWikiFormatter.class.getCanonicalName() );
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if( args.length < 2 )
			throw new IllegalArgumentException();

		final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

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

			TestCollection caseCol = new TestCollection( casesOntology );
			Map<String, TestCase> caseMap = new HashMap<String, TestCase>();
			for( TestCase c : caseCol )
				caseMap.put( c.getIdentifier(), c );

			List<TestCase> cases = caseCol.asList();
			Collections.sort( cases, new Comparator<TestCase>() {
				public int compare(TestCase o1, TestCase o2) {
					return o1.getIdentifier().compareTo( o2.getIdentifier() );
				}
			} );

			TestRunResultParser parser = new TestRunResultParser();
			List<TestRunResult> results = new ArrayList<TestRunResult>();
			for( OWLOntology o : resultsOntologies )
				results.addAll( parser.getResults( o, caseMap ) );

			/* Results by test case */
			Map<TestCase, List<TestRunResult>> caseToResult = new HashMap<TestCase, List<TestRunResult>>();
			for( TestCase t : cases )
				caseToResult.put( t, new ArrayList<TestRunResult>() );
			for( TestRunResult r : results )
				caseToResult.get( r.getTestCase() ).add( r );

			StringBuffer out = new StringBuffer();
			out.append( "= Test Results Summary =\n\n" );

			/* Cases by status */
			Map<Status, Collection<TestCase>> statusToCase = indexByStatus( cases );

			/* General info about report */
			{
				SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mmZ" );
				out.append( format( "Produced: %s\n", df.format( new Date() ) ) );
			}

			out.append( "\n== By Status ==\n\n" );

			Status[] statuses = new Status[Status.values().length + 1];
			System.arraycopy( Status.values(), 0, statuses, 0, Status.values().length );
			statuses[statuses.length - 1] = null;
			for( Status status : statuses ) {
				if( Status.REJECTED.equals( status ) )
					continue;

				out.append( format( "\n=== %s ===\n\n", (status == null)
					? "NO STATUS"
					: status ) );

				Collection<TestCase> sCases = statusToCase.get( status );
				List<TestRunResult> sResults = new ArrayList<TestRunResult>();
				for( TestCase c : sCases )
					sResults.addAll( caseToResult.get( c ) );

				Collection<TestRunner> reasoningRunners = collectReasoningRunners( sResults );

				out.append( "\n==== Reasoning Tests ====\n\n" );

				out.append( "{| border=\"1\"\n|-\n!Test\n!Type\n" );

				for( TestRunner r : reasoningRunners )
					out.append( format( "!%s\n", r.getURI() ) );

				for( TestCase c : statusToCase.get( status ) ) {
					final List<TestRunResult> cResults = caseToResult.get( c );
					Set<RunTestType> testTypes = possibleReasoningRunTypes( c );
					out
							.append( testTypes.size() > 1
								? format( "|-\n|rowspan=\"%d\"|%s\n", testTypes.size(),
										caseToTableCell( c ) )
								: format( "|-\n|%s\n", caseToTableCell( c ) ) );

					boolean firstRow = true;
					for( RunTestType type : testTypes ) {
						if( !firstRow )
							out.append( "|-\n" );
						else
							firstRow = false;

						out.append( format( "%s\n", testTypeToTableCell( type ) ) );

						for( TestRunner runner : reasoningRunners ) {
							boolean match = false;
							for( TestRunResult r : cResults ) {
								if( r.getTestType().equals( type )
										&& r.getTestRunner().equals( runner ) ) {
									out.append( format( "%s\n", resultToTableCell( r ) ) );
									match = true;
									break;
								}
							}
							if( !match )
								out.append( "|\n" );
						}
					}
				}
				out.append( "|}\n" );

				Collection<TestRunner> constraintRunners = collectSyntaxConstraintRunners( sResults );

				out.append( "\n==== Syntax Tests ====\n\n" );
				out.append( "{| border=\"1\"\n|-\n!Test\n!Type\n" );

				for( TestRunner r : constraintRunners )
					out.append( format( "!%s\n", r.getURI() ) );

				for( TestCase c : statusToCase.get( status ) ) {
					final List<TestRunResult> cResults = caseToResult.get( c );
					Set<SyntaxConstraint> testTypes = possibleSyntaxConstraintTests( c );
					out
							.append( testTypes.size() > 1
								? format( "|-\n|rowspan=\"%d\"|%s\n", testTypes.size(),
										caseToTableCell( c ) )
								: format( "|-\n|%s\n", caseToTableCell( c ) ) );

					boolean firstRow = true;
					for( SyntaxConstraint constraint : testTypes ) {
						if( !firstRow )
							out.append( "|-\n" );
						else
							firstRow = false;

						out.append( format( "%s\n", constraintToTableCell( c, constraint ) ) );

						for( TestRunner runner : constraintRunners ) {
							boolean match = false;
							for( TestRunResult r : cResults ) {
								if( r.getTestType().equals( RunTestType.SYNTAX_CONSTRAINT )
										&& r.getTestRunner().equals( runner ) ) {
									if( r instanceof SyntaxConstraintRun ) {
										final SyntaxConstraintRun scr = (SyntaxConstraintRun) r;
										if( constraint.equals( scr.getConstraint() ) ) {
											out.append( format( "%s\n", resultToTableCell( r ) ) );
											match = true;
											break;
										}
									}
									else
										throw new IllegalStateException();
								}
							}
							if( !match )
								out.append( "|\n" );
						}
					}
				}
				out.append( "|}\n" );

			}

			System.out.println( out.toString() );

		} catch( OWLOntologyCreationException e ) {
			log.log( Level.SEVERE, "Ontology creation exception caught.", e );
		}
	}

	private static String testTypeToTableCell(RunTestType t) {
		String ret;
		switch ( t ) {
		case CONSISTENCY:
			ret = "|Consistency";
			break;
		case INCONSISTENCY:
			ret = "|Inconsistency";
			break;
		case POSITIVE_ENTAILMENT:
			ret = "|Positive Entailment";
			break;
		case NEGATIVE_ENTAILMENT:
			ret = "|Negative Entailment";
			break;
		case SYNTAX_TRANSLATION:
			ret = "|Translation";
			break;
		case SYNTAX_CONSTRAINT:
		default:
			throw new IllegalStateException();
		}
		return ret;
	}

	private static String constraintToTableCell(TestCase t, SyntaxConstraint c) {
		String ret = format( "|%s%s", t.getSatisfiedConstraints().contains( c )
			? ""
			: t.getUnsatisfiedConstraints().contains( c )
				? "!"
				: "?", c );
		return ret;
	}

	private static String resultToTableCell(TestRunResult r) {
		String ret;
		switch ( r.getResultType() ) {
		case FAILING:
			ret = "|style=\"background:red; color:white\"|Fail";
			break;
		case INCOMPLETE:
			String details = r.getDetails();
			if( details == null )
				ret = "|Incomplete";
			else
				ret = format( "|<span title=\"%s\">Incomplete</span>", details.replace( "\"",
						"&quot;" ).replace( ">", "&gt;" ).replace( "<", "&gt;" ) );
			break;
		case PASSING:
			ret = "|style=\"background:green; color:white\"|Pass";
			break;
		default:
			throw new IllegalStateException();
		}
		return ret;
	}

	private static String caseToTableCell(TestCase c) {
		return format( "[%s %s]", c.getURI(), c.getIdentifier() );
	}
}
