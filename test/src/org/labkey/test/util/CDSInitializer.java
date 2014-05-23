package org.labkey.test.util;

import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;

public class CDSInitializer
{
    private final BaseWebDriverTest _test;
    private final CDSHelper _cds;
    private final String _project;

    public CDSInitializer(BaseWebDriverTest test, String projectName)
    {
        _test = test;
        _cds = new CDSHelper(_test);
        _project = projectName;
    }

    @LogMethod(category = LogMethod.MethodType.SETUP)
    public void setupDataspace()
    {
        setupProject();
        importData();
        populateFactTable();

        // TODO: Re-enable this check once the verify query has been fixed and re-linked in the management webpart
//        initTest.verifyFactTable();

        preCacheCube();
    }

    @LogMethod
    private void preCacheCube()
    {
        _cds.enterApplication();
        _test.waitForElement(CDSHelper.Locators.getByLocator("Studies"));
        _test.goToProjectHome();
    }

    @LogMethod
    public void setupProject()
    {
        _test._containerHelper.createProject(_project, "Dataspace");
        _test.enableModule(_project, "CDS");
        _test.goToManageStudy();
        _test.clickAndWait(Locator.linkWithText("Change Study Properties"));
        _test.waitForElement(Ext4Helper.Locators.radiobutton(_test, "DATE"));
        _test._ext4Helper.selectRadioButton("DATE");
        //We need to set the root study name to blank to hide it from mondrian (issue 19996)
        _test.setFormElement(Locator.name("Label"), "");
        _test.clickButton("Submit");

        _test.goToProjectHome();
    }

    @LogMethod
    public void importData()
    {
        importComponentStudy(CDSHelper.STUDIES[0]);
        importComponentStudy("NotCHAVI001");
        importComponentStudy(CDSHelper.STUDIES[2]);
        importComponentStudy(CDSHelper.STUDIES[3]);

        //Can't add web part until we actually have the datasets imported above
        _test.clickProject(_project);
        PortalHelper portalHelper = new PortalHelper(_test);
        portalHelper.addWebPart("CDS Management");

        importCDSData("Antigens", "antigens.tsv");
        importCDSData("Sites", "sites.tsv");
        importCDSData("People", "people.tsv");
        importCDSData("Citable", "citable.tsv");
        importCDSData("Citations", "citations.tsv");
        importCDSData("AssayPublications", "assay_publications.tsv");
        importCDSData("Vaccines", "vaccines.tsv");
        importCDSData("VaccineComponents", "vaccinecomponents.tsv");
    }

    @LogMethod
    private void importComponentStudy(String studyName)
    {
        _test._containerHelper.createSubfolder(_project, studyName, "Study");
        _test.importStudyFromZip(BaseWebDriverTest.getSampleData(studyName + ".folder.zip"), true, true);
    }

    @LogMethod
    private void importCDSData(String query, String dataFilePath)
    {
        _test.goToProjectHome();
        _test.waitForTextWithRefresh("Fact Table", _test.defaultWaitForPage * 4);  //wait for study to fully load
        _test.clickAndWait(Locator.linkWithText(query));
        _test._listHelper.clickImportData();

        _test.setFormElementJS(Locator.id("tsv3"), BaseWebDriverTest.getFileContents(BaseWebDriverTest.getSampleData(dataFilePath)));
        _test.clickButton("Submit");
    }

    @LogMethod
    public void populateFactTable()
    {
        _test.goToProjectHome();
        _test.clickAndWait(Locator.linkWithText("Populate Fact Table"));
        _test.uncheckCheckbox(Locator.checkboxByNameAndValue("dataset", "HIV Test Results"));
        _test.uncheckCheckbox(Locator.checkboxByNameAndValue("dataset", "Physical Exam"));
        _test.uncheckCheckbox(Locator.checkboxByNameAndValue("dataset", "Lab Results"));
        _test.uncheckCheckbox(Locator.checkboxByNameAndValue("dataset", "ParticipantTreatments"));
        _test.submit();

        _test.assertElementPresent(Locator.linkWithText("NAb"));
        _test.assertElementPresent(Locator.linkWithText("Luminex"));
        _test.assertElementPresent(Locator.linkWithText("MRNA"));
        _test.assertElementPresent(Locator.linkWithText("ADCC"));
    }
}