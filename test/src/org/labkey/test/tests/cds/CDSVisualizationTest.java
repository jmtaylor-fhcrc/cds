/*
 * Copyright (c) 2014-2016 LabKey Corporation
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
package org.labkey.test.tests.cds;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.test.Locator;
import org.labkey.test.SortDirection;
import org.labkey.test.categories.CDS;
import org.labkey.test.pages.cds.ColorAxisVariableSelector;
import org.labkey.test.pages.cds.DataspaceVariableSelector;
import org.labkey.test.pages.cds.InfoPane;
import org.labkey.test.pages.cds.XAxisVariableSelector;
import org.labkey.test.pages.cds.YAxisVariableSelector;
import org.labkey.test.util.cds.CDSAsserts;
import org.labkey.test.util.cds.CDSHelper;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.Ext4Helper;
import org.labkey.test.util.LogMethod;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.labkey.test.tests.cds.CDSVisualizationTest.Locators.plotBox;
import static org.labkey.test.tests.cds.CDSVisualizationTest.Locators.plotPoint;
import static org.labkey.test.tests.cds.CDSVisualizationTest.Locators.plotTick;
import static org.labkey.test.tests.cds.CDSVisualizationTest.Locators.plotTickLinear;

@Category({CDS.class})
public class CDSVisualizationTest extends CDSReadOnlyTest
{
    private final CDSHelper cds = new CDSHelper(this);
    private final CDSAsserts _asserts = new CDSAsserts(this);
    private final String PGROUP1 = "visgroup 1";
    private final String PGROUP2 = "visgroup 2";
    private final String PGROUP3 = "visgroup 3";
    private final String PGROUP3_COPY = "copy of visgroup 3";
    private final String XPATH_SUBJECT_COUNT = "//div[contains(@class, 'status-row')]//span[contains(@class, 'hl-status-label')][contains(text(), 'Subject')]/./following-sibling::span[contains(@class, ' hl-status-count ')][not(contains(@class, 'hideit'))]";

    protected static final String MOUSEOVER_FILL = "#41C49F";
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
        //TODO add back (and improve already exists test) when verifySavedGroupPlot is implemented.
//        CDSVisualizationTest cvt = (CDSVisualizationTest)getCurrentTest();
//        cvt.createParticipantGroups();
    }

    @AfterClass
    public static void afterClassCleanUp()
    {
        //TODO add back (and improve already exists test) when verifySavedGroupPlot is implemented.
//        CDSVisualizationTest cvt = (CDSVisualizationTest)getCurrentTest();
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
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.confirmSelection();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ELISPOT);
        xaxis.pickVariable(CDSHelper.ELISPOT_MAGNITUDE_RAW);
        xaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        xaxis.confirmSelection();

        assertFalse("For BAMA Magnitude vs NAB Lab x-axis gutter plot was present it should not have been.", hasXGutter());
        assertTrue("For BAMA Magnitude vs NAB Lab y-axis gutter plot was not present.", hasYGutter());

        click(CDSHelper.Locators.cdsButtonLocator("clear"));

        // Makes the test a little more reliable.
        waitForElement(Locator.xpath("//div[contains(@class, 'noplotmsg')][not(contains(@style, 'display: none'))]"));

        log("Validate that a x-axis gutter plot is generated.");
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.NAB);
        yaxis.pickVariable(CDSHelper.NAB_TITERIC80);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.confirmSelection();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_RAW);
        xaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        xaxis.confirmSelection();

        assertTrue("For NAB IC80 vs ICS Magnitude x-axis gutter plot was not present.", hasXGutter());
        assertFalse("For NAB IC80 vs ICS Magnitude y-axis gutter plot was present and it should not have been.", hasYGutter());

        click(CDSHelper.Locators.cdsButtonLocator("clear"));

        // Makes the test a little more reliable.
        waitForElement(Locator.xpath("//div[contains(@class, 'noplotmsg')][not(contains(@style, 'display: none'))]"));

        log("Validate that a gutter plot is generated for both the x and y axis.");
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_RAW);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.setCellType(CDSHelper.CELL_TYPE_CD4);
        yaxis.confirmSelection();

        // Put this in here to work around issue with FireFox scrollIntoView behaving differently than Chrome.
        refresh();
        _ext4Helper.waitForMaskToDisappear();

        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_RAW);
        xaxis.setScale(DataspaceVariableSelector.Scale.Linear);
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
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.confirmSelection();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.TIME_POINTS);
        xaxis.pickVariable(CDSHelper.TIME_POINTS_DAYS);
        xaxis.confirmSelection();

        assertTrue("For ELISPOT Background vs Time Visit Days a study axis was not present.", hasStudyAxis());
        assertFalse("For ELISPOT Background vs Time Visit Days x-axis gutter plot was present, it should not be.", hasXGutter());
        assertFalse("For ELISPOT Background vs Time Visit Days y-axis gutter plot was present, it should not be.", hasYGutter());

        click(CDSHelper.Locators.cdsButtonLocator("clear"));
    }

    @Test
    public void verifyLogGutterPlot()
    {

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);
        ColorAxisVariableSelector coloraxis = new ColorAxisVariableSelector(this);

        CDSHelper cds = new CDSHelper(this);
        String tempStr, expectedTickText;
        int subjectCountBefore, subjectCountAfter;

        log("Generate a plot that has all the gutters.");
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        yaxis.setCellType(CDSHelper.CELL_TYPE_CD4);
        yaxis.confirmSelection();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        xaxis.setCellType(CDSHelper.CELL_TYPE_CD8);
        xaxis.confirmSelection();

        log("Validate that the Log Gutters are there.");
        assertTrue("Did not find the Log Gutter on the bottom of the plot.", hasXLogGutter());
        assertTrue("Did not find the Log Gutter on the left hand side of the plot.", hasYLogGutter());

        tempStr = getText(Locator.xpath(XPATH_SUBJECT_COUNT));
        subjectCountBefore = Integer.parseInt(tempStr.replaceAll(",", ""));

        log("Brush only in the log gutter on the x-axis.");
        cds.dragAndDropFromElement(Locator.css("div:not(.thumbnail) > svg:nth-of-type(2) > g:nth-of-type(3) > g:nth-of-type(1)"), 100, 100);
        waitAndClick(CDSHelper.Locators.cdsButtonLocator("Filter"));
        sleep(1000); // Let the plot redraw.
        _ext4Helper.waitForMaskToDisappear();

        log("Validate that the filter has been applied and there are only x gutters.");
        tempStr = getText(Locator.xpath(XPATH_SUBJECT_COUNT));
        subjectCountAfter = Integer.parseInt(tempStr.replaceAll(",", ""));

        assertTrue("After brushing subject count was not less than before. Count before brushing: " + subjectCountBefore + " Count after brush: " + subjectCountAfter, subjectCountBefore > subjectCountAfter);
        assertTrue("The y-axis gutter plot did not go away, it should have.", !hasYGutter());
        assertTrue("The y-axis log gutter did not go away, it should have.", !hasYLogGutter());
        assertTrue("There is no x-axis gutter, there should be.", hasXGutter());
        assertTrue("There is no x-axis log gutter, there should be.", hasXLogGutter());
        // Removed the check of the plot tick text. Because these test do brushing there is too much randomness to guarantee that the text will alwyas be the same.

        cds.clearFilter(1);
        sleep(1000); // Let the plot redraw.
        _ext4Helper.waitForMaskToDisappear();

        log("Now brush only in the log gutter on the y-axis.");
        cds.dragAndDropFromElement(Locator.css("div:not(.thumbnail) > svg:nth-of-type(2) > g:nth-of-type(4) > g:nth-of-type(1)"), -100, 100);
        waitAndClick(CDSHelper.Locators.cdsButtonLocator("Filter"));
        sleep(1000); // Let the plot redraw.
        _ext4Helper.waitForMaskToDisappear();

        log("Validate that the filter has been applied and there are only y gutters.");
        tempStr = getText(Locator.xpath(XPATH_SUBJECT_COUNT));
        subjectCountAfter = Integer.parseInt(tempStr.replaceAll(",", ""));

        assertTrue("After brushing subject count was not less than before. Count before brushing: " + subjectCountBefore + " Count after brush: " + subjectCountAfter, subjectCountBefore > subjectCountAfter);
        assertTrue("The x-axis gutter plot did not go away, it should have.", !hasXGutter());
        assertTrue("The x-axis log gutter did not go away, it should have.", !hasXLogGutter());
        assertTrue("There is no y-axis gutter, there should be.", hasYGutter());
        assertTrue("There is no y-axis log gutter, there should be.", hasYLogGutter());

        cds.clearFilter(1);
        sleep(1000); // Let the plot redraw.
        _ext4Helper.waitForMaskToDisappear();

        log("Set a color filter and make sure that there are no errors.");
        coloraxis.openSelectorWindow();
        coloraxis.pickSource(CDSHelper.SUBJECT_CHARS);
        coloraxis.pickVariable(CDSHelper.DEMO_COUNTRY);
        coloraxis.confirmSelection();
        sleep(1000);
        _ext4Helper.waitForMaskToDisappear();

        log("Brush just the main plot and validate that all of the gutters disappear.");
        cds.dragAndDropFromElement(Locator.css("div:not(.thumbnail) > svg:nth-of-type(2)"), 100, 100);
        waitAndClick(CDSHelper.Locators.cdsButtonLocator("Filter"));
        sleep(1000); // Let the plot redraw.
        _ext4Helper.waitForMaskToDisappear();

        log("Validate that the filter has been applied and there are no gutter elements.");
        tempStr = getText(Locator.xpath(XPATH_SUBJECT_COUNT));
        subjectCountAfter = Integer.parseInt(tempStr.replaceAll(",", ""));

        assertTrue("After brushing subject count was not less than before. Count before brushing: " + subjectCountBefore + " Count after brush: " + subjectCountAfter, subjectCountBefore > subjectCountAfter);
        assertTrue("The y-axis gutter plot did not go away, it should have.", !hasYGutter());
        assertTrue("The y-axis log gutter did not go away, it should have.", !hasYLogGutter());
        assertTrue("The x-axis gutter plot did not go away, it should have.", !hasXGutter());
        assertTrue("The x-axis log gutter did not go away, it should have.", !hasXLogGutter());

        cds.clearFilter(1);
        sleep(1000); // Let the plot redraw.
        _ext4Helper.waitForMaskToDisappear();

    }

    @Test
    public void verifyScatterPlot()
    {
        //getText(Locator.css("svg")) on Chrome

        final String ELISPOT_DATA_PROV = "0\n500\n1000\n1500\n2000\n2500\n3000\n3500\n0\n5000\n10000\n15000\n20000\n25000\n30000\n35000\n40000\n45000";
        final String ICS_MAGNITUDE = "0\n1\n2\n3\n4\n5\n0\n0.5\n1\n1.5\n2\n2.5\n3\n3.5\n4\n4.5\n5";
        final String NAB_IC50 = "1\n10\n1\n10\n100\n1000";

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ELISPOT);
        xaxis.pickVariable(CDSHelper.ELISPOT_DATA_PROV);
        xaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        xaxis.confirmSelection();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        yaxis.pickSource(CDSHelper.ELISPOT);
        yaxis.pickVariable(CDSHelper.ELISPOT_MAGNITUDE_BACKGROUND_SUB);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.confirmSelection();

        if (CDSHelper.validateCounts)
        {
            cds.assertPlotTickText(ELISPOT_DATA_PROV);
        }

        yaxis.openSelectorWindow();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.confirmSelection();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.NAB);
        xaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        xaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        xaxis.confirmSelection();

        assertTrue("For ELISPOT vs ICS x-axis gutter plot was not present.", hasXGutter());
        assertTrue("For ELISPOT vs ICS y-axis gutter plot was not present.", hasYGutter());

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        xaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        xaxis.confirmSelection();

        if (CDSHelper.validateCounts)
        {
            cds.assertPlotTickText(ICS_MAGNITUDE);
        }

        // Test log scales
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.NAB);
        yaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        yaxis.setScale(DataspaceVariableSelector.Scale.Log);
        yaxis.confirmSelection();

        assertTrue("For NAB vs ICS x-axis gutter plot was not present.", hasXGutter());
        assertTrue("For NAB vs ICS y-axis gutter plot was not present.", hasYGutter());

        // Test disabled for now as a result of side effect of log transformation story. will re-enable when
        // filter refinement is done and compound filter is used to drop <=0 data but retain null.
//        xaxis.openSelectorWindow();
//        xaxis.pickSource(CDSHelper.SUBJECT_CHARS);
//        xaxis.pickVariable(CDSHelper.DEMO_AGE);
//        xaxis.setScale(DataspaceVariableSelector.Scale.Log);
//        xaxis.confirmSelection();
//
//        assertTrue("For NAB vs Demographics x-axis gutter plot was not present.", hasXGutter());
//        assertFalse("For NAB vs Demographics y-axis gutter plot was present and it should not be.", hasYGutter());
//
//        if (CDSHelper.validateCounts)
//        {
//            cds.assertPlotTickText(NAB_IC50);
//        }
    }

    @Test
    public void verifyStudyAndTreatmentVars()
    {
        String expectedXYValues;
        int actualTickCount;
        Pattern pattern;
        final String cssXaxisTickText = "div.plot > svg > g.axis > g.tick-text > a > rect.xaxis-tick-rect";

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.setCellType("All");
        yaxis.confirmSelection();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.STUDY_TREATMENT_VARS);
        xaxis.pickVariable(CDSHelper.DEMO_STUDY_NAME);
        xaxis.confirmSelection();
        expectedXYValues = "RED 4\nRED 5\nRED 6\nZAP 102\nZAP 105\nZAP 106\nZAP 113\nZAP 115\nZAP 116\nZAP 117\nZAP 118\nZAP 124\nZAP 134\nZAP 136\n0\n2\n4\n6\n8\n10\n12\n14";

        log("Validating Study Name");
        cds.assertPlotTickText(expectedXYValues);

        xaxis.openSelectorWindow();
        xaxis.pickVariable(CDSHelper.DEMO_TREAT_SUMM);
        xaxis.confirmSelection();
        actualTickCount = Locator.css(cssXaxisTickText).findElements(getDriver()).size();

        log("Validating Treatment Summary");
        assertEquals("Unexpected number of tick marks on the x-axis.", 89, actualTickCount);

        xaxis.openSelectorWindow();
        xaxis.pickVariable(CDSHelper.DEMO_DATE_SUBJ_ENR);
        xaxis.confirmSelection();
        pattern = Pattern.compile(".*02468101214{1}");

        log("Validating Date Subject Enrolled");
        cds.assertPlotTickText(pattern);

        xaxis.openSelectorWindow();
        xaxis.pickVariable(CDSHelper.DEMO_DATE_FUP_COMP);
        xaxis.confirmSelection();

        // Special casing this test. for what ever reason sometimes it will have 3/13/2011 other times it will be 3/12/2011.
        // Because this value appears to be calculated I will use regular expression to validate.
        log("Validating Followup Complete");
//        Pattern p = Pattern.compile("1/10/20088/11/20093/\\d\\d/201110/11/201202468101214");
        pattern = Pattern.compile(".*02468101214{1}");
        cds.assertPlotTickText(pattern);

        xaxis.openSelectorWindow();
        xaxis.pickVariable(CDSHelper.DEMO_DATE_PUB);
        xaxis.confirmSelection();

        // Another special case scenario.
        log("Validating Date Made Public");
        pattern = Pattern.compile("3/\\d\\d/20117/6/201110/30/20112/23/20126/\\d\\d/201210/11/20122/4/20135/31/201302468101214");
        cds.assertPlotTickText(pattern);

        xaxis.openSelectorWindow();
        xaxis.pickVariable(CDSHelper.DEMO_DATE_START);
        xaxis.confirmSelection();

        // Another special case scenario.
        log("Validating Start Date");
        pattern = Pattern.compile("11/9/20046/10/20061/10/20088/11/20093/\\d\\d/201102468101214");
        cds.assertPlotTickText(pattern);

        xaxis.openSelectorWindow();
        xaxis.pickVariable(CDSHelper.DEMO_NETWORK);
        xaxis.confirmSelection();
        expectedXYValues = "ROGER\nZED\n0\n2\n4\n6\n8\n10\n12\n14";

        log("Validating Network");
        cds.assertPlotTickText(expectedXYValues);

        xaxis.openSelectorWindow();
        xaxis.pickVariable(CDSHelper.DEMO_PROD_CLASS);
        xaxis.confirmSelection();

        // There are too many labels on the xaxis to validate all, so we will just validate the count.
        log("Validating Product Class");
        actualTickCount = Locator.css(cssXaxisTickText).findElements(getDriver()).size();
        assertEquals("Unexpected number of tick marks on the x-axis.", 83, actualTickCount);

        xaxis.openSelectorWindow();
        xaxis.pickVariable(CDSHelper.DEMO_PROD_COMB);
        xaxis.confirmSelection();

        // There are too many labels on the xaxis to validate all, so we will just validate the count.
        log("Validating Product Class Combination");
        actualTickCount = Locator.css(cssXaxisTickText).findElements(getDriver()).size();
        assertEquals("Unexpected number of tick marks on the x-axis.", 81, actualTickCount);

        xaxis.openSelectorWindow();
        xaxis.pickVariable(CDSHelper.DEMO_STUDY_TYPE);
        xaxis.confirmSelection();
        expectedXYValues = "Phase I\nPhase II\nPhase IIB\nundefined\n0\n2\n4\n6\n8\n10\n12\n14";

        log("Validating Study Type");
        cds.assertPlotTickText(expectedXYValues);

        xaxis.openSelectorWindow();
        xaxis.pickVariable(CDSHelper.DEMO_TREAT_ARM);
        xaxis.confirmSelection();

        // There are too many labels on the xaxis to validate all, so we will just validate the count.
        log("Validating Treatment Arm");
        actualTickCount = Locator.css(cssXaxisTickText).findElements(getDriver()).size();
        assertEquals("Unexpected number of tick marks on the x-axis." + actualTickCount, 28, actualTickCount);


        xaxis.openSelectorWindow();
        xaxis.pickVariable(CDSHelper.DEMO_TREAT_CODED);
        xaxis.confirmSelection();

        log("Validating Treatment Arm Coded Label");
        actualTickCount = Locator.css(cssXaxisTickText).findElements(getDriver()).size();
        assertEquals("Unexpected number of tick marks on the x-axis." + actualTickCount, 87, actualTickCount);

        xaxis.openSelectorWindow();
        xaxis.pickVariable(CDSHelper.DEMO_VACC_PLAC);
        xaxis.confirmSelection();
        expectedXYValues = "Placebo\nVaccine\nundefined\n0\n2\n4\n6\n8\n10\n12\n14";

        log("Validating Vaccine or Placebo");
        cds.assertPlotTickText(expectedXYValues);

    }

    @Test
    public void verifyColorStudyAndTreatmentVars()
    {
        int actualTickCount;
        String cssColorLegend = "#colorvarselector-innerCt  svg > path.legend-point";

        sleep(1500);
        _ext4Helper.waitForMaskToDisappear(60000);

        addRaceFilter(CDSHelper.RACE_BLACK);
        _asserts.assertFilterStatusCounts(829, 48, 1, 1, 154);

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.setCellType("All");
        yaxis.confirmSelection();

        ColorAxisVariableSelector coloraxis = new ColorAxisVariableSelector(this);
        coloraxis.openSelectorWindow();
        coloraxis.pickSource(CDSHelper.STUDY_TREATMENT_VARS);
        coloraxis.pickVariable(CDSHelper.DEMO_STUDY_NAME);
        coloraxis.confirmSelection();

        actualTickCount = Locator.css(cssColorLegend).findElements(getDriver()).size();

        assertEquals("Unexpected number of Study Names in the color axis.", 12, actualTickCount);

        coloraxis.openSelectorWindow();
        coloraxis.pickVariable(CDSHelper.DEMO_TREAT_SUMM);
        coloraxis.confirmSelection();

        actualTickCount = Locator.css(cssColorLegend).findElements(getDriver()).size();

        assertEquals("Unexpected number of Treatment Summaries in the color axis.", 43, actualTickCount);

        coloraxis.openSelectorWindow();
        coloraxis.pickVariable(CDSHelper.DEMO_NETWORK);
        coloraxis.confirmSelection();

        actualTickCount = Locator.css(cssColorLegend).findElements(getDriver()).size();

        assertEquals("Unexpected number of Networks in the color axis.", 2, actualTickCount);

        coloraxis.openSelectorWindow();
        coloraxis.pickVariable(CDSHelper.DEMO_PROD_COMB);
        coloraxis.confirmSelection();

        actualTickCount = Locator.css(cssColorLegend).findElements(getDriver()).size();

        assertEquals("Unexpected number of Product Class Combinations in the color axis.", 40, actualTickCount);

        coloraxis.openSelectorWindow();
        coloraxis.pickVariable(CDSHelper.DEMO_PROD_CLASS);
        coloraxis.confirmSelection();

        actualTickCount = Locator.css(cssColorLegend).findElements(getDriver()).size();

        assertEquals("Unexpected number of Product Classes in the color axis.", 41, actualTickCount);

        coloraxis.openSelectorWindow();
        coloraxis.pickVariable(CDSHelper.DEMO_STUDY_TYPE);
        coloraxis.confirmSelection();

        actualTickCount = Locator.css(cssColorLegend).findElements(getDriver()).size();

        assertEquals("Unexpected number of Study Types in the color axis.", 3, actualTickCount);

        coloraxis.openSelectorWindow();
        coloraxis.pickVariable(CDSHelper.DEMO_TREAT_ARM);
        coloraxis.confirmSelection();

        actualTickCount = Locator.css(cssColorLegend).findElements(getDriver()).size();

        assertEquals("Unexpected number of Treatment Arms in the color axis.", 17, actualTickCount);

        coloraxis.openSelectorWindow();
        coloraxis.pickVariable(CDSHelper.DEMO_TREAT_CODED);
        coloraxis.confirmSelection();

        actualTickCount = Locator.css(cssColorLegend).findElements(getDriver()).size();

        assertEquals("Unexpected number of Treatment Arm Coded Labels in the color axis.", 43, actualTickCount);

        coloraxis.openSelectorWindow();
        coloraxis.pickVariable(CDSHelper.DEMO_VACC_PLAC);
        coloraxis.confirmSelection();

        actualTickCount = Locator.css(cssColorLegend).findElements(getDriver()).size();

        assertEquals("Unexpected number of Vaccinne or Placebos in the color axis.", 3, actualTickCount);

    }

    @Test
    public void verifyBoxPlots()
    {
        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        log("Choose the y-axis and verify that only 1 box plot shows if there is no x-axis chosen.");
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.confirmSelection();

        waitForElement(plotBox);

        assertElementPresent(plotBox, 1);
        assertElementPresent(plotPoint, 3627);

        log("Choose a categorical axis to verify that multiple box plots will appear.");
        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.SUBJECT_CHARS);
        xaxis.pickVariable(CDSHelper.DEMO_SEX);
        xaxis.confirmSelection();

        waitForElement(Locators.plotTick.withText("Female"), 20000);

        waitForElement(Locators.plotBox);

        assertElementPresent(plotBox, 2);
        assertElementPresent(plotPoint, 3627);

        log("Choose a continuous axis and verify that the chart goes back to being a scatter plot.");
        xaxis.openSelectorWindow();
        xaxis.backToSource();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND);
        xaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        xaxis.confirmSelection();

        waitForElementToDisappear(plotBox);

        log("Verify that we can go back to boxes after being in scatter mode.");
        xaxis.openSelectorWindow();
        xaxis.backToSource();
        xaxis.pickSource(CDSHelper.SUBJECT_CHARS);
        xaxis.pickVariable(CDSHelper.DEMO_RACE);
        xaxis.confirmSelection();

        waitForElement(Locators.plotBox);
        waitForElement(Locators.plotTick.withText("Asian"), 20000);

        assertElementPresent(plotBox, 10);
        assertElementPresent(plotPoint, 3627);

        log("Verify x axis categories are selectable as filters");
        mouseOver(Locators.plotTick.withText("Asian"));
        waitForElement(Locator.css("svg g.axis g.tick-text a rect.highlight[fill='" + MOUSEOVER_FILL + "']"));
        assertEquals("Incorrect number of points highlighted after mousing over x axis category", 316, getPointCountByColor(MOUSEOVER_FILL));

        click(Locators.plotTick.withText("Asian"));
        waitForElement(Locators.filterDataButton);
        assertElementPresent(Locators.removeButton);

        log("Ensure correct number of points are highlighted");
        assertEquals("Incorrect number of points highlighted after clicking x axis category", 316, getPointCountByColor(MOUSEOVER_FILL));
        log("Ensure correct total number of points.");
        assertEquals("Incorrect total number of points after clicking x axis category", 3627, getPointCount());
        log("Apply category selection as a filter.");

        // Need to do this because there is more than one "Filter" buton in the OM, but only want the visible one.
        waitAndClick(CDSHelper.Locators.cdsButtonLocator("Filter"));
        sleep(3000); // Let the plot redraw.
        _ext4Helper.waitForMaskToDisappear();

        assertEquals("Point counts not as expected.", 316, getPointCount());

        log("Clear filter.");
        click(CDSHelper.Locators.cdsButtonLocator("clear"));

        // Makes the test a little more reliable.
        waitForElement(Locator.xpath("//div[contains(@class, 'noplotmsg')][not(contains(@style, 'display: none'))]"));

        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.confirmSelection();
        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.SUBJECT_CHARS);
        xaxis.pickVariable(CDSHelper.DEMO_RACE);
        xaxis.confirmSelection();

        assertEquals("Point counts not as expected", 3627, getPointCount());

        log("Verify multi-select of categories.");
        selectXAxes(false, "White", "Multiracial", "Native Hawaiian/Paci", "Native American/Alas. Other");
        sleep(3000); // Let the animation end.

        log("Ensure correct number of points are highlighted.");
        assertEquals("Incorrect number of points highlighted after clicking x axis categories",1443, getPointCountByColor(MOUSEOVER_FILL));
        assertEquals("Incorrect total number of points after clicking x axis categories",3627, getPointCount());
        log("Apply selection as exclusive filter.");
        waitAndClick(CDSHelper.Locators.cdsButtonLocator("Remove"));
        sleep(3000); // Let the plot redraw.
        _ext4Helper.waitForMaskToDisappear();
        assertEquals("Point counts not as expected", (3627 - 1443), getPointCount());

        click(CDSHelper.Locators.cdsButtonLocator("clear"));

        log("Validate bug 24806, that null values in a box plot should not generate a gutter plot.");
        waitForElement(Locator.xpath("//div[contains(@class, 'noplotmsg')][not(contains(@style, 'display: none'))]"));

        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.NAB);
        yaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        yaxis.setScale(DataspaceVariableSelector.Scale.Log);
        yaxis.confirmSelection();
        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.NAB);
        xaxis.pickVariable(CDSHelper.NAB_INIT_DILUTION);
        xaxis.confirmSelection();

        String expectedXYValues = "10\nnull\n3\n30\n300\n3000";
        cds.assertPlotTickText(expectedXYValues);
        assertFalse("There is an x-gutter, and there should not be.", hasXGutter());

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
                {
                        {CDSHelper.SUBJECT_CHARS, CDSHelper.DEMO_AGE, CDSHelper.DEMO_BMI},
                        {CDSHelper.BAMA, CDSHelper.BAMA_MAGNITUDE_DELTA, CDSHelper.BAMA_MAGNITUDE_BLANK, CDSHelper.BAMA_MAGNITUDE_BASELINE, CDSHelper.BAMA_MAGNITUDE_DELTA_BASELINE, CDSHelper.BAMA_MAGNITUDE_RAW, CDSHelper.BAMA_MAGNITUDE_RAW_BASELINE},
                        {CDSHelper.ELISPOT, CDSHelper.ELISPOT_MAGNITUDE_BACKGROUND_SUB, CDSHelper.ELISPOT_MAGNITUDE_BACKGROUND, CDSHelper.ELISPOT_MAGNITUDE_RAW},
                        {CDSHelper.ICS, CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB, CDSHelper.ICS_MAGNITUDE_BACKGROUND, CDSHelper.ICS_MAGNITUDE_RAW},
                        {CDSHelper.NAB, CDSHelper.NAB_TITERIC50, CDSHelper.NAB_TITERIC80}
                };
        final String[][] X_AXIS_SOURCES =
                {
                        {CDSHelper.STUDY_TREATMENT_VARS, CDSHelper.DEMO_STUDY_NAME, CDSHelper.DEMO_TREAT_SUMM, CDSHelper.DEMO_DATE_SUBJ_ENR, CDSHelper.DEMO_DATE_FUP_COMP, CDSHelper.DEMO_DATE_PUB, CDSHelper.DEMO_DATE_START, CDSHelper.DEMO_NETWORK, CDSHelper.DEMO_PROD_CLASS, CDSHelper.DEMO_PROD_COMB, CDSHelper.DEMO_STUDY_TYPE, CDSHelper.DEMO_TREAT_ARM, CDSHelper.DEMO_TREAT_CODED, CDSHelper.DEMO_VACC_PLAC},
                        {CDSHelper.SUBJECT_CHARS, CDSHelper.DEMO_AGE, CDSHelper.DEMO_SEX, CDSHelper.DEMO_SPECIES, CDSHelper.DEMO_AGEGROUP, CDSHelper.DEMO_BMI, CDSHelper.DEMO_CIRCUMCISED, CDSHelper.DEMO_COUNTRY, CDSHelper.DEMO_HISPANIC, CDSHelper.DEMO_RACE, CDSHelper.DEMO_SUBSPECIES},
                        {CDSHelper.TIME_POINTS, CDSHelper.TIME_POINTS_DAYS, CDSHelper.TIME_POINTS_WEEKS, CDSHelper.TIME_POINTS_MONTHS},
                        {CDSHelper.BAMA, CDSHelper.BAMA_MAGNITUDE_DELTA, CDSHelper.BAMA_RESPONSE_CALL, CDSHelper.BAMA_ANTIGEN_CLADE, CDSHelper.BAMA_ANTIGEN_NAME, CDSHelper.BAMA_ANTIGEN_TYPE, CDSHelper.BAMA_ASSAY, CDSHelper.BAMA_DETECTION, CDSHelper.BAMA_DILUTION, CDSHelper.BAMA_EXP_ASSAYD, CDSHelper.BAMA_INSTRUMENT_CODE, CDSHelper.BAMA_ISOTYPE, CDSHelper.BAMA_LAB, CDSHelper.BAMA_MAGNITUDE_BLANK, CDSHelper.BAMA_MAGNITUDE_BASELINE, CDSHelper.BAMA_MAGNITUDE_RAW, CDSHelper.BAMA_MAGNITUDE_DELTA_BASELINE, CDSHelper.BAMA_MAGNITUDE_RAW_BASELINE, CDSHelper.BAMA_PROTEIN, CDSHelper.BAMA_PROTEIN_PANEL, CDSHelper.BAMA_SPECIMEN, CDSHelper.BAMA_VACCINE},
                        {CDSHelper.ELISPOT, CDSHelper.ELISPOT_MAGNITUDE_BACKGROUND_SUB, CDSHelper.ELISPOT_RESPONSE, CDSHelper.ELISPOT_ANTIGEN, CDSHelper.ELISPOT_ASSAY, CDSHelper.ELISPOT_CELL_NAME, CDSHelper.ELISPOT_CELL_TYPE, CDSHelper.ELISPOT_EXP_ASSAY, CDSHelper.ELISPOT_MARKER_NAME, CDSHelper.ELISPOT_MARKER_TYPE, CDSHelper.ELISPOT_LAB, CDSHelper.ELISPOT_MAGNITUDE_BACKGROUND, CDSHelper.ELISPOT_MAGNITUDE_RAW, CDSHelper.ELISPOT_PROTEIN, CDSHelper.ELISPOT_PROTEIN_PANEL, CDSHelper.ELISPOT_SPECIMEN, CDSHelper.ELISPOT_VACCINE},
                        {CDSHelper.ICS, CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB, CDSHelper.ICS_RESPONSE, CDSHelper.ICS_ANTIGEN, CDSHelper.ICS_ASSAY, CDSHelper.ICS_CELL_NAME, CDSHelper.ICS_CELL_TYPE, CDSHelper.ICS_EXP_ASSAY, CDSHelper.ICS_MARKER_NAME, CDSHelper.ICS_MARKER_TYPE, CDSHelper.ICS_LAB, CDSHelper.ICS_MAGNITUDE_BACKGROUND, CDSHelper.ICS_MAGNITUDE_RAW, CDSHelper.ICS_PROTEIN, CDSHelper.ICS_SPECIMEN},
                        {CDSHelper.NAB, CDSHelper.NAB_RESPONSE, CDSHelper.NAB_TITERIC50, CDSHelper.NAB_ANTIGEN, CDSHelper.NAB_ANTIGEN_CLADE, CDSHelper.NAB_EXP_ASSAY, CDSHelper.NAB_INIT_DILUTION, CDSHelper.NAB_LAB, CDSHelper.NAB_SPECIMEN, CDSHelper.NAB_TARGET_CELL, CDSHelper.NAB_TITERIC80}
                };
        final String[][] COLOR_AXIS_SOURCES =
                {
                        {CDSHelper.STUDY_TREATMENT_VARS, CDSHelper.DEMO_STUDY_NAME, CDSHelper.DEMO_TREAT_SUMM, CDSHelper.DEMO_NETWORK, CDSHelper.DEMO_PROD_CLASS, CDSHelper.DEMO_PROD_COMB, CDSHelper.DEMO_STUDY_TYPE, CDSHelper.DEMO_TREAT_ARM, CDSHelper.DEMO_TREAT_CODED, CDSHelper.DEMO_VACC_PLAC},
                        {CDSHelper.SUBJECT_CHARS, CDSHelper.DEMO_CIRCUMCISED, CDSHelper.DEMO_COUNTRY, CDSHelper.DEMO_HISPANIC, CDSHelper.DEMO_RACE, CDSHelper.DEMO_SEX, CDSHelper.DEMO_SPECIES, CDSHelper.DEMO_SUBSPECIES},
                        {CDSHelper.BAMA, CDSHelper.BAMA_ANTIGEN_CLADE, CDSHelper.BAMA_ANTIGEN_NAME, CDSHelper.BAMA_ANTIGEN_TYPE, CDSHelper.BAMA_ASSAY, CDSHelper.BAMA_DETECTION, CDSHelper.BAMA_INSTRUMENT_CODE, CDSHelper.BAMA_ISOTYPE, CDSHelper.BAMA_LAB, CDSHelper.BAMA_PROTEIN, CDSHelper.BAMA_PROTEIN_PANEL, CDSHelper.BAMA_RESPONSE_CALL, CDSHelper.BAMA_SPECIMEN, CDSHelper.BAMA_VACCINE},
                        {CDSHelper.ELISPOT, CDSHelper.ELISPOT_ANTIGEN, CDSHelper.ELISPOT_ASSAY, CDSHelper.ELISPOT_CELL_NAME, CDSHelper.ELISPOT_CELL_TYPE, CDSHelper.ELISPOT_CLADE, CDSHelper.ELISPOT_MARKER_NAME, CDSHelper.ELISPOT_MARKER_TYPE, CDSHelper.ELISPOT_LAB, CDSHelper.ELISPOT_PROTEIN, CDSHelper.ELISPOT_PROTEIN_PANEL, CDSHelper.ELISPOT_RESPONSE, CDSHelper.ELISPOT_SPECIMEN, CDSHelper.ELISPOT_VACCINE},
                        {CDSHelper.ICS, CDSHelper.ICS_ANTIGEN, CDSHelper.ICS_ASSAY, CDSHelper.ICS_CELL_NAME, CDSHelper.ICS_CELL_TYPE, CDSHelper.ICS_MARKER_NAME, CDSHelper.ICS_MARKER_TYPE, CDSHelper.ICS_LAB, CDSHelper.ICS_PROTEIN, CDSHelper.ICS_RESPONSE, CDSHelper.ICS_SPECIMEN},
                        {CDSHelper.NAB, CDSHelper.NAB_ANTIGEN, CDSHelper.NAB_ANTIGEN_CLADE, CDSHelper.NAB_ASSAY, CDSHelper.NAB_LAB, CDSHelper.NAB_RESPONSE, CDSHelper.NAB_SPECIMEN, CDSHelper.NAB_TARGET_CELL}
                };

        final Map<String, String> SubjectCounts = new HashMap<>();
        SubjectCounts.put(CDSHelper.STUDY_TREATMENT_VARS, "8,277");
        SubjectCounts.put(CDSHelper.SUBJECT_CHARS, "8,277");
        SubjectCounts.put(CDSHelper.TIME_POINTS, "8,277");
        SubjectCounts.put(CDSHelper.BAMA, "75");
        SubjectCounts.put(CDSHelper.ELISPOT, "477");
        SubjectCounts.put(CDSHelper.ICS, "1,604");
        SubjectCounts.put(CDSHelper.NAB, "839");

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        this.log("Validating the x-axis selector.");
        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        xaxis.openSelectorWindow();

        log("Validating the x-axis header text.");
        assertTrue(isElementVisible(Locator.xpath("//div[contains(@class, 'x-axis-selector')]//div[contains(@class, 'main-title')][text()='x-axis']")));
        assertTrue(isElementVisible(Locator.xpath("//div[contains(@class, 'x-axis-selector')]//div[contains(@class, 'sub-title')]//span[contains(@class, 'nav-text')][text()='Sources']")));
        assertTrue(isElementVisible(Locator.xpath("//div[contains(@class, 'x-axis-selector')]//div[contains(@class, 'sub-title')]//span[contains(@class, 'subject-count')][text()='Subject count']")));

        log("Validating the x-axis sources.");
        for (String[] src : X_AXIS_SOURCES)
        {
            assertTrue(isElementVisible(xaxis.window().append(" div.content-label").withText(src[0])));
            assertTrue(isElementVisible(xaxis.window().append(" div.content-count").withText(SubjectCounts.get(src[0])))); // TODO Bad test. It will pass if there is any tag wtih this count. Need to revisit.
            log("Validating variables for " + src[0]);
            click(xaxis.window().append(" div.content-label").withText(src[0]));
            waitForElement(Locator.xpath("//div[contains(@class, 'x-axis-selector')]//span[contains(@class, 'section-title')][text()='" + src[0] + "']"));
            for (int i = 1; i < src.length; i++)
            {
                assertTrue(isElementVisible(xaxis.window().append(" div.content-label").withText(src[i])));
                click(xaxis.window().append(" div.content-label").withText(src[i]));
            }
            xaxis.backToSource();
        }

        log("Validating the x-axis cancel button.");
        xaxis.cancelSelection();

        log("Validating the y-axis selector.");
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);
        yaxis.openSelectorWindow();

        log("Validating the y-axis header text.");
        assertTrue(isElementVisible(Locator.xpath("//div[contains(@class, 'y-axis-selector')]//div[contains(@class, 'main-title')][text()='y-axis']")));
        assertTrue(isElementVisible(Locator.xpath("//div[contains(@class, 'y-axis-selector')]//div[contains(@class, 'sub-title')]//span[contains(@class, 'nav-text')][text()='Sources']")));
        assertTrue(isElementVisible(Locator.xpath("//div[contains(@class, 'y-axis-selector')]//div[contains(@class, 'sub-title')]//span[contains(@class, 'subject-count')][text()='Subject count']")));

        log("Validating the y-axis sources.");
        for (String[] src : Y_AXIS_SOURCES)
        {
            assertTrue(isElementVisible(yaxis.window().append(" div.content-label").withText(src[0])));
            assertTrue(isElementVisible(yaxis.window().append(" div.content-count").withText(SubjectCounts.get(src[0])))); // TODO Bad test. It will pass if there is any tag wtih this count. Need to revisit.
            log("Validating variables for " + src[0]);
            click(yaxis.window().append(" div.content-label").withText(src[0]));
            waitForElement(Locator.xpath("//div[contains(@class, 'y-axis-selector')]//span[contains(@class, 'section-title')][text()='" + src[0] + "']"));
            for (int i = 1; i < src.length; i++)
            {
                assertTrue(isElementVisible(yaxis.window().append(" div.content-label").withText(src[i])));
                click(yaxis.window().append(" div.content-label").withText(src[i]));
            }
            yaxis.backToSource();
        }

        log("Validating the y-axis cancel button.");
        yaxis.cancelSelection();

        log("Validating the color-axis selector.");
        ColorAxisVariableSelector coloraxis = new ColorAxisVariableSelector(this);
        coloraxis.openSelectorWindow();

        log("Validating the color-axis header text.");
        assertTrue(isElementVisible(Locator.xpath("//div[contains(@class, 'color-axis-selector')]//div[contains(@class, 'main-title')][text()='color']")));
        assertTrue(isElementVisible(Locator.xpath("//div[contains(@class, 'color-axis-selector')]//div[contains(@class, 'sub-title')]//span[contains(@class, 'nav-text')][text()='Sources']")));
        assertTrue(isElementVisible(Locator.xpath("//div[contains(@class, 'color-axis-selector')]//div[contains(@class, 'sub-title')]//span[contains(@class, 'subject-count')][text()='Subject count']")));

        log("Validating the color-axis sources.");
        for (String[] src : COLOR_AXIS_SOURCES)
        {
            assertTrue(isElementVisible(coloraxis.window().append(" div.content-label").withText(src[0])));
            assertTrue(isElementVisible(coloraxis.window().append(" div.content-count").withText(SubjectCounts.get(src[0])))); // TODO Bad test. It will pass if there is any tag wtih this count. Need to revisit.
            log("Validating variables for " + src[0]);
            click(coloraxis.window().append(" div.content-label").withText(src[0]));
            waitForElement(Locator.xpath("//div[contains(@class, 'color-axis-selector')]//span[contains(@class, 'section-title')][text()='" + src[0] + "']"));
            for (int i = 1; i < src.length; i++)
            {
                assertTrue(isElementVisible(coloraxis.window().append(" div.content-label").withText(src[i])));
                click(coloraxis.window().append(" div.content-label").withText(src[i]));
            }
            coloraxis.backToSource();
        }

        log("Validating the color-axis cancel button.");
        coloraxis.cancelSelection();

    }

    @Test
    public void verifySubjectCounts()
    {

        Map<String, String> sourcesSubjectCounts = new HashMap<>();
        CDSHelper cds = new CDSHelper(this);
        Map<String, String> antigenCounts = new HashMap<>();
        Map<String, String> peptidePoolCounts = new HashMap<>();
        Map<String, String> proteinCounts = new HashMap<>();
        Map<String, String> proteinPanelCounts = new HashMap<>();
        Map<String, String> virusCounts = new HashMap<>();

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        // Populate expected counts for antigens.
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_A1_NAME), "75");
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_A244_NAME), "75");
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_AE244_NAME), "75");
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_BCON_NAME), "75");
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_C1086_NAME), "75");
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_CCON_NAME), "75");
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_CONS_NAME), "75");
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_GP70_NAME), "75");
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_P24_NAME), "75");

        // Populate expected counts for peptide pools.
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEC, CDSHelper.PROTEIN_ENV, CDSHelper.PEPTIDE_POOL_ENV1PTEC), "156");
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEC, CDSHelper.PROTEIN_ENV, CDSHelper.PEPTIDE_POOL_ENV2PTEC), "156");
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEC, CDSHelper.PROTEIN_ENV, CDSHelper.PEPTIDE_POOL_ENV3PTEC), "154");
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEC, CDSHelper.PROTEIN_GAG, CDSHelper.PEPTIDE_POOL_GAG1PTEC), "168");
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEC, CDSHelper.PROTEIN_GAG, CDSHelper.PEPTIDE_POOL_GAG2PTEC), "167");
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEC, CDSHelper.PROTEIN_NEF, CDSHelper.PEPTIDE_POOL_NEFPTEC), "156");
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEC, CDSHelper.PROTEIN_POL, CDSHelper.PEPTIDE_POOL_POL1PTEC), "168");
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEC, CDSHelper.PROTEIN_POL, CDSHelper.PEPTIDE_POOL_POL2PTEC), "163");
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEC, CDSHelper.PROTEIN_POL, CDSHelper.PEPTIDE_POOL_POL3PTEC), "159");

        // Populate expected counts for proteins.
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEA, CDSHelper.PROTEIN_ENV), "178");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_ENV), "1203");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_GAG), "1060");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_NEF), "739");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_POL), "1061");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_V503, CDSHelper.PROTEIN_GAG), "219");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_V503, CDSHelper.PROTEIN_NEF), "219");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_V503, CDSHelper.PROTEIN_POL), "219");

        // Populate expected counts for protein panels.
        proteinPanelCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEA), "178");
        proteinPanelCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG), "1239");
        proteinPanelCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_V503), "219");

        // Populate expected counts for viruses.
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_A, CDSHelper.VIRUS_Q23), "8");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_BX08), "73");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_MN3), "727");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_SF162), "667");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_SS1196), "60");

        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_2, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_REJO), "60");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_2, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_SC422), "60");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_2, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_TRO), "60");

        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_A, CDSHelper.VIRUS_92RW), "50");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_CRF01, CDSHelper.VIRUS_NP03), "381");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_CRF01, CDSHelper.VIRUS_TH023), "261");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_9020), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_96ZM), "88");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_97ZA), "50");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_BAL26), "634");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_C1080), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_C3347), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_CAAN), "60");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_CH58), "4");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_CH77), "4");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_CM244), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_CE1086), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_CE1176), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_CE2010), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_DU151), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_DU422), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_MW965), "681");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_R2184), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_REJOLUC), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_RHPALUC), "4");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_SC22), "4");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_SIVLUC), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_SIVNL), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_SVA), "67");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_TV1LUC), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_W61D), "120");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_WITO), "0");

        sourcesSubjectCounts.put(CDSHelper.STUDY_TREATMENT_VARS, "8,277");
        sourcesSubjectCounts.put(CDSHelper.SUBJECT_CHARS, "8,277");
        sourcesSubjectCounts.put(CDSHelper.TIME_POINTS, "8,277");
        sourcesSubjectCounts.put(CDSHelper.BAMA, "75");
        sourcesSubjectCounts.put(CDSHelper.ELISPOT, "477");
        sourcesSubjectCounts.put(CDSHelper.ICS, "1,604");
        sourcesSubjectCounts.put(CDSHelper.NAB, "839");

        subjectCountsHelper(sourcesSubjectCounts, antigenCounts, peptidePoolCounts, proteinCounts, proteinPanelCounts, virusCounts);

    }

    @Test
    public void verifySubjectCountsWithFilters()
    {

        Map<String, String> sourcesSubjectCounts = new HashMap<>();
        Map<String, String> antigenCounts = new HashMap<>();
        Map<String, String> peptidePoolCounts = new HashMap<>();
        Map<String, String> proteinCounts = new HashMap<>();
        Map<String, String> proteinPanelCounts = new HashMap<>();
        Map<String, String> virusCounts = new HashMap<>();

        CDSHelper cds = new CDSHelper(this);

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        log("Validating subject count with a filter of BAMA assay.");

        cds.goToSummary();
        cds.clickBy("Assays");
        cds.selectBars(CDSHelper.ASSAYS[0]); // Select BAMA

        // Populate expected counts for some of the antigens.
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_A1_NAME), "75");
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_A244_NAME), "75");
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_BCON_NAME), "75");
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_C1086_NAME), "75");
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_P24_NAME), "75");

        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEC, CDSHelper.PROTEIN_ENV, CDSHelper.PEPTIDE_POOL_ENV1PTEC), "0");
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEC, CDSHelper.PROTEIN_GAG, CDSHelper.PEPTIDE_POOL_GAG1PTEC), "0");
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEC, CDSHelper.PROTEIN_NEF, CDSHelper.PEPTIDE_POOL_NEFPTEC), "0");
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEC, CDSHelper.PROTEIN_POL, CDSHelper.PEPTIDE_POOL_POL1PTEC), "0");

        proteinPanelCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEA), "0");
        proteinPanelCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG), "74");
        proteinPanelCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_V503), "0");

        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEA, CDSHelper.PROTEIN_ENV), "0");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_ENV), "74");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_GAG), "74");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_NEF), "74");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_POL), "74");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_V503, CDSHelper.PROTEIN_GAG), "0");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_V503, CDSHelper.PROTEIN_NEF), "0");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_V503, CDSHelper.PROTEIN_POL), "0");

        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_A, CDSHelper.VIRUS_Q23), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_BX08), "72");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_MN3), "75");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_SF162), "75");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_SS1196), "0");

        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_2, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_TRO), "0");

        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_CRF01, CDSHelper.VIRUS_NP03), "42");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_CRF01, CDSHelper.VIRUS_TH023), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_9020), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_BAL26), "72");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_DU151), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_DU422), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_MW965), "72");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_R2184), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_REJOLUC), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_RHPALUC), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_SC22), "0");

        sourcesSubjectCounts.put(CDSHelper.STUDY_TREATMENT_VARS, "75");
        sourcesSubjectCounts.put(CDSHelper.SUBJECT_CHARS, "75");
        sourcesSubjectCounts.put(CDSHelper.TIME_POINTS, "75");
        sourcesSubjectCounts.put(CDSHelper.BAMA, "75");
        sourcesSubjectCounts.put(CDSHelper.ELISPOT, "0");
        sourcesSubjectCounts.put(CDSHelper.ICS, "75");
        sourcesSubjectCounts.put(CDSHelper.NAB, "75");

        subjectCountsHelper(sourcesSubjectCounts, antigenCounts, peptidePoolCounts, proteinCounts, proteinPanelCounts, virusCounts);

        cds.clearFilters();

        log("Validating subject count with a filter of race-asian.");
        addRaceFilter(CDSHelper.RACE_ASIAN);
        _asserts.assertFilterStatusCounts(815, 49, 1, 3, 164);

        sourcesSubjectCounts.clear();
        antigenCounts.clear();
        peptidePoolCounts.clear();
        proteinCounts.clear();
        proteinPanelCounts.clear();
        virusCounts.clear();

        // Populate expected counts for some of the antigens.
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_A1_NAME), "5");
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_A244_NAME), "5");
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_BCON_NAME), "5");
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_C1086_NAME), "5");
        antigenCounts.put(cds.buildCountIdentifier(CDSHelper.ANTIGEN_P24_NAME), "5");

        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEC, CDSHelper.PROTEIN_ENV, CDSHelper.PEPTIDE_POOL_ENV1PTEC), "20");
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEC, CDSHelper.PROTEIN_GAG, CDSHelper.PEPTIDE_POOL_GAG1PTEC), "23");
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEC, CDSHelper.PROTEIN_NEF, CDSHelper.PEPTIDE_POOL_NEFPTEC), "22");
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEC, CDSHelper.PROTEIN_POL, CDSHelper.PEPTIDE_POOL_POL1PTEC), "23");
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_ENV, CDSHelper.PEPTIDE_POOL_ENV2PTEG), "6");
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_NEF, CDSHelper.PEPTIDE_POOL_NEFPTEG), "6");
        peptidePoolCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_GAGB, CDSHelper.PROTEIN_GAG, CDSHelper.PEPTIDE_POOL_GAGCONB1), "25");

        proteinPanelCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEA), "12");
        proteinPanelCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG), "116");
        proteinPanelCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_V503), "26");

        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEA, CDSHelper.PROTEIN_ENV), "12");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_ENV), "112");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_GAG), "103");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_NEF), "71");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_POL), "104");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_V503, CDSHelper.PROTEIN_GAG), "26");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_V503, CDSHelper.PROTEIN_NEF), "26");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_V503, CDSHelper.PROTEIN_POL), "26");

        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_A, CDSHelper.VIRUS_Q23), "1");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_BX08), "5");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_MN3), "62");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_SF162), "59");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_SS1196), "3");

        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_2, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_TRO), "3");

        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_CRF01, CDSHelper.VIRUS_NP03), "35");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_CRF01, CDSHelper.VIRUS_TH023), "21");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_9020), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_BAL26), "54");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_DU151), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_DU422), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_MW965), "64");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_R2184), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_REJOLUC), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_RHPALUC), "1");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_SC22), "1");

        sourcesSubjectCounts.put(CDSHelper.STUDY_TREATMENT_VARS, "815");
        sourcesSubjectCounts.put(CDSHelper.SUBJECT_CHARS, "815");
        sourcesSubjectCounts.put(CDSHelper.TIME_POINTS, "815");
        sourcesSubjectCounts.put(CDSHelper.BAMA, "5");
        sourcesSubjectCounts.put(CDSHelper.ELISPOT, "55");
        sourcesSubjectCounts.put(CDSHelper.ICS, "159");
        sourcesSubjectCounts.put(CDSHelper.NAB, "76");

        subjectCountsHelper(sourcesSubjectCounts, antigenCounts, peptidePoolCounts, proteinCounts, proteinPanelCounts, virusCounts);

        cds.clearFilters();

    }

    private void addRaceFilter(String barLabel)
    {
        CDSHelper.NavigationLink.SUMMARY.makeNavigationSelection(this);
        cds.clickBy("Subject characteristics");
        cds.applySelection(barLabel);
        cds.useSelectionAsSubjectFilter();
    }

    @Test
    public void verifySubjectCountsWithFiltersAdvancedOptions()
    {

        Map<String, String> proteinCounts = new HashMap<>();
        Map<String, String> proteinPanelCounts = new HashMap<>();
        Map<String, String> virusCounts = new HashMap<>();

        CDSHelper cds = new CDSHelper(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        log("Validating counts with filters of NAB TZM-bl and then cell types of CD4+, CD8+ and both.");

        cds.goToSummary();
        cds.clickBy("Assays");
        cds.selectBars(CDSHelper.ASSAYS[4]); // Select NAb TZM-bl

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        proteinPanelCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEA), "0");
        proteinPanelCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG), "133");
        proteinPanelCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_V503), "0");

        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEA, CDSHelper.PROTEIN_ENV), "0");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_ENV), "133");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_GAG), "133");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_NEF), "88");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_POL), "133");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_V503, CDSHelper.PROTEIN_GAG), "0");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_V503, CDSHelper.PROTEIN_NEF), "0");
        proteinCounts.put(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_V503, CDSHelper.PROTEIN_POL), "0");

        log("Validating the x-axis selector.");
        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        xaxis.setCellType(CDSHelper.CELL_TYPE_CD4);
        xaxis.setDataSummaryLevel(CDSHelper.ICS_PROTEIN_PANEL);
        xaxis.validateProteinPanelSubjectCount(proteinPanelCounts, false);
        xaxis.back();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        xaxis.setDataSummaryLevel(CDSHelper.ICS_PROTEIN);
        xaxis.validateProteinSubjectCount(proteinCounts, false);
        xaxis.back();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        proteinPanelCounts.replace(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG), "137");

        proteinCounts.replace(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_ENV), "137");
        proteinCounts.replace(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_GAG), "137");
        proteinCounts.replace(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_NEF), "92");
        proteinCounts.replace(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_POL), "137");

        xaxis.setCellType(CDSHelper.CELL_TYPE_CD8);
        xaxis.setDataSummaryLevel(CDSHelper.ICS_PROTEIN_PANEL);
        xaxis.validateProteinPanelSubjectCount(proteinPanelCounts, false);
        xaxis.back();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        xaxis.setDataSummaryLevel(CDSHelper.ICS_PROTEIN);
        xaxis.validateProteinSubjectCount(proteinCounts, false);
        xaxis.back();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        xaxis.setCellType(CDSHelper.CELL_TYPE_CD4, CDSHelper.CELL_TYPE_CD8);
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        xaxis.setDataSummaryLevel(CDSHelper.ICS_PROTEIN_PANEL);
        xaxis.validateProteinPanelSubjectCount(proteinPanelCounts, false);
        xaxis.back();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        xaxis.setDataSummaryLevel(CDSHelper.ICS_PROTEIN);
        xaxis.validateProteinSubjectCount(proteinCounts, false);
        xaxis.backToSource();

        xaxis.cancelSelection();

        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        // Counts for the y-axis are the same as x-axis. So no need to repopulate the counts.

        log("Validating the y-axis selector.");

        proteinPanelCounts.replace(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG), "133");

        proteinCounts.replace(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_ENV), "133");
        proteinCounts.replace(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_GAG), "133");
        proteinCounts.replace(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_NEF), "88");
        proteinCounts.replace(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_POL), "133");

        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.setCellType(CDSHelper.CELL_TYPE_CD4);
        yaxis.setDataSummaryLevel(CDSHelper.ICS_PROTEIN_PANEL);
        yaxis.validateProteinPanelSubjectCount(proteinPanelCounts, false);
        yaxis.back();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        yaxis.setDataSummaryLevel(CDSHelper.ICS_PROTEIN);
        yaxis.validateProteinSubjectCount(proteinCounts, false);
        yaxis.back();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        proteinPanelCounts.replace(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG), "137");

        proteinCounts.replace(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_ENV), "137");
        proteinCounts.replace(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_GAG), "137");
        proteinCounts.replace(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_NEF), "92");
        proteinCounts.replace(cds.buildCountIdentifier(CDSHelper.PROTEIN_PANEL_PTEG, CDSHelper.PROTEIN_POL), "137");

        yaxis.setCellType(CDSHelper.CELL_TYPE_CD8);
        yaxis.setDataSummaryLevel(CDSHelper.ICS_PROTEIN_PANEL);
        yaxis.validateProteinPanelSubjectCount(proteinPanelCounts, false);
        yaxis.back();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        yaxis.setDataSummaryLevel(CDSHelper.ICS_PROTEIN);
        yaxis.validateProteinSubjectCount(proteinCounts, false);
        yaxis.back();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        yaxis.setCellType(CDSHelper.CELL_TYPE_CD4, CDSHelper.CELL_TYPE_CD8);
        yaxis.setDataSummaryLevel(CDSHelper.ICS_PROTEIN_PANEL);
        yaxis.validateProteinPanelSubjectCount(proteinPanelCounts, false);
        yaxis.back();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        yaxis.setDataSummaryLevel(CDSHelper.ICS_PROTEIN);
        yaxis.validateProteinSubjectCount(proteinCounts, false);
        yaxis.backToSource();

        yaxis.cancelSelection();

        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        cds.clearFilters();


        log("Validating counts with filters of Race=white and target cell of A3R5 and TZM-bl.");
        addRaceFilter(CDSHelper.RACE_WHITE);
        _asserts.assertFilterStatusCounts(777, 48, 1, 1, 151);

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        virusCounts.clear();
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_A, CDSHelper.VIRUS_Q23), "2");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_BX08), "8");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_MN3), "62");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_SF162), "58");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_SS1196), "5");

        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_2, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_TRO), "5");

        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_CRF01, CDSHelper.VIRUS_NP03), "28");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_CRF01, CDSHelper.VIRUS_TH023), "18");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_9020), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_BAL26), "55");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_DU151), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_DU422), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_MW965), "53");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_R2184), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_REJOLUC), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_SVA), "5");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_W61D), "9");

        log("Validating the x-axis selector.");
        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.NAB);
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        xaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        xaxis.setTargetCell(CDSHelper.TARGET_CELL_TZM);
        xaxis.validateVirusSubjectCount(virusCounts, false);
        xaxis.back();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        // Counts change when moving to A3R5.

        virusCounts.clear();
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_A, CDSHelper.VIRUS_Q23), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_BX08), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_MN3), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_SF162), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_SS1196), "0");

        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_2, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_TRO), "0");

        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_CRF01, CDSHelper.VIRUS_NP03), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_CRF01, CDSHelper.VIRUS_TH023), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_9020), "9");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_BAL26), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_CH77), "21");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_DU151), "12");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_DU422), "9");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_R2184), "3");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_REJOLUC), "4");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_RHPALUC), "21");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_SC22), "21");

        xaxis.setTargetCell(CDSHelper.TARGET_CELL_A3R5);
        xaxis.validateVirusSubjectCount(virusCounts, false);
        xaxis.back();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        xaxis.cancelSelection();

        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        // Validating for the y-axis re-populate the counts like we did before.

        virusCounts.clear();
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_A, CDSHelper.VIRUS_Q23), "2");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_BX08), "8");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_MN3), "62");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_SF162), "58");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_SS1196), "5");

        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_2, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_TRO), "5");

        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_CRF01, CDSHelper.VIRUS_NP03), "28");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_CRF01, CDSHelper.VIRUS_TH023), "18");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_9020), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_BAL26), "55");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_DU151), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_DU422), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_MW965), "53");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_R2184), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_REJOLUC), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_SVA), "5");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_W61D), "9");

        log("Validating the y-axis selector.");
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.NAB);
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        yaxis.setTargetCell(CDSHelper.TARGET_CELL_TZM);
        yaxis.validateVirusSubjectCount(virusCounts, false);
        yaxis.back();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        virusCounts.clear();
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_A, CDSHelper.VIRUS_Q23), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_BX08), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_MN3), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_SF162), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_SS1196), "0");

        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_2, CDSHelper.ANTIGEN_CLADE_B, CDSHelper.VIRUS_TRO), "0");

        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_CRF01, CDSHelper.VIRUS_NP03), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_CRF01, CDSHelper.VIRUS_TH023), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_9020), "9");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_BAL26), "0");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_CH77), "21");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_DU151), "12");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_DU422), "9");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_R2184), "3");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_REJOLUC), "4");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_RHPALUC), "21");
        virusCounts.put(cds.buildCountIdentifier(CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, CDSHelper.VIRUS_SC22), "21");

        yaxis.setTargetCell(CDSHelper.TARGET_CELL_A3R5);
        yaxis.validateVirusSubjectCount(virusCounts, false);
        yaxis.back();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        yaxis.cancelSelection();

        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        cds.clearFilters();

    }

    @Test
    public void verifyInfoPaneCounts()
    {
        CDSHelper cds = new CDSHelper(this);
        InfoPane ip = new InfoPane(this);
        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        log("Set the y-axis and validate counts are as expected.");
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.confirmSelection();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        ip.waitForSpinners();

        assertEquals("Subjects count not as expected.", 1458, ip.getSubjectCount());
        assertEquals("Species count not as expected.", 2, ip.getSpeciesCount());
        assertEquals("Studies count not as expected.", 14, ip.getStudiesCount());
        assertEquals("Product count not as expected.", 1, ip.getProductCount());
        assertEquals("Treatments count not as expected.", 89, ip.getTreatmentsCount());
        assertEquals("Time Points count not as expected.", 55, ip.getTimePointsCount());
        assertEquals("Antigens In Y count not as expected.", 3, ip.getAntigensInYCount());

        log("Validate a list (species) from the info pane.");
        ip.clickSpeciesCount();
        String text = ip.getSpeciesList();
        assertTrue("Species list does not contain " + CDSHelper.SPECIES_HUMAN, text.contains(CDSHelper.SPECIES_HUMAN));
        assertTrue("Species list does not contain " + CDSHelper.SPECIES_VULCAN, text.contains(CDSHelper.SPECIES_VULCAN));
        ip.clickCancel();

        log("Set the x-axis and validate counts change as expected.");
        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.setCellType(CDSHelper.CELL_TYPE_CD8);
        xaxis.confirmSelection();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        ip.waitForSpinners();

        assertEquals("Subjects count not as expected.", 1453, ip.getSubjectCount());
        assertEquals("Species count not as expected.", 2, ip.getSpeciesCount());
        assertEquals("Studies count not as expected.", 14, ip.getStudiesCount());
        assertEquals("Product count not as expected.", 1, ip.getProductCount());
        assertEquals("Treatments count not as expected.", 89, ip.getTreatmentsCount());
        assertEquals("Time Points count not as expected.", 55, ip.getTimePointsCount());
        assertEquals("Antigens In X count not as expected.", 3, ip.getAntigensInXCount());
        assertEquals("Antigens In Y count not as expected.", 3, ip.getAntigensInYCount());

        log("Verify that Antigens in X and Antigens in Y bring up the variable selector.");
        try{
            log("Checking for x-axis variable selector.");
            ip.clickAntigensInXCount();
            sleep(CDSHelper.CDS_WAIT_ANIMATION);
            xaxis.cancelSelection();
            sleep(CDSHelper.CDS_WAIT_ANIMATION);
            log("Checking for y-axis variable selector.");
            ip.clickAntigensInYCount();
            sleep(CDSHelper.CDS_WAIT_ANIMATION);
            yaxis.cancelSelection();
            sleep(CDSHelper.CDS_WAIT_ANIMATION);
        }
        catch(NoSuchElementException ex)
        {
            fail("Variable selector was not shown as expected.");
        }

        log("Use the info pane to apply a filter to the plot.");
        ip.clickStudiesCount();
        ip.setFilter("RED 4");

        assertEquals("Subjects count not as expected.", 79, ip.getSubjectCount());
        assertEquals("Species count not as expected.", 1, ip.getSpeciesCount());
        assertEquals("Studies count not as expected.", 1, ip.getStudiesCount());
        assertEquals("Product count not as expected.", 1, ip.getProductCount());
        assertEquals("Treatments count not as expected.", 3, ip.getTreatmentsCount());
        assertEquals("Time Points count not as expected.", 2, ip.getTimePointsCount());
        assertEquals("Antigens In X count not as expected.", 1, ip.getAntigensInXCount());
        assertEquals("Antigens In Y count not as expected.", 1, ip.getAntigensInYCount());

        log("Clearing the filter.");
        cds.clearFilter(1);

        sleep(CDSHelper.CDS_WAIT_ANIMATION);

    }

    @Test
    public void verifyTimeAxisBasic()
    {
        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        Map expectedCounts = new HashMap<String, CDSHelper.TimeAxisData>();
        expectedCounts.put("QED_2", new CDSHelper.TimeAxisData("QED 2", 1, 2, 1, 5, 0, 0));
        expectedCounts.put("YOYO_55", new CDSHelper.TimeAxisData("YOYO 55", 1, 3, 1, 5, 0, 3));
        expectedCounts.put("ZAP_128", new CDSHelper.TimeAxisData("ZAP 128", 1, 5, 3, 19, 0, 0));
        expectedCounts.put("ZAP_133", new CDSHelper.TimeAxisData("ZAP 133", 0, 4, 1, 5, 0, 0));
        expectedCounts.put("ZAP_135", new CDSHelper.TimeAxisData("ZAP 135", 0, 0, 0, 0, 0, 0));

        final String yaxisScale = "\n0\n100\n200\n300\n400\n500\n600\n700";
        final String studyDaysScales = "0\n100\n200\n300\n400\n500\n600" + yaxisScale;
        final String studyDaysScaleAligedVaccination = "-300\n-200\n-100\n0\n100\n200\n300" + yaxisScale;
        final String studyWeeksScales = "0\n20\n40\n60\n80" + yaxisScale;
        final String studyWeeksScalesAlignedVaccination = "-40\n-20\n0\n20\n40" + yaxisScale;
        final String studyMonthsScales = "0\n5\n10\n15\n20" + yaxisScale;
        final String studyMonthsScalesAlignedVaccination = "-10\n-5\n0\n5\n10" + yaxisScale;

        log("Verify NAb Titer IC50, A3R5 and Study Days.");
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.NAB);
        yaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.setTargetCell(CDSHelper.TARGET_CELL_A3R5);
        yaxis.confirmSelection();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.TIME_POINTS);
        xaxis.pickVariable(CDSHelper.TIME_POINTS_DAYS);
        xaxis.confirmSelection();

        assertTrue("For NAb Titer 50, A3R5 vs Time Visit Days a study axis was not present.", hasStudyAxis());
        List<WebElement> studies = Locator.css("div.bottomplot > svg > g.study").findElements(getDriver());
        assertTrue("Expected " + expectedCounts.size() + " studies in the Time Axis, found " + studies.size() + ".", studies.size() == expectedCounts.size());
        log("Study count was as expected.");

        validateVisitCounts(studies, expectedCounts);
        cds.assertPlotTickText(studyDaysScales);

        log("Change x-axis to Study weeks, verify visit counts change as expected.");
        xaxis.openSelectorWindow();
        // Should go to the variable selector window by default.
        xaxis.pickVariable(CDSHelper.TIME_POINTS_WEEKS);
        xaxis.confirmSelection();

        // Need to get studies again, otherwise get a stale element error.
        studies = Locator.css("div.bottomplot > svg > g.study").findElements(getDriver());
        assertTrue("Expected " + expectedCounts.size() + " studies in the Time Axis, found " + studies.size() + ".", studies.size() == expectedCounts.size());
        log("Study count was as expected.");

        // Modify expected number of icons to be visible (we should not have overlapping vacc. and follow-up icons).
        expectedCounts.replace("ZAP_128", new CDSHelper.TimeAxisData("ZAP 128", 1, 5, 3, 14, 0, 0));

        validateVisitCounts(studies, expectedCounts);
        cds.assertPlotTickText(studyWeeksScales);

        log("Change x-axis to Study months, verify visit counts change as expected.");
        xaxis.openSelectorWindow();
        // Should go to the variable selector window by default.
        xaxis.pickVariable(CDSHelper.TIME_POINTS_MONTHS);
        xaxis.confirmSelection();

        studies = Locator.css("div.bottomplot > svg > g.study").findElements(getDriver());
        assertTrue("Expected 7 studies in the Time Axis, found " + studies.size() + ".", studies.size() == 5);
        log("Study count was as expected.");

        // Again account for behavior of not having overlapping icons.
        expectedCounts.replace("QED_2", new CDSHelper.TimeAxisData("QED 2", 1, 1, 2, 3, 0, 0));
        expectedCounts.replace("YOYO_55", new CDSHelper.TimeAxisData("YOYO 55", 2, 1, 0, 4, 0, 3));
        expectedCounts.replace("ZAP_128", new CDSHelper.TimeAxisData("ZAP 128", 3, 3, 1, 9, 0, 0));
        expectedCounts.replace("ZAP_133", new CDSHelper.TimeAxisData("ZAP 133", 1, 2, 0, 5, 0, 0));

        validateVisitCounts(studies, expectedCounts);
        cds.assertPlotTickText(studyMonthsScales);

        log("Change x-axis to Study days, change alignment to Enrollment, verify visit counts are as expected.");
        xaxis.openSelectorWindow();
        // Should go to the variable selector window by default.
        xaxis.pickVariable(CDSHelper.TIME_POINTS_DAYS);
        xaxis.setAlignedBy(CDSHelper.TIME_POINTS_ALIGN_ENROLL);
        xaxis.confirmSelection();

        // When changing the alignment to anything other than Day 0 study HVTN 205 will not appear because it has no visit information.
        expectedCounts.remove("ZAP_135");

        // Icon counts should go back to what they were before.
        expectedCounts.replace("QED_2", new CDSHelper.TimeAxisData("QED 2", 1, 2, 1, 5, 0, 0));
        expectedCounts.replace("YOYO_55", new CDSHelper.TimeAxisData("YOYO 55", 1, 3, 1, 5, 0, 3));
        expectedCounts.replace("ZAP_128", new CDSHelper.TimeAxisData("ZAP 128", 1, 5, 3, 19, 0, 0));
        expectedCounts.replace("ZAP_133", new CDSHelper.TimeAxisData("ZAP 133", 0, 4, 1, 5, 0, 0));

        studies = Locator.css("div.bottomplot > svg > g.study").findElements(getDriver());
        assertTrue("Expected " + expectedCounts.size() + " studies in the Time Axis, found " + studies.size() + ".", studies.size() == expectedCounts.size());
        log("Study count was as expected.");

        validateVisitCounts(studies, expectedCounts);
        cds.assertPlotTickText(studyDaysScales);

        log("Change x-axis alignment to Last Vaccination, verify visit counts are as expected.");
        // pre-enrollment has been removed temporarily. Previously QED, YOYO and ZAP 133 had pre-enrollment.
        expectedCounts.replace("QED_2", new CDSHelper.TimeAxisData("QED 2", 1, 2, 1, 5, 0, 0));
        expectedCounts.replace("YOYO_55", new CDSHelper.TimeAxisData("YOYO 55", 1, 3, 1, 5, 0, 3));
        expectedCounts.replace("ZAP_128", new CDSHelper.TimeAxisData("ZAP 128", 1, 5, 2, 20, 0, 0));
        expectedCounts.replace("ZAP_133", new CDSHelper.TimeAxisData("ZAP 133", 0, 4, 1, 5, 0, 0));
        xaxis.openSelectorWindow();
        // Should go to the variable selector window by default.
        xaxis.setAlignedBy("Last Vaccination");
        xaxis.confirmSelection();

        studies = Locator.css("div.bottomplot > svg > g.study").findElements(getDriver());
        assertTrue("Expected " + expectedCounts.size() + " studies in the Time Axis, found " + studies.size() + ".", studies.size() == expectedCounts.size());
        log("Study count was as expected.");

        validateVisitCounts(studies, expectedCounts);
        cds.assertPlotTickText(studyDaysScaleAligedVaccination);

        log("Change x-axis to Study weeks, and go back to aligned by Enrollment, verify visit are as expected.");
        expectedCounts.replace("QED_2", new CDSHelper.TimeAxisData("QED 2", 1, 2, 1, 5, 0, 0));
        expectedCounts.replace("YOYO_55", new CDSHelper.TimeAxisData("YOYO 55", 1, 3, 1, 5, 0, 3));
        expectedCounts.replace("ZAP_128", new CDSHelper.TimeAxisData("ZAP 128", 1, 5, 3, 14, 0, 0));
        expectedCounts.replace("ZAP_133", new CDSHelper.TimeAxisData("ZAP 133", 0, 4, 1, 5, 0, 0));
        xaxis.openSelectorWindow();
        // Should go to the variable selector window by default.
        xaxis.pickVariable(CDSHelper.TIME_POINTS_WEEKS);
        xaxis.setAlignedBy(CDSHelper.TIME_POINTS_ALIGN_ENROLL);
        xaxis.confirmSelection();

        // Need to get studies again, otherwise get a stale element error.
        studies = Locator.css("div.bottomplot > svg > g.study").findElements(getDriver());
        assertTrue("Expected " + expectedCounts.size() + " studies in the Time Axis, found " + studies.size() + ".", studies.size() == expectedCounts.size());
        log("Study count was as expected.");

        validateVisitCounts(studies, expectedCounts);
        cds.assertPlotTickText(studyWeeksScales);

        log("Change x-axis Aligned by Last Vaccination, verify visit are as expected.");
        // pre-enrollment has been removed temporarily. Previously QED, YOYO and ZAP 133 had pre-enrollment.
        expectedCounts.replace("QED_2", new CDSHelper.TimeAxisData("QED 2", 1, 2, 1, 5, 0, 0));
        expectedCounts.replace("YOYO_55", new CDSHelper.TimeAxisData("YOYO 55", 1, 3, 1, 5, 0, 3));
        expectedCounts.replace("ZAP_128", new CDSHelper.TimeAxisData("ZAP 128", 0, 6, 2, 15, 0, 0));
        expectedCounts.replace("ZAP_133", new CDSHelper.TimeAxisData("ZAP 133", 0, 4, 1, 5, 0, 0));
        xaxis.openSelectorWindow();
        // Should go to the variable selector window by default.
        xaxis.pickVariable(CDSHelper.TIME_POINTS_WEEKS);
        xaxis.setAlignedBy(CDSHelper.TIME_POINTS_ALIGN_LAST_VAC);
        xaxis.confirmSelection();

        // Need to get studies again, otherwise get a stale element error.
        studies = Locator.css("div.bottomplot > svg > g.study").findElements(getDriver());
        assertTrue("Expected " + expectedCounts.size() + " studies in the Time Axis, found " + studies.size() + ".", studies.size() == expectedCounts.size());
        log("Study count was as expected.");

        validateVisitCounts(studies, expectedCounts);
        cds.assertPlotTickText(studyWeeksScalesAlignedVaccination);

        log("Change x-axis to Study months, and go back to aligned by Enrollment, verify visit are as expected.");
        expectedCounts.replace("QED_2", new CDSHelper.TimeAxisData("QED 2", 1, 1, 2, 3, 0, 0));
        expectedCounts.replace("YOYO_55", new CDSHelper.TimeAxisData("YOYO 55", 2, 1, 0, 4, 0, 3));
        expectedCounts.replace("ZAP_128", new CDSHelper.TimeAxisData("ZAP 128", 3, 3, 1, 9, 0, 0));
        expectedCounts.replace("ZAP_133", new CDSHelper.TimeAxisData("ZAP 133", 1, 2, 0, 5, 0, 0));
        xaxis.openSelectorWindow();
        // Should go to the variable selector window by default.
        xaxis.pickVariable(CDSHelper.TIME_POINTS_MONTHS);
        xaxis.setAlignedBy(CDSHelper.TIME_POINTS_ALIGN_ENROLL);
        xaxis.confirmSelection();

        // Need to get studies again, otherwise get a stale element error.
        studies = Locator.css("div.bottomplot > svg > g.study").findElements(getDriver());
        assertTrue("Expected " + expectedCounts.size() + " studies in the Time Axis, found " + studies.size() + ".", studies.size() == expectedCounts.size());
        log("Study count was as expected.");

        validateVisitCounts(studies, expectedCounts);
        cds.assertPlotTickText(studyMonthsScales);

        log("Change x-axis Aligned by Last Vaccination, verify visit are as expected.");
        // pre-enrollment has been removed temporarily. Previously QED, YOYO and ZAP 133 had pre-enrollment.
        expectedCounts.replace("QED_2", new CDSHelper.TimeAxisData("QED 2", 2, 1, 0, 3, 0, 0));
        expectedCounts.replace("YOYO_55", new CDSHelper.TimeAxisData("YOYO 55", 3, 1, 0, 2, 0, 2));
        expectedCounts.replace("ZAP_128", new CDSHelper.TimeAxisData("ZAP 128", 3, 3, 0, 11, 0, 0));
        expectedCounts.replace("ZAP_133", new CDSHelper.TimeAxisData("ZAP 133", 1, 3, 0, 3, 0, 0));
        xaxis.openSelectorWindow();
        // Should go to the variable selector window by default.
        xaxis.pickVariable(CDSHelper.TIME_POINTS_MONTHS);
        xaxis.setAlignedBy(CDSHelper.TIME_POINTS_ALIGN_LAST_VAC);
        xaxis.confirmSelection();

        // Need to get studies again, otherwise get a stale element error.
        studies = Locator.css("div.bottomplot > svg > g.study").findElements(getDriver());
        assertTrue("Expected " + expectedCounts.size() + " studies in the Time Axis, found " + studies.size() + ".", studies.size() == expectedCounts.size());
        log("Study count was as expected.");

        validateVisitCounts(studies, expectedCounts);
        cds.assertPlotTickText(studyMonthsScalesAlignedVaccination);

        click(CDSHelper.Locators.cdsButtonLocator("clear"));

        // Makes the test a little more reliable.
        waitForElement(Locator.xpath("//div[contains(@class, 'noplotmsg')][not(contains(@style, 'display: none'))]"));

    }

    @Test
    public void verifyTimeAxisWithMultipleSchedules()
    {
        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        String cssPath;
        List<String> expectedToolTipText;
        Actions builder; // Used to hover the WebElement over a tag.

        Map expectedCounts = new HashMap<String, CDSHelper.TimeAxisData>();
        expectedCounts.put("RED_4", new CDSHelper.TimeAxisData("RED 4", 1, 3, 1, 5, 1, 0));
        expectedCounts.put("ZAP_110", new CDSHelper.TimeAxisData("ZAP 110", 1, 4, 3, 4, 0, 0));
        expectedCounts.put("ZAP_111", new CDSHelper.TimeAxisData("ZAP 111", 1, 4, 2, 6, 0, 0));
        expectedCounts.put("ZAP_134", new CDSHelper.TimeAxisData("ZAP 134", 0, 4, 2, 10, 0, 0));

        final String yaxisScale = "\n0\n5000\n10000\n15000\n20000\n25000\n30000\n35000\n40000\n45000";
        final String studyDaysScales = "0\n200\n400\n600\n800\n1000" + yaxisScale;

        log("Verify ELISPOT Magnitude - Background subtracted and Study Days with axis collapsed and expanded.");
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ELISPOT);
        yaxis.pickVariable(CDSHelper.ELISPOT_MAGNITUDE_BACKGROUND_SUB);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.TIME_POINTS);
        xaxis.pickVariable(CDSHelper.TIME_POINTS_DAYS);
        xaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();

        assertTrue("For ELISPOT Magnitude - Background subtracted vs Time Visit Days a study axis was not present.", hasStudyAxis());
        List<WebElement> studies = Locator.css("div.bottomplot > svg > g.study").findElements(getDriver());
        assertTrue("Expected " + expectedCounts.size() + " studies in the Time Axis, found" + studies.size() + ".", studies.size() == expectedCounts.size());
        log("Study count was as expected.");

        // Get the element again to avoid the stale-element error.
        studies = Locator.css("div.bottomplot > svg > g.study").findElements(getDriver());
        validateVisitCounts(studies, expectedCounts);

        log("Validate that the tool-tips are as expected.");

        // Going to leave the values for the tool-tips hard coded here. Unlikely they would ever be used anywhere else.
        // Alternative to hard coding the values would be to write a generator that would build the expected tool-tips,
        // but that is more effort that I have time for now.
        expectedToolTipText = new ArrayList<>();
        expectedToolTipText.add("RED 4");
        expectedToolTipText.add("Group 1 Arm T1 Vaccine: Enrollment, Vaccination");
        expectedToolTipText.add("Group 2 Arm T2 Vaccine: Enrollment, Vaccination");
        expectedToolTipText.add("Group 3 Arm T3 Vaccine: Enrollment, Vaccination");
        cssPath = "div.bottomplot > svg > g:nth-child(2) > image:nth-of-type(1)";
        timeAxisToolTipsTester(cssPath, expectedToolTipText);

        expectedToolTipText.clear();
        expectedToolTipText.add("RED 4");
        expectedToolTipText.add("Group 1 Arm T1 Vaccine: Last Vaccination");
        expectedToolTipText.add("Group 2 Arm T2 Vaccine: Last Vaccination");
        expectedToolTipText.add("Group 3 Arm T3 Vaccine: Last Vaccination");
        cssPath = "div.bottomplot > svg > g:nth-child(2) > image:nth-of-type(6)";
        timeAxisToolTipsTester(cssPath, expectedToolTipText);

        expectedToolTipText.clear();
        expectedToolTipText.add("ZAP 111");
        expectedToolTipText.add("Group 1 Arm Ca Placebo: Enrollment, Vaccination");
        expectedToolTipText.add("Group 1 Arm T1 Vaccine: Enrollment, Vaccination");
        expectedToolTipText.add("Group 2 Arm Ca Placebo: Enrollment, Vaccination");
        expectedToolTipText.add("Group 2 Arm T2 Vaccine: Enrollment, Vaccination");
        expectedToolTipText.add("Group 3 Arm Ca Placebo: Enrollment, Vaccination");
        expectedToolTipText.add("Group 3 Arm T3 Vaccine: Enrollment, Vaccination");
        expectedToolTipText.add("Group 4 Arm Ca Placebo: Enrollment, Vaccination");
        expectedToolTipText.add("Group 4 Arm T4 Vaccine: Enrollment, Vaccination");
        expectedToolTipText.add("Group 5 Arm Cb Placebo: Enrollment, Vaccination");
        expectedToolTipText.add("Group 5 Arm T5 Vaccine: Enrollment, Vaccination");
        expectedToolTipText.add("Group 7 Arm Cb Placebo: Enrollment, Vaccination");
        expectedToolTipText.add("Group 7 Arm T7 Vaccine: Enrollment, Vaccination");
        cssPath = "div.bottomplot > svg > g:nth-child(4) > image:nth-of-type(1)";
        timeAxisToolTipsTester(cssPath, expectedToolTipText);

        log("Verify that points in the main plot get highlighted when mousing over items on the Study Axis.");

        // Move the mouse out of the way so it doesn't interfer with the highlight count.
        mouseOver(Locator.css("div.logo > img[src$='logo.png']"));

        cssPath = "div.bottomplot > svg > g:nth-child(5) > image"; //[xlink:href$='nonvaccination_normal.svg']";
        scrollIntoView(Locator.css(cssPath));

        List<WebElement> weList = findTimeAxisPointsWithData(cssPath, "nonvaccination_normal.svg");

        assertTrue("No glyphs in the time axis had a value indicating they had data.", weList.size() > 0);
        int totalCount = 0, highlightCount;

        for(WebElement we : weList)
        {
            // Hover over the element.
            builder = new Actions(getDriver());
            builder.moveToElement(we).perform();
            sleep(500);

            // Count the number of points that are highlighted in the main plot.
            highlightCount = getPointCountByColor(MOUSEOVER_FILL);
            log("Highlighted point count: " + highlightCount);
            totalCount = totalCount + highlightCount;
        }

        assertTrue("No points in the plot were highlighted when hovering over a time axis point.", totalCount > 0);

        log("Verify that points get highlighted when hovering over a time axis label.");
        cssPath = "div.bottomplot > svg > g:nth-child(5) > text.study-label";
        mouseOver(Locator.css(cssPath));

        sleep(500);
        highlightCount = getPointCountByColor(MOUSEOVER_FILL);
        assertTrue("No points in the plot were highlighted when hovering over a time axis study label.", highlightCount > 0);

        highlightCount = getTimeAxisPointCountByImage("_hover.svg");
        assertTrue("No points in the time axis were highlighted when hovering over a time axis study label.", highlightCount > 0);

        log("Expand the time axis and verify the counts.");
        Locator.css("div.bottomplot > svg > g > image.img-expand").findElement(getDriver()).click();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        expectedCounts.clear();
        expectedCounts.put("RED_4", new CDSHelper.TimeAxisData("RED 4", 0, 0, 0, 0, 0, 0));
        expectedCounts.put("RED_4-Group_1_Arm_T1_Vaccine", new CDSHelper.TimeAxisData("Group 1 Arm T1 Vaccine", 1, 3, 2, 5, 0, 0));
        expectedCounts.put("RED_4-Group_2_Arm_T2_Vaccine", new CDSHelper.TimeAxisData("Group 2 Arm T2 Vaccine", 1, 3, 1, 5, 1, 0));
        expectedCounts.put("ZAP_110", new CDSHelper.TimeAxisData("ZAP 110", 0, 0, 0, 0, 0, 0));
        expectedCounts.put("ZAP_110-Group_1_Arm_Ca_Placebo", new CDSHelper.TimeAxisData("Group 1 Arm Ca Placebo", 1, 4, 1, 6, 0, 0));
        expectedCounts.put("ZAP_110-Group_2_Arm_Ca_Placebo", new CDSHelper.TimeAxisData("Group 2 Arm Ca Placebo", 1, 4, 1, 6, 0, 0));
        expectedCounts.put("ZAP_110-Group_7_Arm_T7_Vaccine", new CDSHelper.TimeAxisData("Group 7 Arm T7 Vaccine", 1, 4, 3, 4, 0, 0));
        expectedCounts.put("ZAP_111", new CDSHelper.TimeAxisData("ZAP 111", 0, 0, 0, 0, 0, 0));
        expectedCounts.put("ZAP_111-Group_1_Arm_T1_Vaccine", new CDSHelper.TimeAxisData("Group 1 Arm T1 Vaccine", 1, 4, 2, 6, 0, 0));
        expectedCounts.put("ZAP_111-Group_3_Arm_Ca_Placebo", new CDSHelper.TimeAxisData("Group 3 Arm Ca Placebo", 1, 4, 1, 7, 0, 0));
        expectedCounts.put("ZAP_111-Group_4_Arm_T4_Vaccine", new CDSHelper.TimeAxisData("Group 4 Arm T4 Vaccine", 1, 4, 2, 6, 0, 0));
        expectedCounts.put("ZAP_134", new CDSHelper.TimeAxisData("ZAP 134", 0, 0, 0, 0, 0, 0));
        expectedCounts.put("ZAP_134-Group_1_Arm_T1_Vaccine", new CDSHelper.TimeAxisData("Group 1 Arm T1 Vaccine", 0, 4, 2, 10, 0, 0));
        expectedCounts.put("ZAP_134-Group_2_Arm_C1_Placebo", new CDSHelper.TimeAxisData("Group 2 Arm C1 Placebo", 0, 4, 1, 11, 0, 0));

        studies = Locator.css("div.bottomplot > svg > g.study").findElements(getDriver());
        assertTrue("Expected 35 studies in the Time Axis, found" + studies.size() + ".", studies.size() == 35);
        validateVisitCounts(studies, expectedCounts);
        log("The counts are as expected.");

        log("Validate that the tool-tips are as expected when expanded.");

        expectedToolTipText.clear();
        expectedToolTipText.add("ZAP 110 - Day 455");
        expectedToolTipText.add("Group 6 Arm T6 Vaccine: Follow-Up");
        cssPath = "div.bottomplot > svg > g:nth-child(18) > image:nth-of-type(10)";
        timeAxisToolTipsTester(cssPath, expectedToolTipText);

        expectedToolTipText.clear();
        expectedToolTipText.add("ZAP 111 - Day 364");
        expectedToolTipText.add("Group 5 Arm T5 Vaccine: Follow-Up");
        cssPath = "div.bottomplot > svg > g:nth-child(31) > image:nth-of-type(8)";
        timeAxisToolTipsTester(cssPath, expectedToolTipText);

        log("Change time axis alignment and validate things remain the same.");
        xaxis.openSelectorWindow();
        xaxis.pickVariable(CDSHelper.TIME_POINTS_WEEKS);
        xaxis.setAlignedBy(CDSHelper.TIME_POINTS_ALIGN_LAST_VAC);
        xaxis.confirmSelection();

        expectedCounts.put("ZAP_111-Group_1_Arm_T1_Vaccine", new CDSHelper.TimeAxisData("Group 1 Arm T1 Vaccine", 1, 4, 2, 6, 0, 0));
        expectedCounts.put("ZAP_111-Group_3_Arm_Ca_Placebo", new CDSHelper.TimeAxisData("Group 3 Arm Ca Placebo", 1, 4, 1, 7, 0, 0));
        expectedCounts.put("ZAP_111-Group_4_Arm_T4_Vaccine", new CDSHelper.TimeAxisData("Group 4 Arm T4 Vaccine", 1, 4, 2, 6, 0, 0));
        expectedCounts.put("ZAP_134-Group_1_Arm_T1_Vaccine", new CDSHelper.TimeAxisData("Group 1 Arm T1 Vaccine", 0, 4, 2, 10, 0, 0));
        expectedCounts.put("ZAP_134-Group_2_Arm_C1_Placebo", new CDSHelper.TimeAxisData("Group 2 Arm C1 Placebo", 0, 4, 1, 11, 0, 0));

        studies = Locator.css("div.bottomplot > svg > g.study").findElements(getDriver());
        assertTrue("Expected 35 studies in the Time Axis, found" + studies.size() + ".", studies.size() == 35);
        validateVisitCounts(studies, expectedCounts);
        log("The counts are as expected.");

        log("Validate that the tool-tips are as expected when expanded.");

        expectedToolTipText.clear();
        expectedToolTipText.add("ZAP 111 - Day 182");
        expectedToolTipText.add("Group 1 Arm Ca Placebo: Follow-Up");
        cssPath = "div.bottomplot > svg > g.study:nth-child(22) > image.visit-tag[x^='3']";
        timeAxisToolTipsTester(cssPath, expectedToolTipText);

    }

    @Test
    public void verifyTimeAxisFilter()
    {
        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);
        String cssPath;
        Actions builder; // Used to manuver the WebElement.

        final String yaxisScale = "\n0\n5000\n10000\n15000\n20000\n25000\n30000\n35000\n40000\n45000";
        final String studyDaysScales = "0\n200\n400\n600\n800\n1000" + yaxisScale;

        log("Plot ELISPOT Magnitude - Background subtracted vs Study Days.");
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ELISPOT);
        yaxis.pickVariable(CDSHelper.ELISPOT_MAGNITUDE_BACKGROUND_SUB);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.TIME_POINTS);
        xaxis.pickVariable(CDSHelper.TIME_POINTS_DAYS);
        xaxis.confirmSelection();
        _ext4Helper.waitForMaskToDisappear();

        assertTrue("For ELISPOT Magnitude - Background subtracted vs Time Visit Days a study axis was not present.", hasStudyAxis());

        cssPath = "div.bottomplot > svg > g:nth-child(2) > text.study-label";
        click(Locator.css(cssPath));
        assertTextPresent("Study = RED 4", 1);

        clickButton("Filter", 0);

        InfoPane ip = new InfoPane(this);
        ip.waitForSpinners();
        sleep(500);

        assertEquals("Subjects count not as expected.", 60, ip.getSubjectCount());
        assertEquals("Species count not as expected.", 1, ip.getSpeciesCount());
        assertEquals("Studies count not as expected.", 1, ip.getStudiesCount());
        assertEquals("Product count not as expected.", 1, ip.getProductCount());
        assertEquals("Treatments count not as expected.", 3, ip.getTreatmentsCount());
        assertEquals("Time Points count not as expected.", 3, ip.getTimePointsCount());
        assertEquals("Antigens In Y count not as expected.", 9, ip.getAntigensInYCount());

        cssPath = "div.bottomplot > svg > g:nth-child(2) > image";
        List<WebElement> weList = findTimeAxisPointsWithData(cssPath, "challenge_normal.svg");

        assertTrue("No glyphs in the time axis had a value indicating they had data.", weList.size() > 0);

        log("Mouse over the challenge glyph in the time axis.");
        builder = new Actions(getDriver());
        builder.moveToElement(weList.get(0), 0, 4).perform();
        sleep(1000);

        assertTextPresent("Group 1 Arm T1");

        log("Click the challenge glyph in the time axis to apply it as a filter.");
        builder = new Actions(getDriver());
        builder.moveToElement(weList.get(0)).click().build().perform();
        clickButton("Filter", 0);

        ip.waitForSpinners();
        sleep(500);

        assertEquals("Subjects count not as expected.", 58, ip.getSubjectCount());
        assertEquals("Species count not as expected.", 1, ip.getSpeciesCount());
        assertEquals("Studies count not as expected.", 1, ip.getStudiesCount());
        assertEquals("Product count not as expected.", 1, ip.getProductCount());
        assertEquals("Treatments count not as expected.", 3, ip.getTreatmentsCount());
        assertEquals("Time Points count not as expected.", 1, ip.getTimePointsCount());
        assertEquals("Antigens In Y count not as expected.", 9, ip.getAntigensInYCount());

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

        for (int i = 0; i < CDSHelper.ANTIGENS_NAME.length; i++)
        {
            assertElementVisible(Locator.xpath("//div[contains(@class, 'y-axis-selector')]//div[contains(@class, 'content')]//label[contains(@class, 'x-form-cb-label')][text()='" + CDSHelper.ANTIGENS_NAME[i] + "']"));
        }

        yaxis.cancelSelection();

        log("Validate BAMA Antigen panel on xaxis.");
        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.BAMA);
        xaxis.pickVariable(CDSHelper.BAMA_MAGNITUDE_DELTA_BASELINE);
        xaxis.openAntigenPanel();

        for (int i = 0; i < CDSHelper.ANTIGENS_NAME.length; i++)
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
        String uniqueVirusId = cds.buildIdentifier(CDSHelper.TITLE_NAB, CDSHelper.COLUMN_ID_VIRUS_NAME, CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, uniqueVirus);

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.NAB);
        xaxis.pickVariable(CDSHelper.NAB_LAB);
        xaxis.confirmSelection();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        yaxis.pickSource(CDSHelper.NAB);
        yaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.setVirusName(uniqueVirusId);
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
        xaxis.confirmSelection();

        waitForElement(plotTick.withText("Pseudovirus"));
        assertElementPresent(plotBox, 1);

        click(CDSHelper.Locators.cdsButtonLocator("view data"));
        sleep(CDSHelper.CDS_WAIT);
        switchToWindow(1);
        plotDataTable = new DataRegionTable("query", this);
        assertEquals(100, plotDataTable.getDataRowCount());

        // Sort the grid to try and get some predictability to number of specific values in a given column.
        _ext4Helper.resetCssPrefix();
        plotDataTable.setSort("cds_GridBase_SubjectId", SortDirection.ASC);
        plotDataTable.setSort("cds_GridBase_ParticipantSequenceNum", SortDirection.ASC);
        _ext4Helper.setCssPrefix("x-");

        int actualCount = getElementCount(Locator.tagContainingText("td", uniqueVirus));
        assertEquals(100, actualCount);
        getDriver().close();
        switchToMainWindow();

    }

    @Test
    public void verifyAntigenScatterPlot()
    {
        CDSHelper cds = new CDSHelper(this);
        String xVirus = CDSHelper.VIRUS_BAL26;
        String yVirus = CDSHelper.VIRUS_SF162;
        String xVirusId = cds.buildIdentifier(CDSHelper.TITLE_NAB, CDSHelper.COLUMN_ID_VIRUS_NAME, CDSHelper.NEUTRAL_TIER_NA, CDSHelper.ANTIGEN_CLADE_NOT_RECORDED, xVirus);
        String y1VirusId = cds.buildIdentifier(CDSHelper.TITLE_NAB, CDSHelper.COLUMN_ID_VIRUS_NAME, CDSHelper.NEUTRAL_TIER_1, CDSHelper.ANTIGEN_CLADE_B, yVirus);

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.NAB);
        xaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        xaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        xaxis.setVirusName(xVirusId);
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        xaxis.confirmSelection();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        yaxis.pickSource(CDSHelper.NAB);
        yaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.setVirusName(y1VirusId);
        yaxis.confirmSelection();

        waitForElement(plotTickLinear.withText("1000"));
        assertElementPresent(plotPoint, 1209);

        click(CDSHelper.Locators.cdsButtonLocator("view data"));
        sleep(CDSHelper.CDS_WAIT);
        switchToWindow(1);
        Ext4Helper.resetCssPrefix();
        DataRegionTable plotDataTable = new DataRegionTable("query", this);
        assertEquals(100, plotDataTable.getDataRowCount());
        assertElementPresent(Locator.paginationText(1, 100, 2279));
        getDriver().close();
        switchToMainWindow();
        Ext4Helper.setCssPrefix("x-");

        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ELISPOT);
        yaxis.pickVariable(CDSHelper.ELISPOT_MAGNITUDE_BACKGROUND_SUB);
        yaxis.confirmSelection();
        sleep(CDSHelper.CDS_WAIT);
        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        xaxis.setCellType("All");
        xaxis.confirmSelection();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        waitForElement(plotTickLinear.withText("200"));
        assertElementPresent(plotPoint, 290);

        click(CDSHelper.Locators.cdsButtonLocator("view data"));
        sleep(CDSHelper.CDS_WAIT);
        switchToWindow(1);
        Ext4Helper.resetCssPrefix();
        plotDataTable = new DataRegionTable("query", this);
        assertEquals(100, plotDataTable.getDataRowCount());
        assertElementPresent(Locator.paginationText(1, 100, 2929));
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
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_RAW);
        xaxis.setDataSummaryLevel(CDSHelper.DATA_SUMMARY_PROTEIN);
        xaxis.setProtein(cds.buildIdentifier(CDSHelper.DATA_SUMMARY_PROTEIN_PANEL, "all"));
        xaxis.setCellType("All");
        xaxis.confirmSelection();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        // set the y-axis
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND);
        yaxis.setCellType("All");
        yaxis.setDataSummaryLevel(CDSHelper.DATA_SUMMARY_PROTEIN);
        yaxis.setProtein(cds.buildIdentifier(CDSHelper.DATA_SUMMARY_PROTEIN_PANEL, "All"));
        yaxis.confirmSelection();

        // Verify the binning message
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        waitForText("Heatmap on");

        log("Validate that there are bin squares in the plot.");
        int squareCount = getElementCount(Locator.css("svg g.layer a.vis-bin-square"));
        assertTrue("Expected over 2000 bin squares found: " + squareCount, squareCount > 2000);

        cds.ensureNoFilter();
    }

    @Test
    public void verifyLogAndLinearScales()
    {
        String scaleValues, originalScale;
        int expectedCount, originalCount;
        CDSHelper cds = new CDSHelper(this);

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        log("Validate default scale is Log");

        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        yaxis.setCellType("All");
        yaxis.confirmSelection();

        scaleValues = "≤0\n0.0005\n0.005\n0.05\n0.5\n5";
        expectedCount = 1604;

        verifyLogAndLinearHelper(scaleValues, 1, expectedCount, true);
        assertTrue("There was no x-axis log gutter there should be.", hasXLogGutter());
        assertTrue("There was a y-axis log gutter there should not be.", !hasYLogGutter());

        log("Change scale to Linear and validate that values change.");

        yaxis.openSelectorWindow();
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.confirmSelection();

        scaleValues = "0\n2\n4\n6\n8\n10\n12\n14";
        expectedCount = 1604;

        verifyLogAndLinearHelper(scaleValues, 1, expectedCount, false);
        assertTrue("There  x-axis log gutter was present, it should not be there.", !hasXLogGutter());
        assertTrue("There was a y-axis log gutter there should not be.", !hasYLogGutter());

        // Clear the plot.
        cds.clearFilters();

        log("Validate a plot with a values on both y and x axis.");

        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        yaxis.setCellType(CDSHelper.CELL_TYPE_CD4);
        yaxis.confirmSelection();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        xaxis.setCellType(CDSHelper.CELL_TYPE_CD8);
        xaxis.confirmSelection();

        originalScale = "≤0\n0.0005\n0.005\n0.05\n0.5\n5\n≤0\n0.001\n0.01\n0.1\n1";
        originalCount = 1453;
        verifyLogAndLinearHelper(originalScale, 2, originalCount, true);
        assertTrue("There was no x-axis log gutter there should be.", hasXLogGutter());
        assertTrue("There was no y-axis log gutter there should be.", hasYLogGutter());


        log("Change x-axis to be linear.");

        xaxis.openSelectorWindow();
        xaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        xaxis.confirmSelection();

        scaleValues = "0\n2\n4\n6\n8\n10\n12\n14\n≤0\n0.001\n0.01\n0.1\n1";
        expectedCount = 1453;  // Is this right?
        verifyLogAndLinearHelper(scaleValues, 2, expectedCount, true);
        assertTrue("There was no x-axis log gutter there should be.", hasXLogGutter());
        assertTrue("There was a y-axis log gutter there should not be.", !hasYLogGutter());

        log("Change y-axis to be linear.");

        yaxis.openSelectorWindow();
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.confirmSelection();

        scaleValues = "0\n2\n4\n6\n8\n10\n12\n14\n0\n0.5\n1\n1.5\n2\n2.5\n3\n3.5\n4\n4.5\n5";
        expectedCount = 1453;
        verifyLogAndLinearHelper(scaleValues, 2, expectedCount, false);
        assertTrue("There  x-axis log gutter was present, it should not be there.", !hasXLogGutter());
        assertTrue("There was a y-axis log gutter there should not be.", !hasYLogGutter());

        log("Change x-axis back to log.");

        xaxis.openSelectorWindow();
        xaxis.setScale(DataspaceVariableSelector.Scale.Log);
        xaxis.confirmSelection();

        scaleValues = "≤0\n0.0005\n0.005\n0.05\n0.5\n5\n0\n0.5\n1\n1.5\n2\n2.5\n3\n3.5\n4\n4.5\n5";
        expectedCount = 1453;
        verifyLogAndLinearHelper(scaleValues, 2, expectedCount, true);
        assertTrue("There  x-axis log gutter was present, it should not be there.", !hasXLogGutter());
        assertTrue("There was no y-axis log gutter there should be.", hasYLogGutter());

        log("Change y-axis back to log, all values should return to original.");

        yaxis.openSelectorWindow();
        yaxis.setScale(DataspaceVariableSelector.Scale.Log);
        yaxis.confirmSelection();

        verifyLogAndLinearHelper(originalScale, 2, originalCount, true);
        assertTrue("There was no x-axis log gutter there should be.", hasXLogGutter());
        assertTrue("There was no y-axis log gutter there should be.", hasYLogGutter());

        // Clear the plot.
        cds.clearFilters();

        log("Validate log and linear with large scale values.");

        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.NAB);
        yaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        yaxis.confirmSelection();

        scaleValues = "3\n30\n300\n3000";
        expectedCount = 796;
        verifyLogAndLinearHelper(scaleValues, 1, expectedCount, false);
        assertTrue("There  x-axis log gutter was present, it should not be there. (there are no negative values with this plot)", !hasXLogGutter());

        log("Change y-axis to be linear.");

        yaxis.openSelectorWindow();
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.confirmSelection();

        scaleValues = "0\n1000\n2000\n3000\n4000\n5000\n6000\n7000\n8000";
        expectedCount = 796;
        verifyLogAndLinearHelper(scaleValues, 1, expectedCount, false);

        // Clear the plot.
        cds.clearFilters();

        log("Validate with a categorical on x-axis.");

        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ELISPOT);
        yaxis.pickVariable(CDSHelper.ELISPOT_MAGNITUDE_BACKGROUND_SUB);
        yaxis.confirmSelection();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.SUBJECT_CHARS);
        xaxis.pickVariable(CDSHelper.DEMO_AGEGROUP);
        xaxis.confirmSelection();

        originalScale = "10-19\n20-29\n30-39\n40-49\n50-59\n60-69\n≤0\n30\n300\n3000\n30000";
        originalCount = 477;
        verifyLogAndLinearHelper(originalScale, 1, originalCount, true);

        log("Add a filter and make sure that the log scale changes appropriately.");
        addRaceFilter(CDSHelper.RACE_ASIAN);
        _asserts.assertFilterStatusCounts(55, 4, 1, 1, 18);

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);
        originalScale = "10-19\n20-29\n30-39\n40-49\n50-59\n60-69\n≤0\n30\n300";
        originalCount = 55;
        verifyLogAndLinearHelper(originalScale, 1, originalCount, true);

        // Clear the plot.
        cds.clearFilters();

    }

    private void verifyLogAndLinearHelper(String scaleValues, int svgIndex, int expectedCount, boolean msgVisable)
    {
        final String XPATH_SUBJECT_COUNT = "//div[contains(@class, 'status-row')]//span[contains(@class, 'hl-status-label')][contains(text(), 'Subjects')]/./following-sibling::span[contains(@class, ' hl-status-count ')][not(contains(@class, 'hideit'))]";
        String tempStr, styleValue;
        int subjectCount;

        cds.assertPlotTickText(svgIndex, scaleValues);

        tempStr = getText(Locator.xpath(XPATH_SUBJECT_COUNT));
        subjectCount = Integer.parseInt(tempStr.replaceAll(",", ""));
        assertEquals("Subject count not as expected.", expectedCount, subjectCount);

    }

    @Test
    public void verifyPlotToolTips()
    {
        String cssPathToSvg;
        int pointToClick;
        CDSHelper cds = new CDSHelper(this);

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        log("Create a simple data point plot.");

        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        yaxis.setCellType(CDSHelper.CELL_TYPE_CD4);
        yaxis.confirmSelection();

        log("Click on a point in the plot and make sure the tool tip is as expected.");
        // Try to protect from getting an index out of range error.
        pointToClick = getElementCount(Locator.css("div.plot:not(.thumbnail) > svg:nth-of-type(1) a.point"))/4;
        log("Going to click on the " + pointToClick + " element from \"div:not(.thumbnail) > svg:nth-of-type(1) a.point\".");
        cssPathToSvg = "div.plot:not(.thumbnail) > svg:nth-of-type(1)";

        cds.clickPointInPlot(cssPathToSvg, pointToClick);

        // By design the tool tip does not show up instantly, so adding a pause to give it a chance.
        sleep(1000);

        assertElementVisible(Locator.css("div.hopscotch-bubble-container"));

        log("Click someplace else to make the tool tip go away.");
        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        log("Now filter the plot to make it easier to validate the data in teh tool tip.");

        cds.goToSummary();
        cds.clickBy("Studies");
        cds.applySelection("RED 4");

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        log("Click on a point in the plot and make sure the tool tip has the expected text.");

        // Try to protect from getting an index out of range error.
        pointToClick = getElementCount(Locator.css("div.plot:not(.thumbnail) > svg:nth-of-type(1) a.point"))/4;
        log("Going to click on the " + pointToClick + " element from \"div:not(.thumbnail) > svg:nth-of-type(1) a.point\".");
        cssPathToSvg = "div.plot:not(.thumbnail) > svg:nth-of-type(1)";

        cds.clickPointInPlot(cssPathToSvg, pointToClick);

        // By design the tool tip does not show up instantly, so adding a pause to give it a chance.
        sleep(1000);

        assertElementVisible(Locator.css("div.hopscotch-bubble-container"));

        assertTextPresent("RED 4", "elit ac nulla sed vel enim sit", "Cell type: CD4+");

        log("Click someplace else to make the tool tip go away.");
        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        log("Clear the filter and create a plot that has values in the gutter.");
        cds.clearFilter(1);

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        xaxis.setCellType(CDSHelper.CELL_TYPE_CD8);
        xaxis.confirmSelection();

        log("Click on a point in the 'Undefined X value' gutter and make sure the tool tip is as expected.");
        // Try to protect from getting an index out of range error.
        pointToClick = getElementCount(Locator.css("div.plot:not(.thumbnail) > svg:nth-of-type(1) a.point"))/4;
        log("Going to click on the " + pointToClick + " element from \"div:not(.thumbnail) > svg:nth-of-type(1) a.point\".");
        cssPathToSvg = "div.plot:not(.thumbnail) > svg:nth-of-type(1)";

        cds.clickPointInPlot(cssPathToSvg, pointToClick);

        // By design the tool tip does not show up instantly, so adding a pause to give it a chance.
        sleep(1000);

        validateToolTipText("Magnitude (% cells) - Background subtracted", "Data summary level: Protein Panel", "Protein panel: Any HIV PTEg");

        log("Remove the tool tip.");
        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        log("Click on a point in the 'Undefined Y value' gutter and make sure the tool tip is as expected.");
        // Try to protect from getting an index out of range error.
        pointToClick = getElementCount(Locator.css("div.plot:not(.thumbnail) > svg:nth-of-type(1) a.point"))/4;
        log("Going to click on the " + pointToClick + " element from \"div:not(.thumbnail) > svg:nth-of-type(1) a.point\".");
        cssPathToSvg = "div.bottomplot > svg";

        cds.clickPointInPlot(cssPathToSvg, pointToClick);

        // By design the tool tip does not show up instantly, so adding a pause to give it a chance.
        sleep(1000);

        validateToolTipText("Magnitude (% cells) - Background subtracted", "Data summary level: Protein Panel", "Protein panel: Any HIV PTEg");

        log("Remove the tool tip.");
        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        log("Click on a point in the main plot and make sure the tool tip is as expected.");
        // Try to protect from getting an index out of range error.
        pointToClick = getElementCount(Locator.css("div.plot:not(.thumbnail) > svg:nth-of-type(2) a.point"))/4;
        log("Going to click on the " + pointToClick + " element from \"div:not(.thumbnail) > svg:nth-of-type(1) a.point\".");
        cssPathToSvg = "div.plot:not(.thumbnail) > svg:nth-of-type(2)";

        cds.clickPointInPlot(cssPathToSvg, pointToClick);

        // By design the tool tip does not show up instantly, so adding a pause to give it a chance.
        sleep(1000);

        validateToolTipText("Magnitude (% cells) - Background subtracted", "Data summary level: Protein Panel", "Protein panel: Any HIV PTEg");

        log("Change the plot to a heat map.");
        xaxis.openSelectorWindow();
        xaxis.removeVariable();

        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.setCellType("All");
        yaxis.confirmSelection();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.SUBJECT_CHARS);
        xaxis.pickVariable(CDSHelper.DEMO_AGE);
        xaxis.confirmSelection();

        log("Click on one of the heat map points and make sure the tool tip is as expected.");
        // Try to protect from getting an index out of range error.
        pointToClick = getElementCount(Locator.css("div.plot:not(.thumbnail) > svg:nth-of-type(1) a.vis-bin-square"))/4;
        log("Going to click on the " + pointToClick + " element from \"div:not(.thumbnail) > svg:nth-of-type(1) a.vis-bin-square\".");
        cssPathToSvg = "div.plot:not(.thumbnail) > svg:nth-of-type(1)";

        cds.clickHeatPointInPlot(cssPathToSvg, pointToClick);

        // By design the tool tip does not show up instantly, so adding a pause to give it a chance.
        sleep(1000);

        validateToolTipText("Magnitude (% cells) - Background subtracted", "Age at Enrollment", "Functional marker name: IL2", "Data summary level: Protein Panel");

    }

    public void validateToolTipText(String...searchText)
    {
        String toolTipText;
        WebElement weToolTip;
        boolean pass = true;

        weToolTip = Locator.xpath("//div[contains(@class, 'hopscotch-bubble')]//div[contains(@class, 'hopscotch-content')]").findElement(getDriver());
        toolTipText = weToolTip.getText();

        for(String text: searchText)
        {
            if(!toolTipText.contains(text))
            {
                pass = false;
                log("Could not find text: '" + text + "' in the tool tip");
            }
        }

        if(!pass)
        {
            log("Tool tip text: " + toolTipText);
            assertTrue("Tool tip not as expected. See log for missing text.", pass);
        }
    }

    @Test
    public void verifyDensePlotBrushing()
    {
        // This test will only validate that a "Filter" button shows up, but will not validate that the
        // range of the filter is as expected.

        int pointCount, pointToClick;
        CDSHelper cds = new CDSHelper(this);

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);
        ColorAxisVariableSelector coloraxis = new ColorAxisVariableSelector(this);

        log("Brush a single axis plot.");
        // set the y-axis
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.setCellType(CDSHelper.CELL_TYPE_CD8);
        yaxis.confirmSelection();

        // Try to protect from getting an index out of range error.
        pointToClick = getElementCount(Locator.css("div:not(.thumbnail) > svg:nth-of-type(1) a.point"))/4;
        log("Going to click on the " + pointToClick + " element from \"div:not(.thumbnail) > svg:nth-of-type(1) a.point\".");
        brushPlot("div:not(.thumbnail) > svg:nth-of-type(1)", pointToClick, CDSHelper.PlotPoints.POINT, 25, -100, true);

        // Clear the filter.
        cds.clearFilter(1);

        log("Brush a scattered plot.");
        // set the x-axis
        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        xaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        xaxis.setCellType(CDSHelper.CELL_TYPE_CD4);
        xaxis.confirmSelection();

        // Try to protect from getting an index out of range error.
        pointToClick = getElementCount(Locator.css("div:not(.thumbnail) > svg:nth-of-type(2) a.point"))/4;
        brushPlot("div:not(.thumbnail) > svg:nth-of-type(2)", pointToClick, CDSHelper.PlotPoints.POINT, 250, -250, true);

        // Clear the plot.
        cds.clearFilters();
        sleep(500);
        waitForElement(Locator.xpath("//div[contains(@class, 'noplotmsg')][not(contains(@style, 'display: none'))]"));

        // A hacky work around for the scrollIntoView issues I am seeing on Firefox.
        refresh();

        log("Brush a binned plot.");
        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND);
        xaxis.setDataSummaryLevel(CDSHelper.DATA_SUMMARY_PROTEIN);
        xaxis.setProtein(cds.buildIdentifier(CDSHelper.DATA_SUMMARY_PROTEIN_PANEL, "all"));
        xaxis.setCellType("All");
        xaxis.confirmSelection();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        // set the y-axis
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_RAW);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.setCellType("All");
        yaxis.setDataSummaryLevel(CDSHelper.DATA_SUMMARY_PROTEIN);
        yaxis.setProtein(cds.buildIdentifier(CDSHelper.DATA_SUMMARY_PROTEIN_PANEL, "All"));
        yaxis.confirmSelection();

        // Try to protect from getting an index out of range error.
        pointToClick = getElementCount(Locator.css("div:not(.thumbnail) > svg:nth-of-type(1) a.vis-bin-square"))/2;
        brushPlot("div:not(.thumbnail) > svg:nth-of-type(1)", pointToClick, CDSHelper.PlotPoints.BIN, -50, -100, true);

        cds.clearFilters();
        sleep(500);
        waitForElement(Locator.xpath("//div[contains(@class, 'noplotmsg')][not(contains(@style, 'display: none'))]"));

        log("Brush binned plot single axis.");
        // set the y-axis
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.setCellType("All");
        yaxis.confirmSelection();

        // Try to protect from getting an index out of range error.
        pointToClick = getElementCount(Locator.css("div:not(.thumbnail) > svg:nth-of-type(1) a.vis-bin-square"))/2;
        brushPlot("div:not(.thumbnail) > svg:nth-of-type(1)", pointToClick, CDSHelper.PlotPoints.BIN, 0, -50, true);

        // Clear the filter.
        cds.clearFilter(1);

        log("Brush binned with categorical.");

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.SUBJECT_CHARS);
        xaxis.pickVariable(CDSHelper.DEMO_COUNTRY);
        xaxis.confirmSelection();

        // Try to protect from getting an index out of range error.
        pointToClick = getElementCount(Locator.css("div:not(.thumbnail) > svg:nth-of-type(1) a.vis-bin-square"))/3;
        brushPlot("div:not(.thumbnail) > svg:nth-of-type(1)", pointToClick, CDSHelper.PlotPoints.BIN, 0, -50, true);

        // Clear the filter.
        cds.clearFilters();
        sleep(500);

        log("Brush categorical with color.");
        // set the y-axis
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.setCellType(CDSHelper.CELL_TYPE_CD4);
        yaxis.confirmSelection();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.STUDY_TREATMENT_VARS);
        xaxis.pickVariable(CDSHelper.DEMO_TREAT_ARM);
        xaxis.confirmSelection();

        coloraxis.openSelectorWindow();
        coloraxis.pickSource(CDSHelper.SUBJECT_CHARS);
        coloraxis.pickVariable(CDSHelper.DEMO_RACE);
        coloraxis.confirmSelection();

        // Try to protect from getting an index out of range error.
        pointToClick = getElementCount(Locator.css("div:not(.thumbnail) > svg " + CDSHelper.PlotPoints.GLYPH.getTag()))/4;
        brushPlot("div:not(.thumbnail) > svg", pointToClick, CDSHelper.PlotPoints.GLYPH, 0, -50, true);

        // Clear the filter.
        cds.clearFilters();

    }

    @Test
    public void verifyGutterPlotBrushing()
    {
        // This test will only validate that a "Filter" button shows up, but will not validate that the
        // range of the filter is as expected.

        CDSHelper cds = new CDSHelper(this);
        int subjectCountBefore;
        String tempStr;

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);
        ColorAxisVariableSelector coloraxis = new ColorAxisVariableSelector(this);

        log("Test plot with both gutter plots and data in main plot as well.");

        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        yaxis.setCellType(CDSHelper.CELL_TYPE_CD4);
        yaxis.confirmSelection();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        xaxis.setCellType(CDSHelper.CELL_TYPE_CD8);
        xaxis.confirmSelection();

        // Adding color just to make it more interesting.
        coloraxis.openSelectorWindow();
        coloraxis.pickSource(CDSHelper.SUBJECT_CHARS);
        coloraxis.pickVariable(CDSHelper.DEMO_COUNTRY);
        coloraxis.confirmSelection();

        tempStr = getText(Locator.xpath(XPATH_SUBJECT_COUNT));
        subjectCountBefore = Integer.parseInt(tempStr.replaceAll(",", ""));

        gutterPlotBrushingTestHelper(true, true, true, subjectCountBefore, 0);

        // Clean up.
        cds.clearFilters();
        sleep(1000);
        _ext4Helper.waitForMaskToDisappear();

        log("Test plot with x gutter only and data in main plot as well.");

        addRaceFilter(CDSHelper.RACE_BLACK);
        _asserts.assertFilterStatusCounts(829, 48, 1, 1, 154);
        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        yaxis.setCellType(CDSHelper.CELL_TYPE_CD4);
        yaxis.confirmSelection();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_RAW);
        xaxis.setCellType(CDSHelper.CELL_TYPE_CD4, CDSHelper.CELL_TYPE_CD8);
        xaxis.confirmSelection();

        tempStr = getText(Locator.xpath(XPATH_SUBJECT_COUNT));
        subjectCountBefore = Integer.parseInt(tempStr.replaceAll(",", ""));

        gutterPlotBrushingTestHelper(true, false, true, subjectCountBefore, 1);

        // Clean up.
        cds.clearFilters();
        sleep(1000);
        _ext4Helper.waitForMaskToDisappear();

        log("Test plot with y gutter only and data in main plot as well.");

        addRaceFilter(CDSHelper.RACE_BLACK);
        _asserts.assertFilterStatusCounts(829, 48, 1, 1, 154);
        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);
        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ICS);
        yaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_RAW);
        yaxis.setCellType(CDSHelper.CELL_TYPE_CD4, CDSHelper.CELL_TYPE_CD8);
        yaxis.confirmSelection();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.ICS);
        xaxis.pickVariable(CDSHelper.ICS_MAGNITUDE_BACKGROUND_SUB);
        xaxis.setCellType(CDSHelper.CELL_TYPE_CD4);
        xaxis.confirmSelection();

        tempStr = getText(Locator.xpath(XPATH_SUBJECT_COUNT));
        subjectCountBefore = Integer.parseInt(tempStr.replaceAll(",", ""));

        gutterPlotBrushingTestHelper(false, true, true, subjectCountBefore, 1);

        // Clean up.
        cds.clearFilters();
        sleep(1000);
        _ext4Helper.waitForMaskToDisappear();

        log("Test plot with x & y gutter only and no data in main plot as well.");

        yaxis.openSelectorWindow();
        yaxis.pickSource(CDSHelper.ELISPOT);
        yaxis.pickVariable(CDSHelper.ELISPOT_MAGNITUDE_BACKGROUND_SUB);
        yaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        yaxis.confirmSelection();

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.NAB);
        xaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        xaxis.setScale(DataspaceVariableSelector.Scale.Linear);
        xaxis.confirmSelection();
        sleep(500);
        _ext4Helper.waitForMaskToDisappear();

        tempStr = getText(Locator.xpath(XPATH_SUBJECT_COUNT));
        subjectCountBefore = Integer.parseInt(tempStr.replaceAll(",", ""));

        gutterPlotBrushingTestHelper(true, true, false, subjectCountBefore, 0);

        // Clean up.
        cds.clearFilters();
        sleep(1000);
        _ext4Helper.waitForMaskToDisappear();

    }

    // hasXGutter: Does the plot have an x-gutter (i.e. gutter along the bottom).
    // hasYGutter: Does the plot have a y-gutter (i.e. gutter on the left hand side).
    // hasMainPlotDataPoints: Should we expect to find data points in the main plot area
    // subjectCountBefore: What is the subject count before we start brushing.
    // numOfOtherFilters: Have any other filters been applied.
    private void gutterPlotBrushingTestHelper(boolean hasXGutter, boolean hasYGutter, boolean hasMainPlotDataPoints, int subjectCountBefore, int numOfOtherFilters)
    {
        WebElement gutterBrushWindow;
        String dataPointType;
        int heightWidth, pointToClick;
        int mainPlotIndex;
        String tempStr, cssPathBrushWindow;
        CDSHelper.PlotPoints plotPointType;

        refresh();
        sleep(2000);

        if(hasYGutter)
        {
            mainPlotIndex = 2;
            manipulateGutterPlotBrushing(false, mainPlotIndex, subjectCountBefore, numOfOtherFilters);
        }
        else
        {
            mainPlotIndex = 1;
        }

        if (hasXGutter)
        {
            manipulateGutterPlotBrushing(true, mainPlotIndex, subjectCountBefore, numOfOtherFilters);
        }

        log("Brush in main plot area and verify that we don't get a brush window in the gutters.");

        if(hasMainPlotDataPoints)
        {

            // See what kind of data points we have in the main plot.
            if (getElementCount(Locator.css("div.plot:not(.thumbnail) > svg:nth-of-type(" + mainPlotIndex + ") " + CDSHelper.PlotPoints.POINT.getTag())) != 0)
            {
                plotPointType = CDSHelper.PlotPoints.POINT;
            }
            else
            {
                plotPointType = CDSHelper.PlotPoints.BIN;
            }

            dataPointType = plotPointType.getTag();

            // Try to protect from getting an index out of range error. Add one just to make sure that if there is a
            // very small number of points we don't end up with 0 as pointToClick;
            pointToClick = (getElementCount(Locator.css("div:not(.thumbnail) > svg:nth-of-type(" + mainPlotIndex + ") " + dataPointType)) / 4) + 1;
            log("Brushing in the main plot area. Going to click at point: div.plot:not(.thumbnail) > svg:nth-of-type(" + mainPlotIndex + ") " + dataPointType + ":nth-of-type(" + pointToClick + ")");
            brushPlot("div:not(.thumbnail) > svg:nth-of-type(" + mainPlotIndex + ")", pointToClick, plotPointType, 50, -50, false);

        }
        else
        {
            brushEmptyPlot("div:not(.thumbnail) > svg:nth-of-type(" + mainPlotIndex + ")", 100, -100, false);
        }

        if (hasYGutter)
        {
            log("Verify no brush in 'undefined x value' gutter.");
            cssPathBrushWindow = "div.plot:not(.thumbnail) > svg:nth-of-type(1) > g.brush > rect.extent";
            gutterBrushWindow = Locator.css(cssPathBrushWindow).findElement(getDriver());
            tempStr = gutterBrushWindow.getAttribute("height");
            heightWidth = Integer.parseInt(tempStr);
            assertTrue("'undefined x value' gutter has a brush window and it should not.", heightWidth == 0);
        }

        if(hasXGutter)
        {
            log("Verify no brush in 'undefined y value' gutter.");
            cssPathBrushWindow = "div.bottomplot > svg > g.brush > rect.extent";
            gutterBrushWindow = Locator.css(cssPathBrushWindow).findElement(getDriver());
            tempStr = gutterBrushWindow.getAttribute("width");
            heightWidth = Integer.parseInt(tempStr);
            assertTrue("'undefined y value' gutter has a brush window and it should not.", heightWidth == 0);
        }

    }

    // isXGutter: Need to know this so we can find the appropriate svg. If it is an x-gutter (i.e. along the bottom) then use "div.bottomplot > svg" to find it.
    //            Otherwise use "div:not(.thumbnail) > svg:nth-of-type" to find the y-gutter.
    // mainPlotIndex: If there is a y-gutter the main plot will be the second svg in the collection. If there is no y-gutter the main plot will be the first svg.
    // subjectCountBefore: Used only to validate that when a brushing is done, the subject count should go down.
    // numOfOtherFilters: Need to know this when we remove the filter applied by the brushing. An example would be if the test
    //                    filtered on a race before doing the brushing. This will help identify where the brushing filter is in the list.
    private void manipulateGutterPlotBrushing(boolean isXGutter, int mainPlotIndex, int subjectCountBefore, int numOfOtherFilters)
    {
        CDSHelper cds = new CDSHelper(this);
        String cssPathBrushWindow;

        if(isXGutter)
        {
            brushPlot("div.bottomplot > svg > g:nth-child(4) > g.grid-line > path:nth-of-type(2)", -50, 0, false);
        }
        else
        {
            brushPlot("div:not(.thumbnail) > svg:nth-of-type(1) > g:nth-child(5) > g.grid-line > path:nth-of-type(2)", 0, -50, false);
        }

        log("Move the brush window in the 'undefined y value' gutter.");

        if(isXGutter)
        {
            cssPathBrushWindow = "div.bottomplot > svg > g.brush > rect.extent";
            cds.dragAndDropFromElement(Locator.css(cssPathBrushWindow), -100, 0);
        }
        else
        {
            cssPathBrushWindow = "div:not(.thumbnail) > svg:nth-of-type(1) > g.brush > rect.extent";
            cds.dragAndDropFromElement(Locator.css(cssPathBrushWindow), 0, -100);
        }


        sleep(500);

        log("Move the brush window in the main plot.");

        cssPathBrushWindow = "div:not(.thumbnail) > svg:nth-of-type(" + mainPlotIndex + ") > g.brush > rect.extent";
        if(isXGutter)
        {
            cds.dragAndDropFromElement(Locator.css(cssPathBrushWindow), 100, 0);
        }
        else
        {
            cds.dragAndDropFromElement(Locator.css(cssPathBrushWindow), 0, 100);
        }

        sleep(500);

        log("Change the brush window size using the 'handles'.");

        if(isXGutter)
        {
            cssPathBrushWindow = "div.bottomplot > svg > g.brush > g:nth-of-type(1)";
            cds.dragAndDropFromElement(Locator.css(cssPathBrushWindow), -100, 0);
        }
        else
        {
            cssPathBrushWindow = "div:not(.thumbnail) > svg:nth-of-type(1) > g.brush > g:nth-of-type(1)";
            cds.dragAndDropFromElement(Locator.css(cssPathBrushWindow), 0, -100);
        }

        sleep(500);

        if(isXGutter)
        {
            cssPathBrushWindow = "div.bottomplot > svg > g.brush > g:nth-of-type(2)";
            cds.dragAndDropFromElement(Locator.css(cssPathBrushWindow), -100, 0);
        }
        else
        {
            cssPathBrushWindow = "div:not(.thumbnail) > svg:nth-of-type(1) > g.brush > g:nth-of-type(2)";
            cds.dragAndDropFromElement(Locator.css(cssPathBrushWindow), 0, -100);
        }

        log("Move the brush window back to starting point.");

        if(isXGutter)
        {
            cssPathBrushWindow = "div.bottomplot > svg > g.brush > rect.extent";
            cds.dragAndDropFromElement(Locator.css(cssPathBrushWindow), 100, 0);
        }
        else
        {
            cssPathBrushWindow = "div:not(.thumbnail) > svg:nth-of-type(1) > g.brush > rect.extent";
            cds.dragAndDropFromElement(Locator.css(cssPathBrushWindow), 0, 100);
        }

        log("Apply the brushing as a filter.");
        applyBrushAsFilter(subjectCountBefore);

        cds.clearFilter(numOfOtherFilters + 1);
        sleep(1000);
        _ext4Helper.waitForMaskToDisappear();

    }

    private void brushPlot(String cssPathToSvg, int pointIndex, CDSHelper.PlotPoints pointType, int xOffSet, int yOffSet, boolean applyFilter)
    {
        String pointCss;
        pointCss = cssPathToSvg + " " + pointType.getTag() + ":nth-of-type(" + pointIndex + ")";
        brushPlot(pointCss, xOffSet, yOffSet, applyFilter);
    }

    private void brushPlot(String cssPointOfOrigin, int xOffSet, int yOffSet, boolean applyFilter)
    {
        int subjectCountBefore;
        String tempStr;
        Locator plotElement;

        tempStr = getText(Locator.xpath(XPATH_SUBJECT_COUNT));
        subjectCountBefore = Integer.parseInt(tempStr.replaceAll(",", ""));

        // Mouse over the given point.
        plotElement = Locator.css(cssPointOfOrigin);

        sleep(1000);
        cds.dragAndDropFromElement(plotElement, xOffSet, yOffSet);
        sleep(CDSHelper.CDS_WAIT);

        if(applyFilter)
        {
            assertElementVisible(Locator.linkContainingText("Filter"));
            applyBrushAsFilter(subjectCountBefore);
        }

    }

    // Need to special case if trying to brush in an empty plot.
    private void brushEmptyPlot(String cssPathToPlot, int xOffset, int yOffset, boolean applyFilter)
    {
        int subjectCountBefore;
        String tempStr;

        tempStr = getText(Locator.xpath(XPATH_SUBJECT_COUNT));
        subjectCountBefore = Integer.parseInt(tempStr.replaceAll(",", ""));

        // Going to move the mouse over the area where it is about to start dragging.
        clickAt(Locator.css(cssPathToPlot), 1, 1, 0);

        sleep(1000);
        cds.dragAndDropFromElement(Locator.css(cssPathToPlot), xOffset, yOffset);
        sleep(CDSHelper.CDS_WAIT);

        if(applyFilter)
        {
            assertElementVisible(Locator.linkContainingText("Filter"));
            applyBrushAsFilter(subjectCountBefore);
        }

    }

    private void applyBrushAsFilter(int subjectCountBefore)
    {
        int subjectCountAfter;
        String tempStr;

        assertElementVisible(Locator.linkContainingText("Filter"));

        click(Locator.linkContainingText("Filter"));
        sleep(1000); // Wait briefly for the mask to show up.
        _ext4Helper.waitForMaskToDisappear();

        tempStr = getText(Locator.xpath(XPATH_SUBJECT_COUNT));
        subjectCountAfter = Integer.parseInt(tempStr.replaceAll(",", ""));

        assertTrue("The subject count after applying filter was not less than or equal to before. Before: " + subjectCountBefore + " After: " + subjectCountAfter, subjectCountBefore >= subjectCountAfter);
        sleep(1000); // Wait briefly for the mask to show up.
        _ext4Helper.waitForMaskToDisappear();

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
            waitForElement(Locator.xpath("//div[contains(@class, 'selectionpanel')]//div[contains(@class, 'activefilter')]//div[contains(@class, 'selitem')]//div[contains(text(), '" + axes[0] + "')]"));

            if (axes.length > 1)
            {
                Actions builder = new Actions(getDriver());
                builder.keyDown(multiSelectKey).build().perform();

                for (int i = 1; i < axes.length; i++)
                {
                    click(Locators.plotTick.withText(axes[i]));
                    waitForElement(Locator.xpath("//div[contains(@class, 'selectionpanel')]//div[contains(@class, 'activefilter')]//div[contains(@class, 'selitem')]//div[contains(text(), '" + axes[i] + "')]"));
                }
                builder.keyUp(multiSelectKey).build().perform();
            }
    }

    private int getPointCountByColor(String colorCode)
    {
        return getElementCount(Locator.css("svg g a.point path[fill='" + colorCode + "']"));
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
        return hasGutter("div.bottomplot svg");
    }

    private boolean hasGutter(String cssPath){

        boolean hasElement;

        try
        {
            waitForElement(Locator.css(cssPath), 3000);
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

    private boolean hasXLogGutter()
    {
        return hasLogGutter(0);
    }

    private boolean hasYLogGutter()
    {
        return hasLogGutter(1);
    }

    private boolean hasLogGutter(int axisIndex)
    {
        int mainPlotIndex;
        String cssMainPlotWindow;
        List<WebElement> axisElements;
        WebElement mainPlot;
        boolean isPresent;

        if(hasYGutter())
        {
            mainPlotIndex = 2;
        }
        else {
            mainPlotIndex = 1;
        }

        // There will always be two g.axis elements. One horizontal the other vertical.
        cssMainPlotWindow = "div:not(.thumbnail) > svg:nth-of-type(" + mainPlotIndex + ") > g.axis";
        axisElements = Locator.css(cssMainPlotWindow).toBy().findElements(getDriver());
        try
        {
            axisElements.get(axisIndex).findElement(Locator.css("g.log-gutter").toBy());
            isPresent = true;
        }
        catch(org.openqa.selenium.NoSuchElementException ex)
        {
            isPresent = false;
        }

        return isPresent;
    }

    private void validateVisitCounts(List<WebElement> studies, Map<String, CDSHelper.TimeAxisData> expectedCounts)
    {

        for (WebElement study : studies)
        {
            CDSHelper.TimeAxisData tad = expectedCounts.get(study.findElement(Locator.css("text.study-label").toBy()).getAttribute("test-data-value"));

            // If tad is null it means we don't want to check the totals for the given study (or a locator is messed up).
            if (tad != null)
            {

                int nonvacCount = 0, nonvacCountNoData = 0, vacCount = 0, vacCountNoData = 0, chalCount = 0, chalCountNoData = 0;
                List<WebElement> visits;
                WebElement preEnrollment;

                log("Study Name: '" + study.getText() + "' ID: " + study.findElement(Locator.css("text.study-label").toBy()).getAttribute("test-data-value"));
                visits = study.findElements(Locator.css("image.visit-tag").toBy());
                log("Number of visits: " + visits.size());

                // Had hoped to get a collection directly, but had trouble getting css to see the href value.
                // So went with this approach for now. May revisit later.
                for (int i=0; i < visits.size(); i++)
                {
                    if (visits.get(i).getAttribute("href").contains("/nonvaccination_normal.svg"))
                    {
                        nonvacCount++;
                    }
                    if (visits.get(i).getAttribute("href").contains("/nonvaccination_disabled.svg"))
                    {
                        nonvacCountNoData++;
                    }
                    if (visits.get(i).getAttribute("href").contains("/vaccination_normal.svg"))
                    {
                        vacCount++;
                    }
                    if (visits.get(i).getAttribute("href").contains("/vaccination_disabled.svg"))
                    {
                        vacCountNoData++;
                    }
                    if (visits.get(i).getAttribute("href").contains("/challenge_normal.svg"))
                    {
                        chalCount++;
                    }
                    if (visits.get(i).getAttribute("href").contains("/challenge_disabled.svg"))
                    {
                        chalCountNoData++;
                    }
                }

                log("Vaccination Count: " + vacCount);
                log("Vaccination NoData Count: " + vacCountNoData);
                log("Non-Vaccination Count: " + nonvacCount);
                log("Non-Vaccination NoData Count: " + nonvacCountNoData);
                log("Challenge Count: " + chalCount);
                log("Challenge NoData Count: " + chalCountNoData);

                assertTrue("Vaccination count not as expected. Expected: " + tad.vaccinationCount + " found: " + vacCount, tad.vaccinationCount == vacCount);
                assertTrue("Vaccination NoDatat count not as expected. Expected: " + tad.vaccinationCountNoData + " found: " + vacCountNoData, tad.vaccinationCountNoData == vacCountNoData);
                assertTrue("Nonvaccination count not as expected. Expected: " + tad.nonvaccinationCount + " found: " + nonvacCount, tad.nonvaccinationCount == nonvacCount);
                assertTrue("Nonvaccination NoDatat count not as expected. Expected: " + tad.nonvaccinationCountNoData + " found: " + nonvacCountNoData, tad.nonvaccinationCountNoData == nonvacCountNoData);
                assertTrue("Challenge count not as expected. Expected: " + tad.challengeCount + " found: " + chalCount, tad.challengeCount == chalCount);
                assertTrue("Challenge NoDatat count not as expected. Expected: " + tad.challengeCountNoData + " found: " + chalCountNoData, tad.challengeCountNoData == chalCountNoData);

                log("Visit counts as expected.");

            }
            else
            {
                log("Not validating counts for " + study.getText() + " (" + study.findElement(Locator.css("text.study-label").toBy()).getAttribute("test-data-value") + ")");
            }

        }
    }

    private void timeAxisToolTipsTester(String cssVisit, List<String> expectedToolTipText)
    {
        String actualToolTipText, condensedActual, condensedExpected;

        scrollIntoView(Locator.css(cssVisit));
        mouseOver(Locator.css(cssVisit));
        sleep(1000);
        log(getAttribute(Locator.css(cssVisit), "href"));

        assertTrue("Tool-tip was not present.", waitForElement(Locator.xpath("//div[contains(@class, 'hopscotch-bubble')]"), CDSHelper.CDS_WAIT_TOOLTIP, true));

        actualToolTipText = getText(Locator.xpath("//div[contains(@class, 'hopscotch-bubble')]"));

        // Modify the strings to make the comparisons less susceptible to spaces, tabs, /n, etc... and capitalization.
        condensedActual = actualToolTipText.toLowerCase().replaceAll("\\s+", "");

        // Order of text in tool-tip may change from deployment to deployment. So look only from specific text as oppose to looking for an exact match.
        for (String strTemp : expectedToolTipText)
        {
            condensedExpected = strTemp.toLowerCase().replaceAll("\\s+", "");
            assertTrue("Item not found in tool tip. Expected: '" + strTemp + "' (" + condensedExpected + "), actual: '" + actualToolTipText + "' (" + condensedActual + ").", condensedActual.contains(condensedExpected));
        }

    }

    // Return all images from the Time Axis.
    private List<WebElement> findTimeAxisPointsWithData()
    {
        return findTimeAxisPointsWithData("div.bottomplot > svg > g > image");
    }

    // Return all images from a specific part of the Time Axis.
    private List<WebElement> findTimeAxisPointsWithData(String cssPath)
    {
        List<WebElement> list;
        cssPath = "div.bottomplot > svg > g > image"; //[xlink:href$='nonvaccination_normal.svg']";
        list = findTimeAxisPointsWithData(cssPath, "nonvaccination_normal.svg");
        list.addAll(findTimeAxisPointsWithData(cssPath, "vaccination_normal.svg"));
        list.addAll(findTimeAxisPointsWithData(cssPath, "challenge_normal.svg"));

        return list;

    }

    private List<WebElement> findTimeAxisPointsWithData(String cssPath, String imageValue)
    {
        List<WebElement> allImages = Locator.css(cssPath).findElements(getDriver());
        List<WebElement> imgWithData = new ArrayList<>();
        String href;
        for(WebElement img : allImages)
        {
            href = img.getAttribute("href");
            if(href.contains(imageValue))
            {
                imgWithData.add(img);
            }
        }

        return imgWithData;
    }

    private int getTimeAxisPointCountByImage(String image)
    {
        return findTimeAxisPointsWithData("div.bottomplot > svg > g > image", image).size();
    }


    private void subjectCountsHelper(Map<String, String> sourcesSubjectCounts, Map<String, String> antigenCounts,
                                     Map<String, String> peptidePoolCounts, Map<String, String> proteinCounts,
                                     Map<String, String> proteinPanelCounts, Map<String, String> virusCounts)
    {

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);
        ColorAxisVariableSelector coloraxis = new ColorAxisVariableSelector(this);

        log("Validating the x-axis sources.");

        xaxis.openSelectorWindow();


        if (sourcesSubjectCounts != null)
        {
//        Locator.XPathLocator source;
            for (Map.Entry<String, String> entry : sourcesSubjectCounts.entrySet())
            {
                // TODO Would rather test with the commented code (more complete test). However there is an issue if a text value has a &nbsp; the xpath below fails to work, although it works correct in chrome debugger.
//            source = xaxis.xpathWindow().append("//div[contains(@class, 'content-label')][translate(text(), '\\xA0', ' ')='" + entry.getKey() + "']");
//            assertTrue(isElementVisible(source));
//            assertTrue(isElementVisible(source.append("/./following-sibling::div[text()='" + entry.getValue() + "']")));
                assertTrue(isElementVisible(xaxis.window().append(" div.content-label").withText(entry.getKey())));
                assertTrue(isElementVisible(xaxis.window().append(" div.content-count").withText(entry.getValue()))); // TODO Bad test. It will pass if there is any tag wtih this count. Need to revisit.
            }
        }

        if (antigenCounts != null)
        {
            log("Validating subject counts in the x-axis BAMA - Antigen.");
            xaxis.pickSource(CDSHelper.BAMA);
            sleep(CDSHelper.CDS_WAIT_ANIMATION);
            xaxis.setIsotype("IgG");
            xaxis.validateAntigenSubjectCount(antigenCounts, false);
            xaxis.backToSource();
        }

        if (peptidePoolCounts != null)
        {
            log("Validating subject counts in the x-axis ELISPOT - Peptide Pool.");
            xaxis.pickSource(CDSHelper.ELISPOT);
            xaxis.validatePeptidePoolSubjectCount(peptidePoolCounts, false);
            xaxis.backToSource();
        }

        if (proteinPanelCounts != null)
        {
            log("Validating subject counts in the x-axis ICS - Protein Panel.");
            xaxis.pickSource(CDSHelper.ICS);
            xaxis.setDataSummaryLevel(CDSHelper.ICS_PROTEIN_PANEL);
            xaxis.validateProteinPanelSubjectCount(proteinPanelCounts, false);
            xaxis.backToSource();
        }

        if (proteinCounts != null)
        {
            log("Validating subject counts in the x-axis ICS - Protein.");
            xaxis.pickSource(CDSHelper.ICS);
            xaxis.setDataSummaryLevel(CDSHelper.ICS_PROTEIN);
            xaxis.validateProteinSubjectCount(proteinCounts, false);
            xaxis.backToSource();
        }

        if (virusCounts != null)
        {
            log("Validating subject counts in the x-axis NAB - Virus.");
            xaxis.pickSource(CDSHelper.NAB);
            xaxis.pickVariable(CDSHelper.NAB_TITERIC50);
            xaxis.validateVirusSubjectCount(virusCounts, true);
        }
        else
        {
            xaxis.cancelSelection();
        }

        log("Validating the y-axis source.");
        yaxis.openSelectorWindow();

        if (sourcesSubjectCounts != null)
        {
            for (Map.Entry<String, String> entry : sourcesSubjectCounts.entrySet())
            {
                if (entry.getKey().compareTo(CDSHelper.STUDY_TREATMENT_VARS) != 0 && entry.getKey().compareTo(CDSHelper.TIME_POINTS) != 0)
                {
//            source = xaxis.xpathWindow().append("//div[contains(@class, 'content-label')][translate(text(), '\\xA0', ' ')='" + entry.getKey() + "']");
//            assertTrue(isElementVisible(source));
//            assertTrue(isElementVisible(source.append("/./following-sibling::div[text()='" + entry.getValue() + "']")));
                    assertTrue(isElementVisible(yaxis.window().append(" div.content-label").withText(entry.getKey())));
                    assertTrue(isElementVisible(yaxis.window().append(" div.content-count").withText(entry.getValue())));
                }
            }
        }

        if (antigenCounts != null)
        {
            log("Validating subject counts in the y-axis BAMA - Antigen.");
            yaxis.pickSource(CDSHelper.BAMA);
            yaxis.setIsotype("IgG");
            yaxis.validateAntigenSubjectCount(antigenCounts, false);
            yaxis.backToSource();
        }

        if (peptidePoolCounts != null)
        {
            log("Validating subject counts in the y-axis ELISPOT - Peptide Pool.");
            yaxis.pickSource(CDSHelper.ELISPOT);
            yaxis.validatePeptidePoolSubjectCount(peptidePoolCounts, false);
            yaxis.backToSource();
        }

        if (proteinCounts != null)
        {
            log("Validating subject counts in the y-axis ICS - Protein.");
            yaxis.pickSource(CDSHelper.ICS);
            yaxis.setDataSummaryLevel(CDSHelper.ICS_PROTEIN);
            yaxis.validateProteinSubjectCount(proteinCounts, false);
            yaxis.backToSource();
        }

        if (proteinPanelCounts != null)
        {
            log("Validating subject counts in the y-axis ICS - Protein Panel.");
            yaxis.pickSource(CDSHelper.ICS);
            yaxis.setDataSummaryLevel(CDSHelper.ICS_PROTEIN_PANEL);
            yaxis.validateProteinPanelSubjectCount(proteinPanelCounts, false);
            yaxis.backToSource();
        }

        if (virusCounts != null)
        {
            log("Validating subject counts in the y-axis NAB - Virus.");
            yaxis.pickSource(CDSHelper.NAB);
            yaxis.validateVirusSubjectCount(virusCounts, true);
        }
        else
        {
            yaxis.cancelSelection();
        }

        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        if (sourcesSubjectCounts != null)
        {

            log("Validating the color-axis source.");
            coloraxis.openSelectorWindow();

            for (Map.Entry<String, String> entry : sourcesSubjectCounts.entrySet())
            {
                if (entry.getKey().compareTo(CDSHelper.TIME_POINTS) != 0)
                {
//            source = xaxis.xpathWindow().append("//div[contains(@class, 'content-label')][translate(text(), '\\xA0', ' ')='" + entry.getKey() + "']");
//            assertTrue(isElementVisible(source));
//            assertTrue(isElementVisible(source.append("/./following-sibling::div[text()='" + entry.getValue() + "']")));
                    assertTrue(isElementVisible(coloraxis.window().append(" div.content-label").withText(entry.getKey())));
                    assertTrue(isElementVisible(coloraxis.window().append(" div.content-count").withText(entry.getValue())));
                }
            }

            coloraxis.cancelSelection();
        }

    }

    @LogMethod
    private void createParticipantGroups()
    {
        Ext4Helper.resetCssPrefix();
        beginAt("project/" + getProjectName() + "/begin.view?");
        _studyHelper.createCustomParticipantGroup(getProjectName(), getProjectName(), PGROUP1, "Subject", "039-016", "039-014");
        _studyHelper.createCustomParticipantGroup(getProjectName(), getProjectName(), PGROUP2, "Subject", "039-044", "039-042");
        _studyHelper.createCustomParticipantGroup(getProjectName(), getProjectName(), PGROUP3, "Subject", "039-059", "039-060");
        _studyHelper.createCustomParticipantGroup(getProjectName(), getProjectName(), PGROUP3_COPY, "Subject", "039-059", "039-060");
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
        public static Locator plotTickLinear = Locator.css("g.tick-text > g > text");
        public static Locator plotTick = Locator.css("g.tick-text > a > text");
        public static Locator plotPoint = Locator.css("svg a.point");
        public static Locator plotSquare = Locator.css("svg a.vis-bin-square");
        public static Locator filterDataButton = Locator.xpath("//span[text()='Filter']");
        public static Locator removeButton = Locator.xpath("//span[text()='Remove']");
    }
}