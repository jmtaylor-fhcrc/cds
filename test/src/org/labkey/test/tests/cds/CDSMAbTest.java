package org.labkey.test.tests.cds;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.Timeout;
import org.labkey.api.util.Pair;
import org.labkey.test.Locator;
import org.labkey.test.pages.cds.AntigenFilterPanel;
import org.labkey.test.pages.cds.CDSExport;
import org.labkey.test.pages.cds.InfoPane;
import org.labkey.test.pages.cds.MAbDataGrid;
import org.labkey.test.util.cds.CDSHelper;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.labkey.test.pages.cds.MAbDataGrid.*;

@Category({})
public class CDSMAbTest extends CDSReadOnlyTest
{
    private final CDSHelper cds = new CDSHelper(this);

    @Before
    public void preTest()
    {
        cds.enterApplication();
        cds.ensureNoFilter();
        cds.ensureNoSelection();

        cds.goToAppHome();
    }

    @Override
    public BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }

    @Override
    public List<String> getAssociatedModules()
    {
        return Arrays.asList("CDS");
    }

    @Override
    public Timeout testTimeout()
    {
        return new Timeout(60, TimeUnit.MINUTES);
    }

    @Test
    public void testMAbPage()
    {
        CDSHelper.NavigationLink.MABGRID.makeNavigationSelection(this);
        MAbDataGrid grid = new MAbDataGrid(getGridEl(), this, this);

        log("Verify subject based info pane presence for mAb and other tabs");
        Locator.XPathLocator subjectInfoPane = CDSHelper.Locators.subjectInfoPaneHeader().notHidden();
        assertElementVisible(subjectInfoPane);
        CDSHelper.NavigationLink.GRID.makeNavigationSelection(this);
        assertElementPresent(subjectInfoPane);
        CDSHelper.NavigationLink.MABGRID.makeNavigationSelection(this);

        log("Verify mAb summary grid content");
        grid.clearAllFilters();
        Assert.assertEquals("Number of mab/mabmix rows is not as expected", 171, grid.getMabCounts());
        Assert.assertEquals("Geometric mean value for '2F5' is not as expected", "1.50595", grid.getMabCellValue("2F5", GEOMETRIC_MEAN_IC50_COL));
    }

    @Test
    public void testMAbGridWithFiltering()
    {
        CDSHelper.NavigationLink.MABGRID.makeNavigationSelection(this);
        MAbDataGrid grid = new MAbDataGrid(getGridEl(), this, this);
        grid.clearAllFilters();

        log("Verify mAb mix filter");
        List<String> filteredColumns = new ArrayList<>();
        grid.setFacet(MAB_COL,false,"2F5", "A14");
        filteredColumns.add(MAB_COL);
        verifyGridCountAndFilteredColumns(grid, 169, filteredColumns);

        log("Verify mAb mix metadata filters");
        grid.setFacet(SPECIES_COL,false,"llama");
        filteredColumns.add(SPECIES_COL);
        verifyGridCountAndFilteredColumns(grid, 166, filteredColumns);

        grid.setFacet(ISOTYPE_COL,true,"[blank]", "IgG3?");
        filteredColumns.add(ISOTYPE_COL);
        verifyGridCountAndFilteredColumns(grid, 152, filteredColumns);

        grid.setFacet(HXB2_COL,true,"[blank]");
        filteredColumns.add(HXB2_COL);
        verifyGridCountAndFilteredColumns(grid, 151, filteredColumns);

        log("Verify IC50 filter updates geometric mean values");
        Assert.assertEquals("Geometric mean value for 'AB-000402-1' is not as expected prior to filtering", "0.03724", grid.getMabCellValue("AB-000402-1", GEOMETRIC_MEAN_IC50_COL));
        grid.setFacet(GEOMETRIC_MEAN_IC50_COL,true,"< 0.1");
        filteredColumns.add(GEOMETRIC_MEAN_IC50_COL);
        verifyGridCountAndFilteredColumns(grid, 145, filteredColumns);
        Assert.assertEquals("Geometric mean value for 'AB-000402-1' is not as expected after filtering", "0.02393", grid.getMabCellValue("AB-000402-1", GEOMETRIC_MEAN_IC50_COL));

        log("Verify study filter");
        grid.setFacet(STUDIES_COL,true,"z118", "z128");
        filteredColumns.add(STUDIES_COL);
        verifyGridCountAndFilteredColumns(grid, 15, filteredColumns);

        log("Verify virus filter panel reflects active filter counts");
        AntigenFilterPanel virusPanel = grid.openVirusPanel(null);
        String testValue = "virus-1A-B-SF162.LS";
        Assert.assertTrue(virusPanel.isVirusChecked(testValue));
        Assert.assertTrue("Virus should have been filtered out for selection", virusPanel.isVirusDisabled(testValue));
        Assert.assertEquals("MAb count is not as expected", 0, virusPanel.getCount(testValue));

        String testValueCheck = "virus-1B-A-Q23.17";
        Assert.assertTrue(virusPanel.isVirusChecked(testValueCheck));
        Assert.assertFalse("Virus should be active for selection", virusPanel.isVirusDisabled(testValueCheck));
        Assert.assertEquals("MAb count is not as expected", 5, virusPanel.getCount(testValueCheck));

        virusPanel.checkVirus("virus-all", false);
        sleep(2000);
        virusPanel.checkVirus(testValueCheck, true);
        grid.applyFilter();
        filteredColumns.addAll(4, Arrays.asList(VIRUSES_COL, CLADES_COL, TIERS_COL));
        log("Verify virus filter panel reflects active filter counts");
        verifyGridCountAndFilteredColumns(grid, 5, filteredColumns);

        virusPanel = grid.openVirusPanel(CLADES_COL);
        String testValueFiltered = "virus-1A-B-MN.3";
        Assert.assertFalse(virusPanel.isVirusChecked(testValueFiltered));
        Assert.assertEquals("MAb count is not as expected", 6, virusPanel.getCount(testValueFiltered));
        Assert.assertTrue(virusPanel.isVirusChecked(testValueCheck));
        Assert.assertEquals("MAb count is not as expected", 5, virusPanel.getCount(testValueCheck));
        grid.cancelFilter();

        log("Verify removing filters");
        grid.clearAllFilters();
        verifyGridCountAndFilteredColumns(grid, 171, new ArrayList<>());
    }

    private void verifyGridCountAndFilteredColumns(MAbDataGrid grid, int rowCount, List<String> filteredColumns)
    {
        sleep(2000); // wait for grid to update
        Assert.assertEquals("Number of mab/mabmix rows is not as expected", rowCount, grid.getMabCounts());
        Assert.assertEquals("Columns with filtered icons aren't as expected", filteredColumns, grid.getFilteredColumns());
    }

    @Test
    public void testMAbSearchFilter()
    {
        CDSHelper.NavigationLink.MABGRID.makeNavigationSelection(this);
        MAbDataGrid grid = new MAbDataGrid(getGridEl(), this, this);
        grid.clearAllFilters();

        log("Verify mAb mix filter with search");
        List<String> filteredColumns = new ArrayList<>();
        grid.setFacet(MAB_COL,true,"2F5", "A14");
        filteredColumns.add(MAB_COL);
        verifyGridCountAndFilteredColumns(grid, 2, filteredColumns);

        log("Verify search narrows down options");
        grid.setFilterSearch(MAB_COL, "B21");
        Assert.assertEquals("Facet options aren't narrowed down as expected", 1, grid.getFilterOptionsCount());

        log("Verify 'Check All' not present with search");
        Assert.assertFalse("Check All shouldn't be visible with search text present", grid.isCheckAllPresent());

        log("Verify newly checked as well as hidden checked values are both applied as filters");
        grid.setFacet(MAB_COL,true, true, true, false, "B21");
        verifyGridCountAndFilteredColumns(grid, 3, filteredColumns);

        log("Verify backspace on search");
        grid.setFilterSearch(MAB_COL, "f5");
        Assert.assertEquals("Facet options aren't narrowed down as expected", 1, grid.getFilterOptionsCount());
        grid.setFacet(MAB_COL,false, true, true, true, "2F5");

        grid.setFilterSearch(MAB_COL, "f", true);
        Assert.assertEquals("Facet options aren't narrowed down as expected", 5, grid.getFilterOptionsCount());

        log("Verify clear on search");
        grid.setFilterSearch(MAB_COL, "", true);
        Assert.assertTrue("Facet options aren't narrowed down as expected", grid.getFilterOptionsCount() > 100);

        grid.applyFilter();
        verifyGridCountAndFilteredColumns(grid, 2, filteredColumns);
    }

    @Test
    public void testMAbReports()
    {
        CDSHelper.NavigationLink.MABGRID.makeNavigationSelection(this);
        MAbDataGrid grid = new MAbDataGrid(getGridEl(), this, this);
        grid.clearAllFilters();
        grid.clearAllSelections();

        log("Verify mAb report buttons");
        Assert.assertTrue("MAb report buttons not present",
                isElementPresent(grid.getDilutionReportBtn()) && isElementPresent(grid.getIC50ReportBtn()));

        log("Verify error message for view reports when no selection is made");
        click(grid.getDilutionReportBtn());
        waitForElementToBeVisible(Locator.tagWithClassContaining("div", "hopscotch-bubble"));
        assertTextPresent("Select data in the MAb grid, via the check box on each row, that you'd like to see in a report.");

        log("Set an initial set of filters for grid");
        grid.setFacet(MAB_COL,false,"2F5", "A14");
        grid.setFacet(SPECIES_COL,false,"llama");
        AntigenFilterPanel virusPanel = grid.openVirusPanel(null);
        String testValue = "virus-1B-A-Q23.17";
        virusPanel.checkVirus(testValue, false);
        grid.applyFilter();

        log("Select one mAb");
        grid.selectMAbs("4.00E+10");

        log("Verify report content with active filter and selection");
        List<String> expectedContent = Arrays.asList("Query name for filtered unique keys:  CDS_temp_",
                "Number of unique keys:  40",
                "7 Columns for unique keys:",
                "Query name for filtered dataset:  CDS_temp_",
                "Number of filtered data rows:  320");
        grid.openDilutionReport();
        verifyReportContent(expectedContent, grid.getReportOutput());
        grid.leaveReportView();

        log("Change ic50 filter and verify updated report content");
        grid.setFacet(GEOMETRIC_MEAN_IC50_COL,true,"< 0.1");
        grid.openDilutionReport();
        expectedContent = Arrays.asList("Number of unique keys:  3",
                "Number of filtered data rows:  24");
        verifyReportContent(expectedContent, grid.getReportOutput());
        grid.leaveReportView();

        log("Change mAb selections and verify updated report content");
        grid.selectMAbs("AB-000402-1", "AB-000404-1");
        grid.openDilutionReport();
        expectedContent = Arrays.asList("Number of unique keys:  15",
                "Number of filtered data rows:  120");
        verifyReportContent(expectedContent, grid.getReportOutput());
        grid.leaveReportView();

        log("Verify the 2nd R report");
        grid.openIC50Report();
        Assert.assertTrue("Report image is not rendered", isElementPresent(grid.getReportImageOut()));
    }

    private void verifyReportContent(List<String> expectedContent, String reportContent)
    {
        for (String expected : expectedContent)
        {
            Assert.assertTrue("Report content is not as expected", reportContent.contains(expected));
        }
    }

    @Test
    public void testMabGridExport() throws IOException
    {
        CDSHelper.NavigationLink.MABGRID.makeNavigationSelection(this);
        MAbDataGrid grid = new MAbDataGrid(getGridEl(), this, this);
        grid.clearAllFilters();
        grid.clearAllSelections();

        log("Export all data without filter or selection");
        CDSExport expected = new CDSExport(Arrays.asList(new Pair<>(MAbDataGrid.GRID_TITLE_STUDY_AND_MABS, 176),
                new Pair<>(MAbDataGrid.GRID_TITLE_MABS_META, 174),
                new Pair<>(MAbDataGrid.GRID_TITLE_NAB_MAB_ASSAY, 1553*8)));
        updateExpectedMAbExport(expected);
        expected.setStudyNetworks(Arrays.asList("Q", "ROGER", "ROGER", "YOYO", "ZED", "ZED", "ZED", "ZED", "ZED"));
        expected.setStudies(Arrays.asList("QED 2", "RED 4", "RED 5", "YOYO 55", "ZAP 117", "ZAP 118", "ZAP 119", "ZAP 128", "ZAP 133"));
        expected.setAssayProvenances(Arrays.asList("LabKey dataset", "VISC analysis dataset", "VISC analysis dataset", "VISC analysis dataset"));

//        grid.verifyCDSExcel(expected, false); // skip verify excel as it exceeds memory limit
        grid.verifyCDSCSV(expected);

        log("Export selected mab data without filter");
        grid.selectMAbs("2F5", "AB-000402-1");
        expected = new CDSExport(Arrays.asList(new Pair<>(MAbDataGrid.GRID_TITLE_STUDY_AND_MABS, 3),
                new Pair<>(MAbDataGrid.GRID_TITLE_MABS_META, 3),
                new Pair<>(MAbDataGrid.GRID_TITLE_NAB_MAB_ASSAY, 56*8)));
        updateExpectedMAbExport(expected);
        expected.setStudyNetworks(Arrays.asList("ROGER", "ZED"));
        expected.setStudies(Arrays.asList("RED 5", "ZAP 117"));
        expected.setAssayProvenances(Arrays.asList("VISC analysis dataset", "LabKey dataset"));
        expected.setFilterTitles(Arrays.asList("Selected MAb/Mixture(s)"));
        expected.setFilterValues(Arrays.asList("Mab Mix Name Std: 2F5, AB-000402-1"));
        grid.verifyCDSExcel(expected, false);
        grid.verifyCDSCSV(expected);

        log("Export mab data with filters and without selection");
        grid.clearAllSelections();
        log("Create a set of inclusive filters");
        grid.setFacet(MAB_COL,true,"2F5", "b12", "J3", "mAb 120");
        grid.setFacet(SPECIES_COL,true,"[blank]", "human");
        grid.setFacet(STUDIES_COL,true,"q2", "r4", "z117");
        log("Create exclusive filters for IC50 and virus");
        grid.setFacet(GEOMETRIC_MEAN_IC50_COL,false,"< 1");
        AntigenFilterPanel virusPanel = grid.openVirusPanel(null);
        String virusOneExclude = "virus-1B-A-Q23.17";
        String virusTwoExclude = "virus-1A-B-MN.3";
        virusPanel.checkVirus(virusOneExclude, false);
        virusPanel.checkVirus(virusTwoExclude, false);
        grid.applyFilter();

        expected = new CDSExport(Arrays.asList(new Pair<>(MAbDataGrid.GRID_TITLE_STUDY_AND_MABS, 5),
                new Pair<>(MAbDataGrid.GRID_TITLE_MABS_META, 5),
                new Pair<>(MAbDataGrid.GRID_TITLE_NAB_MAB_ASSAY, 77*8)));
        updateExpectedMAbExport(expected);
        expected.setStudyNetworks(Arrays.asList("ROGER", "ZED"));
        expected.setStudies(Arrays.asList("RED 4", "ZAP 117"));
        expected.setAssayProvenances(Arrays.asList("VISC analysis dataset", "LabKey dataset"));
        expected.setFilterTitles(Arrays.asList("Mab characteristics", "", "", "", "Neutralization Antibody - Monoclonal Antibodies"));
        expected.setFilterValues(Arrays.asList("Mab Donor Species: [blank];human",
                "Mab Mix Name Std: 2F5;b12;J3;mAb 120",
                "",
                "",
                "Neutralization tier + Clade + Virus - exclude: 1A B MN.3;1B A Q23.17",
                "Study: q2;r4;z117",
                "Titer Curve IC50: < 0.1",
                "Titer Curve IC50: > 50",
                "Titer Curve IC50: >= 1 AND < 10",
                "Titer Curve IC50: >= 10 AND <= 50"));
        grid.verifyCDSExcel(expected, false);
        grid.verifyCDSCSV(expected);

        log("Export selected mab data with filters");
        grid.selectMAbs("2F5");
        expected = new CDSExport(Arrays.asList(new Pair<>(MAbDataGrid.GRID_TITLE_STUDY_AND_MABS, 2),
                new Pair<>(MAbDataGrid.GRID_TITLE_MABS_META, 2),
                new Pair<>(MAbDataGrid.GRID_TITLE_NAB_MAB_ASSAY, 38*8)));
        updateExpectedMAbExport(expected);
        expected.setStudyNetworks(Arrays.asList("ZED"));
        expected.setStudies(Arrays.asList("ZAP 117"));
        expected.setAssayProvenances(Arrays.asList("LabKey dataset"));
        expected.setFilterTitles(Arrays.asList("Mab characteristics", "", "", "", "Neutralization Antibody - Monoclonal Antibodies",
                "",  "",  "",  "",  "",  "",  "",  "Selected MAb/Mixture(s)"));
        expected.setFilterValues(Arrays.asList("Mab Donor Species: [blank];human",
                "Mab Mix Name Std: 2F5;b12;J3;mAb 120",
                "",
                "",
                "Neutralization tier + Clade + Virus - exclude: 1A B MN.3;1B A Q23.17",
                "Study: q2;r4;z117",
                "Titer Curve IC50: < 0.1",
                "Titer Curve IC50: > 50",
                "Titer Curve IC50: >= 1 AND < 10",
                "Titer Curve IC50: >= 10 AND <= 50",
                "",
                "",
                "Mab Mix Name Std: 2F5"));
        grid.verifyCDSExcel(expected, false);
        grid.verifyCDSCSV(expected);
    }

    // set static export info
    private void updateExpectedMAbExport(CDSExport expectedExport)
    {
        expectedExport.setDataTabHeaders(Arrays.asList(new Pair<>(MAbDataGrid.GRID_TITLE_STUDY_AND_MABS, STUDY_AND_MABS_COLUMNS),
                new Pair<>(MAbDataGrid.GRID_TITLE_MABS_META, MABS_COLUMNS),
                new Pair<>(MAbDataGrid.GRID_TITLE_NAB_MAB_ASSAY, NABMAB_ASSAY_COLUMNS)));
        expectedExport.setFieldLabels(NABMAB_ASSAY_VARIABLES);
        expectedExport.setAssays(Arrays.asList(NABMAB_DATASET_NAME));
        expectedExport.setMAb(true);
    }

    private WebElement getGridEl()
    {
        return Locator.tagWithClass("div", "mab-connector-grid").findElement(getDriver());
    }

    @Test
    public void testMabInfoPane()
    {
        CDSHelper.NavigationLink.MABGRID.makeNavigationSelection(this);
        MAbDataGrid grid = new MAbDataGrid(getGridEl(), this, this);
        grid.clearAllFilters();
        grid.clearAllSelections();

        log("Validate that the counts are as expected.");
        InfoPane ip = new InfoPane(this);
        ip.waitForSpinners();

        Assert.assertEquals("MAbs/Mixtures count not as expected.", 171, ip.getMabMixturesCount());
        Assert.assertEquals("MAbs count not as expected.", 171, ip.getMabCount());
        Assert.assertEquals("Donor Species count not as expected.", 3, ip.getMabDonorCounts());
        Assert.assertEquals("Studies count not as expected.", 10, ip.getMabStudiesCount());
        Assert.assertEquals("MAb-Virus Pairs count not as expected.", 1418, ip.getMabVirusPairCount());
        Assert.assertEquals("Viruses count not as expected.", 159, ip.getMabVirusCount());

        log("Validate that clicking an item in the info pane gives the appropriate list of items.");
        // For most of these just check that the first few entries are present.

        List<String> expectedHasDataInMAbGrid;
        List<String> expectedNoDataInMAbGrid;

        ip.clickMabMixturesCount();
        log("Check MAb/Mixtures list.");

        expectedHasDataInMAbGrid = new ArrayList<>();
        expectedHasDataInMAbGrid.add("2F5");
        expectedHasDataInMAbGrid.add("3.00E+03");
        expectedHasDataInMAbGrid.add("4.00E+10");
        expectedHasDataInMAbGrid.add("A14");
        expectedHasDataInMAbGrid.add("AB-000402-1");

        expectedNoDataInMAbGrid = new ArrayList<>();

        String listText = ip.getMabMixturesList();
        String missingValues = doesListContainExpectedText(listText, expectedHasDataInMAbGrid, expectedNoDataInMAbGrid);
        Assert.assertTrue("List for MAbs/Mixtures did not contain the expected items:\n" + missingValues, missingValues.isEmpty());
        ip.clickClose();

        ip.clickMabCount();
        log("Check MAb list.");

        expectedHasDataInMAbGrid = new ArrayList<>();
        expectedHasDataInMAbGrid.add("2F5");
        expectedHasDataInMAbGrid.add("3.00E+03");
        expectedHasDataInMAbGrid.add("4.00E+10");
        expectedHasDataInMAbGrid.add("A14");
        expectedHasDataInMAbGrid.add("AB-000402-1");

        listText = ip.getMabList();
        missingValues = doesListContainExpectedText(listText, expectedHasDataInMAbGrid, null);
        Assert.assertTrue("List for MAbs did not contain the expected items:\n" + missingValues, missingValues.isEmpty());
        ip.clickClose();

        ip.clickMabDonorCounts();
        log("Check Donor list.");

        expectedHasDataInMAbGrid = new ArrayList<>();
        expectedHasDataInMAbGrid.add("human");
        expectedHasDataInMAbGrid.add("llama");
        expectedHasDataInMAbGrid.add("mouse");

        expectedNoDataInMAbGrid = new ArrayList<>();

        listText = ip.getMabDonorList();
        missingValues = doesListContainExpectedText(listText, expectedHasDataInMAbGrid, expectedNoDataInMAbGrid);
        Assert.assertTrue("List for Donor Species did not contain the expected items:\n" + missingValues, missingValues.isEmpty());
        ip.clickClose();

        ip.clickMabStudiesCount();
        log("Check Studies list.");

        expectedHasDataInMAbGrid = new ArrayList<>();
        expectedHasDataInMAbGrid.add("QED 2");
        expectedHasDataInMAbGrid.add("RED 4");
        expectedHasDataInMAbGrid.add("RED 5");
        expectedHasDataInMAbGrid.add("YOYO 55");
        expectedHasDataInMAbGrid.add("ZAP 117");
        expectedHasDataInMAbGrid.add("ZAP 118");
        expectedHasDataInMAbGrid.add("ZAP 119");
        expectedHasDataInMAbGrid.add("ZAP 128");
        expectedHasDataInMAbGrid.add("ZAP 133");
        expectedHasDataInMAbGrid.add("ZAP 135");

        expectedNoDataInMAbGrid = new ArrayList<>();

        listText = ip.getMabStudiesList();
        missingValues = doesListContainExpectedText(listText, expectedHasDataInMAbGrid, expectedNoDataInMAbGrid);
        Assert.assertTrue("List for Studies did not contain the expected items:\n" + missingValues, missingValues.isEmpty());
        ip.clickClose();

        ip.clickMabVirusPairCount();
        log("Check Virus Pair list.");

        expectedHasDataInMAbGrid = new ArrayList<>();
        expectedHasDataInMAbGrid.add("2F5 - 246-F3_C10_2");
        expectedHasDataInMAbGrid.add("2F5 - 25710-2.43");
        expectedHasDataInMAbGrid.add("2F5 - 398-F1-F6_20");
        expectedHasDataInMAbGrid.add("2F5 - BJOX002000.03.2");
        expectedHasDataInMAbGrid.add("2F5 - CH119.10");

        expectedNoDataInMAbGrid = new ArrayList<>();

        listText = ip.getMabVirusPairList();
        missingValues = doesListContainExpectedText(listText, expectedHasDataInMAbGrid, expectedNoDataInMAbGrid);
        Assert.assertTrue("List for MAb-Virus Pairs did not contain the expected items:\n" + missingValues, missingValues.isEmpty());
        ip.clickClose();

        ip.clickMabVirusCount();
        log("Check Virus list.");

        expectedHasDataInMAbGrid = new ArrayList<>();
        expectedHasDataInMAbGrid.add("0013095-2.11");
        expectedHasDataInMAbGrid.add("001428-2.42");
        expectedHasDataInMAbGrid.add("0260.V5.C36");
        expectedHasDataInMAbGrid.add("0330.v4.c3");
        expectedHasDataInMAbGrid.add("0815.v3.c3");
        expectedHasDataInMAbGrid.add("1394C9_G1 (Rev-)");

        expectedNoDataInMAbGrid = new ArrayList<>();

        listText = ip.getMabVirusList();
        missingValues = doesListContainExpectedText(listText, expectedHasDataInMAbGrid, expectedNoDataInMAbGrid);
        Assert.assertTrue("List for Viruses did not contain the expected items:\n" + missingValues, missingValues.isEmpty());
        ip.clickClose();

        log("Apply various filters and verify counts change.");
        log("Verify mAb mix filter");
        grid.setFacet(MAB_COL,true,"4.00E+10", "A14");

        log("Validate that the counts are as expected after the filter is applied.");
        ip = new InfoPane(this);
        ip.waitForSpinners();

        Assert.assertEquals("MAbs/Mixtures count not as expected.", 2, ip.getMabMixturesCount());
        Assert.assertEquals("MAbs count not as expected.", 2, ip.getMabCount());
        Assert.assertEquals("Donor Species count not as expected.", 2, ip.getMabDonorCounts());
        Assert.assertEquals("Studies count not as expected.", 2, ip.getMabStudiesCount());
        Assert.assertEquals("MAb-Virus Pairs count not as expected.", 64, ip.getMabVirusPairCount());
        Assert.assertEquals("Viruses count not as expected.", 61, ip.getMabVirusCount());

        log("Validate that clicking an item in the info pane shows correct information for the applied filter.");
        // For most of these just check that the first few entries are present.

        ip.clickMabMixturesCount();
        log("Check MAb/Mixtures list.");

        expectedHasDataInMAbGrid = new ArrayList<>();
        expectedHasDataInMAbGrid.add("4.00E+10");
        expectedHasDataInMAbGrid.add("A14");

        expectedNoDataInMAbGrid = new ArrayList<>();
        expectedNoDataInMAbGrid.add("2F5");
        expectedNoDataInMAbGrid.add("3.00E+03");
        expectedNoDataInMAbGrid.add("AB-000402-1");

        listText = ip.getMabMixturesList();
        missingValues = doesListContainExpectedText(listText, expectedHasDataInMAbGrid, expectedNoDataInMAbGrid);
        Assert.assertTrue("List for MAbs/Mixtures did not contain the expected items:\n" + missingValues, missingValues.isEmpty());
        ip.clickClose();

        ip.clickMabStudiesCount();
        log("Check Studies list.");

        expectedHasDataInMAbGrid = new ArrayList<>();
        expectedHasDataInMAbGrid.add("ZAP 117");
        expectedHasDataInMAbGrid.add("ZAP 119");

        expectedNoDataInMAbGrid = new ArrayList<>();
        expectedNoDataInMAbGrid.add("QED 2");
        expectedNoDataInMAbGrid.add("RED 4");
        expectedNoDataInMAbGrid.add("RED 5");
        expectedNoDataInMAbGrid.add("YOYO 55");
        expectedNoDataInMAbGrid.add("ZAP 118");
        expectedNoDataInMAbGrid.add("ZAP 128");
        expectedNoDataInMAbGrid.add("ZAP 133");
        expectedNoDataInMAbGrid.add("ZAP 135");

        listText = ip.getMabStudiesList();
        missingValues = doesListContainExpectedText(listText, expectedHasDataInMAbGrid, expectedNoDataInMAbGrid);
        Assert.assertTrue("List for Studies did not contain the expected items:\n" + missingValues, missingValues.isEmpty());
        ip.clickClose();

        log("Verify virus filter has changed the list");
        AntigenFilterPanel virusPanel = grid.openVirusPanel(null);

        virusPanel.checkVirus("virus-all", false);
        sleep(2000);
        String testValueCheck = "virus-1A-B-MN.3";
        virusPanel.checkVirus(testValueCheck, true);
        testValueCheck = "virus-1A-B-SF162.LS";
        virusPanel.checkVirus(testValueCheck, true);
        testValueCheck = "virus-2-02_AG-928-28";
        virusPanel.checkVirus(testValueCheck, true);
        grid.applyFilter();

        ip = new InfoPane(this);
        ip.waitForSpinners();

        Assert.assertEquals("MAbs/Mixtures count not as expected.", 2, ip.getMabMixturesCount());
        Assert.assertEquals("MAbs count not as expected.", 2, ip.getMabCount());
        Assert.assertEquals("Donor Species count not as expected.", 2, ip.getMabDonorCounts());
        Assert.assertEquals("Studies count not as expected.", 2, ip.getMabStudiesCount());
        Assert.assertEquals("MAb-Virus Pairs count not as expected.", 3, ip.getMabVirusPairCount());
        Assert.assertEquals("Viruses count not as expected.", 3, ip.getMabVirusCount());

        ip.clickMabVirusCount();
        log("Check Virus list.");

        expectedHasDataInMAbGrid = new ArrayList<>();
        expectedHasDataInMAbGrid.add("928-28");
        expectedHasDataInMAbGrid.add("MN.3");
        expectedHasDataInMAbGrid.add("SF162.LS");

        expectedNoDataInMAbGrid = new ArrayList<>();
        expectedNoDataInMAbGrid.add("0013095-2.11");
        expectedNoDataInMAbGrid.add("001428-2.42");
        expectedNoDataInMAbGrid.add("0260.V5.C36");
        expectedNoDataInMAbGrid.add("0330.v4.c3");
        expectedNoDataInMAbGrid.add("0815.v3.c3");
        expectedNoDataInMAbGrid.add("1394C9_G1 (Rev-)");

        listText = ip.getMabVirusList();
        missingValues = doesListContainExpectedText(listText, expectedHasDataInMAbGrid, expectedNoDataInMAbGrid);
        Assert.assertTrue("List for Viruses did not contain the expected items:\n" + missingValues, missingValues.isEmpty());
        ip.clickClose();

        grid.clearAllFilters();
        grid.clearAllSelections();

        log("Add a filter to the Geometric IC50 Curve.");
        grid.setFacet(GEOMETRIC_MEAN_IC50_COL,true,"< 0.1", "< 1");

        log("Validate that the counts are as expected after the filter is applied.");
        ip = new InfoPane(this);
        ip.waitForSpinners();

        Assert.assertEquals("MAbs/Mixtures count not as expected.", 169, ip.getMabMixturesCount());
        Assert.assertEquals("MAbs count not as expected.", 169, ip.getMabCount());
        Assert.assertEquals("Donor Species count not as expected.", 3, ip.getMabDonorCounts());
        Assert.assertEquals("Studies count not as expected.", 10, ip.getMabStudiesCount());
        Assert.assertEquals("MAb-Virus Pairs count not as expected.", 622, ip.getMabVirusPairCount());
        Assert.assertEquals("Viruses count not as expected.", 147, ip.getMabVirusCount());

        log("Go to the Find Subjects page");
        CDSHelper.NavigationLink.SUMMARY.makeNavigationSelection(this);
        ip = new InfoPane(this);
        Assert.assertEquals("Going to 'Find Subjects' did not update info pane as expected. Subjects count is wrong: ", 8277, ip.getSubjectCount());

        log("Go back to MAb tab and validate info pane still shows the filtered values.");
        CDSHelper.NavigationLink.MABGRID.makeNavigationSelection(this);
        ip = new InfoPane(this);

        Assert.assertEquals("MAbs/Mixtures count not as expected.", 169, ip.getMabMixturesCount());
        Assert.assertEquals("MAbs count not as expected.", 169, ip.getMabCount());
        Assert.assertEquals("Donor Species count not as expected.", 3, ip.getMabDonorCounts());
        Assert.assertEquals("Studies count not as expected.", 10, ip.getMabStudiesCount());
        Assert.assertEquals("MAb-Virus Pairs count not as expected.", 622, ip.getMabVirusPairCount());
        Assert.assertEquals("Viruses count not as expected.", 147, ip.getMabVirusCount());

        grid = new MAbDataGrid(getGridEl(), this, this);
        grid.clearAllFilters();
        grid.clearAllSelections();
    }

    private void buildLists(List<String> hasData, List<String> noData, String rawUIText)
    {
        String[] uiEntry = rawUIText.split("\n");

        sleep(2000);
        boolean putInHasData = false;

        int i = 0;

        while (i < uiEntry.length)
        {
            if (uiEntry[i].trim().equalsIgnoreCase("Has data in MAb grid"))
            {
                putInHasData = true;
                i++;
            }
            else if (uiEntry[i].trim().equalsIgnoreCase("No data in MAb grid"))
            {
                putInHasData = false;
                i++;
            }

            if (putInHasData)
            {
                hasData.add(uiEntry[i]);
            }
            else
            {
                noData.add(uiEntry[i]);
            }

            i++;
        }
    }

    private String doesListContainExpectedText(String listText, List<String> expectedHasData, List<String> expectedNoData)
    {
        StringBuilder sb = new StringBuilder();

        log("Raw UI text size: " + listText.length());

        List<String> hasDataInMAbGrid = new ArrayList<>();
        List<String> noDataInMAbGrid = new ArrayList<>();

        buildLists(hasDataInMAbGrid, noDataInMAbGrid, listText);

        log("hasDataInMAbGrid.size(): " + hasDataInMAbGrid.size() + " expectedHasData.size(): " +
                ((expectedHasData != null) ? expectedHasData.size() : 0) + " noDataInMAbGrid.size(): " + noDataInMAbGrid.size() +
                " expectedNoData.size(): " + ((expectedNoData != null) ? expectedNoData.size() : 0));

        if (null != expectedHasData)
        {
            if ((expectedHasData.size() == 0) && (hasDataInMAbGrid.size() != 0))
            {
                sb.append("UI shows values in 'Has data in MAb grid', wasn't expecting any.\n");
            }
            else
            {
                for (String expected : expectedHasData)
                {
                    if (!hasDataInMAbGrid.contains(expected))
                        sb.append("Did not find '" + expected + "' in 'Has data in MAb grid'.\n");
                }
            }
        }
        else
        {
            log("Expected Has Data is null so not going to check.");
        }

        if (null != expectedNoData)
        {

            if ((expectedNoData.size() == 0) && (noDataInMAbGrid.size() != 0))
            {
                sb.append("UI shows values in 'No data in MAb grid', wasn't expecting any.\n");
            }
            else
            {
                for (String expected : expectedNoData)
                {
                    if (!noDataInMAbGrid.contains(expected))
                        sb.append("Did not find '" + expected + "' in 'No data in MAb grid'.\n");
                }
            }

        }
        else
        {
            log("Expected No Data is null so not going to check.");
        }

        return sb.toString();
    }
}
