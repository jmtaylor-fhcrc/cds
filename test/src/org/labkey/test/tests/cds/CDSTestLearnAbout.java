package org.labkey.test.tests.cds;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.test.Locator;
import org.labkey.test.pages.LabKeyPage;
import org.labkey.test.pages.cds.LearnGrid;
import org.labkey.test.util.cds.CDSAsserts;
import org.labkey.test.util.cds.CDSHelper;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Category({})
public class CDSTestLearnAbout extends CDSReadOnlyTest
{
    private final CDSHelper cds = new CDSHelper(this);
    private final CDSAsserts _asserts = new CDSAsserts(this);
    private final String MISSING_SEARCH_STRING = "If this string ever appears something very odd happened.";
    private final String XPATH_TEXTBOX = "//table[contains(@class, 'learn-search-input')]//tbody//tr//td//input";
    private final Locator XPATH_RESULT_ROW_TITLE = LearnGrid.Locators.lockedRow;
    private final Locator XPATH_RESULT_ROW_DATA = LearnGrid.Locators.unlockedRow;

    @Before
    public void preTest()
    {

        cds.enterApplication();
        cds.ensureNoFilter();
        cds.ensureNoSelection();

        // go back to app starting location
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

    @Test
    public void testLearnAboutStudies()
    {
        cds.viewLearnAboutPage("Studies");

        List<String> studies = Arrays.asList(CDSHelper.STUDIES);
        _asserts.verifyLearnAboutPage(studies);
    }

    @Test
    public void clickOnLearnAboutStudyItem()
    {
        List<WebElement> returnedItems;
        String[] itemParts;

        cds.viewLearnAboutPage("Studies");
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        returnedItems = XPATH_RESULT_ROW_TITLE.findElements(getDriver());

        int index = returnedItems.size()/2;

        scrollIntoView(returnedItems.get(index));

        itemParts = returnedItems.get(index).getText().split("\n");
        returnedItems.get(index).click();

        log("Validating title is " + itemParts[0]);
        shortWait().until(ExpectedConditions.visibilityOfElementLocated(Locator.xpath("//div[contains(@class, 'learnheader')]//div//span[text()='" + itemParts[0] + "']").toBy()));

        log("Validating Study Type is: " + itemParts[1]);
        assert(Locator.xpath("//table[contains(@class, 'learn-study-info')]//tbody//tr//td[contains(@class, 'item-value')][text()='" + itemParts[1] + "']").findElement(getDriver()).isDisplayed());

        log("Validating return link works.");
        click(Locator.xpath("//div[contains(@class, 'learn-up')]/span[contains(@class, 'breadcrumb')][text()='Studies / ']"));

        shortWait().until(ExpectedConditions.visibilityOfElementLocated(Locator.xpath("//div[contains(@class, 'title')][text()='Learn about...']").toBy()));
    }

    @Test
    public void testLearnAboutStudiesSearch()
    {
        List<String> searchStrings = new ArrayList<>(Arrays.asList("Proin", "ACETAMINOPHEN", "Phase IIB"));

        cds.viewLearnAboutPage("Studies");

        searchStrings.stream().forEach((searchString) -> validateSearchFor(searchString));

        log("Searching for a string '" + MISSING_SEARCH_STRING + "' that should not be found.");
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        this.setFormElement(Locator.xpath(XPATH_TEXTBOX), MISSING_SEARCH_STRING);
        sleep(CDSHelper.CDS_WAIT);
        _asserts.verifyEmptyLearnAboutStudyPage();

    }

    @Test
    public void verifyLearnAboutStudyDetails()
    {
        final String searchString = "ZAP 117";
        final String grantAffiliation = "Nulla tellus. In sagittis dui vel nisl.";
        final String firstContactName = "Helen Holmes";
        final String firstContactEmail = "hholmest@alexa.com";
        final String rationale = "In sagittis dui vel nisl.";

        cds.viewLearnAboutPage("Studies");
        log("Searching for '" + searchString + "'.");
        this.setFormElement(Locator.xpath(XPATH_TEXTBOX), searchString);
        sleep(CDSHelper.CDS_WAIT);

        log("Verifying data availability on summary page.");
        LearnGrid learnGrid = new LearnGrid(this);
        int rowCount = learnGrid.getRowCount();
        Assert.assertTrue("Expected one row in the grid, found " + rowCount + " row(s).", rowCount == 1);
        Assert.assertTrue("Row did not contain " + searchString, learnGrid.getRowText(0).contains(searchString));

        log("Start verifying study detail page.");
        learnGrid.getCellWebElement(0, 0).click();
        sleep(CDSHelper.CDS_WAIT);

        log("Verifying study information.");
        List<String> fields = Arrays.asList(CDSHelper.LEARN_ABOUT_ZAP117_INFO_FIELDS);
        fields.stream().forEach((field) -> assertTextPresent(field));
        assertTextPresent(grantAffiliation);

        log("Verifying contact information.");
        fields = Arrays.asList(CDSHelper.LEARN_ABOUT_CONTACT_FIELDS);
        fields.stream().forEach((field) -> assertTextPresent(field));

        assertElementPresent(Locator.xpath("//a[contains(@href, 'mailto:" + firstContactEmail + "')][text()='" + firstContactName + "']"));

        log("Verifying description section.");
        fields = Arrays.asList(CDSHelper.LEARN_ABOUT_DESCRIPTION_FIELDS);
        fields.stream().forEach((field) -> assertTextPresent(field));
        assertTextPresent(rationale);
        assertElementPresent(Locator.xpath("//a[text()='Click for treatment schema']"));

        validateToolTip(Locator.linkWithText("NAB").findElement(getDriver()), "provided, but not included");

        validateToolTip(Locator.linkWithText("ICS").findElement(getDriver()), "pending study completion");

        validateToolTip(Locator.linkWithText("BAMA").findElement(getDriver()), "Status not available");

    }

    @Test
    public void testLearnAboutStudyProducts()
    {
        log("Extra logging to record time stamps.");
        cds.viewLearnAboutPage("Study products");
        log("Should now be on the Learn About - Study Products page.");
        sleep(30000);
        log("Should have slept for 30 seconds.");
        refresh();
        log("Page was refreshed.");
        sleep(30000);
        log("Should have slept for another 30 seconds. Now wait at most 60 seconds for the page signal to fire.");
        waitForElement(LabKeyPage.Locators.pageSignal("determinationLearnAboutStudyProductLoaded"), 60000, false);
        log("Signal should have fired. Now wait, at most, 60 seconds for an h2 element with the text 'verapamil hydrochloride'");
        waitForElement(Locator.xpath("//h2").withText("verapamil hydrochloride"), 60000);
        log("Element should be there.");
//        longWait().until(ExpectedConditions.visibilityOfElementLocated(Locator.xpath("//div[contains(@class, 'learnview')]//span//div//div[contains(@class, 'learnstudyproducts')]//div[contains(@class, 'learncolumnheader')]").toBy()));

        List<String> studyProducts = Arrays.asList(CDSHelper.PRODUCTS);
        _asserts.verifyLearnAboutPage(studyProducts);
    }

    @Test
    public void clickOnLearnAboutStudyProductsItem()
    {
        List<WebElement> lockedColItems;
        List<WebElement> freeColItems;

        // This code was put in place because we were seeing failure in TeamCity where the page wasn't loading.
        // The TeamCity configuration has been changed to use chrome which looks like it addressed this issue. Going to remove some of these lines for now.
//        log("Extra logging to record time stamps.");
//        cds.viewLearnAboutPage("Study products");
//        log("Should now be on the Learn About - Study Products page.");
//        sleep(10000);
//        log("Should have slept for 10 seconds.");
        refresh();
        log("Page was refreshed.");
        sleep(10000);
        log("Should have slept for another 10 seconds. Now wait at most 30 seconds for the page signal to fire.");
        waitForElement(LabKeyPage.Locators.pageSignal("determinationLearnAboutStudyProductLoaded"), 30000, false);
        log("Signal should have fired. Now wait, at most, 30 seconds for an h2 element with the text 'verapamil hydrochloride'");
        waitForElement(Locator.xpath("//h2").withText("verapamil hydrochloride"), 30000);
        log("Element should be there.");
        lockedColItems = XPATH_RESULT_ROW_TITLE.findElements(getDriver());
        freeColItems = XPATH_RESULT_ROW_DATA.findElements(getDriver());

        //Because learngrid has a locked column is actually rendered as two grids.
        int listSize = lockedColItems.size();
        int index = listSize / 2;

        scrollIntoView(lockedColItems.get(index));

        String itemTitle = lockedColItems.get(index).getText().split("\n")[0];
        String[] itemClassAndType = freeColItems.get(index).getText().split("\n");

        log("Looking for product: " + itemTitle + " in a list of " + listSize);
        longWait().until(ExpectedConditions.visibilityOf(lockedColItems.get(index)));
        lockedColItems.get(index).click();

        log("Validating title is " + itemTitle);
        longWait().until(ExpectedConditions.visibilityOfElementLocated(Locator.xpath("//div[contains(@class, 'learnheader')]//div//span[text()='" + itemTitle + "']").toBy()));

        log("Validating Product Type is: " + itemClassAndType[0]);
        assert(Locator.xpath("//table[contains(@class, 'learn-study-info')]//tbody//tr//td[contains(@class, 'item-value')][text()='" + itemClassAndType[0] + "']").findElement(getDriver()).isDisplayed());

        log("Validating Class is: " + itemClassAndType[1]);
        assert(Locator.xpath("//table[contains(@class, 'learn-study-info')]//tbody//tr//td[contains(@class, 'item-value')][text()='" + itemClassAndType[0] + "']").findElement(getDriver()).isDisplayed());

        log("Validating return link works.");
        click(Locator.xpath("//div[contains(@class, 'learn-up')]/span[contains(@class, 'breadcrumb')][text()='Study products / ']"));

        shortWait().until(ExpectedConditions.visibilityOfElementLocated(Locator.xpath("//div[contains(@class, 'title')][text()='Learn about...']").toBy()));
    }

    @Test
    public void testLearnAboutStudyProductsSearch()
    {
        List<String> searchStrings = new ArrayList<>(Arrays.asList("Pénélope", "acid", "ART", "is a"));

        cds.viewLearnAboutPage("Study products");

        searchStrings.stream().forEach((searchString) -> validateSearchFor(searchString));

        log("Searching for a string '" + MISSING_SEARCH_STRING + "' that should not be found.");
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        this.setFormElement(Locator.xpath(XPATH_TEXTBOX), MISSING_SEARCH_STRING);
        sleep(CDSHelper.CDS_WAIT);
        _asserts.verifyEmptyLearnAboutStudyProductsPage();
    }

    @Test
    public void testLearnAboutAssays()
    {
        cds.viewLearnAboutPage("Assays");
        List<String> assays = Arrays.asList(CDSHelper.ASSAYS_FULL_TITLES);
        _asserts.verifyLearnAboutPage(assays); // Until the data is stable don't count the assay's shown.

        waitAndClick(Locator.tagWithClass("tr", "detail-row").append("/td//div/div/h2").containing(assays.get(0)));
        waitForElement(Locator.tagWithClass("span", "breadcrumb").containing("Assays /"));
        assertTextPresent(CDSHelper.LEARN_ABOUT_BAMA_ANALYTE_DATA);

        //testing variables page
        waitAndClick(Locator.tagWithClass("h1", "lhdv").withText("Variables"));
        waitForElement(Locator.xpath("//div[contains(@class, 'list-entry-container')]//div[@class='list-entry-title']//h2[text()='Vaccine matched indicator']"));
        assertTextPresent(CDSHelper.LEARN_ABOUT_BAMA_VARIABLES_DATA);

        refresh();

        waitForElement(Locator.xpath("//div[contains(@class, 'list-entry-container')]//div[@class='list-entry-title']//h2[text()='Vaccine matched indicator']"));
        assertTextPresent(CDSHelper.LEARN_ABOUT_BAMA_VARIABLES_DATA);

        //testing BAMA antigens page
        waitAndClick(Locator.tagWithClass("h1", "lhdv").withText("Antigens"));
        waitForElement(Locator.tagWithClass("div", "list-title-bar").append("/div").containing("Antigen"));
        waitForElement(Locator.xpath("//div[@class='list-detail-text']//h2[text()='p24']"));
        assertTextPresent(CDSHelper.LEARN_ABOUT_BAMA_ANTIGEN_DATA);

        refresh(); //refreshes are necessary to clear previously viewed tabs from the DOM.

        //testing ICS antigens page
        waitAndClick(Locator.tagWithClass("span", "breadcrumb").containing("Assays /"));
        waitAndClick(Locator.tagWithClass("tr", "detail-row").append("/td//div/div/h2").containing(assays.get(1)));
        waitForElement(Locator.tagWithClass("span", "breadcrumb").containing("Assays /"));
        waitForElement(Locator.xpath("//h3[text()='Endpoint description']"));

        validateToolTip(Locator.linkWithText("RED 4").findElement(getDriver()), "not approved for sharing");
        validateToolTip(Locator.linkWithText("RED 6").findElement(getDriver()), "not approved for sharing");
        validateToolTip(Locator.tagWithText("span", "w101").findElement(getDriver()), "added");
        validateToolTip(Locator.linkWithText("ZAP 102").findElement(getDriver()), "Status not available");
        validateToolTip(Locator.linkWithText("ZAP 108").findElement(getDriver()), "provided, but not included");
        validateToolTip(Locator.linkWithText("ZAP 115").findElement(getDriver()), "being processed");
        validateToolTip(Locator.linkWithText("ZAP 117").findElement(getDriver()), "pending study completion");

        refresh();

        waitAndClick(Locator.tagWithClass("h1", "lhdv").withText("Antigens"));
        waitForElement(Locator.tagWithClass("div", "list-title-bar").append("/div").containing("Protein Panel"));
        waitForText(CDSHelper.LEARN_ABOUT_ICS_ANTIGEN_TAB_DATA[0]);
        assertTextPresent(CDSHelper.LEARN_ABOUT_ICS_ANTIGEN_TAB_DATA);
    }

    @Test
    public void validateSearchNavigation()
    {
        final String STUDIES_LINK = "//h1[@class='lhdv'][text()='Studies']";
        final String ASSAYS_LINK = "//h1[@class='lhdv'][text()='Assays']";
        final String PRODUCTS_LINK = "//h1[@class='lhdv'][text()='Study products']";
        final String LEARN_ABOUT = "//span[contains(@class, 'right-label')][text()='Learn about']";
        final String BACK_BUTTON = "//div[contains(@class, 'learnview')]/span/div/div[contains(@class, 'x-container')][not(contains(@style, 'display: none'))]//div[contains(@class, 'learn-up')]//span[contains(@class, 'iarrow')]";

        String searchTextStudies, searchTextAssays, searchTextProducts;
        List<WebElement> returnedItems;

        cds.viewLearnAboutPage("Studies");

        searchTextStudies = "Proin leo odio, porttitor id";
        log("Search for '" + searchTextStudies + "' in Studies");
        this.setFormElement(Locator.xpath(XPATH_TEXTBOX), searchTextStudies);
        waitForElement(XPATH_RESULT_ROW_TITLE);

        log("Go to the detail page of the item returned.");
        returnedItems  = XPATH_RESULT_ROW_TITLE.findElements(getDriver());
        returnedItems.get(0).click();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        log("Click back button to validate that the search value is saved.");
        Locator.xpath(BACK_BUTTON).findElement(getDriver()).click();
        waitForText("Learn about...");
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        Assert.assertTrue(Locator.xpath(XPATH_TEXTBOX).findElement(getDriver()).isDisplayed());
        Assert.assertTrue(searchTextStudies.equals(this.getFormElement(Locator.xpath(XPATH_TEXTBOX))));

        log("Click 'Learn about' and validate that the text box gets cleared.");
        click(Locator.xpath(LEARN_ABOUT));
        waitForText("Learn about...");
        sleep(CDSHelper.CDS_WAIT);
        Assert.assertTrue(Locator.xpath(XPATH_TEXTBOX).findElement(getDriver()).isDisplayed());
        Assert.assertTrue(this.getFormElement(Locator.xpath(XPATH_TEXTBOX)).length() == 0);

        log("Search in Studies again to give it a history...");
        searchTextStudies = "Oxygen";
        log("Search for '" + searchTextStudies + "' in Studies.");
        this.setFormElement(Locator.xpath(XPATH_TEXTBOX), searchTextStudies);
        sleep(CDSHelper.CDS_WAIT);

        log("Go to the detail page of one of the items returned.");
        returnedItems.clear();
        returnedItems  = XPATH_RESULT_ROW_TITLE.findElements(getDriver());
        returnedItems.get(0).click();
        sleep(CDSHelper.CDS_WAIT);

        log("Again click the back button to save the search value. It will be checked again in a little while.");
        Locator.xpath(BACK_BUTTON).findElement(getDriver()).click();
        waitForText("Learn about...");
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        Assert.assertTrue(Locator.xpath(XPATH_TEXTBOX).findElement(getDriver()).isDisplayed());
        Assert.assertTrue(searchTextStudies.equals(this.getFormElement(Locator.xpath(XPATH_TEXTBOX))));

        log("Go to Assays and try the same basic scenario.");
        click(Locator.xpath(ASSAYS_LINK));
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        searchTextAssays = "NAB";
        log("Search for '" + searchTextAssays + "' in Assays");
        this.setFormElement(Locator.xpath(XPATH_TEXTBOX), searchTextAssays);
        sleep(CDSHelper.CDS_WAIT);

        log("Go to the detail page for " + searchTextAssays + ".");
        returnedItems.clear();
        returnedItems  = XPATH_RESULT_ROW_TITLE.findElements(getDriver());
        returnedItems.get(0).click();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        log("Click back button to validate that the search value is saved.");
        Locator.xpath(BACK_BUTTON).findElement(getDriver()).click();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        Assert.assertTrue(Locator.xpath(XPATH_TEXTBOX).findElement(getDriver()).isDisplayed());
        Assert.assertTrue(searchTextAssays.equals(this.getFormElement(Locator.xpath(XPATH_TEXTBOX))));

        log("Go to Study products and try the same basic scenario.");
        click(Locator.xpath(PRODUCTS_LINK));
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        searchTextProducts = "M\u00E5ns";
        log("Search for '" + searchTextProducts + "' in Products");
        this.setFormElement(Locator.xpath(XPATH_TEXTBOX), searchTextProducts);
        sleep(CDSHelper.CDS_WAIT);

        log("Go to the detail page for " + searchTextProducts + ".");
        returnedItems.clear();
        returnedItems  = XPATH_RESULT_ROW_TITLE.findElements(getDriver());
        returnedItems.get(0).click();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        log("Click back button to validate that the search value is saved.");
        Locator.xpath(BACK_BUTTON).findElement(getDriver()).click();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        Assert.assertTrue(Locator.xpath(XPATH_TEXTBOX).findElement(getDriver()).isDisplayed());
        Assert.assertTrue(searchTextProducts.equals(this.getFormElement(Locator.xpath(XPATH_TEXTBOX))));

        log("Now click 'Studies' and validate that the search box is populated as expected.");
        click(Locator.xpath(STUDIES_LINK));
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        Assert.assertTrue(searchTextStudies.equals(this.getFormElement(Locator.xpath(XPATH_TEXTBOX))));

        log("Now click 'Assays' and validate that the search box is populated as expected.");
        click(Locator.xpath(ASSAYS_LINK));
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        Assert.assertTrue(searchTextAssays.equals(this.getFormElement(Locator.xpath(XPATH_TEXTBOX))));

        log("Click 'Study Products' and validate that the search box is populated as expected.");
        click(Locator.xpath(PRODUCTS_LINK));
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        Assert.assertTrue(searchTextProducts.equals(this.getFormElement(Locator.xpath(XPATH_TEXTBOX))));

        log("Now go to a different part of the app and return using the 'Learn about' link. Search values should be saved.");
        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        click(Locator.xpath(LEARN_ABOUT));
        waitForText("Learn about...");
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        log("Validate that the 'Study products' search value is there.");
        Assert.assertTrue(searchTextProducts.equals(this.getFormElement(Locator.xpath(XPATH_TEXTBOX))));

        log("Now click 'Assays' and validate that the search box has the value last searched for in Assays.");
        click(Locator.xpath(ASSAYS_LINK));
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        Assert.assertTrue(searchTextAssays.equals(this.getFormElement(Locator.xpath(XPATH_TEXTBOX))));

        log("Go back to Plots and return using the 'Learn about' link. Search values should be saved and show Assays.");
        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        click(Locator.xpath(LEARN_ABOUT));
        waitForText("Learn about...");
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        log("Validate that the 'Assays' search value is there.");
        Assert.assertTrue(searchTextAssays.equals(this.getFormElement(Locator.xpath(XPATH_TEXTBOX))));

        log("Finally repeat the tests with 'Studies'.");
        click(Locator.xpath(STUDIES_LINK));
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        Assert.assertTrue(searchTextStudies.equals(this.getFormElement(Locator.xpath(XPATH_TEXTBOX))));

        log("Go back to Plots and return using the 'Learn about' link. Search values should be saved and show Studies.");
        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        click(Locator.xpath(LEARN_ABOUT));
        waitForText("Learn about...");
        sleep(CDSHelper.CDS_WAIT_ANIMATION);

        log("Validate that the 'Studies' search value is there.");
        Assert.assertTrue(searchTextStudies.equals(this.getFormElement(Locator.xpath(XPATH_TEXTBOX))));
    }

    @Test
    public void validateStudySummaryDataAvailability()
    {
        final int STUDY_WITH_DATA_AVAILABLE = 25;

        cds.viewLearnAboutPage("Studies");
        assertTextPresent("Not added");

        List<WebElement> hasDataRows = Locator.css(".detail-row-has-data").findElements(getDriver());
        List<WebElement> hasDataIcons = Locator.css(".detail-has-data").findElements(getDriver());
        //hasDataRows is larger than hasDataIcons by a factor of two because of locked columns cause rows to be counted twice.
        Assert.assertTrue(hasDataRows.size()/2 == hasDataIcons.size() && hasDataIcons.size() == STUDY_WITH_DATA_AVAILABLE);
    }

    @Test
    public void validateDetailsDataAvailability()
    {
        final String HAS_DATA_ICON = "smallCheck.png";
        final String HAS_NO_DATA_ICON = "smallGreyX.png";

        //Valuse for Study Details inspection
        final String STUDY = "RED 4";
        final String[] ASSAY_TITLES = {"IFNg ELISpot", "ICS", "BAMA"};

        //Valuse for Assay Details inspection
        final int NUM_STUDY_FROM_ASSAY_WITH_DATA = 14;
        final String STUDY_FROM_ASSAY_WITH_NO_DATA = "ZAP 108";

        //Valuse for Study Products Details inspection
        final String PRODUCT = "benztropine mesylate";
        final String[] STUDY_FROM_PRODUCT = {"QED 1", "YOYO 55"};


        log("Testing data availability module in Studies");
        cds.viewLearnAboutPage("Studies");

        Locator element = Locator.xpath("//tr[contains(@class, 'has-data')]/td/div/div/h2[contains(text(), '" + STUDY + "')]");
        assertElementPresent(element);
        waitAndClick(element);

        waitForText("Data Availability");

        Assert.assertTrue(isElementPresent(getDataRowXPath(ASSAY_TITLES[0]).append("//td//img[contains(@src, '" + HAS_DATA_ICON + "')]")));
        Assert.assertTrue(isElementPresent(getDataRowXPath(ASSAY_TITLES[1]).append("//td//img[contains(@src, '" + HAS_DATA_ICON + "')]")));
        Assert.assertTrue(isElementPresent(getDataRowXPath(ASSAY_TITLES[2]).append("//td//img[contains(@src, '" + HAS_NO_DATA_ICON + "')]")));


        log("Testing data availability module in Assays");
        cds.viewLearnAboutPage("Assays");
        Locator loc = Locator.xpath("//h2[contains(text(), '" + CDSHelper.ICS + "')]");
        waitAndClick(loc);

        refresh(); //ensures only selecting elements on viewable page.

        waitForText("Data Availability");

        List<WebElement> smallHasDataIcons =getDataRowXPath("").append("//td//img[contains(@src, '"  + HAS_DATA_ICON +  "')]").findElements(getDriver());
        Assert.assertTrue(smallHasDataIcons.size() == NUM_STUDY_FROM_ASSAY_WITH_DATA);

        Assert.assertFalse(isElementPresent(getDataRowXPath(STUDY_FROM_ASSAY_WITH_NO_DATA).append("//td//img[contains(@src, '"  + HAS_DATA_ICON +  "')]")));
        Assert.assertTrue(isElementPresent(getDataRowXPath(STUDY_FROM_ASSAY_WITH_NO_DATA).append("//td//img[contains(@src, '" + HAS_NO_DATA_ICON + "')]")));


        log("Testing data availability module in Study Products");
        cds.viewLearnAboutPage("Study products");
        waitAndClick(Locator.xpath("//h2[text() = '" + PRODUCT + "']"));

        refresh();

        waitForText("Data Availability");

        Assert.assertTrue(isElementPresent(getDataRowXPath(STUDY_FROM_PRODUCT[0]).append("//td//img[contains(@src, '" + HAS_DATA_ICON + "')]")));
        Assert.assertTrue(isElementPresent(getDataRowXPath(STUDY_FROM_PRODUCT[1]).append("//td//img[contains(@src, '" + HAS_NO_DATA_ICON + "')]")));
    }

    @Test
    public void validateLearnAboutFiltering()
    {
        LearnGrid learnGrid = new LearnGrid(this);

        cds.viewLearnAboutPage("Studies");

        log("Evaluating sorting...");
        learnGrid.sort("Name & Description");
        List<WebElement> sortedStudyTitles = Locator.tagWithClass("tr", "detail-row").append("/td//div/div/h2").findElements(getDriver());

        scrollIntoView(sortedStudyTitles.get(sortedStudyTitles.size() - 1));
        String titleForLastElement = sortedStudyTitles.get(sortedStudyTitles.size() - 1).getText();
        learnGrid.sort("Name & Description");
        Assert.assertTrue(Locator.tagWithClass("tr", "detail-row").append("/td//div/div/h2").findElements(getDriver())
                .get(0).getText()
                .equals(titleForLastElement));

        log("Evaluating filtering...");
        String[] studiesToFilter = {CDSHelper.STUDIES[0], CDSHelper.STUDIES[7], CDSHelper.STUDIES[20]}; //Arbitrarily chosen
        int numRowsPreFilter = XPATH_RESULT_ROW_TITLE.findElements(getDriver()).size();

        learnGrid.setFacet("Name & Description", studiesToFilter);
        List<WebElement> studyTitlesAfterFilter = Locator.tagWithClass("tr", "detail-row")
                .append("/td//div/div/h2")
                .findElements(getDriver());

        List<String> studiesFiltered =  Arrays.asList(studiesToFilter);
        for(WebElement studyTitlesOnPage : studyTitlesAfterFilter)
        {
            scrollIntoView(studyTitlesOnPage);
            Assert.assertTrue(studiesFiltered.contains(studyTitlesOnPage.getText()));
        }

        log("Evaluating clearing a filter");
        learnGrid.clearFilters("Name & Description");
        int numRowsPostFilter = learnGrid.getRowCount();
        Assert.assertTrue(numRowsPreFilter == numRowsPostFilter && numRowsPostFilter == CDSHelper.STUDIES.length);

        log("Evaluating applying two numeric filters");
        //finds the number of rows that have a date column and assay column that satisfy the following filter
        final String yearToFilter = "2004";
        final String numAssaysToFilter = "1";
        int numRowsSatisfyFilter = Locator.xpath("//tr/td/div/div/div[contains(@class, 'detail-gray-text')]" +
                "[contains(text(), '" + numAssaysToFilter + " Assay')]/../../../following-sibling::" +
                "td/div/div/table/tbody/tr[contains(@class, 'detail-gray-text')]/td[contains(text(), '"+ yearToFilter + "')]")
                .findElements(getDriver()).size();

        learnGrid.setFacet("Status", yearToFilter);
        learnGrid.setFacet("Data Added", numAssaysToFilter);
        numRowsPostFilter = learnGrid.getRowCount();

        Assert.assertTrue(numRowsSatisfyFilter == numRowsPostFilter);

        log("Evaluating persisting to URL");
        refresh();
        sleep(CDSHelper.CDS_WAIT);
        int numRowsPostRefresh = learnGrid.getRowCount();
        Assert.assertTrue(numRowsSatisfyFilter == numRowsPostRefresh);

        learnGrid.clearFilters("Status");
        learnGrid.clearFilters("Data Added");
    }

    @Test
    public void validateMultiFiltering()
    {
        LearnGrid learnGrid = new LearnGrid(this);

        //Test basic functionality of multifacet
        cds.viewLearnAboutPage("Studies");
        learnGrid.setWithOptionFacet("Type", "Species", "Vulcan");
        Assert.assertTrue(1 == learnGrid.getRowCount());

        //Test filter for alt property persists correctly
        refresh();
        sleep(CDSHelper.CDS_WAIT);
        Assert.assertTrue(1 == learnGrid.getRowCount());

        //Test clear doesn't fire for wrong selection in facet panel.
        learnGrid.clearFiltersWithOption("Type", "Type");
        Assert.assertTrue(1 == learnGrid.getRowCount());

        //Test basic clear works
        learnGrid.clearFiltersWithOption("Type", "Species");
        Assert.assertTrue(CDSHelper.STUDIES.length == learnGrid.getRowCount());

        //Test setting two different filters in multifacet
        learnGrid.setWithOptionFacet("Type", "Species", "Human");
        learnGrid.setWithOptionFacet("Type", "Type", "Phase IIB");
        Assert.assertTrue(2 == learnGrid.getRowCount());

        //Test combining filter with another column
        learnGrid.setFacet("Data Added", "2");
        _asserts.verifyEmptyLearnAboutStudyPage();

        //clear all filters and check results are correct in succession.
        learnGrid.clearFilters("Data Added");
        Assert.assertTrue(2 == learnGrid.getRowCount());

        learnGrid.clearFiltersWithOption("Type", "Species");
        Assert.assertTrue(2 == learnGrid.getRowCount());
        learnGrid.clearFiltersWithOption("Type", "Type");
        Assert.assertTrue(CDSHelper.STUDIES.length == learnGrid.getRowCount());
    }

    @Test
    public void validateLinksToStudyGrantDocuments()
    {
        final String PDF01_FILE_NAME = "test%20pdf%201.pdf";
        final String PDF02_FILE_NAME = "test%20pdf%202.pdf";
        final String DOCX_FILE_NAME = "test document 1.docx";
        final int PDF01_STUDY = 1;
        final int DOCX01_STUDY = 0;
        final int PDF02_STUDY01 = 14;
        final int PDF02_STUDY02 = 18;
        final int PDF02_STUDY03 = 11;
        final int BROKEN_LINK_STUDY = 4;
        final String STUDY_INFO_TEXT_TRIGGER = "Study information";

        String studyName;
        Locator studyElement;

        log("Validate a link to a pdf file works as expected.");
        validatePDFLink(CDSHelper.STUDIES[PDF01_STUDY], PDF01_FILE_NAME);

        log("Validate that a link to a doc file works as expected.");
        validateDocLink(CDSHelper.STUDIES[DOCX01_STUDY], DOCX_FILE_NAME);

        log("Validated that a document linked to several studies works as expected.");
        validatePDFLink(CDSHelper.STUDIES[PDF02_STUDY01], PDF02_FILE_NAME);
        validatePDFLink(CDSHelper.STUDIES[PDF02_STUDY02], PDF02_FILE_NAME);
        validatePDFLink(CDSHelper.STUDIES[PDF02_STUDY03], PDF02_FILE_NAME);

        log("Validate a study that has link but the document is not there.");
        pauseJsErrorChecker();
        cds.viewLearnAboutPage("Studies");

        studyName = CDSHelper.STUDIES[BROKEN_LINK_STUDY];
        studyElement = Locator.xpath("//h2[text() = '" + studyName + "']");
        scrollIntoView(studyElement);
        click(studyElement);
        sleep(1000);
        waitForText(STUDY_INFO_TEXT_TRIGGER);

        Assert.assertTrue("There was a visible link to a grant document for this study, and there should not be.", getVisibleGrantDocumentLink() == null);

        resumeJsErrorChecker();

        goToHome();
        log("All done.");

    }

    private void validatePDFLink(String studyName, String pdfFileName)
    {
        final String PLUGIN_XPATH = "//embed[@name='plugin']";
        final String STUDY_XPATH_TEMPLATE = "//h2[text() = '$']";
        final String STUDY_INFO_TEXT_TRIGGER = "Study information";

        String studyXPath;
        Locator studyElement;
        WebElement documentLink;

        cds.viewLearnAboutPage("Studies");

        studyXPath = STUDY_XPATH_TEMPLATE.replace("$", studyName);
        studyElement = Locator.xpath(studyXPath);
        log("Validate that study " + studyName + " has a grant document and is of type pdf.");
        scrollIntoView(studyElement);
        click(studyElement);
        sleep(1000);
        waitForText(STUDY_INFO_TEXT_TRIGGER);

        documentLink = getVisibleGrantDocumentLink();
        Assert.assertTrue("Was not able to find link to the document for study '" + studyName + "'.", documentLink != null);

        log("Now click on the document link.");
        documentLink.click();
        sleep(10000);
        switchToWindow(1);

        log("Validate that the pdf document was loaded into the browser.");
        assertElementPresent("Doesn't look like the embed elment is present.", Locator.xpath(PLUGIN_XPATH), 1);
        Assert.assertTrue("The embedded element is not a pdf plugin", getAttribute(Locator.xpath(PLUGIN_XPATH), "type").toLowerCase().contains("pdf"));
        Assert.assertTrue("The source for the plugin is not the expected document. Expected: '" + pdfFileName + "'.", getAttribute(Locator.xpath(PLUGIN_XPATH), "src").toLowerCase().contains(pdfFileName));

        log("Close this window.");
        getDriver().close();

        log("Go back to the main window.");
        switchToMainWindow();

    }

    private void validateDocLink(String studyName, String docFileName)
    {
        final String STUDY_XPATH_TEMPLATE = "//h2[text() = '$']";
        final String STUDY_INFO_TEXT_TRIGGER = "Study information";

        String studyXPath, foundDocumentName;
        Locator studyElement;
        WebElement documentLink;
        File docFile;

        cds.viewLearnAboutPage("Studies");

        studyXPath = STUDY_XPATH_TEMPLATE.replace("$", studyName);
        studyElement = Locator.xpath(studyXPath);
        log("Validate that study " + studyName + " has a grant document and is of type docx.");
        scrollIntoView(studyElement);
        click(studyElement);
        sleep(1000);
        waitForText(STUDY_INFO_TEXT_TRIGGER);

        documentLink = getVisibleGrantDocumentLink();
        Assert.assertTrue("Was not able to find link to the document for study '" + studyName + "'.", documentLink != null);

        log("Now click on the document link.");
        docFile = clickAndWaitForDownload(documentLink);
        foundDocumentName = docFile.getName();
        Assert.assertTrue("Downloaded document not of the expected name. Expected: '" + docFileName + "' Found: '" + foundDocumentName.toLowerCase() + "'.", docFile.getName().toLowerCase().contains(docFileName));

    }

    // Return the visible grant document link, null otherwise.
    private WebElement getVisibleGrantDocumentLink()
    {
        final String DOCUMENT_LINK_XPATH = "//td[@class='item-label'][text()='Grant Affiliation:']/following-sibling::td//a";
        WebElement documentLinkElement = null;


        for(WebElement we : Locator.xpath(DOCUMENT_LINK_XPATH).findElements(getDriver()))
        {
            if(we.isDisplayed())
            {
                documentLinkElement = we;
                break;
            }
        }

        return documentLinkElement;
    }

    //Helper function for data availability tests
    private Locator.XPathLocator getDataRowXPath(String rowText)
    {
        return Locator.xpath("//tr[contains(@class,'item-row')]/td/a[contains(text(), '" + rowText + "')]").parent().parent();
    }

    private void validateSearchFor(String searchString)
    {
        String itemText;
        String[] itemParts;
        List<WebElement> returnedItems;

        log("Searching for '" + searchString + "'.");
        this.setFormElement(Locator.xpath(XPATH_TEXTBOX), searchString);
        sleep(CDSHelper.CDS_WAIT);  // Same elements are reused between searched, this sleep prevents a "stale element" error.
        returnedItems = XPATH_RESULT_ROW_TITLE.findElements(getDriver());
        log("Found " + returnedItems.size() + " items.");

        for (WebElement listItem : returnedItems)
        {
            itemText = listItem.getText();
            itemParts = itemText.split("\n");
            log("Looking at study " + itemParts[0]);
            assert(itemText.toLowerCase().contains(searchString.toLowerCase()));
        }

    }

    private void validateToolTipText(String toolTipText, String... expectedText)
    {
        for(String expected : expectedText)
        {
            Assert.assertTrue("Tool tip did not contain text: '" + expected + "'. Found: '" + toolTipText + "'.", toolTipText.trim().toLowerCase().contains(expected.trim().toLowerCase()));
        }
    }

    private void validateToolTip(WebElement el, String toolTipExpected)
    {
        log("Hover over the link with text '" + el.getText() + "' to validate that the tooltip is shown.");
        String toolTipText;

        Assert.assertTrue("Tooltip for '" + el.getText() + "' didn't show. Show yourself coward!", triggerToolTip(el));
        log("It looks like a tooltip was shown for '" + el.getText()+ "'.");

        toolTipText = getToolTipText();

        validateToolTipText(toolTipText, toolTipExpected);

    }

    private boolean triggerToolTip(WebElement el)
    {
        int elWidth = el.getSize().getWidth();
        int elHeight = el.getSize().getHeight();
        boolean bubblePresent = false;

        Actions builder = new Actions(getDriver());

        for(int i = -10; i <= elWidth && i <= elHeight && !bubblePresent; i++)
        {
            sleep(250); // Wait a moment.
            builder.moveToElement(el, i, i).build().perform();
            bubblePresent = isElementPresent(Locator.css("div.hopscotch-bubble-container"));
        }

        return bubblePresent;
    }

    private String getToolTipText()
    {
        return getText(Locator.css("div.hopscotch-bubble-container div.hopscotch-bubble-content div.hopscotch-content"));
    }
}
