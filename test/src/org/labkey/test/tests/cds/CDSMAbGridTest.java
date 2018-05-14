package org.labkey.test.tests.cds;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.Timeout;
import org.labkey.test.Locator;
import org.labkey.test.pages.cds.AntigenFilterPanel;
import org.labkey.test.pages.cds.MAbDataGrid;
import org.labkey.test.util.cds.CDSHelper;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.labkey.test.pages.cds.MAbDataGrid.*;

@Category({})
public class CDSMAbGridTest extends CDSReadOnlyTest
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
        assertElementNotPresent(subjectInfoPane);
        CDSHelper.NavigationLink.GRID.makeNavigationSelection(this);
        assertElementPresent(subjectInfoPane);
        CDSHelper.NavigationLink.MABGRID.makeNavigationSelection(this);

        log("Verify mAb summary grid content");
        grid.clearAllFilters();
        Assert.assertEquals("Number of mab/mabmix rows is not as expected", 49, grid.getMabCounts());
        Assert.assertEquals("Geometric mean value for '2F5' is not as expected", "1.50583", grid.getMabCellValue("2F5", GEOMETRIC_MEAN_IC50_COL));
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
        verifyGridCountAndFilteredColumns(grid, 47, filteredColumns);

        log("Verify mAb mix metadata filters");
        grid.setFacet(SPECIES_COL,false,"llama");
        filteredColumns.add(SPECIES_COL);
        verifyGridCountAndFilteredColumns(grid, 44, filteredColumns);

        grid.setFacet(ISOTYPE_COL,true,"[blank]", "IgG3?");
        filteredColumns.add(ISOTYPE_COL);
        verifyGridCountAndFilteredColumns(grid, 42, filteredColumns);

        grid.setFacet(HXB2_COL,true,"[blank]");
        filteredColumns.add(HXB2_COL);
        verifyGridCountAndFilteredColumns(grid, 41, filteredColumns);

        log("Verify IC50 filter updates geometric mean values");
        Assert.assertEquals("Geometric mean value for 'AB-000402-1' is not as expected prior to filtering", "0.03717", grid.getMabCellValue("AB-000402-1", GEOMETRIC_MEAN_IC50_COL));
        grid.setFacet(GEOMETRIC_MEAN_IC50_COL,true,"< 0.1");
        filteredColumns.add(GEOMETRIC_MEAN_IC50_COL);
        verifyGridCountAndFilteredColumns(grid, 20, filteredColumns);
        Assert.assertEquals("Geometric mean value for 'AB-000402-1' is not as expected after filtering", "0.02388", grid.getMabCellValue("AB-000402-1", GEOMETRIC_MEAN_IC50_COL));

        log("Verify study filter");
        grid.setFacet(STUDIES_COL,true,"z118", "z128");
        filteredColumns.add(STUDIES_COL);
        verifyGridCountAndFilteredColumns(grid, 9, filteredColumns);

        log("Verify virus filter panel reflects active filter counts");
        AntigenFilterPanel virusPanel = grid.openVirusPanel(null);
        String testValue = "virus-1A-B-MN.3";
        Assert.assertTrue(virusPanel.isVirusChecked(testValue));
        Assert.assertTrue("Virus should have been filtered out for selection", virusPanel.isVirusDisabled(testValue));
        Assert.assertEquals("MAb count is not as expected", 0, virusPanel.getCount(testValue));

        testValue = "virus-1B-A-Q23.17";
        Assert.assertTrue(virusPanel.isVirusChecked(testValue));
        Assert.assertFalse("Virus should be active for selection", virusPanel.isVirusDisabled(testValue));
        Assert.assertEquals("MAb count is not as expected", 3, virusPanel.getCount(testValue));

        virusPanel.checkVirus(testValue, false);
        grid.applyFilter();
        filteredColumns.addAll(4, Arrays.asList(VIRUSES_COL, CLADES_COL, TIERS_COL));
        log("Verify virus filter panel reflects active filter counts");
        verifyGridCountAndFilteredColumns(grid, 7, filteredColumns);

        virusPanel = grid.openVirusPanel(CLADES_COL);
        Assert.assertFalse(virusPanel.isVirusChecked(testValue));
        Assert.assertEquals("MAb count is not as expected", 3, virusPanel.getCount(testValue));
        grid.cancelFilter();

        log("Verify removing filters");
        grid.clearAllFilters();
        verifyGridCountAndFilteredColumns(grid, 49, new ArrayList<>());
    }

    private void verifyGridCountAndFilteredColumns(MAbDataGrid grid, int rowCount, List<String> filteredColumns)
    {
        Assert.assertEquals("Number of mab/mabmix rows is not as expected", rowCount, grid.getMabCounts());
        Assert.assertEquals("Columns with filtered icons aren't as expected", filteredColumns, grid.getFilteredColumns());
    }

//    @Test TODO
//    public void testMAbSearchFilter()
//    {
//
//    }

    private WebElement getGridEl()
    {
        return Locator.tagWithClass("div", "mab-connector-grid").findElement(getDriver());
    }

}
