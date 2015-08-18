/*
 * Copyright (c) 2014-2015 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.labkey.test.tests;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.test.Locator;
import org.labkey.test.categories.CDS;
import org.labkey.test.categories.Git;
import org.labkey.test.pages.ColorAxisVariableSelector;
import org.labkey.test.pages.DataspaceVariableSelector;
import org.labkey.test.pages.XAxisVariableSelector;
import org.labkey.test.pages.YAxisVariableSelector;
import org.labkey.test.util.CDSAsserts;
import org.labkey.test.util.CDSHelper;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.Ext4Helper;
import org.labkey.test.util.LogMethod;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.labkey.test.tests.CDSVisualizationTest.Locators.plotBox;
import static org.labkey.test.tests.CDSVisualizationTest.Locators.plotPoint;
import static org.labkey.test.tests.CDSVisualizationTest.Locators.plotTick;

@Category({CDS.class, Git.class})
public class CDSVisualizationTest extends CDSReadOnlyTest
{
    private final CDSHelper cds = new CDSHelper(this);
    private final CDSAsserts _asserts = new CDSAsserts(this);
    private final String PGROUP1 = "visgroup 1";
    private final String PGROUP2 = "visgroup 2";
    private final String PGROUP3 = "visgroup 3";
    private final String PGROUP3_COPY = "copy of visgroup 3";

    protected static final String MOUSEOVER_FILL = "#01BFC2";
    protected static final String MOUSEOVER_STROKE = "#00EAFF";
    protected static final String BRUSHED_FILL = "#14C9CC";
    protected static final String BRUSHED_STROKE = "#00393A";
    protected static final String NORMAL_COLOR = "#000000";

    @Before
    public void preTest()
    {
        cds.enterApplication();
        cds.ensureNoFilter();
        cds.ensureNoSelection();
    }

    @BeforeClass
    public static void initTest() throws Exception
    {
        CDSVisualizationTest cvt = (CDSVisualizationTest)getCurrentTest();
        //TODO add back (and improve already exists test) when verifySavedGroupPlot is implemented.
//        cvt.createParticipantGroups();
    }

    @AfterClass
    public static void afterClassCleanUp()
    {
        CDSVisualizationTest cvt = (CDSVisualizationTest)getCurrentTest();
        //TODO add back (and improve already exists test) when verifySavedGroupPlot is implemented.
//        cvt.deleteParticipantGroups();
    }

    @Override
    public List<String> getAssociatedModules()
    {
        return Arrays.asList("CDS");
    }

    @Override
    public BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }

    @Test
    public void verifyGutterPlotBasic()
    {

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        log("Validate that a y-axis gutter plot is generated.");
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.NAB);
        yaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        yaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ELISPOT);
        xaxis.pickVariable(CDSHelper.ELISPOT_MAGNITUDE_RAW);
        xaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();

        assertFalse("For BAMA Magnitude vs NAB Lab x-axis gutter plot was present it should not have been.", hasXGutter());
        assertTrue("For BAMA Magnitude vs NAB Lab y-axis gutter plot was not present.", hasYGutter());

        click(CDSHelper.Locators.cdsButtonLocator("clear"));

        // Makes the test a little more reliable.
        waitForElement(Locator.xpath("//div[contains(@class, 'noplotmsg')][not(contains(@style, 'display: none'))]"));

        log("Validate that a x-axis gutter plot is generated.");
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.NAB);
        yaxis.pickVariable(CDSHelper.NAB_TITERIC80);
        yaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_RAW);
        xaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();

        assertTrue("For NAB IC80 vs ICS Magnitude x-axis gutter plot was not present.", hasXGutter());
        assertFalse("For NAB IC80 vs ICS Magnitude y-axis gutter plot was present and it should not have been.", hasYGutter());

        click(CDSHelper.Locators.cdsButtonLocator("clear"));

        // Makes the test a little more reliable.
        waitForElement(Locator.xpath("//div[contains(@class, 'noplotmsg')][not(contains(@style, 'display: none'))]"));

        log("Validate that a gutter plot is generated for both the x and y axis.");
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_RAW);
        yaxis.setCellType(CDSHelper.CELL_TYPE_CD4);
        yaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();

        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_RAW);
        xaxis.setCellType(CDSHelper.CELL_TYPE_CD8);
        xaxis.confirmSelection();

        assertTrue("For ELISPOT Background vs ICS Visit x-axis gutter plot was not present.", hasXGutter());
        assertTrue("For ELISPOT Background vs ICS Visit y-axis gutter plot was not present.", hasYGutter());

        click(CDSHelper.Locators.cdsButtonLocator("clear"));

        // Makes the test a little more reliable.
        waitForElement(Locator.xpath("//div[contains(@class, 'noplotmsg')][not(contains(@style, 'display: none'))]"));

        log("Validate that a study axis (gutter plot with syringe glyph) is generated for the x axis.");
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ELISPOT);
        yaxis.pickVariable(CDSHelper.ELISPOT_MAGNITUDE_BACKGROUND_SUB);
        yaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.TIME_POINTS);
        xaxis.pickVariable(CDSHelper.TIME_POINTS_DAYS);
        xaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();

        assertTrue("For ELISPOT Background vs Time Visit Days a study axis was not present.", hasStudyAxis());
        assertFalse("For ELISPOT Background vs Time Visit Days x-axis gutter plot was present, it should not be.", hasXGutter());
        assertFalse("For ELISPOT Background vs Time Visit Days y-axis gutter plot was present, it should not be.", hasYGutter());

        click(CDSHelper.Locators.cdsButtonLocator("clear"));

    }

    @Test
    public void verifyScatterPlot()
    {
        //getText(Locator.css("svg")) on Chrome

        final String ELISPOT_VISIT = "0\n1000\n2000\n3000\n4000\n5000\n6000\n7000\n8000\n9000\n0\n5000\n10000\n15000\n20000\n25000\n30000\n35000\n40000\n45000"; // TODO Test data dependent.
        final String ICS_MAGNITUDE = "0\n1\n2\n3\n4\n5\n0\n0.5\n1\n1.5\n2\n2.5\n3\n3.5\n4\n4.5\n5"; // TODO Test data dependent.
        final String NAB_IC50 = "1\n10\n1\n10\n100\n1000"; // TODO Test data dependent.

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ELISPOT);
        xaxis.pickVariable(CDSHelper.ELISPOT_VISIT);
        xaxis.confirmSelection();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        yaxis.pickSource(CDSHelper.ELISPOT);
        yaxis.pickVariable(CDSHelper.ELISPOT_MAGNITUDE_BACKGROUND_SUB);
        yaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();

        if(CDSHelper.validateCounts)
        {
            assertSVG(ELISPOT_VISIT);
        }

        yaxis.openSelectorWindow();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        yaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();

        xaxis.openSelectorWindow();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        xaxis.pickSource(CDSHelper.NAB);
        xaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        xaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();

        assertTrue("For ELISPOT vs ICS x-axis gutter plot was not present.", hasXGutter());
        assertTrue("For ELISPOT vs ICS y-axis gutter plot was not present.", hasYGutter());

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        xaxis.confirmSelection();

        _ext4Helper.waitForMaskToDisappear();

        if(CDSHelper.validateCounts)
        {
            assertSVG(ICS_MAGNITUDE);
        }

        // Test log scales
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.NAB);
        yaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        yaxis.setScale(DataspaceVariableSelector.Scale.Log);
        yaxis.confirmSelection();

        assertTrue("For NAB vs ICS x-axis gutter plot was not present.", hasXGutter());
        assertTrue("For NAB vs ICS y-axis gutter plot was not present.", hasYGutter());

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.DEMOGRAPHICS);
        xaxis.pickVariable(CDSHelper.DEMO_AGE);
        xaxis.setScale(DataspaceVariableSelector.Scale.Log);
        xaxis.confirmSelection();

        assertTrue("For NAB vs Demographics x-axis gutter plot was not present.", hasXGutter());
        assertFalse("For NAB vs Demographics y-axis gutter plot was present and it should not be.", hasYGutter());

        if(CDSHelper.validateCounts)
        {
            assertSVG(NAB_IC50);
        }

        //comment starts here

        // TODO: Figure out and enable these hover selectors with completed data filters feature
//        Actions builder = new Actions(getDriver());
//       List<WebElement> points;
//       points = Locator.css("svg g a.point path").findElements(getDriver());
//
//       // Test hover events
//        builder.moveToElement(points.get(71)).perform();
//
//        // Check that related points are colored appropriately.
//       for (int i = 71; i < 76; i++)
//        {
//            assertEquals("Related point had an unexpected fill color", MOUSEOVER_FILL, points.get(i).getAttribute("fill"));
//            assertEquals("Related point had an unexpected stroke color", MOUSEOVER_STROKE, points.get(i).getAttribute("stroke"));
//
//        }
//
//        builder.moveToElement(points.get(33)).moveByOffset(10, 10).perform();
//
//        // Check that the points are no longer highlighted.
//        for (int i = 33; i < 38; i++)
//        {
//            assertEquals("Related point had an unexpected fill color", NORMAL_COLOR, points.get(i).getAttribute("fill"));
//            assertEquals("Related point had an unexpected stroke color", NORMAL_COLOR, points.get(i).getAttribute("stroke"));
//        }
//
//        // Test brush events.
//        builder.moveToElement(points.get(10)).moveByOffset(-45, -55).clickAndHold().moveByOffset(130, 160).release().perform();
//
//        for (int i = 10; i < 15; i++)
//        {
//            assertEquals("Brushed point had an unexpected fill color", BRUSHED_FILL, points.get(i).getAttribute("fill"));
//            assertEquals("Brushed point had an unexpected stroke color", BRUSHED_STROKE, points.get(i).getAttribute("stroke"));
//        }
//
//        builder.moveToElement(points.get(37)).moveByOffset(-25, 0).clickAndHold().release().perform();
//
//        // Check that the points are no longer brushed.
//        for (int i = 10; i < 15; i++)
//        {
//            assertEquals("Related point had an unexpected fill color", NORMAL_COLOR, points.get(i).getAttribute("fill"));
//            assertEquals("Related point had an unexpected stroke color", NORMAL_COLOR, points.get(i).getAttribute("stroke"));
//        }
//
//        // Brush the same area, then apply that selection as a filter.
//        builder.moveToElement(points.get(10)).moveByOffset(-45, -55).clickAndHold().moveByOffset(130, 160).release().perform();
//        waitForElement(Locators.plotSelection);
//
//        assertEquals("An unexpected number of plot selections were visible.", 2, Locators.plotSelection.findElements(getDriver()).size());
//        _asserts.assertSelectionStatusCounts(8, 1, 2);
//
//        Locators.plotSelectionCloseBtn.findElement(getDriver()).click(); // remove the x variable from the selection.
//        waitForElementToDisappear(Locators.plotSelectionCloseBtn.index(1));
//        _asserts.assertSelectionStatusCounts(13, 1, 2);
//        Locators.plotSelectionCloseBtn.findElement(getDriver()).click(); // remove the y variable from the selection.
//        assertElementNotPresent(Locators.plotSelection);
//
//        // Select them again and apply them as a filter.
//        builder.moveToElement(points.get(10)).moveByOffset(-25, -15).clickAndHold().moveByOffset(45, 40).release().perform();
//       waitForElement(Locators.plotSelection);
//
//        assertEquals("An unexpected number of plot selections were visible.", 2, Locators.plotSelection.findElements(getDriver()).size());
//        _asserts.assertSelectionStatusCounts(3, 1, 2);
//
//        cds.useSelectionAsDataFilter();
//        assertEquals("An unexpected number of plot selection filters were visible", 2, Locators.plotSelectionFilter.findElements(getDriver()).size());
//        _asserts.assertFilterStatusCounts(3, 1, 2);
//
//        // Test that variable selectors are reset when filters are cleared (Issue 20138).
//        cds.clearFilter();
//        waitForElement(Locator.css(".yaxisbtn span.x-btn-button").withText("choose variable"));
//        waitForElement(Locator.css(".xaxisbtn span.x-btn-button").withText("choose variable"));

        //commented out section end
    }

    @Test
    public void verifyBoxPlots()
    {
        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        // Choose the y-axis and verify that only 1 box plot shows if there is no x-axis chosen.
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND);
        yaxis.confirmSelection();

        waitForElement(plotBox);

        if(CDSHelper.validateCounts)
        {
            assertElementPresent(plotBox, 1);
            assertElementPresent(plotPoint, 3713); // TODO Test data dependent.
        }

        // Choose a categorical axis to verify that multiple box plots will appear.
        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.DEMOGRAPHICS);
        xaxis.pickVariable(CDSHelper.DEMO_SEX);
        xaxis.confirmSelection();

        waitForElement(Locators.plotTick.withText("Female"), 20000);

        waitForElement(Locators.plotBox);

        if(CDSHelper.validateCounts)
        {
            assertElementPresent(plotBox, 2);
            assertElementPresent(plotPoint, 3713); // TODO Test data dependent.
        }

        // Choose a continuous axis and verify that the chart goes back to being a scatter plot.
        xaxis.openSelectorWindow();
        xaxis.backToSource();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND);
        xaxis.confirmSelection();

        waitForElementToDisappear(plotBox);

        // Verify that we can go back to boxes after being in scatter mode.
        xaxis.openSelectorWindow();
        xaxis.backToSource();
        xaxis.pickSource(CDSHelper.DEMOGRAPHICS);
        xaxis.pickVariable(CDSHelper.DEMO_RACE);
        xaxis.confirmSelection();

        waitForElement(Locators.plotBox);
        waitForElement(Locators.plotTick.withText("Asian"), 20000); // TODO Test data dependent.

        if(CDSHelper.validateCounts)
        {
            assertElementPresent(plotBox, 7); // TODO Test data dependent.
            assertElementPresent(plotPoint, 3713); // TODO Test data dependent.
        }

        //Verify x axis categories are selectable as filters
        mouseOver(Locators.plotTick.withText("Asian")); // TODO Test data dependent.

        if(CDSHelper.validateCounts)
        {
            assertEquals("incorrect number of points highlighted after mousing over x axis category", 76, getPointCountByColor(MOUSEOVER_FILL)); // TODO Test data dependent.
        }

        click(Locators.plotTick.withText("Asian")); // TODO Test data dependent.
        //ensure filter buttons are present
        waitForElement(Locators.filterDataButton);
        assertElementPresent(Locators.removeButton);

        if(CDSHelper.validateCounts)
        {
            //ensure correct number of points are highlighted
            assertEquals("incorrect number of points highlighted after clicking x axis category", 76, getPointCountByColor(MOUSEOVER_FILL)); // TODO Test data dependent.
            //ensure correct total number of points
            assertEquals("incorrect total number of points after clicking x axis category", 3713, getPointCount()); // TODO Test data dependent.
            //apply category selection as a filter
        }

        // Need to do this because there is more than one "Filter" buton in the OM, but only want the visible one.
        waitAndClick(CDSHelper.Locators.cdsButtonLocator("Filter"));

        if(CDSHelper.validateCounts)
        {
            waitForPointCount(76, 20000); // TODO Test data dependent.
        }

        //clear filter
        click(CDSHelper.Locators.cdsButtonLocator("clear"));

        // Makes the test a little more reliable.
        waitForElement(Locator.xpath("//div[contains(@class, 'noplotmsg')][not(contains(@style, 'display: none'))]"));

        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_RAW); // Work around for issue 23845.
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND);
        yaxis.confirmSelection();
        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.DEMOGRAPHICS);
        xaxis.pickVariable(CDSHelper.DEMO_RACE);
        xaxis.confirmSelection();

        if(CDSHelper.validateCounts)
        {
            waitForPointCount(3713, 20000); // TODO Test data dependent.
        }

        //verify multi-select of categories
        selectXAxes(false, "White", "Other", "Native Hawaiian/Paci", "Native American/Alas"); // TODO Test data dependent.
        sleep(3000); // Let the animation end.

        if(CDSHelper.validateCounts)
        {
            //ensure correct number of points are highlighted
            assertEquals("incorrect number of points highlighted after clicking x axis categories",2707, getPointCountByColor(MOUSEOVER_FILL)); // TODO Test data dependent.
            assertEquals("incorrect total number of points after clicking x axis categories",3713, getPointCount()); // TODO Test data dependent.
            //apply selection as exlusive filter
            waitAndClick(CDSHelper.Locators.cdsButtonLocator("Remove"));
            waitForPointCount(3713 - 2707, 10000); // TODO Test data dependent.
        }

        click(CDSHelper.Locators.cdsButtonLocator("clear"));

    }

// TODO CDS does not work with groups created in LabKey, the groups need to be created in CDS.
//    @Test
    public void verifySavedGroupPlot()
    {
        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        yaxis.openSelectorWindow();
        yaxis.pickMeasure("Physical Exam", "Diastolic Blood Pressure");
        yaxis.confirmSelection();

        xaxis.openSelectorWindow();
        xaxis.pickMeasure("User groups", "My saved groups");
        xaxis.setVariableOptions(PGROUP1, PGROUP2, PGROUP3);
        xaxis.confirmSelection();

        waitForElement(plotTick.withText(PGROUP1));
        waitForElement(plotTick.withText(PGROUP2));
        waitForElement(plotTick.withText(PGROUP3));
        assertElementPresent(plotBox, 3);
        waitForElement(plotTick.withText("115"));
        waitForElement(plotTick.withText("70"));

        xaxis.openSelectorWindow();
        xaxis.setVariableOptions(PGROUP1, PGROUP2);
        xaxis.confirmSelection();

        waitForElement(plotTick.withText(PGROUP1));
        waitForElement(plotTick.withText(PGROUP2));
        waitForElementToDisappear(plotTick.withText(PGROUP3));
        assertElementPresent(plotBox, 2);
        waitForElementToDisappear(plotTick.withText("115"));
        waitForElementToDisappear(plotTick.withText("70"));

        xaxis.openSelectorWindow();
        xaxis.setVariableOptions(PGROUP3, PGROUP3_COPY);
        xaxis.confirmSelection();

        waitForElementToDisappear(plotTick.withText(PGROUP1));
        waitForElementToDisappear(plotTick.withText(PGROUP2));
        waitForElement(plotTick.withText(PGROUP3));
        waitForElement(plotTick.withText(PGROUP3_COPY));
        assertElementPresent(plotBox, 2);
        waitForElement(plotTick.withText("115"));
        waitForElement(plotTick.withText("70"));
    }

    @Test
    public void verifyAxisSelectors()
    {

        final String[][] Y_AXIS_SOURCES =
                {{CDSHelper.DEMOGRAPHICS, CDSHelper.DEMO_AGEGROUP, CDSHelper.DEMO_AGE, CDSHelper.DEMO_BMI},
                        {CDSHelper.BAMA, CDSHelper.BAMA_MAGNITUDE_BLANK, CDSHelper.BAMA_MAGNITUDE_BASELINE, CDSHelper.BAMA_MAGNITUDE_DELTA, CDSHelper.BAMA_MAGNITUDE_RAW, CDSHelper.BAMA_MAGNITUDE_DELTA_BASELINE, CDSHelper.BAMA_MAGNITUDE_RAW_BASELINE},
                        {CDSHelper.ELISPOT, CDSHelper.ELISPOT_MAGNITUDE_BACKGROUND, CDSHelper.ELISPOT_MAGNITUDE_BACKGROUND_SUB, CDSHelper.ELISPOT_MAGNITUDE_RAW},
                        {CDSHelper.ICS, CDSHelper.ICS_MAGNITUDE_BACKGROUND, CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB, CDSHelper.ICS_MAGNITUDE_BACKGROUND_RAW},
                        {CDSHelper.NAB, CDSHelper.NAB_TITERIC50, CDSHelper.NAB_TITERIC80}};
        final String[][] X_AXIS_SOURCES =
                {{CDSHelper.DEMOGRAPHICS, CDSHelper.DEMO_AGEGROUP, CDSHelper.DEMO_AGE, CDSHelper.DEMO_BMI, CDSHelper.DEMO_CIRCUMCISED, CDSHelper.DEMO_COUNTRY, CDSHelper.DEMO_HISPANIC, CDSHelper.DEMO_RACE, CDSHelper.DEMO_SEX, CDSHelper.DEMO_SPECIES, CDSHelper.DEMO_SUBSPECIES, CDSHelper.DEMO_VISIT},
                        {CDSHelper.TIME_POINTS, CDSHelper.TIME_POINTS_DAYS, CDSHelper.TIME_POINTS_WEEKS, CDSHelper.TIME_POINTS_MONTHS},
                        {CDSHelper.BAMA, CDSHelper.BAMA_ANTIGEN_CLADE, CDSHelper.BAMA_ANTIGEN_NAME, CDSHelper.BAMA_ANTIGEN_TYPE, CDSHelper.BAMA_ASSAY, CDSHelper.BAMA_DETECTION, CDSHelper.BAMA_DILUTION, CDSHelper.BAMA_EXP_ASSAYD, CDSHelper.BAMA_INSTRUMENT_CODE, CDSHelper.BAMA_ISOTYPE, CDSHelper.BAMA_LAB, CDSHelper.BAMA_MAGNITUDE_BLANK, CDSHelper.BAMA_MAGNITUDE_BASELINE, CDSHelper.BAMA_MAGNITUDE_DELTA, CDSHelper.BAMA_MAGNITUDE_RAW, CDSHelper.BAMA_MAGNITUDE_DELTA_BASELINE, CDSHelper.BAMA_MAGNITUDE_RAW_BASELINE, CDSHelper.BAMA_PROTEIN, CDSHelper.BAMA_PROTEIN_PANEL, CDSHelper.BAMA_RESPONSE_CALL, CDSHelper.BAMA_SPECIMEN, CDSHelper.BAMA_VACCINE, CDSHelper.BAMA_VISIT, CDSHelper.BAMA_VISIT_DAY},
                        {CDSHelper.ELISPOT, CDSHelper.ELISPOT_ANTIGEN, CDSHelper.ELISPOT_ASSAY, CDSHelper.ELISPOT_CELL_NAME, CDSHelper.ELISPOT_CELL_TYPE, CDSHelper.ELISPOT_EXP_ASSAY, CDSHelper.ELISPOT_MARKER_NAME, CDSHelper.ELISPOT_MARKER_TYPE, CDSHelper.ELISPOT_LAB, CDSHelper.ELISPOT_MAGNITUDE_BACKGROUND, CDSHelper.ELISPOT_MAGNITUDE_BACKGROUND_SUB, CDSHelper.ELISPOT_MAGNITUDE_RAW, CDSHelper.ELISPOT_PROTEIN, CDSHelper.ELISPOT_PROTEIN_PANEL, CDSHelper.ELISPOT_RESPONSE, CDSHelper.ELISPOT_SPECIMEN, CDSHelper.ELISPOT_VACCINE, CDSHelper.ELISPOT_VISIT, CDSHelper.ELISPOT_VISIT_DAY},
                        {CDSHelper.ICS, CDSHelper.ICS_ANTIGEN, CDSHelper.ICS_ASSAY, CDSHelper.ICS_CELL_NAME, CDSHelper.ICS_CELL_TYPE, CDSHelper.ICS_EXP_ASSAY, CDSHelper.ICS_MARKER_NAME, CDSHelper.ICS_MARKER_TYPE, CDSHelper.ICS_LAB, CDSHelper.ICS_MAGNITUDE_BACKGROUND, CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB, CDSHelper.ICS_MAGNITUDE_BACKGROUND_RAW, CDSHelper.ICS_PROTEIN, CDSHelper.ICS_PROTEIN_PANEL, CDSHelper.ICS_RESPONSE, CDSHelper.ICS_SPECIMEN, CDSHelper.ICS_VISIT},
                        {CDSHelper.NAB, CDSHelper.NAB_ANTIGEN, CDSHelper.NAB_ANTIGEN_CLADE, CDSHelper.NAB_EXP_ASSAY, CDSHelper.NAB_INIT_DILUTION, CDSHelper.NAB_LAB, CDSHelper.NAB_RESPONSE, CDSHelper.NAB_SPECIMEN, CDSHelper.NAB_TARGET_CELL, CDSHelper.NAB_TITERIC50, CDSHelper.NAB_TITERIC80, CDSHelper.NAB_VISIT, CDSHelper.NAB_VISIT_DAY}};
        final String[][] COLOR_AXIS_SOURCES =
                {{CDSHelper.DEMOGRAPHICS, CDSHelper.DEMO_CIRCUMCISED, CDSHelper.DEMO_COUNTRY, CDSHelper.DEMO_HISPANIC, CDSHelper.DEMO_RACE, CDSHelper.DEMO_SEX, CDSHelper.DEMO_SPECIES, CDSHelper.DEMO_SUBSPECIES},
                        {CDSHelper.BAMA, CDSHelper.BAMA_ANTIGEN_CLADE, CDSHelper.BAMA_ANTIGEN_NAME, CDSHelper.BAMA_ANTIGEN_TYPE, CDSHelper.BAMA_ASSAY, CDSHelper.BAMA_DETECTION, CDSHelper.BAMA_INSTRUMENT_CODE, CDSHelper.BAMA_ISOTYPE, CDSHelper.BAMA_LAB, CDSHelper.BAMA_PROTEIN, CDSHelper.BAMA_PROTEIN_PANEL, CDSHelper.BAMA_RESPONSE_CALL, CDSHelper.BAMA_SPECIMEN, CDSHelper.BAMA_VACCINE},
                        {CDSHelper.ELISPOT, CDSHelper.ELISPOT_ANTIGEN, CDSHelper.ELISPOT_ASSAY, CDSHelper.ELISPOT_CELL_NAME, CDSHelper.ELISPOT_CELL_TYPE, CDSHelper.ELISPOT_CLADE, CDSHelper.ELISPOT_MARKER_NAME, CDSHelper.ELISPOT_MARKER_TYPE, CDSHelper.ELISPOT_LAB, CDSHelper.ELISPOT_PROTEIN, CDSHelper.ELISPOT_PROTEIN_PANEL, CDSHelper.ELISPOT_RESPONSE, CDSHelper.ELISPOT_SPECIMEN, CDSHelper.ELISPOT_VACCINE},
                        {CDSHelper.ICS, CDSHelper.ICS_ANTIGEN, CDSHelper.ICS_ASSAY, CDSHelper.ICS_CELL_NAME, CDSHelper.ICS_CELL_TYPE, CDSHelper.ICS_MARKER_NAME, CDSHelper.ICS_MARKER_TYPE, CDSHelper.ICS_LAB, CDSHelper.ICS_PROTEIN, CDSHelper.ICS_PROTEIN_PANEL, CDSHelper.ICS_RESPONSE, CDSHelper.ICS_SPECIMEN},
                        {CDSHelper.NAB, CDSHelper.NAB_ANTIGEN, CDSHelper.NAB_ANTIGEN_CLADE, CDSHelper.NAB_ASSAY, CDSHelper.NAB_LAB, CDSHelper.NAB_RESPONSE, CDSHelper.NAB_SPECIMEN, CDSHelper.NAB_TARGET_CELL}};

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        this.log("Validating the x-axis selector.");
        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        xaxis.openSelectorWindow();

        this.log("Validating the x-axis header text.");
        assertTrue(this.isElementVisible(Locator.xpath("//div[contains(@class, 'x-axis-selector')]//div[contains(@class, 'main-title')][text()='x-axis']")));
        assertTrue(this.isElementVisible(Locator.xpath("//div[contains(@class, 'x-axis-selector')]//div[contains(@class, 'sub-title')]//span[contains(@class, 'nav-text')][text()='Sources']")));
        assertTrue(this.isElementVisible(Locator.xpath("//div[contains(@class, 'x-axis-selector')]//div[contains(@class, 'sub-title')]//span[contains(@class, 'subject-count')][text()='Subject count']")));

        this.log("Validating the x-axis sources.");
        for (String[] src : X_AXIS_SOURCES)
        {
            assertTrue(this.isElementVisible(xaxis.window().append(" div.content-label").withText(src[0])));
            this.log("Validating variables for " + src[0]);
            click(xaxis.window().append(" div.content-label").withText(src[0]));
            this.waitForElement(Locator.xpath("//div[contains(@class, 'x-axis-selector')]//span[contains(@class, 'section-title')][text()='" + src[0] + "']"));
            for (int i = 1; i < src.length; i++)
            {
                assertTrue(this.isElementVisible(xaxis.window().append(" div.content-label").withText(src[i])));
                click(xaxis.window().append(" div.content-label").withText(src[i]));
            }
            xaxis.backToSource();
        }

        this.log("Validating the x-axis cancel button.");
        xaxis.cancelSelection();

        this.log("Validating the y-axis selector.");
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);
        yaxis.openSelectorWindow();

        this.log("Validating the y-axis header text.");
        assertTrue(this.isElementVisible(Locator.xpath("//div[contains(@class, 'y-axis-selector')]//div[contains(@class, 'main-title')][text()='y-axis']")));
        assertTrue(this.isElementVisible(Locator.xpath("//div[contains(@class, 'y-axis-selector')]//div[contains(@class, 'sub-title')]//span[contains(@class, 'nav-text')][text()='Sources']")));
        assertTrue(this.isElementVisible(Locator.xpath("//div[contains(@class, 'y-axis-selector')]//div[contains(@class, 'sub-title')]//span[contains(@class, 'subject-count')][text()='Subject count']")));

        this.log("Validating the y-axis sources.");
        for (String[] src : Y_AXIS_SOURCES)
        {
            assertTrue(this.isElementVisible(yaxis.window().append(" div.content-label").withText(src[0])));
            this.log("Validating variables for " + src[0]);
            click(yaxis.window().append(" div.content-label").withText(src[0]));
            this.waitForElement(Locator.xpath("//div[contains(@class, 'y-axis-selector')]//span[contains(@class, 'section-title')][text()='" + src[0] + "']"));
            for (int i = 1; i < src.length; i++)
            {
                assertTrue(this.isElementVisible(yaxis.window().append(" div.content-label").withText(src[i])));
                click(yaxis.window().append(" div.content-label").withText(src[i]));
            }
            yaxis.backToSource();
        }

        this.log("Validating the y-axis cancel button.");
        yaxis.cancelSelection();

        this.log("Validating the color-axis selector.");
        ColorAxisVariableSelector coloraxis = new ColorAxisVariableSelector(this);
        coloraxis.openSelectorWindow();

        this.log("Validating the color-axis header text.");
        assertTrue(this.isElementVisible(Locator.xpath("//div[contains(@class, 'color-axis-selector')]//div[contains(@class, 'main-title')][text()='color']")));
        assertTrue(this.isElementVisible(Locator.xpath("//div[contains(@class, 'color-axis-selector')]//div[contains(@class, 'sub-title')]//span[contains(@class, 'nav-text')][text()='Sources']")));
        assertTrue(this.isElementVisible(Locator.xpath("//div[contains(@class, 'color-axis-selector')]//div[contains(@class, 'sub-title')]//span[contains(@class, 'subject-count')][text()='Subject count']")));

        this.log("Validating the color-axis sources.");
        for (String[] src : COLOR_AXIS_SOURCES)
        {
            assertTrue(this.isElementVisible(coloraxis.window().append(" div.content-label").withText(src[0])));
            this.log("Validating variables for " + src[0]);
            click(coloraxis.window().append(" div.content-label").withText(src[0]));
            this.waitForElement(Locator.xpath("//div[contains(@class, 'color-axis-selector')]//span[contains(@class, 'section-title')][text()='" + src[0] + "']"));
            for (int i = 1; i < src.length; i++)
            {
                assertTrue(this.isElementVisible(coloraxis.window().append(" div.content-label").withText(src[i])));
                click(coloraxis.window().append(" div.content-label").withText(src[i]));
            }
            coloraxis.backToSource();
        }

        this.log("Validating the color-axis cancel button.");
        coloraxis.cancelSelection();

    }

    @Test
    public void verifyScatterPlotColorAxis()
    {
        CDSHelper cds = new CDSHelper(this);

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        ColorAxisVariableSelector color = new ColorAxisVariableSelector(this);
        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.NAB);
        xaxis.pickVariable(CDSHelper.NAB_DATA);
        xaxis.setVirusName(cds.buildIdentifier(CDSHelper.COLUMN_ID_NEUTRAL_TIER, CDSHelper.NEUTRAL_TIER_1));
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        xaxis.confirmSelection();
        // yaxis window opens automatically
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        yaxis.pickSource(CDSHelper.NAB);
        yaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        yaxis.setVirusName(cds.buildIdentifier(CDSHelper.COLUMN_ID_NEUTRAL_TIER, CDSHelper.NEUTRAL_TIER_1));
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        yaxis.confirmSelection();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        color.openSelectorWindow();
        color.pickSource(CDSHelper.DEMOGRAPHICS);
        color.pickVariable(CDSHelper.DEMO_RACE);
        color.confirmSelection();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        Locator.CssLocator colorLegend = Locator.css("#color-legend > svg");
        Locator.CssLocator colorLegendGlyph = colorLegend.append("> .legend-point");
        waitForElement(colorLegend);
        assertElementPresent(colorLegendGlyph, 7);

        // TODO Need to revisit this part of the test. Specifically there no longer is a 'Race' attribute to look for.
/*
        List<WebElement> legendGlyphs = colorLegendGlyph.findElements(getDriver());
        Map<String, Integer> raceCounts = new HashMap<>();
        raceCounts.put("American Indian/Alaska Native", 10); // too tired to fix this
        raceCounts.put("American Indian/Alaskan Native", 46);
        raceCounts.put("Asian", 62);
        raceCounts.put("Black/African American", 103);
        raceCounts.put("Indian", 81);
        raceCounts.put("Native Hawaiian or Other Pacific Islander", 16);
        raceCounts.put("Native Hawaiian/Pacific Islander", 21);
        raceCounts.put("White", 129);

        Set<String> foundRaces = new HashSet<>();

        // uncomment if you want help determining these counts
        for (WebElement el : legendGlyphs)
        {
            String fill = el.getAttribute("fill");
            String path = el.getAttribute("d");
            List<WebElement> points = Locator.css(String.format("a.point > path[fill='%s'][d='%s']", fill, path)).findElements(getDriver());

            String race = getPointProperty("Race", points.get(0).findElement(By.xpath("..")));
            log(race + ": (" + points.size() + ")");
        }

        for (WebElement el : legendGlyphs)
        {
            String fill = el.getAttribute("fill");
            String path = el.getAttribute("d");
            List<WebElement> points = Locator.css(String.format("a.point > path[fill='%s'][d='%s']", fill, path)).findElements(getDriver());

            String race = getPointProperty("Race", points.get(0).findElement(By.xpath("..")));
            assertEquals("Wrong number of points for race: " + race, raceCounts.get(race), (Integer)points.size());

            foundRaces.add(race);
        }

        assertEquals("Found incorrect Races", raceCounts.keySet(), foundRaces);

        int expectedPointCount = 0;
        for (Map.Entry<String, Integer> raceCount : raceCounts.entrySet())
        {
            expectedPointCount += raceCount.getValue();
        }
        assertEquals("Wrong number of points on scatter plot", expectedPointCount, Locator.css("a.point").findElements(getDriver()).size());

        // issue 20446
        color.openSelectorWindow();
        color.pickMeasure("Demographics", "Race");
        color.confirmSelection();
        assertEquals("Wrong number of points on scatter plot", expectedPointCount, Locator.css("a.point").findElements(getDriver()).size());
        waitForElement(colorLegendGlyph);
        assertElementPresent(colorLegendGlyph, 8);
        */
    }

    @Test
    public void verifyTimeAxisBasic()
    {
        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        Map expectedCounts = new HashMap<String, CDSHelper.TimeAxisData>();
        expectedCounts.put("HVTN 041", new CDSHelper.TimeAxisData("HVTN 041", 3, 6, 0));
        expectedCounts.put("HVTN 049", new CDSHelper.TimeAxisData("HVTN 049", 6, 8, 0));
        expectedCounts.put("HVTN 049x", new CDSHelper.TimeAxisData("HVTN 049x", 3, 7, 0));
        expectedCounts.put("HVTN 094", new CDSHelper.TimeAxisData("HVTN 094", 6, 22, 0));
        expectedCounts.put("HVTN 096", new CDSHelper.TimeAxisData("HVTN 096", 4, 9, 0));
        expectedCounts.put("HVTN 203", new CDSHelper.TimeAxisData("HVTN 0203", 4, 6, 0));
        expectedCounts.put("HVTN 205", new CDSHelper.TimeAxisData("HVTN 0205", 0, 0, 0));

        final String yaxisScale = "\n0\n200\n400\n600\n800\n1000\n1200\n1400\n1600\n1800"; // TODO Test data dependent.
        final String studyDaysScales = "0\n100\n200\n300\n400\n500\n600" + yaxisScale; // TODO Test data dependent.
        final String studyDaysScaleAligedVaccination = "-300\n-200\n-100\n0\n100\n200\n300" + yaxisScale; // TODO Test data dependent.
        final String studyWeeksScales = "0\n20\n40\n60\n80" + yaxisScale; // TODO Test data dependent.
        final String studyWeeksScalesAlignedVaccination = "-40\n-20\n0\n20\n40" + yaxisScale; // TODO Test data dependent.
        final String studyMonthsScales = "0\n5\n10\n15\n20" + yaxisScale; // TODO Test data dependent.
        final String studyMonthsScalesAlignedVaccination = "-10\n-5\n0\n5\n10" + yaxisScale; // TODO Test data dependent.

        log("Verify NAb Titer IC50, A3R5 and Study Days.");
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.NAB);
        yaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        yaxis.setTargetCell(CDSHelper.TARGET_CELL_A3R5);
        yaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.TIME_POINTS);
        xaxis.pickVariable(CDSHelper.TIME_POINTS_DAYS);
        xaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();

        assertTrue("For NAb Titer 50, A3R5 vs Time Visit Days a study axis was not present.", hasStudyAxis());
        List<WebElement> studies = Locator.css("#study-axis > svg > g.study").findElements(getDriver());
        assertTrue("Expected " + expectedCounts.size() + " studies in the Time Axis, found" + studies.size() + ".", studies.size() == expectedCounts.size());
        log("Study count was as expected.");

        validateVisitCounts(studies, expectedCounts);
        assertSVG(studyDaysScales);

        log("Change x-axis to Study weeks, verify visit counts don't change.");
        xaxis.openSelectorWindow();
        // Should go to the variable selector window by default.
        xaxis.pickVariable(CDSHelper.TIME_POINTS_WEEKS);
        xaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();
        sleep(CDSHelper.CDS_WAIT_ANIMATION); // yuck.

        // Need to get studies again, otherwise get a stale element error.
        studies = Locator.css("#study-axis > svg > g.study").findElements(getDriver());
        assertTrue("Expected " + expectedCounts.size() + " studies in the Time Axis, found " + studies.size() + ".", studies.size() == expectedCounts.size());
        log("Study count was as expected.");

        validateVisitCounts(studies, expectedCounts);
        assertSVG(studyWeeksScales);

        log("Change x-axis to Study months, verify visit counts don't change.");
        xaxis.openSelectorWindow();
        // Should go to the variable selector window by default.
        xaxis.pickVariable(CDSHelper.TIME_POINTS_MONTHS);
        xaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();
        sleep(CDSHelper.CDS_WAIT_ANIMATION); // yuck.

        studies = Locator.css("#study-axis > svg > g.study").findElements(getDriver());
        assertTrue("Expected 7 studies in the Time Axis, found " + studies.size() + ".", studies.size() == 7);
        log("Study count was as expected.");

        validateVisitCounts(studies, expectedCounts);
        assertSVG(studyMonthsScales);

        log("Change x-axis to Study days, change alignment to Enrollment, verify visit counts are as expected.");
        xaxis.openSelectorWindow();
        // Should go to the variable selector window by default.
        xaxis.pickVariable(CDSHelper.TIME_POINTS_DAYS);
        xaxis.setAlignedBy(CDSHelper.TIME_POINTS_ALIGN_ENROLL);
        xaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();
        sleep(CDSHelper.CDS_WAIT_ANIMATION); // yuck. Unfortunately waitForMaskToDisappear is not long enough for the axis to be regenerated.

        // When changing the alignment to anything other than Day 0 study HVTN 205 will not appear because it has no visit information.
        expectedCounts.remove("HVTN 205");

        studies = Locator.css("#study-axis > svg > g.study").findElements(getDriver());
        assertTrue("Expected " + expectedCounts.size() + " studies in the Time Axis, found " + studies.size() + ".", studies.size() == expectedCounts.size());
        log("Study count was as expected.");

        validateVisitCounts(studies, expectedCounts);
        assertSVG(studyDaysScales);

        log("Change x-axis alignment to Last Vaccination, verify visit counts are as expected.");
        xaxis.openSelectorWindow();
        // Should go to the variable selector window by default.
        xaxis.setAlignedBy("Last Vaccination");
        xaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();
        sleep(CDSHelper.CDS_WAIT_ANIMATION); // yuck.

        studies = Locator.css("#study-axis > svg > g.study").findElements(getDriver());
        assertTrue("Expected " + expectedCounts.size() + " studies in the Time Axis, found " + studies.size() + ".", studies.size() == expectedCounts.size());
        log("Study count was as expected.");

        validateVisitCounts(studies, expectedCounts);
        assertSVG(studyDaysScaleAligedVaccination);

        log("Change x-axis to Study weeks, and go back to aligned by Enrollment, verify visit are as expected.");
        xaxis.openSelectorWindow();
        // Should go to the variable selector window by default.
        xaxis.pickVariable(CDSHelper.TIME_POINTS_WEEKS);
        xaxis.setAlignedBy(CDSHelper.TIME_POINTS_ALIGN_ENROLL);
        xaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();
        sleep(CDSHelper.CDS_WAIT_ANIMATION); // yuck.

        // Need to get studies again, otherwise get a stale element error.
        studies = Locator.css("#study-axis > svg > g.study").findElements(getDriver());
        assertTrue("Expected " + expectedCounts.size() + " studies in the Time Axis, found " + studies.size() + ".", studies.size() == expectedCounts.size());
        log("Study count was as expected.");

        validateVisitCounts(studies, expectedCounts);
        assertSVG(studyWeeksScales);

        log("Change x-axis Aligned by Last Vaccination, verify visit are as expected.");
        xaxis.openSelectorWindow();
        // Should go to the variable selector window by default.
        xaxis.pickVariable(CDSHelper.TIME_POINTS_WEEKS);
        xaxis.setAlignedBy(CDSHelper.TIME_POINTS_ALIGN_LAST_VAC);
        xaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();
        sleep(CDSHelper.CDS_WAIT_ANIMATION); // yuck.

        // Need to get studies again, otherwise get a stale element error.
        studies = Locator.css("#study-axis > svg > g.study").findElements(getDriver());
        assertTrue("Expected " + expectedCounts.size() + " studies in the Time Axis, found " + studies.size() + ".", studies.size() == expectedCounts.size());
        log("Study count was as expected.");

        validateVisitCounts(studies, expectedCounts);
        assertSVG(studyWeeksScalesAlignedVaccination);

        log("Change x-axis to Study months, and go back to aligned by Enrollment, verify visit are as expected.");
        xaxis.openSelectorWindow();
        // Should go to the variable selector window by default.
        xaxis.pickVariable(CDSHelper.TIME_POINTS_MONTHS);
        xaxis.setAlignedBy(CDSHelper.TIME_POINTS_ALIGN_ENROLL);
        xaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();
        sleep(CDSHelper.CDS_WAIT_ANIMATION); // yuck.

        // Need to get studies again, otherwise get a stale element error.
        studies = Locator.css("#study-axis > svg > g.study").findElements(getDriver());
        assertTrue("Expected " + expectedCounts.size() + " studies in the Time Axis, found " + studies.size() + ".", studies.size() == expectedCounts.size());
        log("Study count was as expected.");

        validateVisitCounts(studies, expectedCounts);
        assertSVG(studyMonthsScales);

        log("Change x-axis Aligned by Last Vaccination, verify visit are as expected.");
        xaxis.openSelectorWindow();
        // Should go to the variable selector window by default.
        xaxis.pickVariable(CDSHelper.TIME_POINTS_MONTHS);
        xaxis.setAlignedBy(CDSHelper.TIME_POINTS_ALIGN_LAST_VAC);
        xaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();
        sleep(CDSHelper.CDS_WAIT_ANIMATION); // yuck.

        // Need to get studies again, otherwise get a stale element error.
        studies = Locator.css("#study-axis > svg > g.study").findElements(getDriver());
        assertTrue("Expected " + expectedCounts.size() + " studies in the Time Axis, found " + studies.size() + ".", studies.size() == expectedCounts.size());
        log("Study count was as expected.");

        validateVisitCounts(studies, expectedCounts);
        assertSVG(studyMonthsScalesAlignedVaccination);

        click(CDSHelper.Locators.cdsButtonLocator("clear"));

        // Makes the test a little more reliable.
        waitForElement(Locator.xpath("//div[contains(@class, 'noplotmsg')][not(contains(@style, 'display: none'))]"));

    }

    @Test
    public void verifyAntigenVariableSelector()
    {
        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);
        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        ColorAxisVariableSelector coloraxis = new ColorAxisVariableSelector(this);

        log("Validate BAMA Antigen panel on yaxis.");
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.BAMA);
        yaxis.pickVariable(CDSHelper.BAMA_MAGNITUDE_DELTA_BASELINE);
        yaxis.openAntigenPanel();

        for(int i = 0; i < CDSHelper.ANTIGENS_NAME.length; i++)
        {
            assertElementVisible(Locator.xpath("//div[contains(@class, 'y-axis-selector')]//div[contains(@class, 'content')]//label[contains(@class, 'x-form-cb-label')][text()='" + CDSHelper.ANTIGENS_NAME[i] + "']"));
        }

        yaxis.cancelSelection();

        log("Validate BAMA Antigen panel on xaxis.");
        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.BAMA);
        xaxis.pickVariable(CDSHelper.BAMA_MAGNITUDE_DELTA_BASELINE);
        xaxis.openAntigenPanel();

        for(int i = 0; i < CDSHelper.ANTIGENS_NAME.length; i++)
        {
            assertElementVisible(Locator.xpath("//div[contains(@class, 'x-axis-selector')]//div[contains(@class, 'content')]//label[contains(@class, 'x-form-cb-label')][text()='" + CDSHelper.ANTIGENS_NAME[i] + "']"));
        }

        xaxis.cancelSelection();

        log("Validate Antigen panel does not show up on the color selector.");
        coloraxis.openSelectorWindow();
        coloraxis.pickSource(CDSHelper.BAMA);
        assertElementNotPresent("Detail seletor present in color selector, it should not be there.", Locator.xpath("//div[contains(@class, 'color-axis-selector')]//div[contains(@class, 'advanced')]//fieldset//div[contains(@class, 'field-label')][text()='Antigen name:']"));
        coloraxis.cancelSelection();

    }

    @Test
    public void verifyAntigenBoxPlot()
    {
        CDSHelper cds = new CDSHelper(this);
        String sharedVirus = CDSHelper.VIRUS_Q23;
        String uniqueVirus = CDSHelper.VIRUS_BAL26;
        String uniqueVirusId = cds.buildIdentifier(CDSHelper.COLUMN_ID_VIRUS_NAME, CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, uniqueVirus);

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.NAB);
        xaxis.pickVariable(CDSHelper.NAB_LAB);
        xaxis.setVirusName(uniqueVirusId);
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        xaxis.confirmSelection();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        yaxis.pickSource(CDSHelper.NAB);
        yaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        yaxis.setTargetCell(CDSHelper.TARGET_CELL_A3R5);
        yaxis.setVirusName(uniqueVirusId);
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        yaxis.confirmSelection();

        waitForElement(plotTick.withText(CDSHelper.LABS[2]));
        assertElementPresent(plotBox, 1);

        click(CDSHelper.Locators.cdsButtonLocator("view data"));
        sleep(CDSHelper.CDS_WAIT);
        switchToWindow(1);

        DataRegionTable plotDataTable = new DataRegionTable("query", this);
        assertEquals(100, plotDataTable.getDataRowCount());
        assertEquals(100, getElementCount(Locator.tagContainingText("td", uniqueVirus)));
        assertTextNotPresent(sharedVirus, CDSHelper.LABS[1]);
        getDriver().close();
        switchToMainWindow();

        // Current sample data only has viruses that are matched to one lab.
        // Changing original logic of test to have x-axis look at virus type.

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.NAB);
        xaxis.pickVariable(CDSHelper.NAB_VIRUS_TYPE);
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        xaxis.confirmSelection();

        waitForElement(plotTick.withText("Pseudovirus"));
        assertElementPresent(plotBox, 2);

        click(CDSHelper.Locators.cdsButtonLocator("view data"));
        sleep(CDSHelper.CDS_WAIT);
        switchToWindow(1);
        plotDataTable = new DataRegionTable("query", this);
        assertEquals(100, plotDataTable.getDataRowCount());
        assertEquals(100, getElementCount(Locator.tagContainingText("td", uniqueVirus)));
        getDriver().close();
        switchToMainWindow();

    }

    @Test
    public void verifyAntigenScatterPlot()
    {
        CDSHelper cds = new CDSHelper(this);
        String xVirus = CDSHelper.VIRUS_TV1;
        String yVirus = CDSHelper.VIRUS_SF162;
        String xVirusId = cds.buildIdentifier(CDSHelper.COLUMN_ID_VIRUS_NAME, CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_C, xVirus);
        String y1VirusId = cds.buildIdentifier(CDSHelper.COLUMN_ID_VIRUS_NAME, CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, yVirus);

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.NAB);
        xaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        xaxis.setVirusName(xVirusId);
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        xaxis.confirmSelection();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        yaxis.pickSource(CDSHelper.NAB);
        yaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        yaxis.setVirusName(y1VirusId);
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        yaxis.confirmSelection();

        waitForElement(plotTick.withText("5000"));
        assertElementPresent(plotPoint, 1321);

        click(CDSHelper.Locators.cdsButtonLocator("view data"));
        sleep(CDSHelper.CDS_WAIT);
        switchToWindow(1);
        Ext4Helper.resetCssPrefix();
        DataRegionTable plotDataTable = new DataRegionTable("query", this);
        assertEquals(100, plotDataTable.getDataRowCount());
        getDriver().close();
        switchToMainWindow();
        Ext4Helper.setCssPrefix("x-");

        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.NAB);
        yaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        yaxis.setVirusName(cds.buildIdentifier(CDSHelper.COLUMN_ID_NEUTRAL_TIER, "all"));
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        yaxis.confirmSelection();

        waitForElement(plotTick.withText("40"));
        assertElementPresent(plotPoint, 60);

        click(CDSHelper.Locators.cdsButtonLocator("view data"));
        sleep(CDSHelper.CDS_WAIT);
        switchToWindow(1);
        Ext4Helper.resetCssPrefix();
        plotDataTable = new DataRegionTable("query", this);
        assertEquals(60, plotDataTable.getDataRowCount());
        getDriver().close();
        switchToMainWindow();
    }

    @Test
    public void verifyBinnedPlot()
    {
        CDSHelper cds = new CDSHelper(this);

        // make choices that put us over the 'maxRows' parameter specified on the URL
        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        // set the x-axis
        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_DATA);
        xaxis.setDataSummaryLevel(CDSHelper.DATA_SUMMARY_PROTEIN);
        xaxis.setProtein(cds.buildIdentifier(CDSHelper.DATA_SUMMARY_PROTEIN_PANEL, "all"));
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        xaxis.setCellType("All");
        xaxis.confirmSelection();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        // set the y-axis
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_RAW);
        yaxis.setCellType("All");
        yaxis.setDataSummaryLevel(CDSHelper.DATA_SUMMARY_PROTEIN);
        yaxis.setProtein(cds.buildIdentifier(CDSHelper.DATA_SUMMARY_PROTEIN_PANEL, "All"));
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        yaxis.confirmSelection();

        // Verify the binning message
        waitForText("Heatmap enabled");
        click(Locator.linkWithText("Learn why"));
        waitForText("The color variable is disabled");

        // Verify the binning message layers correctly
        xaxis.openSelectorWindow();
        waitForTextToDisappear("Heatmap enabled");
        xaxis.cancelSelection();

        cds.ensureNoFilter();
    }

    private String getPointProperty(String property, WebElement point)
    {
        String titleAttribute = point.getAttribute("title");
        String[] pointProperties = titleAttribute.split(",\n");
        Map<String, String> propertyMap = new HashMap<>();

        for (String pointProperty : pointProperties)
        {
            String[] splitProperty = pointProperty.split(": ");
            propertyMap.put(splitProperty[0], splitProperty[1]);
        }

        return propertyMap.get(property);
    }

    private void selectXAxes(boolean isShift, String... axes)
    {
            if (axes == null || axes.length == 0)
                throw new IllegalArgumentException("Please specify axes to select.");

            Keys multiSelectKey;
            if (isShift)
                multiSelectKey = Keys.SHIFT;
            else if (SystemUtils.IS_OS_MAC)
                multiSelectKey = Keys.COMMAND;
            else
                multiSelectKey = Keys.CONTROL;

            click(Locators.plotTick.withText(axes[0]));

            if (axes.length > 1)
            {
                Actions builder = new Actions(getDriver());
                builder.keyDown(multiSelectKey).build().perform();

                for (int i = 1; i < axes.length; i++)
                {
                    click(Locators.plotTick.withText(axes[i]));
                }
                builder.keyUp(multiSelectKey).build().perform();
            }
    }

    private int getPointCountByColor(String colorCode)
    {
        List<WebElement> points = Locator.css("svg g a.point path").findElements(getDriver());
        int ret = 0;
        for(WebElement point : points)
        {
            if(point.getAttribute("fill").equals(colorCode))
            {
                ret++;
            }
        }
        return ret;
    }

    private int getPointCount()
    {
        return Locator.css("svg g a.point path").findElements(getDriver()).size();
    }

    private void waitForPointCount(int count, int msTimeout)
    {
        final Integer pointCount = count;
        long secTimeout = msTimeout / 1000;
        secTimeout = secTimeout > 0 ? secTimeout : 1;
        WebDriverWait wait = new WebDriverWait(getDriver(), secTimeout);
        try
        {
            wait.until(new ExpectedCondition<Boolean>()
            {
                @Override
                public Boolean apply(WebDriver d)
                {
                    return pointCount.equals(getPointCount());
                }
            });
        }
        catch (TimeoutException ex)
        {
            fail("Timeout waiting for point count [" + secTimeout + "sec]: " + count);
        }
    }

    private boolean hasYGutter()
    {
        return hasGutter("svg g text.yGutter-label");
    }

    private boolean hasXGutter()
    {
        return hasGutter("svg g text.xGutter-label");
    }

    private boolean hasStudyAxis()
    {
        return hasGutter("#study-axis svg");
    }

    private boolean hasGutter(String cssPath){

        boolean hasElement;

        try
        {
            waitForElement(Locator.css(cssPath));
            if (Locator.css(cssPath).findElement(getDriver()).isDisplayed())
            {
                hasElement = true;
            }
            else
            {
                hasElement = false;
            }
        }
        catch(org.openqa.selenium.NoSuchElementException ex){
            hasElement = false;
        }

        return hasElement;

    }

    private void validateVisitCounts(List<WebElement> studies, Map<String, CDSHelper.TimeAxisData> expectedCounts)
    {

        for(WebElement study : studies)
        {
            List<WebElement> visits;

            log("study.getText(): " + study.getText());
            visits = study.findElements(Locator.css("image.visit-tag").toBy());
            log("visits.size(): " + visits.size());

            int nonvacCount = 0, vacCount = 0, chalCount = 0;

            // Had hoped to get a collection directly, but had trouble getting css to see the href value.
            // So went with this approach for now. May revisit later.
            for(int i=0; i < visits.size(); i++)
            {
                if(visits.get(i).getAttribute("href").contains("/nonvaccination_normal.svg"))
                {
                    nonvacCount++;
                }
                if(visits.get(i).getAttribute("href").contains("/vaccination_normal.svg"))
                {
                    vacCount++;
                }
                if(visits.get(i).getAttribute("href").contains("/challenge_normal.svg"))
                {
                    chalCount++;
                }
            }

            log("nonvacCount: " + nonvacCount);
            log("vacCount: " + vacCount);
            log("chalCount: " + chalCount);

            CDSHelper.TimeAxisData tad = expectedCounts.get(study.getText());

            assertTrue("Vaccination count not as expected. Expected: " + tad.vaccinationCount + " found: " + vacCount, tad.vaccinationCount == vacCount);
            assertTrue("Nonvaccination count not as expected. Expected: " + tad.nonvaccinationCount + " found: " + nonvacCount, tad.nonvaccinationCount == nonvacCount);
            assertTrue("Challenge count not as expected. Expected: " + tad.challengeCount + " found: " + chalCount, tad.challengeCount == chalCount);
        }
    }

    @LogMethod
    private void createParticipantGroups()
    {
        Ext4Helper.resetCssPrefix();
        beginAt("project/" + getProjectName() + "/begin.view?");
        _studyHelper.createCustomParticipantGroup(getProjectName(), getProjectName(), PGROUP1, "Subject", "039-016", "039-014");  // TODO Test data dependent.
        _studyHelper.createCustomParticipantGroup(getProjectName(), getProjectName(), PGROUP2, "Subject", "039-044", "039-042");  // TODO Test data dependent.
        _studyHelper.createCustomParticipantGroup(getProjectName(), getProjectName(), PGROUP3, "Subject", "039-059", "039-060");  // TODO Test data dependent.
        _studyHelper.createCustomParticipantGroup(getProjectName(), getProjectName(), PGROUP3_COPY, "Subject", "039-059", "039-060");  // TODO Test data dependent.
    }

    @LogMethod
    private void deleteParticipantGroups()
    {
        beginAt("project/" + getProjectName() + "/begin.view?");
        _studyHelper.deleteCustomParticipantGroup(PGROUP1, "Subject");
        _studyHelper.deleteCustomParticipantGroup(PGROUP2, "Subject");
        _studyHelper.deleteCustomParticipantGroup(PGROUP3, "Subject");
        _studyHelper.deleteCustomParticipantGroup(PGROUP3_COPY, "Subject");
    }

    public static class Locators
    {
        public static Locator plotSelection = Locator.css(".selectionfilter .plot-selection");
        public static Locator plotSelectionFilter = Locator.css(".activefilter .plot-selection");
        public static Locator plotSelectionCloseBtn = Locator.css("div.plot-selection div.closeitem");
        public static Locator plotBox = Locator.css("svg a.dataspace-box-plot");
        public static Locator plotTick = Locator.css("g.tick-text > g > text");
        public static Locator plotPoint = Locator.css("svg a.point");
        public static Locator filterDataButton = Locator.xpath("//span[text()='Filter']");
        public static Locator removeButton = Locator.xpath("//span[text()='Remove']");
    }
}
