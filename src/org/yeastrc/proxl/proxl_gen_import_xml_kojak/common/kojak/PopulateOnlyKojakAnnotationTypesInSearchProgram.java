package org.yeastrc.proxl.proxl_gen_import_xml_kojak.common.kojak;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.yeastrc.proxl.proxl_gen_import_xml_kojak.common.constants.KojakFileContentsConstants;
import org.yeastrc.proxl.proxl_gen_import_xml_kojak.common.exceptions.ProxlGenXMLDataException;
import org.yeastrc.proxl.proxl_gen_import_xml_kojak.kojak.constants.KojakOnlyDefaultFilterValueConstants;
import org.yeastrc.proxl_import.api.xml_dto.DescriptivePsmAnnotationType;
import org.yeastrc.proxl_import.api.xml_dto.DescriptivePsmAnnotationTypes;
import org.yeastrc.proxl_import.api.xml_dto.FilterablePsmAnnotationType;
import org.yeastrc.proxl_import.api.xml_dto.FilterablePsmAnnotationTypes;
import org.yeastrc.proxl_import.api.xml_dto.FilterablePsmPerPeptideAnnotationType;
import org.yeastrc.proxl_import.api.xml_dto.FilterablePsmPerPeptideAnnotationTypes;
import org.yeastrc.proxl_import.api.xml_dto.SearchProgram;
import org.yeastrc.proxl_import.api.xml_dto.SearchProgram.PsmAnnotationTypes;
import org.yeastrc.proxl_import.api.xml_dto.SearchProgram.PsmPerPeptideAnnotationTypes;

public class PopulateOnlyKojakAnnotationTypesInSearchProgram {

	private static final Logger log = Logger.getLogger( PopulateOnlyKojakAnnotationTypesInSearchProgram.class );
	
	public enum SetKojakDefaultCutoffs { YES, NO }
	
	// private constructor
	private PopulateOnlyKojakAnnotationTypesInSearchProgram() { }
	
	public static PopulateOnlyKojakAnnotationTypesInSearchProgram getInstance() {
		return new PopulateOnlyKojakAnnotationTypesInSearchProgram();
	}


	/**
	 * Add data from the Kojak header to the searchProgram object
	 * 
	 * @param searchProgram
	 * @param kojakFileReader
	 * @param setKojakDefaultCutoffs - Should default cutoffs be set on Kojak Filterable annotation types
	 * @throws ProxlGenXMLDataException
	 */
	public void populateKojakAnnotationTypesInSearchProgram( 
			SearchProgram searchProgram, 
			KojakFileReader kojakFileReader, 
			SetKojakDefaultCutoffs setKojakDefaultCutoffs 
			) throws ProxlGenXMLDataException {
		

		searchProgram.setVersion( kojakFileReader.getProgramVersion() );

		PsmAnnotationTypes psmAnnotationTypes = new PsmAnnotationTypes();
		searchProgram.setPsmAnnotationTypes( psmAnnotationTypes );

		/////////////  Filterable
		
		GetKojakFilterableAnnTypeObjects getKojakFilterableAnnTypeObjects = GetKojakFilterableAnnTypeObjects.getInstance();
		
		FilterablePsmAnnotationTypes filterablePsmAnnotationTypes = new FilterablePsmAnnotationTypes();
		psmAnnotationTypes.setFilterablePsmAnnotationTypes( filterablePsmAnnotationTypes );
		
		List<FilterablePsmAnnotationType> filterablePsmAnnotationTypeList =
				filterablePsmAnnotationTypes.getFilterablePsmAnnotationType();
				
		Set<String> filteredAnnotationNamesFromColumnHeaders =
				kojakFileReader.getFilteredAnnotationNamesFromColumnHeaders();
		
		for ( String name : filteredAnnotationNamesFromColumnHeaders ) {
			
			
			if ( KojakFileContentsConstants.SCORE_HEADER_LABEL.equals( name ) ) {
				
				FilterablePsmAnnotationType filterablePsmAnnotationType = 
						getKojakFilterableAnnTypeObjects.getScoreAnnTypeObject();
				
				if ( setKojakDefaultCutoffs == SetKojakDefaultCutoffs.YES ) {
					filterablePsmAnnotationType.setDefaultFilter( true );
					filterablePsmAnnotationType.setDefaultFilterValue( KojakOnlyDefaultFilterValueConstants.KOJAK_SCORE_PSM_VALUE_DEFAULT );
				}
				
				filterablePsmAnnotationTypeList.add( filterablePsmAnnotationType );
				
			} else if ( KojakFileContentsConstants.DSCORE_HEADER_LABEL.equals( name ) ) {

				FilterablePsmAnnotationType filterablePsmAnnotationType = 
						getKojakFilterableAnnTypeObjects.getDScoreAnnTypeObject();
				
				filterablePsmAnnotationTypeList.add( filterablePsmAnnotationType );
				
			} else if ( KojakFileContentsConstants.PEPDIFF_HEADER_LABEL.equals( name ) ) {

				FilterablePsmAnnotationType filterablePsmAnnotationType = 
						getKojakFilterableAnnTypeObjects.getPepDiffAnnTypeObject();
				
				filterablePsmAnnotationTypeList.add( filterablePsmAnnotationType );
				
			} else {
				
				String msg = "Kojak file processing.  Unexpected Filterable annotation name: " + name;
				log.error( msg );
				throw new ProxlGenXMLDataException(msg);
			}
		}
		
		//   Filterable at PSM and PSM Per Peptide
		
		if ( kojakFileReader.headerHasPeptide_1_score() && kojakFileReader.headerHasPeptide_2_score() ) {
			
			//  Process Per-peptide score
			
			//  At PSM level, add High Score and Low Score Ann Types
			{
				FilterablePsmAnnotationType filterablePsmAnnotationType = 
						getKojakFilterableAnnTypeObjects.getPerPeptideHighScoreAnnTypeObject();
				filterablePsmAnnotationTypeList.add( filterablePsmAnnotationType );
			}
			{
				FilterablePsmAnnotationType filterablePsmAnnotationType = 
						getKojakFilterableAnnTypeObjects.getPerPeptideLowScoreAnnTypeObject();
				filterablePsmAnnotationTypeList.add( filterablePsmAnnotationType );
			}

			//  At Psm Per Peptide Level, Add Per Peptide Score
			
			addPsmPerPeptideFilterableAnnotationTypes( searchProgram, getKojakFilterableAnnTypeObjects );
		}
		
		/////////////  Descriptive
		
		
		DescriptivePsmAnnotationTypes descriptivePsmAnnotationTypes = new DescriptivePsmAnnotationTypes();
		psmAnnotationTypes.setDescriptivePsmAnnotationTypes( descriptivePsmAnnotationTypes );
		
		List<DescriptivePsmAnnotationType> descriptivePsmAnnotationTypeList =
				descriptivePsmAnnotationTypes.getDescriptivePsmAnnotationType();
		
		Set<String> descriptiveAnnotationNamesFromColumnHeaders =
				kojakFileReader.getDescriptiveAnnotationNamesFromColumnHeaders();

		for ( String name : descriptiveAnnotationNamesFromColumnHeaders ) {
			
			DescriptivePsmAnnotationType descriptivePsmAnnotationType = new DescriptivePsmAnnotationType();
			descriptivePsmAnnotationTypeList.add( descriptivePsmAnnotationType );
			
			descriptivePsmAnnotationType.setName( name );
			descriptivePsmAnnotationType.setDescription( name );
		}
	}
	
	/**
	 * @param searchProgram
	 */
	private void addPsmPerPeptideFilterableAnnotationTypes( SearchProgram searchProgram, GetKojakFilterableAnnTypeObjects getKojakFilterableAnnTypeObjects ) {

		//  At Psm Per Peptide Level, Add Per Peptide Score
		
		PsmPerPeptideAnnotationTypes psmPerPeptideAnnotationTypes = new PsmPerPeptideAnnotationTypes();
		searchProgram.setPsmPerPeptideAnnotationTypes( psmPerPeptideAnnotationTypes );

		/////////////  Filterable
		
		FilterablePsmPerPeptideAnnotationTypes filterablePsmPerPeptideAnnotationTypes = new FilterablePsmPerPeptideAnnotationTypes();
		psmPerPeptideAnnotationTypes.setFilterablePsmPerPeptideAnnotationTypes( filterablePsmPerPeptideAnnotationTypes );
		
		List<FilterablePsmPerPeptideAnnotationType> filterablePsmPerPeptideAnnotationTypeList =
				filterablePsmPerPeptideAnnotationTypes.getFilterablePsmPerPeptideAnnotationType();
				
		FilterablePsmPerPeptideAnnotationType filterablePsmPerPeptideAnnotationType = 
				getKojakFilterableAnnTypeObjects.getPsmPerPeptideScoreAnnTypeObject();

		filterablePsmPerPeptideAnnotationTypeList.add( filterablePsmPerPeptideAnnotationType );
	}

}