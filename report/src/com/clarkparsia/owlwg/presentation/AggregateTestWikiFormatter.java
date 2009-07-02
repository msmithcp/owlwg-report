package com.clarkparsia.owlwg.presentation;

import static com.clarkparsia.owlwg.Constants.TEST_ONTOLOGY_PHYSICAL_URI;
import static com.clarkparsia.owlwg.presentation.Utilities.match;
import static com.clarkparsia.owlwg.testcase.filter.ConjunctionFilter.and;
import static com.clarkparsia.owlwg.testcase.filter.SatisfiedSyntaxConstraintFilter.DL;
import static com.clarkparsia.owlwg.testcase.filter.SatisfiedSyntaxConstraintFilter.EL;
import static com.clarkparsia.owlwg.testcase.filter.SatisfiedSyntaxConstraintFilter.QL;
import static com.clarkparsia.owlwg.testcase.filter.SatisfiedSyntaxConstraintFilter.RL;
import static com.clarkparsia.owlwg.testcase.filter.StatusFilter.APPROVED;
import static com.clarkparsia.owlwg.testcase.filter.StatusFilter.NOSTATUS;
import static com.clarkparsia.owlwg.testcase.filter.StatusFilter.PROPOSED;
import static com.clarkparsia.owlwg.testcase.filter.StatusFilter.REJECTED;
import static com.clarkparsia.owlwg.testcase.filter.TestTypeFilter.CONSISTENCY;
import static com.clarkparsia.owlwg.testcase.filter.TestTypeFilter.INCONSISTENCY;
import static com.clarkparsia.owlwg.testcase.filter.TestTypeFilter.NEGATIVE_ENTAILMENT;
import static com.clarkparsia.owlwg.testcase.filter.TestTypeFilter.POSITIVE_ENTAILMENT;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
import com.clarkparsia.owlwg.testcase.TestCase;

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
public class AggregateTestWikiFormatter {

	private final static Logger	log;

	static {
		log = Logger.getLogger( AggregateTestWikiFormatter.class.getCanonicalName() );

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
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if( args.length != 1 )
			throw new IllegalArgumentException();

		final StringTemplateGroup stg = StringTemplateGroup.loadGroup( "case-summary-wiki" );
		final StringTemplate template = stg.getInstanceOf( "case-summary" );

		final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		try {
			/*
			 * Load the test ontology from local files before reading the test
			 * cases, otherwise import is likely to fail.
			 */
			manager.loadOntologyFromPhysicalURI( TEST_ONTOLOGY_PHYSICAL_URI );

			OWLOntology casesOntology = manager.loadOntologyFromPhysicalURI( URI.create( args[0] ) );

			TestCollection testCollection = new TestCollection( casesOntology );
			List<TestCase> cases = testCollection.asList();
			testCollection = null;

			/* General info about report */
			{
				SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mmZ" );
				template.setAttribute( "timestamp", df.format( new Date() ) );

				template.setAttribute( "total", cases.size() );
			}

			/* Summary by status */
			{
				template.setAttribute( "approved", match( APPROVED, cases ).size() );
				template.setAttribute( "nostatus", match( NOSTATUS, cases ).size() );
				template.setAttribute( "proposed", match( PROPOSED, cases ).size() );
				template.setAttribute( "rejected", match( REJECTED, cases ).size() );
			}

			/* Summary by type and status */
			{
				template.setAttribute( "consistency", match( CONSISTENCY, cases ).size() );
				template.setAttribute( "inconsistency", match( INCONSISTENCY, cases ).size() );
				template.setAttribute( "positive_entailment", match( POSITIVE_ENTAILMENT, cases )
						.size() );
				template.setAttribute( "negative_entailment", match( NEGATIVE_ENTAILMENT, cases )
						.size() );

				template.setAttribute( "consistency_approved", match( and( CONSISTENCY, APPROVED ),
						cases ).size() );
				template.setAttribute( "consistency_proposed", match( and( CONSISTENCY, PROPOSED ),
						cases ).size() );
				template.setAttribute( "consistency_rejected", match( and( CONSISTENCY, REJECTED ),
						cases ).size() );
				template.setAttribute( "consistency_nostatus", match( and( CONSISTENCY, NOSTATUS ),
						cases ).size() );

				template.setAttribute( "inconsistency_approved", match(
						and( INCONSISTENCY, APPROVED ), cases ).size() );
				template.setAttribute( "inconsistency_proposed", match(
						and( INCONSISTENCY, PROPOSED ), cases ).size() );
				template.setAttribute( "inconsistency_rejected", match(
						and( INCONSISTENCY, REJECTED ), cases ).size() );
				template.setAttribute( "inconsistency_nostatus", match(
						and( INCONSISTENCY, NOSTATUS ), cases ).size() );

				template.setAttribute( "positive_entailment_approved", match(
						and( POSITIVE_ENTAILMENT, APPROVED ), cases ).size() );
				template.setAttribute( "positive_entailment_proposed", match(
						and( POSITIVE_ENTAILMENT, PROPOSED ), cases ).size() );
				template.setAttribute( "positive_entailment_rejected", match(
						and( POSITIVE_ENTAILMENT, REJECTED ), cases ).size() );
				template.setAttribute( "positive_entailment_nostatus", match(
						and( POSITIVE_ENTAILMENT, NOSTATUS ), cases ).size() );

				template.setAttribute( "negative_entailment_approved", match(
						and( NEGATIVE_ENTAILMENT, APPROVED ), cases ).size() );
				template.setAttribute( "negative_entailment_proposed", match(
						and( NEGATIVE_ENTAILMENT, PROPOSED ), cases ).size() );
				template.setAttribute( "negative_entailment_rejected", match(
						and( NEGATIVE_ENTAILMENT, REJECTED ), cases ).size() );
				template.setAttribute( "negative_entailment_nostatus", match(
						and( NEGATIVE_ENTAILMENT, NOSTATUS ), cases ).size() );
			}

			/* Summary by profile and status */
			{
				template.setAttribute( "dl", match( DL, cases ).size() );
				template.setAttribute( "el", match( EL, cases ).size() );
				template.setAttribute( "ql", match( QL, cases ).size() );
				template.setAttribute( "rl", match( RL, cases ).size() );

				template.setAttribute( "dl_approved", match( and( DL, APPROVED ), cases ).size() );
				template.setAttribute( "dl_proposed", match( and( DL, PROPOSED ), cases ).size() );
				template.setAttribute( "dl_rejected", match( and( DL, REJECTED ), cases ).size() );
				template.setAttribute( "dl_nostatus", match( and( DL, NOSTATUS ), cases ).size() );

				template.setAttribute( "el_approved", match( and( EL, APPROVED ), cases ).size() );
				template.setAttribute( "el_proposed", match( and( EL, PROPOSED ), cases ).size() );
				template.setAttribute( "el_rejected", match( and( EL, REJECTED ), cases ).size() );
				template.setAttribute( "el_nostatus", match( and( EL, NOSTATUS ), cases ).size() );

				template.setAttribute( "ql_approved", match( and( QL, APPROVED ), cases ).size() );
				template.setAttribute( "ql_proposed", match( and( QL, PROPOSED ), cases ).size() );
				template.setAttribute( "ql_rejected", match( and( QL, REJECTED ), cases ).size() );
				template.setAttribute( "ql_nostatus", match( and( QL, NOSTATUS ), cases ).size() );

				template.setAttribute( "rl_approved", match( and( RL, APPROVED ), cases ).size() );
				template.setAttribute( "rl_proposed", match( and( RL, PROPOSED ), cases ).size() );
				template.setAttribute( "rl_rejected", match( and( RL, REJECTED ), cases ).size() );
				template.setAttribute( "rl_nostatus", match( and( RL, NOSTATUS ), cases ).size() );
			}

			System.out.print( template.toString() );

		} catch( OWLOntologyCreationException e ) {
			log.log( Level.SEVERE, "Ontology creation exception caught.", e );
		}
	}
}
